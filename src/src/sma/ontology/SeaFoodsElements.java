/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author joan
 */
public class SeaFoodsElements {
    private ArrayList<SeaFood> seaFood;
    
    public SeaFoodsElements(){
        seaFood = new ArrayList<SeaFood>();
    }
    
    public void addSeaFood(SeaFood sf){
        this.seaFood.add(sf);
    }
    
}
