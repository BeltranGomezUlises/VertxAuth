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
import static service.commons.Constants.ACTION;

import static service.commons.Constants.CONFIG_HTTP_SERVER_PORT;
import static service.commons.Constants.UNEXPECTED_ERROR;
import utils.UtilsJWT;
import utils.UtilsResponse;
import static utils.UtilsResponse.responseError;
import utils.UtilsRouter;
import utils.UtilsSecurity;

/**
 *
 * @author ulises
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
                    UtilsResponse.responseWarning(context, "User and/or password invalid");
                }else{
                    JsonObject result = reply.result().body();
                    
                    UtilsResponse.responseOk(context, result);
                }
            } else {
                responseError(context, UNEXPECTED_ERROR, reply.cause().getMessage());
            }
        });

    }

    private void validToken(RoutingContext context) {

    }

    private void refreshToken(RoutingContext context) {

    }

    private void recoverPass(RoutingContext context) {

    }

    private void restorePass(RoutingContext context) {

    }

}
