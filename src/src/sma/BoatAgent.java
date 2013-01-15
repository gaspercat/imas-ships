/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sma;

import jade.core.*;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import jade.proto.AchieveREInitiator;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SimpleAchieveREResponder;
import jade.proto.ContractNetInitiator;
import sma.ontology.*;
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

/**
 *
 * @author joan
 */
public class BoatAgent extends Agent {
    //Position and dimensions of the map

    private int posX, posY, mapDimX, mapDimY;
    //Capacity of the boats
    private double capacityBoats;
    //SeaFoods Groups
    private SeaFood[] seaFoods;
    //Ranking of seafoods
    private ArrayList<FishRank> seaFoodRanking = new ArrayList<FishRank>();
    //Is the boat a leader
    private Boolean isLeader = false;
    //Seafood this boat is going to catch in this fishing turn.
    private SeaFood targetSeafood;
    // Assigned fishing spot in the current fishing turn. It have been established by the team leader.
    private BoatPosition fishingSpot;
    //Ranking of the boats, only used by the leaders
    private ArrayList<FishRank> boatsRanking = new ArrayList<FishRank>();
    //Boats Group, only used by the leaders
    private ArrayList<AID> boatsGroup = new ArrayList<AID>();
    //Best fishRank so far.
    private FishRank bestFishRank;
    //List of messages pendents to response
    private CandidateLeader pendentOfAcceptance = null;
    //Are there any message pendent?
    private Boolean messagePendent = false;
    // Agent AID's
    private AID leader;
    private AID boatCoordinator;
    private ArrayList<AID> ports;
    // Deposits & money
    private DepositsLevel deposits;
    private double money;
    //First delivery generator of movement
    Random generator = new Random();
    
    public BoatAgent() {
        super();
    }

    public DepositsLevel getDeposits() {
        return deposits;
    }

    public double getMoney() {
        return money;
    }

    /**
     * A message is shown in the log area of the GUI
     *
     * @param str String to show
     */
    private void showMessage(String str) {
        System.out.println(getLocalName() + ": " + str);
    }

    protected void setup() {
        Object[] arguments = this.getArguments();

        // Read position arguments
        this.posX = new Integer(arguments[0].toString());
        this.posY = new Integer(arguments[1].toString());
        this.mapDimX = new Integer(arguments[2].toString());
        this.mapDimY = new Integer(arguments[3].toString());
        this.capacityBoats = (Double) arguments[4];

        // Read ports list
        this.ports = (ArrayList<AID>) arguments[5];

        this.deposits = new DepositsLevel(this.capacityBoats);
        this.money = 0;

        //Accept jade objects as messages
        this.setEnabledO2ACommunication(true, 0);

        showMessage("Agent (" + getLocalName() + ") .... [OK]");

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(UtilsAgents.BOAT_AGENT);
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

        ServiceDescription searchBoatCoordCriteria = new ServiceDescription();
        searchBoatCoordCriteria.setType(UtilsAgents.BOAT_COORDINATOR);
        this.boatCoordinator = UtilsAgents.searchAgent(this, searchBoatCoordCriteria);

        //Message Template Preformative filter
        MessageTemplate mt1 = MessageTemplate.MatchOntology("Move");
        MessageTemplate mt2 = MessageTemplate.MatchOntology("ArrayList<SeaFood>");
        MessageTemplate mt3 = MessageTemplate.MatchOntology("Ranking");
        MessageTemplate mt4 = MessageTemplate.MatchContent("Start negotiation");
        MessageTemplate mt5 = MessageTemplate.MatchContent("Organize group");
        MessageTemplate mt6 = MessageTemplate.MatchContent("Give me current position");
        MessageTemplate mt7 = MessageTemplate.MatchOntology("Fishing spots");
        MessageTemplate mt8 = MessageTemplate.MatchContent("Query group target seafood to leader");
        MessageTemplate mt9 = MessageTemplate.MatchContent("Fish");
     
        MessageTemplate preformativeMT = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate mt = MessageTemplate.and(preformativeMT,
                    MessageTemplate.or(mt1, 
                    MessageTemplate.or(mt2,
                    MessageTemplate.or(mt3, 
                    MessageTemplate.or(mt4, 
                    MessageTemplate.or(mt5,
                    MessageTemplate.or(mt6, 
                    MessageTemplate.or(mt7,
                    MessageTemplate.or(mt8,mt9)))))))));
        
        // Add a new behaviour to respond this particular message
        this.addBehaviour(new ResponderBehaviour(this, mt));

        // Add a new behaviour to respond to FishRank message
        this.addBehaviour(new NOLeaderREResponder(this, MessageTemplate.MatchOntology("FishRank")));

        // Add a new behaviour

    }

    //PosX setter
    public void setPosX(int posX) {
        this.posX = posX;
    }

    //PosY setter
    public void setPosY(int posY) {
        this.posY = posY;
    }

