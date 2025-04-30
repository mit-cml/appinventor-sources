package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.components.common.ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL;

/**
 * Mock tab arrangement component.
 *
 * @author jsuyash1514@gmail.com (Suyash Jain)
 */
public class MockTabArrangement extends MockContainer<MockHVLayout> {
  private static final Logger LOG = Logger.getLogger(MockTabArrangement.class.getName());

  /**
   * Component type name.
   */
  public static final String TYPE = "TabArrangement";
  
  /**
   * Component properties.
   */
  public static final String PROPERTY_TAB_BACKGROUND_COLOR = "TabBackgroundColor";
  public static final String PROPERTY_TAB_TEXT_COLOR = "TabTextColor";
  public static final String PROPERTY_SELECTED_TAB_TEXT_COLOR = "SelectedTabTextColor";
  public static final String PROPERTY_TAB_BAR_POSITION = "TabBarPosition";
  public static final String PROPERTY_TAB_BAR_VISIBLE = "TabBarVisible";
  
  private final SimplePanel tabContentView;
  private static MockTab selectedTab;
  
  /**
   * Creates a new component container.
   * <p>
   * Implementations are responsible for constructing their own visual appearance
   * and calling {@link #initWidget(Widget)}.
   * This appearance should include {@link #rootPanel} so that children
   * components are displayed correctly.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockTabArrangement(SimpleEditor editor) {
    super(editor, TYPE, images.tabArrangement(), new MockHVLayout(LAYOUT_ORIENTATION_HORIZONTAL));
  
    // Initialize MockTabArrangement UI.
    tabContentView = new SimplePanel();
    tabContentView.setStylePrimaryName("ode-TabContentViewTop");
    AbsolutePanel tabArrangement = new AbsolutePanel();
    tabArrangement.add(rootPanel);
    tabArrangement.add(tabContentView);
    tabArrangement.setStylePrimaryName("ode-SimpleMockContainer");
    rootPanel.setStylePrimaryName("ode-TabContainer");
    rootPanel.getElement().getStyle().clearPosition();
    initComponent(tabArrangement);
  }
  
  @Override
  protected boolean acceptableSource(DragSource source) {
    // Tab Arrangement can only accept Tab component.
    if (source instanceof SimplePaletteItem) {
      return source.getDragWidget() instanceof MockTab;
    } else {
      return source instanceof MockTab;
    }
  }
  
  @Override
  protected void addComponent(MockComponent component, int beforeVisibleIndex) {
    super.addComponent(component, beforeVisibleIndex);
    Label label = ((MockTab)component).getTabLabel();
    if (component == selectedTab) {
      MockComponentsUtil.setWidgetTextColor(label, getPropertyValue(PROPERTY_SELECTED_TAB_TEXT_COLOR));
    } else {
      MockComponentsUtil.setWidgetTextColor(label, getPropertyValue(PROPERTY_TAB_TEXT_COLOR));
    }
    if (children.size() == 1) {
      tabContentView.setWidget(((MockTab) children.get(0)).getTabContentView());
    }
  }
  
  @Override
  int getWidthHint() {
    int widthHint = super.getWidthHint();
    if (widthHint == LENGTH_PREFERRED) {
      widthHint = LENGTH_FILL_PARENT;
    }
    return widthHint;
  }
  
  @Override
  int getHeightHint() {
    int heightHint = super.getHeightHint();
    if (heightHint == LENGTH_PREFERRED) {
      heightHint = LENGTH_FILL_PARENT;
    }
    return heightHint;
  }
  
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if (propertyName.equals(PROPERTY_TAB_BACKGROUND_COLOR)) {
      setPropertyTabBackgroundColor(newValue);
    } else if (propertyName.equals(PROPERTY_TAB_TEXT_COLOR)) {
      setPropertyTabTextColor(newValue);
    } else if (propertyName.equals(PROPERTY_SELECTED_TAB_TEXT_COLOR)) {
      setPropertySelectedTabTextColor(newValue);
    } else if (propertyName.equals(PROPERTY_TAB_BAR_POSITION)) {
      setPropertyTabBarPosition(newValue);
    } else if (propertyName.equals(PROPERTY_TAB_BAR_VISIBLE)) {
      setPropertyTabBarVisible(newValue);
    }
  }
  
  public void setPropertyTabBackgroundColor (String newValue) {
    // Set background color of root panel
    MockComponentsUtil.setWidgetBackgroundColor(rootPanel,newValue);
    // Set background color of all the tabs
    int nWidgets = rootPanel.getWidgetCount();
    for (int i = 0; i < nWidgets; i++) {
      Widget widget = rootPanel.getWidget(i);
      MockComponentsUtil.setWidgetBackgroundColor(widget,newValue);
    }
  }
  
  public void setPropertyTabTextColor (String newValue) {
    for (MockComponent mockComponent : children) {
      if (mockComponent instanceof  MockTab) {
        Label label = ((MockTab)mockComponent).getTabLabel();
        if (mockComponent == selectedTab) {
          MockComponentsUtil.setWidgetTextColor(label, getPropertyValue(PROPERTY_SELECTED_TAB_TEXT_COLOR));
        } else {
          MockComponentsUtil.setWidgetTextColor(label, newValue);
        }
      }
    }
  }
  
  public void setPropertySelectedTabTextColor (String newValue) {
    for (MockComponent mockComponent : children) {
      if (mockComponent instanceof  MockTab) {
        Label label = ((MockTab)mockComponent).getTabLabel();
        if (mockComponent == selectedTab) {
          MockComponentsUtil.setWidgetTextColor(label, newValue);
        } else {
          MockComponentsUtil.setWidgetTextColor(label, getPropertyValue(PROPERTY_TAB_TEXT_COLOR));
        }
      }
    }
  }
  
  public void setPropertyTabBarPosition(String newValue) {
    boolean tabBarVisible = true;
    try {
      tabBarVisible = Boolean.parseBoolean(getPropertyValue(PROPERTY_TAB_BAR_VISIBLE));
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Exception in retrieving tab bar visibility: ", e);
    }
    if (tabBarVisible) {
      try {
        switch (Integer.parseInt(newValue)) {
          case ComponentConstants.TAB_POSITION_DEFAULT:
            alignTabsAtTop();
            break;
          case ComponentConstants.TAB_POSITION_TOP:
            alignTabsAtTop();
            break;
          case ComponentConstants.TAB_POSITION_BOTTOM:
            alignTabsAtBottom();
            break;
          default:
            // This error should not happen because the higher level
            // setter for TabBarAlignment should screen out illegal inputs.
            ErrorReporter.reportError(MESSAGES.badValueForTabBarPosition(newValue));
        }
      } catch (NumberFormatException e) {
        // As above, this error should not happen
        ErrorReporter.reportError(MESSAGES.badValueForTabBarPosition(newValue));
      }
    }
  }
  
  public void setPropertyTabBarVisible (String newValue) {
    boolean tabBarVisible = Boolean.parseBoolean(newValue);
    rootPanel.setVisible(tabBarVisible);
    if (tabBarVisible) {
      tabContentView.removeStyleName("ode-TabContentView");
      setPropertyTabBarPosition(getPropertyValue(PROPERTY_TAB_BAR_POSITION));
    } else {
      tabContentView.removeStyleName("ode-TabContentViewBottom");
      tabContentView.removeStyleName("ode-TabContentViewTop");
      tabContentView.setStylePrimaryName("ode-TabContentView");
      refreshForm();
    }
  }
  
  public void alignTabsAtTop() {
    rootPanel.removeStyleName("ode-TabContainerBottom");
    rootPanel.setStylePrimaryName("ode-TabContainer");
    tabContentView.removeStyleName("ode-TabContentViewBottom");
    tabContentView.setStylePrimaryName("ode-TabContentViewTop");
    refreshForm();
  }
  
  public void alignTabsAtBottom() {
    rootPanel.removeStyleName("ode-TabContainer");
    rootPanel.setStylePrimaryName("ode-TabContainerBottom");
    tabContentView.removeStyleName("ode-TabContentViewTop");
    tabContentView.setStylePrimaryName("ode-TabContentViewBottom");
    refreshForm();
  }
  
  public void selectTab(MockTab mockTab) {
    if (selectedTab != null) {
      MockComponentsUtil.setWidgetTextColor(selectedTab.getTabLabel(), getPropertyValue(PROPERTY_TAB_TEXT_COLOR));
    }
    selectedTab = mockTab;
    MockComponentsUtil.setWidgetTextColor(mockTab.getTabLabel(), getPropertyValue(PROPERTY_SELECTED_TAB_TEXT_COLOR));
    tabContentView.setWidget(mockTab.getTabContentView());
  }
}
