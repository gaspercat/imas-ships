package sma.gui;

import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.Composite;
import java.awt.geom.Rectangle2D;
import java.awt.AlphaComposite;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage; 
import javax.imageio.*;
import java.io.*;
import java.util.List;

import sma.ontology.AgentType;
import sma.ontology.Cell;
import sma.ontology.CellType;
import sma.ontology.InfoAgent;
import sma.ontology.SeaFoodType;
/**
 * <p><B>Title:</b> IA2-SMA</p>
 * <p><b>Description:</b> Practical exercise 2011-12. Recycle swarm.</p>
 * Visualization of the map. There are several elements to depict, as
 * buildings, streets, recycling centers, the agents, etc.<br>
 * This class *should be* modified and improved in order to show as good as
 * possible all the changes in the simulation. We provide several high-level
 * methods which can be rewritten as needed.<br>
 * <p><b>Copyright:</b> Copyright (c) 2011</p>
 * <p><b>Company:</b> Universitat Rovira i Virgili (<a
 * href="http://www.urv.cat">URV</a>)</p>
 * @author David Isern & Joan Albert L�pez
 */
public class MapVisualizer extends JPanel {

  private int inset = 55;
   int nrows, ncols;
   private Cell[][] t;
   java.awt.Point start, end;
   int dx, dy, gap;
   private Rectangle2D.Double cellBorder;
   
   private Ellipse2D.Double agentFigure;



   public MapVisualizer(Cell[][] t) {
     this.t = t;
     nrows = t.length;
     ncols = t[0].length;

     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     start = new Point(inset, inset);
     end = new Point(screenSize.width - inset * 4, screenSize.height - inset * 4);
     dx = ((end.x-start.x)/ncols);
     dy = ((end.y-start.y)/nrows) + 6;

     gap = 5;
     cellBorder = new Rectangle2D.Double(gap+10, gap+10, dx, dy);

     agentFigure = new Ellipse2D.Double(gap+10+(dx/4),gap+10+(dy/4),(dx/2),(dy/2));

   }

    
    private void moveXY(Graphics2D g2d, int x, int y) {
      g2d.translate(dx * x, dy * y);
    }

    private void drawAgent(Graphics2D g2d, int x, int y, Cell c) {
      if(c.isThereAnAgent()) {
        List<InfoAgent> agents = c.getAgents();
        for (InfoAgent agent : agents)
        {
	        g2d.setPaint(Color.decode("#848484"));
	        g2d.translate((dx/6),(dy/6));
	        g2d.fill(agentFigure);
	        g2d.translate(-(dx/6),-(dy/6));
	        
        	String msg = c.getLabel();
        	
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                 java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.Font font = new java.awt.Font("Serif", java.awt.Font.PLAIN, 9);
            g2d.setFont(font);
            g2d.setPaint(Color.BLACK);
            g2d.drawString(msg,dx-30,dy-3);
        }
      }
      
    }

    private void drawSea(Graphics2D g2d, int x, int y, Cell c) {
      try {
        g2d.setPaint(Color.decode("#A9D0F5"));
        g2d.fill(cellBorder);
        g2d.setPaint(Color.DARK_GRAY);
        g2d.draw(cellBorder);
      } catch(Exception e) {
        e.printStackTrace();
      }

    }
    
    private void drawSeaFood(Graphics2D g2d, int x, int y, Cell c) {
      try {
        if (t[x][y].getSeaFoodType() == SeaFoodType.Tuna){
            g2d.setPaint(Color.GREEN);
            g2d.fill(cellBorder);
            g2d.setPaint(Color.GREEN);
            g2d.draw(cellBorder);
        }else if (t[x][y].getSeaFoodType() ==  SeaFoodType.Octopus){
            g2d.setPaint(Color.decode("#8D38C9"));
            g2d.fill(cellBorder);
            g2d.setPaint(Color.decode("#8D38C9"));
            g2d.draw(cellBorder);
        }else if (t[x][y].getSeaFoodType() ==  SeaFoodType.Lobster){
            g2d.setPaint(Color.YELLOW);
            g2d.fill(cellBorder);
            g2d.setPaint(Color.YELLOW);
            g2d.draw(cellBorder);
        }else{
            g2d.setPaint(Color.RED);
            g2d.fill(cellBorder);
            g2d.setPaint(Color.RED);
            g2d.draw(cellBorder);
        }
        
      } catch(Exception e) {
        e.printStackTrace();
      }

    }
    

    public void paintComponent(Graphics g) {
      clear(g);
      Graphics2D g2d = (Graphics2D)g;
      for(int i=0; i<t.length; i++) {
        for(int j=0; j<t[0].length; j++) {
          g2d.draw(cellBorder);
          if(t[i][j].getCellType()== CellType.Boat)
        	  drawAgent(g2d, i, j, t[i][j]); 
          else if(t[i][j].getCellType() == CellType.Seafood)
                  drawSeaFood(g2d,i,j,t[i][j]);
          else
        	  drawSea(g2d, i, j, t[i][j]);
          g2d.translate(dx,0);
        }
        g2d.translate(-(dx*t[0].length),dy);
      }

      this.repaint();
   }

   protected void clear(Graphics g) {
     super.paintComponent(g);
   }

   protected Ellipse2D.Double getCircle() {
     return(agentFigure);
   }

 }
