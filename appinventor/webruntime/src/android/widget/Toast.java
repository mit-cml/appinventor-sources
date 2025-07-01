package android.widget;

import android.Res;
import android.content.Context;
import android.view.Gravity;

import android.view.View;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class Toast {

  public final static int LENGTH_LONG = 1;
  public final static int LENGTH_SHORT = 0;

  String message;
  int gravity = Gravity.BOTTOM;
  int duration;
  View view;

  public static Toast makeText(Context context, int message, int duration) {
    return makeText(context, Context.resources.getString(message), duration);
  }

  public static Toast makeText(Context context, String message, int duration) {
    Toast toast = new Toast();
    toast.setMessage(message);
    toast.setDuration(duration);
    return toast;
  }

  public void show() {
    final PopupPanel panel = new PopupPanel();
    panel.setStyleName(Res.R.style().toast());

    HTML label;
    if (view != null) {
      label = new HTML();
      label.getElement().appendChild(view.getElement());
      panel.setWidget(label);
    } else {
      label = new HTML(message.replace("\n", "<br/>"));
    }
    panel.setWidget(label);
    panel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = (Window.getClientWidth() - offsetWidth) / 2;
        int top = 0;
        switch (gravity) {
          case Gravity.TOP:
            top = (Window.getClientHeight() - offsetHeight) / 10;
            break;
          case Gravity.CENTER:
            top = (Window.getClientHeight() - offsetHeight) / 2;
            break;
          case Gravity.BOTTOM:
            top = 9 * (Window.getClientHeight() - offsetHeight) / 10;
            break;
        }
        panel.setPopupPosition(left, top);
      }
    });

    // Create a new timer that calls hide().
    Timer t = new Timer() {
      public void run() {
        panel.hide();
      }
    };

    if (duration == LENGTH_SHORT) {
      t.schedule(2500);
    } else {
      t.schedule(4000);
    }
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public void setGravity(int gravity) {
    this.gravity = gravity;
  }

  public int getGravity() {
    return gravity;
  }

  public void setGravity(int gravity, int xOffset, int yOffset) {
    this.gravity = gravity;
  }

  public int getXOffset() {
    return 0; // Not implemented
  }

  public int getYOffset() {
    return 0; // Not implemented
  }

  public void setView(View view) {
    this.view = view;
  }
}
