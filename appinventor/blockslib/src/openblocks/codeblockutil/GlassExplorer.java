package openblocks.codeblockutil;

import openblocks.codeblockutil.CScrollPane.ScrollPolicy;
import openblocks.workspace.Workspace;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * See documentation for Explorer.
 * 
 * A Glass Explorer explores the canvas by normally
 * displaying only the buttons.  When the user selects a
 * particular button, the corresponding canvas is rolled out
 * from underneath in a glass like scroll pane.  We say glass
 * because there exists a special transparency to the background
 * color.
 */
public class GlassExplorer extends JPanel implements Explorer, FocusListener{
    private static final long serialVersionUID = 328149080292L;
    /** The panel containing the buttons */
    protected JPanel buttonPane;
    private CHoverScrollPane buttonPaneScroll;
    /** The panel containing the canvas */
    private CanvasPane canvasPane;
    /** A timer responsible for rolling out the canvas */
    private EnlargerTimer timer;
    /** The internal list of drawers in order */
    private List<GlassCard> drawers;
    /** A an empty pane for free space */
    private JPanel emptyPane;
    /** The index of the canvas which is selected */
    private int selectedCanvasIndex;
    /** The default index signifying no canvases have been selected */
    private static final int DEFAULT_INDEX = 1000;
    /** The width of the canvas panel */
    private static final int SELECTED_CANVAS_WIDTH = 225;
    /** A List of listeners for this canvas */
    private List<ExplorerListener> listeners;
    /** Background color, medium light green */
    private static final Color BACKGROUND_COLOR = CGraphite.lightergreen;

    private GlassCard currentCard;

    /** Keeps track if the previous action was a drawer just closing due to
    a user clicking on a drawer.  This allows us to ensure that a slow
    press/release will not cause a drawer to open on its close click. */
    private boolean drawerJustClosedDueToDrawerClick;
    /** Is the mouse currently depressed due to a user action on a drawer? */
    private boolean mouseDepressed;

    /**
     * Constructor
     */
    public GlassExplorer() {
        super(new BorderLayout());
        this.selectedCanvasIndex=DEFAULT_INDEX;
        this.drawers = new ArrayList<GlassCard>();
        this.setOpaque(true);
        this.setBackground(BACKGROUND_COLOR);
        this.timer = new EnlargerTimer();
        this.listeners = new ArrayList<ExplorerListener>();
        emptyPane = new JPanel();
        emptyPane.setBackground(BACKGROUND_COLOR);
        buttonPane = new JPanel(new GridBagLayout());
        buttonPane.setBackground(BACKGROUND_COLOR);
        this.buttonPaneScroll = new CHoverScrollPane(
            buttonPane,
            ScrollPolicy.VERTICAL_BAR_AS_NEEDED,
            ScrollPolicy.HORIZONTAL_BAR_AS_NEEDED,
            18, CGraphite.lightgreen, CGraphite.scrollbar_background);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        canvasPane = new CanvasPane();
        canvasPane.setOpaque(false);
        buttonPane.add(canvasPane, c);
        this.add(buttonPaneScroll, BorderLayout.NORTH);
        this.revalidate();
        this.drawerJustClosedDueToDrawerClick = false;
        this.mouseDepressed = false;
        this.addFocusListener(this);
    }

        
    /**
     * Returns the default width of the selected sliding container
     */
    public int getSelectedCanvasWidth(){
        return canvasPane.getFinalWidth();
    }
    
    /**
     * Returns true if a drawer is currently opened at its final width.
     * For GlassExplorer, this represents a drawer being "selected".
     */
    public boolean anyCanvasSelected(){
        return (this.canvasPane.getWidth()==canvasPane.getFinalWidth());
    }
    
    public void addListener(ExplorerListener gel){
        this.listeners.add(gel);
    }
    
    public void removeListener(ExplorerListener gel){
        if (this.listeners.contains(gel)){
            this.listeners.remove(gel);
        }
    }
    
    private void notifyListeners(int event){
        GlassExplorerEvent ge = new GlassExplorerEvent(this,event);
        for (ExplorerListener l: this.listeners){
            l.explorerEventOccurred(ge);
        }
    }
    
    /**
     * Reassigns the set of canvases that this explorer controls.
     * Though the collection of canvas may be empty, it may not be null.
     * @param items
     * 
     * @requires items != null &&
     *           for each element in item, element!= null &&
     *           the Collection is backed by a type that gives a consistent iteration order
     */
    public void setDrawersCard(Collection<? extends Canvas> items){
        drawers.clear();
        buttonPane.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.weightx = 1;
        int i = 0;
        for(Canvas canvas : items){
            GlassCard card = new GlassCard(i, canvas, this);
            drawers.add(card);
            buttonPane.add(card.getButton(), c);
            i++;
        }
        // Soak up the extra space so the buttons end up on top
        c.fill = GridBagConstraints.NONE;
        c.weighty = 1;
        buttonPane.add(emptyPane, c);
    }
    
