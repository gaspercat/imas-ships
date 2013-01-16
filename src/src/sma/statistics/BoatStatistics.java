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
        private double mean_movements;
        private DepositsLevel mean_fished;
        
        public CycleStats(ArrayList<ArrayList<InfoBox>> turns){
            // Get mean benefit
            mean_money = 0;
            for(ArrayList<InfoBox> boat: turns){
                mean_money += boat.get(boat.size()-1).getEuros();
            }
            mean_money /= turns.size();
            
            // Get mean amount of fished seafood & movements
            mean_fished = new DepositsLevel();
            mean_movements = 0;
            for(ArrayList<InfoBox> boat: turns){
                mean_fished.add(boat.get(boat.size()-2).getDeposit());
                mean_movements += boat.get(boat.size()-2).getNumMovements();
            }
            mean_fished.divide(turns.size());
            mean_movements /= turns.size();
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
        this.gui.showStatistics("\nFinal statistics - Boats:\n");
        // TODO
    }
}