    //Position setter
    public void setPosition(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    //PosX getter
    public int getPosX() {
        return this.posX;
    }

    //PosY getter
    public int getPosY() {
        return this.posY;
    }

    public Boolean getIsLeader() {
        return isLeader;
    }

    public ArrayList<FishRank> getBoatsRanking() {
        return boatsRanking;
    }

    //Position getter
    public int[] getPosition() {
        int[] position = new int[2];
        position[0] = this.posX;
        position[1] = this.posY;
        return position;
    }

    //Boat Deposit getters
    public DepositsLevel getDL() {
        return this.deposits;
    }

    //Boat capacity getter
    public double getCapacityBoats() {
        return this.capacityBoats;
    }

    public ArrayList<FishRank> getFishRank() {
        return this.seaFoodRanking;
    }

    public void setIsLeader(Boolean isLeader) {
        this.isLeader = isLeader;
    }

    public void setBoatsRanking(ArrayList<FishRank> boatsRanking) {
        this.boatsRanking = boatsRanking;
    }

    /**
     * Set the leader's target seafood. Information extracted from the FishRank leaded by this boat.
     * @param fishRanks An array of FishRank.
     */
    private void setTargetSeafood(ArrayList<FishRank> fishRanks)
    {
        this.targetSeafood = fishRanks.get(0).getSf();
    }  
    
    // Set the destination of the group boats
    public BoatsPosition setBoatsDestinations(BoatsPosition boatsPosition)
    {
        //
        // Seek for the catcher (the boat who will stop the fish).
        // Should be the leader. But just in case...
        //
        
        int minDistceptance = 20+20;
        BoatPosition closestBoatPosition = new BoatPosition(getAID(), getPosX(), getPosY()); // Leader!!
//        for (BoatPosition bp : boatsPosition.getBoatsPositions())
//        {
//            if (targetSeafood.isBlockable(bp))
//            {
//                int distToSeafood = Math.abs(bp.getRow() - targetSeafood.getPosX()) + Math.abs(bp.getColumn() - targetSeafood.getPosY());
//                if (distToSeafood < minDistceptance) 
//                {
//                    minDistceptance = distToSeafood;
//                    closestBoatPosition = bp;
//                }
//            }
//        }

        //
        // Calculate the position of the seafood in the catching instant.
        //
        
        int catchPosX, catchPosY;
        
        BoatPosition catcherDestination = closestBoatPosition.clone();
        int d = targetSeafood.getMovementDirection(); // Hope null-shit won't happen...
        // The rest three relative positions to the seafood position (fishing spots).
        int[][] relatives;
        if(d % 2 == 0) // Vertical displacement
        {
            catchPosX = catcherDestination.getRow();
            catchPosY = targetSeafood.getPosY();
            if (catcherDestination.getColumn() < targetSeafood.getPosY())
            {
                catcherDestination.setColumn(targetSeafood.getPosY() - 1);
                relatives = new int[][] {
                    new int[] {0, 1},
                    new int[] {1, 0},
                    new int[] {-1, 0}
                };
            }
            else
            {
                catcherDestination.setColumn(targetSeafood.getPosY() + 1);
                relatives = new int[][] {
                    new int[] {0, -1},
                    new int[] {1, 0},
                    new int[] {-1, 0}
                };
            }
        }
        else
        {
            catchPosX = targetSeafood.getPosX();
            catchPosY = catcherDestination.getColumn();
            if (catcherDestination.getRow() < targetSeafood.getPosX())
            {
                catcherDestination.setRow(targetSeafood.getPosX() - 1);
                relatives = new int[][] {
                    new int[] {0, -1},
                    new int[] {0, 1},
                    new int[] {1, 0}
                };
            }
            else
            {
                catcherDestination.setRow(targetSeafood.getPosX() + 1);
                relatives = new int[][] {
                    new int[] {0, -1},
                    new int[] {0, 1},
                    new int[] {-1, 0}
                };
            }
        }
        
        //
        // Calculate the best formation minimizing: minimizing the sum of the number boat moves.
        //
       
        ArrayList<BoatPosition> bpArrayList = boatsPosition.getBoatsPositionsArrayList();
        bpArrayList.remove(closestBoatPosition);
        
        int numOfPermutations = 6;
        int[][] idx = {
           new int [] {0, 1, 2},
           new int [] {0, 2, 1},
           new int [] {1, 0, 2},
           new int [] {1, 2, 0},
           new int [] {2, 0, 1},
           new int [] {2, 1, 0}
        };
        
        int bestPerm = 0;
        int minCost = 3 * (20+20);
        for (int p = 0; p < numOfPermutations; p++)
        {
            int[] perm = idx[p];
            int cost = 0;
            for (int k = 0; k < 3; k++)
            {
                int bx = bpArrayList.get(k).getRow();
                int by = bpArrayList.get(k).getColumn();
                int rpx = catchPosX + relatives[perm[k]][0];
                int rpy = catchPosY + relatives[perm[k]][1];
                cost += Math.abs(bx - rpx) + Math.abs(by - rpy);
            }
            if (cost < minCost)
            {
                minCost = cost;
                bestPerm = p;
            }
        }
        
        //
        // Create the BoatsPosition object of the destinations (the fishing spot around their corresponding seafood).
        //
        
        BoatsPosition destinations = new BoatsPosition();
        
        // Catcher boat
        destinations.addPosition(catcherDestination);
        // Auxiliary boats
        int[] perm = idx[bestPerm];
        for (int i = 0; i < 3; i++)
        {
            int x = catchPosX + relatives[perm[i]][0];
            int y = catchPosY + relatives[perm[i]][1];
            destinations.addPosition( new BoatPosition(bpArrayList.get(i).getAID(), x, y) );
        }
        
        for (BoatPosition z : destinations.getBoatsPositions())
        {
            if (z == null)
            {
                System.out.println("MOLT MALO MALO");
            }
        }

        return destinations;
    }
    
    private boolean isNextToSeafood(SeaFood sf)
    {
        int x = getPosX();
        int y = getPosY();
        int sfx = sf.getPosX();
        int sfy = sf.getPosY();
        
        int dist = Math.abs(x - sfx) + Math.abs(y - sfy);
        
        return dist <= 1;
    }
    
    private boolean reachedFishingSpot()
    {
        int x = getPosX();
        int y = getPosY();
        if(null == fishingSpot){
            System.out.println("MALO SUPREMO");
        }
        int fsx = fishingSpot.getRow();
        int fsy = fishingSpot.getColumn();
        
        return x == fsx && y == fsy;
    }
    
    private void moveTowardsFishingSpot()
    {
        int x = getPosX();
        int y = getPosY();
        int fsx = fishingSpot.getRow();
        int fsy = fishingSpot.getColumn();

        Random rand = new Random();
        
        // Move in either x or y dimension. Randomly.
        if (x != fsx && y != fsy)
        {
            int coin = rand.nextInt(2);
            if (coin == 0)
            {
                if (x < fsx) x++;
                else x--;
            }
            else
            {
                if (y < fsy) y++;
                else y--;
            }
        }
        // y position reached, move along x direction.
        else if (x != fsx)
        {
            if (x < fsx) x++;
            else x--;
        }
        // x position reached, move along y direction.
        else if (y != fsy)
        {
            if (y < fsy) y++;
            else y--;
        }
            
        setPosX(x);
        setPosY(y);
    }

//    // Move the boat one position randomly()
//    public int[] move() {
//        // TODO: Move group of boats according to the bestRankFish!!!!!
//        Boolean moved = false;
//
//        while (!moved) {
//            int movementDirection = Math.abs(this.generator.nextInt() % 4);
//
//            if (movementDirection == 0 & this.posY > 0) {
//                this.posY -= 1;
//                moved = true;
//            } else if (movementDirection == 1 & this.posX < this.mapDimX - 1) {
//                this.posX += 1;
//                moved = true;
//            } else if (movementDirection == 2 & this.posY < this.mapDimY - 1) {
//                this.posY += 1;
//                moved = true;
//            } else if (movementDirection == 3 & this.posX > 0) {
//                this.posX -= 1;
//                moved = true;
//            }
//        }
//
//        return this.getPosition();
//    }

    public void negotiateDeposits() {
        // Create message to be sent
        ACLMessage msgFormed = new ACLMessage(ACLMessage.CFP);
        msgFormed.setSender(this.getAID());
        
        
        Random r = new Random();
        this.deposits.setLobsterLevel(r.nextInt((int)deposits.getCapacity()));
        this.deposits.setOctopusLevel(r.nextInt((int)deposits.getCapacity()));
        this.deposits.setShrimpLevel(r.nextInt((int)deposits.getCapacity()));
        this.deposits.setTunaLevel(r.nextInt((int)deposits.getCapacity()));
        //showMessage("Faking desposits to debug "+deposits);
        try {
            msgFormed.setContentObject(this.deposits);
        } catch (IOException ex) {
            showMessage("Error crafting initial port negotiation message!!");
        }
        
        if(this.ports.get(0) == null){
            this.ports = searchPorts();
        }
        
        // Set destination ports
        for (AID receiver : this.ports) {
            msgFormed.addReceiver(receiver);
            AID sender = msgFormed.getSender();
           // showMessage("Sending request to "+receiver.getLocalName() + " From "+sender.getLocalName());

        }
        
        this.addBehaviour(new NegotiateSalesCNInitiator(this, msgFormed));
    }

    public void sellDeposits(double price) {
        this.money += price;
        this.deposits.empty();
    }

    private ArrayList<AID> searchPorts() {
        ArrayList<AID> ports = new ArrayList<AID>();
        ServiceDescription searchPortCoordCriteria = new ServiceDescription();
        searchPortCoordCriteria.setType(UtilsAgents.PORT_AGENT);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(searchPortCoordCriteria);
        SearchConstraints c = new SearchConstraints();
        c.setMaxResults(new Long(-1));
        try {
            DFAgentDescription[] result = DFService.search(this, dfd, c);
            for(DFAgentDescription dfad : result){
                ports.add(dfad.getName());
            }
            
        } catch (FIPAException ex) {
            //TODO throw custom exception
            Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            return ports;
        }
    }

    private void fish() {
        deposits.store(targetSeafood);
        
    }
    
    private void resetAgent(){
        this.seaFoodRanking = new ArrayList<FishRank>();
        this.isLeader = false;
        this.boatsRanking = new ArrayList<FishRank>();
        this.boatsGroup = new ArrayList<AID>();
        this.bestFishRank = null;
        this.messagePendent = false;
        this.leader = null;

        this.posX = new Random().nextInt(this.mapDimX+1);
        this.posY = new Random().nextInt(this.mapDimY+1);
    }

    //Given a particular request, handles it;
    private class ResponderBehaviour extends SimpleAchieveREResponder {

        BoatAgent myAgent;
        MessageTemplate mt;

        //Consturctor of the Behaviour
        public ResponderBehaviour(BoatAgent myAgent, MessageTemplate mt) {
            super(myAgent, mt);
            //Change to match CNInitiator
            this.myAgent = myAgent;
            this.mt = mt;
        }

        //Send an AGREE message to the sender
        protected ACLMessage prepareResponse(ACLMessage request) {
            ACLMessage reply = request.createReply();

            String msgContent = request.getContent();
            
            MessageTemplate mt1 = MessageTemplate.MatchOntology("Move");
            MessageTemplate mt2 = MessageTemplate.MatchContent("Start negotiation");
            MessageTemplate mt3 = MessageTemplate.MatchOntology("ArrayList<SeaFood>");
            MessageTemplate mt4 = MessageTemplate.MatchOntology("Ranking");
            MessageTemplate mt5 = MessageTemplate.MatchContent("Organize group");
            MessageTemplate mt6 = MessageTemplate.MatchContent("Give me current position");
            MessageTemplate mt7 = MessageTemplate.MatchOntology("Fishing spots");
            MessageTemplate mt8 = MessageTemplate.MatchContent("Query group target seafood to leader");
            MessageTemplate mt9 = MessageTemplate.MatchContent("Fish");
            MessageTemplate mt = MessageTemplate.or(mt1, 
                    MessageTemplate.or(mt2,
                    MessageTemplate.or(mt3, 
                    MessageTemplate.or(mt4, 
                    MessageTemplate.or(mt5,
                    MessageTemplate.or(mt6, 
                    MessageTemplate.or(mt7,
                    MessageTemplate.or(mt8, mt9))))))));
            
            if (mt.match(request)) {
                reply.setPerformative(ACLMessage.AGREE);
            } else {
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
            }

            return reply;
        }

        //Return the result of the movement in a INFORM Message
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            String msgContent = request.getContent();

            MessageTemplate mt1 = MessageTemplate.MatchOntology("Move");
            MessageTemplate mt2 = MessageTemplate.MatchContent("Start negotiation");
            MessageTemplate mt3 = MessageTemplate.MatchOntology("ArrayList<SeaFood>");
            MessageTemplate mt4 = MessageTemplate.MatchOntology("Ranking");
            MessageTemplate mt5 = MessageTemplate.MatchContent("Organize group");
            MessageTemplate mt6 = MessageTemplate.MatchContent("Give me current position");
            MessageTemplate mt7 = MessageTemplate.MatchOntology("Fishing spots");
            MessageTemplate mt8 = MessageTemplate.MatchContent("Query group target seafood to leader");
            MessageTemplate mt9 = MessageTemplate.MatchContent("Fish");

            // BoatCoordinator sent to the boat a request to move (to the fishing spot assigned by the team leader)
            if (mt1.match(request))
            {          
                try {
                    ArrayList<SeaFood> updatedSeafoods = (ArrayList<SeaFood>) request.getContentObject();
                    updateTargetSeafoodPosition(updatedSeafoods);
                } catch (UnreadableException ex) {
                    Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
                // Do the boat reached the assigned fishing spot up to now?
                if (!reachedFishingSpot()){   
                    int prevPosX = getPosX(); // Log and debug purposes
                    int prevPosY = getPosY(); // Log and debug purposes

                    moveTowardsFishingSpot(); // Move a cell towards the fishing spot!

                    // Prepare the reply content
                    reply.setOntology("Intermediate position");
                    
                    // Log message
                    String outMessage =
                        " moved from (" + prevPosX + ", " + prevPosY + ") "
                        + " to (" + getPosX() + ", " + getPosY() + ") "
                        + " sailing to reach its fishing spot (" + fishingSpot.getRow() + ", " + fishingSpot.getColumn() + ") ";
                    showMessage(outMessage);
                    // Debug message
                    //System.out.println(outMessage);
                }
                else
                {
                    // Prepare the reply content
                    reply.setOntology("Fishing position");
                    
                    showMessage("i'm ready to fish.");
                }

                // Prepare the rest of the reply content: object
                BoatPosition bp; 
                if (isNextToSeafood(targetSeafood) && isLeader)
                    bp = new BoatPosition(myAgent.getAID(),getPosX(),getPosY(),reachedFishingSpot(),targetSeafood);
                else
                    bp = new BoatPosition(myAgent.getAID(),getPosX(),getPosY(),reachedFishingSpot(),null);
                
                try {  
                    reply.setContentObject(bp);
                } catch (IOException ex) {
                    Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
            // Negotiation start request
            } else if (mt2.match(request)) {
                showMessage("Starting negotiation");
                negotiateDeposits();
            // Rank fishes request
            } else if (mt3.match(request)) {
                try {
                    showMessage("Fish rank send to BoatCoordinator");
                    
                    ArrayList<SeaFood> seaFoods;
                    seaFoods = (ArrayList<SeaFood>) request.getContentObject();
                  
                    
                    for (SeaFood sf : seaFoods) {
                        seaFoodRanking.add(new FishRank(sf, myAgent));
                    }
                    
                    reply.setContentObject(getFishRank());
                } catch (IOException e) {
                    showMessage(e.toString());
                }
                  catch (UnreadableException ex) {
                  Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }

            // Promotion to leader request
            } else if (mt4.match(request)) {
                setIsLeader(true);
                try {
                    ArrayList<FishRank> fishRankArrayList = (ArrayList<FishRank>)request.getContentObject();
                    setTargetSeafood(fishRankArrayList);
                    setBoatsRanking(fishRankArrayList);
                } catch (UnreadableException ex) {
                    showMessage("ERROR: " + ex.toString());
                }

                MessageTemplate mtL1 = MessageTemplate.MatchContent("Initiate grouping");
                MessageTemplate mtL2 = MessageTemplate.MatchContent("No longer interested in form part of the group");
                MessageTemplate mtL3 = MessageTemplate.MatchContent("Pendent request accepted");
                MessageTemplate mtL4 = MessageTemplate.MatchContent("Pendent message rejected");

                MessageTemplate mt = MessageTemplate.or(mtL1, MessageTemplate.or(mtL3, MessageTemplate.or(mtL2, mtL4)));

                myAgent.addBehaviour(new LeaderREResponder(myAgent, mt));

                reply.setContent("Prepared to form groups");
            // Organize group
            }else if(mt5.match(request)){
                //setBoatsDestinations();
                reply.setContent("Organizing groups");
                
                System.out.println(myAgent.getLocalName() + "(" + getPosX() + "," + getPosY() + "): " + targetSeafood.toString());
                myAgent.addBehaviour(new leadSubditsInitiatorBehaviour(myAgent, this.askForCurrentPositionMessageToSubdits()));
            }
            else if (mt6.match(request))
            {
                leader = request.getSender(); // Important! Needed later.
                
                reply.setOntology("Subdit position");
                try {
                    reply.setContentObject(new BoatPosition(getAID(), getPosX(), getPosY()));
                    
                    //System.out.println(myAgent.getLocalName() + ": " + bestFishRank.getSf().toString());
                } catch (IOException ex) {
                    Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.addReceiver(leader);
                req.setContent("Query group target seafood to leader");
                myAgent.addBehaviour(new SubditToLeaderInitiatorBehaviour(myAgent, req));
            }
            else if (mt7.match(request)) // Non leader receiving
            {
                try {
                    BoatsPosition boatsPositions = (BoatsPosition) request.getContentObject();
                    fishingSpot = boatsPositions.get(getAID());
                    reply.setContent("Fishing spot set");
                } catch (UnreadableException ex) {
                    Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if (mt8.match(request)) // Non leader receiving
            {
                reply.setOntology("Target seafood");
                try {
                    reply.setContentObject(targetSeafood);
                } catch (IOException ex) {
                    Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(mt9.match(request))
            {
                myAgent.fish();
                myAgent.resetAgent();
                showMessage("Sending stats");
                reply.setOntology("Stat");
                InfoBox stat = new InfoBox(myAgent.getDeposits(), myAgent.getMoney(), myAgent.getLocalName());
                try {
                    reply.setContentObject(stat);
                } catch (IOException ex) {
                    reply.setPerformative(ACLMessage.FAILURE);
                }
            }

            return reply;
        }
        
        // Message the boat leaders to set their boats destinations
        private ACLMessage askForCurrentPositionMessageToSubdits(){
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

            msg.setSender(getAID());
            for(AID boat : boatsGroup){
                msg.addReceiver(boat);
            }
            
            msg.setContent("Give me current position");
            return msg;
        }

        
        private void updateTargetSeafoodPosition(ArrayList<SeaFood> updatedSeafoods) {
            
            if (updatedSeafoods != null) // If null is the first turn movement, not needed.
            {
                for (SeaFood sf : updatedSeafoods)
                {
                    if (sf.equals(targetSeafood)) // Check id equality between the maintained copy of the target seafood and the updated version from upper agents.
                        targetSeafood = sf;
                }
            }
        }
    }

        //Aimed to organize the group, the leader communicates with its subdits.
    //First, the leader asks for current position of each subdit to then 
    //calculate which the corresponding fishing spot around the seafood,
    //minimizing the sailing distance.
    class leadSubditsInitiatorBehaviour extends AchieveREInitiator{
        Agent myAgent;
        ACLMessage msg;
        
        BoatsPosition destinations;
        
        public leadSubditsInitiatorBehaviour(Agent myAgent, ACLMessage msg){
            super(myAgent, msg);
            this.myAgent = myAgent;
            this.msg = msg;
        }
        
        
        public void handleAgree(ACLMessage msg){
            System.out.println("AGREE message recived from "+msg.getSender().getLocalName());
        }
        
        
        @Override
        protected void handleAllResultNotifications(java.util.Vector resultNotifications){
            Iterator itr = resultNotifications.iterator();
            
            BoatsPosition boatsPositions = new BoatsPosition();
            
            MessageTemplate mt1 = MessageTemplate.MatchContent("Give me current position");
            MessageTemplate mt2 = MessageTemplate.MatchOntology("Fishing spots");
            
            // Give me current position
            if (mt1.match(msg))
            {
                MessageTemplate mt = MessageTemplate.MatchOntology("Subdit position");

                boolean subditsPositionReceived = false;
                boatsPositions.addPosition(new BoatPosition(getAID(), getPosX(), getPosY(),false));
                while (itr.hasNext())
                {
                    ACLMessage msg = (ACLMessage) itr.next();

                    if (mt.match(msg))
                    {
                        subditsPositionReceived = true;
                        try {
                            BoatPosition subditPosition = (BoatPosition) msg.getContentObject();
                            boatsPositions.addPosition(subditPosition);
                            System.out.println("Should be the current position");
                        } catch (UnreadableException ex) {
                            Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

                if (subditsPositionReceived)
                {
                    this.destinations = setBoatsDestinations(boatsPositions);
                    fishingSpot = this.destinations.get(getAID());
                    if(fishingSpot == null){
                        System.out.println("REMALO MALO");
                    }
                    myAgent.addBehaviour(new BoatAgent.leadSubditsInitiatorBehaviour(myAgent, this.fishingSpotMessageToSubdits()));
                }
            }
            // Fishing spots (ontology)
            else if (mt2.match(msg))
            {
                MessageTemplate mt = MessageTemplate.MatchContent("Fishing spot set");   
                
//                int counter = 0;
//                while (itr.hasNext())
//                {
//                    ACLMessage msg = (ACLMessage) itr.next();
//
//                    if (mt.match(msg))
//                    {
//                        counter++;
//                    }
//                    if (counter == 2) // Always fulfilled, DEBUG purposes.
//                    {
                        System.out.println(myAgent.getLocalName() + ": the leader have told the destination to its subdits!");
                           
                        ACLMessage msgFormed = new ACLMessage(ACLMessage.REQUEST);
                        msgFormed.addReceiver(boatCoordinator);
                        msgFormed.setContent("Group organized");
                        myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, msgFormed));
//                    } 
//                }
            }
        }
        
        private ACLMessage fishingSpotMessageToSubdits()
        {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

            
            for(AID boat : boatsGroup){
                msg.addReceiver(boat);
            }
            
            msg.setOntology("Fishing spots");
            try {
                msg.setContentObject(this.destinations);
            } catch (IOException ex) {
                Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
            return msg;
        }
    }
    
    //Implements a SimpleInitiator that deals the communication Subdit -> Leader.
    class SubditToLeaderInitiatorBehaviour extends SimpleAchieveREInitiator{
        Agent myAgent;
        ACLMessage msg;
        
        public SubditToLeaderInitiatorBehaviour(Agent myAgent, ACLMessage msg){
            super(myAgent,msg);
            this.myAgent = myAgent;
            this.msg = msg;
        }
        
        //Handle agree messages
        public void handleAgree(ACLMessage msg){
            //showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }
        
        //handle Inform Messages
        public void handleInform(ACLMessage msg){
            //showMessage("Informative message from "+msg.getSender().getLocalName()+": "+msg.getContent());
            MessageTemplate mt = MessageTemplate.MatchOntology("Target seafood");
            if (mt.match(msg))
            {
                try {
                    targetSeafood = (SeaFood) msg.getContentObject();
                } catch (UnreadableException ex) {
                    Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }    
    
    //Implements a SimpleInitiator that deals with the cominication with the CoordinatorAgent
    class SInitiatorBehaviour extends SimpleAchieveREInitiator {

        Agent myAgent;
        ACLMessage msg;

        public SInitiatorBehaviour(Agent myAgent, ACLMessage msg) {
            super(myAgent, msg);
            this.myAgent = myAgent;
            this.msg = msg;
        }

        //Handle agree messages
        public void handleAgree(ACLMessage msg) {
            //showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }

        //handle Inform Messages
        public void handleInform(ACLMessage msg) {
            showMessage("Informative message from " + msg.getSender().getLocalName() + ": " + msg.getContent());

        }
    }

    private class LeaderREInitiator extends SimpleAchieveREInitiator {

        Agent myAgent;
        ACLMessage msg;

        public LeaderREInitiator(Agent myAgent, ACLMessage msg) {
            super(myAgent, msg);
            this.myAgent = myAgent;
            this.msg = msg;
            messagePendent = true;
        }

        public void handleAgree(ACLMessage msg) {
            //showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }

        //handle Inform Messages
        public void handleInform(ACLMessage msg) {
            showMessage("Informative message from " + msg.getSender().getLocalName() + ": " + msg.getContent());
            MessageTemplate mt1 = MessageTemplate.MatchContent("Accept the proposal");
            MessageTemplate mt2 = MessageTemplate.MatchContent("No accept proposal");

            if (mt1.match(msg)) {
                boatsGroup.add(msg.getSender());
                messagePendent = false;
                if (boatsGroup.size() < 3 && boatsRanking.size() > 0) {
                    sendOffer();
                } else {
                    ACLMessage msgFormed = new ACLMessage(ACLMessage.REQUEST);
                    msgFormed.addReceiver(boatCoordinator);
                    msgFormed.setContent("Group formed");
                    messagePendent = false;
                    myAgent.addBehaviour(new LeaderREInitiator(myAgent, msgFormed));
                }
            } else if (mt2.match(msg) && boatsRanking.size() > 0) {
                messagePendent = false;
                sendOffer();
            }
            showMessage("BoatGroup Size:" + (boatsGroup.size() + 1));
        }
    }

    private class LeaderREResponder extends SimpleAchieveREResponder {

        Agent myAgent;
        MessageTemplate mt;

        //Consturctor of the Behaviour
        public LeaderREResponder(Agent myAgent, MessageTemplate mt) {
            super(myAgent, mt);
            this.myAgent = myAgent;
            this.mt = mt;
        }

        protected ACLMessage prepareResponse(ACLMessage request) {
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.AGREE);

            MessageTemplate mt1 = MessageTemplate.MatchContent("Initiate grouping");
            MessageTemplate mt2 = MessageTemplate.MatchContent("No longer interested in form part of the group");
            MessageTemplate mt3 = MessageTemplate.MatchContent("Pendent request accepted");


            if (mt1.match(request)) {
                //showMessage("Order to initiate grouping recived from "+request.getSender().getLocalName());
            } else if (mt2.match(request)) {
                //showMessage("No longer interested in form part of our group recived from "+request.getSender().getLocalName());
            } else if (mt3.match(request)) {
                //showMessage("Pendent request from "+request.getSender().getLocalName() +" accepted");
            }

            return reply;
        }

        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            MessageTemplate mt1 = MessageTemplate.MatchContent("Initiate grouping");
            MessageTemplate mt2 = MessageTemplate.MatchContent("No longer interested in form part of the group");
            MessageTemplate mt3 = MessageTemplate.MatchContent("Pendent request accepted");
            MessageTemplate mt4 = MessageTemplate.MatchContent("Pendent message rejected");

            if (mt1.match(request)) {
                reply.setContent("Grouping...");
                sendOffer();
            } else if (mt2.match(request) && boatsRanking.size() > 0) {
                if (boatsGroup.size() == 3) {
                    ACLMessage msgDown = new ACLMessage(ACLMessage.REQUEST);
                    msgDown.addReceiver(boatCoordinator);
                    msgDown.setContent("Downgrade group counter");
                    myAgent.addBehaviour(new LeaderREInitiator(myAgent, msgDown));
                    sendOffer();
                }
                boatsGroup.remove(request.getSender());

                if (!messagePendent) {
                    sendOffer();
                }

                reply.setContent("Removed from the group");
            } else if (mt3.match(request)) {
                boatsGroup.add(request.getSender());
                messagePendent = false;
                if (boatsGroup.size() < 3 && boatsRanking.size() > 0) {
                    sendOffer();
                } else {
                    ACLMessage msgFormed = new ACLMessage(ACLMessage.REQUEST);
                    msgFormed.addReceiver(boatCoordinator);
                    msgFormed.setContent("Group formed");
                    myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, msgFormed));
                }
                reply.setContent("Added to group");
            } else if (mt4.match(request)){
                System.out.println("PENDENT REJEEEEEEEEEECTED!!!!!Recived");
                sendOffer();
            }

            return reply;
        }
    }

    private void sendOffer() {
        FishRank candidate = boatsRanking.remove(0);
        showMessage("Sending offer!!! TO: " + candidate.getBoat().getLocalName());
        ACLMessage msgToCandidate = new ACLMessage(ACLMessage.REQUEST);
        msgToCandidate.addReceiver(candidate.getBoat().getAID());
        try {
            msgToCandidate.setContentObject(candidate);
        } catch (IOException ex) {
            Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        msgToCandidate.setOntology("FishRank");
        this.addBehaviour(new LeaderREInitiator(this, msgToCandidate));
    }

    private class NOLeaderREInitiator extends SimpleAchieveREInitiator {

        Agent myAgent;
        ACLMessage msg;

        public NOLeaderREInitiator(Agent myAgent, ACLMessage msg) {
            super(myAgent, msg);
            this.myAgent = myAgent;
            this.msg = msg;
            messagePendent = true;
        }

        public void handleAgree(ACLMessage msg) {
            //showMessage("AGREE message recived from "+msg.getSender().getLocalName());
        }

        //handle Inform Messages
        public void handleInform(ACLMessage msg) {
            showMessage("Informative message from " + msg.getSender().getLocalName() + ": " + msg.getContent());
            MessageTemplate mt1 = MessageTemplate.MatchContent("Removed from the group");

            if (mt1.match(msg)) {
                ACLMessage acceptanceMessage = new ACLMessage(ACLMessage.REQUEST);
                acceptanceMessage.addReceiver(pendentOfAcceptance.candidateAID);
                acceptanceMessage.setContent("Pendent request accepted");
                leader = pendentOfAcceptance.candidateAID;
                bestFishRank = pendentOfAcceptance.candidateRank;
                pendentOfAcceptance = null;
                myAgent.addBehaviour(new SInitiatorBehaviour(myAgent, acceptanceMessage));
            }
        }
    }

    private class NOLeaderREResponder extends SimpleAchieveREResponder {

        Agent myAgent;
        MessageTemplate mt;

        //Consturctor of the Behaviour
        public NOLeaderREResponder(Agent myAgent, MessageTemplate mt) {
            super(myAgent, mt);
            this.myAgent = myAgent;
            this.mt = mt;
        }

        //Send an AGREE message to the sender
        protected ACLMessage prepareResponse(ACLMessage request) {
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.AGREE);
            reply.setOntology("");

            return reply;
        }

        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            ACLMessage reply = request.createReply();
            reply.setOntology("");
            reply.setPerformative(ACLMessage.INFORM);

            MessageTemplate mt1 = MessageTemplate.MatchOntology("FishRank");


            if (mt1.match(request)) {
                try {
                    FishRank candidateRank = (FishRank) request.getContentObject();

                    if (leader == null) {
                        bestFishRank = candidateRank;
                        leader = request.getSender();
                        reply.setContent("Accept the proposal");
                    } else {
                        boolean isNotBest = false;

                        if ((pendentOfAcceptance != null && pendentOfAcceptance.candidateRank.compareTo(candidateRank)==1)||bestFishRank.compareTo(candidateRank) == 1 || isNotBest) {
                            reply.setContent("No accept proposal");
                        } else {
                            ACLMessage msgToPrevLeader = new ACLMessage(ACLMessage.REQUEST);
                            if(pendentOfAcceptance != null){
                                System.out.println("PENDENT REJEEEEEEEEEECTED");
                                msgToPrevLeader.setContent("Pendent message rejected");
                                msgToPrevLeader.addReceiver(pendentOfAcceptance.candidateAID);
                            }else{
                                msgToPrevLeader.setContent("No longer interested in form part of the group");
                                msgToPrevLeader.addReceiver(leader);
                            }
                            myAgent.addBehaviour(new NOLeaderREInitiator(myAgent, msgToPrevLeader));
                            CandidateLeader cl = new CandidateLeader(request.getSender(), candidateRank);
                            pendentOfAcceptance = cl;
                            reply.setContent("Pendent to response");
                        }
                    }
                } catch (UnreadableException ex) {
                    Logger.getLogger(BoatAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return reply;
        }
    }

    private class CandidateLeader implements Comparable<CandidateLeader> {

        AID candidateAID;
        FishRank candidateRank;

        public CandidateLeader(AID candidateAID, FishRank candidateRank) {
            this.candidateAID = candidateAID;
            this.candidateRank = candidateRank;
        }

        @Override
        public int compareTo(CandidateLeader t) {
            return this.candidateRank.compareTo(t.candidateRank);
        }
    }

    private class NegotiateSalesCNInitiator extends ContractNetInitiator {

        BoatAgent myAgent;
        ACLMessage msg;
        double acceptedPrice;
        Vector<ACLMessage> accepted;

        public NegotiateSalesCNInitiator(BoatAgent myAgent, ACLMessage msg) {
            super(myAgent, msg);
            this.myAgent = myAgent;
            this.msg = msg;
            messagePendent = true;
            accepted = new Vector<ACLMessage>();
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            double bestOffer = 0;
            int bestPortIdx = -1;

            this.accepted = new Vector<ACLMessage>();

            for (ACLMessage msg : (Vector<ACLMessage>) responses) {
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    this.accepted.add(msg);

                    try {
                        double offer = ((Double) msg.getContentObject()).doubleValue();
                        if (offer > bestOffer) {
                            bestOffer = offer;
                            bestPortIdx = accepted.size()-1;
                        }
                        //System.out.println(myAgent.getLocalName() + ": Received offer from " + msg.getSender().getLocalName() + ", value is " + String.valueOf(offer));
                    } catch (UnreadableException e) {
                        System.out.println(myAgent.getLocalName() + ": Failed to read offer from " + msg.getSender().getLocalName() + "!!");
                    }

                   // ACLMessage rsp = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                   // rsp.addReceiver(msg.getSender());
                } else if (msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD) {
                    System.out.println(myAgent.getLocalName() + ": Message from " + msg.getSender().getLocalName() + " not understood!!");
                }
            }

            if (bestPortIdx == -1) {
                System.out.println(myAgent.getLocalName() + ": All boats rejected the proposals, failed to sell the seafoods!!");
            } else {
                // Set acceptance response to best offer
                ACLMessage response = (ACLMessage) accepted.get(bestPortIdx);
                ACLMessage acceptedReply  = response.createReply();
                acceptedReply.setContent(null);
                acceptedReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                acceptedReply.setSender(myAgent.getAID());
                Object fu = acceptedReply.getSender();
                
               
                acceptances.clear();              
                
                //Add to reply list
                acceptances.add(acceptedReply);
                
                //Set refusal to the other offers
                for(int i = 0; i < accepted.size(); i++ ){
                    if(i != bestPortIdx){
                        ACLMessage resp = (ACLMessage) accepted.get(i);
                        ACLMessage reply = resp.createReply();
                        reply.setSender(myAgent.getAID());
                        //showMessage("REFUSING PROP "+resp.getSender().getLocalName());
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        acceptedReply.setContent(null);
                        acceptances.add(reply);
                    }
                }
                
                // Set current sell price
                this.acceptedPrice = bestOffer;

                //System.out.println(myAgent.getLocalName() + ": Accepted offer from " + response.getSender().getLocalName() + ", waiting confirmation...");
            }
        }

        // If port finally buys deposits
        @Override
        protected void handleInform(ACLMessage inform) {
            this.myAgent.sellDeposits(acceptedPrice);
            //System.out.println(myAgent.getLocalName() + ": Fish sold to " + inform.getSender().getLocalName() + ".");
        }

        // If port cancels offer after the boat has accepted it
        @Override
        protected void handleFailure(ACLMessage msg) {
            // If no more initial acceptances, dump deposits
            showMessage("Port abortion");
            if (accepted.isEmpty()) {
                showMessage("Dumping deposits");
                this.myAgent.sellDeposits(0);
                return;
            }

            // Prepare new iteration message
            Vector<ACLMessage> messages = new Vector<ACLMessage>();
            ACLMessage rsp = new ACLMessage(ACLMessage.CFP);
            try {
                
                rsp.setContentObject(myAgent.deposits);
            } catch (Exception e) {
                System.out.println(myAgent.getLocalName() + ": Failed to build sale request message (at newIteration)!!");
            }

            // Prepare new iteration receivers
            for (ACLMessage id : this.accepted) {
                rsp.addReceiver(id.getSender());
            }

            // Start new iteration
            messages.add(rsp);
            this.newIteration(messages);
        }
    }
}