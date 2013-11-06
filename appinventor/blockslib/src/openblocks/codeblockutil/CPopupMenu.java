// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import openblocks.codeblockutil.CScrollPane.ScrollPolicy;

public class CPopupMenu extends JPopupMenu implements ActionListener{
	private static final long serialVersionUID = 328149080311L;
	private int ITEM_HEIGHT = 14;
	private Color background = CGraphite.white;
	private JComponent scroll;
	private JComponent view;
	private double zoom = 1.0;
	private int items=0;
	//private int MARGIN = 20;
	public CPopupMenu(){
		super();
		this.setLayout(new BorderLayout());
    	this.setBackground(background);
    	this.setOpaque(false);
    	this.removeAll();
    	this.setBorder(BorderFactory.createMatteBorder(2,2,2,2, CGraphite.darkgreen));
    	view = new JPanel(new GridLayout(0,1));
    	view.setBackground(background);
    	scroll = new CTracklessScrollPane(view,
    			ScrollPolicy.VERTICAL_BAR_AS_NEEDED,
    			ScrollPolicy.HORIZONTAL_BAR_NEVER,
    			9,CGraphite.darkgreen, background);
    	this.add(scroll);
	}
	public Insets getInsets(){
		return new Insets(5,5,5,5);
	}
	public void add(CMenuItem item){
		items++;
		item.addActionListener(this);
		view.add(item);
	}

	public void setZoomLevel(double zoom){
		this.zoom = zoom;
		// change the size of the panel and the text
		view.setPreferredSize(new Dimension((int) (100*this.zoom), (int) (items*ITEM_HEIGHT*this.zoom)));
		this.setPopupSize((int)(100*this.zoom), (int)(Math.min(items*ITEM_HEIGHT+10, 100)*this.zoom));
	}
	
	public void actionPerformed(ActionEvent e) {
		if (this.isVisible()) {
			this.setVisible(false);
		}
	}
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new FlowLayout());
		f.setSize(500, 300);
		f.getContentPane().setBackground(Color.red);
		final CButton c = new CGraphiteButton("hi");
		f.add(c);
		f.setVisible(true);
		f.repaint();
		final CPopupMenu menu = new CPopupMenu();
		for(int i = 0; i<17; i++){
			menu.add(new CMenuItem("hi"));
		}
		c.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				menu.show(c, 0, 0);
			}
		});
		c.add(menu);
		System.out.println(menu.getParent());
	}
	
	/*	protected void paintComponent(Graphics g){
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D)g;
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	g.setColor(background);
	g.fillRoundRect(0,0,this.getWidth()-1, this.getHeight()-1, MARGIN, MARGIN);
	g.setColor(CGraphite.milky);
	g.drawRoundRect(0,0,this.getWidth()-1, this.getHeight()-1, MARGIN, MARGIN);

}*/
}