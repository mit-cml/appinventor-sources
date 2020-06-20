package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import static com.google.appinventor.client.Ode.MESSAGES;

public class MockTab extends MockHVArrangement {
  
  public static final String TYPE = "Tab";
  public static final String PROPERTY_NAME_ICON = "Icon";
  public static final String PROPERTY_NAME_TEXT = "Text";
  public static final String PROPERTY_NAME_SHOW_TEXT = "ShowText";
  public static final String PROPERTY_NAME_SHOW_ICON = "ShowIcon";
  
  private static final int ANDROID_TAB_HEIGHT = 48;
  
  private final AbsolutePanel tab;
  private Image tabImage = null;
  private Label tabLabel;
  private String imagePath = "";
  
  /**
   * Creates a new MockVerticalArrangement component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockTab(SimpleEditor editor) {
    super(editor, TYPE, images.tab(),
        ComponentConstants.LAYOUT_ORIENTATION_VERTICAL,
        ComponentConstants.SCROLLABLE_ARRANGEMENT);
    tab = new AbsolutePanel();
    tab.setWidth("48px");
    tab.setHeight("48px");
    tab.setStylePrimaryName("ode-Tab");
    tabLabel = new Label();
    tab.add(tabLabel);
    layoutWidget.setHeight("100%");
    layoutWidget.removeStyleName("ode-SimpleMockContainer");
    layoutWidget.addHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        MockTab.this.select(clickEvent.getNativeEvent());
      }
    }, ClickEvent.getType());
    initComponent(tab);
  }
  
  public Label getTabLabel() {
    return tabLabel;
  }
  
  protected void addWidthHeightProperties() {
    // Tabs always fill their TabArrangement.
  }
  
  @Override
  public void onCreateFromPalette() {
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }
  
  @Override
  int getWidthHint() {
    return LENGTH_FILL_PARENT;
  }
  
  @Override
  int getHeightHint() {
    return ANDROID_TAB_HEIGHT;
  }
  
  public Widget getTabContentView() {
    return layoutWidget;
  }
  
  @Override
  protected void onSelectedChange(boolean selected) {
    if (selected) {
      if (getContainer() instanceof MockTabArrangement) {
        ((MockTabArrangement) getContainer()).selectTab(this);
      }
    }
    super.onSelectedChange(selected);
  }
  
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    
    if (PROPERTY_NAME_ICON.equals(propertyName)) {
      setIconProperty(newValue);
    } else if (PROPERTY_NAME_TEXT.equals(propertyName)) {
      tabLabel.setText(newValue);
    } else if (PROPERTY_NAME_SHOW_TEXT.equals(propertyName)) {
      setShowText(newValue);
    } else if (PROPERTY_NAME_SHOW_ICON.equals(propertyName)) {
      setShowIcon(newValue);
    }
  }
  
  @SuppressWarnings("Convert2Lambda")
  private void setIconProperty(String newValue) {
    if (tabImage == null) {
      tabImage = new Image();
      tabImage.addErrorHandler(new ErrorHandler() {
        @Override
        public void onError(ErrorEvent errorEvent) {
          refreshForm(true);
        }
      });
      tabImage.addLoadHandler(new LoadHandler() {
        @Override
        public void onLoad(LoadEvent loadEvent) {
          refreshForm(true);
        }
      });
      tabImage.setWidth("24px");
      tabImage.setHeight("24px");
    }
    String url = convertImagePropertyValueToUrl(newValue);
    if (url == null) {
      tabImage.removeFromParent();
    } else {
      tabImage.setUrl(url);
      if (!tabImage.isAttached()) {
        tab.insert(tabImage, 0);
      }
    }
    imagePath = newValue;
  }
  
  private void setShowText(String newValue) {
    if ("True".equals(newValue)) {
      if (!tabLabel.isAttached()) {
        tab.add(tabLabel);
      }
    } else {
      tabLabel.removeFromParent();
    }
  }
  
  private void setShowIcon(String newValue) {
    if (!imagePath.isEmpty()) {
      if ("True".equals(newValue)) {
        if (!tabImage.isAttached()) {
          tab.insert(tabImage, 0);
        }
      } else {
        tabImage.removeFromParent();
      }
    }
  }
}