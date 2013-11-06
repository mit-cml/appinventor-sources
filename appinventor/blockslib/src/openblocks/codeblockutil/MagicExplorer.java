// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import openblocks.codeblockutil.CScrollPane.ScrollPolicy;


/**
 * See documentation for Explorer.
 * 
 * A MagicExplorer explores the canvas by normally
 * displaying only the buttons.  When the user selects a
 * particular button, the corresponding canvas is rolled out
 * from underneath in a glass like scroll pane.  We say glass
 * because there exists a special transparency to the background
 * color.
 */
public class MagicExplorer extends JLayeredPane implements Explorer, ComponentListener, FocusListener{
	private static final long serialVersionUID = 328149080294L;
	/** The set of drawers that wraps each canvas */
	private List<JComponent> canvases;
	/** The panel containing the buttons */
	private JPanel buttonPane;
	/** The panel containing the canvas */
	private JPanel canvasPane;
	/** A timer responsible for rolling out the canvas */
	private EnlargerTimer timer;
    /**
     * Constructor
     */
    public MagicExplorer() {
        super();
        this.canvases = new ArrayList<JComponent>();
        this.setLayout(null);
        this.setOpaque(true);
        this.setBackground(Color.black);
        this.timer = new EnlargerTimer();
        
        buttonPane = new JPanel();
        buttonPane.setBackground(Color.black);
        buttonPane.setLayout(new GridLayout(0,1));
        add(buttonPane, JLayeredPane.PALETTE_LAYER);
        setLayer(buttonPane, JLayeredPane.PALETTE_LAYER, 0);
        
        this.canvasPane = new JPanel(new BorderLayout());
        canvasPane.setBackground(Color.black);
        add(canvasPane, JLayeredPane.PALETTE_LAYER);
        setLayer(canvasPane, JLayeredPane.PALETTE_LAYER, 0);
        
        this.addComponentListener(this);
        this.addFocusListener(this);
    }
    
    public boolean anyCanvasSelected(){
    	return false;
    }
    
    public int getSelectedCanvasWidth(){
    	return canvasPane.getWidth();
    }
    
    public void addListener(ExplorerListener gel){}
    
    public void removeListener(ExplorerListener gel){}
    
	/**
	 * Reassigns the set of canvases that this explorer controls.
	 * Though the collection of canvas may be empty, it may not be null.
	 * @param items
	 * 
	 * @requires items != null &&
	 * 			 for each element in item, element!= null &&
         *           the Collection is backed by a type that gives a consistent iteration order
	 */
	public void setDrawersCard(Collection<? extends Canvas> items){
		canvases.clear();
		buttonPane.removeAll();
                int i = 0;
                for(Canvas item : items){
			final int index  = i;
	        //final CButton button = new CButton(item.getColor(), item.getColor().brighter().brighter().brighter(),item.getName());
	        CButton button = new CBorderlessButton(item.getName());
	        JComponent scroll = new CHoverScrollPane(
    				item.getJComponent(),
    				ScrollPolicy.VERTICAL_BAR_AS_NEEDED,
    				ScrollPolicy.HORIZONTAL_BAR_AS_NEEDED,
    				18, item.getColor(), Color.darkGray);
	        button.addActionListener(new ActionListener(){
	        	public void actionPerformed(ActionEvent e){
	        		selectCanvas(index);
	        	}
	        });
	        canvases.add(scroll);
	        buttonPane.add(button);
                i++;
                }
	}
    public void addDrawersCard(Canvas item, int index) {
      // not implemented!
      throw new RuntimeException("method not implemented");
    }
	/**
	 * Selects the particular canvas at the specified index.
	 * THe canvas is placed onto the it's glass pane and the placed onto
	 * the canvasPane.  A timer is used to roll out the canvasPane.
	 * @param index
	 */
	public void selectCanvas(int index){
		if(index >=0 && index < canvases.size()){
			JComponent scroll = canvases.get(index);
			canvasPane.removeAll();
			canvasPane.add(scroll);
			timer.expand();
			MagicExplorer.this.requestFocus();
		}
	}
	/**
	 * Reforms this explorer based on the new size or location of this explorer.
	 * For some explorers whose implementation does not depend on the size of itself,
	 * this method may trigger no action.
	 */
	public void reformView(){
    	buttonPane.setSize(this.getWidth(), this.getHeight());
    	timer.setWidth(this.getWidth());
    	timer.shrink();
	}
    /**
     * @return a JComponent representation of this explorer
     */
    public JComponent getJComponent(){
    	return this;
    }
    /**
     * Rolls the canvasPane back underneath when focus is lost
     */
    public void focusLost(FocusEvent e){
    	timer.shrink();
    }
    public void focusGained(FocusEvent e){}
    public void componentResized(ComponentEvent e) {
    	reformView();
    }
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    /**
     * A timer repsonible for rolling out the canvaspane.
     */
    private class EnlargerTimer implements ActionListener{
    	int width;
    	private javax.swing.Timer timer;
    	private boolean expand;
    	public EnlargerTimer(){
    		this.expand = true;
        	timer = new Timer(50, this);
    	}
    	/**
    	 * Respinsible for expanding or shriking the canvaspane untill
    	 * it has reached the appropriate size.
    	 */
    	public void actionPerformed(ActionEvent e){
    		if(expand){
        		if(canvasPane.getWidth()<width-25){
        			canvasPane.setBounds(0,0,canvasPane.getWidth()+25, buttonPane.getHeight());
        			canvasPane.revalidate();
        			canvasPane.repaint();
        		}else{
        			timer.stop();
        			canvasPane.setBounds(0,0,buttonPane.getWidth(), buttonPane.getHeight());
        			canvasPane.revalidate();
        			canvasPane.repaint();
        		}
    		}else{
        		if(canvasPane.getWidth()>25){
        			canvasPane.setBounds(0,0,canvasPane.getWidth()-25, buttonPane.getHeight());
        			canvasPane.revalidate();
        			canvasPane.repaint();
        		}else{
        			timer.stop();
        			canvasPane.setBounds(0,0,0, buttonPane.getHeight());
        			canvasPane.revalidate();
        			canvasPane.repaint();
        		}
    		}
    	}
    	/**
    	 * Expands the canvaspane
    	 */
    	public void expand(){
    		this.expand = true;
    		this.timer.start();
    	}
    	/**
    	 *Shrinks the canvaspane
    	 */
    	public void shrink(){
    		this.expand=false;
    		this.timer.start();
    	}
    	public void setWidth(int width){
    		this.width = width;
    	}
    }
}
