// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.codeblocks;

import openblocks.renderable.Complaint;
import openblocks.renderable.RenderableBlock;
import openblocks.workspace.Workspace;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the recording and display of errors and warnings about code blocks.
 *
 */
public class ComplaintDepartment {
  // Error Strings
  public static final String EMPTY_SOCKET = "This block has an empty socket.";
  public static final String UNATTACHED = "This block is unattached.";
  public static final String BAD_INITIALIZER =
    "Global variable definitions cannot reference components or other global variables. " +
    "The Blocks Editor will accept this block, but the app will " +
    "signal an error when it starts, and you cannot package it for the phone. " +
    "To make the variable's initial value reference another " +
    "variable or a component, define the variable with some arbitrary initial value, say 0, " +
    "and set the correct initial value in the screen initialization event handler.";
  public static final String DUPLICATE_HANDLER = "This is a duplicate event handler.";
  public static final String UNBOUND_VARIABLE = "This argument has no value now.";
  public static final String MISSING_VARIABLE = "An argument name for this block is missing.";

  public static final Color ERROR_COLOR = Color.RED;
  public static final Color WARNING_COLOR = Color.YELLOW;

  private static final List<String> compileErrors = new ArrayList<String>();
  private static Rectangle complaintRect;
  private long blockID;

  public ComplaintDepartment(long bID) {
    blockID = bID;
  }

  public static void clearComplaints() {
    for (RenderableBlock rb : RenderableBlock.getAllRenderableBlocks()) {
      if (rb.hasComplaint()) {
        rb.removeComplaint();
      }
    }
    compileErrors.clear();
    complaintRect = new Rectangle(0, 0, -1, -1);
  }

  /**
   * Called from various places that find fault with a block during  yail
   * production. It puts a message in the block's Complaint balloon.
   *
   * @param complaint
   * @param severe true means fatal error
   */
   public void complain(String complaint, boolean severe)  {
    if (severe) {
      compileErrors.add(complaint);
    }
    RenderableBlock rb = RenderableBlock.getRenderableBlock(blockID);
    String message = (severe ? "Error" : "Warning") + ": " + complaint + "\n";
    Complaint balloon = rb.getComplaint();
    if (!balloon.getText().contains(message)) {
      balloon.setText(message + balloon.getText());
    }
    balloon.getBlockNoteLabel().setActive(severe);
    balloon.getBlockNoteLabel().setBackground(severe ? ERROR_COLOR : WARNING_COLOR);
    if (!rb.isVisible()) {
      // The only way this block is not visible is if it belongs to a collapsed
      // clump. Expand the whole clump so user can see the complaint.
      rb.getTopmost().setCollapsed(false);
    }
    complaintRect = complaintRect.union(rb.getBounds().union(balloon.getBounds()));
    balloon.reformBlockNote();
  }


  /* Called from yacodeblocks.WorkSpaceController at the end of compilation.
   *
   * @return number of errors
   */
  public static int showCompiletimeComplaints() {
    //TODO(user) scrollToShowRectangle doesn't seem to work properly at the Swing level
    Workspace.getInstance().getBlockCanvas().scrollToShowRectangle(complaintRect);
    return compileErrors.size();
  }

  /* Called from build server after generating YAIL.
   *
   * @return the compile errors as an array of String
   */
  public static String[] getCompileErrors() {
    return compileErrors.toArray(new String[compileErrors.size()]);
  }

  public static void showRuntimeComplaint(RenderableBlock rb, String complaint) {
    Complaint balloon = rb.getComplaint();
    balloon.getBlockNoteLabel().setActive(true);
    if (!rb.isVisible()) {
      rb.getTopmost().setCollapsed(false);
    }
    balloon.setText("Error: " + complaint + "\n");
    balloon.getBlockNoteLabel().setActive(true);
    balloon.getBlockNoteLabel().setBackground(ERROR_COLOR);
    complaintRect = rb.getBounds().union(balloon.getBounds());
    balloon.reformBlockNote();
    //TODO(user) scrollToShowRectangle doesn't seem to work properly at the Swing level
    Workspace.getInstance().getBlockCanvas().scrollToShowRectangle(complaintRect);
  }

  public void removeComplaint(String complaint) {
    RenderableBlock rb = RenderableBlock.getRenderableBlock(blockID);
    if (rb.hasComplaint()) {
      Complaint balloon = rb.getComplaint();
      String ct = balloon.getText().replaceFirst("(?s)(Error|Warning):" + complaint + "\n",
                                                 "");
      if (ct.length() == 0) {
        rb.removeComplaint();
      } else {
        balloon.setText(ct);
        balloon.reformBlockNote();
      }
    }
  }

}
