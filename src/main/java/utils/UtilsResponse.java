/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import models.ModelReponse;
import static models.ModelReponse.Status.*;
import models.PropertyError;
import static service.commons.Constants.INVALID_DATA;
import static service.commons.Constants.INVALID_DATA_MESSAGE;

/**
 * Utils class for redundant presentation of the responses in http requests, use this to encapsulate data and messages into a generic model
 *
 * @author Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class UtilsResponse {

    public static void responseOk(RoutingContext context, String message, String devMessage, Object data) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        if (data == null) {
            response.end(Json.encode(new ModelReponse(WARNING, "Element not found")));
        } else {
            response.end(Json.encode(new ModelReponse(OK, message, devMessage, data)));
        }
    }

    public static void responseOk(RoutingContext context, String message, Object data) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        if (data == null) {
            response.end(Json.encode(new ModelReponse(WARNING, "Element not found")));
        } else {
            response.end(Json.encode(new ModelReponse(OK, message, data)));
        }
    }

    public static void responseOk(RoutingContext context, Object data) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        if (data == null) {
            response.end(Json.encode(new ModelReponse(WARNING, "Element not found")));
        } else {
            response.end(Json.encode(new ModelReponse(OK, data)));
        }
    }

    public static void responseOk(RoutingContext context, Object data, String devMessage) {
        ModelReponse res = new ModelReponse(OK);
        res.setData(data);
        res.setDevMessage(devMessage);
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        if (data == null) {
            response.end(Json.encode(new ModelReponse(WARNING, "Element not found")));
        } else {
            response.end(Json.encode(res));
        }
    }

    public static void responseOk(RoutingContext context, String message) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(OK, message)));
    }

    public static void responseWarning(RoutingContext context, String message) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(WARNING, message)));
    }

    public static void responseWarning(RoutingContext context, String message, String devMessage) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(WARNING, message, devMessage)));
    }

    public static void responseWarning(RoutingContext context, String message, String devMessage, Object data) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(WARNING, message, devMessage, data)));
    }

    public static void responseWarning(RoutingContext context, String message, Object data) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(WARNING, message, data)));
    }

    public static void responseWarning(RoutingContext context, Object data) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(WARNING, data)));
    }

    public static void responseWarning(RoutingContext context, Object data, String devMessage) {
        ModelReponse res = new ModelReponse(WARNING);
        res.setData(data);
        res.setDevMessage(devMessage);
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(res));
    }

    public static boolean responsePropertyValue(RoutingContext context, UtilsValidation.PropertyValueException ex) {
        responseWarning(context, INVALID_DATA, INVALID_DATA_MESSAGE, new PropertyError(ex.getName(), ex.getError()));
        return false;
    }

    public static void responseInvalidToken(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(INVALID_TOKEN, "Out of session", "the json web token in authorization header is invalid")));
    }

    public static void responseError(RoutingContext context, String message, String devMessage, Object data) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(ERROR, message, devMessage, data)));
    }

    public static void responseError(RoutingContext context, String message, Object data) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(ERROR, message, data)));
    }

    public static void responseError(RoutingContext context, Object data) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(ERROR, data)));
    }

    public static void responseError(RoutingContext context, Object data, String devMessage) {
        ModelReponse res = new ModelReponse(ERROR);
        res.setData(data);
        res.setDevMessage(devMessage);
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(res));
    }

    public static void responseError(RoutingContext context, String message) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(ERROR, "Ocurrió un error inesperado, consulte con el proveedor de sistemas", message)));
    }

    public static void responseError(RoutingContext context, String message, String devMessage) {
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(Json.encode(new ModelReponse(ERROR, message, devMessage)));
    }

}
