package com.google.appinventor.server;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.GalleryStorageIo;
import com.google.appinventor.server.storage.GalleryStorageIoInstanceHolder;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Servlet for receiving project information to import into the AI2 Gallery.
 *
 * This needs to be done from a servlet that does not require login.
 */
public class ReceiveGalleryProjectInfoServlet extends OdeServlet {

  // Logging support
  private static final Logger LOG = Logger.getLogger(ReceiveGalleryProjectInfoServlet.class.getName());

  private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
  private final transient GalleryStorageIo galleryStorageIo = GalleryStorageIoInstanceHolder.INSTANCE;

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (!Flag.createFlag("enable.receivegalleryproject", false).get()) return;

    Map<String, String[]> map = req.getParameterMap();
    String title = map.containsKey("title") ? map.get("title")[0] : null;
    String projectName = title;
    String description = map.containsKey("description") ? map.get("description")[0] : null;
    String moreInfo = map.containsKey("moreInfo") ? map.get("moreInfo")[0] : null;
    String credit = map.containsKey("credit") ? map.get("credit")[0] : null;
    long projectId = Long.valueOf(map.get("projectId")[0]);
    String email = map.get("email")[0];
    boolean isFeatured = map.containsKey("isFeatured") && map.get("isFeatured")[0].equalsIgnoreCase("true");
    User user = storageIo.getUserFromEmail(email);

    GalleryApp galleryApp = galleryStorageIo.createGalleryApp(title, projectName, description, moreInfo, credit, projectId, user.getUserId());
    // put meta data in search indexReceiveGalleryProjectServlet
    GallerySearchIndex.getInstance().indexApp(galleryApp);

    long galleryId = galleryApp.getGalleryAppId();
    LOG.info("created gallery app with id " + galleryId + " title " + title);

    // Mark as featured as needed
    if (isFeatured) galleryStorageIo.markAppAsFeatured(galleryId);

    resp.getWriter().write(String.valueOf(galleryId));
    resp.getWriter().flush();
    resp.getWriter().close();
  }
}
