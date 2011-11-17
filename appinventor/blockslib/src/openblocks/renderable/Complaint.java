package openblocks.renderable;


import openblocks.yacodeblocks.Escapers;
import openblocks.codeblocks.ComplaintDepartment;
import openblocks.codeblockutil.CGraphite;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Dimension;
import javax.swing.JTextArea;


/**
 *
 * Complaint stores and displays errors and warnings.
 *
 */
    public class Complaint extends BlockNote {
      private static final long serialVersionUID = 328149080425L;
      private static int INITIAL_WIDTH = 280;
      private static int INITIAL_HEIGHT = 60;
      private static int MAX_WIDTH = 400;

      /**
       * Constructs a Complaint
       * @param source block that the comment is linked to.
       */
    public Complaint(RenderableBlock source){
      this(new JTextArea(), source);
    }

    private Complaint(JTextArea label, RenderableBlock source) {
      super(label, source, "!", CGraphite.complaint_background, true,
          MAX_WIDTH, INITIAL_HEIGHT);
      textContent = label;
      textContent.setEditable(false);
      textContent.setLineWrap(true);
      textContent.setWrapStyleWord(true);

      fontMetrics = getFontMetrics(label.getFont());
      int textPadding = fontMetrics.stringWidth("M") * TEXT_PADDING_EMS;

      setBlockNoteSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));
      source.chooseLocationForBalloon(this, INITIAL_WIDTH, INITIAL_HEIGHT);
    }

    /**
     * @return The complaint's text
     */
    public String getText() {
      return textContent.getText();
    }

    /**
     * String representation of this
     */
     @Override
     public String toString() {
        return "Complaint ID: " + " at " + getLocation() + " with text: \""
            + getText() + "\"";
    }

    @Override
    public String getSaveString() {
      StringBuffer saveString = new StringBuffer();
      saveString.append("<Complaint>\n");
      saveString.append("<Text>");
      String textValue = getText().replaceAll("`", "'");
      // Encode complaints to preserve international characters.
      textValue = Escapers.encodeInternationalCharacters(textValue);
      saveString.append(Escapers.escapeForXml(textValue));
      saveString.append("</Text>");
      saveString.append(super.getSaveString());
      saveString.append("</Complaint>\n");
      return saveString.toString();
  }

    /**
     * Loads the BlockNote from a NodeList of parts
     * @param complaintChildren
     * @param rb
     * @return the BlockNote from a NodeList of Complaint/BlockNote parts
     */
    public static Complaint loadComplaint(NodeList complaintChildren, RenderableBlock rb) {
      Complaint complaint = new Complaint(rb);
      for (int j = 0; j < complaintChildren.getLength(); j++) {
        Node complaintChild = complaintChildren.item(j);
        if (complaintChild.getNodeName().equals("Text")) {
          String textValue = complaintChild.getTextContent();
          // Decode complaints to preserve international characters.
          textValue = Escapers.decodeInternationalCharacters(textValue);
          complaint.setText(textValue);
          break;
        }
      }
      loadBlockNote(complaintChildren, rb, complaint);
      complaint.getBlockNoteLabel().setBackground(
          complaint.getText().contains("Error") ? ComplaintDepartment.ERROR_COLOR
              : ComplaintDepartment.WARNING_COLOR);
      return complaint;
    }

}
