/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import java.lang.*;
import java.io.*;
import java.sql.SQLException;
import java.sql.ResultSet;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import jade.content.*;
import jade.content.onto.*;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import sma.ontology.*;
import java.util.*;

/**
 *
 * @author joan
 */
public class BoatAgent extends Agent{
    private int posX, posY,mapDimX, mapDimY;
    private AID boatCoordinator;
    
    //First delivery generator of movement
    Random generator = new Random();

    
    public BoatAgent(){
        super();
    }
    
  /**
   * A message is shown in the log area of the GUI
   * @param str String to show
   */
    private void showMessage(String str) {
        System.out.println(getLocalName() + ": " + str);
    }
    
    protected void setup(){
        
        Object[] arguments = this.getArguments();

        this.posX = new Integer(arguments[0].toString());
        this.posY = new Integer(arguments[1].toString());
        this.mapDimX = new Integer(arguments[2].toString());
        this.mapDimY = new Integer(arguments[3].toString());
        
        //Accept jave objects as messages
        this.setEnabledO2ACommunication(true, 0);
        
        showMessage("Agent (" + getLocalName() + ") .... [OK]");
        
        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(UtilsAgents.BOAT_AGENT);
        sd1.setName(getLocalName());
        sd1.setOwnership(UtilsAgents.OWNER);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            showMessage("Registered to the DF");
        }catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF " + "unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }
        
        ServiceDescription searchBoatCoordCriteria = new ServiceDescription();
        searchBoatCoordCriteria.setType(UtilsAgents.BOAT_COORDINATOR);
        this.boatCoordinator = UtilsAgents.searchAgent(this, searchBoatCoordCriteria);
    }
    
    public void setPosX(int posX){
        this.posX = posX;
    }
    
    public void setPosY(int posY){
        this.posY = posY;
    }
    
    public void setPosition(int posX, int posY){
        this.posX = posX;
        this.posY = posY;
    }
    
    public int getPosX(){
        return this.posX;
    }
    
    public int getPosY(){
        return this.posY;
    }
    
    public int[] getPosition(){
        int[] position = new int[2];
        position[0] = this.posX;
        position[1] = this.posY;
        return position;
    }
    
    public int[] move(){
        
        Boolean moved = false;
        
        while (!moved){
            int movementDirection = Math.abs(this.generator.nextInt() % 4);
            
            if (movementDirection == 0 & this.posY != 0){
                this.posY -= 1;
                moved = true;
            }else if(movementDirection == 1 & this.posX != this.mapDimX){
                this.posX += 1;
                moved = true;
            }else if (movementDirection == 2 & this.posY != this.mapDimY){
                this.posY += 1;
                moved = true;
            }else if(movementDirection == 3 & this.posX != 0){
                this.posX -= 1;
                moved = true;
            }
        }
        
        return this.getPosition();
    }
}
