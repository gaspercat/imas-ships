package sma;

import java.io.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import java.util.*;
import sma.ontology.*;

import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SimpleAchieveREResponder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p><B>Title:</b> IA2-SMA</p> <p><b>Description:</b> Practical exercise
 * 2011-12. Recycle swarm.</p> <p><b>Copyright:</b> Copyright (c) 2009</p>
 * <p><b>Company:</b> Universitat Rovira i Virgili (<a
 * href="http://www.urv.cat">URV</a>)</p>
 *
 * @author not attributable
 * @version 2.0
 */
public class CoordinatorAgent extends Agent {

    private static final long serialVersionUID = 1L;
    private static final int TURN_FISHING = 1;
    private static final int TURN_NEGOTIATION = 2;
    private static final int TURN_END = 3;
    private AID centralAgent;
    private AID portsCoordinator;
    private AID boatsCoordinator;
    private AuxInfo gameInfo;
    // Data of interest during game
    private BoatsPosition boatsPosition;
    // Game state
    private int currentNegotiation;
    private int currentTurn;

    public BoatsPosition getBoatsPosition() {
        return boatsPosition;
    }

    /**
     * A message is shown in the log area of the GUI
     *
     * @param str String to show
     */
    private void showMessage(String str) {
        System.out.println(getLocalName() + ": " + str);
    }

    public void setGameInfo(AuxInfo gI) {
        this.gameInfo = gI;
    }

    public void setBoatsPosition(BoatsPosition positions) {
        this.boatsPosition = positions;
    }

