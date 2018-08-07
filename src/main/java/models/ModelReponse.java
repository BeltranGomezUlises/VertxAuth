/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Model for generic responses, this encapsulates the payload of the request
 *
 * @author Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelReponse {

    /**
     * status of the procces of the request
     */
    private Status status;
    /**
     * a message for the end user
     */
    private String message;
    /**
     * a message for the developer (debugging effects)
     */
    private String devMessage;
    /**
     * data or payload of the request
     */
    private Object data;

    public ModelReponse() {
    }

    public ModelReponse(Status status) {
        this.status = status;
    }

    public ModelReponse(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public ModelReponse(Status status, Object data) {
        this.status = status;
        this.data = data;
    }

    public ModelReponse(Status status, String message, String devMessage) {
        this.status = status;
        this.message = message;
        this.devMessage = devMessage;
    }

    public ModelReponse(Status status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ModelReponse(Status status, String message, String devMessage, Object data) {
        this.status = status;
        this.message = message;
        this.devMessage = devMessage;
        this.data = data;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDevMessage() {
        return devMessage;
    }

    public void setDevMessage(String devMessage) {
        this.devMessage = devMessage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static enum Status {
        OK, WARNING, ERROR, INVALID_TOKEN
    }

}
