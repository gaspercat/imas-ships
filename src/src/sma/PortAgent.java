/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public PortAgent(PortType type, AuxInfo auxInfo) {
        this.strategy = type;
        
        this.deposits = new DepositsLevel(1000);
        this.euros = 1000;
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

    private class PortBehaviour extends ContractNetResponder {
        PortStrategy strategy;
        MessageTemplate mt;
        PortAgent myAgent;
        

        public PortBehaviour(PortAgent a, MessageTemplate mt) {
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
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContentObject(new Double(this.strategy.getOffer()));
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                }

                showMessage("Petition to move recived from " + cfp.getSender().getLocalName());


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
