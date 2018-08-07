/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import database.PermissionDBV;
import service.commons.ServiceVerticle;

/**
 *
 * Ulises Beltrán Gómez - beltrangomezulises@gmail.com
 */
public class PermissionSV extends ServiceVerticle {

    @Override
    protected String getDBAddress() {
        return PermissionDBV.class.getSimpleName();
    }

    @Override
    protected String getEndpointAddress() {
        return "/permissions";
    }

}