    public void nextTurn() {
        int turn = -1;

        // Get type of turn
        // ****************************************

        if (this.currentTurn >= 30) {
            turn = TURN_END;
        }

        switch (this.currentTurn % 6) {
            // If negotiation turn
            case 5:
                turn = TURN_NEGOTIATION;

            // If fishing turn
            default:
                turn = TURN_FISHING;
        }

        this.currentTurn++;

        //
        // ****************************************

        if (turn != TURN_END) {
            // Prepare a message to send to the boats coordinator
            ACLMessage boatMove = new ACLMessage(ACLMessage.REQUEST);
            boatMove.setSender(this.getAID());
            boatMove.addReceiver(boatsCoordinator);


           if (turn == TURN_FISHING) {
                try {
                    boatMove.setOntology("New fishing turn");
                    ArrayList<SeaFood> seafoods = new ArrayList<SeaFood>(Arrays.asList(this.gameInfo.getSeaFoods()));
                    boatMove.setContentObject(seafoods);
                } catch (IOException ex) {
                    Logger.getLogger(CoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
           } else if (turn == TURN_NEGOTIATION) {
                boatMove.setContent("New negotiation turn");
             //   boatMove.addReceiver(portsCoordinator);
           }

            // Add a behaviour to initiate a comunication with the boats coordinator
            this.addBehaviour(new InitiatorBehaviour(this, boatMove));
        }
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    protected void setup() {
        // Initialize game states
        this.currentNegotiation = 0;
        this.currentTurn = 0;


        /**
         * ** Very Important Line (VIL) ********
         */
        this.setEnabledO2ACommunication(true, 1);
        /**
         * *************************************
         */
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
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF " + "failed. Reason: " + e.getMessage());
            doDelete();
        }

        // Search for the CentralAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(UtilsAgents.CENTRAL_AGENT);
        this.centralAgent = UtilsAgents.searchAgent(this, searchCriterion);



        // Create ports & boats coordinators
        UtilsAgents.createAgent(this.getContainerController(), "PortCoordinator", "sma.PortCoordinator", null);
        UtilsAgents.createAgent(this.getContainerController(), "BoatCoordinator", "sma.BoatCoordinator", null);

        // Search for the PortCoordinator
        ServiceDescription searchPortCoordCriteria = new ServiceDescription();
        searchPortCoordCriteria.setName("PortCoordinator");
        this.portsCoordinator = UtilsAgents.searchAgent(this, searchPortCoordCriteria);

        // Search for the BoatCoordinator
        ServiceDescription searchBoatCoordCriteria = new ServiceDescription();
        searchBoatCoordCriteria.setName("BoatCoordinator");
        this.boatsCoordinator = UtilsAgents.searchAgent(this, searchBoatCoordCriteria);

        //Set the message template to deal with all the messages from boats coordinator
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

        //add a behaviour to deal with the messages
        addBehaviour(new ResponderBehaviour(this, mt));

        //Set up a new message to central agent asking for the initial information
        ACLMessage initiatorMsg = new ACLMessage(ACLMessage.REQUEST);
        initiatorMsg.addReceiver(centralAgent);
        initiatorMsg.setSender(this.getAID());
        initiatorMsg.setContent("Initial request");

        //Add a behaviour that ask the initial information to the Central agent
        addBehaviour(new InitiatorBehaviour(this, initiatorMsg));
    } //endof setup

    private void sendStatsToCentral(InfoBoxes stats) {
        try {
            ACLMessage sttmsg = new ACLMessage(ACLMessage.REQUEST);
            sttmsg.setContentObject(stats);
            sttmsg.setOntology("Stats");
            sttmsg.addReceiver(centralAgent);
            addBehaviour(new InitiatorBehaviour(this, sttmsg));
        } catch (IOException ex) {
            showMessage("Unable to set stats object to central");
        }
    }

    //Implements a responder to deal with the messages from boatsCoordinator
    private class ResponderBehaviour extends SimpleAchieveREResponder {

        CoordinatorAgent myAgent;
        MessageTemplate mt;

        public ResponderBehaviour(CoordinatorAgent myAgent, MessageTemplate mt) {
            super(myAgent, mt);
            this.myAgent = myAgent;
            this.mt = mt;
        }

        //Return an agree message to the boats coordinator informing that the message has been recived
        protected ACLMessage prepareResponse(ACLMessage request) {
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.AGREE);
            MessageTemplate boat = MessageTemplate.MatchSender(boatsCoordinator);
            MessageTemplate port = MessageTemplate.MatchSender(portsCoordinator);
            if (boat.match(request)) {
                showMessage("Message Recived from boats coordinator, processing...");
            } else if (port.match(request)) {
                showMessage("Message Recived from port coord");
            }
            return reply;
        }

        //Return an inform message to boats coordintor informing them that the message has been sended to the central agent
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            MessageTemplate boatPositionMt = MessageTemplate.MatchOntology("BoatsPosition");
            MessageTemplate statsMt = MessageTemplate.MatchOntology("Stats");

            if (boatPositionMt.match(request)) {
                //TODO POSAR SWITCH ENTER BOATSPOSITIONS I STATS
                try {
                    //prepare the message to send to centralagent
                    ACLMessage outMsg = new ACLMessage(ACLMessage.REQUEST);
                    outMsg.addReceiver(centralAgent);
                    outMsg.setSender(myAgent.getAID());
                    outMsg.setOntology("BoatsPosition");
                    BoatsPosition bp = (BoatsPosition) request.getContentObject();

                    outMsg.setContentObject(bp);

                    //Add a behaviour to initiate a comunication with the centralagent
                    myAgent.addBehaviour(new InitiatorBehaviour(myAgent, outMsg));

                } catch (IOException e) {
                    showMessage(e.toString());
                } catch (UnreadableException e) {
                    showMessage("HERE " + e.toString());
                }
                reply.setContent("Boats Positions Recieved and sent to the central agent");

            } else if (statsMt.match(request)) {
                    try {
                        InfoBoxes stats = (InfoBoxes) request.getContentObject();
                        myAgent.sendStatsToCentral(stats);
                    } catch (UnreadableException ex) {
                        showMessage("Unable to read stats");
                    }
            }
            return reply;
        }
    }

    // Behaviour that iniciate a comunication with a given agent
    private class InitiatorBehaviour extends SimpleAchieveREInitiator {

        CoordinatorAgent myAgent;
        ACLMessage msg;

        public InitiatorBehaviour(CoordinatorAgent myAgent, ACLMessage msg) {
            super(myAgent, msg);
            this.myAgent = myAgent;
            this.msg = msg;
        }

        // Handle agree messages
        public void handleAgree(ACLMessage msg) {
            showMessage("AGREE message recived from " + msg.getSender().getLocalName());
        }

