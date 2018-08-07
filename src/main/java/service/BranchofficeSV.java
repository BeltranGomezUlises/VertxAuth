/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import database.BranchofficeDBV;
import service.commons.ServiceVerticle;

/**
 *
 * @author ulises
 */
public class BranchofficeSV extends ServiceVerticle{

    @Override
    protected String getDBAddress() {
        return BranchofficeDBV.class.getSimpleName();
    }

    @Override
    protected String getEndpointAddress() {
        return "/branchoffices";
    }
    
}
