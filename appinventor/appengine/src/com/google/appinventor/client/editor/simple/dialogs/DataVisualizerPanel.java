// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.dialogs;

import com.google.appinventor.client.editor.simple.DataStoreProvider;
import com.google.appinventor.client.wizards.Dialog;
import com.google.appinventor.shared.rpc.clouddb.DataEntry;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import java.util.List;

/**
 * Floating, non-modal panel that displays the tag/value contents of a data-store
 * component (e.g. CloudDB) at design time.
 *
 * <h3>WCAG 2.1 compliance</h3>
 * <ul>
 *   <li>Extends {@link Dialog}, which provides {@code role="dialog"},
 *       {@code aria-modal}, automatic focus management, and Escape-key handling.</li>
 *   <li>{@link #configureAria} wires {@code aria-labelledby} to the caption element.</li>
 *   <li>The status area carries {@code role="status"} and {@code aria-live="polite"} so
 *       screen readers announce loading and error states without interrupting.</li>
 *   <li>The data table uses semantic {@code role="table"} / {@code role="columnheader"} /
 *       {@code role="row"} / {@code role="cell"} roles and {@code scope="col"} on header
 *       cells.</li>
 *   <li>Action buttons carry explicit {@code aria-label} attributes.</li>
 *   <li>All colours are drawn from the existing theming CSS variables so the panel
 *       respects both light and dark modes, keeping contrast ratios at WCAG AA levels.</li>
 * </ul>
 *
 * <p>Usage: {@code DataVisualizerPanel.show(provider)}.  The panel is a singleton per
 * browser session; subsequent calls update the provider and refresh the data.
 */
public final class DataVisualizerPanel extends Dialog {

  private static DataVisualizerPanel instance;

  private final Label statusLabel;   // loading / error / empty messages
  private final FlexTable dataTable; // tag | value | type rows
  private final Label countLabel;    // "N entries"

  private DataStoreProvider currentProvider;

  // ---- Table column indices ----
  private static final int COL_TAG   = 0;
  private static final int COL_VALUE = 1;
  private static final int COL_TYPE  = 2;
  private static final int HEADER_ROW = 0;

  /**
   * Shows (or updates) the visualizer panel for the given provider.
   * Creates the panel on first call; subsequent calls reuse the instance.
   *
   * @param provider the data-store component whose entries should be displayed
   */
  public static void show(DataStoreProvider provider) {
    if (instance == null) {
      instance = new DataVisualizerPanel();
    }
    instance.setProvider(provider);
    instance.show();
    instance.refresh();
  }

  private DataVisualizerPanel() {
    // Dialog defaults: draggable, non-modal, glass disabled.
    setModal(false);
    setGlassEnabled(false);

    statusLabel = new Label();
    statusLabel.addStyleName("ode-InfoMessage");
    statusLabel.getElement().setAttribute("role", "status");
    statusLabel.getElement().setAttribute("aria-live", "polite");
    statusLabel.getElement().setAttribute("aria-atomic", "true");

    dataTable = new FlexTable();
    dataTable.addStyleName("ode-ComponentTable");
    dataTable.getElement().setAttribute("role", "table");
    buildTableHeader();

    countLabel = new Label();
    countLabel.addStyleName("ode-ProjectFieldLabel");

    Button refreshButton = new Button("↻ Refresh");
    refreshButton.addStyleName("gwt-Button");
    refreshButton.getElement().setAttribute("aria-label", "Refresh data from store");
    refreshButton.addClickHandler(event -> refresh());

    Button closeButton = new Button("Close");
    closeButton.addStyleName("gwt-Button");
    closeButton.getElement().setAttribute("aria-label", "Close visualizer panel");
    closeButton.addClickHandler(event -> hide());

    FlowPanel footer = new FlowPanel();
    footer.addStyleName("ode-Android-footer");
    footer.getElement().getStyle().setProperty("padding", "4px 8px");
    footer.getElement().getStyle().setProperty("display", "flex");
    footer.getElement().getStyle().setProperty("alignItems", "center");
    footer.getElement().getStyle().setProperty("gap", "8px");
    footer.add(countLabel);
    // Push buttons to right
    HTML spacer = new HTML("&nbsp;");
    spacer.getElement().getStyle().setProperty("flex", "1");
    footer.add(spacer);
    footer.add(refreshButton);
    footer.add(closeButton);

    FlowPanel tableArea = new FlowPanel();
    tableArea.getElement().getStyle().setProperty("overflowY", "auto");
    tableArea.getElement().getStyle().setProperty("flex", "1");
    tableArea.getElement().getStyle().setProperty("minHeight", "0");
    tableArea.add(dataTable);
    tableArea.add(statusLabel);

    // resize:both requires overflow != visible and explicit dimensions.
    // No extra CSS class here — the Dialog already provides its own chrome.
    FlowPanel root = new FlowPanel();
    root.getElement().getStyle().setProperty("display", "flex");
    root.getElement().getStyle().setProperty("flexDirection", "column");
    root.getElement().getStyle().setProperty("width", "740px");
    root.getElement().getStyle().setProperty("height", "460px");
    root.getElement().getStyle().setProperty("minWidth", "400px");
    root.getElement().getStyle().setProperty("minHeight", "180px");
    root.getElement().getStyle().setProperty("resize", "both");
    root.getElement().getStyle().setProperty("overflow", "hidden");
    root.getElement().getStyle().setProperty("boxSizing", "border-box");
    root.getElement().getStyle().setProperty("paddingBottom", "20px");
    root.getElement().setAttribute("aria-describedby", getStatusLabelId());

    root.add(tableArea);
    root.add(footer);

    setWidget(root);

    // Wire up ARIA labelledby to the caption element created by Dialog/DialogBox.
    configureAria("CloudDB Data Visualizer");
  }

