package com.pavi2410;

import android.view.View;
import android.widget.TextView;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;

@DesignerComponent(version = 1,
        description = "Simple Label extension",
        category = ComponentCategory.EXTENSION,
        iconName = "images/extension.png",
        hasCustomMock = true)
@SimpleObject(external = true)
public class SimpleLabel extends AndroidViewComponent {

    private TextView tv;

    public SimpleLabel(ComponentContainer container) {
        super(container.$form());

        tv = new TextView(container.$context());

        container.$add(this);
    }

    @Override
    public View getView() {
        return tv;
    }

    @DesignerProperty
    @SimpleProperty
    public void Text(String text) {
        tv.setText(text);
    }
}
