/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import database.EmployeeDBV;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import static service.commons.Constants.ACTION;
import service.commons.ServiceVerticle;
import utils.UtilsResponse;
import utils.UtilsSecurity;
import utils.UtilsValidation;
import static utils.UtilsValidation.*;
import static database.EmployeeDBV.ACTION_ASSIGN_PROFILES;
import static utils.UtilsResponse.responsePropertyValue;

/**
 *
 * Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class EmployeeSV extends ServiceVerticle {

    @Override
    protected String getDBAddress() {
        return EmployeeDBV.class.getSimpleName();
    }

    @Override
    protected String getEndpointAddress() {
        return "/employees";
    }

    @Override
    protected boolean isValidUpdateData(RoutingContext context) {
        try {
            JsonObject body = context.getBodyAsJson();
            isName(body, "name");
            isMail(body, "email");
            isPhoneNumber(body, "phone");
            body.remove("pass");
            context.setBody(body.toBuffer());
        } catch (UtilsValidation.PropertyValueException ex) {
            return UtilsResponse.responsePropertyValue(context, ex);
        }
        return super.isValidUpdateData(context);
    }

    @Override
    protected boolean isValidCreateData(RoutingContext context) {
        try {
            JsonObject body = context.getBodyAsJson();
            isNameAndNotNull(body, "name");
            isMailAndNotNull(body, "email");
            isPhoneNumber(body, "phone");
            body.put("pass", UtilsSecurity.encodeSHA256(body.getString("pass")));
            context.setBody(body.toBuffer());
        } catch (UtilsValidation.PropertyValueException ex) {
            return UtilsResponse.responsePropertyValue(context, ex);
        }
        return super.isValidCreateData(context); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        this.router.get("/profiles/:id").handler(this::profiles);
        this.router.post("/assignProfiles").handler(BodyHandler.create());
        this.router.post("/assignProfiles").handler(this::assignProfiles);
    }

    private void profiles(RoutingContext context) {
        this.validateToken(context, __ -> {
            this.vertx.eventBus().send(
                    this.getDBAddress(),
                    new JsonObject().put("id", Integer.parseInt(context.request().getParam("id"))),
                    new DeliveryOptions().addHeader(ACTION, EmployeeDBV.ACTION_PROFILES),
                    r -> {
                        this.genericResponse(context, r);
                    }
            );
        }
        );
    }

    private void assignProfiles(RoutingContext context) {
        this.validateToken(context, __ -> {
            JsonObject body = context.getBodyAsJson();
            try {
                isGraterAndNotNull(body, "employee_id", 0);
                isEmptyAndNotNull(body.getJsonArray("profiles"), "profiles");

                this.vertx.eventBus().send(
                        this.getDBAddress(),
                        body,
                        this.options(ACTION_ASSIGN_PROFILES),
                        r -> {
                            this.genericResponse(context, r, "Profiles assigned");
                        });
            } catch (PropertyValueException ex) {
                responsePropertyValue(context, ex);
            }
        });
    }

}
