// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;

public class CProgressBar extends JFrame implements ActionListener{
	private static final long serialVersionUID = 328149080260L;
	JProgressBar bar;
	Timer timer;
	public CProgressBar(String text){
		super(text);
		this.setUndecorated(true);
		this.setAlwaysOnTop(true);
		this.setBounds(300, 400, 400,40);
		this.setLayout(new BorderLayout());
		JPanel pane = new JPanel(new BorderLayout());
		pane.setBorder(BorderFactory.createMatteBorder(3,3,3,3,Color.blue));
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setFont(new Font("Ariel", Font.BOLD, 12));
		pane.add(label, BorderLayout.NORTH);
		bar = new JProgressBar(0,100);
		//bar.setStringPainted(true);
		pane.add(bar, BorderLayout.CENTER);
		this.add(pane, BorderLayout.CENTER);
		timer = new Timer(750, this);
		this.setVisible(true);
		timer.start();
	}
	public void actionPerformed(ActionEvent e){
		if(bar.getValue() > bar.getMaximum()*0.75){
			this.setVisible(false);
			this.dispose();
			timer.stop();
		}else{
			bar.setValue(bar.getValue()*2+1);
		}
	}
	public static void main(String[] arguments){
	    try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        System.out.println(UIManager.getSystemLookAndFeelClassName());
	    } catch(Exception e) {
	        System.out.println("Error setting native LAF: " + e);
	    }
		new CProgressBar("MyBarGraph");
	}
}
