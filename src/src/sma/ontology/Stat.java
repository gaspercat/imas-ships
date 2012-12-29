/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma.ontology;

/**
 *
 * @author carles
 */
public class Stat {
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
    
}
