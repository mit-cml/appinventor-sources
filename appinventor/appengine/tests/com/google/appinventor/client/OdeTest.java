package com.google.appinventor.client;

import com.google.appinventor.shared.rpc.user.User;
//import com.google.appinventor.shared.settings.Settings;
import com.google.appinventor.shared.settings.SettingsConstants;

import org.apache.xpath.operations.Bool;

//import java.util.HashMap;
//mport java.util.Map;

//import com.gargoylesoftware.htmlunit.javascript.host.Map;
//import com.google.appinventor.client.settings.CommonSettings;
//import com.google.appinventor.client.settings.user.GeneralSettings;
import com.google.appinventor.client.settings.user.UserSettings;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

//import gnu.commonlisp.lang.CommonLisp;

public class OdeTest extends GWTTestCase {
  public void testCompareLocales() {
    assertTrue("Handles default case one is null",
        Ode.compareLocales(null, "en", "en"));
    assertTrue("Handles both cases being null",
        Ode.compareLocales(null, null, "en"));
    assertTrue("Handles when both cases are the same",
        Ode.compareLocales("en", "en", "en"));
    assertFalse("Handles when the cases are different",
        Ode.compareLocales("en", "fr_FR", "en"));
    assertFalse("Handles when the default is different",
        Ode.compareLocales(null, "fr_FR", "en"));
  }

  @Override
  public String getModuleName() {
    return "com.google.appinventor.YaClient";
  }

  public void testGetUserDyslexicFont() {
    User user = new User("1","abc@email.com",true,true,null);
    UserSettings userSettings = new UserSettings(user);
    //Settings GeneralSettings = new Settings(null, getName());
    
    userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
            changePropertyValue(SettingsConstants.USER_DYSLEXIC_FONT,
                    "" + false);
    userSettings.saveSettings(new Command(){
      public void execute() {
        //Window.Location.reload();
      };
    });
    String value = userSettings.getSettings("GeneralSettings").
    getPropertyValue("DyslexicFont");
    assertEquals(false, Boolean.parseBoolean(value));
    assertFalse(Ode.getUserDyslexicFont());

    userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
            changePropertyValue(SettingsConstants.USER_DYSLEXIC_FONT,
                    "" + true);
    userSettings.saveSettings(new Command(){
      public void execute() {
        //Window.Location.reload();
      };
    });
    String value_true = userSettings.getSettings("GeneralSettings").
    getPropertyValue("DyslexicFont");
    
    assertEquals(true, Boolean.parseBoolean(value_true));
    assertTrue(Ode.getUserDyslexicFont());
  }

  /*public void testSetUserDyslexicFont() {
    User user = new User("1","abc@email.com",true,true,null);
    UserSettings userSettings = new UserSettings(user);
    userSettings.getSettings("GeneralSettings").changePropertyValue(
      "DyslexicFont","");
    //Command command = new Command(){};
    userSettings.saveSettings(null);
  }*/
}
