package sma.strategies;

import java.util.ArrayList;
import sma.ontology.DepositsLevel;
import sma.ontology.SeaFoodType;

public class PortStrategyCheap extends PortStrategy {
    private class FishType{
        private SeaFoodType type;
        private double      min_price;
        private double      max_price;
        
        public FishType(SeaFoodType type, double min_price, double max_price){
            this.type = type;
            this.min_price = min_price;
            this.max_price = max_price;
        }
        
        public SeaFoodType getType(){
            return this.type;
        }
        
        public double getMinPrice(){
            return this.min_price;
        }
        
        public double getPriceMargin(){
            return this.max_price - this.min_price;
        }
    }
    
    public PortStrategyCheap(PortAgent port, DepositsLevel levels){
        super(port, levels);
    }
    
    @Override
    protected void evaluate_offer() {
        DepositsLevel plevels = this.port.deposits;
        
        if(!is_offer_acceptable()){
            this.is_rejected = true;
            return;
        }
        
        // Get seafood types ordered by rank
        ArrayList<FishType> rank = rankFishes();
        
        // Calculate the offer price
        this.offer = 0;
        double acc_prop = 0;
        for(int i=0;i<rank.size();i++){
            FishType type = rank.get(i);
            
            // Get proportion of deposit free & relevance of seafood type
            double free_prop = plevels.getFreeSpaceSeafood(type.getType()) / plevels.getCapacity();
            double relevance = i + 1;
            
            // Calculate seafood price
            double relev = free_prop * (5 - acc_prop) / 4;
            Double price = new Double(type.getMinPrice() + relev*type.getPriceMargin());
            
            // Calculate useful volume
            double volume = this.levels.getSeafoodLevel(type.getType());
            if(volume > this.port.deposits.getFreeSpaceSeafood(type.getType())){
                volume = this.port.deposits.getFreeSpaceSeafood(type.getType());
            }
            
            // Accumulate price to offer
            this.offer += this.levels.getSeafoodLevel(type.getType()) * price;
            
            // Accumulate proportions
            acc_prop += free_prop;
        }
        
        // If price to offer is higher than available money, lower offer
        if(this.offer > this.port.available_money){
            this.offer = this.port.available_money;
        }
        
        // If price to offer is lower than minimum, reject offer
        if(this.offer < calculate_minimum_price()){
            this.is_rejected = true;
            return;
        }
        
        
        // Set price to offer and accept
        this.offer = calculate_minimum_price();
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
    
    private ArrayList<FishType> rankFishes(){
        ArrayList<FishType> ret = new ArrayList<FishType>();
        
        ret.add(new FishType(SeaFoodType.Tuna, this.MIN_TUNA, this.MAX_TUNA));
        ret.add(new FishType(SeaFoodType.Lobster, this.MIN_LOBSTER, this.MAX_LOBSTER));
        ret.add(new FishType(SeaFoodType.Octopus, this.MIN_OCTOPUS, this.MAX_OCTOPUS));
        ret.add(new FishType(SeaFoodType.Shrimp, this.MIN_SHRIMP, this.MAX_SHRIMP));
        
        return ret;
    }
}
