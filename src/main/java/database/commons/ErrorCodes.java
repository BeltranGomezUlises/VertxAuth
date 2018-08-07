/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.commons;

/**
 * Error codes for database operations
 *
 * @author Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public enum ErrorCodes {
    /**
     * Specifies an error when a verticle recives a message without an action in headers
     */
    NO_ACTION_SPECIFIED,
    /**
     * Specifies an error when a verticle recives a message with an action in headers does not exits
     */
    BAD_ACTION,
    /**
     * Specifies an error when a verticle recives a message that the database access returns an error
     */
    DB_ERROR
}
