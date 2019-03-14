package com.google.appinventor.server;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.util.PasswordHash;
import com.google.appinventor.shared.rpc.user.User;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Servlet for creating users.
 *
 * This needs to be done from a servlet that does not require login.
 */
public class UserCreationServlet extends OdeServlet {

  // Logging support
  private static final Logger LOG = Logger.getLogger(UserCreationServlet.class.getName());

  private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
  private final PolicyFactory sanitizer = new HtmlPolicyBuilder().allowElements("p").toFactory();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (!Flag.createFlag("enable.createuser", false).get()) return;

    Map<String, String[]> map = req.getParameterMap();
    User user = storageIo.getUserFromEmail(map.get("email")[0]);

    try {
      String hashedpassword = PasswordHash.createHash(map.get("password")[0]);
      LOG.info("hashed password + " + hashedpassword);
      storageIo.setUserPassword(user.getUserId(), hashedpassword);

      LOG.info("created user for " + user.getUserEmail() + " hashed password + " + user.getPassword() + " id " + user.getUserId());
    } catch (NoSuchAlgorithmException e) {
      fail(req, resp, "System Error hashing password");
      return;
    } catch (InvalidKeySpecException e) {
      fail(req, resp, "System Error hashing password");
      return;
    }
  }

  private void fail(HttpServletRequest req, HttpServletResponse resp, String error) throws IOException {
    resp.sendRedirect("/login/?error=" + sanitizer.sanitize(error));
    return;
  }
}
