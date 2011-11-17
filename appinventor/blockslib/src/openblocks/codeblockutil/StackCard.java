package openblocks.codeblockutil;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import openblocks.codeblockutil.CScrollPane.ScrollPolicy;


/**
 * A StackCard is used by explorers to interface between
 * a CSwing Canvas and a CSwing explorer.
 *
 * An explorer explores Canvases.  This is the foundation
 * onto which the factory UI is built.  However, an
 * Explorer and a Canvas are two very different objects.
 * One is a high-level CSwing UI that manages the display
 * of internal components and controls the position and visibility
 * of internal components.  The other (that is, the Canvas) is a
 * low-level CSwing component that has no control over itself
 * (or at least it shouldn't).  An mediator is required
 * to link the two objects together.
 *
 * We solve this interface problem by employing a
 * mediator between an Explorer and a Canvas.  That's
 * where a StackCard comes in.  A StackCard (whose visibility should be
 * limited within the package) takes commands from
 * it's parent Explorer and directs it's Canvas to
 * follow the commands.  In the opposite direction, a drawer also takes
 * in user-generated actions and informs the parent
 * explorer of what had just happed, so that the parent
 * explorer can decide what to do with it.
 *
 */
class StackCard implements PropertyChangeListener{
	private int buttonHeight = 30;
	/** Canvas that this StackCard renders */
	private Canvas canvas;
	/** A scroll pane to display as much of the Canvas as possible */
	private CardPane drawerPane;
	/** Parent explorer */
	private StackExplorer explorer;
	/** number of partitions */
	private final int partitions = 5;
	// (destination - origin) / partitions
	/** changes in dimensions as time progresses */
	private int dx, dy, dw, dh, count;
	/** source and destination bounds */
	private Rectangle origin, destination;
	/** true iff directed to destination */
	private boolean directedToDestination;

	/**
	 * Constructs new StackCard with a parent Explorer
	 * @param canvas
	 *
	 * @requires canvas!=null && canvas.name!=null
	 * 			&& canvas.JCOmponent != null
	 */
	StackCard(Canvas canvas){
		this(canvas, null);
	}
	/**
	 * Constucts a new StackCard with an explorer
	 * @param canvas
	 * @param explorer
	 *
	 * @requires canvas!=null && canvas.name!=null
	 * 			&& canvas.JCOmponent != null
	 * @throws RuntimeException if canvas==null || canvas.name==null
	 * 			|| canvas.JCOmponent == null
	 */
	StackCard(Canvas canvas, StackExplorer explorer){
		if(canvas == null ||
				canvas.getName() == null ||
				canvas.getJComponent() == null){
			throw new RuntimeException("Parameters may not be null");
		}
		this.canvas = canvas;
		this.explorer = explorer;
		this.drawerPane = new CardPane();


		if(drawerPane == null) throw new RuntimeException("May not pass in a null instance of drawerPane");
		this.directedToDestination = false;
		count = 0;
		origin = drawerPane.getBounds();
		destination = drawerPane.getBounds();
		dx = 0;
		dy = 0;
		dw = 0;
		dh = 0;

		canvas.getJComponent().addPropertyChangeListener(this);
	}
    public void propertyChange(PropertyChangeEvent e){
    	if (e.getPropertyName().equals(Canvas.LABEL_CHANGE)){
    		drawerPane.repaint();
    	}
    }
	/**
	 * notifies parent explorer that the user has selected this drawer.
	 */
	private void notifySelection(){
		if(this.explorer != null){
			this.explorer.notifySelection(this, !directedToDestination);
		}
	}
	/**
	 * True iff directed to DESTINATION
	 */
	boolean isDirectedToDestination(){
		return this.directedToDestination;
	}
	/**
	 * sets the bounds of this drawer
	 * @param r
	 * @requires r != null
	 */
	void setBounds(Rectangle r){
		this.drawerPane.setBounds(r);
	}
	/**
	 * sets the bounds of this drawer
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	void setBounds(int x, int y, int width, int height){
		this.drawerPane.setBounds(x, y, width, height);
	}
	Rectangle getDestination(){
		return this.destination;
	}
	Rectangle getOrigin(){
		return this.origin;
	}
	/**
	 * @return the JComponent representation of this
	 */
	JComponent getJComponent(){
		return this.drawerPane;
	}
	/**
	 * reassigns the source bounds and the destination bounds
	 * of this this StackCard.  When goToOrigin is called, this drawer
	 * moves to "origin".  When gotoDestination is called,
	 * this Drawer moves to "destination"
	 * @param origin
	 * @param destination
	 *
	 * @requires origin != null && destination != null
	 */
	void reformDrawer(Rectangle origin, Rectangle destination){
		this.origin = origin;
		this.destination = destination;
		dx = (destination.x-origin.x)/(partitions);
		dy = (destination.y-origin.y)/(partitions);
		dw = (destination.width-origin.width)/(partitions);
		dh = (destination.height-origin.height)/(partitions);
	}
	/**
	 * move to Origin
	 */
	void goToOrigin(){
		this.directedToDestination = false;
	}
	/**
	 * move to destination
	 */
	void goToDestination(){
		this.directedToDestination = true;
	}


