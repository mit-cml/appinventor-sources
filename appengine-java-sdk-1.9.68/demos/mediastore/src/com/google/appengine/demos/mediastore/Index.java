/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.appengine.demos.mediastore;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Index extends HttpServlet {

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws
      IOException, ServletException {

    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();

    String authURL = (user != null) ? userService.createLogoutURL("/")
      : userService.createLoginURL("/");

    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    Query query = pm.newQuery(MediaObject.class, "owner == userParam");
    query.declareImports("import com.google.appengine.api.users.User");
    query.declareParameters("User userParam");

    List<MediaObject> results = (List<MediaObject>) query.execute(user);

    String[] errors = req.getParameterValues("error");
    if (errors == null) errors = new String[0];

    req.setAttribute("errors", errors);
    req.setAttribute("files", results);
    req.setAttribute("authURL", authURL);
    req.setAttribute("user", user);
    RequestDispatcher dispatcher =
      req.getRequestDispatcher("WEB-INF/templates/main.jsp");
    dispatcher.forward(req, resp);
  }
}
