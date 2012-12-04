/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import java.util.EnumMap;
import java.util.HashMap;
import sma.ontology.SeaFoodType;

/**
 *
 * @author carles
 */
public class Prices {
    private EnumMap<SeaFoodType, Double> minPrices;
    private EnumMap<SeaFoodType, Double> maxPrices;

    
    private Prices() {
        minPrices.put(SeaFoodType.Lobster , 1.0);
        minPrices.put(SeaFoodType.Octopus , 2.0);
        minPrices.put(SeaFoodType.Shrimp , 1.5);
        minPrices.put(SeaFoodType.Tuna , 3.0);
        maxPrices.put(SeaFoodType.Lobster , 2.0);
        maxPrices.put(SeaFoodType.Octopus , 3.0);
        maxPrices.put(SeaFoodType.Shrimp , 2.5);
        maxPrices.put(SeaFoodType.Tuna , 5.0);        
    }
    
    public static Prices getInstance() {
        return PricesHolder.INSTANCE;
    }
    
    private static class PricesHolder {

        private static final Prices INSTANCE = new Prices();
    }
    
    public double getMinPrice(SeaFoodType seafood){
        return minPrices.get(seafood);
    }
    public double getMaxPrice(SeaFoodType seafood){
        return maxPrices.get(seafood);
    }
}
