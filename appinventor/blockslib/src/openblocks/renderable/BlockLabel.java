// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.renderable;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockConnector;
import openblocks.codeblocks.BlockGenus;
import openblocks.codeblocks.BlockStub;
import openblocks.codeblockutil.LabelWidget;
import openblocks.yacodeblocks.ProcedureBlockManager;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * BlockLabel is a region on a block in which text is displayed and possibly
 * edited.  The location and font of a BlockLabel is specified in BlockShape
 * and the text displayed is specified by a Block, BlockLabel is the gateway
 * for text to be rendered and modified.
 *
 * The key nature of a BlockLabel is that it is a JLabel when being viewed,
 * and a JTextField when it is being edited.
 *
 * During mouse move, entered and exited events a white border is toggled
 * around the label for particular blocks. This white border helps to suggest
 * editable labels for blocks that have this enabled.
 */
public class BlockLabel implements MouseListener, MouseMotionListener, KeyListener{
  /**Enum for the different types of labels in codeblocks */
  public enum Type { NAME_LABEL, PAGE_LABEL, PORT_LABEL, DATA_LABEL, DECORATOR_LABEL }

  public final static Font blockFontSmall_Bold  = new Font("Arial", Font.BOLD, 7);
  public final static Font blockFontMedium_Bold = new Font("Arial", Font.BOLD, 10);
  public final static Font blockFontLarge_Bold  = new Font("Arial", Font.BOLD, 12);
  public final static Font blockFontSmall_Plain  = new Font("Arial", Font.PLAIN, 7);
  public final static Font blockFontMedium_Plain = new Font("Arial", Font.PLAIN, 10);
  public final static Font blockFontLarge_Plain  = new Font("Arial", Font.PLAIN, 12);

  private LabelWidget widget;
  /** These keys inputs are delegated back to renderable block */
  private final char[] validOperators = {'-', '+', '/', '*', '=', '<', '>', 'x', 'X'};
  private Long blockID;
  private BlockLabel.Type labelType;
  private double zoom = 1.0;

  /**
   * BlockLabel Constructor
   * NOTE: A true boolean passed into the isEditable parameter does not
   * necessarily make the label editable, but a false boolean will make
   * the label uneditable.
   */
  public BlockLabel(String initLabelText, BlockLabel.Type labelType, boolean isEditable,
      Color tooltipBackground) {
    this(initLabelText, labelType, isEditable, -1, false, tooltipBackground);
  }

  public BlockLabel(String initLabelText, BlockLabel.Type labelType, boolean isEditable,
      long blockID, boolean hasComboPopup, Color tooltipBackground) {

    if (Block.NULL.equals(blockID)){
      throw new RuntimeException("May not pass a null block instance as the parent of a block " +
          "label");
    }
    if (initLabelText == null) {
      initLabelText = "";
    }

    this.blockID = blockID;
    this.labelType = labelType;

    widget = new LabelWidget(initLabelText,
        RenderableBlock.getRenderableBlock(blockID).getColor(),
        tooltipBackground) {

      private static final long serialVersionUID = 328149080424L;
      @Override
      protected void fireTextChanged(String text){
        textChanged(text);
      }
      @Override
      protected void fireGenusChanged(String genus){
        genusChanged(genus, false);
      }
      @Override
      protected void fireDimensionsChanged(Dimension value){
        dimensionsChanged(value);
      }
      @Override
      protected boolean isTextValid(String text){
        return textValid(text);
      }
    };
    if (labelType == null || labelType.equals(BlockLabel.Type.NAME_LABEL)) {
      widget.setFont(BlockLabel.blockFontLarge_Bold);
    } else if (labelType.equals(BlockLabel.Type.PAGE_LABEL)) {
      widget.setFont(BlockLabel.blockFontMedium_Bold);
    } else if (labelType.equals(BlockLabel.Type.PORT_LABEL)) {
      widget.setFont(BlockLabel.blockFontMedium_Bold);
    } else if (labelType.equals(BlockLabel.Type.DATA_LABEL)) {
      widget.setFont(BlockLabel.blockFontMedium_Bold);
    } else if (labelType.equals(BlockLabel.Type.DECORATOR_LABEL)) {
      widget.setFont(BlockLabel.blockFontMedium_Plain);
    }
    if (Block.getBlock(blockID).hasSiblings()) {
      //Map<String, String> siblings = new HashMap<String, String>();
      List<String[]> siblings = Block.getBlock(blockID).getSiblingsList();
      widget.setSiblings(hasComboPopup, siblings);
    }

    widget.addMouseListenerToLabel(this);
    widget.addMouseMotionListenerToLabel(this);
    widget.addKeyListenerToTextField(this);

    updateWidgetAppearance(isEditable, initLabelText, true);
  }

