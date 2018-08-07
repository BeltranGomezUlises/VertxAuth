/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.commons;

import io.vertx.core.json.JsonArray;

/**
 *
 * @author Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class GenericCreate {

    private final String query;
    private final JsonArray params;

    public GenericCreate(String query, JsonArray params) {
        this.query = query;
        this.params = params;
    }

    public String getQuery() {
        return query;
    }

    public JsonArray getParams() {
        return params;
    }

}
