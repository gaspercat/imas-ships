/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.strategies;

import sma.PortAgent;
import sma.ontology.DepositsLevel;

/**
 *
 * @author gaspercat
 */
public abstract class PortStrategy {
    public static final int STRATEGY_STEADY  = 1;
    public static final int STRATEGY_MINIMUM = 2;
    public static final int STRATEGY_MAXIMUM = 3;
    
    private final double MIN_TUNA    = 1.0;
    private final double MAX_TUNA    = 2.0;
    private final double MIN_OCTOPUS = 2.0;
    private final double MAX_OCTOPUS = 3.0;
    private final double MIN_LOBSTER = 1.5;
    private final double MAX_LOBSTER = 2.5;
    private final double MIN_SHRIMP  = 3.0;
    private final double MAX_SHRIMP  = 5.0;
    
    protected PortAgent     port;
    protected DepositsLevel levels;
    
    protected boolean       is_rejected;
    protected boolean       is_aborted;
    protected double        offer;
    
    public static PortStrategy create(int type, PortAgent port, DepositsLevel levels){
        switch(type){
            case STRATEGY_STEADY:   return new PortStrategySteady(port, levels);
            case STRATEGY_MINIMUM:  return new PortStrategyMinimum(port, levels);
            case STRATEGY_MAXIMUM:  return new PortStrategyMaximum(port, levels);
        }
        
        return null;
    }
    
    private PortStrategy(PortAgent port, DepositsLevel levels){
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
        if(this.is_rejected || this.is_aborted) return 0;
        return this.offer;
    }
    
    protected abstract void evaluate_offer();
    protected abstract void confirm_offer();
    
    protected double calculate_minimum_price(){
        double ret = 0;
        
        ret += this.levels.getTunaLevel() * MIN_TUNA;
        ret += this.levels.getOctopusLevel() * MIN_OCTOPUS;
        ret += this.levels.getLobsterLevel() * MIN_LOBSTER;
        ret += this.levels.getShrimpLevel() * MIN_SHRIMP;
        
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
