package com.google.appinventor.server.util;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class JsonpUtil {

  public static void writeJsonResponse(HttpServletResponse resp, List<?> data) throws IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    Gson gson = new Gson();
    String json = gson.toJson(data);
    resp.getWriter().write(json);
  }
}
