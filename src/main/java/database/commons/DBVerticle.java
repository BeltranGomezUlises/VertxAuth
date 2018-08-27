/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.commons;

import static database.commons.Action.FIND_ALL;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLOptions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.toList;
import static service.commons.Constants.ACTION;
import utils.UtilsValidation;

/**
 * Base Verticle for LCRUD entities, this class works as a facade
 *
 * @author Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public abstract class DBVerticle extends AbstractVerticle {

    /**
     * the sql client contains the channel of comunication with the database
     */
    protected SQLClient dbClient;

    /**
     * method that runs when the verticles is deployed
     *
     * @param startFuture future to start with this deployment
     * @throws Exception
     */
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        JsonObject conf = new JsonObject()
                .put("url", config().getString("url"))
                .put("driver_class", config().getString("driver_class"))
                .put("user", config().getString("user"))
                .put("password", config().getString("password"))
                .put("max_pool_size", config().getInteger("max_pool_size"))
                .put("min_pool_size", config().getInteger("min_pool_size"))
                .put("initial_pool_size", config().getInteger("initial_pool_size"))
                .put("max_statements", config().getInteger("max_statements"))
                .put("max_statements_per_connection", config().getInteger("max_statements_per_connection"))
                .put("max_idle_time", config().getInteger("max_idle_time"));

        dbClient = JDBCClient.createShared(vertx, conf);

        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                System.out.println("Could not open a database connection" + ar.cause());
                startFuture.fail(ar.cause());
            } else {
                vertx.eventBus().consumer(this.getClass().getSimpleName(), this::onMessage);
                startFuture.complete();
                ar.result().close();//close de connection, it will not be used
            }
        });
    }

    /**
     * This method takes the action of the message and execute the method that
     * corresponds
     *
     * @param message the message from the event bus
     */
    protected void onMessage(Message<JsonObject> message) {
        if (isValidAction(message)) {
            try {
                Action action = Action.valueOf(message.headers().get(ACTION));
                switch (action) {
                    case CREATE:
                        this.create(message);
                        break;
                    case DELETE_BY_ID:
                        this.deleteById(message);
                        break;
                    case FIND_BY_ID:
                        this.findById(message);
                        break;
                    case FIND_ALL:
                        this.findAll(message);
                        break;
                    case FIND_ALL_V2:
                        this.findAllV2(message);
                        break;
                    case UPDATE:
                        this.update(message);
                        break;
                    case COUNT:
                        this.count(message);
                        break;
                }
            } catch (IllegalArgumentException e) {
            }
        }
    }

    /**
     * Validates if the action in the headers is valid
     *
     * @param message the message from the event bus
     * @return true if containg an action, false otherwise
     */
    protected boolean isValidAction(Message<JsonObject> message) {
        if (!message.headers().contains("action")) {
            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No action header specified");
            return false;
        }
        return true;
    }

    /**
     * This methos has the behaivor for reporting an error in a query execute
     *
     * @param message the message from the eventu bus
     * @param cause the exception giving problems
     */
    protected void reportQueryError(Message<JsonObject> message, Throwable cause) {
        JsonObject catchedError = null;
        if (cause.getMessage().contains("foreign key")) { //a foreign key constraint fails
            Pattern p = Pattern.compile("`(.+?)`");
            Matcher m = p.matcher(cause.getMessage());
            List<String> incidencias = new ArrayList<>(5);
            while (m.find()) {
                incidencias.add(m.group(1));
            }
            catchedError = new JsonObject().put("name", incidencias.get(incidencias.size() - 3)).put("error", "does not exist in the catalog");
        }
        if (cause.getMessage().contains("Data too long")) { //data to long for column            
            Pattern p = Pattern.compile("'(.+?)'");
            Matcher m = p.matcher(cause.getMessage());
            m.find();
            String propertyName = m.group(1);
            catchedError = new JsonObject().put("name", propertyName).put("error", "to long data");
        }
        if (cause.getMessage().contains("Unknown column")) {//unkown column
            Pattern p = Pattern.compile("'(.+?)'");
            Matcher m = p.matcher(cause.getMessage());
            m.find();
            String propertyName = m.group(1);
            catchedError = new JsonObject().put("name", propertyName).put("error", UtilsValidation.PARAMETER_DOES_NOT_EXIST);
        }
        if (cause.getMessage().contains("doesn't have a default")) { //not default value in not null
            Pattern p = Pattern.compile("'(.+?)'");
            Matcher m = p.matcher(cause.getMessage());
            m.find();
            String propertyName = m.group(1);
            catchedError = new JsonObject().put("name", propertyName).put("error", UtilsValidation.MISSING_REQUIRED_VALUE);
        }
        if (cause.getMessage().contains("Duplicate entry")) { //already exist (duplicate key for unique values)
            Pattern p = Pattern.compile("'(.+?)'");
            Matcher m = p.matcher(cause.getMessage());
            m.find();
            String value = m.group(1);
            m.find();
            String propertyName = m.group(1);
            String[] parts = propertyName.split("_");
            //catchedError = new JsonObject().put("name", propertyName).put("error", "value: " + value + " in " + UtilsValidation.ALREADY_EXISTS);
            catchedError = new JsonObject().put("name", parts[1]).put("error", "Already exists");
        }
        if (cause.getMessage().contains("Data truncation")) {
            Pattern p = Pattern.compile("'(.+?)'");
            Matcher m = p.matcher(cause.getMessage());
            m.find();
            String propertyName = m.group(1);
            catchedError = new JsonObject().put("name", propertyName).put("error", "to long data");
        }
        if (catchedError != null) {
            message.reply(catchedError, new DeliveryOptions().addHeader(ErrorCodes.DB_ERROR.toString(), catchedError.getString("error")));
        } else {
            message.fail(ErrorCodes.DB_ERROR.ordinal(), cause.getMessage());
        }
    }

    /**
     * Execute the query to find all the elements of this database verticle
     *
     * @param message message from the event bus
     */
    protected void findAllV2(Message<JsonObject> message) {
        String selectParam = message.body().getString("select");
        String specialJoinParam = message.body().getString("specialJoin");
        String whereParam = message.body().getString("where");
        String joinParam = message.body().getString("joinType");
        String fromParam = message.body().getString("from");
        String toParam = message.body().getString("to");

        /*sets the join type to one valid INNER, RIGHT or LEFT*/
        joinParam = this.validateJoinParam(joinParam);
        Set<Join> joinsNeeded = new HashSet<>();
        /*add especial joins to joinsNeeded*/
        this.addSpecialJoins(specialJoinParam, joinsNeeded, joinParam);

        //create parts of the query
        String querySelect = this.selectV2(selectParam, joinsNeeded, joinParam);
        String queryFrom = " FROM " + this.getTableName() + " ";
        String queryWhere = this.whereV2(whereParam, joinsNeeded, joinParam);
        String queryJoin = this.join(joinsNeeded);

        /*this is the fina query to excecute*/
        String queryToExcecute = querySelect + queryFrom + queryJoin + queryWhere;
        //adds the limit for pagination
        if (fromParam != null && toParam != null) {
            queryToExcecute += " LIMIT " + fromParam + "," + toParam;
        } else if (toParam != null) {
            queryToExcecute += " LIMIT " + toParam;
        }
        dbClient.query(queryToExcecute, reply -> {
            this.genericResponse(message, reply);
        });
    }

    /**
     * Execute the query "select * from"
     *
     * @param message message from the event bus
     */
    protected void findAll(Message<JsonObject> message) {
        String queryParam = message.body().getString("query");
        String fromParam = message.body().getString("from");
        String toParam = message.body().getString("to");

        String queryToExcecute;
        if (queryParam != null) {
            queryToExcecute = this.select(queryParam);
        } else {
            queryToExcecute = "select * from " + this.getTableName() + " where status != " + Status.DELETED.getValue();
        }
        //adds the limit for pagination
        if (fromParam != null && toParam != null) {
            queryToExcecute += " limit " + fromParam + "," + toParam;
        } else if (toParam != null) {
            queryToExcecute += " limit " + toParam;
        }
        dbClient.query(queryToExcecute, reply -> {
            if (reply.succeeded()) {
                message.reply(new JsonArray(reply.result().getRows()));
            } else {
                reportQueryError(message, reply.cause());
            }
        });
    }

    /**
     * Execute the query "select * from table where id = ?"
     *
     * @param message message from the event bus
     */
    protected void findById(Message<JsonObject> message) {
        String query = "select * from " + this.getTableName() + " where id = ?";
        JsonArray params = new JsonArray().add(message.body().getInteger("id"));
        dbClient.queryWithParams(query, params, reply -> {
            if (reply.succeeded()) {
                if (reply.result().getNumRows() > 0) {
                    message.reply(reply.result().getRows().get(0));
                } else {
                    message.reply(null);
                }
            } else {
                reportQueryError(message, reply.cause());
            }
        });
    }

    /**
     * Execute the query "delete from table where id = ?"
     *
     * @param message message from the event bus
     */
    protected void deleteById(Message<JsonObject> message) {
        JsonArray params = new JsonArray().add(message.body().getInteger("id"));
        dbClient.updateWithParams("update " + this.getTableName() + " set status = 3 where id = ?", params, reply -> {
            if (reply.succeeded()) {
                if (reply.result().getUpdated() == 0) { //does not exist element with id 
                    message.reply(null, new DeliveryOptions().addHeader(ErrorCodes.DB_ERROR.toString(), "Element not found"));
                } else {
                    message.reply(null);
                }
            } else {
                reportQueryError(message, reply.cause());
            }
        });
    }

    /**
     * Execute the query "create" generated by the properties of the object in
     * the message
     *
     * @param message message from the event bus
     */
    protected void create(Message<JsonObject> message) {
        GenericCreate model = this.generateGenericCreate(message.body());
        dbClient.getConnection(hdl -> {
            if (hdl.succeeded()) {
                SQLConnection con = hdl.result();
                con.setOptions(new SQLOptions().setAutoGeneratedKeys(true));
                con.updateWithParams(model.getQuery(), model.getParams(), reply -> {
                    if (reply.succeeded()) {
                        message.reply(new JsonObject().put("id", reply.result().getKeys().getInteger(0)));
                    } else {
                        reportQueryError(message, reply.cause());
                    }
                    con.close();
                });
            } else {
                message.fail(ErrorCodes.DB_ERROR.ordinal(), "Imposible to get connection to the database");
            }
        });
    }

    /**
     * Execute the query "update" generated by the properties of the object in
     * the message
     *
     * @param message message from the event bus
     */
    protected void update(Message<JsonObject> message) {
        String query = "select status from " + this.getTableName() + " where id = ?";
        JsonArray params = new JsonArray().add(message.body().getInteger("id"));
        dbClient.queryWithParams(query, params, reply -> {
            if (reply.succeeded()) {
                if (reply.result().getNumRows() > 0) {
                    int status = reply.result().getRows().get(0).getInteger("status");
                    if (status == 3) {
                        message.reply(null, new DeliveryOptions().addHeader(ErrorCodes.DB_ERROR.toString(), "can't update deleted element"));
                    } else {
                        if (status == 0) {
                            if (message.body().getInteger("status") != 1) {
                                message.reply(null, new DeliveryOptions().addHeader(ErrorCodes.DB_ERROR.toString(), "status can only change to active"));
                            } else {
                                executeUpdate(message);
                            }

                        } else {
                            executeUpdate(message);
                        }
                    }
                } else {
                    reportQueryError(message, new Exception("Element not found"));
                }
            } else {
                reportQueryError(message, reply.cause());
            }
        });
        //has to validate that the change of status is permited
        //when a register is in state archived it only be passed to active
        if (message.body().containsKey("status")) {
            //query for the status of the register to update

        } else {
            executeUpdate(message);
        }
    }

    /**
     * executes the update operation with the properties of the JsonObject
     *
     * @param message message to reply
     */
    private void executeUpdate(Message<JsonObject> message) {
        String query = "update " + getTableName() + " set ";
        JsonArray params = new JsonArray();
        for (String fieldName : message.body().fieldNames()) {
            if (!fieldName.equals("id") && message.body().getValue(fieldName) != null) {
                query += fieldName + " = ?, ";
                params.add(message.body().getValue(fieldName));
            }
        }
        query = query.substring(0, query.length() - 2);
        query += " where id = ?";
        params.add(message.body().getInteger("id"));
        dbClient.updateWithParams(query, params, reply -> {
            if (reply.succeeded()) {
                message.reply(null);
            } else {
                reportQueryError(message, reply.cause());
            }
        });
    }

    /**
     * Execute the count query of all elements in the table of this verticle
     *
     * @param message message from the event bus
     */
    protected void count(Message<JsonObject> message) {
        String query = "select count(*) as 'count' from " + this.getTableName();
        dbClient.query(query, reply -> {
            if (reply.succeeded()) {
                message.reply(reply.result().getRows().get(0));
            } else {
                reportQueryError(message, reply.cause());
            }
        });
    }

    /**
     * Execute the query "select {properties} from " where properties are the
     * solicited properties in the query param
     *
     * @param queryParam string query param, that contains the selection and
     * filter to query
     * @return query to excecute in the data base generated with the queryParam
     */
    protected String select(String queryParam) {
        //clausula de selecccion de campos
        String qSelect = "select ";
        //set de elementos en las clausulas where
        Set<String> qWheres = new LinkedHashSet<>();
        //set de elementos en las clausulas from
        Set<String> qFroms = new LinkedHashSet<>();
        qFroms.add(" from " + this.getTableName());
        //obtener los elementos solicitados
        boolean addStatusFilter = true;
        String[] selections = queryParam.split(","); //fields separation
        for (String selection : selections) {
            if (selection.contains(".") && !isText(selection)) { //a point means that joins with a table
                String[] relation = selection.split("\\.");
                if (relation[0].contains("[")) {
                    relation = selection.split("].");
                    String property = relation[1];
                    String populate = relation[0];
                    populate = populate.substring(1, populate.length());
                    String[] fields = populate.split("=");
                    String localProperty = fields[0];
                    String[] tableFields = fields[1].split("\\.");
                    String table = tableFields[0];
                    String field = tableFields[1];

                    qSelect += table + "." + property + " as " + (table + "_" + property) + ",";
                    qFroms.add(table);
                    qWheres.add(this.getTableName() + "." + localProperty + " = " + table + "." + field);
                } else {
                    String whereSelection = whereSelection(relation[1]);
                    if (whereSelection.isEmpty()) {
                        qSelect += relation[0] + "." + relation[1] + " as " + (relation[0] + "_" + relation[1]) + ",";
                        qFroms.add(relation[0]);
                        qWheres.add(this.getTableName() + "." + relation[0] + "_id = " + relation[0] + ".id");
                    } else {
                        String[] fieldDecompose = relation[1].split(whereSelection);
                        qSelect += relation[0] + "." + fieldDecompose[0] + " as " + (relation[0] + "_" + fieldDecompose[0]) + ",";
                        qFroms.add(relation[0]);
                        qWheres.add(this.getTableName() + "." + relation[0] + "_id = " + relation[0] + ".id");
                        qWheres.add(relation[0] + "." + fieldDecompose[0] + " = " + fieldDecompose[1]);
                    }
                }

            } else {//if no joins then only validate a where clause
                String whereSelection = whereSelection(selection);
                if (whereSelection.isEmpty()) {
                    qSelect += this.getTableName() + "." + selection + ",";
                } else {
                    String[] whereSelections = selection.split(whereSelection);
                    if (whereSelections[0].equals("status")) { //ignore de default filter in status column
                        addStatusFilter = false;
                    }
                    if (whereSelection.equals("><")) { //if the where clause is a between operation
                        qSelect += this.getTableName() + "." + whereSelections[0] + ",";
                        String[] betweenValues = whereSelections[1].split("\\|");
                        qWheres.add(this.getTableName() + "." + whereSelections[0] + " between " + betweenValues[0] + " and " + betweenValues[1]);
                    } else {
                        qSelect += this.getTableName() + "." + whereSelections[0] + ",";
                        qWheres.add(this.getTableName() + "." + whereSelections[0] + " " + whereSelection + " " + whereSelections[1]);
                    }
                }
            }
        }
        qSelect = qSelect.substring(0, qSelect.length() - 1);
        String from = String.join(",", qFroms);
        String where = "";
        if (!qWheres.isEmpty()) {
            where += " where ";
            where += String.join(" and ", qWheres);
            if (addStatusFilter) {
                where += " and " + this.getTableName() + ".status != " + Status.DELETED.getValue();
            }
        } else {
            where += " where " + this.getTableName() + ".status != " + Status.DELETED.getValue();
        }
        return qSelect + from + where;
    }

    /**
     * Validates the query param if it is a string
     *
     * @param selection the selecion in the query param
     * @return true if the selection is a specified string
     */
    private boolean isText(final String selection) {
        String whereSelection = whereSelection(selection);
        if (!whereSelection.isEmpty()) {
            String value = selection.split(whereSelection)[1];
            return (value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'"));
        }
        return false;
    }

    /**
     * check if there is a where condition in a selection
     *
     * @param selection the field selection
     * @return the operator to use in the where clause like '=' or '!='
     */
    private String whereSelection(String selection) {
        if (selection.contains(">=")) {
            return ">=";
        }
        if (selection.contains("<=")) {
            return "<=";
        }
        if (selection.contains("><")) { //between, its important the order of the questions
            return "><";
        }
        if (selection.contains("!=")) {
            return "!=";
        }
        if (selection.contains("=")) {
            return "=";
        }
        if (selection.contains(">")) {
            return ">";
        }
        if (selection.contains("<")) {
            return "<";
        }
        return "";
    }

    /**
     * Generates que query and params needed for a generic create operation
     *
     * @param objectBody the object message to create
     * @return model with the query and needed params to create a register in db
     * of this ServiceDatabaseVerticle
     */
    protected final GenericCreate generateGenericCreate(JsonObject objectBody) {
        String query = "insert into " + getTableName() + "(";
        String queryValues = " values (";
        JsonArray params = new JsonArray();
        for (String fieldName : objectBody.fieldNames()) {
            if (objectBody.getValue(fieldName) != null) {
                query += fieldName + ",";
                queryValues += "?,";
                params.add(objectBody.getValue(fieldName));
            }
        }
        query = query.substring(0, query.length() - 1) + ")";
        queryValues = queryValues.substring(0, queryValues.length() - 1) + ")";
        query += queryValues;
        return new GenericCreate(query, params);
    }

    /**
     * Generates a string raw query to insert in database the object in
     * objectBody in the table given
     *
     * @param tableName name of the table to generate the insert
     * @param objectBody object with all the properties to insert
     * @return string with the query
     */
    protected final String generateGenericCreate(final String tableName, final JsonObject objectBody) {
        String query = "insert into " + tableName + "(";
        String queryValues = " values (";
        for (String fieldName : objectBody.fieldNames()) {
            if (objectBody.getValue(fieldName) != null) {
                query += fieldName + ",";
                //evaluate the field if is string
                try {
                    queryValues += "'" + objectBody.getString(fieldName) + "',";
                } catch (Exception e) {
                    queryValues += objectBody.getValue(fieldName) + ",";
                }
            }
        }
        query = query.substring(0, query.length() - 1) + ")";
        queryValues = queryValues.substring(0, queryValues.length() - 1) + ")";
        query += queryValues;
        return query;
    }

    /**
     * Generates a GenericCreate model from a JsonObject in the table specified
     *
     * @param tableName the table name to insert
     * @param objectBody the JsonObeject with the properties to insert
     * @return insert query in string
     */
    protected final GenericCreate generateGenericCreateWithParams(final String tableName, final JsonObject objectBody) {
        String queryValues = "(";
        JsonArray params = new JsonArray();
        for (String fieldName : objectBody.fieldNames()) {
            if (objectBody.getValue(fieldName) != null) {
                queryValues += "?,";
                params.add(objectBody.getValue(fieldName));
            }
        }
        queryValues = queryValues.substring(0, queryValues.length() - 1) + "),";
        return new GenericCreate(queryValues, params);
    }

    /**
     * Generates the insert query from a JsonObject in the table specified
     *
     * @param tableName the table name to insert
     * @param objectBody the JsonObeject with the properties to insert
     * @return insert query in string
     */
    protected final String generateGenericInsertParams(final String tableName, final JsonObject objectBody) {
        String query = "insert into " + tableName + "(";
        for (String fieldName : objectBody.fieldNames()) {
            if (objectBody.getValue(fieldName) != null) {
                query += fieldName + ",";
            }
        }
        query = query.substring(0, query.length() - 1) + ")";
        query += " values ";
        return query;
    }

    /**
     * Need to especifie the name of the entity table, to refer in the
     * properties file, the actions names and queries
     *
     * @return the name of the table to manage in this verticle
     */
    public abstract String getTableName();

    /**
     * Starts a transaction gettin a connection from the pool and setting auto
     * generate keys to true, and auto commit to false
     *
     * @param message message to reply if something goes wrong
     * @param handler the handler called when this operation completes
     */
    protected void startTransaction(Message<JsonObject> message, Handler<SQLConnection> handler) {
        this.dbClient.getConnection(h -> {
            if (h.succeeded()) {
                SQLConnection con = h.result();
                con.setOptions(new SQLOptions().setAutoGeneratedKeys(true));
                con.setAutoCommit(false, t -> {
                    if (t.succeeded()) {
                        handler.handle(con);
                    } else {
                        con.close();
                        message.fail(ErrorCodes.DB_ERROR.ordinal(), t.cause().getMessage());
                    }
                });
            } else {
                message.fail(ErrorCodes.DB_ERROR.ordinal(), h.cause().getMessage());
            }
        });
    }

    /**
     * Roll back the actual connection in transaction with a generic invalid
     * exception message type to the messager
     *
     * @param con connection in actual transaction
     * @param ex exception with the field and message to display
     * @param message message of the serder
     */
    protected void rollback(SQLConnection con, UtilsValidation.PropertyValueException ex, Message<JsonObject> message) {
        con.rollback(h -> {
            con.close();
            message.reply(
                    new JsonObject()
                            .put("name", ex.getName())
                            .put("error", ex.getError()),
                    new DeliveryOptions()
                            .addHeader(ErrorCodes.DB_ERROR.toString(),
                                    "invalid parameter")
            );
        });
    }

    /**
     * Roll back the actual connection in transaction with a generic invalid
     * exception message type to the messager
     *
     * @param con connection in actual transaction
     * @param t cause of the fail in an operation in the transaction
     * @param message message of the serder
     */
    protected void rollback(SQLConnection con, Throwable t, Message<JsonObject> message) {
        con.rollback(h -> {
            con.close();
            reportQueryError(message, t);
        });
    }

    /**
     * Commit the actual transaction and replays to the sender the object
     * provided
     *
     * @param con connection in actual transaction
     * @param message message of the sender
     * @param jsonObject object to reply
     */
    protected void commit(SQLConnection con, Message<JsonObject> message, JsonObject jsonObject) {
        con.commit(h -> {
            con.close();
            message.reply(jsonObject);
        });
    }

    /**
     * Generic response to avoid boilerplate
     *
     * @param message message to reply
     * @param reply reply from the database query
     */
    protected void genericResponse(Message<JsonObject> message, AsyncResult<ResultSet> reply) {
        if (reply.succeeded()) {
            message.reply(new JsonArray(reply.result().getRows()));
        } else {
            reportQueryError(message, reply.cause());
        }
    }

    /**
     * This method is used to add to the set of joins the specified join in the
     * parameter of the URI
     *
     * @param specialJoinParam parameter from the URI
     * @param joinsNeeded joins needed
     * @param joinParam join type
     */
    private void addSpecialJoins(String specialJoinParam, Set<Join> joinsNeeded, String joinParam) {
        if (specialJoinParam != null) {
            if (!specialJoinParam.isEmpty()) {
                String[] specialJoins = specialJoinParam.split(",");
                for (String specialJoin : specialJoins) {
                    //[locaField=table.field].alias
                    String[] parts = specialJoin.split("]");
                    String alias = parts[1];
                    String relation = parts[0].substring(1, parts[0].length());
                    String[] relationParts = relation.split("=");
                    String localField = relationParts[0];
                    String table = relationParts[1];
                    //typeJoin JOIN table alias on localField = field
                    Join join = new Join(joinParam, localField, table, alias, true);
                    joinsNeeded.add(join);
                }
            }
        }
    }

    /**
     * Generates the "WHERE" part of the query
     *
     * @param whereParam parameter from the URI
     * @param joinsNeeded joins needed for the query
     * @param joinParam join type
     * @return the String with the WHERE part of the query
     */
    private String whereV2(String whereParam, Set<Join> joinsNeeded, final String joinParam) {
        List<String> whereParts = new ArrayList<>();
        if (whereParam != null) {
            if (!whereParam.isEmpty()) {
                String[] wheres = whereParam.split(",");
                for (String where : wheres) {
                    if (where.contains(".")) {
                        //table.field>20
                        String comparator = this.whereSelection(where);
                        if (!comparator.isEmpty()) {
                            String[] parts = where.split(comparator);
                            String[] tablePart = parts[0].split("\\.");
                            String table = tablePart[0];
                            if (!table.equals(this.getTableName())) { //other table and needs join
                                //check if the table is a special join
                                boolean exists = joinsNeeded.stream().anyMatch(j -> j.getAlias().equals(table));
                                if (!exists) {
                                    Join join = new Join(joinParam, table + "_id", table, table);
                                    joinsNeeded.add(join);
                                }
                                whereParts.add(parts[0] + " " + comparator + " " + parts[1]);
                            } else {
                                whereParts.add(parts[0] + " " + comparator + " " + parts[1]);
                            }

                        }
                    } else {
                        //id=10
                        String comparator = this.whereSelection(where);
                        if (!comparator.isEmpty()) {
                            String[] parts = where.split(comparator);
                            whereParts.add(parts[0] + " " + comparator + " " + parts[1]);
                        }
                    }
                }

            }
        }
        if (whereParts.isEmpty()) {
            return " WHERE " + this.getTableName() + ".status != " + Status.DELETED.getValue();
        } else {
            return " WHERE " + String.join(" AND ", whereParts);
        }
    }

    /**
     * Generates the "JOIN" part of the query
     *
     * @param joinsNeeded joins needed
     * @return the String with the Join part for the generic query
     */
    private String join(Set<Join> joinsNeeded) {
        return String.join(" ", joinsNeeded.stream()
                .map(j -> {
                    if (j.isSpecialJoin()) {
                        return j.getType()
                                + " JOIN "
                                + j.getTable()
                                + " "
                                + j.getAlias()
                                + " ON "
                                + this.getTableName() + "." + j.getMyField()
                                + " = " + j.getAlias() + ".id";
                    } else {
                        return j.getType()
                                + " JOIN "
                                + j.getTable()
                                + " "
                                + j.getAlias()
                                + " ON "
                                + this.getTableName() + "." + j.getMyField()
                                + " = " + j.getTable() + ".id";
                    }
                })
                .collect(toList()));
    }

    /**
     * Validates de correct name of the join type
     *
     * @param joinParam the parameter from the URI
     * @return INNER, LEFT or RIGHT
     */
    private String validateJoinParam(String joinParam) {
        if (joinParam == null) {
            return "INNER";
        }
        if (!(joinParam.equalsIgnoreCase("INNER") || joinParam.equalsIgnoreCase("LEFT") || joinParam.equalsIgnoreCase("RIGHT"))) {
            return "INNER";
        } else {
            return joinParam;
        }
    }

    /**
     * Generates the "SELECT" part of the generic query
     *
     * @param selectParam parameter from the URI
     * @param joinsNeeded Set of join needed for the query
     * @param joinType join type
     * @return return the String with all the selects
     */
    protected String selectV2(String selectParam, Set<Join> joinsNeeded, String joinType) {
        if (selectParam == null) {
            return "SELECT * ";
        }
        if (selectParam.isEmpty()) {
            return "SELECT * ";
        }
        List<String> fieldsSelect = new ArrayList<>();
        String[] fields = selectParam.split(",");
        for (String field : fields) {
            if (field.contains(".")) {
                String[] parts = field.split("\\.");
                String table = parts[0];
                String tableField = parts[1];
                fieldsSelect.add(table + "." + tableField + " as " + table + "_" + tableField);
                Join join = new Join(joinType, table + "_id", table, table);
                boolean specialJoin = joinsNeeded.stream().anyMatch(j -> j.getAlias().equals(table));
                if (!specialJoin) {
                    joinsNeeded.add(join);
                }
            } else {
                fieldsSelect.add(this.getTableName() + "." + field);
            }
        }
        return "SELECT " + String.join(",", fieldsSelect);
    }

    /**
     * Model to represent a join in a query
     */
    protected class Join {

        private String type;
        private String myField;
        private String table;
        private String alias;
        private boolean specialJoin = false;

        public Join(String type, String myField, String table, String alias) {
            this.type = type;
            this.myField = myField;
            this.table = table;
            this.alias = alias;
        }

        public Join(String type, String myField, String table, String alias, boolean specialJoin) {
            this.type = type;
            this.myField = myField;
            this.table = table;
            this.alias = alias;
            this.specialJoin = specialJoin;
        }

        public boolean isSpecialJoin() {
            return specialJoin;
        }

        public void setSpecialJoin(boolean specialJoin) {
            this.specialJoin = specialJoin;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMyField() {
            return myField;
        }

        public void setMyField(String myField) {
            this.myField = myField;
        }

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 71 * hash + Objects.hashCode(this.type);
            hash = 71 * hash + Objects.hashCode(this.myField);
            hash = 71 * hash + Objects.hashCode(this.table);
            hash = 71 * hash + Objects.hashCode(this.alias);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Join other = (Join) obj;
            if (!Objects.equals(this.type, other.type)) {
                return false;
            }
            if (!Objects.equals(this.myField, other.myField)) {
                return false;
            }
            if (!Objects.equals(this.table, other.table)) {
                return false;
            }
            if (!Objects.equals(this.alias, other.alias)) {
                return false;
            }
            return true;
        }

    }
}
