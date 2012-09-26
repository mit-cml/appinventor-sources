// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * The CSliderPane is a swing-compatible widget that
 * allows users to interface with a slider through
 * the slider itself and three text fields.
 * 
 * The widget itself mains the bounded range model of ONE particular
 * abstract data structure, to be refered to as the abstract model.
 * This model is displayed graphically through the slider and text
 * fields.
 * 
 * The model itself has a maximum, a minimum, and a value (to be
 * consistant with all swing components that wrap a bounded range model).
 * 
 * The GUI representation of the abstract model has three text fields
 * that correspond to the value, left side, and right side of the slider.
 * The text field (left or right) with the lowest numerical value maps to the
 * abstract minimum of the abstract bounded range model.  The text field
 * (left or right) with the highest numerical value maps to the abstract
 * maximum.
 * 
 * As with most swing lightweight components, the CSliderPane is itself
 * a controller that manages the display and interface between the
 * abstract model and UI (view).  The CSliderPane's controller is
 * reponsible for three things:
 * 	1. 	keeping the slider, text fields, and abstarct model consistant; that
 * 		is, they should all display and hold the same information.
 *  2.	Converting between the raw units of the slider, text fields, and
 *  	abstract model.
 *  3	notfying observers of the CSliderPane's ChangeEvents that the abstract
 *  	value was changed by the USER.  This is done by publicly throwing a
 *  	ChangeEvent with a property name of CSliderPane.VALUE_CHANGED
 * 
 * @specfield minium : float // the minimum value of this abstract bounded range model
 * @specfield maximum : float // the maximum value of this abstract bounded range model
 * @specfield value : float // the abstract value of this abstract bounded range model
 * 
 */
public class CSliderPane extends JPanel implements ComponentListener{
	private static final long serialVersionUID = 328149080253L;
	/** Property name of the event thrown by this widget */
	public static final String VALUE_CHANGED = "VALUE_CHANGED";
	/** ratio conversion form raw (int) value to real (float) value */
	private static float ratio = 1000;
	/** the margin of this widget */
	private static int margin = 10;
	/** the header height of this widget */
	private static int header = 20;
	/** the slider that graphcally displays thhe abstract model*/
	private CSlider slider;
	/** the value text field that graphically displays the abstract value */
	private JTextField valueLabel;
	/** the left text field that graphically displays either the min or max */
	private JTextField leftLabel;
	/** the right text field that graphically displays either the min or max */
	private JTextField rightLabel;
	
