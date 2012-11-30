/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.strategies;

import sma.ontology.DepositsLevel;

/**
 *
 * @author gaspercat
 */
public class PortStrategySteady extends PortStrategy {
    public PortStrategySteady(PortAgent port, DepositsLevel levels){
        super(port, levels);
    }
    
    @Override
    protected void evaluate_offer() {
        double mse = calculate_mse();
        
    }

    @Override
    protected void confirm_offer() {
        
    }
    
    // Calculate mean squared error of the deposits balance
    private double calculate_mse(){
        double mean = this.levels.getTotalAmount() / 4;
        
        double mse = 0;
        
        mse += Math.sqrt(mean - this.levels.getLobsterLevel());
        mse += Math.sqrt(mean - this.levels.getOctopusLevel());
        mse += Math.sqrt(mean - this.levels.getShrimpLevel());
        mse += Math.sqrt(mean - this.levels.getTunaLevel());
        
        return mse;
    }
}
