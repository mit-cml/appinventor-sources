package appengine.tests.com.google.appinventor.client.settings;

import org.eclipse.jetty.security.jaspi.modules.UserInfo;
import junit.framework.TestCase;


import com.google.api.server.spi.auth.common.User;
import com.google.appinventor.client.settings.user.GeneralSettings;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import com.google.appinventor.shared.settings.SettingsConstants;

public class GeneralSettings {
    //User user = new User("1","abc@email.com","true","true","sessionid");
    UserInfoProvider user = new UserInfoProvider() {
        @Override
        public String getUserId() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getUserId'");
        }


        @Override
        public String getUserEmail() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getUserEmail'");
        }


        @Override
        public User getUser() {
            try {
            User user= new User("1","abc@email.com");

            //user.setUserId("1");
            return user;
            }
            catch(UnsupportedOperationException un) { };
            //throw new UnsupportedOperationException("Unimplemented method 'getUserTosAccepted'");
        }


        @Override
        public boolean getUserTosAccepted() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getUserTosAccepted'");
        }


        @Override
        public boolean getIsAdmin() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getIsAdmin'");
        }


        @Override
        public String getSessionId() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getSessionId'");
        }


        @Override
        public void setReadOnly(boolean value) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setReadOnly'");
        }


        @Override
        public boolean isReadOnly() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isReadOnly'");
        }


        @Override
        public void setSessionId(String SessionId) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setSessionId'");
        };
    };
    GeneralSettings generalSetting = new GeneralSettings(user);
    EditableProperties editableProperties = new EditableProperties(true);
    EditableProperty editableProperty = new EditableProperty(editableProperties,
    SettingsConstants.USER_DYSLEXIC_FONT, "false",EditableProperty.TYPE_INVISIBLE);
    //assertEquals(editableProperty);
    public void test_User_Dyslexic_font() {
        generalSetting.addProperty(editableProperty);
    }

    // SettingsConstants.USER_DYSLEXIC_FONT, "false",0));
    //assertTrue("Runs the method",addProperty(editableProperty));
}
