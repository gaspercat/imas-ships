package sma.ontology;

import jade.core.AID;

/**
 * <p><B>Title:</b> IA2-SMA</p>
 * <p><b>Description:</b> Practical exercise 2011-12. Recycle swarm.</p>
 * <p><b>Copyright:</b> Copyright (c) 2011</p>
 * <p><b>Company:</b> Universitat Rovira i Virgili (<a
 * href="http://www.urv.cat">URV</a>)</p>
 * @author David Isern & Joan Albert Lopez
 */
public class InfoAgent extends Object implements java.io.Serializable {
	private AID aid;
	private AgentType agentType;
	private PortType portType;
	
	public  double _maxQuantitySeafood = 0, _qtyTuna = 0, _qtyOctopus = 0, _qtyLobster = 0, _qtyShrimp = 0;
	public double _money = 0;
	public String _name, _shortName;

	public PortType getPortType() {
		return portType;
	}
	
	public void setPortType(PortType portType) {
		this.portType = portType;
	}
	
	public AgentType getAgentType()
	{
		return agentType;
	}
	
	public void setAgentType(AgentType AgentType)
	{
		if (AgentType == AgentType.Boat)
		{
			agentType = AgentType.Boat;
			portType = PortType.None;
		}
		else
			agentType = AgentType.Port;
	}
	
	public boolean equals(InfoAgent a) {
		return a.getAID().equals(this.aid);
	}

	public AID getAID() {
		return this.aid;
	}

	public void setAID(AID aid) {
		this.aid = aid;
	}
	
	public String getName()
	{
		return this._name;
	}
	
	public String getShortName()
	{
		return  this._shortName;
	}
	
	public void setName(String name, String shortName)
	{
		this._name = name;
		this._shortName = shortName;
	}

	public String toString() {
		String str = "";
		str = "(info-agent (agent-type " + this.getAgentType().toString() + "))";
		return str;
	}

	/**
	 * Default constructor
	 * @param agentType int Type of the agent we want to save
	 * @param aid AID Agent identifier
	 * @throws Exception Errors in the assignation
	 */
	public InfoAgent(AgentType agentType, AID aid) throws Exception {
		this.setAgentType(agentType);
		this.setAID(aid);
	}

	/**
	 * Constructor for the information of the agent, without its AID
	 * @param agentType int
	 * @throws Exception Errors in the assignation
	 */
	public InfoAgent(AgentType agentType) throws Exception {
		this.setAgentType(agentType);
		agentType = AgentType.Boat;
	}
	
	public void setMaxQuantitySeafood(double MaxQty)
	{
		_maxQuantitySeafood = MaxQty;
	}
	
	public void setQuantityOfTuna(double Qty)
	{
		_qtyTuna = Qty;
	}
	
	public void setQuantityOfOctopus(double Qty)
	{
		_qtyOctopus = Qty;
	}
	
	public void setQuantityOfLobster(double Qty)
	{
		_qtyLobster = Qty;
	}
	
	public void setQuantityOfShrimp(double Qty)
	{
		_qtyShrimp = Qty;
	}
	
	public void setMoneyAvailable(double Money)
	{
		_money = Money;
	}
	
	public double getMaxQuantitySeafood()
	{
		return _maxQuantitySeafood;
	}
	
	public double getQuantityOfTuna()
	{
		return _qtyTuna ;
	}
	
	public double getQuantityOfOctopus()
	{
		return _qtyOctopus;
	}
	
	public double getQuantityOfLobster()
	{
		return _qtyLobster;
	}
	
	public double getQuantityOfShrimp()
	{
		return _qtyShrimp;
	}
	
	public double getAvailableMoney()
	{
		return _money;
	}

} //endof class InfoAgent