        // Handle an information message
        public void handleInform(ACLMessage msg) {
            // If message comes from centralAgent
            if (msg.getSender().equals(centralAgent)) {
                //TODO mt here
                MessageTemplate sttmt = MessageTemplate.MatchContent("Ports updated");
                MessageTemplate mt1 = MessageTemplate.MatchOntology("Seafoods");
                MessageTemplate mt2 = MessageTemplate.MatchOntology("AuxInfo");
                MessageTemplate mt3 = MessageTemplate.MatchContent("Boats updated");
                if (sttmt.match(msg)) {//Debugging purpouses
                    showMessage("Port updated!");

                // Send boat positions redrawn message
                } else if(mt1.match(msg)) {
                    ACLMessage boatMove = new ACLMessage(ACLMessage.REQUEST);
                    boatMove.setSender(myAgent.getAID());
                    boatMove.addReceiver(boatsCoordinator);
                    // Seafoods moved and map redrawn
                    try {
                        ArrayList<SeaFood> seafoods = null;
                        try {
                            seafoods = (ArrayList<SeaFood>) msg.getContentObject();
                        } catch (UnreadableException ex) {
                            Logger.getLogger(CoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        boatMove.setOntology("Seafoods redrawn");
                        boatMove.setContentObject(seafoods);
                    } catch (IOException ex) {
                        Logger.getLogger(CoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                    }




                    // Add a behaviour to initiate a comunication with the boats coordinator
                    myAgent.addBehaviour(new InitiatorBehaviour(myAgent, boatMove));

                }else if(mt2.match(msg)){
                    computeInitialMessage(msg);
                    showMessage("AuxInfo recived from central Agent");
                    myAgent.nextTurn();

                } else if(mt3.match(msg)){
                    showMessage("INITIATING next turn");
                    nextTurn();
                } else {
                    showMessage("Message From Central Agent: " + msg.getContent());
                }

            } else if (msg.getSender().equals(boatsCoordinator)) {
                showMessage("Message from Boats Coordinator: " + msg.getContent());

            }
        }
    }

    //Computes the initial message with the info of the game sent by the central agent
    private void computeInitialMessage(ACLMessage msg) {
        //TODO seems that the ports aid arguments are improperly set
        showMessage("INFORM COORD AGENT received from " + ((AID) msg.getSender()).getLocalName() + " ... [OK]");
        try {
            AuxInfo info = (AuxInfo) msg.getContentObject();
            setGameInfo(info);
            if (info instanceof AuxInfo) {
                // Prepare list of port AIDs
                ArrayList<AID> port_aids = new ArrayList<AID>();
                for (InfoAgent ia : info.getPorts()) {
                    port_aids.add(ia.getAID());
                }

                // Creates as many ports as auxInfo contains
                for (InfoAgent ia : info.getPorts()) {
                    showMessage("Agent ID: " + ia.getName());
                    if (ia.getAgentType() == AgentType.Port) {
                        Object[] arguments = new Object[1];
                        arguments[0] = ia.getPortType();
                        showMessage("Agent type: " + ia.getAgentType().toString());
                        UtilsAgents.createAgent(this.getContainerController(), ia.getName(), "sma.PortAgent", arguments);
                    } else {
                        showMessage("no agent type");
                    }
                }

                //Creates as many boats as auxInfo contains
                for (InfoAgent ia : info.getAgentsInitialPosition().keySet()) {
                    showMessage("Agent ID: " + ia.getName());
                    if (ia.getAgentType() == AgentType.Boat) {
                        showMessage("Agent type: " + ia.getAgentType().toString());
                        Object[] arguments = new Object[7];
                        // Add position arguments
                        arguments[0] = info.getAgentsInitialPosition().get(ia).getRow();
                        arguments[1] = info.getAgentsInitialPosition().get(ia).getColumn();
                        arguments[2] = info.getMap()[0].length;
                        arguments[3] = info.getMap().length;
                        arguments[4] = info.getCapacityBoats();
                        arguments[5] = info.getSeaFoods();
                        // Add ports AID
                        arguments[6] = port_aids;
                        // Create boat agent
                        UtilsAgents.createAgent(this.getContainerController(), ia.getName(), "sma.BoatAgent", arguments);
                    } else {
                        showMessage("no agent type");
                    }
                }
            }
        } catch (Exception e) {
            showMessage("Incorrect content: " + e.toString());
        }
    }
}
