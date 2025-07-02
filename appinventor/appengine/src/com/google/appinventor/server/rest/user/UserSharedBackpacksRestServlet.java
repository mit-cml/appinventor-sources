package com.google.appinventor.server.rest.user;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appinventor.server.rest.RestServlet;

public class UserSharedBackpacksRestServlet extends RestServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String backPackId = getPath(req);
    if (backPackId == null) {
      setStatus(resp, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    setBody(resp, USER_INFO_SERVICE.getSharedBackpack(backPackId));
    setStatus(resp, HttpServletResponse.SC_OK);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String backPackId = getPath(req);
    if (backPackId == null) {
      setStatus(resp, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    USER_INFO_SERVICE.storeSharedBackpack(backPackId, getBody(req, String.class));
    setStatus(resp, HttpServletResponse.SC_OK);
  }
}
