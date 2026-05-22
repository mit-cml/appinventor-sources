// -*- mode: java; c-basic-offset: 2; -*-
package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * First draft of the Translation UI.
 *
 * This intentionally starts small:
 * - show screens/components
 * - generate an internal i18n key
 *
 * Next slice:
 * - scan Designer properties
 * - filter text/string properties
 * - show base text and translation columns
 */
public final class TranslationPanel extends Composite {
  private static final String FIRST_LANGUAGE = "hi";

  private final YaProjectEditor projectEditor;
  private final FlexTable table;
  private final TextArea jsonPreview;
  private final Map<String, String> translationValues;
  private final Map<String, TranslationEntry> translationEntries;

  public TranslationPanel(YaProjectEditor projectEditor) {
    this.projectEditor = projectEditor;
    this.table = new FlexTable();
    this.translationValues = new HashMap<String, String>();
    this.translationEntries = new HashMap<String, TranslationEntry>();
    this.jsonPreview = new TextArea();

    FlowPanel root = new FlowPanel();
    root.setStylePrimaryName("ode-i18n-panel");
    root.setWidth("100%");
    root.setHeight("100%");

    Label title = new Label("Translations");
    title.setStylePrimaryName("ode-i18n-title");

    Label description = new Label(
        "This table lists translatable Designer properties and assigns safe internal "
            + "translation keys. Translation values are currently stored in memory.");

    table.setStylePrimaryName("ode-i18n-table");
    table.setWidth("100%");

    root.add(title);
    root.add(description);
    root.add(table);

    Button exportButton = new Button("Export JSON");
    exportButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        jsonPreview.setText(exportJson());
      }
    });

    jsonPreview.setWidth("100%");
    jsonPreview.setVisibleLines(12);
    jsonPreview.getElement().setAttribute("spellcheck", "false");

    root.add(exportButton);
    root.add(jsonPreview);

    initWidget(root);
  }

  public void refresh() {
    clearTable();

    addHeader();

    int row = 1;
    List<String> formNames = projectEditor.getFormNames();

    for (String formName : formNames) {
      List<String> componentNames = projectEditor.getComponentInstances(formName);

      if (componentNames.isEmpty()) {
        table.setText(row, 0, formName);
        table.setText(row, 1, "");
        table.setText(row, 2, "");
        table.setText(row, 3, "");
        table.setText(row, 4, "");
        table.setText(row, 5, "No components found");
        row++;
        continue;
      }

      for (String componentName : componentNames) {
        String componentType = projectEditor.getComponentType(formName, componentName);

        for (String propertyName : projectEditor.getComponentPropertyNames(formName,
            componentName)) {
          if (!isTranslatableProperty(propertyName)) {
            continue;
          }

          String propertyValue = projectEditor.getComponentPropertyValue(formName,
              componentName, propertyName);
          String generatedKey = TranslationKeyGenerator.generate(formName, componentName,
              propertyName);
          translationEntries.put(generatedKey, new TranslationEntry(generatedKey, formName,
              componentName, componentType, propertyName, propertyValue));

          table.setText(row, 0, formName);
          table.setText(row, 1, componentName);
          table.setText(row, 2, componentType);
          table.setText(row, 3, propertyName);
          table.setText(row, 4, generatedKey);
          table.setText(row, 5, propertyValue);
          table.setWidget(row, 6, createTranslationTextBox(generatedKey));

          row++;
        }
      }
    }
  }

  private void addHeader() {
    table.setText(0, 0, "Screen");
    table.setText(0, 1, "Component");
    table.setText(0, 2, "Type");
    table.setText(0, 3, "Property");
    table.setText(0, 4, "Internal Key");
    table.setText(0, 5, "Base Text");
    table.setText(0, 6, "hi");
    table.getRowFormatter().setStylePrimaryName(0, "ode-i18n-table-header");
  }

  private String exportJson() {
    JSONObject root = new JSONObject();

    root.put("baseLanguage", new JSONString("en"));

    JSONArray languages = new JSONArray();
    languages.set(0, new JSONString(FIRST_LANGUAGE));
    root.put("languages", languages);

    JSONObject entries = new JSONObject();
    ArrayList<String> keys = new ArrayList<String>(translationEntries.keySet());
    Collections.sort(keys);

    for (String key : keys) {
      TranslationEntry entry = translationEntries.get(key);
      if (entry == null) {
        continue;
      }

      JSONObject entryObject = new JSONObject();

      JSONObject source = new JSONObject();
      source.put("screen", new JSONString(entry.getScreenName()));
      source.put("component", new JSONString(entry.getComponentName()));
      source.put("type", new JSONString(entry.getComponentType()));
      source.put("property", new JSONString(entry.getPropertyName()));
      source.put("baseText", new JSONString(entry.getBaseText()));
      entryObject.put("source", source);

      JSONObject translations = new JSONObject();
      String translatedValue = translationValues.get(key);
      if (translatedValue != null && translatedValue.length() > 0) {
        translations.put(FIRST_LANGUAGE, new JSONString(translatedValue));
      }
      entryObject.put("translations", translations);

      entries.put(key, entryObject);
    }

    root.put("entries", entries);

    return root.toString();
  }

  private TextBox createTranslationTextBox(final String translationKey) {
    final TextBox textBox = new TextBox();
    textBox.setWidth("100%");
    textBox.setValue(translationValues.get(translationKey));

    textBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        translationValues.put(translationKey, textBox.getValue());
      }
    });

    return textBox;
  }

  private boolean isTranslatableProperty(String propertyName) {
    return "Text".equals(propertyName)
        || "Hint".equals(propertyName)
        || "Title".equals(propertyName)
        || "Prompt".equals(propertyName);
  }

  private void clearTable() {
    while (table.getRowCount() > 0) {
      table.removeRow(0);
    }
  }
}
