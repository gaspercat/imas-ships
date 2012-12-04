/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

/**
 *
 * @author carles
 */
class Hold {
    private int[] deposits;
    private int maxCapacity;
    private double money;
    private Strategy strategy;

    public Hold(int maxCapacity, double money, Strategy strategy) {
        this.maxCapacity = maxCapacity;
        this.money = money;
        this.deposits = new int[] {0,0,0,0};           
        this.strategy = strategy;
    }

    void updateHold(int[] boatDeposits) {
        for(int i = 0; i < this.deposits.length; i++){
            this.deposits[i] += boatDeposits[i];
        }
    }
    
}
