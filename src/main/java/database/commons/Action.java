/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.commons;

/**
 * Identifiers for standar actions in database. Do not modifie!
 *
 * @author Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public enum Action {
    /**
     * represents the action of find an entity by the id propertie
     */
    FIND_BY_ID,
    /**
     * represents the action of find all the entities in the data base
     */
    FIND_ALL,
    /**
     * represents the action of create an entity
     */
    CREATE,
    /**
     * represents the action of remove an entity by the propertie id in the
     * databese
     */
    DELETE_BY_ID,
    /**
     * represents the action of update an entity in the database
     */
    UPDATE,
    /**
     * represents the action of count an entity in the database
     */
    COUNT
}
