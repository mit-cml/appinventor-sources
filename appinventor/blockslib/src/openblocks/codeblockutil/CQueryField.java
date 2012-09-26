// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CQueryField extends JPanel implements MouseListener, MouseMotionListener{
	private static final long serialVersionUID = 328149080259L;
	private JTextField field;
	private boolean pressed = false;
	private boolean mouseover = false;
	public CQueryField (){
		this(null);
	}
	public CQueryField (String text){
		super(new BorderLayout());
		field = new JTextField(text);
		field.setBorder(null);
		field.setFont(new Font("Ariel", Font.PLAIN, 13));
		
		this.setBounds(0,0,200,20);
		this.setPreferredSize(new Dimension(200,20));
		
		this.setOpaque(false);
		this.add(field, BorderLayout.CENTER);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.revalidate();
		this.repaint();
	}
	public Insets getInsets(){
		int h= this.getHeight();
		return new Insets(h/6,h,h/6,h);
	}
	private Shape getXCross(int w, int h){
		GeneralPath shape = new GeneralPath();
		shape.moveTo(w-h*2/3,h/3);
		shape.lineTo(w-h/3,h*2/3);
		shape.moveTo(w-h/3, h/3);
		shape.lineTo(w-h*2/3, h*2/3);
		return shape;
	}
	private Shape getXBox(int w, int h){
		Ellipse2D.Double box = new Ellipse2D.Double(w-5*h/6, h/6, 2*h/3, 2*h/3);
		//RoundRectangle2D.Double box = new RoundRectangle2D.Double(w-5*h/6, h/6, 2*h/3, 2*h/3, h/3, h/3);
		return box;
	}
	private Shape getMag(int w, int h){
		Ellipse2D.Double e = new Ellipse2D.Double(h/2, h/6, h*1/3, h*1/3) ;
		GeneralPath shape = new GeneralPath();
		shape.moveTo(h/3, h*2/3);
		shape.lineTo(h/2,h/2);
		shape.append(e, false);
		return shape;
	}
	public JTextField getQueryField(){
		return field;
	}
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		int w = this.getWidth();
		int h = this.getHeight();
		g2.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		g2.setColor(Color.white);
		g2.fillRoundRect(0,0,w,h,h,h);
		
		g2.setStroke(new BasicStroke(3));
		g2.setColor(Color.darkGray.brighter());
		g2.draw(this.getMag(w,h));
		
		if(mouseover){
			if(pressed){
				g2.setColor(new Color(170,0,0));
			}else{
				g2.setColor(Color.red);
			}
		}else{
			g2.setColor(Color.pink);
		}
		g2.fill(this.getXBox(w, h));
		
		g2.setColor(Color.white);
		g2.setStroke(new BasicStroke(2));
		g2.draw(this.getXCross(w, h));
		
		super.paint(g);
	}
	public String getText(){
		return field.getText();
	}
	public void setText(String text){
		field.setText(text);
	}
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {
		mouseover = false;
		this.repaint();
	}
	public void mousePressed(MouseEvent e) {
		if (e.getX()> this.getWidth()-this.getHeight()*5/6){
			pressed = true;
			this.repaint();
		}
	}
	public void mouseReleased(MouseEvent e) {
		if (e.getX()> this.getWidth()-this.getHeight()*5/6){
			field.setText("");
			pressed=false;
			this.repaint();
		}
	}
	public void mouseMoved(MouseEvent e){
		if (e.getX()> this.getWidth()-this.getHeight()*5/6){
			if(mouseover == false){
				mouseover = true;
				this.repaint();
			}
		}else{
			if(mouseover == true){
				mouseover = false;
				this.repaint();
			}
		}
	}
	public void mouseDragged(MouseEvent e){}
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new BorderLayout());
		f.setSize(400,100);
		f.add(new CQueryField());
        
		f.setVisible(true);
	}
}
