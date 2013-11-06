// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.yacodeblocks;

import openblocks.codeblocks.Block;
import openblocks.codeblockutil.CGraphite;
import openblocks.renderable.BlockUtilities;
import openblocks.renderable.FactoryRenderableBlock;
import openblocks.renderable.RenderableBlock;
import openblocks.workspace.FactoryException;
import openblocks.workspace.FactoryManager;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceWidget;
import openblocks.workspace.typeblocking.FocusTraversalManager;
import openblocks.yacodeblocks.WorkspaceControllerHolder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;

/**
 * A HoverMenu provides shortcuts contextual support.  It is intended to
 * appear near a code block or region of interest, with the content of the
 * HoverMenu varying depending on the context in which it is displayed.
 *
 *
 */

public class HoverMenu extends JPanel {

  static final Color MENU_BACKGROUND = CGraphite.lightergreen;

  // Micro-drawer constants:
  static final Dimension DRAWER_BUTTON_SIZE = new Dimension(42, 14);
  static final Font DRAWER_BUTTON_FONT = new Font("Helvetica", Font.PLAIN, 10);

  static final Border DRAWER_BUTTON_BORDER =
    new javax.swing.border.SoftBevelBorder(SoftBevelBorder.RAISED);

  //TODO(user) Give the set of little buttons an opaque backing because
  // they sometimes fall on some blocks with matching colors.

  // Icon constants
  static final boolean SHOW_ICON_PANEL = false;
  static final Dimension ICON_SIZE = new Dimension(16,16);

  MicroDrawers microDrawers;
  IconPanel buttons;

  JComponent target = null;  // currently focused target of the menu
  Point location = new Point(0,0);  // cursor location within the target


  /**
   * Creates a HoverMenu with default size and background.
   */
  public HoverMenu() {
    setInitialDisplayValues();
  }

  public void reset() {
    removeAll();
    setInitialDisplayValues();
    microDrawers = null;
    buttons = null;
  }


  private void setInitialDisplayValues(){
    setLayout(new BorderLayout());
    setOpaque(false);
    setBackground(MENU_BACKGROUND);
    setBorder(null);
    setLocation(-1000,-1000);
  }

  /**
   * Creates the micro-drawers and icon panel (if used).
   * Since initialization depends upon prior initialization of the codeblocks
   * drawers, the constructor does not call this method.
   * Instead, we perform initialization the first time a context is set
   * for the hover menu.
   */
  protected void initialize() {
    if (microDrawers == null) {  // not initialized yet
      microDrawers = new MicroDrawers();
      add(microDrawers, BorderLayout.SOUTH);
      if (SHOW_ICON_PANEL) {
        buttons = new IconPanel();
        add(buttons, BorderLayout.CENTER);
      }
      validate();
    }
  }

  /**
   * Sets the current context of the hover menu.

   * @param target the code block that has the user's attention (or null)
   * @param location the point of user attention within the target
   */
  public void setContext(JComponent target, Point location) {
    initialize();
    this.target = target;

    if (target instanceof RenderableBlock) {
      Long blockId = ((RenderableBlock) target).getBlockID();
      FocusTraversalManager focusManager = Workspace.getInstance().getFocusManager();
      if (focusManager.getFocusBlockID() != blockId) {
        focusManager.setFocus(blockId);
      }
    }

    this.location = location;
    // TODO(user): Use context information to vary the content of the
    // menu.  For example, if the location is in a parameter slot, provide
    // additional icon options as "hints" for what can go in that slot.
    setSize(getPreferredSize());
  }

