package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@DesignerComponent(version = YaVersion.TABARRANGEMENT_COMPONENT_VERSION,
    category = ComponentCategory.LAYOUT)
@SimpleObject
public class TabArrangement extends AndroidViewComponent<LinearLayout> implements ComponentContainer {
  private TabLayout tabLayout;
  private ViewPager2 viewPager;
  private RecyclerView.Adapter adapter;
  private List<Tab> tabs;
  
  public TabArrangement(ComponentContainer container) {
    super(container);
    Log.d("tabarrangement","Constructor of TabArrangement");
    viewPager = new ViewPager2(container.$context());
    viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
    tabLayout = new TabLayout(container.$context());
    tabLayout.setBackgroundColor(container.$form().PrimaryColor());
    tabLayout.setSelectedTabIndicatorColor(container.$form().AccentColor());
    tabLayout.setTabMode(TabLayout.MODE_FIXED);
    tabLayout.setTabTextColors(Color.GRAY, Color.WHITE);
    tabs = new ArrayList<>();
    adapter = new RecyclerView.Adapter() {
      @Override
      public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Log.d("tabarrangement","onCreateViewHolder at index: "+i);
        FrameLayout layout = new FrameLayout(viewGroup.getContext());
        layout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        return new RecyclerView.ViewHolder(layout) {
          @Override
          public String toString() {
            return super.toString();
          }
        };
      }
      
      @Override
      public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        Log.d("tabarrangement","onBindViewHolder at position: " + i);
        Log.d("tabarrangement", "Tab details: Index: " + i + " Tab: " + tabs.get(i) + " Expected: " + tabs.get(i).getTab() + " Found: " + tabLayout.getTabAt(i));
        ViewGroup childViewGroup = tabs.get(i).viewLayout.getLayoutManager();
        if(childViewGroup.getParent()!=null) {
          ((ViewGroup) childViewGroup.getParent()).removeView(childViewGroup);
        }
        ((ViewGroup)(viewHolder.itemView)).addView(childViewGroup);
        Log.d("tabarrangement","Number of children: " + childViewGroup.getChildCount());
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
        Log.d("tabarrangement","on Configure Tab at index "+i);
        tab.setText("Object " + i);
        Log.d("tabarrangement","Configured tab text at position: " + i + " " + tab + " to Object " + i);
      }
    }).attach();
    container.$add(this);
  }
  
  @Override
  public LinearLayout getView() {
    Log.d("tabarrangement","Return Linear layout with "+tabLayout.getTabCount()+" tabs");
    LinearLayout layout = new LinearLayout($context());
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setGravity(Gravity.TOP);
    layout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.FILL_PARENT));
    if(tabLayout.getParent() != null) {
      ((ViewGroup)tabLayout.getParent()).removeView(tabLayout);
    }
    layout.addView(tabLayout, RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    if(viewPager.getParent() != null) {
      ((ViewGroup)viewPager.getParent()).removeView(viewPager);
    }
    layout.addView(viewPager, RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.FILL_PARENT);
    return layout;
  }
  
  public TabLayout getTabLayout() {
    return tabLayout;
  }
  
  void addTab(Tab tab){
    tabLayout.addTab(tabLayout.newTab());
    tabs.add(tab);
    Log.d("tabarrangement","Current list of tabs: " + Arrays.toString(tabs.toArray()));
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
}