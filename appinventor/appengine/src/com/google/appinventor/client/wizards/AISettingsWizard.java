// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.shared.rpc.aiagent.BYOKCatalog;
import com.google.appinventor.shared.rpc.aiagent.BYOKCatalog.ModelInfo;
import com.google.appinventor.shared.rpc.aiagent.BYOKCatalog.Provider;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Modal dialog where the user configures BYOK (Bring Your Own Key) for the
 * AI Agent: provider, model, API key, optional base URL, optional reasoning
 * effort. Persists via {@link com.google.appinventor.client.settings.user.UserSettings#saveSettings}.
 *
 * <p>Mirrors the {@code UISettingsWizard} pattern: a single class reused by
 * both UI themes (classic and neo).
 */
public class AISettingsWizard {

  interface AISettingsWizardUiBinder extends UiBinder<Dialog, AISettingsWizard> {}

  @UiField protected Dialog uiDialog;
  @UiField protected Button applyButton;
  @UiField protected Button cancelButton;
  @UiField protected Button clearButton;
  @UiField protected Button topInvisible;
  @UiField protected Button bottomInvisible;
  @UiField protected Label errorLabel;

  @UiField protected ListBox providerSelector;
  @UiField protected FlowPanel modelDropdownRow;
  @UiField protected ListBox modelSelector;
  @UiField protected FlowPanel modelTextRow;
  @UiField protected TextBox modelInput;
  @UiField protected PasswordTextBox apiKeyInput;
  @UiField protected FlowPanel baseUrlRow;
  @UiField protected TextBox baseUrlInput;
  @UiField protected FlowPanel reasoningRow;
  @UiField protected ListBox reasoningSelector;

  private HandlerRegistration resizeHandler;

  public AISettingsWizard() {
    bindUI();
    populateStatic();
    loadFromSettings();
    show();
  }

  private void bindUI() {
    AISettingsWizardUiBinder binder = GWT.create(AISettingsWizardUiBinder.class);
    binder.createAndBindUi(this);
    resizeHandler = Window.addResizeHandler(event -> {
      if (uiDialog.isShowing()) {
        uiDialog.center();
      }
    });
  }

  private void populateStatic() {
    providerSelector.addItem(MESSAGES.aiSettingsProviderDefaultOption(), "");
    for (Provider p : Provider.values()) {
      providerSelector.addItem(p.wireName(), p.wireName());
    }
  }

  private void loadFromSettings() {
    Settings g = generalSettings();
    String provider = nullToEmpty(g.getPropertyValue(
        SettingsConstants.AI_AGENT_BYOK_PROVIDER));
    String model = nullToEmpty(g.getPropertyValue(
        SettingsConstants.AI_AGENT_BYOK_MODEL));
    String baseUrl = nullToEmpty(g.getPropertyValue(
        SettingsConstants.AI_AGENT_BYOK_BASE_URL));
    String reasoning = nullToEmpty(g.getPropertyValue(
        SettingsConstants.AI_AGENT_BYOK_REASONING));

    selectByValue(providerSelector, provider);
    rebuildModelDropdown();
    if (!model.isEmpty()) {
      Provider p = Provider.fromWireName(provider);
      if (p != null && BYOKCatalog.hasFreeTextModel(p)) {
        modelInput.setValue(model);
      } else {
        selectByValue(modelSelector, model);
      }
    }
    // API key is write-only: never round-tripped to client. Show empty.
    apiKeyInput.setValue("");
    baseUrlInput.setValue(baseUrl);
    rebuildReasoningDropdown();
    selectByValue(reasoningSelector, reasoning);
    refreshFieldVisibility();
  }

  // ---- UI handlers ----

  @UiHandler("providerSelector")
  protected void onProviderChange(ChangeEvent e) {
    rebuildModelDropdown();
    rebuildReasoningDropdown();
    refreshFieldVisibility();
  }

  @UiHandler("modelSelector")
  protected void onModelChange(ChangeEvent e) {
    rebuildReasoningDropdown();
    refreshFieldVisibility();
  }

  @UiHandler("modelInput")
  protected void onModelTextChange(ChangeEvent e) {
    rebuildReasoningDropdown();
    refreshFieldVisibility();
  }

  @UiHandler("cancelButton")
  protected void onCancel(ClickEvent e) {
    hide();
  }

  @UiHandler("clearButton")
  protected void onClear(ClickEvent e) {
    Settings g = generalSettings();
    g.changePropertyValue(SettingsConstants.AI_AGENT_BYOK_PROVIDER, "");
    g.changePropertyValue(SettingsConstants.AI_AGENT_BYOK_MODEL, "");
    g.changePropertyValue(SettingsConstants.AI_AGENT_BYOK_API_KEY, "");
    g.changePropertyValue(SettingsConstants.AI_AGENT_BYOK_BASE_URL, "");
    g.changePropertyValue(SettingsConstants.AI_AGENT_BYOK_REASONING, "");
    Ode.getInstance().getUserSettings().saveSettings(null);
    hide();
  }

  @UiHandler("applyButton")
  protected void onApply(ClickEvent e) {
    String providerWire = providerSelector.getSelectedValue();
    if (providerWire == null || providerWire.isEmpty()) {
      // "Use server default" — clear all keys.
      onClear(null);
      return;
    }

    Provider provider = Provider.fromWireName(providerWire);
    if (provider == null) {
      // Defensive: unknown provider in selector.
      return;
    }

    String model = BYOKCatalog.hasFreeTextModel(provider)
        ? modelInput.getValue().trim()
        : modelSelector.getSelectedValue();
    String apiKey = apiKeyInput.getValue();
    String baseUrl = BYOKCatalog.requiresBaseUrl(provider)
        ? baseUrlInput.getValue().trim()
        : "";
    String reasoning = currentModelSupportsReasoning(provider, model)
        ? reasoningSelector.getSelectedValue()
        : "";

    if (!validate(provider, model, apiKey, baseUrl)) {
      return;
    }

    Settings g = generalSettings();
    g.changePropertyValue(SettingsConstants.AI_AGENT_BYOK_PROVIDER, providerWire);
    g.changePropertyValue(SettingsConstants.AI_AGENT_BYOK_MODEL, model);
    g.changePropertyValue(SettingsConstants.AI_AGENT_BYOK_API_KEY, apiKey);
    g.changePropertyValue(SettingsConstants.AI_AGENT_BYOK_BASE_URL, baseUrl);
    g.changePropertyValue(SettingsConstants.AI_AGENT_BYOK_REASONING, reasoning);
    Ode.getInstance().getUserSettings().saveSettings(null);
    hide();
  }

  @UiHandler("topInvisible")
  protected void focusLast(FocusEvent e) {
    applyButton.setFocus(true);
  }

  @UiHandler("bottomInvisible")
  protected void focusFirst(FocusEvent e) {
    providerSelector.setFocus(true);
  }

  // ---- helpers ----

  private boolean validate(Provider provider, String model, String apiKey,
                           String baseUrl) {
    boolean ok = !model.isEmpty() && !apiKey.isEmpty()
        && (!BYOKCatalog.requiresBaseUrl(provider) || !baseUrl.isEmpty());
    errorLabel.setVisible(!ok);
    if (!ok) {
      errorLabel.setText(MESSAGES.aiSettingsValidationIncomplete());
    }
    return ok;
  }

  private void refreshFieldVisibility() {
    String wire = providerSelector.getSelectedValue();
    Provider p = Provider.fromWireName(wire);
    boolean hasProvider = (p != null);
    boolean freeText = hasProvider && BYOKCatalog.hasFreeTextModel(p);
    String currentModel = currentModelId(p);

    modelDropdownRow.setVisible(hasProvider && !freeText);
    modelTextRow.setVisible(hasProvider && freeText);
    baseUrlRow.setVisible(hasProvider && BYOKCatalog.requiresBaseUrl(p));
    reasoningRow.setVisible(currentModelSupportsReasoning(p, currentModel));
    apiKeyInput.setEnabled(hasProvider);
    errorLabel.setVisible(false);
  }

  private void rebuildModelDropdown() {
    modelSelector.clear();
    Provider p = Provider.fromWireName(providerSelector.getSelectedValue());
    if (p == null || BYOKCatalog.hasFreeTextModel(p)) {
      return;
    }
    for (ModelInfo m : BYOKCatalog.models(p)) {
      modelSelector.addItem(m.getId(), m.getId());
    }
  }

  private void rebuildReasoningDropdown() {
    reasoningSelector.clear();
    reasoningSelector.addItem(MESSAGES.aiSettingsReasoningNone(), "");
    Provider p = Provider.fromWireName(providerSelector.getSelectedValue());
    String model = currentModelId(p);
    for (String opt : BYOKCatalog.reasoningOptions(p, model)) {
      reasoningSelector.addItem(opt, opt);
    }
  }

  private String currentModelId(Provider p) {
    if (p == null) {
      return "";
    }
    if (BYOKCatalog.hasFreeTextModel(p)) {
      return modelInput.getValue() == null ? "" : modelInput.getValue().trim();
    }
    String v = modelSelector.getSelectedValue();
    return v == null ? "" : v;
  }

  private boolean currentModelSupportsReasoning(Provider p, String model) {
    return p != null && !model.isEmpty()
        && !BYOKCatalog.reasoningOptions(p, model).isEmpty();
  }

  private void selectByValue(ListBox box, String value) {
    String v = value == null ? "" : value;
    for (int i = 0; i < box.getItemCount(); i++) {
      if (box.getValue(i).equals(v)) {
        box.setSelectedIndex(i);
        return;
      }
    }
    if (box.getItemCount() > 0) {
      box.setSelectedIndex(0);
    }
  }

  private Settings generalSettings() {
    return Ode.getInstance().getUserSettings()
        .getSettings(SettingsConstants.USER_GENERAL_SETTINGS);
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  public void show() {
    uiDialog.center();
    providerSelector.setFocus(true);
  }

  private void hide() {
    uiDialog.hide();
    if (resizeHandler != null) {
      resizeHandler.removeHandler();
      resizeHandler = null;
    }
  }
}
