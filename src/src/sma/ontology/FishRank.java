/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;
import sma.ontology.*;
import sma.BoatAgent;
import java.lang.Math;

/**
 *
 * @author joan
 */
public class FishRank implements Comparable<FishRank>, java.io.Serializable{
    SeaFood sf;
    BoatAgent boat;
    int distance;
    double expectedValue;
    Boolean blockable;
    
    public FishRank(SeaFood sf, BoatAgent boat){
        this.sf = sf;
        this.boat = boat;
        this.setDistance();
        this.setExpectedValue();
        this.setBlockable();
    }

    public SeaFood getSf() {
        return sf;
    }

    public BoatAgent getBoat() {
        return boat;
    }

    public int getDistance() {
        return distance;
    }

    public double getExpectedValue() {
        return expectedValue;
    }

    public Boolean getBlockable() {
        return blockable;
    }
    
    private void setDistance(){
        this.distance = Math.abs(this.boat.getPosX()-this.sf.getPosX()) + Math.abs(this.boat.getPosY()-this.sf.getPosY());
    }
    
    private void setExpectedValue(){
        double capacity, expectedPrice;
        
        if(this.sf.type == SeaFoodType.Tuna){
            capacity = this.boat.getDL().getFreeSpaceTuna();
            expectedPrice = 1.5;
        }else if(this.sf.type == SeaFoodType.Lobster){
            capacity = this.boat.getDL().getFreeSpaceLobster();
            expectedPrice = 2.0;
        }else if(this.sf.type == SeaFoodType.Octopus){
            capacity = this.boat.getDL().getFreeSpaceOctopus();
            expectedPrice = 2.5;
        }else{
            capacity = this.boat.getDL().getFreeSpaceShrimp();
            expectedPrice = 4;
        }
        
        if(this.sf.getQuantity()/4 < capacity){
            this.expectedValue = (this.sf.quantity/4)*expectedPrice; 
        }else{
            this.expectedValue = capacity*expectedPrice;
        }        
    }
    
    public void setBlockable(){
        blockable = true;
        
        if((this.sf.getPosY() > this.boat.getPosY() && this.sf.getMovementDirection() == 1)||(this.sf.getPosY() < this.boat.getPosY() && this.sf.getMovementDirection() == 3)||(this.sf.getPosX() > this.boat.getPosX() && this.sf.getMovementDirection() == 2)||(this.sf.getPosX() < this.boat.getPosX() && this.sf.getMovementDirection() == 0)){
            blockable = false;
        }
        
        if (blockable){
            int dX = Math.abs(this.sf.posX - this.boat.getPosX());
            int dY = Math.abs(this.sf.posY - this.boat.getPosY());
            if(this.sf.movementDirection%2==0){
                if (!(dX >= dY - 1)) blockable = false;
            }else{
                if (!(dY >= dX - 1)) blockable = false;
            }
        }
    }
    
    @Override
    public int compareTo(FishRank fr){
        int comparation;
        
        if(this.expectedValue > fr.expectedValue){
            comparation = 1;
        }else if(this.expectedValue == fr.expectedValue){
            comparation = 0;
        }else{
            comparation = -1;
        }
        
        if(comparation == 0){
            if(this.distance < fr.distance){
                comparation = 1;
            }else if(this.distance == fr.distance){
                comparation = 0;
            }else{
                comparation = -1;
            }
        }
        
        if(comparation == 0){
            if (this.blockable && !fr.blockable) {
                comparation = 1;
            }else if(fr.blockable && !this.blockable){
                comparation = -1;
            }else{
                comparation = 0;
            }
        }
        
        return comparation;
    }
}
