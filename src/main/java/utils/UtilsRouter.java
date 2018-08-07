/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import java.util.HashSet;
import java.util.Set;

/**
 * Singleton class for main router
 *
 * @author Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class UtilsRouter {

    private static Router router;
    private static final Set<String> ALLOWD_HEADERS = new HashSet<>();
    private static final Set<HttpMethod> ALLOWED_METHODS = new HashSet<>();

    static {
        ALLOWD_HEADERS.add("x-requested-with");
        ALLOWD_HEADERS.add("Access-Control-Allow-Origin");
        ALLOWD_HEADERS.add("origin");
        ALLOWD_HEADERS.add("Content-Type");
        ALLOWD_HEADERS.add("accept");
        ALLOWD_HEADERS.add("X-PINGARUNER");
        ALLOWD_HEADERS.add("authorization");

        ALLOWED_METHODS.add(HttpMethod.GET);
        ALLOWED_METHODS.add(HttpMethod.POST);
        ALLOWED_METHODS.add(HttpMethod.OPTIONS);
        ALLOWED_METHODS.add(HttpMethod.DELETE);
        ALLOWED_METHODS.add(HttpMethod.PATCH);
        ALLOWED_METHODS.add(HttpMethod.PUT);
    }

    public static Router getInstance(Vertx vertx) {
        if (router == null) {
            router = Router.router(vertx);
            router.route().handler(CorsHandler.create("*").allowedHeaders(ALLOWD_HEADERS).allowedMethods(ALLOWED_METHODS));
        }
        return router;
    }

}
