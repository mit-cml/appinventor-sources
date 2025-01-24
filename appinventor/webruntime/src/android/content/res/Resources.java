package android.content.res;

import android.util.DisplayMetrics;
import android.view.Menu;

import com.google.gwt.user.client.ui.Widget;

public class Resources {
  static final String TAG = "Resources";

  public final class Theme {
  }

  public String getIdAsString(int id) {
    switch (id) {
      case android.R.id.list:
        return "list";
      case android.R.id.title:
        return "title";
    }
    return null;
  }

  public String getString(int id) {
    return null;
  }

  public String[] getStringArray(int id) {
    return null;
  }

  public CharSequence getText(int id) {
    return getString(id);
  }

  public Menu getMenu(int id) {
    return null;
  }

  public int getColor(int id) {
    return 0;
  }

  public String getDrawable(int id) {
    switch (id) {
      case android.R.drawable.actionbar_menu:
        return "ic_menu.svg";
      case android.R.drawable.actionbar_indicator_back:
        return "ic_indicator_back.svg";
      case android.R.drawable.ic_drawer:
        return "ic_drawer.svg";
    }
    return null;
  }

  public Widget getLayout(int id) {
    return null;
  }

  public DisplayMetrics getDisplayMetrics() {
    return computeDisplayMetrics(new DisplayMetrics());
  }

  private native DisplayMetrics computeDisplayMetrics(DisplayMetrics dm) /*-{
    dm.density = $wnd.devicePixelRatio;
    dm.widthPixels = $wnd.innerWidth;
    dm.heightPixels = $wnd.innerHeight;
    return dm;
  }-*/;
}
