// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.PaintUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Use a tab arrangement component to arrange the group of components in tab format.
 *
 * This component is a formatting element in which to place components that should be displayed
 * in the tab format.
 *
 * This component uses `com.google.android.material.tabs.TabLayout` and `androidx.viewpager2.widget.ViewPager2`
 * to arrange the group of components in tab format. `TabLayout` provides a horizontal layout to display tabs.
 * ViewPager2 enables the paging of the tab content and provides a built-in swipe gestures to transition through
 * pages (or tabs) and displays screen slide animations during the transition.
 *
 * `TabArrangement` component accepts only the `Tab` component. Within each tab, components are
 * vertically aligned.
 *
 * If a `TabArrangement`'s {@link #Height()} property is set to `Automatic`, the actual height
 * of the arrangement is set to `Fill Parent`.
 *
 * If a `TabArrangement`'s {@link #Width()} property is set to `Automatic`, the actual width
 * of the arrangement is set to `Fill Parent`.
 *
 * @author jsuyash1514@gmail.com (Suyash Jain)
 */
@DesignerComponent(version = YaVersion.TABARRANGEMENT_COMPONENT_VERSION,
    category = ComponentCategory.LAYOUT,
    description = "<p>A formatting element in which to place components " +
        "that should be displayed in the tab format</p>")
