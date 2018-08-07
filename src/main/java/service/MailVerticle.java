/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import static service.commons.Constants.ACTION;

/**
 *
 * Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class MailVerticle extends AbstractVerticle {

    public static final String ACTION_SEND_RECOVER_PASS = "MailVerticle.sendRecoverPass";

    private MailClient mailClient;

    @Override
    public void start() throws Exception {
        super.start();
        MailConfig config = new MailConfig();
        config.setHostname(config().getString("mail.hostName"));
        config.setPort(config().getInteger("mail.port"));
        config.setUsername(config().getString("mail.userName"));
        config.setPassword(config().getString("mail.password"));
        config.setSsl(config().getBoolean("mail.ssl"));
        mailClient = MailClient.createShared(vertx, config);

        this.vertx.eventBus().consumer(this.getClass().getSimpleName(), this::onMessage);
        System.out.println(this.getClass().getSimpleName() + " Running");
    }

    private void onMessage(Message<JsonObject> message) {
        String action = message.headers().get(ACTION);
        switch (action) {
            case ACTION_SEND_RECOVER_PASS:
                this.sendRecoverPass(message);
                break;
        }
    }

    private void sendRecoverPass(Message<JsonObject> message) {
        String employeeName = message.body().getString("employee_name");
        String employeeMail = message.body().getString("employee_email");
        String recoverCode = message.body().getString("recover_code");

        MailMessage mail = new MailMessage();
        mail.setFrom(config().getString("mail.userName"));
        mail.setTo(employeeMail);
        mail.setSubject("Recover password");
        mail.setHtml("<html>\n"
                + "    <head>\n"
                + "        <title>Servicio de recuperación de contraseña</title>\n"
                + "    </head>\n"
                + "    <body style=\"color: rgba(0,0,0, 0.8); font-family: verdana; font-size: 14px;\">\n"
                + "        <div style=\"width: 100%; display: flex; justfy-content: center;\">\n"
                + "            <div style=\"width: 70%;\">\n"
                + "                <div style=\"border: 1px solid rgba(128, 128, 128, 0.31); width: 100%; padding: 20px;\">\n"
                + "                    <h3 style=\"margin-top: 0;\">"
                + "HOLA " + employeeName
                + "                    </h3>\n"
                + "                    <p>Utilize el siguiente código para continuar con el proceso de recuperación de su contraseña</p>\n"
                + "\n"
                + "                    <div style=\"background: #009BD2; color: white; padding: 10px; width: 100px; text-align: center;\">\n"
                + recoverCode
                + "                    </div>\n"
                + "                </div>\n"
                + "\n"
                + "            </div>\n"
                + "        </div>\n"
                + "    </body>\n"
                + "</html>");
        mailClient.sendMail(mail, reply -> {
            if (reply.succeeded()) {
                message.reply(new JsonObject());
            } else {
                message.fail(0, reply.cause().getMessage());
            }
        });
    }

}
