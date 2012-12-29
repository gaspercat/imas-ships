/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import sma.ontology.SeaFoodType;

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
    
    // ** GETTER METHODS
    // *****************************************

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
    
    public double getSeafoodLevel(SeaFoodType type){
        if(type == SeaFoodType.Lobster) return this.lobsterLevel;
        if(type == SeaFoodType.Tuna) return this.tunaLevel;
        if(type == SeaFoodType.Octopus) return this.octopusLevel;
        if(type == SeaFoodType.Shrimp) return this.shrimpLevel;
        
        return -1;
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
    
    public double getFreeSpaceSeafood(SeaFoodType type){
        if(type == SeaFoodType.Lobster) return this.capacity - this.lobsterLevel;
        if(type == SeaFoodType.Tuna) return this.capacity - this.tunaLevel;
        if(type == SeaFoodType.Octopus) return this.capacity - this.octopusLevel;
        if(type == SeaFoodType.Shrimp) return this.capacity - this.shrimpLevel;
        
        return -1;
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

    // ** SETTER METHODS
    // *****************************************
    
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
    
    // ** OPERATORS
    // *****************************************
    
    public void add(DepositsLevel deposits){
        this.lobsterLevel += deposits.lobsterLevel;
        if(this.lobsterLevel > this.capacity) this.lobsterLevel = this.capacity;
        
        this.octopusLevel += deposits.octopusLevel;
        if(this.octopusLevel > this.capacity) this.octopusLevel = this.capacity;
        
        this.shrimpLevel += deposits.shrimpLevel;
        if(this.shrimpLevel > this.capacity) this.shrimpLevel = this.capacity;
        
        this.tunaLevel += deposits.tunaLevel;
        if(this.tunaLevel > this.capacity) this.tunaLevel = this.capacity;
    }
    
    public void empty(){
        this.lobsterLevel = 0;
        this.octopusLevel = 0;
        this.shrimpLevel = 0;
        this.tunaLevel = 0;
    }
    
    public DepositsLevel addition(DepositsLevel deposits){
        double capacity = (this.capacity > deposits.capacity) ? this.capacity : deposits.capacity;
        DepositsLevel ret = new DepositsLevel(capacity);
        
        ret.lobsterLevel = this.lobsterLevel + deposits.lobsterLevel;
        if(ret.lobsterLevel > ret.capacity) ret.lobsterLevel = ret.capacity;
        
        ret.octopusLevel = this.octopusLevel + deposits.octopusLevel;
        if(ret.octopusLevel > ret.capacity) ret.octopusLevel = ret.capacity;
        
        ret.shrimpLevel = this.shrimpLevel + deposits.shrimpLevel;
        if(ret.shrimpLevel > ret.capacity) ret.shrimpLevel = ret.capacity;
        
        ret.tunaLevel = this.tunaLevel + deposits.tunaLevel;
        if(ret.tunaLevel > ret.capacity) ret.tunaLevel = ret.capacity;
        
        return ret;
    }

    @Override
    public String toString() {
        return "DepositsLevel{" + "tunaLevel=" + tunaLevel + ", lobsterLevel=" + lobsterLevel + ", octopusLevel=" + octopusLevel + ", shrimpLevel=" + shrimpLevel + ", capacity=" + capacity + '}';
    }
    
    
    
}
