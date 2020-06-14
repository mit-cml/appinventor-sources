package com.google.appinventor.components.runtime;

import android.view.View;

/**
 * Listener for distributing the Activity onCreateContextMenu() method to interested
 * components.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */
public interface OnCreateContextMenuListener {
    public void onCreateContextMenu(android.view.ContextMenu menu, View view, android.view.ContextMenu.ContextMenuInfo menuInfo);
}
