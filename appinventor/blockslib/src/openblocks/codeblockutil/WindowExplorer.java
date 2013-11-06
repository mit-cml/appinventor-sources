// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import openblocks.codeblockutil.CScrollPane.ScrollPolicy;



/**
 * A WindowExplorer is an Explorer that explores its set of canvases by displaying a panel
 * of tabs on the top or bottom .  In the center is the current active canvas.  When a user
 * presses a tab, the corresponding canvas changes to reflect the new active canvas.
 *
 *
 */
public class WindowExplorer extends JPanel implements Explorer{
	private static final long serialVersionUID = 328149080308L;
	private static final int buttonHeight = 13;
	/** The set of drawers that wraps each canvas */
	private List<JComponent> canvases;
	/** The canvas portion */
	private JPanel canvasPane;
	/** The tab portion */
	private JPanel buttonPane;
	/**
	 * Constructs new stack explorer
	 */
	public WindowExplorer(){
		super();
		this.setLayout(new BorderLayout());
		this.canvases = new ArrayList<JComponent>();
		this.canvasPane = new JPanel(new BorderLayout());
		this.buttonPane = new JPanel();
		buttonPane.setBorder(BorderFactory.createMatteBorder(3,0,0,0, new Color(0,150,240)));
		buttonPane.setBackground(Color.black);
		this.add(canvasPane, BorderLayout.CENTER);
		buttonPane.setLayout(new GridLayout(0,2,10,5));
		this.add(buttonPane, BorderLayout.SOUTH);
	}

	public boolean anyCanvasSelected(){
            	return false;
        }

	public int getSelectedCanvasWidth(){
                return this.canvasPane.getWidth();
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
		int size = items.size();
		if(size%2 == 1) size++;
		size = buttonHeight*size;
		buttonPane.setPreferredSize(new Dimension(6,size));
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
	        canvases.add(scroll);
	        button.addActionListener(new ActionListener(){
	        	public void actionPerformed(ActionEvent e){
	        		selectCanvas(index);
	        	}
	        });
	        buttonPane.add(button);
                i++;
		}
		if(!canvases.isEmpty())
    		canvasPane.add(canvases.get(0));
		this.revalidate();

	}
    public void addDrawersCard(Canvas item, int index) {
      // not implemented!
      throw new RuntimeException("method not implemented");
    }
	public void selectCanvas(int index){
		if(index >=0 && index < canvases.size()){
			JComponent scroll = canvases.get(index);
			canvasPane.removeAll();
			canvasPane.add(scroll);
			canvasPane.revalidate();
			canvasPane.repaint();
		}
	}
	/**
	 * Reforms this explorer based on the new size or location of this explorer.
	 * For some explorers whose implementation does not depend on the size of itself,
	 * this method may trigger no action.
	 */
	public void reformView(){}
    /**
     * @return a JCOmponent representation of this explorer
     */
    public JComponent getJComponent(){
    	return this;
    }
}
