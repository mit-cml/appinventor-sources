// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.ftc;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;


public class Utility {
  public static final String CONFIG_FILES_DIR = Environment.getExternalStorageDirectory() + "/FIRST/";
  public static final String FILE_EXT = ".xml";

  public static void complainToast(String msg, Context context){
    Toast complainToast =  Toast.makeText(context, msg, Toast.LENGTH_SHORT);
    complainToast.setGravity(Gravity.CENTER, 0, 0);
    // makes it a nice red color, but squares the corners for some reason :(
    //complainToast.getView().setBackgroundColor(Color.RED);
    TextView message = (TextView) complainToast.getView().findViewById(android.R.id.message);
    message.setTextColor(Color.WHITE);
    message.setTextSize(18);
    complainToast.show();
  }
}
