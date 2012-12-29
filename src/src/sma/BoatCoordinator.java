/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import java.io.*;
import jade.core.*;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import sma.ontology.*;
import java.util.*;
import jade.proto.AchieveREInitiator;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SimpleAchieveREResponder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joan
 */
public class BoatCoordinator extends Agent {

    private AID coordinatorAgent;
    private BoatsPosition boatsPosition;
    private ArrayList<AID> leaders;
    
    private int actualGroups, positionedGroups, numGroups;

    public BoatCoordinator() {
        super();

        this.boatsPosition = new BoatsPosition();
    }

    public BoatsPosition getBoatsPosition() {
        return this.boatsPosition;
    }

    public void setBoatPosition(BoatPosition boat) {
        this.boatsPosition.setBoatPosition(boat);
    }

    public AID getCoordinatorAgent() {
        return coordinatorAgent;
    }

    private void showMessage(String str) {
        System.out.println(getLocalName() + ": " + str);
    }

    @Override
    protected void setup() {
        //Accept jave objects as messages
        this.setEnabledO2ACommunication(true, 0);

        showMessage("Agent (" + getLocalName() + ") .... [OK]");

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(UtilsAgents.BOAT_COORDINATOR);
        sd1.setName(getLocalName());
        sd1.setOwnership(UtilsAgents.OWNER);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            showMessage("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF " + "unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }

        //Search for the CentralAgent
        ServiceDescription searchBoatCoordCriteria = new ServiceDescription();
        searchBoatCoordCriteria.setType(UtilsAgents.COORDINATOR_AGENT);
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchBoatCoordCriteria);

        // Register response behaviours
        //Template form REQUEST Messages from the coordinatorAgent
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

        
        //Register a responder behavior to deal with the messages from the coordinatorAgent
        this.addBehaviour(new ResponderBehaviour(this,mt));        
    }
    
    //Behaviour to deal with the requestes from the coordinator agent
    class ResponderBehaviour extends SimpleAchieveREResponder{
        Agent myAgent;
        MessageTemplate mt;
        
        public ResponderBehaviour(Agent myAgent, MessageTemplate mt){
            super(myAgent,mt);
            this.myAgent = myAgent;
            this.mt = mt;
        }
        
        //Return an AGREE message confirming the reception of the message.
        protected ACLMessage prepareResponse(ACLMessage request){
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.AGREE);
            //showMessage("Message Recived from "+request.getSender().getLocalName()+", processing...");
            return reply;
        }
        
        //Return a message INFORM with the information about the actions taken.
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            
            MessageTemplate mt1 = MessageTemplate.MatchContent("New fishing turn");
            MessageTemplate mt2 = MessageTemplate.MatchContent("Group formed");
            MessageTemplate mt3 = MessageTemplate.MatchContent("Downgrade group counter");
            MessageTemplate mt4 = MessageTemplate.MatchContent("New negotiation turn");
            MessageTemplate mt5 = MessageTemplate.MatchContent("Boats destination assigned");
            
            // New fishing turn
            if(mt1.match(request)){
                reply.setContent("New fishing turn message received");
                System.out.println("Number of groups Formed: "+actualGroups);
                
                // Ask boats to rank the utility of the fishes
                myAgent.addBehaviour(new boatsInitiatorBehaviour(myAgent, prepareRankFishMessageToBoats()));
                leaders = new ArrayList<AID>();
                
            // New negotiation turn
            }else if(mt4.match(request)){
                reply.setContent("New negotiation turn message received");
                System.out.println("New negotiation turn started!");
                
                // Ask boats to start negotiations
                myAgent.addBehaviour(new boatsInitiatorBehaviour(myAgent, prepareStartNegotiationMessageToBoats()));
                
            // Group formed
            }else if(mt2.match(request)){
                reply.setContent("Wait for the other groups");
                actualGroups++;
                System.out.println("Number of groups Formed: "+actualGroups);
                
                if(actualGroups == numGroups){
                    showMessage("AAAAAAAAAAAAAAAAAALL Groups Formed");
                    addBehaviour(new boatsInitiatorBehaviour(myAgent, this.prepareDestinationsMessageToBoats()));
                }
                
            // Downgrade group counter
            }else if(mt3.match(request)){
                actualGroups--;
                reply.setContent("Number of groups downgraded");
                System.out.println("Number of groups Formed: "+actualGroups);
            
            // Boat destinations assigned
            }else if(mt5.match(request)){
                reply.setContent("Wait for the other groups");
                System.out.println("Number of groups with destinations set: " + positionedGroups + "/" + numGroups);
                
                positionedGroups++;
                if(positionedGroups == numGroups){
                    showMessage("All boat destinations assigned, starting ship movements");
                    // TODO: Send information to coordinator agent -> central agent
                    // addBehaviour(new boatsInitiatorBehaviour(myAgent, this.prepareMoveMessageToBoats()));
                }
                
            }
            
