/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import jade.util.leap.ArrayList;



/**
 *
 * @author carles
 */
public class Stats {
    private ArrayList stats;
    private boolean isPort;

    public Stats() {
        stats = new ArrayList();
        isPort = false;
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

}
