package android.app;

import android.AndroidManifest;
import android.Res;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewFactory;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import java.io.File;

public class Activity extends Context {

  public static final String TAG = "Activity";

  public static final int RESULT_CANCELED = 0;
  public static final int RESULT_FIRST_USER = 1;
  public static final int RESULT_OK = -1;

  public static final String ACTIVITY_ID = "activity";

  int status = 0;
  int targetStatus = 0;

  String title;
  public ViewGroup view;

  Intent intent;
  Integer requestCode;
  int resultCode = RESULT_OK;
  Intent resultData;

  // Data when we return from another activity
  Integer returnRequestCode;
  int returnResultCode;
  Intent returnResultData;

  Menu menu;
  PopupMenu popupMenu;
  private static final Window WINDOW = new Window();

  protected void onCreate(Bundle savedInstanceState) {

  }

  protected void onPostCreate(Bundle savedInstanceState) {
  }

  protected void onStart() {
  }

  protected void onResume() {
    if (view != null) {
      view.setVisibility(View.VISIBLE);
    }
  }

  protected void onPostResume() {
  }

  protected void onPause() {
    if (view != null) {
      view.setVisibility(View.GONE);
    }
  }

  protected void onStop() {
  }

  protected void onDestroy() {
    if (popupMenu != null) {
      popupMenu.destroy();
    }
    if (view != null) {
      view.getElement().removeFromParent();
    }
  }

  protected Dialog onCreateDialog(int id) {
    return null;
  }

  protected void onPrepareDialog(int id, Dialog dialog) {
    // dialog.setOwnerActivity(this);
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    return false;
  }

  boolean onPrepareOptionsMenu(Menu menu) {
    return false;
  }

  public void invalidateOptionsMenu() {
    if (popupMenu != null) {
      popupMenu.destroy();
    }
    menu = new Menu();
    onCreateOptionsMenu(menu);
    onPrepareOptionsMenu(menu);
    createMenu();
  }

  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    return onOptionsItemSelected(item);
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    return false;
  }

  private void createMenu() {
    LinearLayout actionBarRight = new LinearLayout(findViewById("MenuItems"));
    if (actionBarRight.getElement() == null) {
      Log.e(TAG, "MenuItems div not found");
      return;
    }
    actionBarRight.removeAllViews();

    if (menu.menuItems.size() > 0) {
      popupMenu = null;
      ImageButton menuButton = null;

      for (final MenuItem item : menu.menuItems) {
        if (item.getTitle() != 0 || item.getIcon() != 0) {
          if (item.getShowAsAction() == MenuItem.SHOW_AS_ACTION_ALWAYS) {
            ImageButton b = new ImageButton(this);
            if (item.getIcon() != 0) {
              b.setImageResource(item.getIcon());
              b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  onMenuItemSelected(0, item);
                }
              });
              b.getElement().addClassName(Res.R.style().actionbarButton());
              b.getElement().addClassName(Res.R.style().controlHighlight());
              actionBarRight.addView(b);
            }
          } else {
            if (popupMenu == null) {
              // Create popupMenu and menuButton
              menuButton = new ImageButton(this);
              menuButton.getElement().addClassName(Res.R.style().actionbarButton());
              menuButton.getElement().addClassName(Res.R.style().controlHighlight());
              menuButton.setImageResource(android.R.drawable.actionbar_menu);
              menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  popupMenu.toggle();
                }
              });

              popupMenu = new PopupMenu(this, menuButton);
              popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                  return onMenuItemSelected(0, item);
                }
              });
            }

            if (item.getTitleString() != null) {
              popupMenu.getMenu().add(item.getGroupId(), item.getItemId(), item.getOrder(), item.getTitleString());
            } else if (item.getTitle() != 0) {
              popupMenu.getMenu().add(item.getGroupId(), item.getItemId(), item.getOrder(), item.getTitle());
            }
          }
        }
      }
      if (menuButton != null) {
        actionBarRight.addView(menuButton);
      }
    }
  }

  public void openOptionsMenu() {
    if (popupMenu != null) {
      popupMenu.show();
    }
  }

  public void closeOptionsMenu() {
    if (popupMenu != null) {
      popupMenu.dismiss();
    }
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
    onTitleChanged(title, 0xFFFFFFFF);
  }

  public Context getApplicationContext() {
    return AndroidManifest.applicatonContext;
  }

  public void finish() {
    ActivityManager.finish(this);
  }

  public void setContentView(int layoutId) {
    setContentView(getResources().getLayout(layoutId));
  }

  public void setContentView(Widget htmlPanel) {
    String id = HTMLPanel.createUniqueId();
    view = new LinearLayout(this);
    DOM.getElementById(ACTIVITY_ID).appendChild(view.getElement());
    view.getElement().setId(id);
    RootPanel.get(id).add(htmlPanel);
  }

  protected void onSaveInstanceState(Bundle outState) {
  }

  public void startActivityForResult(Intent intent, int requestCode) {
    ActivityManager.startActivity(intent, requestCode);
  }

  public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
    ActivityManager.startActivity(intent, requestCode);
  }

  public void setResult(int resultCode, Intent resultData) {
    this.resultCode = resultCode;
    this.resultData = resultData;
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  }

  public void onBackPressed() {
    finish();
  }

  public View findViewById(int id) {
    return getWindow().findViewById(id);
  }

  public View findViewById(String id) {
    return getWindow().findViewById(id);
  }

  public Intent getIntent() {
    return intent;
  }

  public MenuInflater getMenuInflater() {
    return new MenuInflater();
  }

  public Window getWindow() {
    return WINDOW;
  }

  public void runOnUiThread(Runnable r) {
    // We are always on the UI thread in JavaScript
    r.run();
  }

  public native void postDelayed(Runnable r, int delayMillis) /*-{
    $wnd.setTimeout(function() {
      r.@java.lang.Runnable::run(*)();
    }, delayMillis);
  }-*/;

  protected void onNewIntent(Intent intent) {
  }

  public AssetManager getAssets() {
    return new AssetManager();
  }

  public void onRequestPermissionsResult() {

  }

  public File getCacheDir() {
    return null;
  }

  public File getFilesDir() {
    return null;
  }

  public int getRequestedOrientation() {
    return 0;
  }

  public void setRequestedOrientation(int orientation) {

  }

  public Object getSystemService(String name) {
    // TODO(ewpatton): Real implementation
    if (VIBRATOR_SERVICE.equals(name)) {
      return new Vibrator();
    }
    return null;
  }

  public View getCurrentFocus() {
    // TODO(ewpatton): Real implementation
    return null;
  }

  public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    /* callback - no nothing */
  }

  public void setTheme(int resid) {
    // TODO(ewpatton): Real implementation
  }

  public void onConfigurationChanged(Configuration newConfig) {
    // TODO(ewpatton): Real implementation
  }

  protected void onTitleChanged(CharSequence title, int color) {
    // TODO(ewpatton): Real implementation
  }

  public void setContentView(View view) {
    // TODO(ewpatton): Real implementation
    this.view = (ViewGroup) view;
    String id = HTMLPanel.createUniqueId();

  }

  public final void setResult(int resultCode) {
    this.resultCode = resultCode;
    this.resultData = null;
  }

  public final void setVolumeControlStream(int streamType) {
    // TODO(ewpatton): Real implementation
  }

}
