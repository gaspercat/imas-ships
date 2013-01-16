/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.statistics;

import java.util.ArrayList;
import sma.gui.GraphicInterface;

import sma.ontology.InfoBox;
import sma.ontology.DepositsLevel;
import sma.ontology.InfoGame;

/**
 *
 * @author Marc
 */
public class BoatStatistics extends Statistics{
    private class CycleStats{
        private double mean_money;
        private double stdev_money;
        private double mean_movements;
        private double stdev_movements;
        private DepositsLevel mean_fished;
        
        public CycleStats(ArrayList<ArrayList<InfoBox>> turns){
            // Get mean benefit
            mean_money = 0;
            for(ArrayList<InfoBox> boat: turns){
                mean_money += boat.get(boat.size()-1).getEuros();
            }
            mean_money /= turns.size();
            stdev_money = 0;
            for(ArrayList<InfoBox> boat: turns){
                double eur = boat.get(boat.size()-1).getEuros();
                stdev_money += (eur - mean_money) * (eur - mean_money);
            }
            stdev_money /= turns.size();
            stdev_money = Math.sqrt(stdev_money);
            // Get mean amount of fished seafood & movements
            mean_fished = new DepositsLevel();
            mean_movements = 0;
            for(ArrayList<InfoBox> boat: turns){
                mean_fished.add(boat.get(boat.size()-2).getDeposit());
                mean_movements += boat.get(boat.size()-2).getNumMovements();
            }

            mean_fished.divide(turns.size());
            mean_movements /= turns.size();
             stdev_movements = 0;
            for(ArrayList<InfoBox> boat: turns){
                double mov = boat.get(boat.size()-2).getNumMovements();
                stdev_movements += (mov - mean_movements) * (mov - mean_movements);
            }
            stdev_movements /= turns.size();
            stdev_movements = Math.sqrt(stdev_movements);
        }

        
    }
    
    private ArrayList<CycleStats> cycles;
    
    public BoatStatistics(InfoGame game, GraphicInterface gui, ArrayList<String> names){
        super(game, gui, names);
        
        // Build cycles list
        cycles = new ArrayList<CycleStats>();
    }
    
    public int getTurnNumber(){
        return this.cycles.size();
    }
    
    public double getCycleMeanBenefit(){
        if(cycles.isEmpty()) return 0;
        
        if(cycles.size() == 1){
            return cycles.get(0).mean_money;
        }else{
            int n = cycles.size();
            return cycles.get(n-1).mean_money - cycles.get(n-2).mean_money;
        }
    }
    
    public DepositsLevel getCycleMeanFished(){
        if(cycles.isEmpty()) return new DepositsLevel();
        
        int n = cycles.size();
        return cycles.get(n-1).mean_fished;
    }
    
    public double getCycleMeanMovements(){
        if(cycles.isEmpty()) return 0;
        return cycles.get(cycles.size()-1).mean_movements;
    }
    
    
    public boolean hasFinished() {
        return this.cycles.size() == this.game.getInfo().getNumNegotiationPhases();
    }
    
    // Private anaysis & printing methods
    // ********************************************
    
    @Override
    protected void analyzeStatistics(){
        // If last turn was a negotiation turn
        ArrayList<InfoBox> ship = this.info.get(0);
        if(ship.size() == game.getInfo().getNumFishingPhasesToNegotiate() + 1){
            // Build cycle statistics and remove turns data
            cycles.add(new CycleStats(this.info));
            for(ArrayList<InfoBox> boat: this.info){
                InfoBox tmp = boat.get(boat.size()-1);
                boat.clear();
                boat.add(tmp);
            }
            
            // Print cycle statistics
            this.showTurnStatistics();
            if(this.cycles.size() == game.getInfo().getNumNegotiationPhases()){
                showFinalStatistics();
            }
        }
    }
    
    private void showTurnStatistics(){
        this.gui.showStatistics("\nNegotiation turn" + this.getTurnNumber() + " - Boats:\n");

        // STATISTICS OF THE BOATS
        // ********************************

        this.gui.showStatistics("  Mean boats benefit:\t" + this.getCycleMeanBenefit() + "\n");
        this.gui.showStatistics("  Mean boats movments:\t" + this.getCycleMeanMovements() + "\n");

        DepositsLevel levels = this.getCycleMeanFished();
        this.gui.showStatistics("  Mean boats fished lobster:\t" + levels.getLobsterLevel() + "\n");
        this.gui.showStatistics("  Mean boats fished octopus:\t" + levels.getOctopusLevel() + "\n");
        this.gui.showStatistics("  Mean boats fished shrimp:\t" + levels.getShrimpLevel() + "\n");
        this.gui.showStatistics("  Mean boats fished tuna:\t" + levels.getTunaLevel() + "\n");
    }
    
    private void showFinalStatistics(){
        double mean_money = 0;
        double std_money = 0;
        double mean_mov = 0;
        double std_mov = 0;
        DepositsLevel mean_dep = new DepositsLevel();
        for(CycleStats cy : this.cycles){
            mean_money += cy.mean_money;
            std_money += cy.stdev_money;
            mean_mov += cy.mean_movements;
            std_mov += cy.stdev_movements;
            mean_dep.add(cy.mean_fished);
        }        
        mean_money /= this.cycles.size();
        std_money /= this.cycles.size();
        mean_mov /= this.cycles.size();
        std_mov /= this.cycles.size();
        mean_dep.divide(this.cycles.size());
             
        this.gui.showStatistics("\nFinal statistics - Boats:\n");
        
        this.gui.showStatistics("  Mean boats benefit:\t" + mean_money + "\n");
        this.gui.showStatistics("  Mean boats movments:\t" + mean_mov+ "\n");
        
        this.gui.showStatistics("  Stdev boats benefit:\t" + std_money + "\n");
        this.gui.showStatistics("  Stdev boats movments:\t" + std_mov+ "\n");
        
        this.gui.showStatistics("  Mean boats fished lobster:\t" + mean_dep.getLobsterLevel() + "\n");
        this.gui.showStatistics("  Mean boats fished octopus:\t" + mean_dep.getOctopusLevel() + "\n");
        this.gui.showStatistics("  Mean boats fished shrimp:\t" + mean_dep.getShrimpLevel() + "\n");
        this.gui.showStatistics("  Mean boats fished tuna:\t" + mean_dep.getTunaLevel() + "\n");       
    }
}
