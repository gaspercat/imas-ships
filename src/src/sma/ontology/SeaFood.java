/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import jade.core.*;
import java.io.Serializable;

/**
 *
 * @author joan
 */
public class SeaFood implements java.io.Serializable{
    private static int ids = 0;
    
    int id;
    
    int posX, posY, mapX, mapY, movementDirection;
    float quantity;
    SeaFoodType type;
    // Number of boats blocking the seafood. Must be 4 to start fishing.
    int blocks;
    
    public SeaFood(SeaFoodType type, int posX, int posY, int mapX, int mapY, float quantity){
        SeaFood.ids++;
        this.id = ids;
        
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.mapX = mapX-1;
        this.mapY = mapY-1;
        this.movementDirection = this.setMovementDirection();
        this.quantity = quantity;
        this.blocks = 0;
    }
    
    public int getID(){
        return this.id;
    }
    
    public void setPosX(int posX){
        this.posX = posX;
    }
    
    public int getPosX(){
        return this.posX;
    }
    
    public void setPosY(int posY){
        this.posY = posY;
    }
    
    public int getPosY(){
        return this.posY;
    }
    
    public int setMovementDirection(){
        int[] distances = new int[4];
        distances[0] = this.posX;
        distances[1] = this.mapY - this.posY;
        distances[2] = this.mapX - this.posX;
        distances[3] = this.posY;
        
        int maxIndx = 0;
        
        for(int i = 0; i < distances.length; i++){
            if (distances[i] > distances[maxIndx]){
                maxIndx = i;
            }
        }
        
        return maxIndx;
    }
    
    public int getMovementDirection(){
        return this.movementDirection;
    }
    
    public void setType(SeaFoodType type){
        this.type = type;
    }
    
    public SeaFoodType getType(){
        return this.type;
    }
    
    public void setQuantity(float quantity){
        this.quantity = quantity;
    }
    
    public float getQuantity(){
        return this.quantity;
    }
    
    public Boolean onTheMap(){
        if (posX >= 0 & posY >= 0 & posX <= this.mapX & posY <= this.mapX){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Movements of the seafood. The next position depends on the movement 
     * direction. 0 North, 1 East, 2 South, 3 West, and -1 no movement.
     */
    public void move(){
        if(this.blocks > 0) return;
        
        if(this.movementDirection == 0){
            this.posX -= 1;
        }else if(this.movementDirection == 1){
            this.posY += 1;
        }else if(this.movementDirection ==2){
            this.posX += 1;
        }else{
            this.posY -= 1;
        }
    }
    
    public boolean isBlockable(BoatPosition bp)
    {
        int boatPosX = bp.getRow();
        int boatPosY = bp.getColumn();
        
        if((getPosY() > boatPosY && getMovementDirection() == 1)||(getPosY() < boatPosY && getMovementDirection() == 3)||(getPosX() > boatPosX && getMovementDirection() == 2)||(getPosX() < boatPosX && getMovementDirection() == 0)){
            return false;
        }
        
        int dX = Math.abs(posX - boatPosX);
        int dY = Math.abs(posY - boatPosY);
        if(movementDirection%2==0){
            if (!(dX >= dY - 1)) return false;
        }else{
            if (!(dY >= dX - 1)) return false;
        }
        
        return true;
    }
    
    @Override
    public String toString()
    {
        return String.valueOf(posX) + " " + String.valueOf(posY)
                + " " + this.getType().toString() 
                + " " + String.valueOf(movementDirection);
    }
    
    
    /**
     * A boat blocks the fish and it stops. The first block stops the fish
     * setting the movement direction different from the defined directions.
     * This method should be called from one of the 4 boats.
     */
    public void block()
    {
        this.blocks++; // Each boat trying to catch it blockes a way.
        this.movementDirection = -1;
    }      
    
    /**
     * Is surrounded by the 4 corresponding boats. All boats blocked the fish on
     * its particular one of the four ways.
     * @return Is surrounded by the four corresponding4 boats.
     */
    public boolean isSurrounded()
    {
        return this.blocks == 4;
    }
}