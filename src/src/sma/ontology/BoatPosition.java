/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import jade.core.*;

/**
 *
 * @author gaspercat, aclapes
 */
public class BoatPosition implements java.io.Serializable{
    private AID id = null;
    private int row = -1;
    private int column = -1;
    
    private boolean isDestination;
    
    // For leaders
    private SeaFood catchedSeafood;
    
    public BoatPosition(AID id, int row, int column){
        this.id = id;
        this.row = row;
        this.column = column;
        this.isDestination = false;
        this.catchedSeafood = null;
    }    
    
    public BoatPosition(AID id, int row, int column, boolean destination){
        this.id = id;
        this.row = row;
        this.column = column;
        this.isDestination = destination;
        this.catchedSeafood = null;
    }    
    
    public BoatPosition(AID id, int row, int column, boolean destination, SeaFood bsf){
        this.id = id;
        this.row = row;
        this.column = column;
        this.isDestination = destination;
        this.catchedSeafood = bsf;
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
    
    public boolean equals(Object boat)
    {
        if (boat == null) return false;
        if (this.getClass() != boat.getClass()) return false;
        
        if (!this.id.equals(((BoatPosition)boat).id)) return false;
        
        return true;
    }
    
    public String toString(){
        return "(" + this.id.getName() + "=" + this.row + "," + this.column + ")";
    }
    
    @Override
    public BoatPosition clone()
    {
        return new BoatPosition ((AID) id.clone(), this.row, this.column, this.isDestination, this.catchedSeafood);
    }
    
    public boolean isDestination()
    {
        return isDestination;
    }
    
    public void catchSeafood(SeaFood sf)
    {
        this.catchedSeafood = sf;
    }
    
    public SeaFood getCatchedSeafood()
    {
        return this.catchedSeafood;
    }
}