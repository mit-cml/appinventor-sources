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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Servlet for storing AIA to import into the AI2 Gallery.
 *
 * This needs to be done from a servlet that does not require login.
 */
public class ReceiveGalleryProjectServlet extends OdeServlet {

  // Logging support
  private static final Logger LOG = Logger.getLogger(ReceiveGalleryProjectServlet.class.getName());
  private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

  private final GalleryService galleryService = new GalleryServiceImpl();
  private final GallerySettings settings = galleryService.loadGallerySettings();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (!Flag.createFlag("enable.receivegalleryproject", false).get()) return;

    // URIs for receivegalleryproject requests are structured as follows:
    //   /<baseurl>/receivegalleryproject/galleryProjectId/projectName
    String uriComponents[] = req.getRequestURI().split("/", 5);
    long galleryId = Long.parseLong(uriComponents[3]);
    String projectName = uriComponents[4];

    LOG.info("Storing aia file for gallery project: " + projectName + " galleryId: " + String.valueOf(galleryId));

    // build the aia file name using the ai project name and code stolen
    // from DownloadServlet to normalize...
    String aiaName = StringUtils.normalizeForFilename(projectName) + ".aia";

    InputStream input = req.getInputStream();

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
}
