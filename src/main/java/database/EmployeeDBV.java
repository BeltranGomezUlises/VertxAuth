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
 * Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class EmployeeDBV extends DBVerticle {

    public static final String ACTION_LOGIN = "EmployeeDBV.login";
    public static final String ACTION_FIND_BY_MAIL = "EmployeeDBV.findByMail";
    public static final String ACTION_UPDATE_PASSWORD = "EmployeeDBV.updatePassword";

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
            case ACTION_FIND_BY_MAIL:
                this.findByMail(message);
                break;
            case ACTION_UPDATE_PASSWORD:
                this.updatePassword(message);
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

    private void findByMail(Message<JsonObject> message) {
        JsonArray params = new JsonArray()
                .add(message.body().getString("email"));
        this.dbClient.queryWithParams(QUERY_FIND_BY_MAIL, params, reply -> {
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

    private void updatePassword(Message<JsonObject> message) {
        JsonArray params = new JsonArray()
                .add(message.body().getString("new_password"))
                .add(message.body().getString("employee_email"));
        this.dbClient.updateWithParams(QUERY_UPDATE_PASSWORD, params, reply -> {
            if (reply.succeeded()) {
                message.reply(null);
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

    private static final String QUERY_FIND_BY_MAIL = "SELECT\n"
            + "	id, name, email \n"
            + "FROM\n"
            + "	employee\n"
            + "WHERE\n"
            + "	email = ?";

    private static final String QUERY_UPDATE_PASSWORD = "UPDATE\n"
            + "	employee\n"
            + "SET\n"
            + "	pass = ?\n"
            + "WHERE\n"
            + "	email = ?";
//</editor-fold>

}
