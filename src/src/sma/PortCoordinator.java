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
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

/**
 *
 * @author carles
 */
public class PortCoordinator extends Agent{
    private AID coordinatorAgent;
    
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

            showMessage("TIOOOOO  "+request.getContent());
            
            return reply;
        }

        //Return the result of the movement in a INFORM Message
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            String msgContent = request.getContent();

            MessageTemplate mt1 = MessageTemplate.MatchContent("Upgrade sold counter");

            

            return reply;
        }
    }
}
