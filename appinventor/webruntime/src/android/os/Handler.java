package android.os;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;

public class Handler {

  public void handleMessage(Message msg) {

  }

  public void sendEmptyMessage(int what) {
    sendMessage(Message.obtain(this, what));
  }

  public void sendMessage(final Message msg) {
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

      @Override
      public void execute() {
        handleMessage(msg);
      }
    });
  }

  public void sendMessageAtTime(final Message msg, long uptimeMillis) {
    Timer timer = new Timer() {
      public void run() {
        handleMessage(msg);
      }
    };

    // Execute the timer to expire 2 seconds in the future
    timer.schedule((int) (uptimeMillis - SystemClock.uptimeMillis()));
  }

  public Message obtainMessage() {
    return new Message();
  }

  public Message obtainMessage(int what, Object obj) {
    return Message.obtain(this, what, obj);
  }

  public Message obtainMessage(int what, int arg1, int arg2) {
    return Message.obtain(this, what, arg1, arg2);
  }

  public Message obtainMessage(int what, int arg1, int arg2, Object obj) {
    return Message.obtain(this, what, arg1, arg2, obj);
  }

  public void removeMessages(int what) {
    // TODO
  }

  public native void post(Runnable runnable) /*-{
    runnable.$internal$timeout$id = $wnd.setTimeout(function() {
      runnable.@java.lang.Runnable::run(*).call(runnable);
    }, 0);
  }-*/;

  public native void postDelayed(Runnable runnable, int delayMillis) /*-{
    runnable.$internal$timeout$id = $wnd.setTimeout(function() {
      runnable.@java.lang.Runnable::run(*).call(runnable);
    }, delayMillis);
  }-*/;

  public native void removeCallbacks(Runnable runnable) /*-{
    if (runnable.$internal$timeout$id) {
      $wnd.clearTimeout(runnable.$internal$timeout$id);
      runnable.$internal$timeout$id = undefined;
    }
  }-*/;
}
