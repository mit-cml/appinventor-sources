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

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Upload extends HttpServlet {

  private BlobstoreService blobstoreService =
    BlobstoreServiceFactory.getBlobstoreService();

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws
      IOException, ServletException {

    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();

    String authURL = userService.createLogoutURL("/");
    String uploadURL = blobstoreService.createUploadUrl("/post");

    req.setAttribute("uploadURL", uploadURL);
    req.setAttribute("authURL", authURL);
    req.setAttribute("user", user);

    RequestDispatcher dispatcher = 
      req.getRequestDispatcher("WEB-INF/templates/upload.jsp");
    dispatcher.forward(req, resp);
  }
}
