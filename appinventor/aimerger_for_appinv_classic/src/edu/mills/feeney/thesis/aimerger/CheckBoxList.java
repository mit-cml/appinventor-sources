package edu.mills.feeney.thesis.aimerger;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

/**
 * Displays a list of checkboxes.
 * 
 * Informed by code at www.devx.com/tips/Tip/5342 by Trevor Harmon, February 10, 1999.
 * 
 * @author feeney.kate
 */
public class CheckBoxList extends JList<JCheckBox> {
  private static final String UNSELECTABLE_SCREEN = "Screen1";
  private final LinkedList<String> checked = new LinkedList<>();
  protected static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

  public CheckBoxList() {
    setCellRenderer(new CellRenderer());
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        int index = locationToIndex(e.getPoint());
        if (index != -1) {
          JCheckBox checkbox = getModel().getElementAt(index);
          String text = checkbox.getText();

          if (!text.equals(UNSELECTABLE_SCREEN)) {
            checkbox.setSelected(!checkbox.isSelected());

            if (checkbox.isSelected() && !checked.contains(text)) {
              checked.add(text);
            } else if (!checkbox.isSelected()) {
              checked.remove(text);
            }
            repaint();
          }
        }
      }
    });
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

  private class CellRenderer implements ListCellRenderer<JCheckBox> {
    @Override
    public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
      value.setBackground(isSelected ? getSelectionBackground() : getBackground());
      value.setForeground(isSelected ? getSelectionForeground() : getForeground());
      value.setEnabled(isEnabled());
      value.setFont(getFont());
      value.setFocusPainted(false);
      value.setBorderPainted(true);
      value.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

      if (value.getText().equals(UNSELECTABLE_SCREEN)) {
        value.setForeground(Color.GRAY);
      }
      
      return value;
    }
  }
}
