/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import database.EmployeeDBV;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import service.commons.ServiceVerticle;
import utils.UtilsResponse;
import utils.UtilsSecurity;
import utils.UtilsValidation;
import static utils.UtilsValidation.*;

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

}
