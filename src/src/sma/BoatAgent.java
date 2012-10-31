/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import jade.proto.SimpleAchieveREResponder;
import sma.ontology.*;
import java.util.*;
import java.io.*;
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
        
        
        //Message Template Preformative filter
        MessageTemplate preformativeMT = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        
        //Message Template of Content
        MessageTemplate contentMT = MessageTemplate.MatchContent("Move");
        
        //Adding both message templates as a conditions
        MessageTemplate mt = MessageTemplate.and(preformativeMT, contentMT);
        
        //Add a new behaviour to respond this particular message
        this.addBehaviour(new ResponderBehaviour(this,mt));

    }
    
    //PosX setter
    public void setPosX(int posX){
        this.posX = posX;
    }
    
    //PosY setter
    public void setPosY(int posY){
        this.posY = posY;
    }
    
    //Position setter
    public void setPosition(int posX, int posY){
        this.posX = posX;
        this.posY = posY;
    }
    
    //PosX getter
    public int getPosX(){
        return this.posX;
    }
    
    //PosY getter
    public int getPosY(){
        return this.posY;
    }
    
    //Position getter
    public int[] getPosition(){
        int[] position = new int[2];
        position[0] = this.posX;
        position[1] = this.posY;
        return position;
    }
    
    //Move the boat one position randomly()
    public int[] move(){
        
        Boolean moved = false;
        
        while (!moved){
            int movementDirection = Math.abs(this.generator.nextInt() % 4);
            
            if (movementDirection == 0 & this.posY > 0){
                this.posY -= 1;
                moved = true;
            }else if(movementDirection == 1 & this.posX < this.mapDimX-1){
                this.posX += 1;
                moved = true;
            }else if (movementDirection == 2 & this.posY < this.mapDimY-1){
                this.posY += 1;
                moved = true;
            }else if(movementDirection == 3 & this.posX > 0){
                this.posX -= 1;
                moved = true;
            }
        }
        
        return this.getPosition();
    }
    
    
    //Given a particular request, handles it;
    private class ResponderBehaviour extends SimpleAchieveREResponder{
        
        Agent myAgent;
        MessageTemplate mt;
        
        //Consturctor of the Behaviour
        public ResponderBehaviour(Agent myAgent, MessageTemplate mt){
            super(myAgent, mt);
            this.myAgent = myAgent;
            this.mt = mt;
        }
        
        //Send an AGREE message to the sender
        protected ACLMessage prepareResponse(ACLMessage request){
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.AGREE);
            
            showMessage("Petition to move recived from "+request.getSender().getLocalName());
            
            return reply;
        }
        
        //Return the result of the movement in a INFORM Message
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            move();
            try{
                BoatPosition pb = new BoatPosition(myAgent.getAID(),getPosX(),getPosY());
                showMessage("New position send to BoatCoordinator");
                reply.setContentObject(pb);
            }catch (IOException e){
                showMessage(e.toString());
            }

            return reply;
        }
    }      
}