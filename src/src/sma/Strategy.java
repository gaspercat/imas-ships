/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import sma.ontology.PortType;
import sma.ontology.SeaFoodType;

/**
 *
 * @author carles
 */
abstract class Strategy {
    protected PortType type;
    protected Prices prices;

    public Strategy(PortType type) throws InvalidPortTypeException {
        if(isInvalidType(type)){
            throw new InvalidPortTypeException(type);
        }
        //this.type = type;
        //DEBUG 
        this.prices = Prices.getInstance();
        
        this.type = PortType.Steady;
    }
  
    public abstract Bid willBuy(int[] supplies, int[] diposits, double euros);

    private boolean isInvalidType(PortType type) {
        return type == PortType.None;
    }
 
}
