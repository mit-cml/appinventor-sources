// Copyright 2008 Google Inc. All Rights Reserved.
package com.google.appengine.demos.helloorm;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Max Ross <maxr@google.com>
 */
public class GetFlight extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html");
    String key = req.getParameter("key");
    if (key == null) {
      resp.getWriter().println("No key provided.");
      return;
    }
    Flight f;
    if (PersistenceStandard.get() == PersistenceStandard.JPA) {
      f = findJPA(Long.valueOf(key));
    } else {
      f = findJDO(Long.valueOf(key));
    }
    resp.getWriter().println("<form action=\"updateFlight\" method=\"post\">");
    resp.getWriter().println("<input name=\"key\" type=\"hidden\" value=\"" + key + "\"/>");
    resp.getWriter().println("<table>");
    resp.getWriter().println("<tr>");
    resp.getWriter().println("<th>Origin</th><td><input name=\"orig\" type=\"text\" value=\"" + f.getOrig() + "\"/></td>");
    resp.getWriter().println("<th>Destination</th><td><input name=\"dest\" type=\"text\" value=\"" + f.getDest() + "\"/></td>");
    resp.getWriter().println("</tr>");
    resp.getWriter().println("<tr><td><input type=\"submit\" value=\"Update Flight\"></td></tr>");
    resp.getWriter().println("</table>");
    resp.getWriter().println("</form>");
    resp.getWriter().println("<form action=\"deleteFlight\" method=\"post\">");
    resp.getWriter().println("<input name=\"key\" type=\"hidden\" value=\"" + key + "\"/>");
    resp.getWriter().println("<input type=\"submit\" value=\"Delete Flight\"></td></tr>");
    resp.getWriter().println("</form>");
  }

  private Flight findJPA(long key) {
    EntityManager em = EMF.get().createEntityManager();
    try {
      return em.find(Flight.class, key);
    } finally {
      em.close();
    }
  }

  private Flight findJDO(long key) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      return pm.getObjectById(Flight.class, key);
    } finally {
      pm.close();
    }
  }
}
