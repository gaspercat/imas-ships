/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import sma.ontology.PortType;
import sma.ontology.SeaFoodType;


public class MininumStrategy extends Strategy {

    public MininumStrategy(PortType type) throws InvalidPortTypeException {
        super(type);
    }

    @Override
    public Bid willBuy(int[] supplies, int[] diposits, double euros) {
        double price = 0;
        
        //TODO acabar de modelar els diposits com a diccionari
        for(int i = 0; i < supplies.length; i++){
            price += supplies[i] * this.prices.getMinPrice(SeaFoodType.values()[i]);
        }
        return new Bid(true, price);
    }
    
    
}
