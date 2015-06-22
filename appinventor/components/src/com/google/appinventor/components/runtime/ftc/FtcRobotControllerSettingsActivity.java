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
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

//package com.qualcomm.ftcrobotcontroller;
package com.google.appinventor.components.runtime.ftc;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import com.qualcomm.ftccommon.Device;
import com.qualcomm.robotcore.util.RobotLog;

import java.io.Serializable;

public class FtcRobotControllerSettingsActivity extends Activity {

  // Added for App Inventor:
  private static final int CONFIGURE_ROBOT = 3;

  public static class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Load the preferences from an XML resource
      addPreferencesFromResource(R.xml.preferences);


      Preference pref = (Preference) findPreference(getString(R.string.pref_launch_configure));
      pref.setOnPreferenceClickListener(onPreferenceClickListener);

      Preference prefAutoconfigure = (Preference) findPreference(getString(R.string.pref_launch_autoconfigure));
      prefAutoconfigure.setOnPreferenceClickListener(onPreferenceClickListener);

      if (Build.MANUFACTURER.equalsIgnoreCase(Device.MANUFACTURER_ZTE) && Build.MODEL.equalsIgnoreCase(Device.MODEL_ZTE_SPEED)) {
        Preference launchSettings =  findPreference(getString(R.string.pref_launch_settings));
        launchSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

          public boolean onPreferenceClick(Preference preference) {
            Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.zte.wifichanneleditor");
            try {
              startActivity(intent);
            } catch (NullPointerException e) {
              Toast.makeText(getActivity(), "Unable to launch ZTE WifiChannelEditor", Toast.LENGTH_SHORT).show();
            }
            return true;
          }
        });
      }

      if (Build.MODEL.equals(Device.MODEL_FOXDA_FL7007)) {
        Preference launchSettings =  findPreference(getString(R.string.pref_launch_settings));
        launchSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

          public boolean onPreferenceClick(Preference preference) {
            Intent viewIntent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(viewIntent);

            return true;
          }
        });
      }
    }

    Preference.OnPreferenceClickListener onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Intent intent =  new Intent(preference.getIntent().getAction());
        android.util.Log.e("HeyLiz", "starting activity with intent " + intent);
        startActivityForResult(intent, CONFIGURE_ROBOT);
        return true;
      }
    };


    @Override
    public void onActivityResult(int request, int result, Intent intent) {
      if (request == CONFIGURE_ROBOT) {
        if (result == RESULT_OK) {
          getActivity().setResult(RESULT_OK, intent);
        }
      }
    }


  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Added for App Inventor:
    R = new ResourceIds(this);

    // Display the fragment as the main content.
    getFragmentManager().beginTransaction()
    .replace(android.R.id.content, new SettingsFragment())
    .commit();
  }

  // Added for App Inventor:
  private static ResourceIds R;
}