            return reply;
        }
        
        private ACLMessage prepareRankFishMessageToBoats(){
            jade.util.leap.List boats = buscarAgents("boat",null);
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            Iterator itr = boats.iterator();
            while(itr.hasNext()){
                AID boat = (AID)itr.next();
                msg.addReceiver(boat);
            }
            msg.setContent("Rank Fish");
            return msg;
        }
        
        private ACLMessage prepareStartNegotiationMessageToBoats(){
            jade.util.leap.List boats = buscarAgents("boat", null);
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            Iterator itr = boats.iterator();
            while(itr.hasNext()){
                AID boat = (AID)itr.next();
                msg.addReceiver(boat);
            }
            msg.setContent("Start negotiation");
            return msg;
        }
        
        // Message asking the boat leaders to set their boats destinations
        private ACLMessage prepareDestinationsMessageToBoats(){
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

            for(AID boat: leaders){
                msg.addReceiver(boat);
            }
            
            msg.setContent("Set boats destinations");
            return msg;
        }
        
        // Message asking the boats to start their movement
        private ACLMessage prepareMoveMessageToBoats(){
            jade.util.leap.List boats = buscarAgents("boat",null);
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);            
            Iterator itr = boats.iterator();
            while(itr.hasNext()){
                AID boat = (AID)itr.next();
                msg.addReceiver(boat);
            }
            msg.setContent("Move");
            return msg;
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
            
            MessageTemplate mt1 = MessageTemplate.MatchContent("Prepared to form groups");
            
            if(mt1.match(msg)){
                ACLMessage iniGMsg = new ACLMessage(ACLMessage.REQUEST);
                iniGMsg.addReceiver(msg.getSender());
                iniGMsg.setContent("Initiate grouping");
                myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, iniGMsg));
            }
        }
    }

    //Initiates the petiotion to move all boats
    class boatsInitiatorBehaviour extends AchieveREInitiator{
        Agent myAgent;
        ACLMessage msg;
        
        public boatsInitiatorBehaviour(Agent myAgent, ACLMessage msg){
            super(myAgent, msg);
            this.myAgent = myAgent;
            this.msg = msg;
        }
        
        //Handle agree messages
        public void handleAgree(ACLMessage msg){
           // showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }
        
        //Handle all the messages from boats in order to send that result to the CoordinatorAgent
        protected void handleAllResultNotifications(java.util.Vector resultNotifications){
            Iterator itr = resultNotifications.iterator();
            
            MessageTemplate mt1 = MessageTemplate.MatchContent("Move");
            MessageTemplate mt2 = MessageTemplate.MatchContent("Rank Fish");
            MessageTemplate mt3 = MessageTemplate.MatchContent("Initiate grouping");
                
            if (mt1.match(msg)){
                //Boats positions that we have to send to the Coordinator agent
                BoatsPosition boatsPos = new BoatsPosition();

                //iterate over all the responses
                while(itr.hasNext()){
                   ACLMessage msg = (ACLMessage)itr.next(); 
                   if(msg.getPerformative() == ACLMessage.INFORM){
                       try{
                           BoatPosition bp = (BoatPosition) msg.getContentObject();
                           boatsPos.addPosition(bp);
                       }catch(UnreadableException e){
                           showMessage(e.toString());
                       }
                   }
                }
                
                try{
                    //Prepare the message to the Coordinator agents
                    ACLMessage outMessage = new ACLMessage(ACLMessage.REQUEST);
                    outMessage.addReceiver(coordinatorAgent);
                    outMessage.setSender(myAgent.getAID());
                    outMessage.setContentObject(boatsPos);

                    //Add a behaviour that initiate a conversation with the coordinator agents
                    myAgent.addBehaviour(new SInitiatorBehaviour(myAgent,outMessage));
                }catch(IOException e){
                    showMessage(e.toString());
                }
            }else if(mt2.match(msg)){
                ArrayList<ArrayList<FishRank>> fishRanks = new ArrayList();
                
                while(itr.hasNext()){
                   ACLMessage msg = (ACLMessage)itr.next(); 
                   if(msg.getPerformative() == ACLMessage.INFORM){
                       try{
                           ArrayList<FishRank> fr = (ArrayList<FishRank>) msg.getContentObject();
                           fishRanks.add(fr);
                       }catch(UnreadableException e){
                           showMessage(e.toString());
                       }
                   }
                }             
                setUPleaders(fishRanks);           
            }else if(mt3.match(msg)){
                
            }
        }
    }
    
    //Find all the agents with by name or/and type
    private jade.util.leap.List buscarAgents(String type, String name) {
        jade.util.leap.List results = new jade.util.leap.ArrayList();
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        if (type != null) {
            sd.setType(type);
        }
        if (name != null) {
            sd.setName(name);
        }
        dfd.addServices(sd);
        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults(new Long(-1));
            DFAgentDescription[] DFAgents = DFService.search(this, dfd, c);
            int i = 0;
            while ((DFAgents != null) && (i < DFAgents.length)) {
                DFAgentDescription agent = DFAgents[i];
                i++;
                Iterator services = agent.getAllServices();
                boolean found = false;
                ServiceDescription service = null;
                while (services.hasNext() && !found) {
                    service = (ServiceDescription) services.next();
                    showMessage("SERVICE " + service.getType() + " , " + service.getName());
                    found = (service.getType().equals(type) || service.getName().equals(name));
                }
                if (found) {
                    results.add((AID) agent.getName());
                    //System.out.println(agent.getName()+"\n");
                }
            }
        } catch (FIPAException e) {
            System.out.println("ERROR: " + e.toString());
        }
        return results;
    }
    
    
    /**
     * Setup the leaders for each fishing group. One for each fish school present on the map. Also generates the descending ranking of boats for each seafood in order to pass it to the leader.
     * Once the process is complete sends this information to each leader and start the grouping phase.
     * @param frList An array of arrays containing all the evaluations of each seafood for each boat.
     */
    private void setUPleaders(ArrayList<ArrayList<FishRank>> frList){
        ArrayList<Rankings> rankings= this.getSeaFoodsBoatsRanking(frList);
        int l = 0;
        
        while(l < rankings.size()){                    
            //Initial Best
            int bestRankIndex = -1;
            for(int i = 0; i < rankings.size();i++){
                if(rankings.get(i).getLeader() == null){
                    bestRankIndex = i;
                    break;
                }
            }
            
            //Get the best rank given the initial best rank index
            FishRank bestRank = rankings.get(bestRankIndex).seaFoodsBoatsBlockersRanking.get(0);
            
            //Iterate over the rankings checking for the best rank.
            for(int i = 0; i < rankings.size(); i++){              
                FishRank candidateBestRank = rankings.get(i).seaFoodsBoatsBlockersRanking.get(0);
                
                if (bestRank.compareTo(candidateBestRank) == 1 && rankings.get(i).getLeader() == null){
                    bestRank = candidateBestRank;
                    bestRankIndex = i;
                }
            }
            
            if (rankings.get(bestRankIndex).getLeader() == null) {
                Boolean uniqueInOtherRank = false;

                for(int i = 0; i < rankings.size();i++){
                    if(rankings.get(i).seaFoodsBoatsBlockersRanking.size()>1){
                        rankings.get(i).removeBoat(bestRank.getBoat(), rankings.get(i).seaFoodsBoatsBlockersRanking);
                    }else if(rankings.get(i).seaFoodsBoatsBlockersRanking.size() == 1){
                        if(rankings.get(i).indexBoatInRanking(bestRank.getBoat(), rankings.get(i).seaFoodsBoatsBlockersRanking) == 0 && i != bestRankIndex){
                            uniqueInOtherRank = true;
                        }
                    }
                }
                
                if(!uniqueInOtherRank){
                        l++;
                        rankings.get(bestRankIndex).setLeader(bestRank);
                }
            }
        }
        
        this.numGroups = rankings.size();
        this.actualGroups = 0;
        this.positionedGroups = 0;
        
        for (int i = 0; i < rankings.size();i++){
            leaders.add(rankings.get(i).leader.getBoat().getAID());
            for (int j = 0; j < rankings.size();j++){
                rankings.get(j).removeBoat(rankings.get(i).leader.getBoat(), rankings.get(j).seaFoodsBoatsRanking);
            }
        }
        
        for (int i = 0; i < rankings.size();i++){
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(rankings.get(i).leader.getBoat().getAID());       
            msg.setOntology("Ranking");
            System.out.println(rankings.get(i).toString());
            try {
                msg.setContentObject(rankings.get(i).seaFoodsBoatsRanking);
            } catch (IOException ex) {
                this.showMessage("ERROR: "+ex.toString());
            }

            this.addBehaviour(new SInitiatorBehaviour(this,msg));
        }
        
    }
    
    /**
     * Return an ArrayList of rankings objects given all the utility values of the SeaFoods for each boat
     * @param frList An array of arrays containing all the evaluations of each seafood for each boat.
     * @return ArrayList of rankings objects
     */
    private ArrayList<Rankings> getSeaFoodsBoatsRanking(ArrayList<ArrayList<FishRank>> frList){
        //Number of seafood
        int numSeaFoods = frList.get(0).size();
        
        //Array containing the numSeaFoodsRankings
        ArrayList<Rankings> seaFoodsBoatsRanking = new ArrayList<Rankings>();
        
        //Fill the rankings
        for(int i = 0; i < numSeaFoods; i++){
            Iterator itFrList = frList.iterator();
            Rankings sfBoatsRank = new Rankings();
            seaFoodsBoatsRanking.add(sfBoatsRank);
            while(itFrList.hasNext()){
                FishRank fr = ((ArrayList<FishRank>) itFrList.next()).get(i);
                seaFoodsBoatsRanking.get(i).seaFoodsBoatsRanking.add(fr);
                //If the seafood is blockable for a determined boad.
                if (fr.getBlockable()) {
                    seaFoodsBoatsRanking.get(i).seaFoodsBoatsBlockersRanking.add(fr);
                }
            }
            //Sort both rankings
            seaFoodsBoatsRanking.get(i).sortRanks();
        }
        
        return seaFoodsBoatsRanking;
    }

    /**
     * Class that implements a ranking of boats for each seafood.
     */
    private class Rankings{
        //A ranking of boats for each seafood
        ArrayList<FishRank> seaFoodsBoatsRanking = new ArrayList<FishRank>();
        
        //A ranking of boats capable of catch a seafood group.
        ArrayList<FishRank> seaFoodsBoatsBlockersRanking = new ArrayList<FishRank>();
        
        
        int othersMembers = 0;
        
        //Leader of the seafood
        FishRank leader;
        
        /**
         * Sort the two ranks in descendent order.
         */
        public void sortRanks(){
            Collections.sort(this.seaFoodsBoatsRanking,Collections.reverseOrder());
            Collections.sort(this.seaFoodsBoatsBlockersRanking,Collections.reverseOrder());
        }

        /**
         * Leader getter.
         * @return The leader of the seafood group
         */
        public FishRank getLeader() {
            return leader;
        }
        
        /**
         * Leader setter.
         * @param leader The leader of the seafood group
         */
        public void setLeader(FishRank leader) {
            this.leader = leader;
        }
        
        /**
         * Remove a boat from the blockers ranking.
         * @param boat The boat to be deleted.
         */
        public void removeBoat(BoatAgent boat, ArrayList<FishRank> ranking){
            int indxBoat = this.indexBoatInRanking(boat, ranking);
            if (indxBoat >= 0){
                ranking.remove(indxBoat);
            }
        }
        
        /**
         * Find the index of the boat in the blockers ranking.
         * @param boatToFind The boat to find
         * @return The index of the boat in the blockersRanking. If no exist returns -1.
         */
        private int indexBoatInRanking(BoatAgent boatToFind, ArrayList<FishRank> ranking){
            for(int i = 0; i < ranking.size();i++){
                FishRank fr= ranking.get(i);
                String boat = fr.getBoat().getLocalName();
                if(boat.equalsIgnoreCase(boatToFind.getLocalName())){
                    return i;
                }
            }
            return -1;
        }

        /**
         * Returns a String showing the information related to the ranking
         * @return A string containing the representation of the ranking.
         */
        @Override
        public String toString() {
            StringBuffer msg = new StringBuffer();
            msg.append("Seafood: "+this.leader.getSf().getMovementDirection()+"("+this.leader.getSf().getPosX()+", "+this.leader.getSf().getPosY()+")"+"\n");
            msg.append("Leader:\t"+this.leader.getBoat().getLocalName()+"("+this.leader.getBoat().getPosX()+", "+this.leader.getBoat().getPosY()+")");
            msg.append("\tExpected:\t"+this.leader.getExpectedValue());
            msg.append("\tDistance:\t"+this.leader.getDistance()+"\n");
            for (int i = 0; i < this.seaFoodsBoatsRanking.size();i++){
                BoatAgent boat = this.seaFoodsBoatsRanking.get(i).getBoat();
                msg.append("Boat:\t"+boat.getLocalName()+"("+boat.getPosX()+", "+boat.getPosY()+")");
                msg.append("\tExpected:\t"+this.seaFoodsBoatsRanking.get(i).getExpectedValue());
                msg.append("\tDistance:\t"+this.seaFoodsBoatsRanking.get(i).getDistance());
                msg.append("\tBlockable:\t");
                if (this.seaFoodsBoatsRanking.get(i).getBlockable()){
                    msg.append("Blockable\n");
                }else{
                    msg.append("No blockable\n");
                }
            }
            return msg.toString();
        }
        
        
    }
}