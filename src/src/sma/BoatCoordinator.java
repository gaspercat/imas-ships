/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import java.io.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import sma.ontology.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joan
 */
public class BoatCoordinator extends Agent {

    private AID coordinatorAgent;
    private BoatsPosition boatsPosition;

    public BoatCoordinator() {
        super();

        this.boatsPosition = new BoatsPosition();
    }

    public BoatsPosition getBoatsPosition() {
        return this.boatsPosition;
    }

    public void setBoatPosition(BoatPosition boat) {
        this.boatsPosition.setBoatPosition(boat);
    }

    public AID getCoordinatorAgent() {
        return coordinatorAgent;
    }

    private void showMessage(String str) {
        System.out.println(getLocalName() + ": " + str);
    }

    protected void setup() {
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
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF " + "unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }

        //Search for the CentralAgent
        ServiceDescription searchBoatCoordCriteria = new ServiceDescription();
        searchBoatCoordCriteria.setType(UtilsAgents.COORDINATOR_AGENT);
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchBoatCoordCriteria);

        // Register response behaviours
        //TODO canviar i posar l'altra content
        this.addBehaviour(new seqBehaviour(this));        
    }

    private jade.util.leap.List buscarAgents(String type, String name) {
        jade.util.leap.List results = new jade.util.leap.ArrayList();
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        if (type != null) {
            sd.setType(type);
        }
        if (name != null) {
            sd.setName(name);
        }
        dfd.addServices(sd);
        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults(new Long(-1));
            DFAgentDescription[] DFAgents = DFService.search(this, dfd, c);
            int i = 0;
            while ((DFAgents != null) && (i < DFAgents.length)) {
                DFAgentDescription agent = DFAgents[i];
                i++;
                Iterator services = agent.getAllServices();
                boolean found = false;
                ServiceDescription service = null;
                while (services.hasNext() && !found) {
                    service = (ServiceDescription) services.next();
                    showMessage("SERVICE " + service.getType() + " , " + service.getName());
                    found = (service.getType().equals(type) || service.getName().equals(name));
                }
                if (found) {
                    results.add((AID) agent.getName());
                    //System.out.println(agent.getName()+"\n");
                }
            }
        } catch (FIPAException e) {
            System.out.println("ERROR: " + e.toString());
        }
        return results;
    }

    
    class seqBehaviour extends Behaviour {

        private Agent myAgent;
        int step = 1;
        
        public seqBehaviour(Agent myAgent) {
            super(myAgent);
            this.myAgent = myAgent;
        }

        public void action(){
            
            BoatsPosition boatsPos = new BoatsPosition();
            jade.util.leap.List recievers = buscarAgents("boat", null);

            switch(step){
                case 1:
                    MessageTemplate mt = MessageTemplate.MatchContent("Movement request");
                    ACLMessage reqMsg = myAgent.blockingReceive();
                    step += 1;
                case 2:
                    showMessage("Prepare movement result");
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.setContent("Move");
                    showMessage("SIZE " + recievers.size());
                    for (int i = 0; i < recievers.size(); i++) {
                        AID rec = (AID) recievers.get(i);
                        msg.addReceiver(rec);
                        showMessage("ADDING RECIVER " + rec.getName());
                    }
                    myAgent.send(msg);
                    step += 1;
                case 3:
                    int receivedMsg = 0;
                    boatsPos = new BoatsPosition();
                    while(receivedMsg < recievers.size()){
                        ACLMessage incomingMsg = myAgent.blockingReceive();
                        try {
                            BoatPosition bp = (BoatPosition) incomingMsg.getContentObject();
                            boatsPos.addPosition(bp);
                        } catch (UnreadableException ex) {
                            Logger.getLogger(BoatCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        receivedMsg ++;                        
                    }
                    step++;
                case 4:
                    ACLMessage outMsg = new ACLMessage(ACLMessage.INFORM);
                    outMsg.addReceiver(coordinatorAgent);
                    try {
                        outMsg.setContentObject(boatsPos);
                    } catch (IOException ex) {
                        Logger.getLogger(BoatCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    myAgent.send(outMsg);
                    step = 1;
            }
        }
        
        public boolean done(){
            return step == 5;
        }
        
        public int onEnd() {
            reset();
            myAgent.addBehaviour(this);
            return super.onEnd();
        }
    }
    
}