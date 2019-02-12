package com.google.appinventor.server.galleryAuth;

import com.google.appinventor.server.OdeRemoteServiceServlet;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.shared.rpc.gallery.GalleryAuthService;
import com.google.appinventor.shared.rpc.user.User;
import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;
import org.keyczar.util.Base64Coder;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Gallery Authentication Service implementation
 */
public class GalleryAuthServlet extends OdeRemoteServiceServlet implements GalleryAuthService {

  private static final Logger LOG = Logger.getLogger(GalleryAuthServlet.class.getName());

  private static Crypter crypter = null; // accessed through getCrypter only
  private static final Object crypterSync = new Object();

  private static final Flag<String> sessionKeyFile = Flag.createFlag("session.keyfile", "WEB-INF/authkey");
  private static final Flag<Integer> renewTime = Flag.createFlag("session.renew", 30);
  private static final boolean DEBUG = Flag.createFlag("appinventor.debugging", false).get();

  /*
   * returns the token to log into the new Gallery server
   */
  @Override
  public String getToken() {
    User user =  userInfoProvider.getUser();

    UserInfo userInfo = new UserInfo(user.getUserId(), user.getUserEmail(), user.getIsAdmin());
    if (user.getIsAdmin()) { // If we are a developer, we are always an admin
      userInfo.setIsAdmin(true);
    }

    String newToken = userInfo.buildToken(false);
    if (DEBUG) {
      LOG.info("newToken = " + newToken);
    }
    return newToken;
  }

  /*
   * represents the data encoded in the Gallery login token
   */
  public static class UserInfo implements Serializable {
    String userId;
    String email;
    boolean isAdmin;
    String domain = "ai2.appinventor.mit.edu";
    long ts;

    transient boolean modified = false;

    public UserInfo(String userId, String email, boolean isAdmin) {
      this.userId = userId;
      this.email = email;
      this.isAdmin = isAdmin;
      this.ts = System.currentTimeMillis();
    }

    public void setUserId(String userId) {
      this.userId = userId;
      modified = true;
    }

    public void setEmail(String email) {
      this.email = email;
      modified = true;
    }

    public void setIsAdmin(boolean isAdmin) {
      this.isAdmin = isAdmin;
      modified = true;
    }

    public String getUserId() {
      return userId;
    }

    public String getEmail() {
      return email;
    }

    public boolean getIsAdmin() {
      return isAdmin;
    }

    public String buildToken(boolean ifNeeded) {
      try {
        long offset = System.currentTimeMillis() - this.ts;
        offset /= 1000;
        if (offset > (60*renewTime.get())) {    // Renew if it is time
          modified = true;
          ts = System.currentTimeMillis();
        }
        if (!ifNeeded || modified) {
          Crypter crypter = getCrypter();
          GalleryAuth.token token = GalleryAuth.token.newBuilder()
                  .setUuid(this.userId)
                  .setTs(this.ts)
                  .setIsAdmin(this.isAdmin)
                  .setDomain(this.domain)
                  .setEmail(this.email).build();
          return Base64Coder.encode(crypter.encrypt(token.toByteArray()));
        } else {
          return null;
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static Crypter getCrypter() throws KeyczarException {
    synchronized(crypterSync) {
      if (crypter != null) {
        return crypter;
      } else {
        crypter = new Crypter(sessionKeyFile.get());
        return crypter;
      }
    }
  }
}

