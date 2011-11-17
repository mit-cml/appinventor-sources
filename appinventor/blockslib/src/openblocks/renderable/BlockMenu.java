// Copyright 2010 Google Inc. All Rights Reserved.

package openblocks.renderable;

import openblocks.codeblocks.Block;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;
import openblocks.yacodeblocks.PhoneCommManager;
import openblocks.yacodeblocks.WorkspaceControllerHolder;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * BlockMenu performs operations for a block, currently in response to right clicks
 */

public class BlockMenu implements ActionListener {

  private static final String ADD_COMMENT = "Add Comment";
  private static final String REMOVE_COMMENT = "Remove Comment";
  private static final String DEACTIVATE = "Deactivate";
  private static final String REMOVE_COMPLAINT = "Remove Complaint";
  private static final String ACTIVATE = "Activate";
  private static final String DO_IT = "Do It";
  private static final String REMOVE_REPORT = "Stop Watching";
  private static final String REPORT_EXECUTIONS = "Watch";
  private final PopupMenu popupMenu;
  private final MenuItem commentItem;
  private final MenuItem complaintItem;
  private final MenuItem reportItem;
  private final MenuItem ableItem;
  private final MenuItem doItItem;

  private final RenderableBlock renderable;

  /**
   * Constructs a new BlockMenu instance with the specified parent
   * WorkspaceWidget and
   *
   * @param renderable block
   */
  BlockMenu(RenderableBlock renderable){
    this.renderable = renderable;
    Block block = renderable.getBlock();
    popupMenu = new PopupMenu();

    commentItem = new MenuItem(ADD_COMMENT);
    commentItem.addActionListener(this);
    popupMenu.add(commentItem);

    ableItem = new MenuItem(DEACTIVATE);
    ableItem.addActionListener(this);
    if (!renderable.getBlock().hasPlug()) {
      popupMenu.add(ableItem);
    }

    reportItem = new MenuItem(REPORT_EXECUTIONS);
    reportItem.setActionCommand(REPORT_EXECUTIONS);
    reportItem.addActionListener(this);
    if (!(block.isProcedureDeclBlock() || block.isEventHandlerBlock())) {
      popupMenu.add(reportItem);
    }

    complaintItem = new MenuItem(REMOVE_COMPLAINT);
    complaintItem.setActionCommand(REMOVE_COMPLAINT);
    complaintItem.addActionListener(this);
    popupMenu.add(complaintItem);

    doItItem = new MenuItem(DO_IT);
    doItItem.setActionCommand(DO_IT);
    doItItem.addActionListener(this);
    if (!(block.isDeclaration() || block.isArgument())) {
      popupMenu.add(doItItem);
    }

    renderable.add(popupMenu);
  }

  void displayMenu(MouseEvent e) {
    PhoneCommManager rcm = WorkspaceControllerHolder.get().getPhoneCommManager();
    if (renderable.hasComment()) {
      commentItem.setLabel(REMOVE_COMMENT);
      commentItem.setActionCommand(REMOVE_COMMENT);
    } else {
      commentItem.setLabel(ADD_COMMENT);
      commentItem.setActionCommand(ADD_COMMENT);
    }

    if (renderable.activated()) {
      ableItem.setLabel(DEACTIVATE);
      ableItem.setActionCommand(DEACTIVATE);
    } else {
      ableItem.setLabel(ACTIVATE);
      ableItem.setActionCommand(ACTIVATE);
    }

    complaintItem.setEnabled(renderable.hasComplaint());

    if (renderable.getBlock().shouldReceiveReport()) {
      reportItem.setLabel(REMOVE_REPORT);
      reportItem.setActionCommand(REMOVE_REPORT);
    } else {
      reportItem.setLabel(REPORT_EXECUTIONS);
      reportItem.setActionCommand(REPORT_EXECUTIONS);
    }

    doItItem.setEnabled(rcm.connectedToPhone());

    popupMenu.show(renderable, e.getX(), e.getY());
  }

  public void actionPerformed(ActionEvent a) {
    if (a.getActionCommand() == ADD_COMMENT){
      Comment comment = renderable.getComment();
      comment.getBlockNoteLabel().setActive(true);
      comment.setVisible(true);
      comment.requestFocusInWindow();
    } else if (a.getActionCommand() == REMOVE_COMMENT){
      renderable.removeComment();
    } else if (a.getActionCommand() == REMOVE_COMPLAINT){
      renderable.removeComplaint();
    } else if (a.getActionCommand() == REMOVE_REPORT){
      renderable.getBlock().setShouldReceiveReport(false);
      Workspace.getInstance().notifyListeners(new WorkspaceEvent(renderable.getParentWidget(),
          renderable.getBlockID(), WorkspaceEvent.BLOCK_REPORT_CHANGE));
      renderable.removeReport();
    } else if (a.getActionCommand() == REPORT_EXECUTIONS){
      renderable.getBlock().setShouldReceiveReport(true);
      renderable.getReport().getBlockNoteLabel().setActive(false);
      Workspace.getInstance().notifyListeners(new WorkspaceEvent(renderable.getParentWidget(),
          renderable.getBlockID(), WorkspaceEvent.BLOCK_REPORT_CHANGE));
          // This will trigger re-compilation and transmission to phone for enclosing definition.
    } else if (a.getActionCommand() == DEACTIVATE){
      renderable.setActivate(false);
    } else if (a.getActionCommand() == ACTIVATE){
      renderable.setActivate(true);
    } else if (a.getActionCommand() == DO_IT) {
      Workspace.getInstance().notifyListeners(new WorkspaceEvent(renderable.getParentWidget(),
          renderable.getBlockID(), WorkspaceEvent.BLOCK_DO_IT));
    }
  }

}
