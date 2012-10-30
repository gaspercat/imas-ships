package sma.ontology;

import java.util.*;
import java.io.Serializable;

/**
 * <p>
 * <B>Title:</b> IA2-SMA
 * </p> *
 * <p>
 * <b>Description:</b> Practical exercise 2011-12. Recycle swarm. This class
 * keeps all the information about a cell in the map.
 * <p>
 * <b>Copyright:</b> Copyright (c) 2011
 * </p> *
 * <p>
 * <b>Company:</b> Universitat Rovira i Virgili (<a
 * href="http://www.urv.cat">URV</a>)
 * </p>
 * 
 * @author not attributable
 * @version 2.0
 */
public class Cell implements Serializable {

	private List<InfoAgent> agents;
	private int row = -1;
	private int column = -1;
	
	private CellType type;
        
        private String seaFoodType;

	public Cell(CellType Type) {
		agents = new ArrayList<InfoAgent>();
		type = Type;
	}
	
	public void setType(CellType Type)
	{
		type = Type;
	}

	/** *********************************************************************** */

	public void setRow(int i) {
		this.row = i;
	}

	public int getRow() {
		return this.row;
	}

	public void setColumn(int i) {
		this.column = i;
	}

	public int getColumn() {
		return this.column;
	}


	public boolean isThereAnAgent() {
		return (agents.size() > 0? true : false);
	}

	public void addAgent(InfoAgent newAgent) throws Exception {
		System.out.println("Adding agent to " + this.toString());
		
		if (agents.contains(newAgent))
			throw new Exception("Agent already exists in cell");
		else
			// if everything is OK, we add the new agent to the cell
			this.agents.add(newAgent);
	}

	private boolean isAgent(InfoAgent infoAgent) {
		if (infoAgent == null)
			return false;
		else {
			if (this.agents != null)
				return this.agents.contains(infoAgent);
			else
				return false;
		}
	}

	public void removeAgent(InfoAgent oldInfoAgent) throws Exception {
		boolean removed = agents.remove(oldInfoAgent);
		
		if (!removed)
			throw new Exception("InfoAgent not here");
	}

	public List<InfoAgent> getAgents() {
		return this.agents;
	}

		/** *********************************************************************** */

	public String toString() {
		String str = "";
		try {
			str = "(cell- " + " "+ "(r " + this.getRow() + ")" + "(c "+ this.getColumn() + ")";
			str = str + ")";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}
	
	public String getLabel()
	{
		String lbl = "";
		
		if (this.isThereAnAgent())
		{
			for (InfoAgent agent : agents)
			{
				if (lbl.equalsIgnoreCase(""))
					lbl += agent.getShortName() + "\r";
				else
					lbl += "-" + agent.getShortName() + "\r";
			}
		}
		
		return lbl;
	}
	
	public CellType getCellType()
	{
		return type;
	}
	
        public void setSeaFoodType(String type){
            this.seaFoodType = type;
        }
        
        public String getSeaFoodType(){
            return this.seaFoodType;
        }
	

} // endof class Cell

