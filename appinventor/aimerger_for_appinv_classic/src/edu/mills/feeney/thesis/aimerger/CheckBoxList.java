package edu.mills.feeney.thesis.aimerger;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

/**
 * Displays the list of checkboxes. 
 * 
 * Informed by code at www.devx.com/tips/Tip/5342 by Trevor Harmon, Febuary 10, 1999.
 * 
 * @author feeney.kate@gmail.com (Kate Feeney)
 *
 */
public class CheckBoxList extends JList {
  private LinkedList<String> checked = new LinkedList<String>();
  protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

  public CheckBoxList() {
    setCellRenderer(new CellRenderer());
    // A mouse listener for when the mouse clicks a checkbox
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        // Get index of box clicked.
        int index = locationToIndex(e.getPoint());
        if (index != -1) {
          JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
          // For all checkboxes other than Screen1 toggle. Screen1 is not selectable since
          // Screen1 from the main project is automatically merged.
          if (!checkbox.getText().equals("Screen1")) {
            checkbox.setSelected(!checkbox.isSelected());
            if (checkbox.isEnabled() && !checked.contains(checkbox.getText())) {
              checked.add(checkbox.getText());
            } else if (checked.contains(checkbox.getText())) {
              checked.remove(checkbox.getText());
            }
          }
          repaint();
        }
      }
    });
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  }

  public LinkedList<String> getChecked() {
    return checked;
  }
  
  /**
   * Clears the list of checked items.
   */
  public void clearChecked() {
    checked.clear();
  }

  protected class CellRenderer implements ListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
      JCheckBox checkbox = (JCheckBox) value;
      checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
      checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
      checkbox.setEnabled(isEnabled());
      checkbox.setFont(getFont());
      checkbox.setFocusPainted(false);
      checkbox.setBorderPainted(true);
      checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder")
          : noFocusBorder);
      if (checkbox.getText().equals("Screen1")) {
        checkbox.setForeground(Color.gray);
      }
      return checkbox;
    }
  }
}