  /**
   * A MicroDrawers object has one button and popup menu corresponding to each
   * codeblocks drawer.
   */
  class MicroDrawers extends JPanel {
    public MicroDrawers() {
      super(new FlowLayout());
      setOpaque(false);
      Workspace ws = Workspace.getInstance();
      FactoryManager fm = ws.getFactoryManager();
      int count = 0;
      for (String name : fm.getStaticDrawers()) {
        final JButton b = new JButton();
        b.setBorder(DRAWER_BUTTON_BORDER);
        b.setPreferredSize(DRAWER_BUTTON_SIZE);

        // TODO(young-android-dev) Colors were previously hard coded here
        // when plans for context (i.e. block) specific hover menu drawers
        // were still in scope.  If these get re-added, special handling
        // will be needed for their colors.
        b.setBackground(fm.getStaticDrawerColor(name));

        try {
          final JPopupMenu popup = new BlockPopupMenu(fm.getStaticBlocks(name));
          popup.setLabel(name);
          b.setToolTipText(name);
          b.setFont(DRAWER_BUTTON_FONT);
          b.setText(name);
          b.setForeground(CGraphite.darkgray);
          b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              popup.show(b, 0, b.getHeight());
            }
          });
          add(b);
        } catch (FactoryException e) {
          System.out.println("Can't find blocks for drawer named " + name);
        }
      }
    }
  }

  /**
   * A BlockPopupMenu supports creating instances of a collection of code block
   * types, as represented by a given collection of code blocks.
   * Typically, this collection would be the items in a codeblocks drawer.
   * When an item is selected from the menu, a new instance of that
   * codeblock is created and placed in an open space to the right of the
   * user's last focus.
   */
  class BlockPopupMenu extends JPopupMenu {

    /**
     * Creates a BlockPopupMenu
     * @param rblocks the codeblocks to be represented as menu items
     */
    public BlockPopupMenu(Collection<RenderableBlock> rblocks) {
      setBackground(MENU_BACKGROUND);
      for (RenderableBlock rblock : rblocks) {
        Block block = rblock.getBlock();
        final FactoryRenderableBlock frb = (FactoryRenderableBlock) rblock;

        add(new AbstractAction(block.getBlockLabel()) {
          public void actionPerformed(ActionEvent arg0) {
            if (!WorkspaceControllerHolder.get().haveProject()) {
              FeedbackReporter.showInfoMessage(FeedbackReporter.NO_PROJECT_MESSAGE);
              return;
            }
            RenderableBlock newRenderableBlock = frb.createNewInstance();
            if (!BlockUtilities.isNullBlockInstance(newRenderableBlock.getBlockID())) {
              addBlock(newRenderableBlock);
            }
          }
        });
      }
    }
  }

  /**
   * Adds a renderable block to the current page and gives the block focus.
   * @param block the block to add.
   */
  public void addBlock(RenderableBlock block){
    setVisible(false);
    Workspace workspace = Workspace.getInstance();
    JComponent canvas = workspace.getBlockCanvas().getCanvas();
    WorkspaceWidget page = workspace.getBlockCanvas().getTheOnlyPage();
    canvas.add(block, 0);
    block.ignoreDefaultArguments();

    // Put the block to its new position.
    block.setLocation(getDropPosition());
    // Link it if it's close to plug or socket
    block.tryLinking(page);

    workspace.getFocusManager().setFocus(block.getBlockID());
  }

  /**
   * Returns the point where a new block should be dropped on the workspace.
   * @return the point where a new block should be dropped on the workspace.
   */
  private Point getDropPosition(){
    return SwingUtilities.convertPoint(target, location,
        Workspace.getInstance().getBlockCanvas().getCanvas());
  }

  /**
   * An IconPanel is intended as a context-sensitive collection of
   * buttons for acting on the current target of the HoverMenu.
   * TODO(user): Add context-sensitive content.
   */
  class IconPanel extends JPanel {

    /**
     * Creates a default icon panel.
     */
    public IconPanel() {
      super(new FlowLayout());
      setOpaque(false);
      add(new IconButton("Copy16.gif", "Copy"));
      add(new IconButton("SaveAll16.gif", "Save all"));
      add(new IconButton("Information16.gif", "Help"));
      add(new IconButton("Trash16.gif", "Delete"));
    }

    /**
     * IconButton is a convenience class for populating the IconPanel.
     */
    class IconButton extends JButton {
      /**
       * Creates an IconButton with the given image and tool tip text.
       * @param imageFileName
       * @param tip
       */
      IconButton(String imageFileName, String tip) {
        super(new ImageIcon(imageFileName));
        setOpaque(false);
        setPreferredSize(ICON_SIZE);
        setToolTipText(tip);
      }
    }
  }
}
