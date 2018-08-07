/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import database.commons.DBVerticle;

/**
 *
 * @author ulises
 */
public class ModuleDBV extends DBVerticle {

    @Override
    public String getTableName() {
        return "module";
    }
    
}
