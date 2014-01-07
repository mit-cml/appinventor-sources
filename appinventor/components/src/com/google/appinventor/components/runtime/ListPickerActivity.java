// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;



import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.google.appinventor.components.runtime.util.AnimationUtil;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;

/**
 * ListPickerActivity class - Brings up a list of items specified in an intent
 * and returns the selected item as the result.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author M. Hossein Amerkashi (kkashi01@gmail.com)
 */
public class ListPickerActivity extends Activity implements AdapterView.OnItemClickListener {

  private String closeAnim = "";
  private ListView listView;

  // Listview Adapter
  ArrayAdapter<String> adapter;

  // Search EditText
  EditText txtSearchBox;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout viewLayout = new LinearLayout(this);
    viewLayout.setOrientation(LinearLayout.VERTICAL);

    Intent myIntent = getIntent();
    if (myIntent.hasExtra(ListPicker.LIST_ACTIVITY_ANIM_TYPE)) {
      closeAnim = myIntent.getStringExtra(ListPicker.LIST_ACTIVITY_ANIM_TYPE);
    }
    if (myIntent.hasExtra(ListPicker.LIST_ACTIVITY_TITLE)) {
      String title = myIntent.getStringExtra(ListPicker.LIST_ACTIVITY_TITLE);
      setTitle(title);
    }
    if (myIntent.hasExtra(ListPicker.LIST_ACTIVITY_ARG_NAME)) {
      String items[] = getIntent().getStringArrayExtra(ListPicker.LIST_ACTIVITY_ARG_NAME);
      listView = new ListView(this);
      listView.setOnItemClickListener(this);

      // Adding items to listview
      adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
      listView.setAdapter(adapter);
      String showFilterBar =myIntent.getStringExtra(ListPicker.LIST_ACTIVITY_SHOW_SEARCH_BAR);

      // Determine if we should even show the search bar
      txtSearchBox = new EditText(this);
      txtSearchBox.setSingleLine(true);
      txtSearchBox.setWidth(Component.LENGTH_FILL_PARENT);
      txtSearchBox.setPadding(10, 10, 10, 10);
      txtSearchBox.setHint("Search list...");

      if (showFilterBar == null || !showFilterBar.equalsIgnoreCase("true")) {
        txtSearchBox.setVisibility(View.GONE);
      }

      //set up the listener
      txtSearchBox.addTextChangedListener(new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
          // When user changed the Text
          ListPickerActivity.this.adapter.getFilter().filter(cs);
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
          // no-op. Required method
        }

        @Override
        public void afterTextChanged(Editable arg0) {
            // no-op. Required method
        }
      });

    }
    else {
      setResult(RESULT_CANCELED);
      finish();
      AnimationUtil.ApplyCloseScreenAnimation(this, closeAnim);
    }
    viewLayout.addView(txtSearchBox);
    viewLayout.addView(listView);

    this.setContentView(viewLayout);
    viewLayout.requestLayout();

    //hide the keyboard
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    String selected = (String) parent.getAdapter().getItem(position);
    Intent resultIntent = new Intent();
    resultIntent.putExtra(ListPicker.LIST_ACTIVITY_RESULT_NAME, selected);
    resultIntent.putExtra(ListPicker.LIST_ACTIVITY_RESULT_INDEX, position + 1);
    closeAnim = selected;
    setResult(RESULT_OK, resultIntent);
    finish();
    AnimationUtil.ApplyCloseScreenAnimation(this, closeAnim);
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


}
