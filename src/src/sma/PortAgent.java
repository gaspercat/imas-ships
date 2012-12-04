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

/**
 *
 * @author carles
 */
public class PortAgent extends Agent {

    private AID portCoordinator;
    private int[] deposits;
    private Strategy strategy;
    private double euros;
    private double capacityPorts;

    public PortAgent(PortType type, AuxInfo auxInfo) {
        try {
            this.strategy = new StrategyFactory.createStrategy(type);
        } catch (InvalidPortTypeException ex) {
            System.err.println("PORT FaiL " + this);
            Logger.getLogger(PortAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Bid willBuy(int[] supplies) {
        return this.strategy.willBuy(supplies, this.deposits, this.euros);
    }
    
    private void updateHold(int[] deposits){
        this.strategy.updateHold(deposits);
    }

    private class PortBehaviour extends ContractNetResponder {

        MessageTemplate mt;
        Agent myAgent;

        public PortBehaviour(Agent a, MessageTemplate mt) {
            super(a, mt);
            this.myAgent = a;
            this.mt = mt;
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
            ACLMessage reply = cfp.createReply();
            try {

                int supplies[] = (int[]) cfp.getContentObject();

                Bid bid;
                bid = willBuy(supplies);
                if (bid.willBuy()) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContentObject(bid.getMoney());
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
            try {
                int[] deposits;
                deposits = (int[]) cfp.getContentObject();
                updateHold(deposits);
                reply.setPerformative(ACLMessage.INFORM);
            } catch (UnreadableException ex) {
                showMessage(ex.getMessage());
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
