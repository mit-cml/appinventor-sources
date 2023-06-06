package com.google.appinventor.client.settings;

import com.google.appinventor.client.settings.user.UserSettings;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.junit.client.GWTTestCase;

public class GeneralSettingsTest extends GWTTestCase {
    @Override
    public String getModuleName() {
        return "com.google.appinventor.client.settings";
    }
    User user = new User("1","abc@email.com",true,true,null);
    UserSettings userSettings = new UserSettings(user);

    public void test_User_Dyslexic_font_true() {

        userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
            changePropertyValue(SettingsConstants.USER_DYSLEXIC_FONT,
                "" + true);

        String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
                           getPropertyValue(SettingsConstants.USER_DYSLEXIC_FONT);
        assertTrue(Boolean.parseBoolean(value));
    }
}
