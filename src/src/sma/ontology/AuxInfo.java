package sma.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import sma.gui.UtilsGUI;
import java.util.*;

/**
 * <p>
 * <B>Title:</b> IA2-SMA
 * </p>
 * <p>
 * <b>Description:</b> Practical exercise 2011-12. Recycle swarm.
 * </p>
 * Main information about the game which is sent to the coordinator agent during the
 * initialization. This object is initialized from a file.
 * <p>
 * <b>Copyright:</b> Copyright (c) 2011
 * </p>
 * <p>
 * <b>Company:</b> Universitat Rovira i Virgili (<a
 * href="http://www.urv.cat">URV</a>)
 * </p>
 * 
 * @author David Isern & Joan Albert L�pez
 * @see sma.CoordinatorAgent
 * @see sma.CentralAgent
 */
public class AuxInfo implements java.io.Serializable {

	protected Cell[][] map;

	private int numSeafoodGroups;
	private int numBoats;
	private int numPorts;
	private double moneyPorts;
	private double capacityBoats;
	private double capacityPorts;
	private int numFishingPhasesToNegotiate;
	private int numTotalNegotiations;
	private HashMap<PortType, Integer> portTypeQty = new HashMap<PortType, Integer>();
	private HashMap<InfoAgent, Cell> agentsInitialPosition = new HashMap<InfoAgent, Cell>();
        private SeaFoodsElements seaFoods = new SeaFoodsElements();
	// For each InfoAgent it contains its initial cell
	
	private List<InfoAgent> ports = new ArrayList<InfoAgent>();
	
	public Cell[][] getMap() {
		return this.map;
	}

	public void setMap(Cell[][] map) {
		this.map = map;
	}
	
	public List<InfoAgent> getPorts()
	{
		return ports;
	}
	
	public void setPortAgent(InfoAgent agent)
	{
		if (!ports.contains(agent))
			ports.add(agent);
	}
	
	public void fillAgentsInitialPositions(List<Cell> agents) {
		for (Cell c : agents)
		{
			for (InfoAgent agent : c.getAgents())
				agentsInitialPosition.put(agent, c);
		}
			
	}

	public HashMap<InfoAgent, Cell> getAgentsInitialPosition() {
		return agentsInitialPosition;
	}

	public void setAgentsInitialPosition(InfoAgent agent, Cell cell) {
		if (!agentsInitialPosition.containsKey(agent))
			agentsInitialPosition.put(agent, cell);
	}
	
	
	public void setPortTypesQuantity(PortType port, Integer qty) {
		if (!portTypeQty.containsKey(port))
			portTypeQty.put(port, qty);
	}
	
	public HashMap<PortType, Integer> getPortTypesQuantity() {
		return portTypeQty;
	}

	public Cell getCell(int x, int y) {
		return this.map[x][y];
	}

	public void setCell(int x, int y, Cell c) {
		this.map[x][y] = c;
	}

	public int getNumSeafoodGroups() {
		return this.numSeafoodGroups;
	}
	
	public int getNumPorts()
	{
		return this.numPorts;
	}

	public int getNumBoats() {
		return this.numBoats;
	}
	
	public int getNumFishingPhasesToNegotiate()
	{
		return this.numFishingPhasesToNegotiate;
	}
	
	public int getNumNegotiationPhases()
	{
		return this.numTotalNegotiations;
	}

	protected void setNumSeafoodGroups(int numSeafoodGroups) {
		this.numSeafoodGroups = numSeafoodGroups;
	}
	
	protected void setNumPorts(int numPorts)
	{
		this.numPorts = numPorts;
	}

	protected void setNumBoats(int numBoats) {
		this.numBoats = numBoats;
	}
	
	protected void setNumFishingPhasesToNegotiate(int numFishingPhasesToNegotiate)
	{
		this.numFishingPhasesToNegotiate = numFishingPhasesToNegotiate;
	}
	
	protected void setNumTotalNegotiations(int numTotalNeg)
	{
		this.numTotalNegotiations = numTotalNeg;
	}
	

	protected void setCapacityBoatsPerDeposit(double capacityBoats) {
		this.capacityBoats = capacityBoats;
	}

	public double getCapacityBoats() {
		return this.capacityBoats;
	}

	protected void setCapacityPortsPerDeposit(double capacityPorts) {
		this.capacityPorts = capacityPorts;
	}

	public double getCapacityPorts() {
		return this.capacityPorts;
	}

	protected void setMoneyPorts(double money) {
		this.moneyPorts = money;
	}

	public double getMoneyPorts() {
		return this.moneyPorts;
	}
        
        public void setSeaFoods(SeaFoodsElements sf){
            this.seaFoods = sf;
        }
        
        public SeaFoodsElements getSeaFoods(){
            return this.seaFoods;
        }
}
