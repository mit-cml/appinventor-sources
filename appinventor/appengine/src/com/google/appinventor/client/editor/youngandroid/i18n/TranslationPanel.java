// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class TranslationPanel extends Composite {
  private static final String DEFAULT_LANGUAGE = "hi";

  private final YaProjectEditor projectEditor;
  private final FlexTable table;
  private final Map<String, Map<String, String>> translationValues;
  private final Map<String, TranslationEntry> translationEntries;
  private final Map<String, DynamicTranslationEntry> dynamicTranslationEntries;
  private final List<String> languages;
  private final TextBox languageTextBox;
  private final ListBox languageListBox;
  private final TextBox dynamicKeyTextBox;
  private final TextBox dynamicBaseTextBox;
  private final TextBox dynamicPlaceholdersTextBox;
  private String selectedLanguage;
  private static final String LOCATOR_SEPARATOR = "\u0000";
  private final Map<String, String> locatorToTranslationKey;

  private boolean savedTranslationsLoaded;

  private static final Logger LOG = Logger.getLogger(TranslationPanel.class.getName());

  public TranslationPanel(YaProjectEditor projectEditor) {
    this.projectEditor = projectEditor;
    this.table = new FlexTable();
    this.translationValues = new HashMap<String, Map<String, String>>();
    this.translationEntries = new HashMap<String, TranslationEntry>();
    this.dynamicTranslationEntries = new HashMap<String, DynamicTranslationEntry>();
    this.savedTranslationsLoaded = false;
    this.languages = new ArrayList<String>();
    this.languages.add(DEFAULT_LANGUAGE);
    this.selectedLanguage = DEFAULT_LANGUAGE;
    this.languageTextBox = new TextBox();
    this.languageListBox = new ListBox();
    this.dynamicKeyTextBox = new TextBox();
    this.dynamicBaseTextBox = new TextBox();
    this.dynamicPlaceholdersTextBox = new TextBox();
    this.locatorToTranslationKey = new HashMap<String, String>();

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

    Label dynamicLabel = new Label("Dynamic translations:");
    dynamicLabel.setStylePrimaryName("ode-i18n-subtitle");

    Label dynamicHelpLabel = new Label(
        "Create user-defined message keys for runtime lookup. "
            + "Example key: welcome_message, base text: Hello {name}, placeholders: name");

    dynamicKeyTextBox.setWidth("180px");
    dynamicKeyTextBox.getElement().setPropertyString("placeholder", "welcome_message");

    dynamicBaseTextBox.setWidth("320px");
    dynamicBaseTextBox.getElement().setPropertyString("placeholder", "Hello {name}");

    dynamicPlaceholdersTextBox.setWidth("180px");
    dynamicPlaceholdersTextBox.getElement().setPropertyString("placeholder", "name,count");

    Button addDynamicButton = new Button("Add Dynamic Key");
    addDynamicButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        addDynamicTranslationEntry();
      }
    });

    root.add(dynamicLabel);
    root.add(dynamicHelpLabel);
    root.add(new Label("Key:"));
    root.add(dynamicKeyTextBox);
    root.add(new Label("Base text:"));
    root.add(dynamicBaseTextBox);
    root.add(new Label("Placeholders:"));
    root.add(dynamicPlaceholdersTextBox);
    root.add(addDynamicButton);

    root.add(table);

    Label languageLabel = new Label("Language code, e.g. hi, es, pt-BR:");
    languageTextBox.setWidth("80px");

    Button addLanguageButton = new Button("Add Language");
    addLanguageButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        addLanguage(languageTextBox.getValue(), true);
        languageTextBox.setValue("");
        refresh();
      }
    });

    Label languagesLabel = new Label("Languages:");
    languageListBox.setVisibleItemCount(1);
    languageListBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int selectedIndex = languageListBox.getSelectedIndex();
        if (selectedIndex >= 0) {
          selectedLanguage = languageListBox.getItemText(selectedIndex);
          refresh();
        }
      }
    });

    Button deleteLanguageButton = new Button("Delete Language");
    deleteLanguageButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        deleteSelectedLanguage();
      }
    });

    root.add(languageLabel);
    root.add(languageTextBox);
    root.add(addLanguageButton);
    root.add(exportButton);
    root.add(saveButton);
    root.add(languagesLabel);
    root.add(languageListBox);
    root.add(deleteLanguageButton);

    initWidget(root);
  }



  public void refresh() {
    loadSavedTranslations();
    ensureSelectedLanguage();
    refreshLanguageListBox();

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
          String generatedKey = getOrCreateTranslationKey(formName,
              componentName, propertyName);
          translationEntries.put(generatedKey, new TranslationEntry(generatedKey, formName,
              componentName, componentType, propertyName, propertyValue));

          table.setText(row, 0, formName);
          table.setText(row, 1, componentName);
          table.setText(row, 2, componentType);
          table.setText(row, 3, propertyName);
          table.setText(row, 4, generatedKey);
          table.setText(row, 5, propertyValue);
          table.setWidget(row, 6, createTranslationTextBox(generatedKey, selectedLanguage));
          table.setText(row, 7, "");
          row++;
        }
      }
    }
    row = addDynamicRows(row);
  }

  private int addDynamicRows(int row) {
    ArrayList<String> keys = new ArrayList<String>(dynamicTranslationEntries.keySet());
    Collections.sort(keys);

    for (final String key : keys) {
      DynamicTranslationEntry entry = dynamicTranslationEntries.get(key);
      if (entry == null) {
        continue;
      }

      Button deleteButton = new Button("Delete");
      deleteButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          deleteDynamicTranslationEntry(key);
        }
      });

      table.setText(row, 0, "Dynamic");
      table.setText(row, 1, "");
      table.setText(row, 2, "Dynamic");
      table.setText(row, 3, "Message");
      table.setText(row, 4, key);
      table.setText(row, 5, entry.getBaseText());
      table.setWidget(row, 6, createTranslationTextBox(key, selectedLanguage));
      table.setWidget(row, 7, deleteButton);
      row++;
    }

    return row;
  }

  private void addHeader() {
    table.setText(0, 0, "Screen");
    table.setText(0, 1, "Component");
    table.setText(0, 2, "Type");
    table.setText(0, 3, "Property");
    table.setText(0, 4, "Internal Key");
    table.setText(0, 5, "Base Text");
    table.setText(0, 6, selectedLanguage);
    table.setText(0, 7, "Actions");
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

  private void saveJsonSilently() {
    String json = exportJson();

    projectEditor.changeProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_I18N_TRANSLATIONS,
        json);

    projectEditor.saveProjectSettings(null);
  }

  private void saveJson() {
    final String json = exportJson();

    String currentJson = projectEditor.getProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_I18N_TRANSLATIONS);

    projectEditor.changeProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_I18N_TRANSLATIONS,
        json);

    if (json.equals(currentJson)) {
      showJsonDialog("Translations Saved",
          "No translation changes were pending.",
          json);
      return;
    }

    projectEditor.saveProjectSettings(new Command() {
      @Override
      public void execute() {
        showJsonDialog("Translations Saved",
            "Saved translations to project settings.",
            json);
      }
    });
  }

  private String getJsonString(JSONObject object, String name) {
    if (object == null || object.get(name) == null || object.get(name).isString() == null) {
      return "";
    }

    return object.get(name).isString().stringValue();
  }

  private List<String> getJsonStringArray(JSONObject object, String name) {
    List<String> values = new ArrayList<String>();

    if (object == null || object.get(name) == null || object.get(name).isArray() == null) {
      return values;
    }

    JSONArray array = object.get(name).isArray();
    for (int i = 0; i < array.size(); i++) {
      JSONValue value = array.get(i);
      if (value != null && value.isString() != null) {
        values.add(value.isString().stringValue());
      }
    }

    return values;
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
            addLanguage(languageValue.isString().stringValue(), false);
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

        String kind = getJsonString(entry, "kind");
        if ("dynamic".equals(kind)) {
          String baseText = getJsonString(entry, "baseText");
          List<String> placeholders = getJsonStringArray(entry, "placeholders");

          dynamicTranslationEntries.put(key,
              new DynamicTranslationEntry(key, baseText, placeholders));
        }

        JSONValue sourceValue = entry.get("source");
        JSONObject source = null;
        if (sourceValue != null && sourceValue.isObject() != null) {
          source = sourceValue.isObject();
        }

        if (source != null) {
          String screenName = getJsonString(source, "screen");
          String componentName = getJsonString(source, "component");
          String propertyName = getJsonString(source, "property");

          if (screenName.length() > 0 && componentName.length() > 0
              && propertyName.length() > 0) {
            locatorToTranslationKey.put(makeLocator(screenName, componentName, propertyName),
                key);
          }
        }

        JSONValue translationsValue = entry.get("translations");
        if (translationsValue == null || translationsValue.isObject() == null) {
          continue;
        }

        JSONObject translations = translationsValue.isObject();
        for (String language : translations.keySet()) {
          JSONValue translatedValue = translations.get(language);
          if (translatedValue != null && translatedValue.isString() != null) {
            addLanguage(language, false);
            setTranslationValue(key, language, translatedValue.isString().stringValue());
          }
        }
      }
    } catch (RuntimeException e) {
      // Ignore invalid saved data for now. The table can still rebuild from the current project.
    }
  }

  public void handleComponentRenamed(String screenName, String oldName, String newName) {
    loadSavedTranslations();

    if (oldName == null || oldName.length() == 0 || newName == null || newName.length() == 0
        || oldName.equals(newName)) {
      return;
    }

    boolean changed = false;
    ArrayList<String> oldLocators = new ArrayList<String>(locatorToTranslationKey.keySet());

    for (String oldLocator : oldLocators) {
      String[] parts = splitLocator(oldLocator);
      if (parts.length != 3) {
        continue;
      }

      if (!screenName.equals(parts[0]) || !oldName.equals(parts[1])) {
        continue;
      }

      String propertyName = parts[2];
      String key = locatorToTranslationKey.remove(oldLocator);
      String newLocator = makeLocator(screenName, newName, propertyName);

      LOG.info("i18n preserving key on rename: key=" + key
          + " oldLocator=" + oldLocator
          + " newLocator=" + newLocator);

      locatorToTranslationKey.put(newLocator, key);
      changed = true;
    }

    refresh();

    if (changed) {
      saveJsonSilently();
    }
  }

  public void handleComponentRemoved(String screenName, String componentName) {
    loadSavedTranslations();

    if (componentName == null || componentName.length() == 0) {
      return;
    }

    boolean changed = false;
    ArrayList<String> locatorsToRemove = new ArrayList<String>();

    for (String locator : locatorToTranslationKey.keySet()) {
      String[] parts = splitLocator(locator);
      if (parts.length != 3) {
        continue;
      }

      if (screenName.equals(parts[0]) && componentName.equals(parts[1])) {
        locatorsToRemove.add(locator);
      }
    }

    for (String locator : locatorsToRemove) {
      String key = locatorToTranslationKey.remove(locator);
      if (key != null) {
        translationValues.remove(key);
        translationEntries.remove(key);
        changed = true;
      }
    }

    refresh();

    if (changed) {
      saveJsonSilently();
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
      entryObject.put("kind", new JSONString("static"));

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

    ArrayList<String> dynamicKeys = new ArrayList<String>(dynamicTranslationEntries.keySet());
    Collections.sort(dynamicKeys);

    for (String key : dynamicKeys) {
      DynamicTranslationEntry entry = dynamicTranslationEntries.get(key);
      if (entry == null) {
        continue;
      }

      JSONObject entryObject = new JSONObject();
      entryObject.put("kind", new JSONString("dynamic"));
      entryObject.put("baseText", new JSONString(entry.getBaseText()));

      JSONArray placeholders = new JSONArray();
      for (int i = 0; i < entry.getPlaceholders().size(); i++) {
        placeholders.set(i, new JSONString(entry.getPlaceholders().get(i)));
      }
      entryObject.put("placeholders", placeholders);

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

  private void addDynamicTranslationEntry() {
    loadSavedTranslations();

    String key = dynamicKeyTextBox.getValue();
    String baseText = dynamicBaseTextBox.getValue();
    String placeholdersText = dynamicPlaceholdersTextBox.getValue();

    if (key != null) {
      key = key.trim();
    }

    if (baseText != null) {
      baseText = baseText.trim();
    }

    if (!isValidDynamicKey(key)) {
      Window.alert("Use a safe dynamic key such as welcome_message or errors.network_timeout.");
      return;
    }

    if (baseText == null || baseText.length() == 0) {
      Window.alert("Base text is required for a dynamic translation.");
      return;
    }

    if (dynamicTranslationEntries.containsKey(key) || translationEntries.containsKey(key)) {
      Window.alert("A translation key with this name already exists.");
      return;
    }

    List<String> placeholders = parsePlaceholders(placeholdersText);
    if (placeholders == null) {
      Window.alert("Placeholders must be comma-separated names like name, count, or user_id.");
      return;
    }

    dynamicTranslationEntries.put(key, new DynamicTranslationEntry(key, baseText, placeholders));

    dynamicKeyTextBox.setValue("");
    dynamicBaseTextBox.setValue("");
    dynamicPlaceholdersTextBox.setValue("");

    refresh();
  }

  private void deleteDynamicTranslationEntry(String key) {
    if (key == null || key.length() == 0) {
      return;
    }

    boolean confirmed = Window.confirm("Delete dynamic translation key '" + key + "'?");
    if (!confirmed) {
      return;
    }

    dynamicTranslationEntries.remove(key);
    translationValues.remove(key);

    refresh();
  }

  private boolean isValidDynamicKey(String key) {
    return key != null
        && key.length() > 0
        && key.matches("[A-Za-z][A-Za-z0-9_.-]*");
  }

  private List<String> parsePlaceholders(String placeholdersText) {
    List<String> placeholders = new ArrayList<String>();

    if (placeholdersText == null || placeholdersText.trim().length() == 0) {
      return placeholders;
    }

    String[] parts = placeholdersText.split(",");
    for (String part : parts) {
      String placeholder = part.trim();
      if (placeholder.length() == 0) {
        continue;
      }

      if (!isValidPlaceholderName(placeholder)) {
        return null;
      }

      if (!placeholders.contains(placeholder)) {
        placeholders.add(placeholder);
      }
    }

    return placeholders;
  }

  private boolean isValidPlaceholderName(String placeholder) {
    return placeholder != null
        && placeholder.length() > 0
        && placeholder.matches("[A-Za-z_][A-Za-z0-9_]*");
  }

  private void addLanguage(String language, boolean selectLanguage) {
    if (language == null) {
      return;
    }

    language = language.trim();

    if (!isValidLanguageCode(language)) {
      Window.alert("Use a language code such as hi, es, fr, or pt-BR.");
      return;
    }

    if (!languages.contains(language)) {
      languages.add(language);
    }

    if (selectLanguage) {
      selectedLanguage = language;
    }
  }

  private boolean isValidLanguageCode(String language) {
    return language != null
        && language.matches("[a-z]{2,3}(-[A-Z]{2})?");
  }

  private void ensureSelectedLanguage() {
    if (languages.isEmpty()) {
      languages.add(DEFAULT_LANGUAGE);
    }

    if (selectedLanguage == null || !languages.contains(selectedLanguage)) {
      selectedLanguage = languages.get(0);
    }
  }

  private void refreshLanguageListBox() {
    languageListBox.clear();

    for (int i = 0; i < languages.size(); i++) {
      String language = languages.get(i);
      languageListBox.addItem(language);

      if (language.equals(selectedLanguage)) {
        languageListBox.setSelectedIndex(i);
      }
    }
  }

  private void deleteSelectedLanguage() {
    ensureSelectedLanguage();

    if (languages.size() <= 1) {
      Window.alert("At least one translation language must remain.");
      return;
    }

    String languageToDelete = selectedLanguage;
    boolean confirmed = Window.confirm("Delete language '" + languageToDelete
        + "' and all translation values for this language?");

    if (!confirmed) {
      return;
    }

    languages.remove(languageToDelete);

    ArrayList<String> emptyKeys = new ArrayList<String>();
    for (String key : translationValues.keySet()) {
      Map<String, String> values = translationValues.get(key);
      if (values == null) {
        continue;
      }

      values.remove(languageToDelete);
      if (values.isEmpty()) {
        emptyKeys.add(key);
      }
    }

    for (String key : emptyKeys) {
      translationValues.remove(key);
    }

    selectedLanguage = languages.get(0);
    refresh();
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

  private String makeLocator(String screenName, String componentName, String propertyName) {
    return safe(screenName) + LOCATOR_SEPARATOR
        + safe(componentName) + LOCATOR_SEPARATOR
        + safe(propertyName);
  }

  private String[] splitLocator(String locator) {
    return locator.split(LOCATOR_SEPARATOR, -1);
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }

  private String getOrCreateTranslationKey(String screenName, String componentName,
      String propertyName) {
    String locator = makeLocator(screenName, componentName, propertyName);
    String existingKey = locatorToTranslationKey.get(locator);
    if (existingKey != null && existingKey.length() > 0) {
      return existingKey;
    }

    String generatedKey = TranslationKeyGenerator.generate(screenName, componentName, propertyName);
    locatorToTranslationKey.put(locator, generatedKey);
    return generatedKey;
  }

}
