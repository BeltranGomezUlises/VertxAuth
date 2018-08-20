package main;

import database.BranchofficeDBV;
import database.EmployeeDBV;
import database.ModuleDBV;
import database.PermissionDBV;
import database.ProfileDBV;
import database.SubModuleDBV;
import database.commons.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Function;
import service.AuthVerticle;
import service.BranchofficeSV;
import service.EmployeeSV;
import service.MailVerticle;
import service.ModuleSV;
import service.PermissionSV;
import service.ProfileSV;
import service.SubModulesSV;
import service.commons.*;
import utils.UtilsRouter;

/**
 * Main class to start all verticles
 *
 * @author Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class MainVerticle extends AbstractVerticle {

    private static final String CONFIG_FILE_PATH = "./config.json";

    private final String configFilePath;

    public MainVerticle(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public static void main(String[] args) {
        MainVerticle main;
        if (args.length > 0) {
            main = new MainVerticle(args[0]);
        } else {
            main = new MainVerticle(null);
        }
        Vertx v = Vertx.vertx();
        v.deployVerticle(main);
    }

    @Override
    public void start() throws Exception {
        //!important!//
        //initialize main router
        UtilsRouter.getInstance(vertx);

        //load config
        JsonObject config = this.loadConfigFromFile();
        //run verticles
        initializeVerticle(new BranchofficeDBV(), new BranchofficeSV(), config);
        initializeVerticle(new EmployeeDBV(), new EmployeeSV(), config);
        initializeVerticle(new ProfileDBV(), new ProfileSV(), config);
        initializeVerticle(new ModuleDBV(), new ModuleSV(), config);
        initializeVerticle(new SubModuleDBV(), new SubModulesSV(), config);
        initializeVerticle(new PermissionDBV(), new PermissionSV(), config);
        vertx.deployVerticle(new AuthVerticle(), new DeploymentOptions().setConfig(config));
        vertx.deployVerticle(new MailVerticle(), new DeploymentOptions().setConfig(config));
    }

    /**
     * Deploy dbVerticle and if and only if the dbVerticle is successful, the
     * serviceVerticle starts, both of them runs with the object conf as
     * configuration
     *
     * @param dbVerticle data base verticle with the crud operations
     * @param serviceVerticle service verticle with the crud http services
     * @param config configuration object
     */
    public void initializeVerticle(DBVerticle dbVerticle, ServiceVerticle serviceVerticle, JsonObject config) {
        Future<String> dbVerticleDeployment = Future.future();
        vertx.deployVerticle(dbVerticle, new DeploymentOptions().setConfig(config), dbVerticleDeployment);

        dbVerticleDeployment.compose(__ -> {
            Future<String> httpVerticleDeployment = Future.future();
            vertx.deployVerticle(serviceVerticle, new DeploymentOptions().setConfig(config), httpVerticleDeployment.completer());
            return httpVerticleDeployment;
        });
    }

    /**
     * Deploy dbVerticle and if and only if the dbVerticle is successful, the
     * serviceVerticle starts, both of them runs with the object conf as
     * configuration
     *
     * @param dbVerticle data base verticle with the crud operations
     * @param serviceVerticle service verticle with the crud http services
     * @param config configuration object
     * @param finishHandler composed at the end
     */
    public void initializeVerticle(DBVerticle dbVerticle, ServiceVerticle serviceVerticle, JsonObject config, Function finishHandler) {
        Future<String> dbVerticleDeployment = Future.future();
        vertx.deployVerticle(dbVerticle, new DeploymentOptions().setConfig(config), dbVerticleDeployment);

        dbVerticleDeployment.compose(__ -> {
            Future<String> httpVerticleDeployment = Future.future();
            vertx.deployVerticle(serviceVerticle, new DeploymentOptions().setConfig(config), httpVerticleDeployment.completer());
            return httpVerticleDeployment;
        }).compose(finishHandler);
    }

    /**
     * loads into the JsonObject dbConfig the configuration from the file in db
     *
     * @param config the config file to set the properties
     */
    private JsonObject loadConfigFromFile() throws IOException {
        if (this.configFilePath != null) {
            return this.loadConfigToJsonObject(this.configFilePath);
        } else {
            return this.loadConfigToJsonObject(CONFIG_FILE_PATH); //load the default config file for db
        }
    }

    /**
     * Loads a config file in json format to deploy verticles
     *
     * @param filePath the path of the to load
     * @return json object with the properties in the file
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException if an error occuer while reading
     */
    private JsonObject loadConfigToJsonObject(final String filePath) throws IOException {
        JsonObject result;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("Running app with file: " + filePath + ", and the configs are:");
            result = new JsonObject(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println("The file: " + filePath + ", was not found, running with default configs to develop enviroment:");
            result = new JsonObject()
                    .put("url", "jdbc:mysql://localhost:3306/auth")
                    .put("driver_class", "com.mysql.jdbc.Driver")
                    .put("user", "mysql")
                    .put("password", "2424")
                    .put("max_pool_size", 15) //the maximum number of connections to pool - default is 15         
                    .put("min_pool_size", 3) //the number of connections to initialise the pool with - default is 3
                    .put("initial_pool_size", 3) //the minimum number of connections to pool
                    .put("max_statements", 3) //the maximum number of prepared statements to cache - default is 0
                    .put("max_statements_per_connection", 0) //the maximum number of prepared statements to cache per connection - default is 0                   
                    .put("max_idle_time", 7200) //number of seconds after which an idle connection will be closed - default is 0 (never expire)
                    .put(Constants.CONFIG_HTTP_SERVER_PORT, 8041)
                    .put("mail.hostName", "smtp.googlemail.com")
                    .put("mail.port", 465)
                    .put("mail.ssl", true)
                    .put("mail.userName", "yourMail@gmail.com")
                    .put("mail.password", "yourPass");
        }
        System.out.println(Json.encodePrettily(result));
        return result;
    }
}
