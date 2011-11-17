package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import openblocks.codeblockutil.CScrollPane.ScrollPolicy;


/**
 * See documentaton for Explorer.
 * 
 * A PopupExplorer explores the canvas by normally
 * displaying only the canvas.  When the user mouses over
 * the left edge, the button pane is rolled out.
 */
public class PopupExplorer extends JLayeredPane implements Explorer, ComponentListener{
	private static final long serialVersionUID = 328149080299L;
	/** The panel containing the buttons */
	protected JPanel buttonPane;
	/** The panel containing the canvas */
	private JPanel viewport;
	/** The scrolling pane */
	private JComponent scroll;
	/** The internal list of drawers in order */
	private List<Canvas> components;
    /**
     * Constructor
     */
    public PopupExplorer() {
        super();
        this.components = new ArrayList<Canvas>();
        this.setLayout(null);
        this.setOpaque(true);
        this.setBackground(Color.black);
        
        this.viewport = new JPanel(new BorderLayout());
		scroll = new CHoverScrollPane(
				viewport,
				ScrollPolicy.VERTICAL_BAR_ALWAYS,
				ScrollPolicy.HORIZONTAL_BAR_NEVER,
				20, Color.blue, new Color(0,0,50));
        add(scroll, JLayeredPane.DEFAULT_LAYER);
        setLayer(scroll, JLayeredPane.DEFAULT_LAYER, 0);
        
        buttonPane = new ButtonPanel();
        add(buttonPane, JLayeredPane.PALETTE_LAYER);
        setLayer(buttonPane, JLayeredPane.PALETTE_LAYER, 0);
        this.addComponentListener(this);
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
	 * Reassigns the set of canvases that this explorer controls.
	 * Though the collection of canvas may be empty, it may not be null.
	 * @param items
	 * 
	 * @requires items != null &&
	 * 			 for each element in item, element!= null &&
         *           the Collection is backed by a type that gives a consistent iteration order
	 */
	public void setDrawersCard(Collection<? extends Canvas> items){
		components.clear();
		components.addAll(items);
		reformView();
                for(Canvas item : items){
                  // just want the first item, but no easy way to do this for a collection
                  this.setViewPort(item);
                  break;
                }
	}
    public void addDrawersCard(Canvas item, int index) {
      // not implemented!
      throw new RuntimeException("method not implemented");
    }
	/**
	 * Reforms this explorer based on the new size or location of this explorer.
	 * For some explorers whose implementation does not depend on the size of itself,
	 * this method may trigger no action.
	 */
	public void reformView(){
    	scroll.setBounds(0,0,this.getWidth(), this.getHeight());
    	scroll.revalidate();
    	scroll.repaint();
    	buttonPane.setSize(60, this.getSize().height);
	}
	/**
	 * Selects the particular canvas at the specified index.
	 * THe canvas is placed onto the it's glass pane and the placed onto
	 * the canvasPane.
	 */
	void setViewPort(Canvas view){
		this.viewport.removeAll();
		this.viewport.add(view.getJComponent(), BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}
	/**
	 * Selects the particular canvas at the specified index.
	 * THe canvas is placed onto the it's glass pane and the placed onto
	 * the canvasPane.
	 */
	public void selectCanvas(int index){
		if(index >=0 && index < components.size()){
			Canvas view = components.get(index);
			this.viewport.removeAll();
			this.viewport.add(view.getJComponent(), BorderLayout.CENTER);
			this.revalidate();
			this.repaint();
		}
	}
	
    /**
     * @return a JCOmponent representation of this explorer
     */
    public JComponent getJComponent(){
    	return this;
    }
    public void componentResized(ComponentEvent e) {
    	reformView();
    }
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    /**
     * The button Pane
     */
    private class ButtonPanel extends JPanel implements MouseListener, MouseMotionListener{
    	private static final long serialVersionUID = 328149080300L;
    	private EnlargerTimer timer;
    	protected ButtonPanel(){
    		super(null);
            this.setOpaque(false);
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
            this.timer = new EnlargerTimer();
            this.setFont(new Font ("Ariel", Font.BOLD, 13));
    	}
    	protected void paintComponent(Graphics g){
    		super.paintComponent(g);
    		if(!components.isEmpty()){
	    		Graphics2D g2 = (Graphics2D)g;
	    		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    		int yloc = 0;
	    		int dh=0;
				dh = this.getHeight()/(components.size());
	    		int i = 0;
	    		for(Canvas p : components){
	    			g.setColor(new Color((i*27)%255,(i*47)%255,(i*57)%255, 150));
	    			g.fillRoundRect(-10, yloc, this.getWidth()+9, dh, 20, 20);
	    			g.setColor(Color.white);
	    			g.drawRoundRect(-10, yloc, this.getWidth()+9, dh, 20, 20);
	    			if(p.getName() == null){
	        			g.drawString(p.toString(), 0, yloc+dh/2+g.getFontMetrics().getHeight()/2);
	    			}else
	    				g.drawString(p.getName(), 0, yloc+dh/2+g.getFontMetrics().getHeight()/2);
	    			yloc = yloc +dh;
	    			i++;
	    		}
    		}
    	}
    	public void mouseMoved(MouseEvent e){}
    	public void mouseClicked(MouseEvent e){}
    	public void mouseDragged(MouseEvent e){}
    	public void mouseReleased(MouseEvent e) {}
    	public void mousePressed(MouseEvent e) {
    		int index = (components.size())*e.getY()/this.getHeight();
    		if(index>=0 && index < components.size()){
	    		PopupExplorer.this.setViewPort(components.get(index));
    		}
    	}
    	public void mouseEntered(MouseEvent e) {
    		timer.expand();
    	}
    	public void mouseExited(MouseEvent e) {
    		timer.shrink();
    	}
    }
    private class EnlargerTimer implements ActionListener{
    	private int count;
    	private javax.swing.Timer timer;
    	private boolean expand;
    	public EnlargerTimer(){
    		count = 0;
    		this.expand = true;
        	timer = new Timer(15, this);
    	}
    	public void actionPerformed(ActionEvent e){
    		if(expand){
        		if(count>10){
        			timer.stop();
        		}else{
        			count = count + 1;
        			buttonPane.setBounds(0,0,60+count*10, buttonPane.getHeight());
        			repaint();
        		}
    		}else{
        		if(count<=0){
        			timer.stop();
        		}else{
        			count = count -1;
        			buttonPane.setBounds(0,0,60+count*10, buttonPane.getHeight());
        			repaint();
        		}
    		}
    		if(count<-100 || count > 100) throw new RuntimeException("Timer continues infinityly");
    	}
    	public void expand(){
    		this.expand = true;
    		this.timer.start();
    	}
    	public void shrink(){
    		this.expand=false;
    		this.timer.start();
    	}
    }
}