	/**
	 * @param min - default min.  May be mutated by this.setMinimum()
	 * @param max - default max.  May be mutated by this.setMaximum()
	 * @param value - default value.  May be mutated by this.setValue()
	 * 
	 * @requires CSliderPane.ratio != 0.
	 * @effects constructs a new CSliderPane with the left text field
	 * set to "min", the right text field set to "max", and the value text
	 * field set to "value".  The abstract BoundedRangeModel is set to:
	 * [minimum: "min"; maximum: "max"; value: "value"].
	 */
	public CSliderPane(float min, float max, float value){
		super(null);
		this.setOpaque(false);
		
		//constructs internal Swing components of this widget
		slider = new CSlider((int)(min*ratio),(int)(max*ratio),(int)(value*ratio));
		slider.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e) {
				sliderValueChanged(e);
			}
		});
		valueLabel = new CNumberTextField(String.valueOf(value)){
			private static final long serialVersionUID = 328149080254L;
			public void evaluateTextFieldData(){
				try{
					int newvalue = Math.round(Float.parseFloat(this.getText())*ratio);
					slider.setValue(newvalue);
					notifyValueChanged();
				}catch (NumberFormatException exception){}
			}
		};
		valueLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		leftLabel = new CNumberTextField(String.valueOf(min)){
			private static final long serialVersionUID = 328149080256L;
			public void evaluateTextFieldData(){
				try{
					int newvalue = Math.round(Float.parseFloat(this.getText())*ratio);
					slider.setLeft(newvalue);
					notifyValueChanged();
				}catch (NumberFormatException exception){}
			}
		};
		leftLabel.setHorizontalAlignment(SwingConstants.LEADING);
		rightLabel = new CNumberTextField(String.valueOf(max)){
			private static final long serialVersionUID = 328149080255L;
			public void evaluateTextFieldData(){
				try{
					int newvalue = Math.round(Float.parseFloat(this.getText())*ratio);
					slider.setRight(newvalue);
					notifyValueChanged();
				}catch (NumberFormatException exception){}
			}
		};
		rightLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		//add the internal swing components to this widget
		this.add(valueLabel);
		this.add(leftLabel);
		this.add(rightLabel);
		this.add(slider);
		
		//add listeners
		this.addComponentListener(this);
		slider.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				notifyValueChanged();
			}
		});
		slider.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
				notifyValueChanged();
			}
		});
	}
	/**
	 * @return this.minimum
	 */
	public float getMinimum(){
		//get lower value of the slider's left and right fields
		float rawMin = Math.min(slider.getLeft(), slider.getRight());
		//return converted real minimum value
		return rawMin / ratio;
	}
	/**
	 * @return this.maximum
	 */
	public float getMaximum(){
		//get lower value of the slider's left and right fields
		float rawMax = Math.max(slider.getLeft(), slider.getRight());
		//return converted real minimum value
		return rawMax / ratio;
	}
	/**
	 * @return this.value
	 */
	public float getValue(){
		//return converted real minimum value
		return (float)slider.getValue() / ratio;
	}
	/**
	 * @param min
	 * 
	 * @requires none
	 * @modifies this.minimum && this.value && GUI components
	 * @effects 1] Set this.minimum to "min".
	 * 			2] If the value is now out of bounds, set to
	 * 			   either this.minimum or this.maximum, which one is closer.
	 * 			3] Update the GUI components such that the slider
	 * 			   reflects the current bounded range model
	 */
	public void setMinimum(float min){
		if(slider.getLeft()<slider.getRight()){
			slider.setLeft((int)(min*ratio));
		}else{
			slider.setRight((int)(min*ratio));
		}
	}
	/**
	 * @param max
	 * 
	 * @requires none
	 * @modifies this.maximum && this.value && GUI components
	 * @effects 1] Set this.maximum to "max".
	 * 			2] If the value is now out of bounds, set to
	 * 			   either this.minimum or this.maximum, which one is closer.
	 * 			3] Update the GUI components such that the slider
	 * 			   reflects the current bounded range model
	 */
	public void setMaximum(float max){
		if(slider.getLeft()>slider.getRight()){
			slider.setLeft((int)(max*ratio));
		}else{
			slider.setRight((int)(max*ratio));
		}
	}
	/**
	 * @param value
	 * 
	 * @requires none
	 * @modifies this.value && GUI components
	 * @effects 1] Set this.minimum to "min".
	 * 			2] If the value is now out of bounds, set to
	 * 			   either this.minimum or this.maximum, which one is closer.
	 * 			3] Update the GUI components such that the slider
	 * 			   reflects the current bounded range model
	 */
	public void setValue(float value){
		slider.setValue((int)(value*ratio));
		valueLabel.setText(String.valueOf(slider.getValue()/ratio));
	}
	
	
	//////////////////////////////////////////////////
	//THE METHODS BELOW ARE IMPLEMENTATION SPECIFIC	//
	//AND SHOULD NOT BE CHANGE OR OVERRIDEN			//
	//////////////////////////////////////////////////
	
	/**
	 * When the abstract value is changed by the user, the system
	 * backend must be notified of this change by throwing a ChangeEvent
	 * with property name set to CSliderPane.VALUE_CHANGED.
	 * 
	 * This method is called from changes in the labels of the slider pane
	 */
	private void notifyValueChanged(){
		valueLabel.setText(String.valueOf(slider.getValue()/ratio));
		repositionComponent();
		this.firePropertyChange(VALUE_CHANGED, -1, 1);
	}
	
	/**
	 * Fires a PropertyChangeEvent from the specified event e, which should
	 * originate from a PropertyChangeEvent from the slider.  
	 * @param e PropertyChangeEvent originating from a change in the slider instance
	 */
	private void sliderValueChanged(PropertyChangeEvent e) {
		this.firePropertyChange(VALUE_CHANGED, e.getOldValue(), e.getNewValue());
	}
	
	/**
	 * Repositions the components of this widget.  Should be called
	 * whenever clients invoke an internal method that changes the
	 * bounding strings of the text fields or size of this widget
	 */
	private void repositionComponent(){
		valueLabel.setBounds(
				this.getWidth()-margin-valueLabel.getPreferredSize().width-1,
				header,
				valueLabel.getPreferredSize().width+1,
				10);
		slider.setBounds(
				margin,
				header+10,
				this.getWidth()-2*margin,
				20);
		leftLabel.setBounds(
				margin, 
				header+30,
				leftLabel.getPreferredSize().width+1,
				10);
		rightLabel.setBounds(
				this.getWidth()-margin-rightLabel.getPreferredSize().width-1,
				header+30,
				rightLabel.getPreferredSize().width+1,
				10);
		this.revalidate();
	}
	/** Do nothing when this widget is hidden */
	public void componentHidden(ComponentEvent e){}
	/** Do nothing when this widget is moved */
	public void componentMoved(ComponentEvent e){}
	/** Do nothing when this widget is shown */
    public void componentShown(ComponentEvent e){}
	/** Reposition text fields and slider when this widget is resized */
	public void componentResized(ComponentEvent e){
		repositionComponent();
		revalidate();
	}
	
	/**
	 * This private helper class creates the foundation of the bare minimum
	 * expectations of the internal text fields in the CSliderPane widget.
	 * This CNumberTextField should autoselect it's whole text when clicked
	 * on and should reevalutate the numerical value within the text field
	 * whenever the user presses enter, escape, or moves the focus elsewhere.
	 * the text fields should allow users to only enter in numerical values
	 * and should display a white border when mouse-overed.
	 */
	private abstract class CNumberTextField extends JTextField implements KeyListener, FocusListener, MouseListener{
    	private final int[] validKeyCodes = {KeyEvent.VK_BACK_SPACE,
    			KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT,
    			KeyEvent.VK_RIGHT, KeyEvent.VK_END, KeyEvent.VK_HOME};
    	//private final char[] validKeyChar = {'.', '-'};
		//constructs this
		public CNumberTextField(String text){
			super(text);
			this.setOpaque(false);
			this.setFont(new Font("Monospaced", Font.PLAIN, 9));
			this.setForeground(Color.white);
			this.setBorder(null);
			this.addFocusListener(this);
			this.addMouseListener(this);
			this.addKeyListener(this);
		}
		//unique evaluator for every CNumberTextField
		public abstract void evaluateTextFieldData();
		//autoselect text when clicked on
		public void mouseClicked(MouseEvent e){
			this.setSelectionStart(0);
			this.setSelectionEnd(this.getText().length());
		}
		//add white border when mouse enters
		public void mouseEntered(MouseEvent e) {
			this.setBorder(BorderFactory.createLineBorder(Color.white));
		}
		//remove white border when mouse leaves
		public void mouseExited(MouseEvent e) {
			this.setBorder(null);
		}
		public void mousePressed(MouseEvent e) {}
		public void mouseDragged(MouseEvent e){}
		public void mouseReleased(MouseEvent e) {}
		public void mouseMoved(MouseEvent e){}
		//revaluate text widget abstract model and GUI displays
		//whever users moves focus to different component
		public void focusLost(FocusEvent e){
			evaluateTextFieldData();
		}
		public void focusGained(FocusEvent e){}
		//reposition this text field's size and location when user enters in text
		public void keyPressed(KeyEvent e){
			repositionComponent();
		}
		//reposition this text field's size and location when user enters in text
		public void keyReleased(KeyEvent e){
			repositionComponent();
		}
		//reposition this text field's size and location when user enters in text
		public void keyTyped(KeyEvent e){
			repositionComponent();
		}
		//only allow numerically logical text to be entered
		//re-evaluate abstract model and update GUI when user presses enter or escapse
	    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed){
	    	for(int validKeyCode : validKeyCodes){
	    		if(e.getKeyCode() == validKeyCode){
	    			return super.processKeyBinding(ks, e, condition, pressed);
	    		}
	    	}
	    	if(e.getKeyChar() =='.' && !this.getText().contains(".")){
	    		return super.processKeyBinding(ks, e, condition, pressed);
	    	}
	    	if(e.getKeyChar() == '-' && (this.getCaretPosition()==0 || this.getSelectionStart()==0 ) && !this.getText().contains("-")){
	    		return super.processKeyBinding(ks, e, condition, pressed);
	    	}
	    	if(Character.isDigit(e.getKeyChar())){
	    		return super.processKeyBinding(ks, e, condition, pressed);
	    	}else if(e.getKeyCode() == KeyEvent.VK_ENTER){
	    		//evaluateTextFieldData();
	    		slider.requestFocus();
	    		return false;
	    	}else if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
	    		//evaluateTextFieldData();
	    		slider.requestFocus();
	    		return false;
	    	}else{
	    		return false;
	    	}
	    }
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(400, 400);
		f.setLayout(new BorderLayout());
		JPanel p = new JPanel(new BorderLayout());
		p.setBackground(Color.red);
		p.setBounds(0,0,400,50);
		CSliderPane s = new CSliderPane(0f,10f,1f);
		s.setBounds(0,0,200,60);
		p.add(s);
		f.add(p);
		f.setVisible(true);
	}
}


