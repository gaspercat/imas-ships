package sma;

import java.io.*;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import jade.proto.SimpleAchieveREResponder;

import sma.ontology.*;
import sma.gui.*;

/**
 * <p><B>Title:</b> IA2-SMA</p>
 * <p><b>Description:</b> Practical exercise 2011-12. Recycle swarm.</p>
 * <p><b>Copyright:</b> Copyright (c) 2011</p>
 * <p><b>Company:</b> Universitat Rovira i Virgili (<a
 * href="http://www.urv.cat">URV</a>)</p>
 * @author not attributable
 * @version 2.0
 */
public class CentralAgent extends Agent {
  private sma.gui.GraphicInterface gui;
  private sma.ontology.InfoGame game;

  private BoatsPosition boats;
  
  SeaFood[] sfList;
  
  private AID coordinatorAgent;
  
  public CentralAgent() {
    super();
  }
  
  public void setBoatsPosition(BoatsPosition positions){
      boats = positions;
  }

  /**
   * A message is shown in the log area of the GUI
 private void showMessage(String str) {
    if (gui!=null) gui.showLog(str + "\n");
    System.out.println(getLocalName() + ": " + str);
  }  * @param str String to show
   */
  private void showMessage(String str) {
    if (gui!=null) gui.showLog(str + "\n");
    System.out.println(getLocalName() + ": " + str);
  }

   /**
   * Agent setup method - called when it first come on-line. Configuration
   * of language to use, ontology and initialization of behaviours.
   */
  protected void setup() {

    /**** Very Important Line (VIL) *********/
    this.setEnabledO2ACommunication(true, 1);
    /****************************************/

    showMessage("Agent (" + getLocalName() + ") .... [OK]");
    try {
    // Register the agent to the DF
    ServiceDescription sd1 = new ServiceDescription();
    sd1.setType(UtilsAgents.CENTRAL_AGENT);
    sd1.setName(getLocalName());
    sd1.setOwnership(UtilsAgents.OWNER);
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.addServices(sd1);
    dfd.setName(getAID());

      DFService.register(this, dfd);
      showMessage("Registered to the DF");
    }
    catch (FIPAException e) {
      System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
      doDelete();
    }


   /**************************************************/

    try {
      this.game = new InfoGame(); //object with the game data
      this.game.readGameFile("game.txt");
      sfList = this.game.getInfo().getSeaFoods();
    } catch(Exception e) {
      e.printStackTrace();
      System.err.println("Game NOT loaded ... [KO]");
    }
    try {
      this.gui = new GraphicInterface(game);
      gui.setVisible(true);
      
      showMessage("Game loaded ... [OK]");
    } catch (Exception e) {
      e.printStackTrace();
    }
     
    /**************************************************/
        
   // search CoordinatorAgent
   ServiceDescription searchCriterion = new ServiceDescription();
   searchCriterion.setType(UtilsAgents.COORDINATOR_AGENT);
   this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
   // searchAgent is a blocking method, so we will obtain always a correct AID

   // add behaviours

   // we wait for the initialization of the game
   MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
   
   this.addBehaviour(new ResponderBehaviour(this,mt));

   // Setup finished. When the last inform is received, the agent itself will add
   // a behavious to send/receive actions

  } //endof setup
  
  private class ResponderBehaviour extends SimpleAchieveREResponder{
      Agent myAgent;
      MessageTemplate mt;
      
      public ResponderBehaviour(Agent myAgent, MessageTemplate mt){
          super(myAgent, mt);
          this.myAgent = myAgent;
      }
      
      //Return an agree message to the boats coordinator informing that the message has been recived
      protected ACLMessage prepareResponse(ACLMessage request){
          ACLMessage reply = request.createReply();
          reply.setPerformative(ACLMessage.AGREE);
          showMessage("Message Recived from coordinator coordinator, processing...");
          return reply;
      }
      
      //Return an inform message with the info of the actions requested by the coordinator agent
      protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            try{
                if(request.getContent().equalsIgnoreCase("Initial request")){
                    showMessage("Initial Request recived");
                    reply.setOntology("AuxInfo");
                    reply.setContentObject(game.getInfo());
                }else if(request.getOntology().equalsIgnoreCase("BoatsPosition")){
                    showMessage("New positions recived");
                    boats = (BoatsPosition)request.getContentObject();
                    refreshMap();
                    myAgent.doWait(1000);
                    reply.setContent("Map reloaded");
                }else{
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                }
            }catch(IOException e){
                showMessage(e.toString());
            }catch(UnreadableException e){
                showMessage(e.toString());
            }
            return reply;
      }
  }
  
  
  //Move the fishes to a determined direction
  protected void moveFishes(){
      for(int i = 0; i < this.sfList.length; i++){
          sfList[i].move();
      }
  }
  
  
  //Refresh the map with the new info
  public void refreshMap(){
      Cell[][] map = game.getInfo().getMap();
      int rows = map.length;
      int cols = map[0].length;
      
      // Overwrite map
      for(int i=0;i<rows;i++){
          for(int j=0;j<rows;j++) map[j][i].clean();
      }
      
      // Place fishes to map
      
      
      // Place boats to map
      BoatPosition[] boats = this.boats.getBoatsPositions();
      for(int i=0;i<boats.length;i++){
          try{
            BoatPosition boat = boats[i];  
            showMessage("GUI "+boat.getColumn()+" "+boat.getRow());
            
            InfoAgent agent = new InfoAgent(AgentType.Boat);
          
            agent.setAgentType(AgentType.Boat);
            agent.setAID(boat.getAID());
            agent.setName(boat.getAID().getLocalName(),boat.getAID().getLocalName().substring(4));       
            
            map[boat.getRow()][boat.getColumn()].setType(CellType.Boat);
            map[boat.getRow()][boat.getColumn()].addAgent(agent);
                    
          }catch(Exception e){
              this.showMessage("Error updating the map");
          }
      }
      
      this.moveFishes();
      
      for(int i = 0; i < this.sfList.length; i++){
          SeaFood sf = this.sfList[i];
          if(sf.onTheMap()){
              map[sf.getPosX()][sf.getPosY()].setType(CellType.Seafood);
              map[sf.getPosX()][sf.getPosY()].setSeaFoodType(sf.getType());
          }
      }
      
      try {
          gui.showGameMap(map);
          gui.showPanelInfo(this.game.getInfo());
      
    } catch (Exception e) {
      e.printStackTrace();
    }      
  }
  
} //endof class AgentCentral
