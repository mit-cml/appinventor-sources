package com.google.appinventor.server;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tools.cloudstorage.*;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.http.MultipartMap;
import com.google.appinventor.server.storage.GalleryStorageIo;
import com.google.appinventor.server.storage.GalleryStorageIoInstanceHolder;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryService;
import com.google.appinventor.shared.rpc.project.GallerySettings;
import com.google.appinventor.shared.rpc.user.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet for receiving project images to import into the AI2 Gallery.
 *
 * This needs to be done from a servlet that does not require login.
 */
public class ReceiveGalleryProjectImageServlet extends OdeServlet {

  // Logging support
  private static final Logger LOG = Logger.getLogger(ReceiveGalleryProjectImageServlet.class.getName());
  private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

  private final GalleryService galleryService = new GalleryServiceImpl();
  private final GallerySettings settings = galleryService.loadGallerySettings();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    if (!Flag.createFlag("enable.receivegalleryproject", false).get()) return;

    // URIs for receivegalleryproject requests are structured as follows:
    //   /<baseurl>/receivegalleryprojectimage/galleryProjectId
    String uriComponents[] = req.getRequestURI().split("/", 4);
    long galleryId = Long.parseLong(uriComponents[3]);

    try {
      GcsService gcsService = GcsServiceFactory.createGcsService();
      InputStream input = req.getInputStream();

      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
      int bytesRead;
      ByteArrayOutputStream bao = new ByteArrayOutputStream();

      while ((bytesRead = input.read(buffer)) != -1) {
        bao.write(buffer, 0, bytesRead);
      }
      // close the project image file
      input.close();

      // if image is greater than 200 X 200, it will be scaled (200 X 200).
      // otherwise, it will be stored as origin.
      byte[] oldImageData = bao.toByteArray();
      byte[] newImageData;
      ImagesService imagesService = ImagesServiceFactory.getImagesService();
      Image oldImage = ImagesServiceFactory.makeImage(oldImageData);
      //if image size is too big, scale it to a smaller size.
      if(oldImage.getWidth() > 200 && oldImage.getHeight() > 200){
        Transform resize = ImagesServiceFactory.makeResize(200, 200);
        Image newImage = imagesService.applyTransform(resize, oldImage);
        newImageData = newImage.getImageData();
      }else{
        newImageData = oldImageData;
      }

      // set up the cloud file (options)
      // After publish, copy the /projects/projectId image into /apps/appId
      String galleryKey = settings.getImageKey(galleryId);

      GcsFilename outfilename = new GcsFilename(settings.getBucket(), galleryKey);
      GcsFileOptions options = new GcsFileOptions.Builder().mimeType("image/jpeg")
          .acl("public-read").cacheControl("no-cache").build();
      GcsOutputChannel writeChannel = gcsService.createOrReplace(outfilename, options);
      writeChannel.write(ByteBuffer.wrap(newImageData));

      // Now finalize
      writeChannel.close();
      LOG.log(Level.INFO, "added image for gallery id " + galleryId);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.INFO, "FAILED WRITING IMAGE TO GCS");
      e.printStackTrace();
    }
  }
}
