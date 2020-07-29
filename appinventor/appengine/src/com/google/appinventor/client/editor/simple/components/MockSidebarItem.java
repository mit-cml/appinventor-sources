package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.InlineHTML;

import static com.google.appinventor.client.explorer.youngandroid.ReportList.MESSAGES;

public class MockSidebarItem extends MockVisibleComponent {

    // Component type names
    public static final String TYPE = "SidebarItem";

    // Property names
    private static final String PROPERTY_NAME_TEXT = "Text";

    // GWT widget used to mock a sidebar item
    private InlineHTML itemWidget;

    /**
     * Creates a new instance of a visible component.
     *
     * @param editor editor of source file the component belongs to
     */
    public MockSidebarItem(SimpleEditor editor) {
        super(editor, TYPE, images.sidebar());

        // Initialize mock sidebar item UI
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
