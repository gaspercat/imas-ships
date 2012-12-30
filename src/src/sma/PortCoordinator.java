/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SimpleAchieveREResponder;
import jade.util.leap.ArrayList;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import sma.ontology.Stat;
import sma.ontology.Stats;

/**
 *
 * @author carles
 */
public class PortCoordinator extends Agent{
    private AID coordinatorAgent;
    private int nBoats = 20;
    private int boatCounter;
    private ArrayList ports = new ArrayList();
    private Stats stats = new Stats(true);
            
    public PortCoordinator() {
        super();
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
        sd1.setType(UtilsAgents.PORT_COORDINATOR);
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
        
        // Create boat agents

        // Search for the CoordinatorAgent
        ServiceDescription searchBoatCoordCriteria = new ServiceDescription();
        searchBoatCoordCriteria.setType(UtilsAgents.COORDINATOR_AGENT);
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchBoatCoordCriteria);
        
        //Register a responder behavior to deal with the messages from the coordinatorAgent and the ports
        
        MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate mt2 = MessageTemplate.MatchContent("Upgrade sold counter");
        MessageTemplate mt3 = MessageTemplate.MatchContent("New negotiation turn");

        MessageTemplate mt = MessageTemplate.or(mt1, MessageTemplate.or(mt2, mt3));
        
        this.addBehaviour(new PortCoordinator.ResponderBehaviour(this, mt));
    }

    private void incBoatCounter() {
        boolean done = false;
        showMessage("Getting lock");
       // synchronized(this){
            this.boatCounter++;
            if(this.boatCounter == this.nBoats){
                done = true;
            }
       // }
        showMessage("Boat done "+this.boatCounter);
        if(done){
            showMessage("End of negotiation turn");
            getStatPortInfo();
        }
    }

    private void getStatPortInfo() {
        ACLMessage infoRqst = new ACLMessage(ACLMessage.REQUEST);
        infoRqst.setContent("Get stats");
        if(this.ports.isEmpty()){
            searchPorts();
        } 
        for(int i = 0; i < this.ports.size(); i++){
            AID port = (AID) ports.get(i);
            infoRqst.addReceiver(port);
        }
        this.addBehaviour(new PortCoordinator.InitiatorBehaviour(this, infoRqst));
    }

    private void searchPorts() {
        ServiceDescription searchPortCoordCriteria = new ServiceDescription();
        searchPortCoordCriteria.setType(UtilsAgents.PORT_AGENT);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(searchPortCoordCriteria);
        SearchConstraints c = new SearchConstraints();
        c.setMaxResults(new Long(-1));
        try {
            DFAgentDescription[] result = DFService.search(this, dfd, c);
            for(DFAgentDescription dfad : result){
                ports.add(dfad.getName());
            }
            
        } catch (FIPAException ex) {
            //TODO throw custom exception
            Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendStatsCoord() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);       
        try {
            msg.setOntology("Stats");
            msg.addReceiver(coordinatorAgent);
            msg.setContentObject(this.stats);
            addBehaviour(new PortCoordinator.SInitiatorBehaviour(this, msg));
        } catch (IOException ex) {
            showMessage("Failed to build stats msg");
        }
        
        
    }

    
    //Implements a SimpleInitiator that deals with the cominication with the CoordinatorAgent
    class SInitiatorBehaviour extends SimpleAchieveREInitiator {

        Agent myAgent;
        ACLMessage msg;

        public SInitiatorBehaviour(Agent myAgent, ACLMessage msg) {
            super(myAgent, msg);
            this.myAgent = myAgent;
            this.msg = msg;
        }

        //Handle agree messages
        public void handleAgree(ACLMessage msg) {
            //showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }

        //handle Inform Messages
        public void handleInform(ACLMessage msg) {
            //showMessage("Informative message from " + msg.getSender().getLocalName() + ": " + msg.getContent());
        }
    }
        
    
            //Given a particular request, handles it;
    private class ResponderBehaviour extends SimpleAchieveREResponder {

        PortCoordinator myAgent;
        MessageTemplate mt;

        //Consturctor of the Behaviour
        public ResponderBehaviour(PortCoordinator myAgent, MessageTemplate mt) {
            super(myAgent, mt);
            //Change to match CNInitiator
            this.myAgent = myAgent;
            this.mt = mt;
        }

        //Send an AGREE message to the sender
        protected ACLMessage prepareResponse(ACLMessage request) {
            ACLMessage reply = request.createReply();

            String msgContent = request.getContent();
            
            MessageTemplate mt1 = MessageTemplate.MatchContent("Upgrade sold counter");
            //MessageTemplate mt2 = MessageTemplate.MatchContent("Start negotiation");

            //MessageTemplate mt = MessageTemplate.or(mt1, mt2);
            mt = mt1;
            if (mt.match(request)) {
                reply.setPerformative(ACLMessage.AGREE);
            } else {
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
            }

            
            return reply;
        }

        //Return the result of the movement in a INFORM Message
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            String msgContent = request.getContent();

            MessageTemplate mt1 = MessageTemplate.MatchContent("Upgrade sold counter");

            if(mt1.match(request)){
                myAgent.incBoatCounter();
            }
            showMessage("RETURNING from "+request.getSender().getLocalName());
            return reply;
        }
    }


    //Implements a SimpleInitiator that deals with the cominication with the CoordinatorAgent
    class InitiatorBehaviour extends AchieveREInitiator {

        PortCoordinator myAgent;
        ACLMessage msg;

        public InitiatorBehaviour(PortCoordinator myAgent, ACLMessage msg) {
            super(myAgent, msg);
            this.myAgent = myAgent;
            this.msg = msg;
        }

        //Handle agree messages
        @Override
        public void handleAgree(ACLMessage msg) {
            //showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }

        @Override
        protected void handleAllResultNotifications(java.util.Vector resultNotifications){
            MessageTemplate mt = MessageTemplate.MatchOntology("Stat");
            for(int i = 0; i < resultNotifications.size(); i++){
                ACLMessage rsult = (ACLMessage) resultNotifications.get(i);
                if(mt.match(rsult)){
                    try {
                        Stat stat = (Stat)rsult.getContentObject();
                        stats.addStat(stat);
                        
                    } catch (UnreadableException ex) {
                        showMessage("Couldn't read stat: "+ex.getMessage());
                    }

                }    
            }
            showMessage("Got "+myAgent.stats.size()+ " stats");
            myAgent.sendStatsCoord();
        }
    }
}
