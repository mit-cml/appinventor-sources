package edu.mills.feeney.thesis.aimerger;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Displays a list of checkboxes.
 * 
 * Informed by code at www.devx.com/tips/Tip/5342 by Trevor Harmon, February 10, 1999.
 * 
 * @author feeney.kate
 */
public class CheckBoxList extends JList<JCheckBox> {
    private static final String UNSELECTABLE_SCREEN = "Screen1";
    private final Set<String> checkedItems = new HashSet<>();
    protected static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    public CheckBoxList() {
        setCellRenderer(new CheckBoxRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e);
            }
        });
    }

    /**
     * Handles mouse clicks to toggle checkbox selection, except for unselectable screens.
     */
    private void handleMouseClick(MouseEvent e) {
        int index = locationToIndex(e.getPoint());
        if (index != -1) {
            JCheckBox checkbox = getModel().getElementAt(index);
            toggleCheckboxSelection(checkbox);
            repaint();
        }
    }

    /**
     * Toggles the selection of a checkbox, updating the checked items list.
     *
     * @param checkbox the checkbox to toggle
     */
    private void toggleCheckboxSelection(JCheckBox checkbox) {
        String text = checkbox.getText();

        if (!UNSELECTABLE_SCREEN.equals(text)) {
            checkbox.setSelected(!checkbox.isSelected());
            if (checkbox.isSelected()) {
                checkedItems.add(text);
            } else {
                checkedItems.remove(text);
            }
        }
    }

    /**
     * Returns the list of checked items.
     * 
     * @return a set of checked item texts
     */
    public Set<String> getCheckedItems() {
        return new HashSet<>(checkedItems);
    }

    /**
     * Clears the list of checked items.
     */
    public void clearCheckedItems() {
        checkedItems.clear();
        for (int i = 0; i < getModel().getSize(); i++) {
            getModel().getElementAt(i).setSelected(false);
        }
        repaint();
    }

    /**
     * Renders each checkbox in the list, adjusting appearance for unselectable screens.
     */
    private class CheckBoxRenderer implements ListCellRenderer<JCheckBox> {
        @Override
        public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox checkbox,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
            checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusPainted(false);
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

            if (UNSELECTABLE_SCREEN.equals(checkbox.getText())) {
                checkbox.setForeground(Color.GRAY);
            }

            return checkbox;
        }
    }
}
