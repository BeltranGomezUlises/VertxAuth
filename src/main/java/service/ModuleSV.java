/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import database.ModuleDBV;
import service.commons.ServiceVerticle;

/**
 *
 * Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class ModuleSV extends ServiceVerticle {

    @Override
    protected String getDBAddress() {
        return ModuleDBV.class.getSimpleName();
    }

    @Override
    protected String getEndpointAddress() {
        return "/modules";
    }

}
