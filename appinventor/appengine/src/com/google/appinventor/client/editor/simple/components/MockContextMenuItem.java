// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.InlineHTML;

/**
 * Mock Context Menu Item component.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */
public final class MockContextMenuItem extends MockVisibleComponent {

    // Component type name
    public static final String TYPE = "ContextMenuItem";

    // Property names
    private static final String PROPERTY_NAME_TEXT = "Text";

    // GWT widget used to mock a context menu item
    private InlineHTML itemWidget;

    /**
     * Creates a new MockContextMenuItem component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockContextMenuItem(SimpleEditor editor) {
        super(editor, TYPE, images.menuitem());

        // Initialize mock menu item UI
        itemWidget = new InlineHTML();
        itemWidget.setStylePrimaryName("ode-SimpleMockComponent");
        initComponent(itemWidget);
    }

    @Override
    protected boolean isPropertyVisible(String propertyName) {
        if (propertyName.equals(PROPERTY_NAME_WIDTH) ||
                propertyName.equals(PROPERTY_NAME_HEIGHT)) {
            return false;
        }
        return super.isPropertyVisible(propertyName);
    }

    @Override
    public void onCreateFromPalette() {
        // Change item text to component name
        changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
    }

    /*
     * Sets the item's Text property to a new value.
     */
    private void setTextProperty(String text) {
        itemWidget.setText(text);
    }

    // PropertyChangeListener implementation
    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);

        // Apply changed properties to the mock component
        if (propertyName.equals(PROPERTY_NAME_TEXT)) {
            setTextProperty(newValue);
            refreshForm();
        }
    }

}
