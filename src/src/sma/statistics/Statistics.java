/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.statistics;

import java.util.ArrayList;

import sma.ontology.InfoBoxes;
import sma.ontology.InfoBox;

/**
 *
 * @author gaspercat
 */
public abstract class Statistics {
    ArrayList<String> names;
    ArrayList<ArrayList<InfoBox>> info;
    
    public Statistics(ArrayList<String> names){
        this.names = names;
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
    }
}
