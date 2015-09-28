package edu.mills.feeney.thesis.aimerger;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

/**
 * Displays the list of checkboxes.
 * <p/>
 * Informed by code at www.devx.com/tips/Tip/5342 by Trevor Harmon, Febuary 10, 1999.
 *
 * @author feeney.kate@gmail.com (Kate Feeney)
 *         <p/>
 *         Modified by Arezu Esmaili (arezuesmaili1@gmail.com) - July 2015
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
          if (!checkbox.getText().equals("Check All")) {
            // Toggle for all checkboxes.
            checkbox.setSelected(!checkbox.isSelected());
            if (checkbox.isEnabled() && !checked.contains(checkbox.getText())) {
              checked.add(checkbox.getText());
            } else if (checked.contains(checkbox.getText())) {
              checked.remove(checkbox.getText());
            }
          } else {
            if (!checked.contains(checkbox.getText())) {
              checkAll();
            } else {
              clearAll();
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

  /**
   * Checks all checkboxes.
   */
  public void checkAll() {
    for (int i = 0; i < getModel().getSize(); i++) {
      JCheckBox checkbox = (JCheckBox) getModel().getElementAt(i);
      if (checkbox.isEnabled() && !checked.contains(checkbox.getText())) {
        checkbox.setSelected(true);
        checked.add(checkbox.getText());
      }
      repaint();
    }
  }

  /**
   * Clears all checkboxes.
   */
  public void clearAll() {
    for (int i = 0; i < getModel().getSize(); i++) {
      JCheckBox checkbox = (JCheckBox) getModel().getElementAt(i);
      if (checkbox.isEnabled() && checked.contains(checkbox.getText())) {
        checkbox.setSelected(false);
        checked.remove(checkbox.getText());
      }
      repaint();
    }
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
      return checkbox;
    }

  }
}