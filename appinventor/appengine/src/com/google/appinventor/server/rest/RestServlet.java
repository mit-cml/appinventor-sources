package com.google.appinventor.server.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appinventor.server.UserInfoServiceImpl;
import com.google.appinventor.server.util.CacheHeaders;
import com.google.appinventor.server.util.CacheHeadersImpl;
import com.google.gson.Gson;

public abstract class RestServlet extends HttpServlet {
  private static final CacheHeaders CACHE_HEADERS = new CacheHeadersImpl();
  private static final Gson GSON = new Gson();
  protected static final UserInfoServiceImpl USER_INFO_SERVICE = new UserInfoServiceImpl();

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    CACHE_HEADERS.setNotCacheable(resp);
    if ("PATCH".equals(req.getMethod())) {
      doPatch(req, resp);
    } else {
      super.service(req, resp);
    }
  }

  protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  protected static String getPath(HttpServletRequest request) {
    return Optional.ofNullable(request.getPathInfo()).map(pathInfo -> pathInfo.substring(1))
        .orElse(null);
  }

  protected static <T> T getBody(HttpServletRequest req, Class<T> reqClass) throws IOException {
    final BufferedReader reader = req.getReader();
    final String json = reader.lines().collect(Collectors.joining("\n"));
    return GSON.fromJson(json, reqClass);
  }

  protected static void setBody(HttpServletResponse resp, Object respObj) throws IOException {
    resp.setContentType("application/json; charset=utf-8");
    resp.getWriter().write(GSON.toJson(respObj));
  }

  protected static void setStatus(HttpServletResponse resp, int status) {
    resp.setStatus(status);
  }
}
