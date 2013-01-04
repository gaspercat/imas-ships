/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import jade.core.*;

/**
 *
 * @author gaspercat
 */
public class BoatPosition implements java.io.Serializable{
    private AID id = null;
    private int row = -1;
    private int column = -1;
    
    public BoatPosition(AID id, int row, int column){
        this.id = id;
        this.row = row;
        this.column = column;
    }
    
    public AID getAID(){
        return this.id;
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
    
    @Override
    public BoatPosition clone()
    {
        return new BoatPosition ((AID) id.clone(), this.row, this.column);
    }
}