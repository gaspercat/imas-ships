/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import java.lang.*;
import java.io.*;
import java.sql.SQLException;
import java.sql.ResultSet;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import jade.content.*;
import jade.content.onto.*;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import sma.ontology.*;
import java.util.*;
/**
 *
 * @author joan
 */
public class BoatCoordinator extends Agent{
    private AID coordinatorAgent;
    
    private BoatsPosition boatsPosition;
    
    public BoatCoordinator(){
        super();
        
        this.boatsPosition = new BoatsPosition();
    }
    
    public BoatsPosition getBoatsPosition(){
        return this.boatsPosition;
    }
    
    public void setBoatPosition(BoatPosition boat){
        this.boatsPosition.setBoatPosition(boat);
    }
    
    private void showMessage(String str) {
        System.out.println(getLocalName() + ": " + str);
    }
    
    protected void setup(){
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
        }catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF " + "unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }
        
        //Search for the CentralAgent
        ServiceDescription searchBoatCoordCriteria = new ServiceDescription();
        searchBoatCoordCriteria.setType(UtilsAgents.COORDINATOR_AGENT);
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchBoatCoordCriteria);

        // Register response behaviours
        this.addBehaviour(new RequestResponseBehaviour(this, null));
    }
    
    private jade.util.leap.List buscarAgents(String type,String name) { 
        jade.util.leap.List results = new jade.util.leap.ArrayList();
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        if(type!=null) sd.setType(type);
        if(name!=null) sd.setName(name);
        dfd.addServices(sd);
        try {
            SearchConstraints c = new SearchConstraints(); c.setMaxResults(new Long(-1));
            DFAgentDescription[] DFAgents = DFService.search(this,dfd,c); int i=0;
            while ((DFAgents != null) && (i<DFAgents.length)) {
                DFAgentDescription agent = DFAgents[i];
                i++;
                Iterator services = agent.getAllServices();
                boolean found = false;
                ServiceDescription service = null;
                while (services.hasNext() && !found) {
                    service = (ServiceDescription)services.next();
                    found = (service.getType().equals(type) || service.getName().equals(name));
                }
                if (found) {
                      results.add((AID)agent.getName());
                      //System.out.println(agent.getName()+"\n");
                }
            }
        } catch (FIPAException e) {
            System.out.println("ERROR: "+e.toString());
        }
        return results;
    }
    
  /**************************************************************************/
  /**************************************************************************/
    
  private class RequestResponseBehaviour extends AchieveREResponder {
    BoatCoordinator receiver;

    /**
     * Constructor for the <code>RequestResponseBehaviour</code> class.
     * @param myAgent The agent owning this behaviour
     * @param mt Template to receive future responses in this conversation
     */
    public RequestResponseBehaviour(BoatCoordinator myAgent, MessageTemplate mt) {
      super(myAgent, mt);
      showMessage("Waiting REQUESTs from authorized agents");
      this.receiver = receiver;
    }

    protected ACLMessage prepareResponse(ACLMessage msg) {
      /* method called when the message has been received. If the message to send
       * is an AGREE the behaviour will continue with the method prepareResultNotification. */
      ACLMessage reply = msg.createReply();
      try {
        Object contentRebut = (Object)msg.getContent();
        if(contentRebut.equals("Movement request")) {
          showMessage("Movement request received");
          reply.setPerformative(ACLMessage.AGREE);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      showMessage("Answer sent"); //: \n"+reply.toString());
      return reply;
    } //endof prepareResponse   

    /**
     * This method is called after the response has been sent and only when
     * one of the following two cases arise: the response was an agree message
     * OR no response message was sent.
     * @param msg ACLMessage the received message
     * @param response ACLMessage the previously sent response message
     * @return ACLMessage to be sent as a result notification (i.e. one of
     * inform, failure).
     */
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) {
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.INFORM);
      
      Object content = (Object)msg.getContent();
        
      // Prepare response for a ships movement request
      if(content.equals("Movement request")){
          prepareMovementResultNotitication(reply);
            
      // Prepare response for a start negotiation requests
      }else if(content.equals("Negotiation request")){
          
      }
      
      showMessage("Answer sent"); //+reply.toString());
      return reply;

    } //endof prepareResultNotification

    private void prepareMovementResultNotitication(ACLMessage reply){
        // TODO: COMMUNICATE WITH SHIPS AND ASK TO MOVE
        
        // Notify boat positions to the coordinaor agent
        try {
            reply.setContentObject(this.receiver.getBoatsPosition());
        } catch (Exception e) {
            reply.setPerformative(ACLMessage.FAILURE);
            System.err.println(e.toString());
            e.printStackTrace();
        }
    }
    

    /**
     *  No need for any specific action to reset this behaviour
     */
    public void reset() {
    }

  } //end of RequestResponseBehaviour
}
