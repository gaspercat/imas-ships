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
public class Stats implements Serializable{
    private ArrayList stats;
    private boolean isPort;

    public Stats(boolean isPort) {
        stats = new ArrayList();
        this.isPort = isPort;
    }

    public ArrayList getStats() {
        return stats;
    }

    public boolean isPort() {
        return isPort;
    }

    public void addStat(Stat stat) {
        this.stats.add(stat);
    }

    public void setIsPort(boolean isPort) {
        this.isPort = isPort;
    }

    public int size() {
        return this.stats.size();
    }

}
