/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

/**
 *
 * @author joan
 */
public class DepositsLevel implements java.io.Serializable{
    private double tunaLevel;
    private double lobsterLevel;
    private double octopusLevel;
    private double shrimpLevel;
    private double capacity;
    
    public DepositsLevel(double capacity){
        this.capacity = capacity;
        
        this.tunaLevel = 0;
        this.lobsterLevel = 0;
        this.octopusLevel = 0;
        this.shrimpLevel = 0;
    }

    public double getTunaLevel() {
        return this.tunaLevel;
    }

    public double getLobsterLevel() {
        return this.lobsterLevel;
    }

    public double getOctopusLevel() {
        return this.octopusLevel;
    }

    public double getShrimpLevel() {
        return this.shrimpLevel;
    }
    
    public double getTotalAmount() {
        int ret = 0;
        
        ret += this.lobsterLevel;
        ret += this.tunaLevel;
        ret += this.octopusLevel;
        ret += this.shrimpLevel;
        
        return ret;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setTunaLevel(double tunaLevel) {
        this.tunaLevel = tunaLevel;
    }

    public void setLobsterLevel(double lobsterLevel) {
        this.lobsterLevel = lobsterLevel;
    }

    public void setOctopusLevel(double octopusLevel) {
        this.octopusLevel = octopusLevel;
    }

    public void setShrimpLevel(double shrimpLevel) {
        this.shrimpLevel = shrimpLevel;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }
    
    public double getFreeSpaceTuna(){
        return this.capacity - this.tunaLevel;
    }
    
    public double getFreeSpaceLobster(){
        return this.capacity - this.lobsterLevel;
    }
    
    public double getFreeSpaceOctopus(){
        return this.capacity - this.octopusLevel;
    }
    
    public double getFreeSpaceShrimp(){
        return this.capacity - this.shrimpLevel;
    }
}
