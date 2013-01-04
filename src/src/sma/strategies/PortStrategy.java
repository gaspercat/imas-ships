/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.strategies;

import sma.PortAgent;
import sma.ontology.DepositsLevel;
import sma.ontology.PortType;

/**
 *
 * @author gaspercat
 */
public abstract class PortStrategy {
    public static final int STRATEGY_STEADY    = 1;
    public static final int STRATEGY_MINIMUM   = 2;
    public static final int STRATEGY_MAXIMUM   = 3;
    public static final int STRATEGY_MEDIUM    = 4;
    public static final int STRATEGY_EXPENSIVE = 5;
    public static final int STRATEGY_CHEAP     = 6;
    
    protected final double MIN_TUNA    = 1.0;
    protected final double MAX_TUNA    = 2.0;
    protected final double MIN_OCTOPUS = 2.0;
    protected final double MAX_OCTOPUS = 3.0;
    protected final double MIN_LOBSTER = 1.5;
    protected final double MAX_LOBSTER = 2.5;
    protected final double MIN_SHRIMP  = 3.0;
    protected final double MAX_SHRIMP  = 5.0;
    
    protected PortAgent     port;
    protected DepositsLevel levels;
    
    protected boolean       is_rejected;
    protected boolean       is_aborted;
    protected double        offer;
    
    public static PortStrategy create(PortAgent port, DepositsLevel levels){
        PortType type = port.getType();
        
        if(type == PortType.Steady) return new PortStrategySteady(port, levels);
        if(type == PortType.Minimum) return new PortStrategyMinimum(port, levels);
        if(type == PortType.Maximum) return new PortStrategyMaximum(port, levels);
        if(type == PortType.Medium) return new PortStrategyMedium(port, levels);
        if(type == PortType.Expensive) return new PortStrategyExpensive(port, levels);
        if(type == PortType.Cheap) return new PortStrategyCheap(port, levels);
        
        return null;
    }
    
    protected PortStrategy(PortAgent port, DepositsLevel levels){
        this.port = port;
        this.levels = levels;
        
        this.is_aborted = true;
        this.is_rejected = true;
        offer = 0;
    }
    
    /*
     * Check if the order is finally bought after the boat has accepted the offer
     * given by the port.
     * @return True if the transacition is finally aborted, false if accepted
     */
    public boolean isRejected(){
        evaluate_offer();
        return is_rejected;
    }
    
    /*
     * Check if the order is finally bought after the boat has accepted the offer
     * given by the port.
     * @return True if the transacition is finally aborted, false if accepted
     */
    public boolean isAborted(){
        confirm_offer();
        return is_aborted;
    }
    
    /*
     * Return the ammount of money to be offered to the boat agent
     * @return Value > 0 if not rejected, 0 otherwise
     */
    public double getOffer(){
        //TODO configar que || this.is_aborted sobra aqui! MARC
        if(this.is_rejected ) return 0;
        return this.offer;
    }
    
    public DepositsLevel getDeposits(){
        return this.levels;
    }
    
    protected abstract void evaluate_offer();
    protected abstract void confirm_offer();
    
    protected double calculate_minimum_price(){
        double ret = 0;
        if(this.levels == null){
            System.out.println("FATAL NULL ERROR AT "+ port.getLocalName());
        }
        ret += this.levels.getTunaLevel() * MIN_TUNA;
        ret += this.levels.getOctopusLevel() * MIN_OCTOPUS;
        ret += this.levels.getLobsterLevel() * MIN_LOBSTER;
        ret += this.levels.getShrimpLevel() * MIN_SHRIMP;
        return ret;
    }
    
    protected double calculate_medium_price(){
        double ret = 0;
        
        ret += this.levels.getTunaLevel() * (MIN_TUNA + MAX_TUNA) / 2;
        ret += this.levels.getOctopusLevel() * (MIN_OCTOPUS + MAX_OCTOPUS) / 2;
        ret += this.levels.getLobsterLevel() * (MIN_LOBSTER + MAX_OCTOPUS) / 2;
        ret += this.levels.getShrimpLevel() * (MIN_SHRIMP + MAX_SHRIMP) / 2;
        
        return ret;
    }
    
    protected double calculate_maximum_price(){
        double ret = 0;
        
        ret += this.levels.getTunaLevel() * MAX_TUNA;
        ret += this.levels.getOctopusLevel() * MAX_OCTOPUS;
        ret += this.levels.getLobsterLevel() * MAX_LOBSTER;
        ret += this.levels.getShrimpLevel() * MAX_SHRIMP;
        
        return ret;
    }
}
