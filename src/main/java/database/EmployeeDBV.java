/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import database.commons.DBVerticle;
import static database.commons.ErrorCodes.DB_ERROR;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import static service.commons.Constants.ACTION;

/**
 *
 * Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class EmployeeDBV extends DBVerticle {

    public static final String ACTION_LOGIN = "EmployeeDBV.login";
    public static final String ACTION_FIND_BY_MAIL = "EmployeeDBV.findByMail";
    public static final String ACTION_UPDATE_PASSWORD = "EmployeeDBV.updatePassword";
    public static final String ACTION_PROFILES = "EmployeeDBV.profiles";
    public static final String ACTION_ASSIGN_PROFILES = "EmployeeDBV.assignProfiles";
    public static final String ACTION_VALIDATE_PASS = "EmployeeDBV.validatePass";
    public static final String ACTION_RESET_PASS = "EmployeeDBV.resetPass";

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
            case ACTION_PROFILES:
                this.profiles(message);
                break;
            case ACTION_ASSIGN_PROFILES:
                this.assignProfiles(message);
                break;
            case ACTION_VALIDATE_PASS:
                this.validatePass(message);
                break;
            case ACTION_RESET_PASS:
                this.resetPass(message);
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

    private void profiles(Message<JsonObject> message) {
        int id = message.body().getInteger("id");
        this.dbClient.queryWithParams(QUERY_EMPLOYEE_PROFILES, new JsonArray().add(id), r -> {
            this.genericResponse(message, r);
        });
    }

    private void assignProfiles(Message<JsonObject> message) {
        JsonObject body = message.body();
        int id = body.getInteger("employee_id");
        List<String> batch = new ArrayList<>();
        List<String> values = new ArrayList<>();
        batch.add(QUERY_DELETE_EMPLOYEE_PROFILE + id);
        JsonArray profiles = body.getJsonArray("profiles");
        for (int i = 0; i < profiles.size(); i++) {
            int profile = profiles.getInteger(i);
            values.add("(" + id + "," + profile + ")");
        }
        batch.add(QUERY_INSERT_EMPLOYEE_PROFILE + String.join(",", values));
        this.startTransaction(message, con -> {
            con.batch(batch, reply -> {
                if (reply.succeeded()) {
                    this.commit(con, message, null);
                } else {
                    this.rollback(con, reply.cause(), message);
                }
            });
        });
    }

    private void validatePass(Message<JsonObject> message) {
        JsonObject body = message.body();
        int id = body.getInteger("employee_id");
        String pass = body.getString("pass");
        JsonArray params = new JsonArray().add(id).add(pass);
        this.dbClient.queryWithParams(QUERY_VALITE_PASS, params, reply -> {
            if (reply.succeeded()) {
                if (!reply.result().getResults().isEmpty()) {
                    message.reply(null);
                } else {
                    message.fail(0, "");
                }
            } else {
                message.fail(DB_ERROR.ordinal(), reply.cause().getMessage());
            }
        });
    }

    private void resetPass(Message<JsonObject> message) {
        JsonObject body = message.body();
        int id = body.getInteger("employee_id");
        String newPass = body.getString("pass");
        JsonArray params = new JsonArray().add(newPass).add(id);
        this.dbClient.queryWithParams(QUERY_RESET_PASS, params, reply -> {
            if (reply.succeeded()) {
                message.reply("updated");
            } else {
                message.fail(DB_ERROR.ordinal(), reply.cause().getMessage());
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
    private static final String QUERY_EMPLOYEE_PROFILES = "SELECT "
            + "profile_id, "
            + "p.name, "
            + "p.description "
            + "FROM employee_profile\n"
            + "JOIN profile p ON p.id = profile_id\n"
            + "WHERE employee_id = ?";

    private static final String QUERY_DELETE_EMPLOYEE_PROFILE = "DELETE\n"
            + "FROM\n"
            + "	employee_profile\n"
            + "WHERE\n"
            + "	employee_id = ";

    private static final String QUERY_INSERT_EMPLOYEE_PROFILE = "INSERT\n"
            + "	INTO\n"
            + "		employee_profile( employee_id,\n"
            + "		profile_id )\n"
            + "	VALUES ";

    private static final String QUERY_VALITE_PASS = "SELECT\n"
            + "	*\n"
            + "FROM\n"
            + "	employee\n"
            + "WHERE\n"
            + "	id = ?\n"
            + "	AND pass = ?;";

    private static final String QUERY_RESET_PASS = "UPDATE\n"
            + "	employee\n"
            + "SET\n"
            + "	pass = ?\n"
            + "WHERE\n"
            + "	id = ?;";
//</editor-fold>
}