  private void updateWidgetAppearance(boolean isEditable, String labelText, boolean initializing){
    widget.setNumeric(Block.getBlock(this.blockID).getGenusName().equals("number"));

    // Only editable if the isEditable parameter was true, the label is either a Block's name or
    // socket label, the block can edit labels, and the block is not in the factory.
    widget.setEditable(
        isEditable &&
        (labelType == BlockLabel.Type.NAME_LABEL || labelType == BlockLabel.Type.PORT_LABEL) &&
        Block.getBlock(blockID).isLabelEditable() &&
        !(RenderableBlock.getRenderableBlock(blockID) instanceof FactoryRenderableBlock));

    //set text
    widget.updateLabelText(labelText, initializing);
    //add and show the textLabel initially
    widget.setEditingState(false);
    widget.setColors(RenderableBlock.getRenderableBlock(blockID).getColor().darker(),
        widget.getTooltipBackground());

    widget.validate();
  }

  public void setZoomLevel(double newZoom){
    this.zoom = newZoom;
    widget.setZoomLevel(newZoom);
  }

  public int getAbstractWidth(){
    if (widget.hasSiblings()){
      return (int) (widget.getWidth() / zoom) - LabelWidget.DROP_DOWN_MENU_WIDTH;
    } else {
      return (int) (widget.getWidth() / zoom);
    }
  }

  public int getAbstractHeight(){
    return (int) (widget.getHeight() / zoom);
  }

  public int getPixelWidth(){
    return widget.getWidth();
  }

  public int getPixelHeight(){
    return widget.getHeight();
  }

  public Point getPixelLocation(){
    return widget.getLocation();
  }

  public void setEditable(boolean isEditable){
    widget.setEditable(isEditable);
  }

  public boolean editingText(){
    return widget.editingText();
    }

  public void highlightText(){
    widget.highlightText();
  }

  public void setPixelLocation(int x, int y){
    widget.setLocation(x, y);
  }

  public String getText(){
    return widget.getText();
  }

  public void setText(String text){
    widget.setText(text);
  }

  public void setText(boolean text){
    widget.setText(text);
  }

  public void setText(double text){
    widget.setText(text);
 }

  public void setToolTipText(String text){
    widget.assignToolTipToLabel(text);
  }

  public void showMenuIcon(boolean show){
    widget.showMenuIcon(show);
  }

  public JComponent getJComponent(){
    return widget;
  }

  public void setEditingState(boolean editing){
    widget.setEditingState(editing);
  }

  protected int rescale(int x){
    return (int) (x * zoom);
  }

  protected int rescale(double x){
    return (int) (x * zoom);
  }

  protected int descale(int x){
    return (int) (x / zoom);
  }

  protected int descale(double x){
    return (int) (x / zoom);
  }

  /** returns the blockID for this BlockLabel */
  Long getBlockID() {
    return blockID;
  }

  protected void textChanged(String text){
    if ((this.labelType.equals(BlockLabel.Type.NAME_LABEL) ||
        this.labelType.equals(BlockLabel.Type.PORT_LABEL)) &&
        Block.getBlock(blockID).isLabelEditable()) {
      if (this.labelType.equals(BlockLabel.Type.NAME_LABEL)) {
        Block.getBlock(blockID).setBlockLabel(text);
      }
      BlockConnector plug = Block.getBlock(blockID).getPlug();
      // Check if we're connected to a block. If we are and the the block we're connected to
      // has stubs, update them.
      if (plug != null && plug.getBlockID() != Block.NULL) {
        if (Block.getBlock(plug.getBlockID()) != null) {
          Block plugBlock = Block.getBlock(plug.getBlockID());
          if ((plugBlock.isProcedureDeclBlock() ||
              ProcedureBlockManager.isProcDeclBlock(plugBlock))
              && plugBlock.hasStubs()) {
            // Blocks already store their socket names when saved so it is not necessary
            // nor desired to call the connectors changed event again.
            if (!RenderableBlock.getRenderableBlock(plug.getBlockID()).isLoading()) {
              BlockStub.parentConnectorsChanged(plug.getBlockID());
            }
          }
        }
      }
      RenderableBlock rb = RenderableBlock.getRenderableBlock(blockID);
      if (rb != null) {
        Workspace.getInstance().notifyListeners(new WorkspaceEvent(rb.getParentWidget(), blockID,
            WorkspaceEvent.BLOCK_RENAMED));
      }
    }
  }

