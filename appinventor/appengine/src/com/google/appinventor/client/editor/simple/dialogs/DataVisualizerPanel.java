// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.dialogs;

import com.google.appinventor.client.editor.simple.DataStoreProvider;
import com.google.appinventor.client.editor.simple.MutableDataStoreProvider;
import com.google.appinventor.client.utils.MessageDialog;
import com.google.appinventor.client.wizards.Dialog;
import com.google.appinventor.shared.rpc.clouddb.DataEntry;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Floating, non-modal panel that displays the tag/value contents of a data-store
 * component (e.g. CloudDB) at design time. When the provider implements
 * {@link MutableDataStoreProvider}, the panel also allows creating, editing, and
 * deleting entries directly from the designer.
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
  private final FlexTable dataTable; // tag | value | type | actions rows
  private final Label countLabel;    // "N entries" / "N of M entries"
  private Button headerAddButton;    // "Add" button in the actions column header

  private DataStoreProvider currentProvider;

  // ---- Table column indices ----
  private static final int COL_TAG     = 0;
  private static final int COL_VALUE   = 1;
  private static final int COL_TYPE    = 2;
  private static final int COL_ACTIONS = 3;
  private static final int HEADER_ROW  = 0;

  // ---- Filter + sort state ----
  private enum SortField { TAG, TYPE }
  private enum SortOrder { ASCENDING, DESCENDING }

  private List<DataEntry> allEntries = new ArrayList<>();
  private String filterText = "";
  private SortField sortField = null;
  private SortOrder sortOrder = SortOrder.ASCENDING;

  private TextBox filterBox;
  private InlineLabel tagSortAsc, tagSortDec;
  private InlineLabel typeSortAsc, typeSortDec;

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

    Button refreshButton = new Button("\u21BB Refresh");
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

    filterBox = new TextBox();
    filterBox.getElement().setAttribute("type", "search");
    filterBox.getElement().setAttribute("placeholder", "Search tags, values, types\u2026");
    filterBox.getElement().setAttribute("aria-label", "Filter entries");
    filterBox.getElement().getStyle().setProperty("width", "100%");
    filterBox.getElement().getStyle().setProperty("boxSizing", "border-box");
    filterBox.getElement().getStyle().setProperty("marginBottom", "4px");
    filterBox.addKeyDownHandler(event -> event.stopPropagation());
    filterBox.addKeyUpHandler(event -> {
      filterText = filterBox.getValue();
      applyFilterAndSort();
    });

    FlowPanel searchBar = new FlowPanel();
    searchBar.getElement().getStyle().setProperty("padding", "4px 8px 0 8px");
    searchBar.add(filterBox);

    FlowPanel tableArea = new FlowPanel();
    tableArea.getElement().getStyle().setProperty("overflowY", "auto");
    tableArea.getElement().getStyle().setProperty("flex", "1");
    tableArea.getElement().getStyle().setProperty("minHeight", "0");
    tableArea.add(searchBar);
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
    // Show the Add button only for mutable providers.
    if (headerAddButton != null) {
      headerAddButton.setVisible(provider instanceof MutableDataStoreProvider);
    }
  }

  /**
   * Fetches fresh data from the current provider and repopulates the table.
   * Announced to screen readers via the aria-live status region.
   */
  public void refresh() {
    if (currentProvider == null) {
      return;
    }
    setStatus("Loading\u2026");
    clearDataRows();

    currentProvider.fetchEntries(new AsyncCallback<List<DataEntry>>() {
      @Override
      public void onSuccess(List<DataEntry> entries) {
        allEntries = new ArrayList<>(entries);
        applyFilterAndSort();
      }

      @Override
      public void onFailure(Throwable caught) {
        setStatus("Error: " + caught.getMessage());
        countLabel.setText("");
      }
    });
  }

  private void applyFilterAndSort() {
    clearDataRows();
    String query = filterText == null ? "" : filterText.trim().toLowerCase();

    List<DataEntry> filtered = new ArrayList<>();
    for (DataEntry entry : allEntries) {
      if (query.isEmpty()) {
        filtered.add(entry);
      } else {
        String tag = entry.getTag() != null ? entry.getTag().toLowerCase() : "";
        String val = formatValue(entry.getValue()).toLowerCase();
        String type = detectType(entry.getValue()).toLowerCase();
        if (tag.contains(query) || val.contains(query) || type.contains(query)) {
          filtered.add(entry);
        }
      }
    }

    if (sortField != null) {
      Collections.sort(filtered, buildComparator());
    }

    if (filtered.isEmpty()) {
      setStatus(query.isEmpty()
          ? "No data found for this project ID."
          : "No entries match \u201C" + filterText.trim() + "\u201D.");
    } else {
      setStatus("");
      for (DataEntry entry : filtered) {
        addDataRow(entry.getTag(), entry.getValue());
      }
    }

    int total = allEntries.size();
    int shown = filtered.size();
    if (!query.isEmpty() && shown != total) {
      countLabel.setText(shown + " of " + total + " entr" + (total == 1 ? "y" : "ies"));
    } else {
      countLabel.setText(total + " entr" + (total == 1 ? "y" : "ies"));
    }
  }

  private Comparator<DataEntry> buildComparator() {
    return (a, b) -> {
      String aVal = sortField == SortField.TAG
          ? (a.getTag() != null ? a.getTag() : "")
          : detectType(a.getValue());
      String bVal = sortField == SortField.TAG
          ? (b.getTag() != null ? b.getTag() : "")
          : detectType(b.getValue());
      int cmp = aVal.compareToIgnoreCase(bVal);
      return sortOrder == SortOrder.ASCENDING ? cmp : -cmp;
    };
  }

  private void onSortClicked(SortField field) {
    if (sortField == field) {
      sortOrder = (sortOrder == SortOrder.ASCENDING) ? SortOrder.DESCENDING : SortOrder.ASCENDING;
    } else {
      sortField = field;
      sortOrder = SortOrder.ASCENDING;
    }
    refreshSortIndicators();
    applyFilterAndSort();
  }

  private void refreshSortIndicators() {
    tagSortAsc.setVisible(sortField == SortField.TAG && sortOrder == SortOrder.ASCENDING);
    tagSortDec.setVisible(sortField == SortField.TAG && sortOrder == SortOrder.DESCENDING);
    typeSortAsc.setVisible(sortField == SortField.TYPE && sortOrder == SortOrder.ASCENDING);
    typeSortDec.setVisible(sortField == SortField.TYPE && sortOrder == SortOrder.DESCENDING);

    String tagAriaSort = sortField == SortField.TAG
        ? (sortOrder == SortOrder.ASCENDING ? "ascending" : "descending") : "none";
    String typeAriaSort = sortField == SortField.TYPE
        ? (sortOrder == SortOrder.ASCENDING ? "ascending" : "descending") : "none";
    dataTable.getCellFormatter().getElement(HEADER_ROW, COL_TAG).setAttribute("aria-sort", tagAriaSort);
    dataTable.getCellFormatter().getElement(HEADER_ROW, COL_TYPE).setAttribute("aria-sort", typeAriaSort);
  }

  private void buildTableHeader() {
    FlexCellFormatter fmt = dataTable.getFlexCellFormatter();

    // Tag header — sortable
    tagSortAsc = new InlineLabel(" \u25b2");
    tagSortDec = new InlineLabel(" \u25bc");
    tagSortAsc.setVisible(false);
    tagSortDec.setVisible(false);
    FlowPanel tagHeader = new FlowPanel();
    tagHeader.getElement().getStyle().setProperty("cursor", "pointer");
    tagHeader.add(new InlineLabel("Tag"));
    tagHeader.add(tagSortAsc);
    tagHeader.add(tagSortDec);
    tagHeader.addDomHandler(event -> onSortClicked(SortField.TAG), ClickEvent.getType());
    dataTable.setWidget(HEADER_ROW, COL_TAG, tagHeader);

    dataTable.setText(HEADER_ROW, COL_VALUE, "Value");

    // Type header — sortable
    typeSortAsc = new InlineLabel(" \u25b2");
    typeSortDec = new InlineLabel(" \u25bc");
    typeSortAsc.setVisible(false);
    typeSortDec.setVisible(false);
    FlowPanel typeHeader = new FlowPanel();
    typeHeader.getElement().getStyle().setProperty("cursor", "pointer");
    typeHeader.add(new InlineLabel("Type"));
    typeHeader.add(typeSortAsc);
    typeHeader.add(typeSortDec);
    typeHeader.addDomHandler(event -> onSortClicked(SortField.TYPE), ClickEvent.getType());
    dataTable.setWidget(HEADER_ROW, COL_TYPE, typeHeader);

    for (int col = 0; col < COL_ACTIONS; col++) {
      com.google.gwt.dom.client.Element th = dataTable.getCellFormatter()
          .getElement(HEADER_ROW, col);
      th.setAttribute("role", "columnheader");
      th.setAttribute("scope", "col");
      th.addClassName("ode-ComponentHeaderLabel");
    }
    dataTable.getCellFormatter().getElement(HEADER_ROW, COL_TAG).setAttribute("aria-sort", "none");
    dataTable.getCellFormatter().getElement(HEADER_ROW, COL_TYPE).setAttribute("aria-sort", "none");

    // Actions column header: Add button (hidden until a mutable provider is set).
    headerAddButton = new Button("+ Add");
    headerAddButton.addStyleName("gwt-Button");
    headerAddButton.getElement().setAttribute("aria-label", "Add new entry");
    headerAddButton.addClickHandler(event -> onAddClicked());
    headerAddButton.setVisible(false);
    dataTable.setWidget(HEADER_ROW, COL_ACTIONS, headerAddButton);
    com.google.gwt.dom.client.Element actTh = dataTable.getCellFormatter()
        .getElement(HEADER_ROW, COL_ACTIONS);
    actTh.setAttribute("role", "columnheader");
    actTh.setAttribute("scope", "col");

    dataTable.getRowFormatter().addStyleName(HEADER_ROW, "ode-ComponentHeaderRow");

    fmt.setWidth(HEADER_ROW, COL_TAG,     "30%");
    fmt.setWidth(HEADER_ROW, COL_VALUE,   "47%");
    fmt.setWidth(HEADER_ROW, COL_TYPE,    "15%");
    fmt.setWidth(HEADER_ROW, COL_ACTIONS, "8%");
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

    for (int col = 0; col < COL_ACTIONS; col++) {
      dataTable.getCellFormatter().getElement(row, col).setAttribute("role", "cell");
    }
    dataTable.getCellFormatter().addStyleName(row, COL_VALUE, "ode-ComponentNameLabel");
    dataTable.getCellFormatter().getElement(row, COL_VALUE)
        .getStyle().setProperty("wordBreak", "break-word");

    // Actions column: Edit + Delete buttons for mutable providers only.
    // The cell must be created (via setWidget) before its ARIA role can be set.
    if (currentProvider instanceof MutableDataStoreProvider) {
      final MutableDataStoreProvider mutableProvider = (MutableDataStoreProvider) currentProvider;
      final String rawValue = value;

      Button editBtn = new Button("\u270F");
      editBtn.addStyleName("gwt-Button");
      editBtn.getElement().setAttribute("aria-label", "Edit entry: " + tag);
      editBtn.addClickHandler(event ->
          new EntryEditDialog(tag, rawValue, mutableProvider, DataVisualizerPanel.this).show());

      Button deleteBtn = new Button("\u2715");
      deleteBtn.addStyleName("gwt-Button");
      deleteBtn.getElement().setAttribute("aria-label", "Delete entry: " + tag);
      deleteBtn.addClickHandler(event -> onDeleteClicked(tag, mutableProvider));

      FlowPanel actionsPanel = new FlowPanel();
      actionsPanel.getElement().getStyle().setProperty("display", "flex");
      actionsPanel.getElement().getStyle().setProperty("gap", "2px");
      actionsPanel.add(editBtn);
      actionsPanel.add(deleteBtn);
      dataTable.setWidget(row, COL_ACTIONS, actionsPanel);
      dataTable.getCellFormatter().getElement(row, COL_ACTIONS).setAttribute("role", "cell");
    }
  }

  private void onAddClicked() {
    if (!(currentProvider instanceof MutableDataStoreProvider)) {
      return;
    }
    new EntryEditDialog(null, null,
        (MutableDataStoreProvider) currentProvider, this).show();
  }

  private void onDeleteClicked(final String tag, final MutableDataStoreProvider provider) {
    MessageDialog.messageDialog(
        "Delete Entry",
        "Delete entry \u201C" + tag + "\u201D? This cannot be undone.",
        "Delete", "Cancel",
        new MessageDialog.Actions() {
          @Override
          public void onOK() {
            setStatus("Deleting\u2026");
            provider.deleteEntry(tag, new AsyncCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                refresh();
              }
              @Override
              public void onFailure(Throwable caught) {
                setStatus("Delete failed: " + caught.getMessage());
              }
            });
          }
          @Override
          public void onCancel() {
            // nothing
          }
        });
  }

  /**
   * Trims JSON strings to a displayable length without truncating the type hint.
   */
  private static String formatValue(String raw) {
    if (raw == null || raw.isEmpty()) return "(empty)";
    if (raw.length() > 120) {
      return raw.substring(0, 117) + "\u2026";
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

  /**
   * Modal dialog for creating a new entry (add mode) or updating the value of
   * an existing entry (edit mode). Tags are not editable in edit mode because
   * renaming a Redis key requires a separate delete + add flow.
   */
  static final class EntryEditDialog extends Dialog {

    private final boolean isAddMode;
    private final MutableDataStoreProvider provider;
    private final DataVisualizerPanel panel;
    private final TextBox tagBox;
    private final Label tagDisplayLabel;
    private final TextArea valueArea;
    private final Label errorLabel;

    /**
     * @param existingTag   the tag to edit; {@code null} to create a new entry
     * @param existingValue the current JSON value; {@code null} for a new entry
     * @param provider      the mutable data store to write/delete on
     * @param panel         the parent panel to refresh after a successful save
     */
    EntryEditDialog(String existingTag, String existingValue,
        MutableDataStoreProvider provider, DataVisualizerPanel panel) {
      // Dialog base class sets glass=true — override it here. The glass element
      // ends up at the same z-index as the popup in this GWT version, covering the
      // dialog and absorbing all mouse events (keyboard still works via focus).
      setGlassEnabled(false);
      this.isAddMode = (existingTag == null);
      this.provider = provider;
      this.panel = panel;

      tagBox = new TextBox();
      tagBox.setVisibleLength(40);

      tagDisplayLabel = new Label(existingTag != null ? existingTag : "");

      valueArea = new TextArea();
      valueArea.setVisibleLines(6);
      valueArea.setCharacterWidth(50);
      if (existingValue != null) {
        valueArea.setValue(existingValue);
      }

      Label jsonHint = new Label(
          "Values are JSON \u2014 e.g. \"hello\", 42, true, [1,2], {\"k\":\"v\"}");
      jsonHint.addStyleName("ode-ProjectFieldLabel");

      errorLabel = new Label();
      errorLabel.addStyleName("ode-ErrorMessage");
      errorLabel.getElement().setAttribute("role", "alert");
      errorLabel.setVisible(false);

      Button saveButton = new Button(isAddMode ? "Add" : "Save");
      saveButton.addStyleName("gwt-Button");
      saveButton.getElement().setAttribute("aria-label",
          isAddMode ? "Save new entry" : "Save changes to " + existingTag);
      saveButton.addClickHandler(event -> onSave());

      Button cancelButton = new Button("Cancel");
      cancelButton.addStyleName("gwt-Button");
      cancelButton.getElement().setAttribute("aria-label", "Cancel and close dialog");
      cancelButton.addClickHandler(event -> hide());

      FlowPanel buttonRow = new FlowPanel();
      buttonRow.getElement().getStyle().setProperty("marginTop", "8px");
      buttonRow.getElement().getStyle().setProperty("display", "flex");
      buttonRow.getElement().getStyle().setProperty("gap", "4px");
      buttonRow.add(saveButton);
      buttonRow.add(cancelButton);

      VerticalPanel layout = new VerticalPanel();
      layout.setSpacing(4);
      layout.add(new Label("Tag:"));
      layout.add(isAddMode ? tagBox : tagDisplayLabel);
      layout.add(new Label("Value (JSON):"));
      layout.add(valueArea);
      layout.add(jsonHint);
      layout.add(errorLabel);
      layout.add(buttonRow);

      setWidget(layout);
      configureAria(isAddMode ? "Add CloudDB Entry" : "Edit CloudDB Entry");
      setCaption(isAddMode ? "Add Entry" : "Edit: " + existingTag);
    }

    private void onSave() {
      String tag = isAddMode ? tagBox.getValue().trim() : tagDisplayLabel.getText().trim();
      String value = valueArea.getValue().trim();

      if (tag.isEmpty()) {
        showError("Tag must not be empty.");
        return;
      }
      if (value.isEmpty()) {
        showError("Value must not be empty.");
        return;
      }
      try {
        com.google.gwt.json.client.JSONParser.parseStrict(value);
      } catch (com.google.gwt.json.client.JSONException e) {
        showError("Invalid JSON: " + e.getMessage()
            + " \u2014 Use double quotes, e.g. {\"k\":\"v\"} not {'k':'v'}");
        return;
      }
      showError("");

      provider.setEntry(tag, value, new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          hide();
          panel.refresh();
        }
        @Override
        public void onFailure(Throwable caught) {
          showError("Save failed: " + caught.getMessage());
        }
      });
    }

    private void showError(String message) {
      errorLabel.setText(message);
      errorLabel.setVisible(message != null && !message.isEmpty());
    }
  }
}