    /**
     * Add a canvas at the given index (0-based) to the set of canvases that 
     * this explorer controls.
     * @param item the new canvas
     */
    public void addDrawersCard(Canvas item, int index){
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = GridBagConstraints.RELATIVE;
      c.weightx = 1;
      GlassCard card = new GlassCard(index, item, this);
      drawers.add(index, card);
      buttonPane.add(card.getButton(), c, index);
      for (int i = index + 1; i < drawers.size(); i++) {
        drawers.get(i).setIndex(i);  // update the card's index for cards
                                     // that got moved as a result of the insert
      }
      // if this is the first card, add padding
      if (drawers.size() == 1) {
        // Soak up the extra space so the buttons end up on top
        c.fill = GridBagConstraints.NONE;
        c.weighty = 1;
        buttonPane.add(emptyPane, c);
      }
  }

    public void cardsMousePressed(int index){
      mouseDepressed = true;
    }
    /**
     * Selects the particular canvas at the specified index.
     * The canvas is placed onto the its glass pane and the placed onto
     * the canvasPane.  A timer is used to roll out the canvasPane.
     * @param index
     */
    public void selectCanvas(int index){
      // Only open a drawer if either:
      //   - the timer is running and the user has selected a different drawer
      //   - the timer is not running and the user has selected a different drawer
      //       or the user has selected the same drawer,
      //          and we can guarantee that that drawer wasn't closed on the mouse press that this is the release of
        if ((this.selectedCanvasIndex != index) ||
            (!timer.timer.isRunning() && !drawerJustClosedDueToDrawerClick)) {
            if (index >= 0 && index < drawers.size()) {
                this.selectedCanvasIndex=index;
                if (currentCard != null) {
                  currentCard.toggleButtonSelected(false);
                }
                GlassCard card = drawers.get(index);
                card.toggleButtonSelected(true);
                setCurrentCard(card);
                canvasPane.removeAll();
                canvasPane.add(card.getScroll());
                canvasPane.setBackground(card.getBackgroundColor());
                timer.expand();
                Workspace.getInstance().add(canvasPane, Workspace.WIDGET_LAYER);
                Workspace.getInstance().revalidate();
                canvasPane.setBounds(buttonPane.getWidth(), buttonPaneScroll.getY(),
                        0, 0);
                this.requestFocus();
            }
        }
        drawerJustClosedDueToDrawerClick = false;
        mouseDepressed = false;
    }

    private void setCurrentCard(GlassCard card) {
      currentCard = card;
    }
        
    /**
     * Reforms this explorer based on the new size or location of this explorer.
     * For some explorers whose implementation does not depend on the size of itself,
     * this method may trigger no action.
     */
    private int canvasHeight = 0;
    public void reformView(){
        canvasHeight = this.getHeight();
        canvasPane.setSize(0, this.getHeight());
        buttonPaneScroll.setPreferredSize(new Dimension(0,canvasHeight));
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
      // A temporary focusLost event occurs when the mouse leaves the
      // codeblocks window entirely, but only on some architectures.  Do
      // not close the drawer in response to this (since an extra
      // focusGained event will follow it once the mouse returns,
      // confusing us mightily).
      if (! e.isTemporary()) {
        if (mouseDepressed) drawerJustClosedDueToDrawerClick = true;
        timer.shrink();
        currentCard.toggleButtonSelected(false);
      }
    }
    public void focusGained(FocusEvent e){
    }

    /**
     * This class represents the glass pane that slides out when 
     * a drawer is selected.
     */
    private class CanvasPane extends JPanel {
        private static final long serialVersionUID = 328149080293L;
        
        // the width that this pane should be when opened.  When
        // new things are added this should be updated.
        private int finalWidth = 20; 

        // an extra, invisible panel that maintains a constant size
        // so that contained components don't resize while the pane slides open.
        private JPanel extraPanel;

        // border around the extraPanel to leave room for the CanvasPane
        // decorations like rounded corners and a little blank space.
        private Insets insets = new Insets(15,5,35,3);

        
        /**
         * Creates a new CanvasPane
         */
        public CanvasPane() {
            // the extraPanel only exists for sizing reasons.  It is invisible.
            // Without it, when the CanvasPane opens (gets wider), the contents
            // try to resize themselves.  The extraPanel gives the contents
            // a frame of reference to size by, and it doesn't change size
            // as the CanvasPane opens and closes (i.e. widens and shrinks)
            if (extraPanel == null) {
                extraPanel = new JPanel();
                extraPanel.setLayout(new BorderLayout());
                extraPanel.setBackground(new Color(0,0,0,0));
                extraPanel.setOpaque(false);
                super.add(extraPanel);
            }
        }
        
