package openblocks.codeblockutil;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

/**
 * A CArrowButton is a swing-compatible widget that
 * allows clients to display a semi-transpanrent arrow
 * in any of the four traditional directions:
 * NORTH, SOUTH, EAST, WEST.
 * 
 * Clients of the CArrowButton may subscribe mouse triggers
 * to particular actions by doing the following:
 * this.addCButtonListener(new CButtonListener());
 */
public abstract class CArrowButton extends CButton implements ActionListener{
	private static final long serialVersionUID = 328149080231L;
	/** Directions */
	public enum Direction{NORTH, SOUTH, EAST, WEST};
	private static final int m = 3;
	private final Direction dir;
	private static final Color highlight = CGraphite.darkgreen;
	private static final Color arrowColor = CGraphite.darkgreen;
	
	/**
	 * @param dir
	 * @effects Constructs a new gray arrow button that
	 * 			brightens up to white when mouse-over
	 */
	public CArrowButton(Direction dir){
		super(Color.black, CGraphite.darkgreen, null);
		this.setOpaque(false);
		this.dir=dir;
		this.addActionListener(this);
	}
	
	private Shape getShape(Direction dir){
		if(dir == Direction.NORTH){
			return new GeneralPath();
		}else if(dir == Direction.SOUTH){
			return new GeneralPath();
		}else if(dir == Direction.EAST){
			GeneralPath arrow = new GeneralPath();
			arrow.moveTo(m, m);
			arrow.lineTo(this.getWidth()-m, this.getHeight()/2);
			arrow.lineTo(m, this.getHeight()-m);
			arrow.lineTo(m, m);
			arrow.closePath();
			return arrow;
		}else {//if(dir == WEST){
			GeneralPath arrow = new GeneralPath();
			arrow.moveTo(this.getWidth()-m, m);
			arrow.lineTo(m, this.getHeight()/2);
			arrow.lineTo(this.getWidth()-m, this.getHeight()-m);
			arrow.lineTo(this.getWidth()-m, m);
			arrow.closePath();
			return arrow;
		}
		/*
		if(dir == Direction.NORTH){
			return new GeneralPath();
		}else if(dir == Direction.SOUTH){
			return new GeneralPath();
		}else if(dir == Direction.EAST){
			GeneralPath shape = new GeneralPath();
			shape.moveTo(m,2*m);
			shape.lineTo(2*m,2*m);
			shape.lineTo(2*m,m);
			shape.lineTo(w-m,h/2);
			shape.lineTo(2*m,h-m);
			shape.lineTo(2*m,h-2*m);
			shape.lineTo(m, h-2*m);
			shape.lineTo(m,2*m);
			shape.closePath();
			return shape;
		}else {//if(dir == WEST){
			GeneralPath shape = new GeneralPath();
			shape.moveTo(m,h/2);
			shape.lineTo(w-2*m,m);
			shape.lineTo(w-2*m,2*m);
			shape.lineTo(w-m,2*m);
			shape.lineTo(w-m,h-2*m);
			shape.lineTo(w-2*m,h-2*m);
			shape.lineTo(w-2*m,h-m);
			shape.lineTo(m, h/2);
			shape.closePath();
			return shape;
		}
		 */
	}
	
	/**
	 * repaints this
	 */
	public void paint(Graphics g){
		//super.paint(g);
		int w = this.getWidth();
		int h = this.getHeight();
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Shape arrow = this.getShape(this.dir);
		if (focus){
			g2.setColor(Color.gray);
			g2.drawRoundRect(0,0,w-1,h-1,2*m,2*m);
		}
		if(pressed){
			//g2.setPaint(new GradientPaint(0, 0, fade, 0, this.getHeight()/2,arrowColor, true));
			g2.setColor(highlight);
			g2.fill(arrow);
			g2.setColor(Color.yellow);
			g2.draw(arrow);
		}else{
			g2.setColor(arrowColor);
			g2.fill(arrow);
			g2.setColor(Color.white);
			g2.draw(arrow);
		}
	}
	
	/**
	 * continue to trigger the action of this arrow as user hold down the arrow
	 */
	public void mousePressed(MouseEvent e) {
		this.pressed=true;
		this.repaint();
		//timer.start();
	}
	/**
	 * stop triggering the action os this arrow as the user holds down the arrow
	 */
	public void mouseReleased(MouseEvent e) {
		this.pressed=false;
		this.repaint();
		//timer.stop();
	}
	/**
	 * this method has no use
	 */
	public void actionPerformed(ActionEvent e){
		triggerAction();
	}
	/**
	 * The action triggered by mouse clicks and pressing and holding arrows
	 */
	abstract public void triggerAction();
}