  protected void genusChanged(String genus, boolean preserveLabel){
    if (widget.hasSiblings()) {
      Block oldBlock = Block.getBlock(blockID);
      oldBlock.changeGenusTo(genus, preserveLabel);
      updateWidgetAppearance(BlockGenus.getGenusWithName(genus).isLabelEditable(),
          oldBlock.getBlockLabel(), false);

      RenderableBlock rb = RenderableBlock.getRenderableBlock(blockID);
      rb.repaintBlock();

      Workspace.getInstance().notifyListeners(new WorkspaceEvent(rb.getParentWidget(), blockID,
          WorkspaceEvent.BLOCK_GENUS_CHANGED));
    }
  }

  protected void dimensionsChanged(Dimension value){
    if (RenderableBlock.getRenderableBlock(blockID) != null) {
      RenderableBlock.getRenderableBlock(blockID).repaintBlock();
    }
  }

  protected boolean textValid(String text){
    // todo(user) once the error reporting in codeblocks is setup, here we should tell the user
    // something about how they failed at inputting a legal label
    return BlockUtilities.isLabelValid(blockID, text);
  }

  public void mouseClicked(MouseEvent e) {
    if (!((e.getClickCount() == 1) && widget.isEditable())) {
      RenderableBlock.getRenderableBlock(blockID).processMouseEvent(SwingUtilities.
          convertMouseEvent(widget, e, widget.getParent()));
    }
  }

  public void mousePressed(MouseEvent e) {
    if (widget.getParent() != null && widget.getParent() instanceof MouseListener) {
      RenderableBlock.getRenderableBlock(blockID).processMouseEvent(SwingUtilities.
          convertMouseEvent(widget, e, widget.getParent()));
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (widget.getParent() != null && widget.getParent() instanceof MouseListener) {
      RenderableBlock.getRenderableBlock(blockID).processMouseEvent(SwingUtilities.
          convertMouseEvent(widget, e, widget.getParent()));
    }
  }

  public void mouseEntered(MouseEvent e) {
    if (widget.getParent() != null && widget.getParent() instanceof MouseListener) {
      RenderableBlock.getRenderableBlock(blockID).processMouseEvent(SwingUtilities.
          convertMouseEvent(widget, e, widget.getParent()));
    }
  }
  public void mouseExited(MouseEvent e) {
    if (widget.getParent() != null && widget.getParent() instanceof MouseListener) {
      RenderableBlock.getRenderableBlock(blockID).processMouseEvent(SwingUtilities.
          convertMouseEvent(widget, e, widget.getParent()));
    }
  }

  public void mouseDragged(MouseEvent e) {
    if (widget.getParent() != null && widget.getParent() instanceof MouseMotionListener) {
      ((MouseMotionListener)widget.getParent()).mouseDragged(SwingUtilities.
          convertMouseEvent(widget, e, widget.getParent()));
    }
  }

  public void mouseMoved(MouseEvent e) {}

  public void keyPressed(KeyEvent e){
    switch (e.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
        RenderableBlock.getRenderableBlock(blockID).requestFocus();
        return;
      case KeyEvent.VK_ENTER:
        RenderableBlock.getRenderableBlock(blockID).requestFocus();
        return;
      case KeyEvent.VK_TAB:
        RenderableBlock.getRenderableBlock(blockID).processKeyPressed(e);
        return;
    }
    if (Block.getBlock(this.blockID).getGenusName().equals("number")) {
      if (e.getKeyChar() == '-' && widget.canProcessNegativeSign()) {
        return;
      }
      for (char c : validOperators) {
        if (e.getKeyChar() == c) {
          RenderableBlock.getRenderableBlock(blockID).processKeyPressed(e);
          return;
        }
      }
    }
  }

  public void keyReleased(KeyEvent e) {}
  public void keyTyped(KeyEvent e) {}
}
