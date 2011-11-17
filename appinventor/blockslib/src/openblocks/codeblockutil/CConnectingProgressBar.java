// Copyright 2010 Google Inc. All Rights Reserved.

package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JProgressBar;

/**
 * A CConnectingProgressBar is an extension around the swing progress bar,
 * which provides methods to disable and re-enable its functionality and
 * update its appearance accordingly.
 *
 * @author kerr@google.com (Debby Wallach)
 *
 */
public class CConnectingProgressBar extends JProgressBar {

  private boolean live = false;

  public CConnectingProgressBar() {
    super(0, 100);
    setStringPainted(false);
    setVisible(false);
  }

  public void startProgress() {
    live = true;
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    setIndeterminate(true);
    setVisible(true);
    repaint();
  }

  // Returns false if the progress bar was not running
  public boolean endProgress() {
    if (! live) return false;
    setVisible(false);
    setCursor(null); // turn off the wait cursor
    setIndeterminate(false);
    repaint();
    live = false;
    return true;
  }
}
