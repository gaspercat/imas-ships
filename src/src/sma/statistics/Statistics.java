/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.statistics;

import java.util.ArrayList;

import sma.gui.GraphicInterface;
import sma.ontology.InfoBoxes;
import sma.ontology.InfoBox;
import sma.ontology.InfoGame;

/**
 *
 * @author Marc
 */
public abstract class Statistics{
    protected InfoGame game;
    protected GraphicInterface gui;
    protected ArrayList<String> names;
    protected ArrayList<ArrayList<InfoBox>> info;
    
    public Statistics(InfoGame game, GraphicInterface gui, ArrayList<String> names){
        this.game = game;
        this.gui = gui;
        this.names = names;
        
        this.info = new ArrayList<ArrayList<InfoBox>>();
        for(int i=0;i<names.size();i++){
            this.info.add(new ArrayList<InfoBox>());
        }
    }
    
    public ArrayList<String> getNames(){
        return this.names;
    }
    
    public void addStatistics(InfoBoxes info){
        jade.util.leap.ArrayList stats = info.getStats();
        
        for (int i = 0; i < stats.size(); i++) {
            InfoBox stat = (InfoBox) stats.get(i);
            String  name = stat.getName();
            
            // Find related agent
            for(int j=0;j<names.size();j++){
                if(this.names.get(j).equals(name)){
                    this.info.get(j).add(stat);
                    break;
                }
            }
        }
        
        analyzeStatistics();
    }
    
    public int getNumIndividuals(){
        return this.info.size();
    }
    
    public String getName(int i){
        if(i<0 || i>=this.names.size()) return null;
        return this.names.get(i);
    }
    
    public int getIndex(String name){
        for(int i=0;i<names.size();i++){
            if(names.get(i).equals(name)){
                return i;
            }
        }
        
        return -1;
    }
    
    public int getNumCycles(){
        if(this.info.size() == 0) return 0;
        return this.info.get(0).size();
    }
    
    
    abstract protected void analyzeStatistics();
}
