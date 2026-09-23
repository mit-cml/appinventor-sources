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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class UploadPost extends HttpServlet {

  private BlobstoreService blobstoreService = 
    BlobstoreServiceFactory.getBlobstoreService();

  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws
      IOException {

    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();

    Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
    if (blobs.keySet().isEmpty()) {
      resp.sendRedirect("/?error=" + 
        URLEncoder.encode("No file uploaded", "UTF-8"));
      return;
    }

    Iterator<String> names = blobs.keySet().iterator();
    String blobName = names.next();
    BlobKey blobKey = blobs.get(blobName);

    if (user == null) {
      blobstoreService.delete(blobKey);
      resp.sendRedirect("/?error=" + 
        URLEncoder.encode("Must be logged in to upload", "UTF-8"));
      return;
    }

    BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
    BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);

    String contentType = blobInfo.getContentType();
    long size = blobInfo.getSize();
    Date creation = blobInfo.getCreation();
    String fileName = blobInfo.getFilename();

    String title = req.getParameter("title");
    String description = req.getParameter("description");
    boolean isShared = "public".equalsIgnoreCase(req.getParameter("share"));

    try {
      MediaObject mediaObj = new MediaObject(user, blobKey, creation, 
        contentType, fileName, size, title, description, isShared);
      PMF.get().getPersistenceManager().makePersistent(mediaObj);
      resp.sendRedirect("/");
    } catch (Exception e) {
      blobstoreService.delete(blobKey);
      resp.sendRedirect("/?error=" + 
        URLEncoder.encode("Object save failed: " + e.getMessage(), "UTF-8"));
    }
  }
}
