// -*- mode: java; c-basic-offset: 2; -*-
package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TranslationPanel extends Composite {
  private static final String DEFAULT_LANGUAGE = "hi";

  private final YaProjectEditor projectEditor;
  private final FlexTable table;
  private final Map<String, Map<String, String>> translationValues;
  private final Map<String, TranslationEntry> translationEntries;
  private final List<String> languages;
  private final TextBox languageTextBox;

  private boolean savedTranslationsLoaded;

  public TranslationPanel(YaProjectEditor projectEditor) {
    this.projectEditor = projectEditor;
    this.table = new FlexTable();
    this.translationValues = new HashMap<String, Map<String, String>>();
    this.translationEntries = new HashMap<String, TranslationEntry>();
    this.savedTranslationsLoaded = false;
    this.languages = new ArrayList<String>();
    this.languages.add(DEFAULT_LANGUAGE);
    this.languageTextBox = new TextBox();

    FlowPanel root = new FlowPanel();
    root.setStylePrimaryName("ode-i18n-panel");
    root.setWidth("100%");
    root.setHeight("100%");

    Label title = new Label("Translations");
    title.setStylePrimaryName("ode-i18n-title");

    Label description = new Label(
        "This table lists translatable Designer properties and assigns safe internal "
            + "translation keys. Translation values are stored in project settings.");

    table.setStylePrimaryName("ode-i18n-table");
    table.setWidth("100%");

    Button exportButton = new Button("Export JSON");
    exportButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
      showJsonDialog("Export Translation JSON",
          "Copy or inspect the current i18n JSON below.",
          exportJson());
      }
    });

    Button saveButton = new Button("Save JSON");
    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        saveJson();
      }
    });

    root.add(title);
    root.add(description);
    root.add(table);

    Label languageLabel = new Label("Language code:");
    languageTextBox.setWidth("80px");

    Button addLanguageButton = new Button("Add Language");
    addLanguageButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        addLanguage(languageTextBox.getValue());
        languageTextBox.setValue("");
        refresh();
      }
    });

    root.add(languageLabel);
    root.add(languageTextBox);
    root.add(addLanguageButton);
    root.add(exportButton);
    root.add(saveButton);

    initWidget(root);
  }

  public void refresh() {
    loadSavedTranslations();

    clearTable();
    translationEntries.clear();

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
          for (int i = 0; i < languages.size(); i++) {
            String language = languages.get(i);
            table.setWidget(row, 6 + i, createTranslationTextBox(generatedKey, language));
          }

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
    for (int i = 0; i < languages.size(); i++) {
      table.setText(0, 6 + i, languages.get(i));
    }
    table.getRowFormatter().setStylePrimaryName(0, "ode-i18n-table-header");
  }

  private void showJsonDialog(String title, String message, String json) {
    final DialogBox dialog = new DialogBox(false, true);
    dialog.setText(title);
    dialog.setGlassEnabled(false);
    dialog.setAnimationEnabled(true);

    VerticalPanel panel = new VerticalPanel();
    panel.setSpacing(8);
    panel.setWidth("720px");

    Label messageLabel = new Label(message);

    TextArea jsonTextArea = new TextArea();
    jsonTextArea.setText(json);
    jsonTextArea.setWidth("700px");
    jsonTextArea.setVisibleLines(16);
    jsonTextArea.getElement().setAttribute("spellcheck", "false");

    Anchor closeLink = new Anchor("Close");
    closeLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    panel.add(messageLabel);
    panel.add(jsonTextArea);
    panel.add(closeLink);

    dialog.setWidget(panel);
    dialog.center();
  }

  private void saveJson() {
    String json = exportJson();

    projectEditor.changeProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_I18N_TRANSLATIONS,
        json);

    showJsonDialog("Translations Saved",
        "Saved translations to project settings.",
        json);
  }

  private void loadSavedTranslations() {
    if (savedTranslationsLoaded) {
      return;
    }

    savedTranslationsLoaded = true;

    try {
      String savedJson = projectEditor.getProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_I18N_TRANSLATIONS);

      if (savedJson == null || savedJson.length() == 0) {
        return;
      }

      JSONValue parsed = JSONParser.parseStrict(savedJson);
      JSONObject root = parsed.isObject();
      if (root == null) {
        return;
      }

      JSONValue languagesValue = root.get("languages");
      if (languagesValue != null && languagesValue.isArray() != null) {
        JSONArray savedLanguages = languagesValue.isArray();
        languages.clear();

        for (int i = 0; i < savedLanguages.size(); i++) {
          JSONValue languageValue = savedLanguages.get(i);
          if (languageValue != null && languageValue.isString() != null) {
            addLanguage(languageValue.isString().stringValue());
          }
        }

        if (languages.isEmpty()) {
          languages.add(DEFAULT_LANGUAGE);
        }
      }

      JSONValue entriesValue = root.get("entries");
      if (entriesValue == null || entriesValue.isObject() == null) {
        return;
      }

      JSONObject entries = entriesValue.isObject();
      for (String key : entries.keySet()) {
        JSONValue entryValue = entries.get(key);
        if (entryValue == null || entryValue.isObject() == null) {
          continue;
        }

        JSONObject entry = entryValue.isObject();
        JSONValue translationsValue = entry.get("translations");
        if (translationsValue == null || translationsValue.isObject() == null) {
          continue;
        }

        JSONObject translations = translationsValue.isObject();
        for (String language : translations.keySet()) {
          JSONValue translatedValue = translations.get(language);
          if (translatedValue != null && translatedValue.isString() != null) {
            addLanguage(language);
            setTranslationValue(key, language, translatedValue.isString().stringValue());
          }
        }
      }
    } catch (RuntimeException e) {
      // Ignore invalid saved data for now. The table can still rebuild from the current project.
    }
  }

  private String exportJson() {
    JSONObject root = new JSONObject();

    root.put("baseLanguage", new JSONString("en"));

    JSONArray languagesJson = new JSONArray();
    for (int i = 0; i < languages.size(); i++) {
      languagesJson.set(i, new JSONString(languages.get(i)));
    }
    root.put("languages", languagesJson);

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
      for (String language : languages) {
        String translatedValue = getTranslationValue(key, language);
        if (translatedValue.length() > 0) {
          translations.put(language, new JSONString(translatedValue));
        }
      }
      entryObject.put("translations", translations);

      entries.put(key, entryObject);
    }

    root.put("entries", entries);

    return root.toString();
  }

  private void addLanguage(String language) {
    if (language == null) {
      return;
    }

    language = language.trim();
    if (language.length() == 0 || languages.contains(language)) {
      return;
    }

    languages.add(language);
  }

  private String getTranslationValue(String translationKey, String language) {
    Map<String, String> values = translationValues.get(translationKey);
    if (values == null) {
      return "";
    }

    String value = values.get(language);
    return value == null ? "" : value;
  }

  private void setTranslationValue(String translationKey, String language, String value) {
    Map<String, String> values = translationValues.get(translationKey);
    if (values == null) {
      values = new HashMap<String, String>();
      translationValues.put(translationKey, values);
    }

    if (value == null || value.length() == 0) {
      values.remove(language);
      if (values.isEmpty()) {
        translationValues.remove(translationKey);
      }
    } else {
      values.put(language, value);
    }
  }

  private TextBox createTranslationTextBox(final String translationKey, final String language) {
    final TextBox textBox = new TextBox();
    textBox.setWidth("100%");
    textBox.setValue(getTranslationValue(translationKey, language));

    textBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        setTranslationValue(translationKey, language, textBox.getValue());
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
