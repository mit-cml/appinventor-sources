// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblocks;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import openblocks.workspace.Workspace;
import openblocks.codeblockutil.GraphicsManager;

/**
 * This class can be used to add dragging capability to any JComponents.
 * It contains the methods and data members needed to support automatic dragging,
 * and contains methods to impliment both MouseListener, MouseMotionListener.
 * In general, any existing JComponent can be made to be draggable simple by
 * creating an instance of JComponentDragHandler (passing a reference to itself)
 * and registering the JComponentDragHandler as the listener for all mouse events.
 *
 * Classes that need similar, but not identical, behavior, or that need to add
 * functionality to the mouse methods here can create an inner class that extends
 * this class.  In this way the inner class can maintain the functionality of
 * JComponentDragHandler while also having access to data members and methods
 * of its enclosing class for the purposes of extension.
 *
 *
 */
public class JComponentDragHandler implements MouseListener, MouseMotionListener {

    /**
     * These data members save the point at which the mouse was pressed
     * relative to the (0,0) corner of the JComponent.
     */
	public int mPressedX; //at mouse pressed
	public int mPressedY; //at mouse pressed

	public int mCurrentX; //where the mouse is currently
	public int mCurrentY; //where the mouse is currently

	public int dragDX; // amount of last drag in X direction
	public int dragDY; // amount of last drag in Y direction

	public int oldLocX; //where the component was before dragging
	public int oldLocY;

    private static Cursor openHandCursor = null;
    private static Cursor closedHandCursor = null;

    /**
     * Stores location data (typically of this JComponent)
     * as a Point for easy manipulation and to avoid re-creating a new object every time
     * these manipulations are done.
     */
	public Point myLoc = new Point();

	private JComponent myComponent;

	/**
	 * Creates a new instance of a JComponentDragHandler with a pointer to the
	 * given JComponent.  Remember to register this JComponentDragHandler as the
	 * listener for mouse events in the JComponent in order for this class to
	 * be allowed to handle those events.
	 * @param jc the JComponent whose mouse events will be handled by this JComponentDragHandler
	 */
    public JComponentDragHandler(JComponent jc) {
    	// this is the JComponent whose mouse events will be handled in this class
    	    myComponent = jc;
        if(openHandCursor == null || closedHandCursor == null)
            initHandCursors();
    }

    private static void initHandCursors(){
        openHandCursor = createHandCursor("open_hand.png", "openHandCursor");
        closedHandCursor = createHandCursor("closed_hand.png", "closedHandCursor");
    }

    private static Cursor createHandCursor(String location, String cursorName){
        java.net.URL handURL = JComponentDragHandler.class.getResource(location);
        assert handURL != null : "Can not find hand cursor image "+cursorName;
        ImageIcon handicon = new ImageIcon(handURL);

        Dimension cursize = Toolkit.getDefaultToolkit().getBestCursorSize(handicon.getIconWidth(), handicon.getIconHeight());
        BufferedImage buffImg = GraphicsManager.gc.createCompatibleImage(
                cursize.width,
                cursize.height,
                Transparency.TRANSLUCENT);
        Graphics2D buffImgG2 = (Graphics2D)buffImg.getGraphics();
        Point cpoint = new Point(cursize.width/2-handicon.getIconWidth()/2, cursize.height/2-handicon.getIconHeight()/2);
        buffImgG2.drawImage(handicon.getImage(), cpoint.x, cpoint.y, null);

        return Toolkit.getDefaultToolkit().createCustomCursor(buffImg, new Point(cpoint.x+5, cpoint.y), cursorName);

    }

    /**
     * Returns the Cursor instance that is used when a mouse is over a draggable object
     * @return the Cursor instance that is used when a mouse is over a draggable object
     */
    public Cursor getDragHintCursor(){
        return openHandCursor;
    }

    /**
     * Returns the Cursor instance that is used on mouse drags
     * @return the Cursor instance that is used on mouse drags
     */
    public Cursor getDraggingCursor(){
        return closedHandCursor;
    }

    /**
     * @return the Point where the mouse is, in the JComponent's coordinate frame
     */
    public Point getMousePoint() {
    	return new Point(mCurrentX, mCurrentY);
    }


    ///////////////////
    //MOUSE EVENTS
    ///////////////////

    /**
     * Called when the mouse is pressed over the JComponent.
     * Saves the point (which is measured relative to the JComponent's corner)
     * over which the mouse was pressed.
     */
    public void mousePressed(MouseEvent e) {
        myComponent.setCursor(closedHandCursor);
        mPressedX = e.getX();
        mPressedY = e.getY();
        oldLocX = myComponent.getX();
        oldLocY = myComponent.getY();
    }


    /**
     * This method is called when the mouse is dragged over the JComponent.
     * Moves the JComponent by the amount of the drag such that the point
     * under which the mouse the pressed remains under the mouse cursor.  In
     * other words, "drags" the JComponent.
     */
    public void mouseDragged(MouseEvent e) {
//        System.out.println("mouse dragged: " + myComponent.getLocation());
        myComponent.setCursor(closedHandCursor);
    	mCurrentX = e.getX();
        mCurrentY = e.getY();
        int dx = mCurrentX - mPressedX;
        int dy = mCurrentY - mPressedY;
        int curX = myComponent.getX();
        int curY = myComponent.getY();

        // shift new location by amount of drag
        int newX = dx + curX;
        int newY = dy + curY;

        /*
         * Prevent dragging outside of the canvas (keep the mouse-down point inside the canvas)
         */
        Workspace.getInstance().scrollToShowRectangle(new Rectangle(curX + mCurrentX,
                                                                    curY + mCurrentY, 1, 1));
        Point p = SwingUtilities.convertPoint(myComponent, newX + mPressedX, newY + mPressedY,
                                              Workspace.getInstance());
        if (Workspace.getInstance().getWidgetAt(p) == null && !Workspace.getInstance().contains(p)) {
        	// how is this not working?  if it's in the window, shouldn't it be dragging?
        	// I guess the drawer cards aren't widgets, so it's getting confused...
        	//...should add them as widgets but pass calls to the drawer.

        	//return; TODO djwendel - is the above way the best to do it?  Figure it out then do it.
        }
        /*        if (newY + mPressedY < 0) {
        	newY = 1-mPressedY;
        }
        if (newX + mPressedX < 0) {
        	newX = 1-mPressedX;
        }
        if (newY + mPressedY >= myComponent.getParent().getHeight()) {
        	newY = myComponent.getParent().getHeight()-mPressedY-1;
        }
        if (newX + mPressedX >= myComponent.getParent().getWidth()) {
        	newX = myComponent.getParent().getWidth()-mPressedX-1;
        }
*/
        // save how much this drag amount is
        dragDX = newX - myComponent.getX();
        dragDY = newY - myComponent.getY();

        // move to the new location
        myComponent.setLocation(newX, newY);
    }

    /**
     * update the current location of the mouse
     */
    public void mouseMoved(MouseEvent e) {
    	mCurrentX = e.getX();
        mCurrentY = e.getY();
    }

    /*
     *  The following methods can be extended by children of this
     *  class, and are provided here to fill out the implementations
     *  of MouseListener and MouseMotionListener.
     */

    public void mouseReleased(MouseEvent e) {
        myComponent.setCursor(openHandCursor);
    }

    public void mouseClicked(MouseEvent arg0) {
        myComponent.setCursor(openHandCursor);
    }

    public void mouseEntered(MouseEvent arg0) {
        myComponent.setCursor(openHandCursor);
    }

    public void mouseExited(MouseEvent arg0) {
        myComponent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