	void animate(){
		if(count < 0 || count > partitions){
			new RuntimeException("StackCard may not grow or move beyond the" +
					"boundaries set by the origin and destination bounds").printStackTrace();
		}else{
			int x, y, w, h;
			x = this.origin.x + dx*count;
			y = this.origin.y + dy*count;
			w = this.origin.width + dw*count;
			h = this.origin.height + dh*count;
			drawerPane.setBounds(x,y,w,h);
			drawerPane.revalidate();
			if(directedToDestination){
				if(count == partitions){
					//do nothing, it has already reached it's end
				}else{
					count++;
				}
			}else{
				if(count == 0){
					//do nothing, it has already reached it's end
				}else{
					count--;
				}
			}
		}
	}

	/**
	 * The scroll pane that displays the canvas.  We must use a
	 * scroll pane because the size of the Canvas may be
	 * much greater than the size of the Drawer itself.
	 * @author An Ho
	 */
	private class CardPane extends JPanel{
		private static final long serialVersionUID = 49283583495L;
		/** The scroll pane that wraps the entire canvas. */
		private CScrollPane scroll;
		/** The drawer's label */
		private CardLabel label;
		/** Constructor */
		private CardPane () {
			super(new BorderLayout());
			this.setOpaque(false);
			this.scroll = new CHoverScrollPane(
					canvas.getJComponent(),
					ScrollPolicy.VERTICAL_BAR_AS_NEEDED,
					ScrollPolicy.HORIZONTAL_BAR_AS_NEEDED,
					18, canvas.getColor(), new Color(100,100,100,100));
			this.label = new CardLabel();
			this.add(label, BorderLayout.NORTH);
			this.add(scroll, BorderLayout.CENTER);
		}
	}

	/**
	 * The Tabs that displays the name and hints at the colors
	 * of this canvas.
	 * @author An Ho
	 */
	private class CardLabel extends JButton implements ActionListener{
		private static final long serialVersionUID = 3489589234L;
    	//To get the shadow effect the text must be displayed multiple times at
		//multiple locations.  x represents the center, white label.
		// o is color values (0,0,0,0.5f) and Y is black.
		//			  o o
		//			o x Y o
		//			o Y o
		//			  o
		//offsetArrays representing the translation movement needed to get from
		// the center location to a specific offset location given in {{x,y},{x,y}....}
		//..........................................grey points.............................................black points
		private final int[][] shadowPositionArray = {{0,-1},{1,-1}, {-1,0}, {2,0},	{-1,1}, {1,1},  {0,2}, 	{1,0},  {0,1}};
		private final float[] shadowColorArray =	{0.5f,	0.5f,	0.5f, 	0.5f, 	0.5f, 	0.5f,	0.5f,	0,		0};
		private double offsetSize = 1;
		/**
		 * Constructs a new DrawerLabel with a shadaow-outlined text.
		 */
		private CardLabel(){
			super();
			this.setOpaque(false);
			this.setBorder(null);
			this.setFont(new Font("Ariel", Font.BOLD, buttonHeight - 3));
			this.setPreferredSize(new Dimension(0, buttonHeight));
			this.setCursor(new Cursor(Cursor.HAND_CURSOR));
			this.addActionListener(this);
		}
		/**
		 * Notifies parent explorer that this drawer was selected
		 */
		public void actionPerformed(ActionEvent e){
			StackCard.this.notifySelection();
		}
		/**
		 * Paints this label with a shadow-outlined text and background
		 * matching that of the canvas's color, or grey by default.
		 */
		public void paint(Graphics g){
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			//draw tab
			Color highlight = canvas.getHighlight();
			if(highlight == null){
				//g2.setPaint(new GradientPaint(0,0,new Color(canvas.getColor().getRed(), canvas.getColor().getGreen(), canvas.getColor().getBlue(), 100),0,this.getHeight()/3,canvas.getColor(), false));
				g2.setColor(canvas.getColor());
			}else{
				g2.setColor(highlight);
				//g2.setColor(canvas.getColor());
			}
			g2.fillRoundRect(0,0,this.getWidth()-2, 2*this.getHeight(), this.getHeight(),this.getHeight());
			if(highlight == null){
				g2.setStroke(new BasicStroke(2));
				g2.setColor(new Color(255,255,255,100));
			}else{
				g2.setStroke(new BasicStroke(2));
				//g2.setColor(new Color(canvas.getColor().getRed(), canvas.getColor().getGreen(), canvas.getColor().getBlue(), 100));
				//g2.setColor(new Color(240,240,40, 240));
				g2.setColor(new Color(0,0,0,100));
			}
			g2.drawRoundRect(0,0,this.getWidth()-2, 2*this.getHeight(), this.getHeight(),this.getHeight());

			//draw text
			String text = canvas.getName();
			if(text != null){
				Font font = g2.getFont().deriveFont((float)(((float)this.getHeight()) * .4));
				g2.setFont(font);
				FontMetrics metrics = g2.getFontMetrics();
				Rectangle2D textBounds = metrics.getStringBounds(text,g2);
				float x = (float)((this.getWidth() / 2) - (textBounds.getWidth() / 2));
				float y = (float)((this.getHeight() / 2) + (textBounds.getHeight() / 2)) - metrics.getDescent();

				g.setColor(Color.black);
				for (int i = 0; i < shadowPositionArray.length; i++) {
					int dx = shadowPositionArray[i][0];
					int dy = shadowPositionArray[i][1];
					g2.setColor(new Color(0,0,0, shadowColorArray[i]));
					g2.drawString(text, x+(int)((4+dx)*offsetSize), y+(int)((dy-6)*offsetSize));
				}
				if(highlight == null){
					g2.setColor(Color.white);
				}else{
					g2.setColor(highlight);
				}
				g2.drawString(text, x+(int)((4)*offsetSize), y+(int)((-6)*offsetSize));
			}
		}
	}
}