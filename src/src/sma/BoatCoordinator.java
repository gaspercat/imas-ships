/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import java.io.*;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import sma.ontology.*;
import java.util.*;
import jade.proto.AchieveREInitiator;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SimpleAchieveREResponder;

/**
 *
 * @author joan
 */
public class BoatCoordinator extends Agent {

    private AID coordinatorAgent;
    private BoatsPosition boatsPosition;

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
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchSender(coordinatorAgent));
        
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
            showMessage("Message Recived from coordinator agent, processing...");
            return reply;
        }
        
        //Return a message INFORM with the information about the actions taken.
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent("Movement Message recieved");
            
            //Add a initiator behaviour that sends a request to boats asking them to move
            myAgent.addBehaviour(new boatsInitiatorBehaviour(myAgent,prepareMessageToBoats()));
            return reply;
        }
        
        //Return a message to send to the boats
        private ACLMessage prepareMessageToBoats(){
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
            showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }
        
        //handle Inform Messages
        public void handleInform(ACLMessage msg){
            showMessage("Informative message from CoordAgent: "+msg.getContent());
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
            showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }
        
        //Handle all the messages from boats in order to send that result to the CoordinatorAgent
        protected void handleAllResultNotifications(java.util.Vector resultNotifications){
            Iterator itr = resultNotifications.iterator();
            
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

}