        /**
         * The CanvasPane keeps track of the width it would like to
         * be when opened fully.  This method returns that value.
         * @return the preferred final width of the CanvasPane when opened.
         */
        public int getFinalWidth() {
            return finalWidth;
        }
                
        public void paint(Graphics g){      
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = this.getWidth();
            int h = this.getHeight();
            g.setColor(this.getBackground());
            g.fillRoundRect(-10,10,w+9,h-40,10*2,10*2);
            g2.setStroke(new BasicStroke(1));
            g.setColor(new Color(0,0,0,100));
            g.drawRoundRect(-10,10,w+8,h-41,10*2,10*2);
            g.setColor(this.getBackground());
            g.drawLine(-10, 10, w-11, 10);
            g.setColor(new Color(255,255,255,80));
            g.drawLine(-10, 10, w-11, 10);
            g2.setStroke(new BasicStroke(1));
            
            // slide the contents right-aligned rather than left-aligned
            extraPanel.setLocation((w-finalWidth)+insets.left,insets.top);
            super.paint(g);
        }
        
        /**
         * This method behaves as JPanel.add() except that it sets its final
         * size according to the preferred size of the component being added.
         * This assumes that only ONE component will be added!  If more are to
         * be added, first add them to another container and set that container's
         * preferredSize.
         * @override java.awt.Container#add(java.awt.Component)
         * @param comp the Component to be added
         * @requires the CanvasPane doesn't yet contain any components, and 
         * comp has its preferredSize set
         */
        public Component add(Component comp) {
            // behave exactly like JPanel.add(), except add the component
            // to the statically-sized extraPanel rather than adding
            // it directly to the CanvasPane.  Tell the extraPanel to
            // take on the preferredSize of whatever is added.
            if (comp.getPreferredSize().width != extraPanel.getWidth()) {
                finalWidth = comp.getPreferredSize().width + insets.right + insets.left;
                extraPanel.setSize(comp.getPreferredSize().width, getHeight() - insets.top - insets.bottom);
                extraPanel.setMaximumSize(new Dimension(comp.getPreferredSize().width, extraPanel.getHeight()));
                extraPanel.setMinimumSize(new Dimension(comp.getPreferredSize().width, extraPanel.getHeight()));
                extraPanel.setPreferredSize(new Dimension(comp.getPreferredSize().width, extraPanel.getHeight()));
            }
            return extraPanel.add(comp);
        }
        
        /**
         * @override java.awt.Container#remove(java.awt.Component)
         * @see java.awt.Container#remove(java.awt.Component)
         */
        public void remove(Component comp) {
            // remove from the extraPanel rather than the CanvasPane itself
            if (extraPanel != null && !comp.equals(extraPanel)) {
                extraPanel.remove(comp);
            }
        }
        
        /**
         * @override java.awt.Container#removeAll()
         * @see java.awt.Container#removeAll()
         */
        public void removeAll() {
            // remove from the extraPanel rather than the CanvasPane itself
            if (extraPanel != null) {
                extraPanel.removeAll();
            }
        }
    };
    
    
    /**
     * A timer responsible for rolling out the canvasPane.
     */
    private class EnlargerTimer implements ActionListener{
        private javax.swing.Timer timer;
        private boolean expand;
        public EnlargerTimer(){
            this.expand = true;
            // Set a delay of 10 between animation jumps.
            timer = new Timer(10, this);
        }
        /**
         * Responsible for expanding or shrinking the canvasPane until
         * it has reached the appropriate size.
         */
        public void actionPerformed(ActionEvent e){
            if(expand){
              if (canvasPane.getWidth() < canvasPane.getFinalWidth()) {
                canvasPane.setSize(canvasPane.getFinalWidth(), canvasHeight);
                canvasPane.revalidate();
                canvasPane.repaint();
              }else{
                timer.stop();
                GlassExplorer.this.notifyListeners(GlassExplorerEvent.SLIDING_CONTAINER_FINISHED_OPEN);
                canvasPane.setSize(canvasPane.getFinalWidth(), canvasHeight);
                canvasPane.revalidate();
                canvasPane.repaint();
              }
            }else{
                if(canvasPane.getWidth() > 5){
                  canvasPane.setSize(canvasPane.getWidth()/4, canvasHeight);
                    canvasPane.revalidate();
                    canvasPane.repaint();
                }else{
                    timer.stop();
                    GlassExplorer.this.notifyListeners(GlassExplorerEvent.SLIDING_CONTAINER_FINISHED_CLOSED);
                    canvasPane.setSize(0, canvasHeight);
                    canvasPane.revalidate();
                    canvasPane.repaint();
                }
            }
        }
        /**
         * Expands the canvasPane
         */
        public void expand(){
            this.expand = true;
            this.timer.start();
        }
        /**
         * Shrinks the canvasPane
         */
        public void shrink(){
            this.expand=false;
            timer.start();
        }
    }
}
