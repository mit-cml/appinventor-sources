package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Defines a widget that has the appearance of a red close button.
 * The Widget is clicked to delete the associated component
 */
public class ComponentRemoveWidget extends Image {
    private static ImageResource imageResource = null;

    public ComponentRemoveWidget(SimpleComponentDescriptor scd) {
        if (imageResource == null) {
            Images images = Ode.getImageBundle();
            imageResource = images.deleteComponent();
        }
        AbstractImagePrototype.create(imageResource).applyTo(this);
        addClickListener(new ClickListener() {

            @Override
            public void onClick(Widget widget) {
                Window.confirm("Delete Coming Soooon!!!");
            }
        });
    }
}
