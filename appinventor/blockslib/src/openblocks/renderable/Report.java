// Copyright 2010 Google Inc. All Rights Reserved.

package openblocks.renderable;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JTextArea;


/**
 * Report stores strings that come from the phone.
 *
 *
 */

public class Report extends BlockNote {

  private static int INITIAL_WIDTH = 120;
  private static int INITIAL_HEIGHT = 40;
  private static int MAX_WIDTH = 500;


    /**
     * Constructs a Report
     * @param source block that the report is linked to.
     */
    public Report(RenderableBlock source){
      this(new JTextArea(), source);
    }

  // TODO(halabelson): This has some duplicate code with Complaint and Report, and
    // even Comment, that could be moved up into BlockNote, but it's not clear how
    // similar we want these notes to ultimately look.
    //

    private Report(JTextArea text, RenderableBlock source) {
      super(text, source, "=", Color.WHITE, false, MAX_WIDTH, INITIAL_HEIGHT);
      textContent = text;
      textContent.setEditable(false);
      textContent.setLineWrap(true);
      textContent.setWrapStyleWord(true);
      textContent.setFont(new Font(FONT_NAME, Font.BOLD, (int) (FONT_SIZE * zoom)));
      fontMetrics = getFontMetrics(text.getFont());
      textPadding = fontMetrics.stringWidth("M") * TEXT_PADDING_EMS;
      source.chooseLocationForBalloon(this, INITIAL_WIDTH, INITIAL_HEIGHT);
      setBlockNoteSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));
      blockNoteLabel.setBackground(Color.WHITE);
    }


    public String getText() {
      return textContent.getText();
    }

    /**
     * String representation of this
     */
     @Override
     public String toString() {
        return "Report ID: " + " at " + getLocation() + " with text: \""
                                   + textContent.getText() + "\"";
    }

     @Override
     public String getSaveString() {
       return "";  // For the moment, Reports are evanescent.
     }

    /**
     * Loads the BlockNote from a NodeList of parts
     * ... in case we decide to save them someday, this may be called.
     * @param reportChildren
     * @param rb
     * @return the BlockNote from a NodeList of Report/BlockNote parts
     */
    public static Report loadReport(NodeList reportChildren,
        RenderableBlock rb) {
      final Report report = new Report(rb);
      for (int j = 0; j < reportChildren.getLength(); j++) {
        Node reportChild = reportChildren.item(j);
        if (reportChild.getNodeName().equals("Text")) {
          report.setText(reportChild.getTextContent());
        }
      }
      loadBlockNote(reportChildren, rb, report);
      return report;
    }

}
