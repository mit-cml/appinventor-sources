package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import static com.google.appinventor.components.common.ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL;

public class MockTabArrangement extends MockContainer<MockHVLayout> {
  
  public static final String TYPE = "TabArrangement";
  
  private final SimplePanel tabContentView;
  
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
    
    tabContentView = new SimplePanel();
    tabContentView.setStylePrimaryName("ode-TabContentView");
    
    AbsolutePanel tabArrangement = new AbsolutePanel();
    tabArrangement.add(tabContentView);
    tabArrangement.add(rootPanel);
    tabArrangement.setStylePrimaryName("ode-SimpleMockContainer");
    rootPanel.setStylePrimaryName("ode-TabContainer");
    rootPanel.getElement().getStyle().clearPosition();
    
    initComponent(tabArrangement);
  }
  
  @Override
  protected boolean acceptableSource(DragSource source) {
    if (source instanceof SimplePaletteItem) {
      return source.getDragWidget() instanceof MockTab;
    } else {
      return source instanceof MockTab;
    }
  }
  
  @Override
  protected void addComponent(MockComponent component, int beforeVisibleIndex) {
    super.addComponent(component, beforeVisibleIndex);
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
  
  public void selectTab(MockTab mockTab) {
    tabContentView.setWidget(mockTab.getTabContentView());
  }
}