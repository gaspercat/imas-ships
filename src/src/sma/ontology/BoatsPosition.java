/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import jade.core.AID;
import java.util.List;
import java.util.ArrayList;

/*
 *
 * @author gaspercat
 */
public class BoatsPosition implements java.io.Serializable{
    private List<BoatPosition> positions;
    private ArrayList<SeaFood> seafoods;

    private boolean allSeafoodsBlocked;
    
    public BoatsPosition(){
        this.positions = new ArrayList<>();
        this.seafoods = new ArrayList<>();
        allSeafoodsBlocked = false;
    }
    
    public BoatsPosition(List<BoatPosition> positions){
        this.positions = positions;
        this.seafoods = new ArrayList<>();
        allSeafoodsBlocked = false;
    }
    
    public BoatPosition[] getBoatsPositions(){
        BoatPosition[] ret = new BoatPosition[positions.size()];
        
        for(int i=0;i<this.positions.size();i++){
            ret[i] = this.positions.get(i);
        }
        
        return ret;
    }
    
    public ArrayList<BoatPosition> getBoatsPositionsArrayList()
    {
        return (ArrayList<BoatPosition>) positions;
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
    
    public void addPosition(BoatPosition bp){
        this.positions.add(bp);
    }
    
    public void removePosition(BoatPosition bp)
    {
        this.positions.remove(bp);
    }
    
    @Override
    public String toString(){
        String ret = "BOATPOSITION and fish:";
        
        for(BoatPosition bp : positions){
            ret = ret + bp.toString();
        }
        if(positions.size() == 0) ret = "Empty";
        
        return ret;
    }
    
    public BoatPosition get(AID id)
    {
        for (BoatPosition bp : positions)
        {
            if (bp.getAID().equals(id))
                return bp;
        }
        
        return null;
    }
    
    public ArrayList<SeaFood> getSeafoods()
    {
        return this.seafoods;
    }
    
    public void setSeafoods(ArrayList<SeaFood> seafoods)
    {
        this.seafoods = seafoods;
    }
    
    public void declareAllSeafoodsBlocked()
    {
        this.allSeafoodsBlocked = true;
    }
                
    public boolean areAllBoatsPositioned()
    {
        boolean allpositioned = true;
        for (BoatPosition bp : positions)
        {
            if (!bp.isDestination())
                return false;
        }
        return true;
    }
    
    public boolean areAllSeafoodsBlocked()
    {
        return this.allSeafoodsBlocked;
    }

}
