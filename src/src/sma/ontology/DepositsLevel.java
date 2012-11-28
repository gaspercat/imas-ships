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
    private double tunaLevel, lobsterLevel, octopusLevel, shrimpLevel = 0;
    private double capacity;
    
    public DepositsLevel(double capacity){
        this.capacity = capacity;
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
