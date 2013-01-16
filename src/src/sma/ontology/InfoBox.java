/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

import jade.util.leap.Serializable;

/**
 *
 * @author carles
 */
public class InfoBox implements Serializable{
    private DepositsLevel deposit;
    private double euros;
    private int num_movements;
    private String name;
    
    public InfoBox(DepositsLevel deposit, double euros, String name) {
        this.deposit = deposit;
        this.euros = euros;
        this.num_movements = 0;
        this.name = name;
    }
    
    public InfoBox(DepositsLevel deposit, double euros, int nmoves, String name) {
        this.deposit = deposit;
        this.euros = euros;
        this.num_movements = nmoves;
        this.name = name;
    }

    public DepositsLevel getDeposit() {
        return deposit;
    }

    public double getEuros() {
        return euros;
    }
    
    public double getNumMovements(){
        return num_movements;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Stat{" + "deposit=" + deposit + ", euros=" + euros + ", name=" + name + '}';
    }
    
}