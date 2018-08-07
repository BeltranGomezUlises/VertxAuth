/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.commons;

import io.netty.handler.codec.http.HttpResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static service.commons.Constants.CONFIG_HTTP_SERVER_PORT;
import utils.UtilsJWT;
import utils.UtilsResponse;
import utils.UtilsRouter;

/**
 *
 * Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class FileManagmentVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        super.start();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(this.vertx);
        router.post("/upload").handler(this::upload);
        router.get("/download/:folder/:file").handler(this::download);

        UtilsRouter.getInstance(vertx).mountSubRouter("/fileManager", router);
        Integer portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT);
        if (portNumber == null) {
            System.out.println("Could not start a HTTP server" + this.getClass().getSimpleName() + ", no port speficied in configuration");
        }
        server.requestHandler(UtilsRouter.getInstance(vertx)::accept)
                .listen(portNumber, ar -> {
                    if (ar.succeeded()) {
                        System.out.println(this.getClass().getSimpleName() + " running");
                    } else {
                        System.out.println("Could not start a HTTP server " + this.getClass().getSimpleName() + ", " + ar.cause());
                    }
                });
    }

    private void upload(RoutingContext context) {
        String token = context.request().headers().get("Authorization");
        if (UtilsJWT.isAccessTokenValid(token)) {
            int createdBy = UtilsJWT.getEmployeeIdFrom(token);
            HttpServerRequest req = context.request();
            req.setExpectMultipart(true);
            req.uploadHandler(upload -> {
                String folder = upload.name();
                String fileName = upload.filename();
                String date = String.valueOf(new Date().getTime());
                String finalFileName = createdBy + "_" + date + "_" + fileName;
                //actual directoy, files directory, the folder specified, the file name
                String fileFinalPath = new File("").getAbsolutePath() + "/files/" + folder + "/" + finalFileName;

                upload.exceptionHandler(cause -> {
                    UtilsResponse.responseError(context, cause.getMessage());
                });

                upload.endHandler(__ -> {
                    UtilsResponse.responseOk(context,
                            new JsonObject()
                                    .put("folder", folder)
                                    .put("file", finalFileName));
                });

                Path pathToFile = Paths.get(fileFinalPath);
                try {
                    Files.createDirectories(pathToFile.getParent());
                    upload.streamToFileSystem(fileFinalPath);
                } catch (IOException ex) {
                    Logger.getLogger(FileManagmentVerticle.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } else {
            UtilsResponse.responseInvalidToken(context);
        }
    }

    private void download(RoutingContext context) {
        String token = context.request().headers().get("Authorization");
        if (UtilsJWT.isAccessTokenValid(token)) {
            String folder = context.request().getParam("folder");
            String file = context.request().getParam("file");
            String absolutePathfile = new File("").getAbsolutePath() + "/files/" + folder + "/" + file;
            vertx.fileSystem().open(absolutePathfile, new OpenOptions(), readEvent -> {
                if (readEvent.failed()) {
                    UtilsResponse.responseError(context, readEvent.cause().getMessage());
                    return;
                }
                HttpServerResponse response = context.response()
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                        .putHeader("Content-Disposition", "attachment; filename=" + file);

                AsyncFile asyncFile = readEvent.result();

                response.setChunked(true);
                Pump pump = Pump.pump(asyncFile, response);
                pump.start();
                asyncFile.endHandler(__ -> {
                    asyncFile.close();

                    response.end();
                });
            });
        } else {
            UtilsResponse.responseInvalidToken(context);
        }
    }
}
