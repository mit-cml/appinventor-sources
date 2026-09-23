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
public class DeleteFlight extends HttpServlet {

  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String key = req.getParameter("key");
    if (key == null) {
      resp.getWriter().println("No key provided.");
    }
    if (PersistenceStandard.get() == PersistenceStandard.JPA) {
      doPostJPA(Long.valueOf(key));
    } else {
      doPostJDO(Long.valueOf(key));
    }
    resp.sendRedirect("/");
  }

  private void doPostJDO(long key) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Flight f = pm.getObjectById(Flight.class, key);
      pm.deletePersistent(f);
    } finally {
      if (pm.currentTransaction().isActive()) {
        pm.currentTransaction().rollback();
      }
      pm.close();
    }
  }

  private void doPostJPA(long key) {
    EntityManager em = EMF.get().createEntityManager();
    try {
      Flight f = em.find(Flight.class, key);
      em.remove(f);
    } finally {
      em.close();
    }
  }

}
