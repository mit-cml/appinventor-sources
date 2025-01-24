package androidx.appcompat.app;

import androidx.appcompat.view.ActionMode;

public interface AppCompatCallback {
  void onSupportActionModeStarted(ActionMode var1);

  void onSupportActionModeFinished(ActionMode var1);

  ActionMode onWindowStartingSupportActionMode(ActionMode.Callback var1);
}
