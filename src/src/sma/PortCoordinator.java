/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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
        
        //Register a responder behavior to deal with the messages from the coordinatorAgent
        //MessageTemplate mt_coord = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        //this.addBehaviour(new BoatCoordinator.ResponderBehaviour(this, mt_coord));
    }
}
