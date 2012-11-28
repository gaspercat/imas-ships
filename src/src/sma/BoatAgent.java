/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import jade.core.*;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import jade.proto.SimpleAchieveREInitiator;
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
    private AID leader;
    private ArrayList<FishRank> boatsRanking = new ArrayList<FishRank>();
    private ArrayList<AID> boatsGroup = new ArrayList<AID>();
    private SeaFood sfToFish;
    private FishRank bestFishRank;

    
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

    public SeaFood getSfToFish() {
        return sfToFish;
    }

    public void setSfToFish(SeaFood sfToFish) {
        this.sfToFish = sfToFish;
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
            
            MessageTemplate mt1 = MessageTemplate.MatchContent("Move");
            MessageTemplate mt2 = MessageTemplate.MatchContent("Rank fish");
            MessageTemplate mt3 = MessageTemplate.MatchContent("Initiate grouping");
            MessageTemplate mt4 = MessageTemplate.MatchOntology("Ranking");
            MessageTemplate mt5 = MessageTemplate.MatchOntology("FishRank");
            
            if (mt2.match(request)){
                showMessage("Petition to rank fish"+request.getSender().getLocalName());
            }else if (mt1.match(request)){
                showMessage("Petition to move recived from "+request.getSender().getLocalName());
            }else if (mt3.match(request)){
                showMessage("Order to initiate grouping recived");
            }else if (mt4.match(request)){
                showMessage("Ranking recieved");
            }else if (mt5.match(request)){
                showMessage("Fish Rank recieved");
            }
            
            return reply;
        }
        
        //Return the result of the movement in a INFORM Message
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            
            String msgContent = request.getContent();
            
            MessageTemplate mt1 = MessageTemplate.MatchContent("Move");
            MessageTemplate mt2 = MessageTemplate.MatchContent("Rank fish");
            MessageTemplate mt3 = MessageTemplate.MatchContent("Initiate grouping");
            MessageTemplate mt4 = MessageTemplate.MatchOntology("Ranking");
            MessageTemplate mt5 = MessageTemplate.MatchOntology("FishRank");
            MessageTemplate mt6 = MessageTemplate.MatchContent("No longer interested in form part of the group");

            if (mt1.match(request)){
                move();
                try{
                    BoatPosition pb = new BoatPosition(myAgent.getAID(),getPosX(),getPosY());
                    showMessage("New position send to BoatCoordinator");
                    reply.setContentObject(pb);
                }catch (IOException e){
                    showMessage(e.toString());
                }
            }else if(mt2.match(request)){
                try{
                    showMessage("Fish rank send to BoatCoordinator");
                    reply.setContentObject(getFishRank());
                }catch (IOException e){
                    showMessage(e.toString());
                }
            }else if(mt3.match(request)){
                reply.setContent("Grouping...");
                FishRank candidate = boatsRanking.remove(0);
                ACLMessage msgToCandidate = new ACLMessage(ACLMessage.REQUEST);
                msgToCandidate.addReceiver(candidate.getBoat().getAID());
                try {
                    msgToCandidate.setContentObject(candidate);
                } catch (IOException ex) {
                    Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
                msgToCandidate.setOntology("FishRank");
                myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, msgToCandidate));
                
            }else if(mt4.match(request)){
                setIsLeader(true);
                try {
                    setBoatsRanking((ArrayList<FishRank>)request.getContentObject());
                } catch (UnreadableException ex) {
                    showMessage("ERROR: "+ex.toString());
                }
                reply.setContent("Prepared to form groups");
            }else if(mt5.match(request)){
                try {
                    FishRank candidateRank = (FishRank) request.getContentObject();
                    
                    if(leader == null){
                        bestFishRank = candidateRank;
                        leader = request.getSender();
                        reply.setContent("Accept the proposal");
                    }else{
                        if(bestFishRank.compareTo(candidateRank) == -1){
                            reply.setContent("No accept proposal");
                        }else{
                            ACLMessage msgToPrevLeader = new ACLMessage(ACLMessage.REQUEST);
                            msgToPrevLeader.setContent("No longer interested in form part of the group");
                            msgToPrevLeader.addReceiver(leader);
                            myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, msgToPrevLeader));
                            leader = request.getSender();
                            bestFishRank = candidateRank;
                            reply.setContent("Accept the proposal");
                        }
                    }
                } catch (UnreadableException ex) {
                    Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else if(mt6.match(request)){
                if(boatsGroup.size()==3){
                    ACLMessage msgDown = new ACLMessage(ACLMessage.REQUEST);
                    msgDown.addReceiver(boatCoordinator);
                    msgDown.setContent("Downgrade group counter");
                    myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, msgDown));
                }
                boatsGroup.remove(request.getSender());
                
                FishRank candidate = boatsRanking.remove(0);
                ACLMessage msgToCandidate = new ACLMessage(ACLMessage.REQUEST);
                msgToCandidate.addReceiver(candidate.getBoat().getAID());
                try {
                    msgToCandidate.setContentObject(candidate);
                } catch (IOException ex) {
                    Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
                msgToCandidate.setOntology("FishRank");
                myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, msgToCandidate));
                reply.setContent("Removed from the group");
            }
            
            return reply;
        }
    }
    
    //Implements a SimpleInitiator that deals with the cominication with the CoordinatorAgent
    class SInitiatorBehaviour extends SimpleAchieveREInitiator{
        Agent myAgent;
        ACLMessage msg;
        
        public SInitiatorBehaviour(Agent myAgent, ACLMessage msg){
            super(myAgent,msg);
            this.myAgent = myAgent;
            this.msg = msg;
        }
        
        //Handle agree messages
        public void handleAgree(ACLMessage msg){
            showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }
        
        //handle Inform Messages
        public void handleInform(ACLMessage msg){
            showMessage("Informative message from "+msg.getSender()+": "+msg.getContent());
            MessageTemplate mt1 = MessageTemplate.MatchContent("Accept the proposal");
            MessageTemplate mt2 = MessageTemplate.MatchContent("No accept proposal");
            
            String x = "BoatGroup Size:"+(boatsGroup.size()+1);
            showMessage(x);
            
            if(mt1.match(msg)){
                boatsGroup.add(msg.getSender());
                if(boatsGroup.size()<3){
                    sendOffer();
                }else{
                    ACLMessage msgFormed = new ACLMessage(ACLMessage.REQUEST);
                    msgFormed.addReceiver(boatCoordinator);
                    msgFormed.setContent("Group formed");
                    myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, msgFormed));
                }
            }else if(mt2.match(msg)){
                sendOffer();
            }
        }
        
        private void sendOffer(){
            FishRank candidate = boatsRanking.remove(0);
            ACLMessage msgToCandidate = new ACLMessage(ACLMessage.REQUEST);
            msgToCandidate.addReceiver(candidate.getBoat().getAID());
            try {
                msgToCandidate.setContentObject(candidate);
            } catch (IOException ex) {
                Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
            msgToCandidate.setOntology("FishRank");
            myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, msgToCandidate));
        }
        
    }
}