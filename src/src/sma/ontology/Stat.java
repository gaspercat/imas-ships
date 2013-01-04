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
public class Stat implements Serializable{
    private DepositsLevel deposit;
    private double euros;
    private String name;
    
    public Stat(DepositsLevel deposit, double euros, String name) {
        this.deposit = deposit;
        this.euros = euros;
        this.name = name;
    }

    public DepositsLevel getDeposit() {
        return deposit;
    }

    public double getEuros() {
        return euros;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Stat{" + "deposit=" + deposit + ", euros=" + euros + ", name=" + name + '}';
    }
    
    
}
