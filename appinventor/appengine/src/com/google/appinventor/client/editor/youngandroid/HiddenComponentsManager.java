// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.components.MockForm;

import java.util.logging.Logger;

/**
 * The HiddenComponentsManager keeps track of which screen you’re on and what its checkbox should do.
 * solved issue #3365(Hidden Components Checkbox not working );
 */

public class HiddenComponentsManager {
    private static final HiddenComponentsManager INSTANCE = new HiddenComponentsManager();
    private MockForm currentForm;
    private static final Logger LOG = Logger.getLogger(HiddenComponentsManager.class.getName());

    private HiddenComponentsManager() {}

    public static HiddenComponentsManager getInstance() {
        return INSTANCE;
    }

    public void setCurrentForm(MockForm form) {
        LOG.info("Setting current form to: " + (form != null ? form.getTitle() : "null"));
        this.currentForm = form;
    }

    public void toggle() {
        if (currentForm == null) {
            LOG.warning("Toggle called but no current form is set");
            return;
        }
        String screenTitle = currentForm.getTitle();
        Boolean currentState = Ode.getCurrentProjectEditor().getScreenCheckboxState(screenTitle);
        boolean effectiveCurrentState = (currentState != null) ? currentState : false;
        boolean newState = !effectiveCurrentState;
        LOG.info("Screen " + screenTitle + ": Changing state from " + effectiveCurrentState + " to " + newState);
        Ode.getCurrentProjectEditor().setScreenCheckboxState(screenTitle, newState);
        currentForm.doRefresh();
    }

    public boolean getState(String screen) {
        Boolean state = Ode.getCurrentProjectEditor().getScreenCheckboxState(screen);
        return state != null ? state : false;
    }
}
