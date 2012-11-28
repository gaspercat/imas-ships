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
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author joan
 */
public class BoatAgent extends Agent{
    private int posX, posY,mapDimX, mapDimY;
    private double capacityBoats;
    private DepositsLevel dl;
    private AID boatCoordinator;
    private SeaFood[] seaFoods;
    private ArrayList<FishRank> seaFoodRanking = new ArrayList<FishRank>();
    private Boolean isLeader = false;
    private ArrayList<FishRank> boatsRanking = new ArrayList<FishRank>();

    
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
        this.capacityBoats = (Double) arguments[4];
        this.seaFoods = (SeaFood[]) arguments[5];
        
        this.dl = new DepositsLevel(this.capacityBoats);
        
        for(int i = 0; i < this.seaFoods.length;i++){
            this.seaFoodRanking.add(new FishRank(this.seaFoods[i],this));
        }
        
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
        
        //Add a new behaviour to respond this particular message
        this.addBehaviour(new ResponderBehaviour(this,preformativeMT));

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

    public Boolean getIsLeader() {
        return isLeader;
    }

    public ArrayList<FishRank> getBoatsRanking() {
        return boatsRanking;
    }
    
    
    
    //Position getter
    public int[] getPosition(){
        int[] position = new int[2];
        position[0] = this.posX;
        position[1] = this.posY;
        return position;
    }
    
    //Boat Deposit getters
    public DepositsLevel getDL(){
        return this.dl;
    }
   
    //Boat capacity getter
    public double getCapacityBoats(){
        return this.capacityBoats;
    }
    
    public ArrayList<FishRank> getFishRank(){
        return this.seaFoodRanking;
    }

    public void setIsLeader(Boolean isLeader) {
        this.isLeader = isLeader;
    }

    public void setBoatsRanking(ArrayList<FishRank> boatsRanking) {
        this.boatsRanking = boatsRanking;
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
            
            String msgContent = request.getContent();
            
            if (msgContent.equalsIgnoreCase("Rank fish")){
                showMessage("Petition to rank fish"+request.getSender().getLocalName());
            }else if (msgContent.equalsIgnoreCase("Move")){
                showMessage("Petition to move recived from "+request.getSender().getLocalName());
            }else if (msgContent.equalsIgnoreCase("Initiate grouping")){
                showMessage("Order to initiate grouping recived");
            }else if (request.getOntology().equalsIgnoreCase("Ranking")){
                showMessage("Ranking recived");
            }
            
            return reply;
        }
        
        //Return the result of the movement in a INFORM Message
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            
            String msgContent = request.getContent();

            if (msgContent.equalsIgnoreCase("Move")){
                move();
                try{
                    BoatPosition pb = new BoatPosition(myAgent.getAID(),getPosX(),getPosY());
                    showMessage("New position send to BoatCoordinator");
                    reply.setContentObject(pb);
                }catch (IOException e){
                    showMessage(e.toString());
                }
            }else if(msgContent.equalsIgnoreCase("Rank fish")){
                try{
                    showMessage("Fish rank send to BoatCoordinator");
                    reply.setContentObject(getFishRank());
                }catch (IOException e){
                    showMessage(e.toString());
                }
            }else if(msgContent.equalsIgnoreCase("Initiate grouping")){
                reply.setContent("Grouping...");
            }else if(request.getOntology().equalsIgnoreCase("Ranking")){
                setIsLeader(true);
                try {
                    setBoatsRanking((ArrayList<FishRank>)request.getContentObject());
                } catch (UnreadableException ex) {
                    showMessage("ERROR: "+ex.toString());
                }
                reply.setContent("Prepared to form groups");
            }
            return reply;
        }
    }      
}