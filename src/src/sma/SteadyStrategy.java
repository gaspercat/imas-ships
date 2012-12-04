/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import sma.ontology.PortType;


public class SteadyStrategy extends Strategy {

    public SteadyStrategy(PortType type) throws InvalidPortTypeException {
        super(type);
    }

    @Override
    public Bid willBuy(int[] supplies, int[] diposits, double euros) {
        
    }
    
}
