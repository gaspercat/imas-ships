/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import jade.core.*;

/**
 *
 * @author joan
 */
public class SeaFood {
    int posX, posY, mapX, mapY, movementDirection;
    SeaFoodType type;
    
    public SeaFood(SeaFoodType type, int posX, int posY, int mapX, int mapY){
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.mapX = mapX;
        this.mapY = mapY;
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
    
    public void setMovementDirection(int MovementDirection){
        this.movementDirection = movementDirection;
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
}