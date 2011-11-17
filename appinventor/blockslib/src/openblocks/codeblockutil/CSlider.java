package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CSlider extends JPanel implements MouseListener, MouseMotionListener{
	private static final long serialVersionUID = 328149080257L;
	/** Property name of the event thrown by this widget */
	public static String VALUE_CHANGED = "VALUE_CHANGED";
	/** Rendering Hints of this */
	static final RenderingHints renderingHints = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
	/** value */
	private int value;
	/** The value representing the left side of slider */
	private int left;
	/** The value representing the right side of the slider */
	private int right;
	/** track color from min to value */
	private final Color leadingColor;
	/** track color from value to max */
	private final Color trailingColor;
	/** color of thumb */
	private final Color thumbColor;

	private final SliderBlueprint blueprint;
	private final float trackThickness;//a floating point number between 0 and 1
	private int offset = 0;
	private int thumbStart = 0; // position where the thumb starts off
	private boolean setTicks = false; // on off ticks
	private int tickNumber = 1; //number of ticks when ticks/sticky is turned on
	private boolean startMark = false; // whether the start mark of the thumb is turned on
	private String startMarkLabel = ""; //what the label for the start point is
	
	/**
	 * @requires none
	 * @effects Constructs a CSlider such that min=0 && max=100 &&
	 * 			value = 50 && the track color from min to value
	 * 			is RED && the track color from value to max is gray &&
	 * 			the color of the thumb is BROWN
	 */
	public CSlider(){
		this(0, 100, 50, 0.2f,  Color.blue, Color.black, Color.gray, false, 0, false, "");
	}

	/**
	 * @param left - the value representing the left side of the slider
	 * @param right - the value representing the right side of the slider
	 * @param value - value
	 * 
	 * @requires min(left, right) < value < max(left, right)
	 * @effects Constructs a CSlider such that left = left, right=right
	 * 			&& value = value &&
	 *          The track color from min to value
	 * 			is RED && the track color from value to max is gray &&
	 * 			the color of the thumb is BROWN
	 */
	public CSlider(int left, int right, int value){
		this(left, right, value, 0.2f, Color.blue, Color.black, Color.gray, false, 0, false, "");
	}

	
	/**
	 * @param left - the value representing the left side of the slider
	 * @param right - the value representing the right side of the slider
	 * @param value - value
	 * @param setTicks - boolean representing whether there should be ticks
	 * 
	 * @requires min(left, right) < value < max(left, right)
	 * @effects Constructs a CSlider such that left = left, right=right
	 * 			&& value = value &&
	 *          The track color from min to value
	 * 			is RED && the track color from value to max is gray &&
	 * 			the color of the thumb is BROWN
	 * 			turns on or off the sticky tick intervals
	 */
	public CSlider(int left, int right, int value, boolean setTicks, int numTicks){
		this(left, right, value, 0.2f, Color.blue, Color.black, Color.gray, setTicks, numTicks, false, "");
	}

	/**
	 * @param left - the value representing the left side of the slider
	 * @param right - the value representing the right side of the slider
	 * @param value - value
	 * @param setTicks - boolean representing whether there should be ticks
	 * @param startMark - turn the mark for the start of the thumb on
	 * 
	 * @requires min(left, right) < value < max(left, right)
	 * @effects Constructs a CSlider such that left = left, right=right
	 * 			&& value = value &&
	 *          The track color from min to value
	 * 			is RED && the track color from value to max is gray &&
	 * 			the color of the thumb is BROWN
	 * 			turns on or off the sticky tick intervals
	 */
	public CSlider(int left, int right, int value, boolean setTicks, int numTicks, boolean startMark, String startMarkLabel){
		this(left, right, value, 0.2f, CGraphite.darkgreen, Color.black, Color.gray, setTicks, numTicks, startMark, startMarkLabel);
	}
	
	/**
	 * @param left - the value representing the left side of the slider
	 * @param right - the value representing the right side of the slider
	 * @param value - value
	 * @param leadingTrackColor - the color of the track from min to value
	 * @param trailingTrackColor - the color of the track from value to max
	 * @param thumbColor - the color of the thumb
	 * 
	 * @requires leadingColor != null &&
	 * 			 trailingCOor 1=null &&
	 * 			 thumbColor ! =null &&
	 * 			 min(left, right) < value < max(left, right)
	 * @effects Constructs a CSlider
	 */
	public CSlider(int left, int right, int value, float thickness,
			Color leadingTrackColor, Color trailingTrackColor, Color thumbColor, 
			boolean setTicks, int numTicks, boolean startMark, String startMarkLabel){
		super(null);
		this.startMark = startMark;
		this.startMarkLabel = startMarkLabel;
		thumbStart = value;
		this.setTicks = setTicks;
		tickNumber = numTicks;
		setSize(100,25);
		setOpaque(false);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		leadingColor = leadingTrackColor;
		trailingColor = trailingTrackColor;
		this.thumbColor = thumbColor;
		this.left = left;
		this.value = value;
		this.right = right;
		trackThickness = thickness;
		blueprint = new SliderBlueprint();
		// Just to get the thumb to start off properly on start-up
		reformBlueprint(blueprint,
		    getWidth(),
		    getHeight(), (int)(trackThickness*getHeight()), thumbStart);
		addActionListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	private void addActionListener(CSlider slider) {
		repaint();
	}

	/**
	 * @return right
	 */
	public int getRight(){
		return right;
	}
	/**
	 * @return left
	 */
	public int getLeft(){
		return left;
	}
	/**
	 * @return value
	 */
	public int getValue(){
		return value;
	}

	/**
	 * sets the right side value
	 * @param right
	 * 
	 * @requires min(left, right) < value < max(left, right)
	 * @modifies right
	 * @effects right = right
	 */
	public void setRight(int right){
		this.right=right;
		if(value < Math.min(left, right)){
			setValue(Math.min(left, right));
		}else if(value > Math.max(left, right)){
			setValue(Math.max(left, right));
		}
		repaint();
	}
	/**
	 * sets the value representing the left side of slider
	 * @param left
	 * 
	 * @requires min(left, right) < value < max(left, right)
	 * @modifies left
	 * @effects sets left=left
	 */
	public void setLeft(int left){
	  this.left = left;   // Somebody is setting left to 0, I think.
	  System.out.println("jhm: setLeft called with " + Integer.toString(left));
	  if(value < Math.min(left, right)){
	    setValue(Math.min(left, right));
	  }else if(value > Math.max(left, right)){
	    setValue(Math.max(left, right));
	  }
	  repaint();
	}



	/**
	 * sets value
	 * @param val
	 * 
	 * @requires  min(left, right) < value < max(left, right)
	 * @modifies value
	 * @effects sets value to val;
	 */
	public void setValue(int val){
      int oldvalue = value;
	  if(val < Math.min(left, right)){
	    value = Math.min(left, right);
	  }else if(val > Math.max(left, right)){
	    value = Math.max(left, right);
	  }else{
	    value = val;
	  }
	  repaint();
	  firePropertyChange(VALUE_CHANGED, oldvalue, value);
	}

	/**
	 * creates the shape of the track on the left side of the thumb
	 * @param blueprint
	 * @return general path shape of track on the left side of the thumb
	 */
	
	public Shape reformLeadingTrack(SliderBlueprint blueprint){
		GeneralPath shape = new GeneralPath();
		shape.moveTo(blueprint.closeTrackEdgeLeft, blueprint.trackTop);
		shape.lineTo(blueprint.thumbCenter, blueprint.trackTop);
		shape.lineTo(blueprint.thumbCenter, blueprint.trackBottom);
		shape.lineTo(blueprint.closeTrackEdgeLeft, blueprint.trackBottom);
		shape.curveTo(blueprint.farTrackEdgeLeft, blueprint.trackBottom,
				blueprint.farTrackEdgeLeft, blueprint.trackTop,
				blueprint.closeTrackEdgeLeft, blueprint.trackTop);
		shape.closePath();
		return shape;
	}
	
	/**
	 * creates the shape of the track on the right side of the thumb
	 * @param blueprint
	 * @return general path shape of the track on the right side of the thumb
	 */
	public Shape reformTrailingTrack(SliderBlueprint blueprint){
		GeneralPath shape = new GeneralPath();
		shape.moveTo(blueprint.thumbCenter, blueprint.trackTop);
		shape.lineTo(blueprint.closeTrackEdgeRight, blueprint.trackTop);
		shape.curveTo(blueprint.farTrackEdgeRight, blueprint.trackTop,
				blueprint.farTrackEdgeRight, blueprint.trackBottom,
				blueprint.closeTrackEdgeRight, blueprint.trackBottom);
		shape.lineTo(blueprint.thumbCenter, blueprint.trackBottom);
		shape.lineTo(blueprint.thumbCenter, blueprint.trackTop);
		shape.closePath();
		return shape;
	}
	
	/**
	 * creates the shape of the thumb
	 * @param blueprint
	 * @param diameter
	 * @return ellipse of the thumb
	 */
	public Shape reformThumb(SliderBlueprint blueprint, int diameter){
		return new Ellipse2D.Double(blueprint.thumbCenter-diameter / 2,
				blueprint.trackMiddleY-diameter / 2, diameter, diameter);
	}

	/**
	 * creates the shape of the ticks
	 * @param blueprint
	 * @return general path of the ticks
	 */
	public Shape reformTicks(SliderBlueprint blueprint){
		GeneralPath ticks = new GeneralPath();
		int count = 0;
		float position = blueprint.closeTrackEdgeLeft;
		float interval = (float) (blueprint.closeTrackEdgeRight-blueprint.closeTrackEdgeLeft)/tickNumber;
		while (count  <  (tickNumber + 1)) {
			ticks.moveTo((int) position, blueprint.trackTop);
			ticks.lineTo((int) position, blueprint.trackBottom);
			position += interval;
			count += 1;
		}
		ticks.closePath();
		return ticks;
	}

	/**
	 * Sets the values in the class SliderBlueprint
	 * @param blueprint
	 * @param width
	 * @param height
	 * @param girth
	 * @param thumbX - distance from the upper left corner to the center of the thumb
	 */
	public void reformBlueprint(SliderBlueprint blueprint,
			int width, int height, int girth, int thumbX){
		blueprint.farTrackEdgeLeft=height / 2-girth / 2;
		blueprint.closeTrackEdgeLeft=height / 2;
		blueprint.thumbCenter=thumbX;
		blueprint.closeTrackEdgeRight=width-height / 2;
		blueprint.farTrackEdgeRight=width-height / 2 + girth / 2;
		blueprint.trackTop=height / 2-girth / 2;
		blueprint.trackMiddleY=height / 2;
		blueprint.trackBottom=height / 2 + girth / 2;
	}
	
	/**
	 * Paints the CSlider
	 */
	
	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		g2.addRenderingHints(renderingHints);
		
		// gets the distance from the left side of the slider to the thumb in pixel
		int thumbX = getThumbX();

		reformBlueprint(blueprint,
				getWidth(),
				getHeight(), (int)(trackThickness*getHeight()), thumbX);
		Shape leading = reformLeadingTrack(blueprint);
		Shape trailing = reformTrailingTrack(blueprint);
		Shape thumb = reformThumb(blueprint, getHeight() / 2);
		Shape miniThumb = reformThumb(blueprint, getHeight()/4);


		//draw shapes
		g2.setPaint(new GradientPaint(
				0,blueprint.trackTop,leadingColor,
				0,blueprint.trackBottom, Color.white, false));
		g2.fill(leading);
		g2.setPaint(new GradientPaint(
				0,blueprint.trackTop,trailingColor,
				0,blueprint.trackBottom, Color.white, false));
		g2.fill(trailing);

		g2.setColor(new Color(250,250,250,100));
		g2.draw(leading);
		g2.draw(trailing);
		
		// draws the tick marks if ticks are turned on
		// also draws where the triangle where thumb starts
		g2.setColor(Color.lightGray);
		if (tickNumber != 0 && setTicks) {
			Shape ticks = reformTicks(blueprint);
			g2.draw(ticks);
		}
		
		// draws the start label if start mark is turned on
		// depending on what the start mark was set as when initialized
		if (startMark) {
			g2.setColor(Color.darkGray);
			Font font = new Font("Dialog", Font.PLAIN, 8);
			FontMetrics metrics = getFontMetrics(font);
			// gets the width of the text so that the words can be centered later on
			int textWidth = metrics.stringWidth(startMarkLabel);
			g2.setFont(font);
			g2.drawString(startMarkLabel, (convertToPixels(thumbStart) - 
			                              (textWidth / 2)),(int) (blueprint.trackBottom*1.7));
		}

		//g2.setPaint(new GradientPaint(0,0,Color.white,0,th,thumbColor, false));
		g2.setColor(Color.lightGray);
		g2.fill(thumb);

		//g2.setPaint(new GradientPaint(0,0,Color.darkGray,0,th,thumbColor, false));
		g2.setColor(Color.darkGray);
		g2.fill(miniThumb);

		g2.setColor(thumbColor);
		g2.draw(thumb);
	}
	
	/**
	 * returns the distance from the left of slider to the center of the thumb (converts to pixels)
	 * @return the distance from the left of slider to the center of the thumb
	 */
	
	private int getThumbX(){
	  if (left == 0) {
	    System.out.println("jhm: left === 0!");
	  }
	  int i = right - left;
	  return blueprint.closeTrackEdgeLeft + 
	  (value-left)*(blueprint.closeTrackEdgeRight-blueprint.closeTrackEdgeLeft)/(i == 0 ? 1 : i);
	}
	
	/**
	 * Given an x value in pixels (offset from this component's left side) returns
	 * the associated value on the slider.
	 * @param value the X coordinate 
	 * @return the slider value associated with the given X coordinate
	 */
	private int convertToAbstract(int value){
		float i = blueprint.closeTrackEdgeRight - blueprint.closeTrackEdgeLeft;
		return left + Math.round((value-blueprint.closeTrackEdgeLeft)*(right-left)/(i == 0 ? 1 : i));
	}
	
	/**
	 * Given an x value in slider value returns
	 * the associated pixels (offset from this component's left side)
	 * @param value the slider value
	 * @return x coordinates associated with the slider value
	 */
	private int convertToPixels(int value){
		float i = right-left;
		return blueprint.closeTrackEdgeLeft + 
		Math.round((value-left)*(blueprint.closeTrackEdgeRight-blueprint.closeTrackEdgeLeft)/(i == 0 ? 1 : i));
	}
	
	public void mousePressed(MouseEvent e) {
		int thumbX = getThumbX();
		// offset is if the mouse clicked on the thumb
		if(Math.abs(e.getX()-thumbX) < getHeight() / 2){
			offset = e.getX()-thumbX;
		}else{
			offset = 0;
			setValue(convertToAbstract(e.getX()));
		}
		repaint();
	}
	public void mouseDragged(MouseEvent e){
		setValue(convertToAbstract(e.getX()-offset));
		repaint();
	}

	public void mouseReleased(MouseEvent e) { 
		int xPos = convertToAbstract(e.getX()-offset);
		// checks if the sticky ticks are on
		if (setTicks){
			stickTicks(xPos);
		} else {
			setValue(xPos);
		}
		offset = 0;
		repaint();
	}

	public void mouseMoved(MouseEvent e){}
	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}

	/**
	 * creates the sticky position of the intervals/ticks in the slider
	 * @param mouseReleaseXPos
	 */
	public void stickTicks(int mouseReleaseXPos) {
		int xPos = mouseReleaseXPos;
		int total = right - left;
		int interval = total/tickNumber;
		int remain = xPos%interval;
		//if the thumb goes over the edge, sets to farthest position
		if (xPos >= right) {
			xPos = right;
		} else if (xPos <= left) {
			xPos = left;
		// if not at the intervals, set position to the intervals
		} else if (remain != 0) {
			// move to smaller numbered position
			if (remain <= (interval / 2)) {
				//check if goes past beginning position, if true then set to beginning position
				if ((Math.abs(thumbStart - xPos) < (interval - remain)) || 
						((xPos < thumbStart) && ((xPos - remain) > thumbStart) &&
								((xPos + interval) > thumbStart))){
					xPos = thumbStart;
				} else {
					xPos = xPos - remain;
				}
			// move to larger numbered position
			} else {
				//check if goes past beginning position, if true then set to beginning position
				if ((Math.abs(thumbStart - xPos) < (interval - remain)) || 
						((xPos < thumbStart) && ((xPos + (interval - remain)) > thumbStart) &&
								((xPos + interval) > thumbStart))) {
					xPos = thumbStart;
				} else {
					xPos = xPos + (interval-remain);
				}
			}
		}	else if (remain == 0) {
			xPos = mouseReleaseXPos;
		}else {
			xPos = thumbStart;
		}
		setValue(xPos);
	}


	/** debugging */
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new GridLayout(0,1));
		f.setSize(400, 200);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		final CSlider c1 = new CSlider(0,200,105, true, 10);
		panel.add(c1,BorderLayout.CENTER);
		panel.add(new JLabel("label:"), BorderLayout.WEST);
		CSlider c2 = new CSlider(0,200,100);
		CSlider c3 = new CSlider(-5,5,0);
		CSlider c4 = new CSlider(2,0,1);
		CSlider c5 = new CSlider(0,2,1);
		CSlider c6 = new CSlider(0,0,0);
		CSlider c7 = new CSlider(0,200,100);
		f.add(panel);
		f.add(c2);
		f.add(c3);
		f.add(c4);
		f.add(c5);
		f.add(c6);
		f.add(c7);

		JButton b = new JButton();
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println(c1.getValue());
				c1.setRight(175);
				//c.setValue(125);
				c1.revalidate();
				c1.repaint();
				System.out.println(c1.getValue());
			}
		});
		f.add(b);
		f.setVisible(true);
		f.repaint();
	}
}

/**
 * Contains the location for the CSlider components
 * (in pixels)
 */
class SliderBlueprint{
	int farTrackEdgeLeft=0;
	int closeTrackEdgeLeft=0;
	int thumbCenter=0;
	int closeTrackEdgeRight=0;
	int farTrackEdgeRight=0;

	int trackTop=0;
	int trackMiddleY=0;
	int trackBottom=0;

}
