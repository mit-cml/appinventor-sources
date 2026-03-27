package com.google.appinventor.server.rest.user;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appinventor.server.rest.RestServlet;

public class UserBackpackRestServlet extends RestServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setBody(resp, USER_INFO_SERVICE.getUserBackpack());
    setStatus(resp, HttpServletResponse.SC_OK);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    USER_INFO_SERVICE.storeUserBackpack(getBody(req, String.class));
    setStatus(resp, HttpServletResponse.SC_OK);
  }
}
