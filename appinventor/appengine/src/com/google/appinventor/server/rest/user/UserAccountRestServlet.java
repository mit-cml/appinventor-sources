package com.google.appinventor.server.rest.user;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appinventor.server.rest.RestServlet;

public class UserAccountRestServlet extends RestServlet {
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String deleteAccountUrl = USER_INFO_SERVICE.deleteAccount();
    setBody(resp, deleteAccountUrl);
    setStatus(resp, HttpServletResponse.SC_OK);
  }
}
