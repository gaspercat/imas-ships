package sma.strategies;

import sma.ontology.DepositsLevel;
import sma.PortAgent;

public class PortStrategySteady extends PortStrategy {
    private final double MAX_MSE = 200;
    
    public PortStrategySteady(PortAgent port, DepositsLevel levels){
        super(port, levels);
    }
    
    @Override
    protected void evaluate_offer() {
        if(!is_offer_acceptable()){
            this.is_rejected = true;
            return;
        }
        
        DepositsLevel current = this.port.getDeposits();
        DepositsLevel future = this.port.getDeposits().addition(this.levels);
        double mse_current = calculate_mse(current);
        double mse_future = calculate_mse(future);
        double inc = future.getTotalAmount() - current.getTotalAmount();
        
        double min_price = calculate_minimum_price();
        double max_price = calculate_maximum_price();
        
        // Calculate price to offer
        double pdiff = max_price - min_price;
        double factor = ((mse_current - mse_future) + inc) / (mse_current + this.levels.getCapacity()*4);
        double price =  min_price + factor*pdiff;
        
        // Set price to offer and accept
        this.offer = price;
        this.is_rejected = false;
        this.is_aborted = false;
    }

    @Override
    protected void confirm_offer() {
        if(!is_offer_confirmable() || !is_offer_acceptable()){
            this.is_aborted = true;
            return;
        }
        
        this.is_aborted = false;
    }
    
    private boolean is_offer_acceptable() {
        double mse_future = calculate_mse(this.port.getDeposits().addition(this.levels));
        
        double min_price = calculate_minimum_price();
        // Reject if can't offer minimum price
        if(this.port.getMoney() < min_price){
            return false;
        }
        
        // Reject if mean suqared error is too high
        if(mse_future > MAX_MSE){
            return false;
        }
        
        return true;
    }
    
    private boolean is_offer_confirmable() {
        if(this.offer == 0 || this.offer > this.port.getMoney()) return false;
        return true;
    }
    
    // Calculate mean squared error of the deposits balance
    private double calculate_mse(DepositsLevel levels){
        
        double mean = levels.getTotalAmount() / 4;
        
        double mse = 0;
        
        mse += Math.pow(mean - levels.getLobsterLevel(), 2);
        mse += Math.pow(mean - levels.getOctopusLevel(), 2);
        mse += Math.pow(mean - levels.getShrimpLevel(), 2);
        mse += Math.pow(mean - levels.getTunaLevel(), 2);
        
        return mse;
    }
}
