/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.commons;

/**
 * status codes for catalogs
 *
 * @author Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public enum Status {
    /**
     *
     */
    ARCHIVED(0),
    /**
     *
     */
    ACTIVE(1),
    /**
     *
     */
    INACTIVE(2),
    /**
     *
     */
    DELETED(3);

    private final int value;

    public int getValue() {
        return value;
    }

    Status(int value) {
        this.value = value;
    }

}
