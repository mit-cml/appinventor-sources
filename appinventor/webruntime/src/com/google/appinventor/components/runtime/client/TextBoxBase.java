package com.google.appinventor.components.runtime.client;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.PropertyCategory;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

@SimpleObject
public abstract class TextBoxBase extends AndroidViewComponent {

    protected final TextBox textBox;

    public TextBoxBase(ComponentContainer container, com.google.gwt.user.client.ui.TextBox textBox) {
        super(container);
        this.textBox = textBox;
    }

    @SimpleProperty(
        category = PropertyCategory.BEHAVIOR,
        description = "Gets the text in the TextBox."
    )
    public String Text() {
        return textBox.getText();
    }

    @SimpleProperty(
        description = "Sets the text in the TextBox."
    )
    public void Text(String text) {
        textBox.setText(text);
    }

    // @Override
    public com.google.gwt.user.client.ui.Widget getView() {
        return textBox;
    }

    
}
