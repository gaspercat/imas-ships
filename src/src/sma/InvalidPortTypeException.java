/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import sma.ontology.PortType;

/**
 *
 * @author carles
 */
class InvalidPortTypeException extends Exception {

    
    public InvalidPortTypeException(PortType type) {
        super("Invalid Port Type "+type);
    }
    
    
    
}
