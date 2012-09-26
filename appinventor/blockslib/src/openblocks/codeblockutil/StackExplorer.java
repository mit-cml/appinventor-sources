// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.Timer;

/**
 * See documentation for Explorer.
 * 
 * A StackExplorer is an Explorer that stacks
 * its canvases from top down.  It controls them by moving
 * them up and down depending on what canvas is in view.
 */
public class StackExplorer extends JLayeredPane implements Explorer, ActionListener{
	private static final long serialVersionUID = 328149080301L;
	/** the total VISIBLE hight of each tab */
	private static final int buttonHeight = 20;
	/** The set of drawers that wraps each canvas */
	private List<StackCard> drawers;
	/** Timer to help regulate the sliding of drawers */
	Timer timer;
	/** The number of times to slide */
    private int count = 5;
	/**
	 * Constructs new stack explorer
	 */
	public StackExplorer(){
		super();
		this.setLayout(null);
		this.setOpaque(false);
		this.drawers = new ArrayList<StackCard>();
		this.timer = new Timer(35, this);
	}
	
	public boolean anyCanvasSelected(){
    	return false;
    }
	
	public int getSelectedCanvasWidth(){
    	return this.getJComponent().getWidth();
    }
	
	public void addListener(ExplorerListener gel){}
	
	public void removeListener(ExplorerListener gel){}
	
	/**
	 * Reassigns this explorer to the new list of canvases and
	 * reforms the location of each canvas's origin and destination
	 * based on the current size of this explorer.
	 * @param items
         * @requires the Collection is backed by a type that gives a consistent iteration order
	 */
	public void setDrawersCard(Collection<? extends Canvas> items){
		drawers.clear();
		this.removeAll();
		int h = this.getHeight();//total height of pane
		int n = items.size();//number of drawers
		
                int i = 0;
                for(Canvas item : items){
	        Rectangle origin = new Rectangle(0,i*buttonHeight,this.getWidth(), this.getHeight()-n*buttonHeight);
	        Rectangle destination = new Rectangle(0,i <=0 ? 0 : h-buttonHeight*(n-i+1),this.getWidth(), this.getHeight()-n*buttonHeight);
	        
			StackCard drawer = new StackCard(item, this);
			this.add(drawer.getJComponent(), JLayeredPane.DEFAULT_LAYER);
	        this.setLayer(drawer.getJComponent(), JLayeredPane.DEFAULT_LAYER, 0);
	        drawer.setBounds(origin);
	        drawer.reformDrawer( origin, destination);
	        if (!drawers.add(drawer)) throw new RuntimeException("Counldn't add drawer");
                i++;
                }
		this.revalidate();
		this.repaint();
		
	}
    public void addDrawersCard(Canvas item, int index) {
      // not implemented!
      throw new RuntimeException("method not implemented");
    }
	public void selectCanvas(int index){
		if(index >=0 && index < drawers.size()){
			notifySelection(drawers.get(index), !drawers.get(index).isDirectedToDestination());
		}
	}
	/**
	 * Slides all canvases under the selected canvas down
	 * and all canvases above up.
	 */
	public void notifySelection(StackCard selectedDrawer, boolean directedToDestination){
		if(!drawers.contains(selectedDrawer)){
			throw new RuntimeException("Cannot select a drawer not contained in this Drawer Pane");
		}
		int index = drawers.indexOf(selectedDrawer);
		if(directedToDestination){
			for(int i = index+1; i<drawers.size(); i++){
				drawers.get(i).goToDestination();
			}
		}else{
			for(int i = 0; i<index+1; i++){
				drawers.get(i).goToOrigin();
			}
		}
		timer.start();
	}
	/**
	 * reforms the location of each canvas's origin and destination
	 * based on the current size of this explorer.
	 */
	public void reformView(){
		int h = this.getHeight();//total height of pane
		int n = drawers.size()-1;//number of drawers
		for(int i = 0; i<=n; i++){
			StackCard drawer = drawers.get(i);
	        Rectangle origin = new Rectangle(0,i*buttonHeight,this.getWidth(), this.getHeight()-n*buttonHeight);
	        Rectangle destination = new Rectangle(0,i <=0 ? 0 : h-buttonHeight*(n-i+1),this.getWidth(), this.getHeight()-n*buttonHeight);
			if(drawer.isDirectedToDestination()){
				drawer.setBounds(destination);
			}else{
				drawer.setBounds(origin);
			}
	        drawer.reformDrawer( origin, destination);
		}
	}
    /**
     * @return a JCOmponent representation of this explorer
     */
    public JComponent getJComponent(){
    	return this;
    }
    /**
     * Slides all the drawers
     */
    public void actionPerformed(ActionEvent e){
    	if(count < 0){
    		//System.out.println("Ending Stack Sliding: "+count);
    		timer.stop();
    		count = 5;
    	}else{
	    	//System.out.println("Animating Stack Sliding: "+count);
	    	for (StackCard d : this.drawers){
	    		d.animate();
	    	}
    		count--;
    	}
    }
}
