package openblocks.workspace;

import java.awt.Color;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JComponent;
import javax.swing.JPanel;

import openblocks.renderable.RenderableBlock;
import openblocks.codeblocks.Block;
import openblocks.codeblockutil.CGraphite;
import openblocks.codeblockutil.Canvas;

/**
 * A Canvas that acts as the parent of all blocks.
 * A FactoryCanvas is the actually graphical "drawer".
 * As a canvas is must support all the name/color
 * accessor methods.  In addition it also supports
 * various methods for blocks such as searching,
 * highlighting, etc.
 */
public class FactoryCanvas extends JPanel
    implements
    Canvas,
    SearchableContainer,
    RBParent,
    ComponentListener {

  private static final boolean DEBUG = false;

  private static final long serialVersionUID = 328149080291L;

        private static final int BORDER_WIDTH = 10;
        /** The highlight of this canvas */
        private Color highlight = null;
        /** The color of this canvas */
        private Color color;
        public static final Color DEFAULT_CANVAS_COLOR = CGraphite.lightgreen;
        /**
         * Constructs a new FactoryCanvas
         * @param name
         * @param color
         */
        FactoryCanvas(String name, Color color){
                super();
                this.setBackground(CGraphite.gray);
                this.setName(name);
                this.setColor(color);
                this.setLayout(null);
        }
        FactoryCanvas(String name){
                this(name, DEFAULT_CANVAS_COLOR);
        }
        public Iterable<? extends SearchableElement> getSearchableElements(){
                return this.getBlocks();
        }
        ArrayList<RenderableBlock> getBlocks() {
                ArrayList<RenderableBlock> list = new ArrayList<RenderableBlock> ();
                for(Component comp : this.getComponents()){
                        if(comp instanceof RenderableBlock){
                                list.add((RenderableBlock)comp);
                        }
                }
                return list;
    }

        public void updateContainsSearchResults(boolean containsSearchResults){
                Color previousHighlight = this.highlight;
                if(containsSearchResults){
                        this.setHighlight(Color.yellow);
                }else{
                        this.setHighlight(null);
                }
                this.firePropertyChange(Canvas.LABEL_CHANGE, this.highlight, previousHighlight);
        }
        void setHighlight(Color highlight){
                this.highlight = highlight;
        }
        public Color getHighlight(){
                return this.highlight;
        }
        public Color getColor(){
                return color;
        }
        public void setColor(Color color){
                if (color == null) {
                        this.color = DEFAULT_CANVAS_COLOR;
                }else{
                        this.color = color;
                }
        }
        public JComponent getJComponent(){
                return this;
        }

        @Override
        public void setName(String name){
                super.setName(name);
                this.repaint();
        }
    void addBlock(RenderableBlock block) {
        //make sure block isn't a null instance
        if(block == null || Block.NULL.equals(block.getBlockID())) return;
        addToBlockLayer(block);
        block.setHighlightParent(this);
                block.addComponentListener(this);
        }
    void removeBlock(RenderableBlock block) {
        //make sure block isn't a null instance
        if(block == null || Block.NULL.equals(block.getBlockID())) return;
        //remove from canvas graphically
        this.remove(block);
        block.setHighlightParent(this);
                block.removeComponentListener(this);
    }
    public void layoutBlocks(){
        int maxWidth = 20;
        int tx=BORDER_WIDTH;
        int ty=BORDER_WIDTH;
        ArrayList<RenderableBlock> myBlocks = this.getBlocks();
        // Special-case the My Definitions drawer to list the
        // blocks in a specific order, as determined by compareBlockLabels
        if (this.getName().equals(FactoryManager.DEFINITIONS_NAME)) {
         Collections.sort(myBlocks, new compareBlockLabels());
        }
        for(RenderableBlock rb : myBlocks){
                        rb.setBounds(tx, ty, rb.getBlockWidth(), rb.getBlockHeight());
                        if (rb.isVisible()) { //don't make space for invisible blocks
                          ty = ty + BORDER_WIDTH + rb.getBlockHeight();
                        }
                        rb.repaint();
                        if (maxWidth < rb.getBlockWidth() + BORDER_WIDTH) {
                                maxWidth = rb.getBlockWidth() + BORDER_WIDTH;
                        }
                }
        this.setPreferredSize(new Dimension(maxWidth, ty));
    }

    private class compareBlockLabels implements Comparator<RenderableBlock> {
      // Compare blocks by lexical ordering on block labels.
      // TODO(halabelson): Think about also putting all call blocks first, then global
      // and set global blocks, then value blocks
      @Override
      public int compare(RenderableBlock block1, RenderableBlock block2) {
        String name1 = block1.getBlock().getBlockLabel();
        String name2 = block2.getBlock().getBlockLabel();
        if (DEBUG) {
          System.out.println("block1: " + block1 + "/" + name1 + " block2: " + "/" + name2);
        }
        return name1.compareToIgnoreCase(name2);
      }
    }

    private final Integer BLOCK_HIGHLIGHT_LAYER = new Integer(0);
    private final Integer BLOCK_LAYER = new Integer(1);

    public void addToBlockNoteLayer(Component c) {
      // Include this just to satisfy the RBParent interface.
      throw new UnsupportedOperationException("You shouldn't add notes to factory blocks");
    }

    public void addToBlockArrowLayer(Component c) {
      // Include this just to satisfy the RBParent interface.
      throw new UnsupportedOperationException("You shouldn't add notes to factory blocks");
    }

    public void addToBlockLayer(Component c) {
      add(c, BLOCK_LAYER);
    }

    public void addToHighlightLayer(Component c) {
      add(c, BLOCK_HIGHLIGHT_LAYER);
    }

    public void componentResized(ComponentEvent e){
      this.layoutBlocks();
    }

    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentShown(ComponentEvent e){}
}
