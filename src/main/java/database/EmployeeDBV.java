/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import database.commons.DBVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import static service.commons.Constants.ACTION;

/**
 *
 * @author ulises
 */
public class EmployeeDBV extends DBVerticle {

    public static final String ACTION_LOGIN = "EmployeeDBV.login";

    @Override
    public String getTableName() {
        return "employee";
    }

    @Override
    protected void onMessage(Message<JsonObject> message) {
        super.onMessage(message);
        switch (message.headers().get(ACTION)) {
            case ACTION_LOGIN:
                this.login(message);
                break;
        }
    }

    private void login(Message<JsonObject> message) {
        JsonArray params = new JsonArray()
                .add(message.body().getString("email"))
                .add(message.body().getString("pass"));
        this.dbClient.queryWithParams(QUERY_LOGIN, params, reply -> {
            if (reply.succeeded()) {                
                if (reply.result().getNumRows() == 0) {
                    message.reply(null);
                } else {
                    message.reply(reply.result().getRows().get(0));
                }
            } else {
                reportQueryError(message, reply.cause());
            }
        });
    }

//<editor-fold defaultstate="collapsed" desc="queries">
    private static final String QUERY_LOGIN = "SELECT\n"
            + "	id,\n"
            + "	name,\n"
            + "	phone,\n"
            + "	email\n"
            + "FROM\n"
            + "	employee\n"
            + "WHERE\n"
            + "	email = ?\n"
            + "	AND pass = ?";
//</editor-fold>
}
