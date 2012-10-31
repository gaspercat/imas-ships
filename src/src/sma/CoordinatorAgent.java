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

import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SimpleAchieveREResponder;

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
    
    //Set the message template to deal with all the messages from boats coordinator
    MessageTemplate mt  = MessageTemplate.and(MessageTemplate.MatchSender(boatsCoordinator),MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
    
    //add a behaviour to deal with the messages
    addBehaviour(new ResponderBehaviour(this,mt));
    
    //Set up a new message to central agent asking for the initial information
    ACLMessage initiatorMsg = new ACLMessage(ACLMessage.REQUEST);
    initiatorMsg.addReceiver(centralAgent);
    initiatorMsg.setSender(this.getAID());
    initiatorMsg.setContent("Initial request");
    
    //Add a behaviour that ask the initial information to the Central agent
    addBehaviour(new InitiatorBehaviour(this,initiatorMsg));
    
    
    
    // Execute the finite state automata
    this.currentState = STATE_INITIAL_REQUEST;
  } //endof setup

  //Implements a responder to deal with the messages from boatsCoordinator
  private class ResponderBehaviour extends SimpleAchieveREResponder{
      Agent myAgent;
      MessageTemplate mt;
      
      public ResponderBehaviour(Agent myAgent, MessageTemplate mt){
          super(myAgent, mt);
          this.myAgent = myAgent;
          this.mt = mt;
      }
      
      //Return an agree message to the boats coordinator informing that the message has been recived
      protected ACLMessage prepareResponse(ACLMessage request){
          ACLMessage reply = request.createReply();
          reply.setPerformative(ACLMessage.AGREE);
          showMessage("Message Recived from boats coordinator, processing...");
          return reply;
      }
      
      //Return an inform message to boats coordintor informing them that the message has been sended to the central agent
      protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
          ACLMessage reply = request.createReply();
          reply.setPerformative(ACLMessage.INFORM);
            try{
                //prepare the message to send to centralagent
                ACLMessage outMsg = new ACLMessage(ACLMessage.REQUEST);
                outMsg.addReceiver(centralAgent);
                outMsg.setSender(myAgent.getAID());
                outMsg.setOntology("BoatsPosition");
                BoatsPosition bp = (BoatsPosition)request.getContentObject();

                outMsg.setContentObject(bp);
                
                //Add a behaviour to initiate a comunication with the centralagent
                myAgent.addBehaviour(new InitiatorBehaviour(myAgent,outMsg));
            }catch(IOException e){
                showMessage(e.toString());
            }catch(UnreadableException e){
                showMessage("HERE "+e.toString());
            }
            reply.setContent("Boats Positions Recieved and sended to the central agent");

          return reply;
      }
      
  }
  
  //Behaviour that iniciate a comunication with a given agent
  private class InitiatorBehaviour extends SimpleAchieveREInitiator{
      Agent myAgent;
      ACLMessage msg;
      
      public InitiatorBehaviour(Agent myAgent, ACLMessage msg){
          super(myAgent, msg);
          this.myAgent = myAgent;
          this.msg = msg;
      }
      
      //Handle agree messages
      public void handleAgree(ACLMessage msg){
          showMessage("AGREE message recived from "+msg.getSender().getLocalName());
      }
      
      //Handle an information message
      public void handleInform(ACLMessage msg){
          //If message comes from centralAgent
          if(msg.getSender().equals(centralAgent)){
              if(msg.getOntology().equalsIgnoreCase("AuxInfo")){
                  computeInitialMessage(msg);
                  showMessage("AuxInfo recived from central Agent");
              }else{
                  showMessage("Message From Central Agent: "+msg.getContent());
              }
              //prepare a message to sent to the boats coordinator
              ACLMessage boatMove = new ACLMessage(ACLMessage.REQUEST);
              boatMove.setSender(myAgent.getAID());
              boatMove.addReceiver(boatsCoordinator);
              boatMove.setContent("Movement request");
              
              //Add a behaviour to initiate a comunication with the boats coordinator
              myAgent.addBehaviour(new InitiatorBehaviour(myAgent,boatMove));
          }else if(msg.getSender().equals(boatsCoordinator)){  
                  showMessage("Message from Boats Coordinator: "+msg.getContent());
          }
      }
      
  }
  
  
  //Computes the initial message with the info of the game sended by the central agent
  private void computeInitialMessage(ACLMessage msg) {
    	showMessage("INFORM COORD AGENT received from "+ ( (AID)msg.getSender()).getLocalName()+" ... [OK]");
        try {
          AuxInfo info = (AuxInfo)msg.getContentObject();
          setGameInfo(info);
          if (info instanceof AuxInfo) {
            //Creates as many boats as auxInfo contain
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
