package android.app;

import android.Res;
import android.content.DialogInterface;

import android.view.KeyEvent;
import com.google.gwt.user.client.ui.PopupPanel;

public class Dialog implements DialogInterface {

  PopupPanel popupPanel;
  boolean mCancelable = true;

  public Dialog(boolean cancelable) {
    popupPanel = new PopupPanel(cancelable);
    popupPanel.setStyleName(Res.R.style().dialog());
    mCancelable = cancelable;
  }

  public boolean isShowing() {
    return popupPanel.isShowing();
  }

  @Override
  public void cancel() {
    popupPanel.hide();
  }

  @Override
  public void dismiss() {
    popupPanel.hide();
  }

  public void show() {
    popupPanel.center();
    popupPanel.show();
  }

  public void hide() {
    popupPanel.hide();
  }

  /**
   * Sets whether this dialog is cancelable with the
   * {@link KeyEvent#KEYCODE_BACK BACK} key.
   */
  public void setCancelable(boolean flag) {
    mCancelable = flag;
    popupPanel.setAutoHideEnabled(flag);
  }
}
