package com.google.appinventor.server.rest.user;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appinventor.server.rest.RestServlet;

public class UserNoopRestServlet extends RestServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    USER_INFO_SERVICE.noop();
    setStatus(resp, HttpServletResponse.SC_OK);
  }
}
