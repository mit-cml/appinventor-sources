package com.google.appinventor.server;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tools.cloudstorage.*;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.http.MultipartMap;
import com.google.appinventor.server.storage.*;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryService;
import com.google.appinventor.shared.rpc.project.GallerySettings;
import com.google.appinventor.shared.rpc.user.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet for receiving project information to import into the AI2 Gallery.
 *
 * This needs to be done from a servlet that does not require login because
 * posts from the Build Server do not contain login information.
 */
@MultipartConfig
public class ReceiveGalleryProjectServlet extends OdeServlet {

  // Logging support
  private static final Logger LOG = Logger.getLogger(ReceiveBuildServlet.class.getName());
  private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

  private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
  private final transient GalleryStorageIo galleryStorageIo = GalleryStorageIoInstanceHolder.INSTANCE;
  private final GalleryService galleryService = new GalleryServiceImpl();
  private final GallerySettings settings = galleryService.loadGallerySettings();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    if (!Flag.createFlag("enable.receivegalleryproject", false).get()) return;

    MultipartMap map = new MultipartMap(req, this);
    String title = map.getParameter("title");
    String projectName = map.getParameter("projectName");
    String description = map.getParameter("description");
    String moreInfo = map.getParameter("moreInfo");
    String credit = map.getParameter("credit");
    long projectId = Long.valueOf(map.getParameter("projectId"));
    String email = map.getParameter("email");
    boolean isFeatured = map.getParameter("isFeatured").equalsIgnoreCase("true");
    User user = storageIo.getUserFromEmail(email);

    GalleryApp galleryApp = galleryStorageIo.createGalleryApp(title, projectName, description, moreInfo, credit, projectId, user.getUserId());

    long galleryId = galleryApp.getGalleryAppId();

    // Mark as featured as needed
    if (isFeatured) galleryStorageIo.markAppAsFeatured(galleryId);

    // Store AIA file
    if (req.getPart("aiaFile") != null) {
      storeAIA(galleryId, projectName, req);
    }

    // Set gallery app image
    if (req.getPart("imageFile") != null) {
      setGalleryAppImage(galleryId, req);
    }
  }

  private void storeAIA(long galleryId, String projectName, HttpServletRequest req) throws IOException, ServletException {
    LOG.info("Storing aia file for gallery project: " + projectName + " galleryId: " + String.valueOf(galleryId));

    // build the aia file name using the ai project name and code stolen
    // from DownloadServlet to normalize...
    String aiaName = StringUtils.normalizeForFilename(projectName) + ".aia";

    InputStream input = new BufferedInputStream(req.getPart("aiaFile").getInputStream(), DEFAULT_BUFFER_SIZE);

    // setup cloud
    GcsService gcsService = GcsServiceFactory.createGcsService();
    String galleryKey = settings.getSourceKey(galleryId);
    GcsFilename filename = new GcsFilename(settings.getBucket(), galleryKey);

    GcsFileOptions options = new GcsFileOptions.Builder().mimeType("application/zip")
        .acl("public-read").cacheControl("no-cache").addUserMetadata("title", aiaName).build();
    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);

    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    for (int length; ((length = input.read(buffer)) > 0);) {
      writeChannel.write(ByteBuffer.wrap(buffer, 0, length));
    }

    // Now finalize
    input.close();
    writeChannel.close();
  }

  private void setGalleryAppImage(long galleryId, HttpServletRequest req) throws ServletException {
    try {
      GcsService gcsService = GcsServiceFactory.createGcsService();
      InputStream input = new BufferedInputStream(req.getPart("imageFile").getInputStream(), DEFAULT_BUFFER_SIZE);

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

    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.INFO, "FAILED WRITING IMAGE TO GCS");
      e.printStackTrace();
    }
  }
}
