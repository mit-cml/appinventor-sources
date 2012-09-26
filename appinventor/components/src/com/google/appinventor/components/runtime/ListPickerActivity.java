// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;



import com.google.appinventor.components.runtime.util.AnimationUtil;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * ListPickerActivity class - Brings up a list of items specified in an intent
 * and returns the selected item as the result.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class ListPickerActivity extends ListActivity {

  private String closeAnim = "";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String items[] = null;
    Intent myIntent = getIntent();
    if (myIntent.hasExtra(ListPicker.LIST_ACTIVITY_ANIM_TYPE)) {
      closeAnim = myIntent.getStringExtra(ListPicker.LIST_ACTIVITY_ANIM_TYPE);
    }
    if (myIntent.hasExtra(ListPicker.LIST_ACTIVITY_ARG_NAME)) {
      items = getIntent().getStringArrayExtra(ListPicker.LIST_ACTIVITY_ARG_NAME);
      setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
      getListView().setTextFilterEnabled(true);
    } else {
      setResult(RESULT_CANCELED);
      finish();
      AnimationUtil.ApplyCloseScreenAnimation(this, closeAnim);
    }
  }

  // Capture the hardware back button to make sure the screen animation
  // still applies. (In API level 5, we can override onBackPressed instead)
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      boolean handled = super.onKeyDown(keyCode, event);
      AnimationUtil.ApplyCloseScreenAnimation(this, closeAnim);
      return handled;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public void onListItemClick(ListView lv, View v, int position, long id) {
    Intent resultIntent = new Intent();
    String selected = (String) getListView().getItemAtPosition(position);
    resultIntent.putExtra(ListPicker.LIST_ACTIVITY_RESULT_NAME,
                          selected);
    resultIntent.putExtra(ListPicker.LIST_ACTIVITY_RESULT_INDEX, position + 1);
    closeAnim = selected;
    setResult(RESULT_OK, resultIntent);
    finish();
    AnimationUtil.ApplyCloseScreenAnimation(this, closeAnim);
  }

}
