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
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SimpleAchieveREResponder;
import java.io.IOException;
import sma.ontology.DepositsLevel;
import sma.ontology.PortType;
import sma.strategies.PortStrategy;



/**
 *
 * @author carles
 */
public class PortAgent extends Agent {
    private AID portCoordinator;
    private PortType strategy;
    
    private DepositsLevel deposits;
    private double euros;

    public PortAgent() {
        super();
        
        this.deposits = new DepositsLevel(1000);
        this.euros = 1000;
    }
    
    protected void setup(){
        // Read port type argument
        Object[] arguments = this.getArguments();
        this.strategy = (PortType)arguments[0];
        
        if(this.portCoordinator == null){
            ServiceDescription searchBoatCoordCriteria = new ServiceDescription();
            searchBoatCoordCriteria.setType(UtilsAgents.PORT_COORDINATOR);
            this.portCoordinator = UtilsAgents.searchAgent(this, searchBoatCoordCriteria);
        }
        
        //Accept jade objects as messages
        this.setEnabledO2ACommunication(true, 0);
        
        showMessage("Agent (" + getLocalName() + ") .... [OK]");
        
        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(UtilsAgents.PORT_AGENT);
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
        
        // Add a new behaviour to respond to sale requests
        MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        MessageTemplate mt3 = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
        MessageTemplate mt = MessageTemplate.or(mt1, MessageTemplate.or(mt2, mt3));
        this.addBehaviour(new PortAgent.TradeBehaviour(this,mt));
    }
    
    public PortType getType(){
        return this.strategy;
    }
    
    public double getMoney(){
        return this.euros;
    }
    
    public DepositsLevel getDeposits(){
        return this.deposits;
    }
    
    private void updateHold(DepositsLevel deposits){
        this.deposits.add(deposits);
    }
    
    private void withrawMoney(double amount){
        this.euros -= amount;
    }

    private void warnSoldCoordinator() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(portCoordinator);
        msg.setContent("Upgrade sold counter");
        System.out.println("tioooooo" +portCoordinator);
        this.addBehaviour(new PortAgent.SInitiatorBehaviour(this, msg));
    }


    private class TradeBehaviour extends ContractNetResponder {
        PortStrategy strategy;
        MessageTemplate mt;
        PortAgent myAgent;
        

        public TradeBehaviour(PortAgent a, MessageTemplate mt) {
            super(a, mt);
            this.myAgent = a;
            this.mt = mt;
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
            ACLMessage reply = cfp.createReply();
            
            try {
                DepositsLevel levels = (DepositsLevel)cfp.getContentObject();
                this.strategy = PortStrategy.create(this.myAgent, levels);
                if (!this.strategy.isRejected()) {
                    //showMessage("ACCEPTING proposal from "+cfp.getSender().getLocalName()+" with offer of "+this.strategy.getOffer());
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContentObject(new Double(this.strategy.getOffer()));
                } else {
                    //showMessage("REJECTING proposal from "+cfp.getSender().getLocalName());
                    reply.setPerformative(ACLMessage.REFUSE);
                }



            } catch (UnreadableException ex) {
                reply.setPerformative(ACLMessage.FAILURE);
                showMessage(ex.getMessage());
            } catch (IOException ex) {
                reply.setPerformative(ACLMessage.FAILURE);
                showMessage(ex.getMessage());
            }
            return reply;
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            ACLMessage reply = accept.createReply();
            
            if(!strategy.isAborted()){
                this.myAgent.updateHold(this.strategy.getDeposits());
                this.myAgent.withrawMoney(strategy.getOffer());
                reply.setPerformative(ACLMessage.INFORM);
            }else{
                reply.setPerformative(ACLMessage.FAILURE);
            }
            
            myAgent.warnSoldCoordinator();
            return reply;
        }

        @Override
        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            super.handleRejectProposal(cfp, propose, reject);
            //showMessage("Reject from "+reject.getSender().getLocalName());
        }

        @Override
        protected void handleOutOfSequence(ACLMessage msg) {
            super.handleOutOfSequence(msg);
            System.out.println("PORT " + super.myAgent + " got OUT OF SEQUENCE " + msg);;
        }
    }

        //Given a particular request, handles it;
    private class ResponderBehaviour extends SimpleAchieveREResponder {

        BoatAgent myAgent;
        MessageTemplate mt;

        //Consturctor of the Behaviour
        public ResponderBehaviour(BoatAgent myAgent, MessageTemplate mt) {
            super(myAgent, mt);
            //Change to match CNInitiator
            this.myAgent = myAgent;
            this.mt = mt;
        }

        //Send an AGREE message to the sender
        protected ACLMessage prepareResponse(ACLMessage request) {
            ACLMessage reply = request.createReply();

            String msgContent = request.getContent();
            
            MessageTemplate mt1 = MessageTemplate.MatchContent("Move");
            MessageTemplate mt2 = MessageTemplate.MatchContent("Start negotiation");
            MessageTemplate mt3 = MessageTemplate.MatchContent("Rank fish");
            MessageTemplate mt4 = MessageTemplate.MatchOntology("Ranking");
            MessageTemplate mt5 = MessageTemplate.MatchContent("Set boats destinations");
            MessageTemplate mt = MessageTemplate.or(mt1, MessageTemplate.or(mt2, MessageTemplate.or(mt3, MessageTemplate.or(mt4, mt5))));

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

            MessageTemplate mt1 = MessageTemplate.MatchContent("Move");
            MessageTemplate mt2 = MessageTemplate.MatchContent("Start negotiation");
            MessageTemplate mt3 = MessageTemplate.MatchContent("Rank fish");
            MessageTemplate mt4 = MessageTemplate.MatchOntology("Ranking");
            MessageTemplate mt5 = MessageTemplate.MatchContent("Set boats destinations");

            

            return reply;
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
            showMessage("Informative message from " + msg.getSender().getLocalName() + ": " + msg.getContent());

        }
    }

    
    /**
     * A message is shown in the log area of the GUI
     *
     * @param str String to show
     */
    private void showMessage(String str) {
        System.out.println(getLocalName() + ": " + str);
    }
}
