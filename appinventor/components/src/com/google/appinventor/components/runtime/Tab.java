package com.google.appinventor.components.runtime;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.TAB_COMPONENT_VERSION,
    category = ComponentCategory.LAYOUT)
@SimpleObject
public class Tab extends HVArrangement<ViewGroup> implements Component, ComponentContainer {
  private static final String LOG_TAG = Tab.class.getSimpleName();
  private com.google.android.material.tabs.TabLayout.Tab tab;
  private String text = "";
  
  public Tab (TabArrangement container) {
    super(container, HVArrangement.LAYOUT_ORIENTATION_VERTICAL, new FrameLayout(container.$context()));
    Log.d("tabarrangement" ,"Constructor for tab: " + this);
    container.addTab(this);
  }
  
  public com.google.android.material.tabs.TabLayout.Tab getTab() {
    return tab;
  }
  
  public void setTab (TabLayout.Tab tab) {
    this.tab = tab;
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Text(String text) {
    Log.d("tabarrangement", "Setting text for tab: " + this + " " + tab + " to: " + text);
    tab.setText(text);
  }
  
  @SimpleProperty
  public String Text() {
    CharSequence text = tab.getText();
    this.text = text == null ? "" : text.toString();
    return this.text;
  }
  
  @SimpleEvent
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
  }
  
  @SimpleFunction
  public void Show() {
    tab.select();
  }
}