/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import jade.core.*;
import java.io.Serializable;

/**
 *
 * @author gaspercat, aclapes
 */
public class BoatPosition implements java.io.Serializable{
    private AID id = null;
    private int row = -1;
    private int column = -1;
    
    public BoatPosition() { }
    
    public BoatPosition(AID id, int row, int column){
        this.id = id;
        this.row = row;
        this.column = column;
    }
    
    public AID getAID(){
        return this.id;
    }

    public void setAID(AID id) {
        this.id = id;
    }
    
    public int getRow(){
        return this.row;
    }
    
    public int getColumn(){
        return this.column;
    }
    
    public void setRow(int row){
        this.row = row;
    }
    
    public void setColumn(int column){
        this.column = column;
    }
    
    public boolean equals(BoatPosition boat){
        return this.id.equals(boat.id);
    }
    
    public String toString(){
        return "(" + this.id.getName() + "=" + this.row + "," + this.column + ")";
    }
    
    public BoatPosition clone()
    {
        return new BoatPosition ((AID) id.clone(), this.row, this.column);
    }
}
