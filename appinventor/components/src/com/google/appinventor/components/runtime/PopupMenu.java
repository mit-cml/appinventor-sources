package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for MenuItems
 * @author singhalsara48@gmail.com (Sara Singhal)
 *
 */
@DesignerComponent(version = YaVersion.POPUP_MENU_COMPONENT_VERSION,
        category = ComponentCategory.LAYOUT,
        description = "Component for Popup menu to hold MenuItems.")
@SimpleObject
public class PopupMenu implements Component, ComponentContainer {

    private AndroidViewComponent component;
    private ComponentContainer container;

    private List<ContextMenuItem> items;

    /**
     * Create a new Popup Menu component.
     */
    public PopupMenu(ComponentContainer container) {
        this.container = container;
        items = new ArrayList<>();
    }

    public void showPopup(View view) {
        android.widget.PopupMenu popupMenu = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            popupMenu = new android.widget.PopupMenu(container.$context(),view);
            for (ContextMenuItem item : items) {
                item.addToPopupMenu(popupMenu);
            }
            popupMenu.show();
        }
    }

    public void addPopupMenuItem(ContextMenuItem item) {
        items.add(item);
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public AndroidViewComponent ComponentSelector() {
        return component;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COMPONENT )
    @SimpleProperty
    public void ComponentSelector(AndroidViewComponent component) {
        this.component=component;
        component.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);
            }
        });
    }



    // ComponentContainer implementation

    @Override
    public Activity $context() {
        return $form();
    }

    @Override
    public Form $form() {
        return $form();
    }

    @Override
    public void $add(AndroidViewComponent component) {
        throw new UnsupportedOperationException("Menu.$add() called");
    }

    @Override
    public void setChildWidth(AndroidViewComponent component, int width) {
        throw new UnsupportedOperationException("Menu.setChildWidth() called");
    }

    @Override
    public void setChildHeight(AndroidViewComponent component, int height) {
        throw new UnsupportedOperationException("Menu.setChildHeight() called");
    }

    @Override
    public int Width() {
        return 0;
    }

    @Override
    public int Height() {
        return 0;
    }

    // Component implementation

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return $form();
    }
}
