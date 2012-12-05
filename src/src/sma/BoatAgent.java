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
import jade.proto.ContractNetInitiator;
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
    private HashMap pendentOfAcceptance = new HashMap();
    private Boolean messagePendent = false;

    
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
        MessageTemplate mt1 = MessageTemplate.MatchContent("Move");
        MessageTemplate mt2 = MessageTemplate.MatchContent("Rank fish");
        MessageTemplate mt3 = MessageTemplate.MatchOntology("Ranking");
        MessageTemplate preformativeMT = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate mt = MessageTemplate.and(preformativeMT,MessageTemplate.or(mt1, MessageTemplate.or(mt2,mt3)));
        
        //Add a new behaviour to respond this particular message
        this.addBehaviour(new ResponderBehaviour(this,mt));
        this.addBehaviour(new NOLeaderREResponder(this, MessageTemplate.MatchOntology("FishRank")));

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
            MessageTemplate mt4 = MessageTemplate.MatchOntology("Ranking");
            
            if (mt2.match(request)){
                //showMessage("Petition to rank fish from "+request.getSender().getLocalName());
            }else if (mt1.match(request)){
                //showMessage("Petition to move recived from "+request.getSender().getLocalName());
            }else if (mt4.match(request)){
                //showMessage("Ranking recieved from "+request.getSender().getLocalName());
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
            MessageTemplate mt3 = MessageTemplate.MatchOntology("Ranking");

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
                setIsLeader(true);
                try {
                    setBoatsRanking((ArrayList<FishRank>)request.getContentObject());
                } catch (UnreadableException ex) {
                    showMessage("ERROR: "+ex.toString());
                }
                
                MessageTemplate mtL1 = MessageTemplate.MatchContent("Initiate grouping");
                MessageTemplate mtL2 = MessageTemplate.MatchContent("No longer interested in form part of the group");
                MessageTemplate mtL3 = MessageTemplate.MatchContent("Pendent request accepted");
                
                MessageTemplate mt = MessageTemplate.or(mtL1, MessageTemplate.or(mtL3, mtL2));
                
                myAgent.addBehaviour(new LeaderREResponder(myAgent, mt));
                
                reply.setContent("Prepared to form groups");
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
            //showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }
        
        //handle Inform Messages
        public void handleInform(ACLMessage msg){
            showMessage("Informative message from "+msg.getSender().getLocalName()+": "+msg.getContent());
            
        }
    }
    
    private class LeaderREInitiator extends SimpleAchieveREInitiator{
        Agent myAgent;
        ACLMessage msg;
        
        public LeaderREInitiator(Agent myAgent, ACLMessage msg){
            super(myAgent,msg);
            this.myAgent = myAgent;
            this.msg = msg;
            messagePendent = true;
        }
        
        public void handleAgree(ACLMessage msg){
            //showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }
        
        //handle Inform Messages
        public void handleInform(ACLMessage msg){
            showMessage("Informative message from "+msg.getSender().getLocalName()+": "+msg.getContent());
            MessageTemplate mt1 = MessageTemplate.MatchContent("Accept the proposal");
            MessageTemplate mt2 = MessageTemplate.MatchContent("No accept proposal");
            
            if(mt1.match(msg)){
                boatsGroup.add(msg.getSender());
                messagePendent = false;
                if(boatsGroup.size()<3 && boatsRanking.size() > 0){
                    sendOffer();
                }else{
                    ACLMessage msgFormed = new ACLMessage(ACLMessage.REQUEST);
                    msgFormed.addReceiver(boatCoordinator);
                    msgFormed.setContent("Group formed");
                    messagePendent = false;
                    myAgent.addBehaviour(new LeaderREInitiator(myAgent, msgFormed));
                }
            }else if(mt2.match(msg) && boatsRanking.size() > 0){
                messagePendent = false;
                sendOffer();
            }
            showMessage("BoatGroup Size:"+(boatsGroup.size()+1));
        }
    }
    
    private class LeaderREResponder extends SimpleAchieveREResponder{

        Agent myAgent;
        MessageTemplate mt;

        //Consturctor of the Behaviour
        public LeaderREResponder(Agent myAgent, MessageTemplate mt){
            super(myAgent, mt);
            this.myAgent = myAgent;
            this.mt = mt;
        }
        
        protected ACLMessage prepareResponse(ACLMessage request){
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.AGREE);
                        
            MessageTemplate mt1 = MessageTemplate.MatchContent("Initiate grouping");
            MessageTemplate mt2 = MessageTemplate.MatchContent("No longer interested in form part of the group");
            MessageTemplate mt3 = MessageTemplate.MatchContent("Pendent request accepted");

            
            if (mt1.match(request)){
                //showMessage("Order to initiate grouping recived from "+request.getSender().getLocalName());
            }else if (mt2.match(request)){
                //showMessage("No longer interested in form part of our group recived from "+request.getSender().getLocalName());
            }else if (mt3.match(request)){
                //showMessage("Pendent request from "+request.getSender().getLocalName() +" accepted");
            }
            
            return reply;
        }
        
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            
            MessageTemplate mt1 = MessageTemplate.MatchContent("Initiate grouping");
            MessageTemplate mt2 = MessageTemplate.MatchContent("No longer interested in form part of the group");
            MessageTemplate mt3 = MessageTemplate.MatchContent("Pendent request accepted");
            
            if(mt1.match(request)){
                reply.setContent("Grouping...");
                sendOffer();
            }else if(mt2.match(request) && boatsRanking.size() > 0){                
                if(boatsGroup.size()==3){
                    ACLMessage msgDown = new ACLMessage(ACLMessage.REQUEST);
                    msgDown.addReceiver(boatCoordinator);
                    msgDown.setContent("Downgrade group counter");
                    myAgent.addBehaviour(new LeaderREInitiator(myAgent, msgDown));
                    sendOffer();
                }
                boatsGroup.remove(request.getSender());
                
                if(!messagePendent)
                    sendOffer();
                
                reply.setContent("Removed from the group");
            }else if(mt3.match(request)){
                boatsGroup.add(request.getSender());
                messagePendent = false;
                if(boatsGroup.size()<3 && boatsRanking.size() > 0){
                    sendOffer();
                }else{
                    ACLMessage msgFormed = new ACLMessage(ACLMessage.REQUEST);
                    msgFormed.addReceiver(boatCoordinator);
                    msgFormed.setContent("Group formed");
                    myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, msgFormed));
                }
                reply.setContent("Added to group");
            }
            
            return reply;
        }
        
    }
    
    private void sendOffer(){
        FishRank candidate = boatsRanking.remove(0);
        showMessage("Sending offer!!! TO: "+candidate.getBoat().getLocalName());
        ACLMessage msgToCandidate = new ACLMessage(ACLMessage.REQUEST);
        msgToCandidate.addReceiver(candidate.getBoat().getAID());
        try {
            msgToCandidate.setContentObject(candidate);
        } catch (IOException ex) {
            Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        msgToCandidate.setOntology("FishRank");
        this.addBehaviour(new LeaderREInitiator(this, msgToCandidate));
    }
    
    private class NOLeaderREInitiator extends SimpleAchieveREInitiator{
        Agent myAgent;
        ACLMessage msg;
        
        public NOLeaderREInitiator(Agent myAgent, ACLMessage msg){
            super(myAgent,msg);
            this.myAgent = myAgent;
            this.msg = msg;
            messagePendent = true;
        }
        
        public void handleAgree(ACLMessage msg){
            //showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }
        
        //handle Inform Messages
        public void handleInform(ACLMessage msg){
            showMessage("Informative message from "+msg.getSender().getLocalName()+": "+msg.getContent());
            MessageTemplate mt1 = MessageTemplate.MatchContent("Removed from the group");
            
            if(mt1.match(msg)){
                ACLMessage acceptanceMessage = new ACLMessage(ACLMessage.REQUEST);
                ArrayList value = (ArrayList) pendentOfAcceptance.get(leader);
                pendentOfAcceptance.remove(leader);
                acceptanceMessage.addReceiver((AID) value.get(0));
                acceptanceMessage.setContent("Pendent request accepted");
                leader = (AID) value.get(0);
                bestFishRank = (FishRank) value.get(1);
                myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, acceptanceMessage));
            }
        }
    }
    
    private class NOLeaderREResponder extends SimpleAchieveREResponder{

        Agent myAgent;
        MessageTemplate mt;

        //Consturctor of the Behaviour
        public NOLeaderREResponder(Agent myAgent, MessageTemplate mt){
            super(myAgent, mt);
            this.myAgent = myAgent;
            this.mt = mt;
        }
        
        //Send an AGREE message to the sender
        protected ACLMessage prepareResponse(ACLMessage request){
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.AGREE); 
            reply.setOntology("");
            
            return reply;
        }
        
        
        
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
            ACLMessage reply = request.createReply();
            reply.setOntology("");
            reply.setPerformative(ACLMessage.INFORM);
            
            MessageTemplate mt1 = MessageTemplate.MatchOntology("FishRank");

            
            if(mt1.match(request)){
                try {
                    FishRank candidateRank = (FishRank) request.getContentObject();
                    
                    if(leader == null){
                        bestFishRank = candidateRank;
                        leader = request.getSender();
                        reply.setContent("Accept the proposal");
                    }else{
                        if(bestFishRank.compareTo(candidateRank) == 1){
                            reply.setContent("No accept proposal");
                        }else{
                            ACLMessage msgToPrevLeader = new ACLMessage(ACLMessage.REQUEST);
                            msgToPrevLeader.setContent("No longer interested in form part of the group");
                            msgToPrevLeader.addReceiver(leader);
                            myAgent.addBehaviour(new NOLeaderREInitiator(myAgent, msgToPrevLeader));
                            ArrayList value = new ArrayList();
                            value.add(request.getSender());
                            value.add(candidateRank);
                            pendentOfAcceptance.put(leader, value);
                            reply.setContent("Pendent to response");
                        }
                    }
                } catch (UnreadableException ex) {
                    Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            return reply;
        }
        
    }
    
    private class NegotiateSalesInitiator extends ContractNetInitiator{
        BoatAgent myAgent;
        ACLMessage msg;
        
        public NegotiateSalesInitiator(BoatAgent myAgent, ACLMessage msg){
            super(myAgent,msg);
            this.myAgent = myAgent;
            this.msg = msg;
            messagePendent = true;
        }
        
        // If port sends an offer for the fishes
        @Override
        protected void handlePropose(ACLMessage propose, Vector acceptances){
            
        }

        // If port message not understood
        @Override
        protected void handleNotUnderstood(ACLMessage msg){
            
        }    
            
        // If port rejects request
        @Override
        protected void handleRefuse(ACLMessage msg){
            
        }
        
        // If port cancels offer after the boat has accepted it
        @Override
        protected void handleFailure(ACLMessage msg){
            
        }
    }
}