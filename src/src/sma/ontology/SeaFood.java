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
    int posX, posY, mapX, mapY, movementDirection;
    SeaFoodType type;
    
    public SeaFood(SeaFoodType type, int posX, int posY, int mapX, int mapY){
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.mapX = mapX-1;
        this.mapY = mapY-1;
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
        distances[0] = this.posY;
        distances[1] = this.mapX - this.posX;
        distances[2] = this.mapY - this.posY;
        distances[3] = this.posX;
        
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
    
    public Boolean onTheMap(){
        if (posX >= 0 & posY >= 0 & posX < this.mapX & posY < this.mapX){
            return true;
        }else{
            return false;
        }
    }
    
    public void move(){
        if(this.movementDirection == 0){
            this.posY -= 1;
        }else if(this.movementDirection == 1){
            this.posX += 1;
        }else if(this.movementDirection ==2){
            this.posY += 1;
        }else{
            this.posX -= 1;
        }
    }
}