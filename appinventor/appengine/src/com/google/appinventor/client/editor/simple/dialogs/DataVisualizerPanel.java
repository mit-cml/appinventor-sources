// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.dialogs;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.DataStoreProvider;
import com.google.appinventor.client.editor.simple.MutableDataStoreProvider;
import com.google.appinventor.client.utils.MessageDialog;
import com.google.appinventor.client.wizards.Dialog;
import com.google.appinventor.shared.rpc.clouddb.DataEntry;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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

  private static final int COL_TAG     = 0;
  private static final int COL_VALUE   = 1;
  private static final int COL_TYPE    = 2;
  private static final int COL_ACTIONS = 3;
  private static final int HEADER_ROW  = 0;

  private enum SortField { TAG, TYPE }
  private enum SortOrder { ASCENDING, DESCENDING }

  private List<DataEntry> allEntries = new ArrayList<>();
  private String filterText = "";
  private SortField sortField = null;
  private SortOrder sortOrder = SortOrder.ASCENDING;

  private TextBox filterBox;
  private InlineLabel tagSortAsc, tagSortDec;
  private InlineLabel typeSortAsc, typeSortDec;

  private Timer autoRefreshTimer;
  private int autoRefreshIntervalMs = 0; // 0 = Off

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
    instance.restartTimer();
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

    Button refreshButton = new Button(MESSAGES.clouddbVizRefreshButton());
    refreshButton.addStyleName("gwt-Button");
    refreshButton.getElement().setAttribute("aria-label", MESSAGES.clouddbVizRefreshAriaLabel());
    refreshButton.addClickHandler(event -> refresh());

    Button closeButton = new Button(MESSAGES.clouddbVizCloseButton());
    closeButton.addStyleName("gwt-Button");
    closeButton.getElement().setAttribute("aria-label", MESSAGES.clouddbVizCloseAriaLabel());
    closeButton.addClickHandler(event -> hide());

    ListBox intervalBox = new ListBox();
    intervalBox.addItem(MESSAGES.clouddbVizAutoOff(), "0");
    intervalBox.addItem("5s",  "5000");
    intervalBox.addItem("10s", "10000");
    intervalBox.addItem("30s", "30000");
    intervalBox.addItem("60s", "60000");
    intervalBox.getElement().setAttribute("aria-label", MESSAGES.clouddbVizAutoRefreshAriaLabel());
    intervalBox.addChangeHandler(event -> {
      autoRefreshIntervalMs =
          Integer.parseInt(intervalBox.getValue(intervalBox.getSelectedIndex()));
      restartTimer();
    });

    FlowPanel footer = new FlowPanel();
    footer.addStyleName("ode-Android-footer");
    footer.getElement().getStyle().setProperty("padding", "4px 8px");
    footer.getElement().getStyle().setProperty("display", "flex");
    footer.getElement().getStyle().setProperty("alignItems", "center");
    footer.getElement().getStyle().setProperty("gap", "8px");
    footer.add(countLabel);
    // Push controls to right
    HTML spacer = new HTML("&nbsp;");
    spacer.getElement().getStyle().setProperty("flex", "1");
    footer.add(spacer);
    footer.add(intervalBox);
    footer.add(refreshButton);
    footer.add(closeButton);

    addCloseHandler(event -> stopTimer());

    filterBox = new TextBox();
    filterBox.getElement().setAttribute("type", "search");
    filterBox.getElement().setAttribute("placeholder", MESSAGES.clouddbVizSearchPlaceholder());
    filterBox.getElement().setAttribute("aria-label", MESSAGES.clouddbVizFilterAriaLabel());
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
    configureAria(MESSAGES.clouddbVizPanelTitle());
  }

  private void setProvider(DataStoreProvider provider) {
    this.currentProvider = provider;
    setCaption(MESSAGES.clouddbVizCaption(
        provider.getDataStoreName(), provider.getDataStoreType()));
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
    if (allEntries.isEmpty()) {
      setStatus(MESSAGES.clouddbVizLoading());
    }

    currentProvider.fetchEntries(new AsyncCallback<List<DataEntry>>() {
      @Override
      public void onSuccess(List<DataEntry> entries) {
        allEntries = new ArrayList<>(entries);
        applyFilterAndSort();
      }

      @Override
      public void onFailure(Throwable caught) {
        setStatus(MESSAGES.clouddbVizLoadError(caught.getMessage()));
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
          ? MESSAGES.clouddbVizNoData()
          : MESSAGES.clouddbVizNoMatch(filterText.trim()));
    } else {
      setStatus("");
      for (DataEntry entry : filtered) {
        addDataRow(entry.getTag(), entry.getValue());
      }
    }

    int total = allEntries.size();
    int shown = filtered.size();
    if (!query.isEmpty() && shown != total) {
      countLabel.setText(MESSAGES.clouddbVizFilteredCount(shown, total));
    } else {
      countLabel.setText(MESSAGES.clouddbVizTotalCount(total));
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

  private void restartTimer() {
    stopTimer();
    if (autoRefreshIntervalMs > 0) {
      autoRefreshTimer = new Timer() {
        @Override
        public void run() {
          refresh();
        }
      };
      autoRefreshTimer.scheduleRepeating(autoRefreshIntervalMs);
    }
  }

  private void stopTimer() {
    if (autoRefreshTimer != null) {
      autoRefreshTimer.cancel();
      autoRefreshTimer = null;
    }
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
    tagHeader.add(new InlineLabel(MESSAGES.clouddbVizColTag()));
    tagHeader.add(tagSortAsc);
    tagHeader.add(tagSortDec);
    tagHeader.addDomHandler(event -> onSortClicked(SortField.TAG), ClickEvent.getType());
    dataTable.setWidget(HEADER_ROW, COL_TAG, tagHeader);

    dataTable.setText(HEADER_ROW, COL_VALUE, MESSAGES.clouddbVizColValue());

    // Type header — sortable
    typeSortAsc = new InlineLabel(" \u25b2");
    typeSortDec = new InlineLabel(" \u25bc");
    typeSortAsc.setVisible(false);
    typeSortDec.setVisible(false);
    FlowPanel typeHeader = new FlowPanel();
    typeHeader.getElement().getStyle().setProperty("cursor", "pointer");
    typeHeader.add(new InlineLabel(MESSAGES.clouddbVizColType()));
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
    headerAddButton = new Button(MESSAGES.clouddbVizAddButton());
    headerAddButton.addStyleName("gwt-Button");
    headerAddButton.getElement().setAttribute("aria-label", MESSAGES.clouddbVizAddAriaLabel());
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
      editBtn.getElement().setAttribute("aria-label", MESSAGES.clouddbVizEditEntryAriaLabel(tag));
      editBtn.addClickHandler(event ->
          new EntryEditDialog(tag, rawValue, mutableProvider, DataVisualizerPanel.this).show());

      Button deleteBtn = new Button("\u2715");
      deleteBtn.addStyleName("gwt-Button");
      deleteBtn.getElement().setAttribute("aria-label", MESSAGES.clouddbVizDeleteEntryAriaLabel(tag));
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
        MESSAGES.clouddbVizDeleteTitle(),
        MESSAGES.clouddbVizDeleteConfirm(tag),
        MESSAGES.clouddbVizDeleteConfirmButton(), MESSAGES.cancelButton(),
        new MessageDialog.Actions() {
          @Override
          public void onOK() {
            setStatus(MESSAGES.clouddbVizDeleting());
            provider.deleteEntry(tag, new AsyncCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                refresh();
              }
              @Override
              public void onFailure(Throwable caught) {
                setStatus(MESSAGES.clouddbVizDeleteFailed(caught.getMessage()));
              }
            });
          }
          @Override
          public void onCancel() {
            // nothing
          }
        }, true);
  }

  /**
   * Trims JSON strings to a displayable length without truncating the type hint.
   */
  private static String formatValue(String raw) {
    if (raw == null || raw.isEmpty()) return MESSAGES.clouddbVizEmptyValue();
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
   *
   * <p>The value editor defaults to a type-aware visual editor. An "Edit as text"
   * link below the editor reveals a raw JSON textarea for advanced users.
   */
  static final class EntryEditDialog extends Dialog {

    private static final int TAB_STRUCTURED = 0;
    private static final int TAB_RAW        = 1;

    private final boolean isAddMode;
    private final MutableDataStoreProvider provider;
    private final DataVisualizerPanel panel;
    private final TextBox tagBox;
    private final Label tagDisplayLabel;
    private final JsonNodeEditor rootEditor;
    private final TextArea rawTextArea;
    private final Label errorLabel;
    private final Anchor modeLink;
    private final SimplePanel tabContentPanel;
    private int currentTab = TAB_STRUCTURED;

    EntryEditDialog(String existingTag, String existingValue,
        MutableDataStoreProvider provider, DataVisualizerPanel panel) {
      setGlassEnabled(false);
      this.isAddMode = (existingTag == null);
      this.provider = provider;
      this.panel = panel;

      tagBox = new TextBox();
      tagBox.setVisibleLength(40);

      tagDisplayLabel = new Label(existingTag != null ? existingTag : "");

      rawTextArea = new TextArea();
      rawTextArea.setVisibleLines(8);
      rawTextArea.setCharacterWidth(50);
      if (existingValue != null) {
        rawTextArea.setValue(existingValue);
      }

      // Parse existing value for the structured editor.
      JSONValue parsed = null;
      if (existingValue != null && !existingValue.trim().isEmpty()) {
        try {
          parsed = JSONParser.parseStrict(existingValue);
        } catch (JSONException e) {
          // Unparseable — structured editor starts blank, raw tab has the text.
        }
      }
      rootEditor = new JsonNodeEditor(parsed);

      errorLabel = new Label();
      errorLabel.addStyleName("ode-ErrorMessage");
      errorLabel.getElement().setAttribute("role", "alert");
      errorLabel.getElement().getStyle().setProperty("maxWidth", "510px");
      errorLabel.getElement().getStyle().setProperty("wordWrap", "break-word");
      errorLabel.getElement().getStyle().setProperty("overflowWrap", "break-word");
      errorLabel.getElement().getStyle().setProperty("whiteSpace", "normal");
      errorLabel.setVisible(false);

      tabContentPanel = new SimplePanel();
      tabContentPanel.setWidget(rootEditor);
      tabContentPanel.setWidth("500px");
      tabContentPanel.getElement().getStyle().setProperty("maxHeight", "400px");
      tabContentPanel.getElement().getStyle().setProperty("overflowY", "auto");
      tabContentPanel.getElement().getStyle().setProperty("overflowX", "auto");
      tabContentPanel.getElement().getStyle().setProperty("border", "1px solid #ddd");
      tabContentPanel.getElement().getStyle().setProperty("padding", "4px");
      tabContentPanel.getElement().getStyle().setProperty("boxSizing", "border-box");

      modeLink = new Anchor(MESSAGES.clouddbVizEditAsText());
      modeLink.getElement().getStyle().setProperty("fontSize", "12px");
      modeLink.getElement().getStyle().setProperty("cursor", "pointer");
      modeLink.addClickHandler(e -> {
        e.preventDefault();
        switchToTab(currentTab == TAB_STRUCTURED ? TAB_RAW : TAB_STRUCTURED);
      });

      FlowPanel linkRow = new FlowPanel();
      linkRow.getElement().getStyle().setProperty("textAlign", "right");
      linkRow.getElement().getStyle().setProperty("marginTop", "2px");
      linkRow.add(modeLink);

      Button saveButton = new Button(isAddMode
          ? MESSAGES.clouddbVizAddEntryButton()
          : MESSAGES.clouddbVizSaveButton());
      saveButton.addStyleName("gwt-Button");
      saveButton.getElement().setAttribute("aria-label", isAddMode
          ? MESSAGES.clouddbVizSaveNewAriaLabel()
          : MESSAGES.clouddbVizSaveChangesAriaLabel(existingTag));
      saveButton.addClickHandler(event -> onSave());

      Button cancelButton = new Button(MESSAGES.cancelButton());
      cancelButton.addStyleName("gwt-Button");
      cancelButton.getElement().setAttribute("aria-label", MESSAGES.clouddbVizCancelAriaLabel());
      cancelButton.addClickHandler(event -> hide());

      FlowPanel buttonRow = new FlowPanel();
      buttonRow.getElement().getStyle().setProperty("marginTop", "8px");
      buttonRow.getElement().getStyle().setProperty("display", "flex");
      buttonRow.getElement().getStyle().setProperty("gap", "4px");
      buttonRow.add(saveButton);
      buttonRow.add(cancelButton);

      VerticalPanel layout = new VerticalPanel();
      layout.setSpacing(4);
      layout.add(new Label(MESSAGES.clouddbVizTagLabel()));
      layout.add(isAddMode ? tagBox : tagDisplayLabel);
      layout.add(tabContentPanel);
      layout.add(linkRow);
      layout.add(errorLabel);
      layout.add(buttonRow);

      setWidget(layout);
      configureAria(isAddMode
          ? MESSAGES.clouddbVizAddEntryDialogAriaLabel()
          : MESSAGES.clouddbVizEditEntryDialogAriaLabel());
      setCaption(isAddMode
          ? MESSAGES.clouddbVizAddEntryTitle()
          : MESSAGES.clouddbVizEditEntryTitle(existingTag));
    }

    private void switchToTab(int tab) {
      if (tab == currentTab) return;
      showError("");
      if (tab == TAB_RAW) {
        rawTextArea.setValue(rootEditor.getValue());
        tabContentPanel.setWidget(rawTextArea);
        modeLink.setText(MESSAGES.clouddbVizBackToVisual());
      } else {
        String raw = rawTextArea.getValue().trim();
        if (!raw.isEmpty()) {
          try {
            rootEditor.setValue(JSONParser.parseStrict(raw));
          } catch (JSONException e) {
            showError(MESSAGES.clouddbVizJsonFixError(e.getMessage()));
            return;
          }
        }
        tabContentPanel.setWidget(rootEditor);
        modeLink.setText(MESSAGES.clouddbVizEditAsText());
      }
      currentTab = tab;
    }

    private void onSave() {
      String tag = isAddMode ? tagBox.getValue().trim() : tagDisplayLabel.getText().trim();
      if (tag.isEmpty()) {
        showError(MESSAGES.clouddbVizTagEmpty());
        return;
      }

      String value;
      if (currentTab == TAB_RAW) {
        value = rawTextArea.getValue().trim();
        if (value.isEmpty()) {
          showError(MESSAGES.clouddbVizValueEmpty());
          return;
        }
        try {
          JSONParser.parseStrict(value);
        } catch (JSONException e) {
          showError(MESSAGES.clouddbVizJsonInvalid(e.getMessage()));
          return;
        }
      } else {
        value = rootEditor.getValue();
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
          showError(MESSAGES.clouddbVizSaveFailed(caught.getMessage()));
        }
      });
    }

    private void showError(String message) {
      errorLabel.setText(message);
      errorLabel.setVisible(message != null && !message.isEmpty());
    }
  }

  /**
   * A recursive GWT widget that edits a single JSON value.
   *
   * <p>A type-selector {@link ListBox} lets the user switch between text, number,
   * boolean, list, and dictionary modes. For list and dict types the body renders
   * child {@code JsonNodeEditor} instances, enabling arbitrary nesting depth.
   */
  static final class JsonNodeEditor extends Composite {

    private static int instanceCounter = 0;

    private static final String TYPE_TEXT    = "text";
    private static final String TYPE_NUMBER  = "number";
    private static final String TYPE_BOOLEAN = "boolean";
    private static final String TYPE_LIST    = "list";
    private static final String TYPE_DICT    = "dict";

    private final String uid = "jne" + (++instanceCounter);
    private final ListBox typeSelector;
    private final SimplePanel bodyPanel;

    // Lazily-created per-type controls.
    private TextBox textBox;
    private TextBox numberBox;
    private FlowPanel boolPanel;
    private RadioButton trueButton;
    private RadioButton falseButton;

    // List state.
    private final List<JsonNodeEditor> listItems = new ArrayList<>();

    // Dict state: parallel lists of keys and value editors.
    private final List<TextBox>       dictKeyBoxes     = new ArrayList<>();
    private final List<JsonNodeEditor> dictValueEditors = new ArrayList<>();

    private String currentType = TYPE_TEXT;

    JsonNodeEditor() {
      this(null);
    }

    JsonNodeEditor(JSONValue initialValue) {
      typeSelector = new ListBox();
      typeSelector.addItem(MESSAGES.clouddbVizTypeText(),    TYPE_TEXT);
      typeSelector.addItem(MESSAGES.clouddbVizTypeNumber(),  TYPE_NUMBER);
      typeSelector.addItem(MESSAGES.clouddbVizTypeBoolean(), TYPE_BOOLEAN);
      typeSelector.addItem(MESSAGES.clouddbVizTypeList(),    TYPE_LIST);
      typeSelector.addItem(MESSAGES.clouddbVizTypeDict(),    TYPE_DICT);
      typeSelector.getElement().setAttribute("aria-label", MESSAGES.clouddbVizValueTypeAriaLabel());

      bodyPanel = new SimplePanel();
      bodyPanel.getElement().getStyle().setProperty("marginTop", "4px");
      bodyPanel.getElement().getStyle().setProperty("overflowX", "auto");

      typeSelector.addChangeHandler(e -> {
        currentType = typeSelector.getValue(typeSelector.getSelectedIndex());
        renderBody();
      });

      VerticalPanel root = new VerticalPanel();
      root.add(typeSelector);
      root.add(bodyPanel);
      initWidget(root);

      if (initialValue != null) {
        setValue(initialValue);
      } else {
        renderBody();
      }
    }

    /** Populate the editor from a parsed {@link JSONValue}. */
    void setValue(JSONValue v) {
      if (v == null || v instanceof JSONNull) {
        selectType(TYPE_TEXT);
        ensureTextBox();
        textBox.setValue("");
      } else if (v instanceof JSONString) {
        selectType(TYPE_TEXT);
        ensureTextBox();
        textBox.setValue(((JSONString) v).stringValue());
      } else if (v instanceof JSONNumber) {
        selectType(TYPE_NUMBER);
        ensureNumberBox();
        double d = ((JSONNumber) v).doubleValue();
        numberBox.setValue(isWholeNumber(d) ? String.valueOf((long) d) : String.valueOf(d));
      } else if (v instanceof JSONBoolean) {
        selectType(TYPE_BOOLEAN);
        ensureBoolPanel();
        boolean b = ((JSONBoolean) v).booleanValue();
        trueButton.setValue(b);
        falseButton.setValue(!b);
      } else if (v instanceof JSONArray) {
        selectType(TYPE_LIST);
        listItems.clear();
        JSONArray arr = (JSONArray) v;
        for (int i = 0; i < arr.size(); i++) {
          listItems.add(new JsonNodeEditor(arr.get(i)));
        }
      } else if (v instanceof JSONObject) {
        selectType(TYPE_DICT);
        dictKeyBoxes.clear();
        dictValueEditors.clear();
        JSONObject obj = (JSONObject) v;
        for (String key : obj.keySet()) {
          dictKeyBoxes.add(newKeyBox(key));
          dictValueEditors.add(new JsonNodeEditor(obj.get(key)));
        }
      }
      renderBody();
    }

    /** Serialize the current editor state to a JSON string. */
    String getValue() {
      switch (currentType) {
        case TYPE_TEXT:
          return new JSONString(textBox != null ? textBox.getValue() : "").toString();
        case TYPE_NUMBER: {
          String s = numberBox != null ? numberBox.getValue().trim() : "0";
          try {
            return new JSONNumber(Double.parseDouble(s)).toString();
          } catch (NumberFormatException e) {
            return "0";
          }
        }
        case TYPE_BOOLEAN:
          return (trueButton != null && trueButton.getValue()) ? "true" : "false";
        case TYPE_LIST: {
          StringBuilder sb = new StringBuilder("[");
          for (int i = 0; i < listItems.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(listItems.get(i).getValue());
          }
          return sb.append("]").toString();
        }
        case TYPE_DICT: {
          StringBuilder sb = new StringBuilder("{");
          for (int i = 0; i < dictKeyBoxes.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(new JSONString(dictKeyBoxes.get(i).getValue()).toString());
            sb.append(":");
            sb.append(dictValueEditors.get(i).getValue());
          }
          return sb.append("}").toString();
        }
        default:
          return "null";
      }
    }

    private void selectType(String type) {
      currentType = type;
      for (int i = 0; i < typeSelector.getItemCount(); i++) {
        if (type.equals(typeSelector.getValue(i))) {
          typeSelector.setSelectedIndex(i);
          return;
        }
      }
    }

    private void renderBody() {
      switch (currentType) {
        case TYPE_TEXT:
          ensureTextBox();
          bodyPanel.setWidget(textBox);
          break;
        case TYPE_NUMBER:
          ensureNumberBox();
          bodyPanel.setWidget(numberBox);
          break;
        case TYPE_BOOLEAN:
          ensureBoolPanel();
          bodyPanel.setWidget(boolPanel);
          break;
        case TYPE_LIST:
          bodyPanel.setWidget(buildListWidget());
          break;
        case TYPE_DICT:
          bodyPanel.setWidget(buildDictWidget());
          break;
        default:
          break;
      }
    }

    private void ensureTextBox() {
      if (textBox == null) {
        textBox = new TextBox();
        textBox.setWidth("200px");
        textBox.getElement().setAttribute("aria-label", MESSAGES.clouddbVizTextValueAriaLabel());
      }
    }

    private void ensureNumberBox() {
      if (numberBox == null) {
        numberBox = new TextBox();
        numberBox.setWidth("120px");
        numberBox.getElement().setAttribute("aria-label", MESSAGES.clouddbVizNumberValueAriaLabel());
      }
    }

    private void ensureBoolPanel() {
      if (boolPanel == null) {
        trueButton  = new RadioButton(uid + "-bool", "true");
        falseButton = new RadioButton(uid + "-bool", "false");
        trueButton.setValue(true);
        boolPanel = new FlowPanel();
        boolPanel.getElement().getStyle().setProperty("display", "flex");
        boolPanel.getElement().getStyle().setProperty("gap", "16px");
        boolPanel.getElement().getStyle().setProperty("padding", "4px 0");
        boolPanel.add(trueButton);
        boolPanel.add(falseButton);
      }
    }

    private Widget buildListWidget() {
      VerticalPanel vp = new VerticalPanel();
      vp.setSpacing(2);
      for (int i = 0; i < listItems.size(); i++) {
        final int idx = i;
        FlowPanel row = new FlowPanel();
        row.getElement().getStyle().setProperty("display", "flex");
        row.getElement().getStyle().setProperty("alignItems", "flex-start");
        row.getElement().getStyle().setProperty("gap", "4px");
        row.getElement().getStyle().setProperty("marginBottom", "2px");

        Label num = new Label((i + 1) + ".");
        num.getElement().getStyle().setProperty("minWidth", "20px");
        num.getElement().getStyle().setProperty("paddingTop", "4px");

        Button del = new Button("\u00D7");
        del.addStyleName("gwt-Button");
        del.getElement().setAttribute("aria-label", MESSAGES.clouddbVizRemoveItem(i + 1));
        del.getElement().getStyle().setProperty("padding", "0 6px");
        del.addClickHandler(e -> { listItems.remove(idx); renderBody(); });

        row.add(num);
        row.add(listItems.get(i));
        row.add(del);
        vp.add(row);
      }

      Button addBtn = new Button(MESSAGES.clouddbVizAddItem());
      addBtn.addStyleName("gwt-Button");
      addBtn.addClickHandler(e -> { listItems.add(new JsonNodeEditor()); renderBody(); });
      vp.add(addBtn);
      return vp;
    }

    private Widget buildDictWidget() {
      VerticalPanel vp = new VerticalPanel();
      vp.setSpacing(2);
      for (int i = 0; i < dictKeyBoxes.size(); i++) {
        final int idx = i;

        // Header row: key label + key TextBox + delete button.
        FlowPanel headerRow = new FlowPanel();
        headerRow.getElement().getStyle().setProperty("display", "flex");
        headerRow.getElement().getStyle().setProperty("alignItems", "center");
        headerRow.getElement().getStyle().setProperty("gap", "4px");
        headerRow.getElement().getStyle().setProperty("marginBottom", "2px");

        Label keyLbl = new Label(MESSAGES.clouddbVizKeyLabel());
        keyLbl.getElement().getStyle().setProperty("flexShrink", "0");

        Button del = new Button("\u00D7");
        del.addStyleName("gwt-Button");
        del.getElement().setAttribute("aria-label",
            MESSAGES.clouddbVizRemoveKey(dictKeyBoxes.get(i).getValue()));
        del.getElement().getStyle().setProperty("padding", "0 6px");
        del.addClickHandler(e -> {
          dictKeyBoxes.remove(idx);
          dictValueEditors.remove(idx);
          renderBody();
        });

        headerRow.add(keyLbl);
        headerRow.add(dictKeyBoxes.get(i));
        headerRow.add(del);

        // Value row: indented below the key.
        FlowPanel valueRow = new FlowPanel();
        valueRow.getElement().getStyle().setProperty("display", "flex");
        valueRow.getElement().getStyle().setProperty("alignItems", "flex-start");
        valueRow.getElement().getStyle().setProperty("gap", "4px");
        valueRow.getElement().getStyle().setProperty("paddingLeft", "24px");
        valueRow.getElement().getStyle().setProperty("marginBottom", "6px");

        Label valLbl = new Label(MESSAGES.clouddbVizValueRowLabel());
        valLbl.getElement().getStyle().setProperty("paddingTop", "4px");
        valLbl.getElement().getStyle().setProperty("flexShrink", "0");

        valueRow.add(valLbl);
        valueRow.add(dictValueEditors.get(i));

        vp.add(headerRow);
        vp.add(valueRow);
      }

      Button addBtn = new Button(MESSAGES.clouddbVizAddKey());
      addBtn.addStyleName("gwt-Button");
      addBtn.addClickHandler(e -> {
        dictKeyBoxes.add(newKeyBox(""));
        dictValueEditors.add(new JsonNodeEditor());
        renderBody();
      });
      vp.add(addBtn);
      return vp;
    }

    private static TextBox newKeyBox(String value) {
      TextBox kb = new TextBox();
      kb.setValue(value);
      kb.setWidth("110px");
      kb.getElement().setAttribute("aria-label", MESSAGES.clouddbVizKeyNameAriaLabel());
      return kb;
    }

    private static boolean isWholeNumber(double d) {
      return !Double.isInfinite(d) && !Double.isNaN(d)
          && d == Math.floor(d) && Math.abs(d) < 1e15;
    }
  }
}
