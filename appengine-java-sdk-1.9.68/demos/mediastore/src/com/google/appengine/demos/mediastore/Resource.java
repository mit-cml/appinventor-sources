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
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Resource extends HttpServlet {
  private BlobstoreService blobstoreService =
    BlobstoreServiceFactory.getBlobstoreService();

  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    BlobKey blobKey = new BlobKey(req.getParameter("key"));

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

    MediaObject result = results.get(0);
    if (!result.isPublic()) {
      UserService userService = UserServiceFactory.getUserService();
      User user = userService.getCurrentUser();

      if (!result.getOwner().equals(user)) {
        resp.sendRedirect("/?error=" +
          URLEncoder.encode("Not authorized to access", "UTF-8"));
        return;
      }
    }

    String rotation = req.getParameter("rotate");
    if (rotation != null && !"".equals(rotation) && !"null".equals(rotation)) {
      int degrees = Integer.parseInt(rotation);

      ImagesService imagesService = ImagesServiceFactory.getImagesService();
      Image image = ImagesServiceFactory.makeImageFromBlob(blobKey);
      Transform rotate = ImagesServiceFactory.makeRotate(degrees);
      Image newImage = imagesService.applyTransform(rotate, image);
      byte[] imgbyte = newImage.getImageData();

      resp.setContentType(result.getContentType());
      resp.getOutputStream().write(imgbyte);
      return;
    }
    blobstoreService.serve(blobKey, resp);
  }
}
