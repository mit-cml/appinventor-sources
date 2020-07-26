package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
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
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.PaintUtil;

import java.util.ArrayList;
import java.util.List;

@DesignerComponent(version = YaVersion.TABARRANGEMENT_COMPONENT_VERSION,
    category = ComponentCategory.LAYOUT)
@SimpleObject
public class TabArrangement extends AndroidViewComponent<LinearLayout> implements ComponentContainer {
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
  
  public TabArrangement(ComponentContainer container) {
    super(container);
    Log.d("tabarrangement","Constructor of TabArrangement");
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
    adapter = new RecyclerView.Adapter() {
      @Override
      public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.d("tabarrangement","onCreateViewHolder at index: "+viewType);
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
        Log.d("tabarrangement","onCreateViewHolder at adapter position: "+viewHolder.getAdapterPosition());
        return viewHolder;
      }
      
      @Override
      public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
//        Log.d("tabarrangement","onBindViewHolder at position: " + i);
//        Log.d("tabarrangement", "Tab details: Index: " + i + " Tab: " + tabs.get(i) + " Expected: " + tabs.get(i).getTab() + " Found: " + tabLayout.getTabAt(i));
        ViewGroup childViewGroup = tabs.get(i).viewLayout.getLayoutManager();
        if (childViewGroup.getParent()!=null) {
          ((ViewGroup) childViewGroup.getParent()).removeView(childViewGroup);
        }
        ((ViewGroup)(viewHolder.itemView)).addView(childViewGroup);
//        Log.d("tabarrangement","Number of children: " + childViewGroup.getChildCount());
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
    Log.d("TabArrangement", "Setting default view");
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setGravity(Gravity.TOP);
    if (tabLayout.getParent() != null) {
      ((ViewGroup)tabLayout.getParent()).removeView(tabLayout);
    }
    layout.addView(tabLayout, new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
    if (viewPager.getParent() != null) {
      ((ViewGroup)viewPager.getParent()).removeView(viewPager);
    }
    layout.addView(viewPager, new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, 0, 1));
    tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_BOTTOM);
    return layout;
  }
  
  void addTab(Tab tab) {
    tabLayout.addTab(tabLayout.newTab());
    tabs.add(tab);
//    Log.d("tabarrangement","Current list of tabs: " + Arrays.toString(tabs.toArray()));
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
  
  @SimpleEvent
  public void ShowTab(Tab tab) {
    EventDispatcher.dispatchEvent(this, "ShowTab", tab);
  }
  
  @Override
  @SimpleProperty
  public void Width(int width) {
    if (width == LENGTH_PREFERRED) {
      width = LENGTH_FILL_PARENT;
    }
    super.Width(width);
  }
  
  @Override
  @SimpleProperty
  public void Height(int height) {
    if (height == LENGTH_PREFERRED) {
      height = LENGTH_FILL_PARENT;
    }
    super.Height(height);
  }
  
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  @IsColor
  public int TabBackgroundColor() {
    return tabBackgroundColor;
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
  defaultValue = ComponentConstants.DEFAULT_PRIMARY_COLOR)
  @SimpleProperty
  public void TabBackgroundColor(int argb) {
    tabBackgroundColor = argb;
    tabLayout.setBackgroundColor(tabBackgroundColor);
  }
  
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  @IsColor
  public int SelectedTabIndicatorColor() {
    return selectedTabIndicatorColor;
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = ComponentConstants.DEFAULT_ACCENT_COLOR)
  @SimpleProperty
  public void SelectedTabIndicatorColor(int argb) {
    selectedTabIndicatorColor = argb;
    tabLayout.setSelectedTabIndicatorColor(selectedTabIndicatorColor);
  }
  
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  @IsColor
  public int TabTextColor() {
    return textColor;
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
  @SimpleProperty
  public void TabTextColor(int argb) {
    textColor = argb;
    tabLayout.setTabTextColors(textColor,selectedTabTextColor);
  }
  
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  @IsColor
  public int SelectedTabTextColor() {
    return selectedTabTextColor;
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void SelectedTabTextColor(int argb) {
    selectedTabTextColor = argb;
    tabLayout.setTabTextColors(textColor,selectedTabTextColor);
  }
  
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public int TabBarPosition() {
    return tabBarPosition;
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TAB_BAR_POSITION,
      defaultValue = ComponentConstants.TAB_POSITION_DEFAULT + "")
  @SimpleProperty
  public void TabBarPosition(int tabBarPosition) {
    Log.d("TabArrangement", "Setting tab bar position property: " + tabBarPosition + "\t" + tabBarVisible);
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
  
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public boolean TabBarVisible() {
    return tabBarVisible;
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void TabBarVisible(boolean tabBarVisible) {
    Log.d("TabArrangement", "Setting tab bar visibility property: " + tabBarPosition + "\t" + tabBarVisible);
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
    Log.d("TabArrangement", "Aligning tabs at top with visibility: " + tabBarVisible);
    layout.removeAllViews();
    if (tabBarVisible) {
      if (tabLayout.getParent() != null) {
        ((ViewGroup) tabLayout.getParent()).removeView(tabLayout);
      }
      layout.addView(tabLayout, new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
    }
    if (viewPager.getParent() != null) {
      ((ViewGroup)viewPager.getParent()).removeView(viewPager);
    }
    layout.addView(viewPager, new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, 0, 1));
    tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_BOTTOM);
  }
  
  public void alignTabsAtBottom() {
    Log.d("TabArrangement", "Aligning tabs at bottom with visibility: " + tabBarVisible);
    layout.removeAllViews();
    if (viewPager.getParent() != null) {
      ((ViewGroup)viewPager.getParent()).removeView(viewPager);
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