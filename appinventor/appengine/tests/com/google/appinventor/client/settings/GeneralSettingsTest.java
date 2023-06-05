package appengine.tests.com.google.appinventor.client.settings;

import static org.junit.Assert.assertFalse;

import javax.validation.constraints.AssertTrue;

//import org.eclipse.jetty.security.jaspi.modules.UserInfo;
import org.powermock.api.easymock.annotation.Mock;

//import com.google.api.server.spi.auth.common.User;
import com.google.appinventor.client.Ode;

import junit.framework.TestCase;
import org.junit.Assert;
//import static org.mockito.Mockito.*;
import com.google.appinventor.shared.rpc.user.User;
import org.powermock.api.easymock.PowerMock;
import com.google.api.server.spi.auth.common.User;
import com.google.appinventor.client.settings.user.GeneralSettings;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import com.google.appinventor.shared.settings.SettingsConstants;

import org.mockito.*;
// import org.mockito.Mock;
// import org.mockito.Mockito;

// @RunWith(PowerMockRunner.class)
// @PrepareForTest({ LocalUser.class })
public class GeneralSettingsTest {
    //User user = new User("1","abc@email.com","true","true","sessionid");
    //UserInfoProvider userInfoProvider;
    //@Mock
    //private UserInfoProvider userInfoProvider;

    Ode ode;
    boolean valueee = ode.getUserDyslexicFont();
    //system.out.println(val);
    //assertTrue(valueee, True);
    //assertFalse("false",value);
    //User user = userInfoProvider.getUser();
    /*UserInfoProvider user = new UserInfoProvider() {
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
    };*/
    // GeneralSettings generalSetting = new GeneralSettings(userInfoProvider);
    // EditableProperties editableProperties = new EditableProperties();
    // EditableProperty editableProperty = new EditableProperty(editableProperties,
    // SettingsConstants.USER_DYSLEXIC_FONT, "false",EditableProperty.TYPE_INVISIBLE);
    // //assertEquals(editableProperty);
    // public void test_User_Dyslexic_font() {
    //     editableProperties.addProperty(editableProperty);
    // }
    // @Mock
    // User user = new User();
    // user.setUserId("1");

    
    
    
    //user.User("1","abc@email.com","true","true","sessionid");


    // SettingsConstants.USER_DYSLEXIC_FONT, "false",0));
    //assertTrue("Runs the method",addProperty(editableProperty));
    User user = new User("1","abc@email.com",true,true,null);
    UserInfoProvider userInfoProvider = new UserInfoProvider() {
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
            User user= new User("1","abc@email.com",true,true,null);

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
}
