/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import database.EmployeeDBV;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Random;
import static service.commons.Constants.ACTION;
import static service.commons.Constants.AUTHORIZATION;
import static service.commons.Constants.CONFIG_HTTP_SERVER_PORT;
import static service.commons.Constants.UNEXPECTED_ERROR;
import utils.UtilsJWT;
import utils.UtilsJWT.RefreshException;
import utils.UtilsResponse;
import static utils.UtilsResponse.responseError;
import static utils.UtilsResponse.responseOk;
import static utils.UtilsResponse.responsePropertyValue;
import static utils.UtilsResponse.responseWarning;
import utils.UtilsRouter;
import utils.UtilsSecurity;
import utils.UtilsValidation;

/**
 *
 * Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class AuthVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        super.start();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.post("/login").handler(BodyHandler.create());
        router.post("/login").handler(this::login);
        router.get("/validAccessToken/:token").handler(this::validToken);
        router.post("/refreshToken").handler(BodyHandler.create());
        router.post("/refreshToken").handler(this::refreshToken);
        router.post("/recoverPass").handler(BodyHandler.create());
        router.post("/recoverPass").handler(this::recoverPass);
        router.post("/restorePass").handler(BodyHandler.create());
        router.post("/restorePass").handler(this::restorePass);
        router.post("/resetPass").handler(BodyHandler.create());
        router.post("/resetPass").handler(this::resetPass);
        UtilsRouter.getInstance(vertx).mountSubRouter("/auth", router);
        Integer portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT);
        if (portNumber == null) {
            System.out.println("Could not start a HTTP server" + this.getClass().getSimpleName() + ", no port speficied in configuration");
        }
        server.requestHandler(UtilsRouter.getInstance(vertx)::accept)
                .listen(portNumber, ar -> {
                    if (ar.succeeded()) {
                        System.out.println(this.getClass().getSimpleName() + " running");
                    } else {
                        System.out.println("Could not start a HTTP server " + this.getClass().getSimpleName() + ", " + ar.cause());
                    }
                });
    }

    private void login(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        String email = body.getString("email");
        String pass = UtilsSecurity.encodeSHA256(body.getString("pass"));
        JsonObject send = new JsonObject()
                .put("email", email)
                .put("pass", pass);
        DeliveryOptions options = new DeliveryOptions().addHeader(ACTION, EmployeeDBV.ACTION_LOGIN);
        this.vertx.eventBus().send(EmployeeDBV.class.getSimpleName(), send, options,
                (AsyncResult<Message<JsonObject>> reply) -> {
                    if (reply.succeeded()) {
                        if (reply.result().body() == null) {
                            UtilsResponse.responseWarning(context, "User and/or password are invalid");
                        } else {
                            JsonObject result = reply.result().body();
                            result.put("accessToken", UtilsJWT.generateAccessToken(result.getInteger("id")))
                                    .put("refreshToken", UtilsJWT.generateRefreshToken(result.getInteger("id")));
                            UtilsResponse.responseOk(context, result);
                        }
                    } else {
                        responseError(context, UNEXPECTED_ERROR, reply.cause().getMessage());
                    }
                });

    }

    private void validToken(RoutingContext context) {
        String token = context.request().getParam("token");
        if (UtilsJWT.isAccessTokenValid(token)) {
            UtilsResponse.responseOk(context, "valid");
        } else {
            UtilsResponse.responseWarning(context, "not valid");
        }
    }

    private void refreshToken(RoutingContext context) {
        try {
            JsonObject body = context.getBodyAsJson();
            JsonObject newAccessToken = UtilsJWT.refreshToken(body.getString("refreshToken"), body.getString("accessToken"));
            UtilsResponse.responseOk(context, new JsonObject().put("newAccessToken", newAccessToken));
        } catch (RefreshException ex) {
            UtilsResponse.responseWarning(context, ex.getMessage(), ex.getRefreshProblem());
        } catch (Exception ex) {
            UtilsResponse.responseWarning(context, ex.getMessage());
        }
    }

    private void recoverPass(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        this.vertx.eventBus().send(
                EmployeeDBV.class.getSimpleName(),
                body, new DeliveryOptions().addHeader(ACTION, EmployeeDBV.ACTION_FIND_BY_MAIL),
                (AsyncResult<Message<JsonObject>> reply) -> {
                    if (reply.succeeded()) {
                        if (reply.result().body() != null) {
                            Random r = new Random();
                            //generar codigo de 8 digitos aleatorios
                            String code = String.valueOf(r.nextInt(99));
                            code += String.valueOf(r.nextInt(99));
                            code += String.valueOf(r.nextInt(99));
                            code += String.valueOf(r.nextInt(99));

                            final String employeeMail = reply.result().body().getString("email");
                            final String recoverCode = code;

                            JsonObject send = new JsonObject()
                                    .put("employee_email", employeeMail)
                                    .put("employee_name", reply.result().body().getString("name"))
                                    .put("recover_code", recoverCode);

                            this.vertx.eventBus().send(MailVerticle.class.getSimpleName(), send,
                                    new DeliveryOptions()
                                            .addHeader(ACTION, MailVerticle.ACTION_SEND_RECOVER_PASS),
                                    mailReply -> {
                                        if (mailReply.succeeded()) {
                                            String jws = UtilsJWT.generateRecoverPasswordToken(recoverCode, employeeMail);
                                            responseOk(context, new JsonObject()
                                                    .put("recover_token", jws), "Mail with code sended");
                                        } else {
                                            responseWarning(context, "can't send mail",
                                                    mailReply.cause().getMessage());
                                        }
                                    });
                        } else {
                            responseWarning(context, "Employee not found");
                        }
                    } else {
                        responseWarning(context, "can't found employee", reply.cause().getMessage());
                    }
                }
        );
    }

    private void restorePass(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        String recoverCode = body.getString("recoverCode");
        String recoverToken = body.getString("recoverToken");
        String newPassword = body.getString("newPassword");
        UtilsJWT.RecoverValidation validation = UtilsJWT.isRecoverTokenMatching(recoverToken, recoverCode);
        if (validation.isValid()) {
            JsonObject send = new JsonObject()
                    .put("employee_email", validation.getEmployeeMail())
                    .put("new_password", UtilsSecurity.encodeSHA256(newPassword));
            DeliveryOptions options = new DeliveryOptions().addHeader(ACTION, EmployeeDBV.ACTION_UPDATE_PASSWORD);
            this.vertx.eventBus().send(EmployeeDBV.class.getSimpleName(), send, options, reply -> {
                if (reply.succeeded()) {
                    responseOk(context, "Password restored");
                } else {
                    responseError(context, reply.cause().getMessage());
                }
            });
        } else {
            UtilsResponse.responseWarning(context, "Recover code or recover token are not matching");
        }
    }

    private void resetPass(RoutingContext context) {
        String token = context.request().headers().get(AUTHORIZATION);
        if (UtilsJWT.isAccessTokenValid(token)) {
            JsonObject body = context.getBodyAsJson();
            try {
                UtilsValidation.isGraterEqualAndNotNull(body, "employee_id", 0);
                UtilsValidation.isEmptyAndNotNull(body, "actual_pass");
                UtilsValidation.isEmptyAndNotNull(body, "new_pass");
                int employeeId = UtilsJWT.getEmployeeIdFrom(token);
                String actualPass = body.getString("actual_pass");
                String newPass = body.getString("new_pass");
                if (newPass.equals(actualPass)) {
                    responseWarning(context, "The new password can not be equal to the previous one");
                    return;
                }
                String actualPassEncoded = UtilsSecurity.encodeSHA256(actualPass);
                String newPassEncoded = UtilsSecurity.encodeSHA256(newPass);

                DeliveryOptions options = new DeliveryOptions()
                        .addHeader(ACTION, EmployeeDBV.ACTION_VALIDATE_PASS);

                this.vertx.eventBus().send(EmployeeDBV.class.getSimpleName(),
                        new JsonObject()
                                .put("employee_id", employeeId)
                                .put("pass", actualPassEncoded),
                        options,
                        reply -> {
                            if (reply.succeeded()) {
                                //update pass
                                DeliveryOptions optionsUpdate = new DeliveryOptions()
                                        .addHeader(ACTION, EmployeeDBV.ACTION_RESET_PASS);
                                this.vertx.eventBus().send(EmployeeDBV.class.getSimpleName(),
                                        new JsonObject()
                                                .put("employee_id", employeeId)
                                                .put("pass", newPassEncoded),
                                        optionsUpdate,
                                        updateReply -> {
                                            if (updateReply.succeeded()) {
                                                responseOk(context, "Pass reseted");
                                            } else {
                                                responseError(context, updateReply.cause().getMessage());
                                            }
                                        });
                            } else {
                                responseWarning(context, "Invalid actual password");
                            }
                        });
            } catch (UtilsValidation.PropertyValueException ex) {
                responsePropertyValue(context, ex);
            }
        } else {
            UtilsResponse.responseInvalidToken(context);
        }
    }

}
