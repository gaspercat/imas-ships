/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author gaspercat
 */
public class BoatsPosition implements java.io.Serializable{
    List<BoatPosition> positions;
    
    public BoatsPosition(){
        this.positions = new ArrayList<BoatPosition>();
    }
    
    public BoatsPosition(List<BoatPosition> positions){
        this.positions = positions;
    }
    
    public List<BoatPosition> getBoatsPositions(){
        return this.positions;
    }
    
    public void setBoatPosition(BoatPosition position){
        // If boat position present, replace
        for(BoatPosition boat : positions){
            if(boat.equals(position)){
                boat.setRow(position.getRow());
                boat.setColumn(position.getColumn());
                return;
            }
        }
        
        // If boat position not present, add to list
        this.positions.add(position);
    }
}
