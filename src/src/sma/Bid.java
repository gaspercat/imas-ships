/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

/**
 *
 * @author carles
 */
class Bid {
    private boolean willBuy;
    private double money;

    public Bid(boolean willBuy, double money) {
        this.willBuy = willBuy;
        this.money = money;
    }

    public boolean willBuy() {
        return willBuy;
    }

    public double getMoney() {
        return money;
    }
    
    
    
}
