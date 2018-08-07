/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

/**
 * Model to construct properties error messages
 *
 * @author Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class PropertyError {

    private String name;
    private String error;

    public PropertyError() {
    }

    public PropertyError(String name, String error) {
        this.name = name;
        this.error = error;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
