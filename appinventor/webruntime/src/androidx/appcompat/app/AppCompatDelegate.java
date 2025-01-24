package androidx.appcompat.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class AppCompatDelegate {

  private Activity owner;
  private ActionBar actionBar;
  private AppCompatCallback callback;
  private ViewGroup view;

  private AppCompatDelegate(Activity activity, AppCompatCallback callback) {
    this.owner = activity;
    actionBar = new ActionBar(activity);
    this.callback = callback;
  }

  public static AppCompatDelegate create(Activity activity, AppCompatCallback callback) {
    return new AppCompatDelegate(activity, callback);
  }

  public void onCreate(Bundle var1) {}

  public void onPostCreate(Bundle var1) {}

  public void onPostResume() {}

  public void onConfigurationChanged(Configuration var1) {}

  public void onStop() {}

  public void onDestroy() {}

  public void setTitle(CharSequence var1) {
    actionBar.setTitle(var1);
  }

  public void setContentView(View var1) {
    final String id = HTMLPanel.createUniqueId();
    view = new LinearLayout(owner);
    view.addView(actionBar.view);
    view.addView(var1);
    var1.element.setClassName("ContentView");

    Element el = view.getElement();
    DOM.getElementById(Activity.ACTIVITY_ID).appendChild(el);
    el.setId(id);
  }

  public ActionBar getSupportActionBar() {
    return actionBar;
  }

  public View findViewById(int id) {
    return view.findViewById(id);
  }
}
