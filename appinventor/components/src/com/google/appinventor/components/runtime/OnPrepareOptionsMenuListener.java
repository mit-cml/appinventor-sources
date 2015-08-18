package com.google.appinventor.components.runtime;

import android.view.Menu;

/**
 * Listener for distributing the Activity onPrepareOptionsMenu() method to interested components.
 *
 * @author liyucun2012@gmail.com (Yucun Li)
 */
public interface OnPrepareOptionsMenuListener {
    public void onPrepareOptionsMenu(Menu menu);
}