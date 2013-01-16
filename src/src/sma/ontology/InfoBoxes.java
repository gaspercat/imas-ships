/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import jade.util.leap.ArrayList;
import jade.util.leap.Serializable;



/**
 *
 * @author carles
 */
public class InfoBoxes implements Serializable{
    private ArrayList stats;
    private boolean isPort;

    public InfoBoxes(boolean isPort) {
        stats = new ArrayList();
        this.isPort = isPort;
    }

    public ArrayList getStats() {
        return stats;
    }

    public boolean isPort() {
        return isPort;
    }

    public void addStat(InfoBox stat) {
        this.stats.add(stat);
    }

    public void setIsPort(boolean isPort) {
        this.isPort = isPort;
    }

    public int size() {
        return this.stats.size();
    }

    
    public java.util.ArrayList<String> collectNames(){
        java.util.ArrayList<String> ret = new java.util.ArrayList<String>();
        
        for(int i=0;i<stats.size();i++){
            InfoBox box = (InfoBox)stats.get(i);
            ret.add(box.getName());
        }
        
        return ret;
    }
}
