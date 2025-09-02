package com.google.appinventor.server.rest.user;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.server.rest.RestServlet;

public class UserSystemConfigRestServlet extends RestServlet {
  @Override
  protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final Config userSystemConfigPatch = getBody(req, Config.class);
    String userSessionId = Optional.ofNullable(userSystemConfigPatch).map(Config::getUser)
        .map(User::getSessionId).orElse(null);
    setBody(resp, USER_INFO_SERVICE.getSystemConfig(userSessionId));
    setStatus(resp, HttpServletResponse.SC_OK);
  }
}
