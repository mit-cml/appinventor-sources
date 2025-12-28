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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Display extends HttpServlet {

  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {

    String blobKeyString = req.getParameter("key");
    if (blobKeyString == null || blobKeyString.equals("")) {
      resp.sendRedirect("/?error=" + 
        URLEncoder.encode("BlobKey not provided", "UTF-8"));
      return;
    }

    BlobKey blobKey = new BlobKey(blobKeyString);
    PersistenceManager pm = PMF.get().getPersistenceManager();

    Query query = pm.newQuery(MediaObject.class, "blob == blobParam");
    query.declareImports("import " +
      "com.google.appengine.api.blobstore.BlobKey");
    query.declareParameters("BlobKey blobParam");

    List<MediaObject> results = (List<MediaObject>) query.execute(blobKey);
    if (results.isEmpty()) {
      resp.sendRedirect("/?error=" +
        URLEncoder.encode("BlobKey does not exist", "UTF-8"));
      return;
    }

    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();

    MediaObject result = results.get(0);
    if (!result.isPublic() && !result.getOwner().equals(user)) {
      resp.sendRedirect("/?error=" +
        URLEncoder.encode("Not authorized to access", "UTF-8"));
      return;
    }

    String rotation = req.getParameter("rotate");
    String displayURL = result.getURLPath() + "&rotate=" + rotation;
    String authURL = (user != null) ?
      userService.createLogoutURL("/") : userService.createLoginURL("/");

    req.setAttribute("displayURL", displayURL);
    req.setAttribute("authURL", authURL);
    req.setAttribute("user", user);
    req.setAttribute("rotation", rotation);
    req.setAttribute("item", result);
    req.setAttribute("blobkey", blobKeyString);

    RequestDispatcher dispatcher =
      req.getRequestDispatcher("WEB-INF/templates/display.jsp");
    dispatcher.forward(req, resp);
  }
}