@SimpleObject
@UsesLibraries({"cardview.jar", "cardview.aar", "material.jar", "material.aar", "recyclerview.aar", "recyclerview.jar", "viewpager2.jar", "viewpager2.aar"})
public class TabArrangement extends AndroidViewComponent<LinearLayout> implements
    ComponentContainer {
  private TabLayout tabLayout;
  private ViewPager2 viewPager;
  private RecyclerView.Adapter adapter;
  private List<Tab> tabs;
  private final LinearLayout layout;
  private int tabBackgroundColor;
  private int selectedTabIndicatorColor;
  private int textColor = COLOR_LTGRAY;
  private int selectedTabTextColor = COLOR_WHITE;
  private int tabBarPosition;
  private boolean tabBarVisible = true;

  /**
   * Creates a new TabArrangement component.
   *
   * @param container container, component will be placed in
   */
  public TabArrangement(ComponentContainer container) {
    super(container);
    layout = new LinearLayout($context());
    viewPager = new ViewPager2(container.$context());
    viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
    tabLayout = new TabLayout(container.$context());
    TabBackgroundColor(PaintUtil.hexStringToInt(ComponentConstants.DEFAULT_PRIMARY_COLOR));
    SelectedTabIndicatorColor(PaintUtil.hexStringToInt(ComponentConstants.DEFAULT_ACCENT_COLOR));
    TabTextColor(textColor);
    SelectedTabTextColor(selectedTabTextColor);
    tabLayout.setTabMode(TabLayout.MODE_FIXED);
    tabs = new ArrayList<>();

    // ViewPager2 uses the recycler view adapter, which allows us to paginate custom views.
    adapter = new RecyclerView.Adapter() {
      @Override
      public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // The method getItemViewType(int position) is overridden such that
        // view type of each list item is same as its position.
        int position = viewType;
        FrameLayout layout = new FrameLayout(viewGroup.getContext());
        if (tabs.get(position).isScrollable) {
          layout = new ScrollView(viewGroup.getContext());
        }
        layout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(layout) {
          @Override
          public String toString() {
            return super.toString();
          }
        };
        return viewHolder;
      }

      @Override
      public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        ViewGroup childViewGroup = tabs.get(i).viewLayout.getLayoutManager();
        if (childViewGroup.getParent() != null) {
          ((ViewGroup) childViewGroup.getParent()).removeView(childViewGroup);
        }
        ((ViewGroup) (viewHolder.itemView)).addView(childViewGroup);
      }

      @Override
      public int getItemViewType(int position) {
        return position;
      }

      @Override
      public int getItemCount() {
        return tabs.size();
      }
    };
    viewPager.setAdapter(adapter);

    /**
     * TabLayoutMediator is a mediator to link a TabLayout with a ViewPager2.
     * The mediator will synchronize the ViewPager2's position with the selected tab when a tab is selected,
     * and the TabLayout's scroll position when the user drags the ViewPager2.
     */
    new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
      @Override
      public void onConfigureTab(TabLayout.Tab tab, int i) {
        tabs.get(i).setTab(tab);
      }
    }).attach();
    container.$add(this);
  }

  @Override
  public LinearLayout getView() {
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setGravity(Gravity.TOP);
    if (tabLayout.getParent() != null) {
      ((ViewGroup) tabLayout.getParent()).removeView(tabLayout);
    }
    layout.addView(tabLayout, new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
    if (viewPager.getParent() != null) {
      ((ViewGroup) viewPager.getParent()).removeView(viewPager);
    }
    layout.addView(viewPager, new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, 0, 1));
    tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_BOTTOM);
    return layout;
  }

  void addTab(Tab tab) {
    tabLayout.addTab(tabLayout.newTab());
    tabs.add(tab);
    adapter.notifyDataSetChanged();
  }

  @Override
  public Activity $context() {
    return container.$context();
  }

  @Override
  public Form $form() {
    return container.$form();
  }

  @Override
  public void $add(AndroidViewComponent<? extends View> component) {
  }

  @Override
  public void setChildWidth(AndroidViewComponent<? extends View> component, int width) {
  }

  @Override
  public void setChildHeight(AndroidViewComponent<? extends View> component, int height) {
  }

  @Override
  public List<? extends Component> getChildren() {
    return Collections.unmodifiableList(tabs);
  }

  @Override
  public void setChildNeedsLayout(AndroidViewComponent<?> component) {
    // not needed for linear layout
  }

  /**
   * Indicates when the selected tab has changed in the TabArrangement.
   */
  @SimpleEvent
  public void ShowTab(Tab tab) {
    EventDispatcher.dispatchEvent(this, "ShowTab", tab);
  }

  /**
   * Specifies the horizontal width of the `TabArrangement`, measured in pixels.
   *
   * @param width in pixels
   */
  @Override
  @SimpleProperty(description = "Specifies the horizontal width of the %type%, measured in pixels.")
  public void Width(int width) {
    if (width == LENGTH_PREFERRED) {
      width = LENGTH_FILL_PARENT;
    }
    super.Width(width);
  }

  /**
   * Specifies the `TabArrangement`'s vertical height, measured in pixels.
   *
   * @param height in pixels
   */
  @Override
  @SimpleProperty(description = "Specifies the vertical height of the %type%, measured in pixels.")
  public void Height(int height) {
    if (height == LENGTH_PREFERRED) {
      height = LENGTH_FILL_PARENT;
    }
    super.Height(height);
  }

  /**
   * Returns the background color of the tab bar as an alpha-red-green-blue
   * integer.
   *
   * @return background RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the background color of the tab bar")
  @IsColor
  public int TabBackgroundColor() {
    return tabBackgroundColor;
  }

  /**
   * Specifies the background color of the tab bar as an alpha-red-green-blue
   * integer.
   *
   * @param argb background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = ComponentConstants.DEFAULT_PRIMARY_COLOR)
  @SimpleProperty(description = "Specifies the background color of the tab bar")
  public void TabBackgroundColor(int argb) {
    tabBackgroundColor = argb;
    tabLayout.setBackgroundColor(tabBackgroundColor);
  }

  /**
   * Returns the indicator color of the selected tab as an alpha-red-green-blue
   * integer.
   *
   * @return indicator RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the indicator color of the selected tab")
  @IsColor
  public int SelectedTabIndicatorColor() {
    return selectedTabIndicatorColor;
  }

  /**
   * Specifies the indicator color of the selected tab as an alpha-red-green-blue
   * integer.
   *
   * @param argb indicator RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = ComponentConstants.DEFAULT_ACCENT_COLOR)
  @SimpleProperty(description = "Specifies the indicator color of the selected tab")
  public void SelectedTabIndicatorColor(int argb) {
    selectedTabIndicatorColor = argb;
    tabLayout.setSelectedTabIndicatorColor(selectedTabIndicatorColor);
  }

  /**
   * Returns the text color of the tab label as an alpha-red-green-blue
   * integer.
   *
   * @return label text RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the text color of the tab label")
  @IsColor
  public int TabTextColor() {
    return textColor;
  }

  /**
   * Specifies the text color of the tab label as an alpha-red-green-blue
   * integer.
   *
   * @param argb label text RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
  @SimpleProperty(description = "Specifies the text color of the tab label")
  public void TabTextColor(int argb) {
    textColor = argb;
    tabLayout.setTabTextColors(textColor, selectedTabTextColor);
  }

  /**
   * Returns the text color of the label of selected tab as an alpha-red-green-blue
   * integer.
   *
   * @return label text RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the text color of the label of selected tab")
  @IsColor
  public int SelectedTabTextColor() {
    return selectedTabTextColor;
  }

  /**
   * Specifies the text color of the label of selected tab as an alpha-red-green-blue
   * integer.
   *
   * @param argb label text RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty(description = "Specifies the text color of the label of selected tab")
  public void SelectedTabTextColor(int argb) {
    selectedTabTextColor = argb;
    tabLayout.setTabTextColors(textColor, selectedTabTextColor);
  }

  /**
   * Returns a number that encodes the position of the tab bar.
   * The choices are: 1 = default, 2 = top, 3 = bottom.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns a number that encodes the position of the tab bar. " +
          "The choices are: 1 = default, 2 = top, 3 = bottom.")
  public int TabBarPosition() {
    return tabBarPosition;
  }

  /**
   * Specifies a number that encodes the position of the tab bar.
   * The choices are: 1 = default, 2 = top, 3 = bottom.
   *
   * @param tabBarPosition
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TAB_BAR_POSITION,
      defaultValue = ComponentConstants.TAB_POSITION_DEFAULT + "")
  @SimpleProperty(description = "Specifies a number that encodes the position of the tab bar." +
      "The choices are: 1 = default, 2 = top, 3 = bottom.")
  public void TabBarPosition(int tabBarPosition) {
    this.tabBarPosition = tabBarPosition;
    switch (tabBarPosition) {
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
        alignTabsAtTop();
        break;
    }
  }

  /**
   * Returns true if the tab bar is visible.
   *
   * @return tabBarVisible boolean
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns true if the tab bar is visible")
  public boolean TabBarVisible() {
    return tabBarVisible;
  }

  /**
   * Specifies whether the tab bar is visible.
   *
   * @param tabBarVisible
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(description = "Specifies whether the tab bar is visible")
  public void TabBarVisible(boolean tabBarVisible) {
    this.tabBarVisible = tabBarVisible;
    switch (tabBarPosition) {
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
        alignTabsAtTop();
        break;
    }
  }

  public void alignTabsAtTop() {
    layout.removeAllViews();
    if (tabBarVisible) {
      if (tabLayout.getParent() != null) {
        ((ViewGroup) tabLayout.getParent()).removeView(tabLayout);
      }
      layout.addView(tabLayout, new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
    }
    if (viewPager.getParent() != null) {
      ((ViewGroup) viewPager.getParent()).removeView(viewPager);
    }
    layout.addView(viewPager, new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, 0, 1));
    tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_BOTTOM);
  }

  public void alignTabsAtBottom() {
    layout.removeAllViews();
    if (viewPager.getParent() != null) {
      ((ViewGroup) viewPager.getParent()).removeView(viewPager);
    }
    layout.addView(viewPager, new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, 0, 1));
    if (tabBarVisible) {
      if (tabLayout.getParent() != null) {
        ((ViewGroup) tabLayout.getParent()).removeView(tabLayout);
      }
      layout.addView(tabLayout, new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
    }
    tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_TOP);
  }
}
