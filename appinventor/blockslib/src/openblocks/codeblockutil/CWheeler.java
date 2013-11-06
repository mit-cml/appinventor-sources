// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import openblocks.codeblockutil.CScrollPane.ScrollPolicy;

/**
 * displays item by their preferred size
 *
 */
public class CWheeler extends JPanel{
	static final long serialVersionUID = 82391823L; 
	static final Color background = new Color(50,50,50);
	Collection<? extends JComponent> elements;
	CScrollPane scroll;
	JComponent view;
	public CWheeler(boolean includeScrollbar){
		this(new ArrayList<JComponent>(), includeScrollbar, background);
	}
	public CWheeler(Collection<JComponent> items){
		this(items, true, background);
	}
	public CWheeler(Collection<JComponent> items, boolean includeScrollbar, Color backgroundColor){
		super(new BorderLayout());
		this.setBackground(backgroundColor);
		this.elements=items;
		if(includeScrollbar){
			CArrowButton left=new CArrowButton(CArrowButton.Direction.WEST){
				private static final long serialVersionUID = 328149080242L;
				public void triggerAction(){
					scrollLeft();
				}
			};
			left.addActionListener(left);
			left.setPreferredSize(new Dimension(15,15));
			CArrowButton right=new CArrowButton(CArrowButton.Direction.EAST){
				private static final long serialVersionUID = 328149080243L;
				public void triggerAction(){
					scrollRight();
				}
			};
			right.addActionListener(right);
			right.setPreferredSize(new Dimension(15,15));
			this.view=new JPanel(null);
			this.view.setBackground(backgroundColor);
			this.scroll=new CHoverScrollPane(view,
					ScrollPolicy.VERTICAL_BAR_NEVER,
					ScrollPolicy.HORIZONTAL_BAR_ALWAYS,
					20, CGraphite.darkgreen, new Color(0,0,50));
			
			JPanel leftPane = new JPanel(new BorderLayout());
			leftPane.setBackground(backgroundColor);
			leftPane.add(left, BorderLayout.SOUTH);
			this.add(leftPane, BorderLayout.WEST);
			JPanel rightPane = new JPanel(new BorderLayout());
			rightPane.setBackground(backgroundColor);
			rightPane.add(right, BorderLayout.SOUTH);
			this.add(rightPane, BorderLayout.EAST);
		}else{
			this.view=new JPanel(null);
			this.view.setBackground(backgroundColor);
			this.scroll=new CTracklessScrollPane(view,
					ScrollPolicy.VERTICAL_BAR_NEVER,
					ScrollPolicy.HORIZONTAL_BAR_NEVER,
					20, Color.blue, new Color(0,0,50));
		}
		for(JComponent element : elements){
			view.add(element);
		}
		this.add(scroll, BorderLayout.CENTER);
		reformItems();
	}
	public void scrollLeft(){
		int v = scroll.getHorizontalModel().getValue();
		int accumulatedWidth = 0;
		for(JComponent element : elements){
			accumulatedWidth = accumulatedWidth + element.getWidth();
			if(accumulatedWidth>=v){
				accumulatedWidth = accumulatedWidth - element.getWidth();
				break;
			}
		}
		scroll.getHorizontalModel().setValue(accumulatedWidth);
		scroll.revalidate();
		scroll.repaint();
	}
	public void scrollRight(){
		int v = scroll.getHorizontalModel().getValue();
		int accumulatedWidth = 0;
		for(JComponent element : elements){
			accumulatedWidth = accumulatedWidth + element.getWidth();
			if(accumulatedWidth>v){
				break;
			}
		}
		scroll.getHorizontalModel().setValue(accumulatedWidth);
		scroll.revalidate();
		scroll.repaint();
	}
	
	private void reformItems(){
		int totalWidth = 0;
		int maxHeight = 0;
		for(JComponent element : elements){
			element.setBounds(totalWidth, 0,
					element.getPreferredSize().width, element.getPreferredSize().height);
			totalWidth = totalWidth + element.getPreferredSize().width;
			maxHeight = Math.max(maxHeight, element.getPreferredSize().height);
		}
		view.setPreferredSize(new Dimension(totalWidth, maxHeight));
		view.setBounds(0,0,totalWidth, maxHeight);
		this.revalidate();
		this.repaint();
	}
	
	public void setElements(Collection<? extends JComponent> items){
		this.elements=items;
		view.removeAll();
		for(JComponent element : items){
			view.add(element);
		}
		reformItems();
	}
	
	public void scrollToWheelItem(JComponent item){
		scroll.validate();
		scroll.getHorizontalModel().setValue(item.getX());
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new BorderLayout());
		f.setSize(500, 200);
		List<JComponent> items = new ArrayList<JComponent>();
		for(int i = 0; i<15 ; i++){
			CWheelItem item = new CWheelItem();
			item.setBackground(Color.red);
			items.add(item);
		}
		f.add(new CWheeler(items));
		f.setVisible(true);
		f.repaint();
	}
}