package openblocks.codeblockutil;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import openblocks.codeblockutil.CScrollPane.ScrollPolicy;


/**
 * A Tab Card is used by glass explorers explorers as a
 * mediator to their canvases.
 * 
 * It wraps a button, a scrollpane, and a invoker.
 * 
 * The button uses information about the current color
 * and highlight of the canvas to dipict itself.  The
 * scrollpane takes the canvas and puts it inside a scroll
 * pane so that users can navigate a very large canvas
 * in small space.  The invoker respnds to button presses
 * and invokes the right method in the explorer.
 * 
 * A tabbed pane does more than just store the scroll, button,
 * and canvas, it also holds information about the menuitem
 * for the tab oane's drop down menu.
 */
public class TabCard implements PropertyChangeListener{
	/** The index of this card's tab location */
	private int index;
	/** the canvas to be shown in the tabbed pane */
	private Canvas canvas;
	/** the parent explorer */
	private TabbedExplorer explorer;
	/** the scroll viewport of the tab pane */
	private JComponent scroll;
	/** the tab */
	private CButton button;
	/** The menu item for its tab */
	private JMenuItem menuItem;
	/**
	 * Constructor
	 * @param i
	 * @param canvas
	 * @param ex
	 * @param scrollable
	 */
	TabCard(int i, Canvas canvas, TabbedExplorer ex, boolean scrollable){
		this.index = i;
		this.canvas = canvas;
		this.explorer = ex;
		if(scrollable){
		this.scroll = new CHoverScrollPane(
				canvas.getJComponent(),
				ScrollPolicy.VERTICAL_BAR_AS_NEEDED,
				ScrollPolicy.HORIZONTAL_BAR_AS_NEEDED,
				18, canvas.getColor(), Color.darkGray);
		}else{
			this.scroll = canvas.getJComponent();
		}
        button = new CTabButton(canvas.getName());
        menuItem = new JMenuItem(button.getText());
        button.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		explorer.selectCanvas(index);
        	}
        });
        menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				explorer.selectCanvas(index);
				explorer.scrollToWheelItem(index);
			}
		});
        canvas.getJComponent().addPropertyChangeListener(this);
	}
	/**
	 * @return the canvas
	 */
	Canvas getCanvas() {
		return canvas;
	}
	/**
	 * @return the scroll pane
	 */
	JComponent getScroll() {
		return scroll;
	}
	/**
	 * @return the button
	 */
	CButton getButton() {
		return button;
	}
	/**
	 * @return the menu item
	 */
	JMenuItem getMenuItem(){
		return menuItem;
	}
    public void propertyChange(PropertyChangeEvent e){
    	if (e.getPropertyName().equals(Canvas.LABEL_CHANGE)){
    		button.setText(canvas.getName());
    		menuItem.setText(canvas.getName());
    	}
    }
}
