// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.demos.helloservers;

import com.google.appengine.api.backends.BackendService;
import com.google.appengine.api.backends.BackendServiceFactory;

import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple app for experimenting with App Engine Backends.
 *
 *
 */
public class HelloServers extends HttpServlet {
  private static final Map<String, AtomicInteger> vars;
  static {
    vars = new ConcurrentHashMap<String, AtomicInteger>();
    vars.put("requests", new AtomicInteger(0));
  }

  /**
   * Loops for a number of seconds, printing a message every second.
   *
   * @param seconds The number of seconds to loop.
   * @param writer The writer to use when printing messages.
   */
  private void loop(int seconds, PrintWriter writer) {
    try {
      for (int i = 0; i < seconds; i++) {
        Thread.sleep(1000);
        writer.append("slept for " + i + "/" + seconds + " seconds\n");
      }
    } catch (InterruptedException e) {
      writer.append("loop canceled\n");
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    res.setHeader("Content-Type", "text/plain");
    int requestCount = vars.get("requests").incrementAndGet();
    res.getWriter().append("Requests: " + requestCount + "\n");

    BackendService backendsApi = BackendServiceFactory.getBackendService();
    String backend = backendsApi.getCurrentBackend();
    if (backend != null) {
      res.getWriter().append("Backend: " + backend + "\n");
      int instance = backendsApi.getCurrentInstance();
      res.getWriter().append("Instance: " + instance + "\n");
    }

    handleOperations(req, res);

    if ("job".equals(backend)) {
      loop(24 * 60 * 60, res.getWriter());
    }
  }

  private void handleOperations(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    String grow = req.getParameter("grow");
    if (grow != null) {
      try {
        int megabytes = Integer.parseInt(grow);
        State.grow(megabytes);
        res.getWriter().append("Growing by " + megabytes + "MB\n");
      } catch (NumberFormatException e) {
        res.getWriter().append("The grow parameter must have an integer value.");
      }
    }
    if (State.size() > 0) {
      res.getWriter().append("State size is: " + State.size() + "MB\n");
    }

    String loop = req.getParameter("loop");
    if (loop != null) {
      try {
        int seconds = Integer.parseInt(loop);
        loop(seconds, res.getWriter());
      } catch (NumberFormatException e) {
        res.getWriter().append("The loop parameter must have an integer value.");
      }
    }

    if (req.getParameter("env") != null) {
      showEnv(req, res.getWriter());
    }
  }

  private static void showEnv(HttpServletRequest req, PrintWriter writer) {
    {
      writer.append("\n################ Thread Local Envrionment###################\n");
      Environment env = ApiProxy.getCurrentEnvironment();

      writer.append("app_id: " + env.getAppId() + "\n");
      writer.append("auth_domain: " + env.getAuthDomain() + "\n");
      writer.append("email: " + env.getEmail() + "\n");
      writer.append("version_id: " + env.getVersionId() + "\n");

      for (Entry<String, Object> e : env.getAttributes().entrySet()) {
        writer.append(e.getKey() + ": " + e.getValue() + "\n");
      }
    }
    {
      writer.append("################ Request Headers ###################\n");
      Enumeration headers = req.getHeaderNames();
      while (headers.hasMoreElements()) {
        Object h = headers.nextElement();
        writer.append(h + ": " + req.getHeader((String) h) + "\n");
      }
    }
    {
      writer.append("\n################ Request Attributes ###################\n");
      Enumeration attr = req.getAttributeNames();
      while (attr.hasMoreElements()) {
        Object a = attr.nextElement();
        writer.append(a + ": " + req.getAttribute((String) a) + "\n");
      }
    }
    {
      writer.append("\n################ Request Parameters ###################\n");
      Enumeration attr = req.getParameterNames();
      while (attr.hasMoreElements()) {
        Object a = attr.nextElement();
        writer.append(a + ": " + req.getParameter((String) a) + "\n");
      }
    }
    {
      if (req.getCookies() != null) {
        writer.append("\n################ Request Cookies ###################\n");
        for (Cookie c : req.getCookies()) {
          writer.append(
              c.getName() + "(" + c.getDomain() + ": " + c.getPath() + ": " + c.getMaxAge() + "): "
                  + c.getValue() + "\n");
        }
      }
    }
    {
      writer.append("\n################ System properties ###################\n");
      Properties p = System.getProperties();
      for (Entry<Object, Object> e : p.entrySet()) {
        writer.append(e.getKey() + ": " + e.getValue() + "\n");
      }
    }
  }
}
