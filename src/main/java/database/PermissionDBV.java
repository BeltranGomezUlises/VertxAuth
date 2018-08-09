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
import java.util.ArrayList;
import java.util.List;
import static service.commons.Constants.ACTION;

/**
 *
 * Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class PermissionDBV extends DBVerticle {

    public static final String ACTION_PROFILE_PERMISSIONS = "PermissionDBV.profilePermissions";
    public static final String ACTION_EMPLOYEE_PERMISSIONS = "PermissionDBV.employeePermissions";
    public static final String ACTION_ASSIGN_PROFILE_PERMISSIONS = "PermissionDBV.assignProfilePermissions";
    public static final String ACTION_ASSIGN_EMPLOYEE_PERMISSIONS = "PermissionDBV.assignEmployeePermissions";

    @Override
    public String getTableName() {
        return "permission";
    }

    @Override
    protected void onMessage(Message<JsonObject> message) {
        super.onMessage(message);
        String action = message.headers().get(ACTION);
        switch (action) {
            case ACTION_PROFILE_PERMISSIONS:
                this.profilePermissions(message);
                break;
            case ACTION_EMPLOYEE_PERMISSIONS:
                this.employeePermissions(message);
                break;
            case ACTION_ASSIGN_PROFILE_PERMISSIONS:
                this.assignProfilePermission(message);
                break;
            case ACTION_ASSIGN_EMPLOYEE_PERMISSIONS:
                this.assignEmployeePermission(message);
                break;
        }
    }

    private void profilePermissions(Message<JsonObject> message) {
        int id = message.body().getInteger("id");
        this.dbClient.queryWithParams(QUERY_PROFILE_PERMISSIONS, new JsonArray().add(id), reply -> {
            if (reply.succeeded()) {
                message.reply(new JsonArray(reply.result().getRows()));
            } else {
                reportQueryError(message, reply.cause());
            }
        });
    }

    private void employeePermissions(Message<JsonObject> message) {
        int id = message.body().getInteger("id");
        this.dbClient.queryWithParams(QUERY_EMPLOYEE_PERMISSIONS, new JsonArray().add(id), reply -> {
            if (reply.succeeded()) {
                message.reply(new JsonArray(reply.result().getRows()));
            } else {
                reportQueryError(message, reply.cause());
            }
        });
    }

    private void assignProfilePermission(Message<JsonObject> message) {
        JsonObject body = message.body();
        int profileId = body.getInteger("profile_id");
        JsonArray permissions = body.getJsonArray("permissions");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < permissions.size(); i++) {
            JsonObject permission = permissions.getJsonObject(i);
            values.add(
                    "(" + profileId + ", "
                    + permission.getInteger("permission_id") + ","
                    + permission.getInteger("branchoffice_id") + ")"
            );
        }
        List<String> batch = new ArrayList<>();
        batch.add(QUERY_DELETE_PROFILE_PERMISSIONS + profileId);
        batch.add(QUERY_INSERT_PROFILE_PERMISSIONS + String.join(",", values));
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

    private void assignEmployeePermission(Message<JsonObject> message) {
        JsonObject body = message.body();
        int employeeId = body.getInteger("employee_id");
        JsonArray permissions = body.getJsonArray("permissions");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < permissions.size(); i++) {
            JsonObject permission = permissions.getJsonObject(i);
            values.add(
                    "(" + employeeId + ", "
                    + permission.getInteger("permission_id") + ","
                    + permission.getInteger("branchoffice_id") + ")"
            );
        }
        List<String> batch = new ArrayList<>();
        batch.add(QUERY_DELETE_EMPLOYEE_PERMISSIONS + employeeId);
        batch.add(QUERY_INSERT_EMPLOYEE_PERMISSIONS + String.join(",", values));
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

//<editor-fold defaultstate="collapsed" desc="queries">
    private static final String QUERY_PROFILE_PERMISSIONS = "SELECT\n"
            + "	permission_id,\n"
            + "	branchoffice_id\n"
            + "FROM\n"
            + "	profile_permission_branchoffice\n"
            + "WHERE\n"
            + "	profile_id = ?;";

    private static final String QUERY_EMPLOYEE_PERMISSIONS = "SELECT\n"
            + "	permission_id,\n"
            + "	branchoffice_id\n"
            + "FROM\n"
            + "	employee_permission_branchoffice\n"
            + "WHERE\n"
            + "	employee_id = ?;";

    private static final String QUERY_DELETE_PROFILE_PERMISSIONS = "DELETE\n"
            + "FROM\n"
            + "	profile_permission_branchoffice\n"
            + "WHERE\n"
            + "	profile_id = ";

    private static final String QUERY_INSERT_PROFILE_PERMISSIONS = "INSERT INTO\n"
            + "	profile_permission_branchoffice ( \n"
            + "	profile_id,\n"
            + "	permission_id,\n"
            + "	branchoffice_id \n"
            + ") VALUES ";

    private static final String QUERY_INSERT_EMPLOYEE_PERMISSIONS = "INSERT INTO\n"
            + "	employee_permission_branchoffice ( \n"
            + "		employee_id,\n"
            + "		permission_id,\n"
            + "		branchoffice_id \n"
            + ")VALUES";

    private static final String QUERY_DELETE_EMPLOYEE_PERMISSIONS = "DELETE\n"
            + "FROM\n"
            + "	employee_permission_branchoffice\n"
            + "WHERE\n"
            + "	employee_id = ";
//</editor-fold>

}
