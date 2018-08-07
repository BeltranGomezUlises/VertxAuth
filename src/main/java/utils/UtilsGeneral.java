/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.Enumeration;
import java.util.Properties;

/**
 *
 * @author Ulises Beltrán Gómez - beltrangomezulises@gmail.com at 16/04/2018
 */
public class UtilsGeneral {

    /**
     * imprime todas las propiedades en un objeto Properties
     *
     * @param props objeto con las propiedades a imprimir
     */
    public static void printProps(Properties props) {
        //impresion de props
        Enumeration<?> enums = props.propertyNames();
        String key;
        String prop;
        while (enums.hasMoreElements()) {
            key = (String) enums.nextElement();
            prop = props.getProperty(key);
            System.out.println(key + " = " + prop);
        }
    }

}
