// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.codeblockutil;

import java.awt.Cursor;
// import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Provides a Swing component (extending JScrollPane) that can display
 * HTML-formatted text and clickable hyperlinks (using &lt;a href&gt;).
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class HTMLPane extends JScrollPane {
  final JEditorPane edPane;

  public HTMLPane(String msgText) {
    super();
    edPane = new JEditorPane();
    edPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
    edPane.setEditable(false);
    edPane.setContentType("text/html");
    edPane.setText("<html><body> " + msgText + " </body></html>");
    edPane.setBackground(this.getBackground());

    // TIP: Make the JOptionPane resizable using the HierarchyListener
    edPane.addHierarchyListener(new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        Window window = SwingUtilities.getWindowAncestor(edPane);
        if (window instanceof Dialog) {
          Dialog dialog = (Dialog)window;
          if (!dialog.isResizable()) {
            dialog.setResizable(true);
          }
        }
      }
    });

    edPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              // TIP: Show hand cursor
              SwingUtilities.getWindowAncestor(edPane).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              // TIP: Show URL as the tooltip
              edPane.setToolTipText(e.getURL().toExternalForm());
            }
          });
        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              // Show default cursor
              SwingUtilities.getWindowAncestor(edPane).setCursor(Cursor.getDefaultCursor());

              // Reset tooltip
              edPane.setToolTipText(null);
            }
          });
        } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          /* TODO(sharon): We still require Java 5 compatability for codeblocks, so need
           * another way to do this - probably via reflection
          // TIP: Starting with JDK6 you can show the URL in desktop browser
          if (Desktop.isDesktopSupported()) {
            try {
              Desktop.getDesktop().browse(e.getURL().toURI());
            } catch (Exception ex) {
            }
          }
          //System.out.println("Go to URL: " + e.getURL());
           */
        }
      }});
    setViewportView(edPane);
  }
}
