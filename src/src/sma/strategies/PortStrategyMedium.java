package sma.strategies;

import sma.ontology.DepositsLevel;

public class PortStrategyMedium extends PortStrategy {
    public PortStrategyMedium(PortAgent port, DepositsLevel levels){
        super(port, levels);
    }
    
    @Override
    protected void evaluate_offer() {
        if(!is_offer_acceptable()){
            this.is_rejected = true;
            return;
        }
        
        // Set price to offer
        this.offer = calculate_medium_price();
        if(this.offer > this.port.available_money){
            this.offer = this.port.available_money;
        }
        
        // Acccept offer
        this.is_rejected = false;
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
        double min_price = calculate_minimum_price();
        
        // Reject if can't offer minimum price
        if(this.port.available_money < min_price) return false;
        return true;
    }
    
    private boolean is_offer_confirmable() {
        if(this.offer == 0 || this.offer > this.port.available_money) return false;
        return true;
    }
}
