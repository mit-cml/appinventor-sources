/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

// Modified for App Inventor by Liz Looney
// Copyright 2015 MIT, All rights reserved
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

  /**
   * Displays a red message with white text  in the center of the screen.
   * @param msg - the message to be displayed context - the context to display it.
   */
  public void complainToast(String msg, Context context){
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
