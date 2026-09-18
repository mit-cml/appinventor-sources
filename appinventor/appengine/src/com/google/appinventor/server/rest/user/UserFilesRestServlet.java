package com.google.appinventor.server.rest.user;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appinventor.server.rest.RestServlet;

public class UserFilesRestServlet extends RestServlet {
  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String fileName = getPath(req);
    if (fileName == null) {
      setStatus(resp, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (USER_INFO_SERVICE.hasUserFile(fileName)) {
      setStatus(resp, HttpServletResponse.SC_OK);
    } else {
      setStatus(resp, HttpServletResponse.SC_NOT_FOUND);
    }
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String fileName = getPath(req);
    if (fileName == null) {
      setStatus(resp, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    USER_INFO_SERVICE.deleteUserFile(fileName);
    setStatus(resp, HttpServletResponse.SC_OK);
  }
}
