/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.statistics;

import java.util.ArrayList;
import sma.gui.GraphicInterface;
import sma.ontology.DepositsLevel;
import sma.ontology.InfoBox;
import sma.ontology.InfoGame;

/**
 *
 * @author Marc
 */
public class PortStatistics extends Statistics{
    private class CycleStats{
        double[] money;
        DepositsLevel[] deposits;
                
        private double mean_money;
        private DepositsLevel mean_deposits;
        
        public CycleStats(int n){
            this.money = new double[n];
            this.deposits = new DepositsLevel[n];
            
            this.mean_money = game.getInfo().getMoneyPorts();
            this.mean_deposits = new DepositsLevel();
            
            for(int i=0;i<n;i++){
                this.money[i] = this.mean_money;
                this.deposits[i] = new DepositsLevel();
            }
        }
        
        public CycleStats(ArrayList<ArrayList<InfoBox>> turns){
            // Initialize individual port data
            money = new double[turns.size()];
            deposits = new DepositsLevel[turns.size()];
            
            // Get mean benefit & deposit levels
            mean_money = 0;
            mean_deposits = new DepositsLevel();
            for(ArrayList<InfoBox> port: turns){
                // Save individual port data
                int idx = getIndex(port.get(0).getName());
                money[idx] = port.get(0).getEuros();
                deposits[idx] = port.get(0).getDeposit();
                
                // Add to means
                mean_money += port.get(0).getEuros();
                mean_deposits.add(port.get(0).getDeposit());
                port.clear();
            }
            mean_money /= turns.size();
            mean_deposits.divide(turns.size());
        }
    }
    
    private ArrayList<CycleStats> cycles;
    
    public PortStatistics(InfoGame game, GraphicInterface gui, ArrayList<String> names){
        super(game, gui, names);
        
        // Build cycles list
        cycles = new ArrayList<CycleStats>();
        cycles.add(new CycleStats(names.size()));
    }
    
    public int getTurnNumber(){
        return this.cycles.size() - 1;
    }
    
    // Private anaysis & printing methods
    // ********************************************
    
    @Override
    protected void analyzeStatistics(){
        // Build cycle statistics and remove turns data
        cycles.add(new CycleStats(this.info));
        for(ArrayList<InfoBox> port: this.info){
            port.clear();
        }

        // Print cycle statistics
        this.showTurnStatistics();
        if(this.cycles.size() == 6){
            showFinalStatistics();
        }
    }
    
    private void showTurnStatistics(){
        this.gui.showStatistics("\nNegotiation turn" + this.getTurnNumber() + " - Ports:\n");

        // STATISTICS OF THE PORTS
        // ********************************

        // Get current and previous cycle data, number of ports
        int n = this.names.size();
        CycleStats c_curr = this.cycles.get(this.cycles.size()-1);
        CycleStats c_prev = this.cycles.get(this.cycles.size()-2);
        
        // Sow mean buy price by boat and benefits
        for(int i=0;i<n;i++){
            
            double spent = c_prev.money[i] - c_curr.money[i];
            double bought = c_curr.deposits[i].getTotalAmount() - c_prev.deposits[i].getTotalAmount();
                    
            gui.showStatistics("  For " + this.names.get(i) + ":\tSpent = ");
            gui.showStatistics(spent+"€");
            gui.showStatistics(", Amount bought = ");
            gui.showStatistics(bought+"u.");
            gui.showStatistics(", Price/unit = ");
            gui.showStatistics(((bought > 0) ? (spent / bought) : 0)+"€/u.");
            gui.showStatistics("\n");
        }
        
        double spent = c_prev.mean_money - c_curr.mean_money;
        double bought = c_curr.mean_deposits.getTotalAmount() - c_prev.mean_deposits.getTotalAmount();
        
        gui.showStatistics("  Total money spent = " + (spent * n) + "€\n");
        gui.showStatistics("  Total amount bought = " + (bought * n) + "u.\n");
        gui.showStatistics("  Mean buy price = " + (spent / bought) + "€/u.\n");
    }
    
    private void showFinalStatistics(){
        this.gui.showStatistics("\nFinal statistics - Ports:\n");
        // TODO
    }
}
