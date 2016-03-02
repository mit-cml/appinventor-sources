// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.component.ComponentComparators;
import com.google.appinventor.client.explorer.component.ComponentManagerEventListener;
import com.google.appinventor.shared.rpc.component.Component;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The component list shows all components in a table.
 *
 * <p> The component name and version will be shown in the table.
 *
 */
public class ComponentList extends Composite implements ComponentManagerEventListener {
  private enum SortField {
    NAME,
    VERSION
  }
  private enum SortOrder {
    ASCENDING,
    DESCENDING,
  }

  private final List<Component> components;
  private final List<Component> selectedComponents;
  private final Map<Component, ComponentWidgets> componentWidgets;
  private SortField sortField;
  private SortOrder sortOrder;

  private final Grid table;
  private final Label nameSortIndicator;
  private final Label versionSortIndicator;

  public ComponentList() {
    components = new ArrayList<Component>();
    selectedComponents = new ArrayList<Component>();
    componentWidgets = new HashMap<Component, ComponentWidgets>();

    sortField = SortField.NAME;
    sortOrder = SortOrder.ASCENDING;

    table = new Grid(1, 3); // The table initially contains just the header row.
    table.addStyleName("ode-ComponentTable");
    table.setWidth("100%");
    table.setCellSpacing(0);
    nameSortIndicator = new Label("");
    versionSortIndicator = new Label("");
    refreshSortIndicators();
    setHeaderRow();

    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");

    panel.add(table);
    initWidget(panel);

    // It is important to listen to component manager events as soon as possible.
    Ode.getInstance().getComponentManager().addEventListener(this);
  }

  /**
   * Adds the header row to the table.
   *
   */
  private void setHeaderRow() {
    table.getRowFormatter().setStyleName(0, "ode-ComponentHeaderRow");

    HorizontalPanel nameHeader = new HorizontalPanel();
    final Label nameHeaderLabel = new Label(MESSAGES.componentNameHeader());
    nameHeaderLabel.addStyleName("ode-ComponentHeaderLabel");
    nameHeader.add(nameHeaderLabel);
    nameSortIndicator.addStyleName("ode-ComponentHeaderLabel");
    nameHeader.add(nameSortIndicator);
    table.setWidget(0, 1, nameHeader);

    HorizontalPanel versionHeader = new HorizontalPanel();
    final Label versionHeaderLabel = new Label(MESSAGES.componentVersionHeader());
    versionHeaderLabel.addStyleName("ode-ComponentHeaderLabel");
    versionHeader.add(versionHeaderLabel);
    versionSortIndicator.addStyleName("ode-ComponentHeaderLabel");
    versionHeader.add(versionSortIndicator);
    table.setWidget(0, 2, versionHeader);

    MouseDownHandler mouseDownHandler = new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent e) {
        SortField clickedSortField;
        if (e.getSource() == nameHeaderLabel || e.getSource() == nameSortIndicator) {
          clickedSortField = SortField.NAME;
        } else {
          clickedSortField = SortField.VERSION;
        }
        changeSortOrder(clickedSortField);
      }
    };
    nameHeaderLabel.addMouseDownHandler(mouseDownHandler);
    nameSortIndicator.addMouseDownHandler(mouseDownHandler);
    versionHeaderLabel.addMouseDownHandler(mouseDownHandler);
    versionSortIndicator.addMouseDownHandler(mouseDownHandler);
  }

  private void changeSortOrder(SortField clickedSortField) {
    if (sortField != clickedSortField) {
      sortField = clickedSortField;
      sortOrder = SortOrder.ASCENDING;
    } else {
      if (sortOrder == SortOrder.ASCENDING) {
        sortOrder = SortOrder.DESCENDING;
      } else {
        sortOrder = SortOrder.ASCENDING;
      }
    }
    refreshTable(true);
  }

  private void refreshSortIndicators() {
    String text = (sortOrder == SortOrder.ASCENDING)
        ? "\u25B2"      // up-pointing triangle
        : "\u25BC";     // down-pointing triangle
    switch (sortField) {
      case NAME:
        nameSortIndicator.setText(text);
        versionSortIndicator.setText("");
        break;
      case VERSION:
        nameSortIndicator.setText("");
        versionSortIndicator.setText(text);
    }
  }

  private class ComponentWidgets {
    final CheckBox checkBox;
    final Label nameLabel;
    final Label versionLabel;

    private ComponentWidgets(final Component component) {
      checkBox = new CheckBox();
      checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
          int row = 1 + components.indexOf(component);
          if (isChecked) {
            table.getRowFormatter().setStyleName(row, "ode-ComponentRowHighlighted");
            selectedComponents.add(component);
          } else {
            table.getRowFormatter().setStyleName(row, "ode-ComponentRowUnHighlighted");
            selectedComponents.remove(component);
          }
          Ode.getInstance().getComponentToolbar().updateButtons();
        }
      });

      nameLabel = new Label(component.getName());
      nameLabel.addStyleName("ode-ComponentNameLabel");

      versionLabel = new Label(Long.toString(component.getVersion()));
    }
  }

  private void refreshTable(boolean needToSort) {
    if (needToSort) {
      Comparator<Component> comparator;
      switch (sortField) {
        default:
        case NAME:
          comparator = (sortOrder == SortOrder.ASCENDING)
              ? ComponentComparators.COMPARE_BY_NAME_ASCENDING
              : ComponentComparators.COMPARE_BY_NAME_DESCENDING;
          break;
        case VERSION:
          comparator = (sortOrder == SortOrder.ASCENDING)
              ? ComponentComparators.COMPARE_BY_VERSION_ASCENDING
              : ComponentComparators.COMPARE_BY_VERSION_DESCENDING;
          break;
      }
      Collections.sort(components, comparator);
    }

    refreshSortIndicators();

    // Refill the table.
    table.resize(1 + components.size(), 5);
    int row = 1;
    for (Component component : components) {
      ComponentWidgets cw = componentWidgets.get(component);
      if (selectedComponents.contains(component)) {
        table.getRowFormatter().setStyleName(row, "ode-ComponentRowHighlighted");
        cw.checkBox.setValue(true);
      } else {
        table.getRowFormatter().setStyleName(row, "ode-ComponentRowUnHighlighted");
        cw.checkBox.setValue(false);
      }
      table.setWidget(row, 0, cw.checkBox);
      table.setWidget(row, 1, cw.nameLabel);
      table.setWidget(row, 2, cw.versionLabel);

      row++;
    }

    Ode.getInstance().getComponentToolbar().updateButtons();
  }

  /**
   * Gets the number of selected components
   *
   * @return the number of selected components
   */
  public int getNumSelectedComponents() {
    return selectedComponents.size();
  }

  /**
   * Returns the list of selected components
   *
   * @return the selected components
   */
  public List<Component> getSelectedComponents() {
    return selectedComponents;
  }

  // ComponentManagerEventListener implementation

  @Override
  public void onComponentAdded(Component component) {
    components.add(component);
    componentWidgets.put(component, new ComponentWidgets(component));
    refreshTable(true);
  }

  @Override
  public void onComponentRemoved(Component component) {
    components.remove(component);
    componentWidgets.remove(component);
    selectedComponents.remove(component);

    refreshTable(false);

    Ode.getInstance().getComponentToolbar().updateButtons();
  }

  @Override
  public void onComponentsLoaded() {
    components.clear();
    selectedComponents.clear();
    componentWidgets.clear();

    for (Component comp : Ode.getInstance().getComponentManager().getComponents()) {
      components.add(comp);
      componentWidgets.put(comp, new ComponentWidgets(comp));
    }

    refreshTable(true);
  }
}