  private void setProvider(DataStoreProvider provider) {
    this.currentProvider = provider;
    setCaption("Data: " + provider.getDataStoreName()
        + " (" + provider.getDataStoreType() + ")");
    // Re-wire aria-labelledby after caption text changes (caption element ID is stable).
    configureAria(null);
  }

  /**
   * Fetches fresh data from the current provider and repopulates the table.
   * Announced to screen readers via the aria-live status region.
   */
  public void refresh() {
    if (currentProvider == null) {
      return;
    }
    setStatus("Loading…");
    clearDataRows();

    currentProvider.fetchEntries(new AsyncCallback<List<DataEntry>>() {
      @Override
      public void onSuccess(List<DataEntry> entries) {
        clearDataRows();
        if (entries.isEmpty()) {
          setStatus("No data found for this project ID.");
          countLabel.setText("0 entries");
        } else {
          setStatus("");
          for (DataEntry entry : entries) {
            addDataRow(entry.getTag(), entry.getValue());
          }
          countLabel.setText(entries.size() + " entr" + (entries.size() == 1 ? "y" : "ies"));
        }
      }

      @Override
      public void onFailure(Throwable caught) {
        setStatus("Error: " + caught.getMessage());
        countLabel.setText("");
      }
    });
  }

  private void buildTableHeader() {
    FlexCellFormatter fmt = dataTable.getFlexCellFormatter();

    dataTable.setText(HEADER_ROW, COL_TAG,   "Tag");
    dataTable.setText(HEADER_ROW, COL_VALUE, "Value");
    dataTable.setText(HEADER_ROW, COL_TYPE,  "Type");

    for (int col = 0; col < 3; col++) {
      com.google.gwt.dom.client.Element th = dataTable.getCellFormatter()
          .getElement(HEADER_ROW, col);
      th.setAttribute("role", "columnheader");
      th.setAttribute("scope", "col");
      th.addClassName("ode-ComponentHeaderLabel");
    }

    dataTable.getRowFormatter().addStyleName(HEADER_ROW, "ode-ComponentHeaderRow");

    fmt.setWidth(HEADER_ROW, COL_TAG,   "30%");
    fmt.setWidth(HEADER_ROW, COL_VALUE, "55%");
    fmt.setWidth(HEADER_ROW, COL_TYPE,  "15%");
  }

  private void clearDataRows() {
    while (dataTable.getRowCount() > 1) {
      dataTable.removeRow(1);
    }
  }

  private void addDataRow(String tag, String value) {
    int row = dataTable.getRowCount();
    String styleClass = (row % 2 == 0) ? "ode-ComponentRowUnHighlighted" : "ode-ComponentRowHighlighted";

    dataTable.setText(row, COL_TAG,   tag);
    dataTable.setText(row, COL_VALUE, formatValue(value));
    dataTable.setText(row, COL_TYPE,  detectType(value));

    dataTable.getRowFormatter().addStyleName(row, styleClass);

    dataTable.getRowFormatter().getElement(row).setAttribute("role", "row");
    for (int col = 0; col < 3; col++) {
      dataTable.getCellFormatter().getElement(row, col).setAttribute("role", "cell");
    }
    dataTable.getCellFormatter().addStyleName(row, COL_VALUE, "ode-ComponentNameLabel");
    dataTable.getCellFormatter().getElement(row, COL_VALUE)
        .getStyle().setProperty("wordBreak", "break-word");
  }

  /**
   * Trims JSON strings to a displayable length without truncating the type hint.
   */
  private static String formatValue(String raw) {
    if (raw == null || raw.isEmpty()) return "(empty)";
    if (raw.length() > 120) {
      return raw.substring(0, 117) + "…";
    }
    return raw;
  }

  /**
   * Infers a human-readable type label from the JSON value string.
   * Values in CloudDB are always JSON-encoded.
   */
  private static String detectType(String value) {
    if (value == null || value.isEmpty()) return "";
    String v = value.trim();
    if (v.startsWith("["))  return "list";
    if (v.startsWith("{"))  return "dict";
    if ("true".equals(v) || "false".equals(v)) return "boolean";
    if (v.startsWith("\"")) return "text";
    try {
      Double.parseDouble(v);
      return "number";
    } catch (NumberFormatException e) {
      return "text";
    }
  }

  private void setStatus(String message) {
    statusLabel.setText(message);
    // Visibility: hide the label div when empty so it doesn't occupy space,
    // but keep it in the DOM (aria-live regions must stay in the DOM).
    statusLabel.setVisible(message != null && !message.isEmpty());
  }

  private String getStatusLabelId() {
    String id = statusLabel.getElement().getId();
    if (id == null || id.isEmpty()) {
      id = Document.get().createUniqueId();
      statusLabel.getElement().setId(id);
    }
    return id;
  }
}
