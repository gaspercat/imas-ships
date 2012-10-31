package sma;

import java.io.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import sma.ontology.*;
/**
 * <p><B>Title:</b> IA2-SMA</p>
 * <p><b>Description:</b> Practical exercise 2011-12. Recycle swarm.</p>
 * <p><b>Copyright:</b> Copyright (c) 2009</p>
 * <p><b>Company:</b> Universitat Rovira i Virgili (<a
 * href="http://www.urv.cat">URV</a>)</p>
 * @author not attributable
 * @version 2.0
 */
public class CoordinatorAgent extends Agent {
  private static final long serialVersionUID = 1L;
  
  private static final int STATE_NONE = -1;
  private static final int STATE_INITIAL_REQUEST = 0;
  private static final int STATE_MOVE_BOATS = 1;
  private static final int STATE_UPDATE_MAP = 2;
  private static final int STATE_TURNS_INTERVAL = 3;

  private AID centralAgent;
  private AID boatsCoordinator;
  
  private AuxInfo gameInfo;
  
  // Data of interest during game
  private BoatsPosition boatsPosition;
  
  // Game state
  private int currentNegotiation;
  private int currentTurn;
  private int currentState;

  public BoatsPosition getBoatsPosition(){
      return boatsPosition;
  }
  
  /**
   * A message is shown in the log area of the GUI
   * @param str String to show
   */
  private void showMessage(String str) {
    System.out.println(getLocalName() + ": " + str);
  }

  public void setGameInfo(AuxInfo gI){
      this.gameInfo = gI;
  }
  
  public void setBoatsPosition(BoatsPosition positions){
      this.boatsPosition = positions;
  }

  /**
   * Agent setup method - called when it first come on-line. Configuration
   * of language to use, ontology and initialization of behaviours.
   */
  protected void setup() {
    // Initialize game states
    this.currentNegotiation = 0;
    this.currentTurn = 0;
      
      
    /**** Very Important Line (VIL) *********/
    this.setEnabledO2ACommunication(true, 1);
    /****************************************/
    
    // Register the agent to the DF
    ServiceDescription sd1 = new ServiceDescription();
    sd1.setType(UtilsAgents.COORDINATOR_AGENT);
    sd1.setName(getLocalName());
    sd1.setOwnership(UtilsAgents.OWNER);
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.addServices(sd1);
    dfd.setName(getAID());
    try {
      DFService.register(this, dfd);
      showMessage("Registered to the DF");
    }
    catch (FIPAException e) {
      System.err.println(getLocalName() + " registration with DF " + "failed. Reason: " + e.getMessage());
      doDelete();
    }

    // Search for the CentralAgent
    ServiceDescription searchCriterion = new ServiceDescription();
    searchCriterion.setType(UtilsAgents.CENTRAL_AGENT);
    this.centralAgent = UtilsAgents.searchAgent(this, searchCriterion);
    
    // Create boats coordinator
      UtilsAgents.createAgent(this.getContainerController(), "BoatCoordinator", "sma.BoatCoordinator", null);

      //Search for the BoatCoordinator
      ServiceDescription searchBoatCoordCriteria = new ServiceDescription();
      searchBoatCoordCriteria.setName("BoatCoordinator");
      this.boatsCoordinator = UtilsAgents.searchAgent(this, searchBoatCoordCriteria);

    MessageTemplate mt  = MessageTemplate.MatchContent("UpdateBoatPosition");
    addBehaviour(new mainBehaviour(this));
    
    
    
    // Execute the finite state automata
    this.currentState = STATE_INITIAL_REQUEST;
  } //endof setup

  /**************************************************************************/
  /**************************************************************************/

  
  private class mainBehaviour extends Behaviour{
      Agent myAgent;
      
      public mainBehaviour(Agent myAgent){
          this.myAgent = myAgent;
      }
      
      public void action(){
          int state = 0;
          
          ACLMessage incomingMessage;
          ACLMessage outMessage;
          
          switch(state){
              case 0:
                  outMessage = new ACLMessage(ACLMessage.REQUEST);
                  outMessage.addReceiver(centralAgent);
                  outMessage.setSender(myAgent.getAID());
                  outMessage.setContent("Initial request");
                  myAgent.send(outMessage);
                  incomingMessage = myAgent.blockingReceive();
                  computeInitialMessage(incomingMessage);
                  state ++;
              case 1:
                  outMessage = new ACLMessage(ACLMessage.REQUEST);
                  outMessage.addReceiver(boatsCoordinator);
                  outMessage.setSender(myAgent.getAID());
                  myAgent.send(outMessage);
                  state++;
              case 2:
                  incomingMessage = myAgent.blockingReceive();                  
                  ACLMessage out2Message = new ACLMessage(ACLMessage.REQUEST);
                  out2Message.addReceiver(centralAgent);
                  out2Message.setSender(myAgent.getAID());
            try {
                BoatsPosition bp = (BoatsPosition) incomingMessage.getContentObject();
                out2Message.setContentObject(bp);
                myAgent.send(out2Message);
            } catch (UnreadableException ex) {
                Logger.getLogger(CoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException e){
                
            }
                  state = 1;
                  myAgent.doWait(1000);

          }
      }
      
      public boolean done(){
          return false;
      }
  }
  
 
  /**************************************************************************/
  /**************************************************************************/
  
  private void computeInitialMessage(ACLMessage msg) {
    	showMessage("INFORM COORD AGENT received from "+ ( (AID)msg.getSender()).getLocalName()+" ... [OK]");
        try {
          AuxInfo info = (AuxInfo)msg.getContentObject();
          setGameInfo(info);
          if (info instanceof AuxInfo) {
            for (InfoAgent ia : info.getAgentsInitialPosition().keySet()){  
          	showMessage("Agent ID: " + ia.getName());          	  
                if (ia.getAgentType() == AgentType.Boat){
                    showMessage("Agent type: " + ia.getAgentType().toString());
                    Object[] position = new Object[4];
                    position[0] = info.getAgentsInitialPosition().get(ia).getRow();
                    position[1] = info.getAgentsInitialPosition().get(ia).getColumn();
                    position[2] = info.getMap()[0].length;
                    position[3] = info.getMap().length;
                    UtilsAgents.createAgent(this.getContainerController(),ia.getName(), "sma.BoatAgent", position);
                }else showMessage("no agent type");
          	  
                Cell pos = (Cell)info.getAgentsInitialPosition().get(ia);
                showMessage("pos: " + pos);
             }
          }
        } catch (Exception e) {
          showMessage("Incorrect content: "+e.toString());
        }
  }
}
