package com.google.appinventor.components.runtime;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.TAB_COMPONENT_VERSION,
    category = ComponentCategory.LAYOUT)
@SimpleObject
public class Tab extends HVArrangement<ViewGroup> implements Component, ComponentContainer {
  private static final String LOG_TAG = Tab.class.getSimpleName();
  private final com.google.android.material.tabs.TabLayout.Tab tab;
  
  public Tab (TabArrangement container) {
    super(container, HVArrangement.LAYOUT_ORIENTATION_VERTICAL, new FrameLayout(container.$context()));
    tab = container.getTabLayout().newTab();
    container.addTab(this);
  }
  
  public com.google.android.material.tabs.TabLayout.Tab getTab() {
    return tab;
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