/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import jade.core.*;;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import jade.proto.ContractNetResponder;
import java.io.IOException;
import sma.ontology.AuxInfo;
import sma.ontology.PortType;
import sma.ontology.DepositsLevel;

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
                    showMessage("ACCEPTING proposal from "+cfp.getSender().getLocalName()+" with offer of "+this.strategy.getOffer());
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContentObject(new Double(this.strategy.getOffer()));
                } else {
                    showMessage("REJECTING proposal from "+cfp.getSender().getLocalName());
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
            
            return reply;
        }

        @Override
        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            super.handleRejectProposal(cfp, propose, reject);
            showMessage("Reject from "+reject.getSender().getLocalName());
        }

        @Override
        protected void handleOutOfSequence(ACLMessage msg) {
            super.handleOutOfSequence(msg);
            System.out.println("PORT " + super.myAgent + " got OUT OF SEQUENCE " + msg);;
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
