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
    protected PortAgent     port;
    protected DepositsLevel levels;
    
    protected boolean       is_rejected;
    protected boolean       is_aborted;
    protected double        offer;
    
    public PortStrategy(PortAgent port, DepositsLevel levels){
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
}
