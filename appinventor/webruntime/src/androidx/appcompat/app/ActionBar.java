package androidx.appcompat.app;

import android.Res;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gwt.dom.client.Style;

public class ActionBar {
  static final String TAG = "ActionBar";

  Activity activity;
  int homeImageRes = android.R.drawable.actionbar_indicator_back;
  boolean displayHomeAsUpEnabled = false;

  LinearLayout view;
  public ImageView actionBarHome;
  LinearLayout menuItems;

  TextView titleView;

  MenuItem homeItem;

  public ActionBar(Activity activity) {
    this.activity = activity;

    view = new LinearLayout(activity);
    view.getElement().setId("ActionBar");
    view.getElement().addClassName(Res.R.style().actionbar());

    actionBarHome = new ImageView(activity);
    view.addView(actionBarHome);

    menuItems = new LinearLayout(activity);
    menuItems.getElement().setId("MenuItems");
    menuItems.getElement().getStyle().setFloat(Style.Float.RIGHT);
    view.addView(menuItems);

    homeItem = new MenuItem();
    homeItem.setIcon(Context.icon);
    homeItem.setItemId(android.R.id.home);

    actionBarHome.getElement().addClassName(Res.R.style().actionbarHome());
    actionBarHome.getElement().addClassName(Res.R.style().controlHighlight());
    actionBarHome.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onHomeAction();
      }
    });
    actionBarHome.setVisibility(View.GONE);

    titleView = new TextView(activity);
    titleView.getElement().addClassName(Res.R.style().actionbarTitle());
    titleView.getElement().setId("title");
    view.addView(titleView);
  }

  private void onHomeAction() {
    activity.onMenuItemSelected(0, homeItem);
  }

  public void setTitle(int title) {
    setTitle(Context.resources.getString(title));
  }

  public void setDisplayHomeAsUpEnabled(boolean displayHomeAsUpEnabled) {
    if (this.displayHomeAsUpEnabled == displayHomeAsUpEnabled) {
      return;
    }
    this.displayHomeAsUpEnabled = displayHomeAsUpEnabled;

    if (displayHomeAsUpEnabled) {
      actionBarHome.setImageResource(homeImageRes);
      actionBarHome.setVisibility(View.VISIBLE);
    } else {
      if (actionBarHome != null) {
        actionBarHome.setVisibility(View.GONE);
      }
    }
  }

  public void setHomeButtonEnabled(boolean homeButtonEnabled) {

  }

  public void setHomeAsUpIndicator(int resId) {
    this.homeImageRes = resId;
    actionBarHome.setImageResource(homeImageRes);
  }

  public void setBackgroundDrawable(Drawable var1) {

  }

  public void show() {}

  public void hide() {}

  public void setTitle(CharSequence var1) {
    titleView.setText(var1);
  }

  public void setShowHideAnimationEnabled(boolean enabled) {
  }
}
