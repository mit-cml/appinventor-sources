package com.google.appinventor.server;

import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.GalleryStorageIo;
import com.google.appinventor.server.storage.GalleryStorageIoInstanceHolder;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryService;
import com.google.appinventor.shared.rpc.project.GallerySettings;
import com.google.appinventor.shared.rpc.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet for receiving project information to import into the AI2 Gallery.
 *
 * This needs to be done from a servlet that does not require login.
 */
public class RemoveGalleryProjectServlet extends OdeServlet {

  // Logging support
  private static final Logger LOG = Logger.getLogger(RemoveGalleryProjectServlet.class.getName());

  private final GalleryService galleryService = new GalleryServiceImpl();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    if (!Flag.createFlag("enable.receivegalleryproject", false).get()) return;

    Map<String, String[]> map = req.getParameterMap();
    long galleryId = Long.valueOf(map.get("galleryId")[0]);

    galleryService.deleteApp(galleryId);
    LOG.info("deleted gallery app with id " + galleryId);
  }
}
