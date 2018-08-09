/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import database.PermissionDBV;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import static service.commons.Constants.ACTION;
import service.commons.ServiceVerticle;
import utils.UtilsJWT;
import utils.UtilsResponse;
import utils.UtilsValidation;

/**
 *
 * Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class PermissionSV extends ServiceVerticle {

    @Override
    protected String getDBAddress() {
        return PermissionDBV.class.getSimpleName();
    }

    @Override
    protected String getEndpointAddress() {
        return "/permissions";
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        this.router.get("/profile/:id").handler(this::profilePermissions);
        this.router.get("/employee/:id").handler(this::employeePermissions);
        this.router.post("/assign/employee").handler(BodyHandler.create());
        this.router.post("/assign/employee").handler(this::assignEmployeePermissions);
        this.router.post("/assign/profile").handler(BodyHandler.create());
        this.router.post("/assign/profile").handler(this::assingProfilePermissions);

    }

    private void profilePermissions(RoutingContext context) {
        String token = context.request().headers().get("Authorization");
        if (UtilsJWT.isAccessTokenValid(token)) {
            try {
                Integer profileId = Integer.parseInt(context.request().getParam("id"));
                JsonObject send = new JsonObject()
                        .put("id", profileId);

                this.vertx.eventBus().send(
                        getDBAddress(),
                        send,
                        options(PermissionDBV.ACTION_PROFILE_PERMISSIONS),
                        reply -> {
                            this.genericResponse(context, reply);
                        });
            } catch (Exception e) {
                UtilsResponse.responsePropertyValue(
                        context,
                        new UtilsValidation.PropertyValueException("id", UtilsValidation.MISSING_REQUIRED_VALUE));
            }
        } else {
            UtilsResponse.responseInvalidToken(context);
        }
    }

    private void employeePermissions(RoutingContext context) {
        String token = context.request().headers().get("Authorization");
        if (UtilsJWT.isAccessTokenValid(token)) {
            try {
                Integer employeeId = Integer.parseInt(context.request().getParam("id"));
                JsonObject send = new JsonObject()
                        .put("id", employeeId);
                this.vertx.eventBus().send(
                        getDBAddress(),
                        send,
                        options(PermissionDBV.ACTION_EMPLOYEE_PERMISSIONS),
                        reply -> {
                            this.genericResponse(context, reply);
                        });
            } catch (Exception e) {
                UtilsResponse.responsePropertyValue(
                        context,
                        new UtilsValidation.PropertyValueException("id", UtilsValidation.MISSING_REQUIRED_VALUE));
            }
        } else {
            UtilsResponse.responseInvalidToken(context);
        }
    }

    private void assignEmployeePermissions(RoutingContext context) {
        String token = context.request().headers().get("Authorization");
        if (UtilsJWT.isAccessTokenValid(token)) {
            try {
                JsonObject body = context.getBodyAsJson();
                UtilsValidation.isGraterAndNotNull(body, "employee_id", 0);
                UtilsValidation.isEmptyAndNotNull(body.getJsonArray("permissions"), "permissions");
                this.vertx.eventBus().send(
                        getDBAddress(),
                        body,
                        options(PermissionDBV.ACTION_ASSIGN_EMPLOYEE_PERMISSIONS),
                        reply -> {
                            this.genericResponse(context, reply, "Permissions assigend");
                        });
            } catch (UtilsValidation.PropertyValueException ex) {
                UtilsResponse.responsePropertyValue(context, ex);
            }

        } else {
            UtilsResponse.responseInvalidToken(context);
        }
    }

    private void assingProfilePermissions(RoutingContext context) {
        String token = context.request().headers().get("Authorization");
        if (UtilsJWT.isAccessTokenValid(token)) {
            try {
                JsonObject body = context.getBodyAsJson();
                UtilsValidation.isGraterAndNotNull(body, "profile_id", 0);
                UtilsValidation.isEmptyAndNotNull(body.getJsonArray("permissions"), "permissions");
                this.vertx.eventBus().send(
                        getDBAddress(),
                        body,
                        options(PermissionDBV.ACTION_ASSIGN_PROFILE_PERMISSIONS),
                        reply -> {
                            this.genericResponse(context, reply, "Permissions assigend");
                        });
            } catch (UtilsValidation.PropertyValueException ex) {
                UtilsResponse.responsePropertyValue(context, ex);
            }
        } else {
            UtilsResponse.responseInvalidToken(context);
        }
    }

}
