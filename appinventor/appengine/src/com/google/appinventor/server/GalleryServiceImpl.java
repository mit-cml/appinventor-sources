// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.GalleryStorageIo;
import com.google.appinventor.server.storage.GalleryStorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.Email;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.GalleryAppReport;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.GalleryModerationAction;
import com.google.appinventor.shared.rpc.project.GalleryReportListResult;
import com.google.appinventor.shared.rpc.project.GalleryService;
import com.google.appinventor.shared.rpc.project.GallerySettings;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;

/**
 * The implementation of the RPC service which runs on the server.
 *
 * <p>Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 */
public class GalleryServiceImpl extends OdeRemoteServiceServlet implements GalleryService {

  private static final Logger LOG = Logger.getLogger(GalleryServiceImpl.class.getName());
  private static final long serialVersionUID = -8316312003804169166L;
  private final transient GalleryStorageIo galleryStorageIo =
      GalleryStorageIoInstanceHolder.INSTANCE;
  // fileExporter used to get the source code from project being published
  private final FileExporter fileExporter = new FileExporterImpl();

  @Override
  public GallerySettings loadGallerySettings() {
    String bucket = Flag.createFlag("gallery.bucket", "").get();
    boolean galleryEnabled = Flag.createFlag("use.gallery",false).get();
    String envirnment = SystemProperty.environment.value().toString();
    String adminEmail = Flag.createFlag("gallery.admin.email", "").get();
    GallerySettings settings = new GallerySettings(galleryEnabled, bucket, envirnment, adminEmail);
    return settings;
  }

  /**
   * Publishes a gallery app
   * @param projectId id of the project being published
   * @param projectName name of project
   * @param title title of new gallery app
   * @param description description of new gallery app
   * @return a {@link GalleryApp} for new galleryApp
   */
  @Override
  public GalleryApp publishApp(long projectId, String title, String projectName, String description, String moreInfo, String credit)  throws IOException {
    final String userId = userInfoProvider.getUserId();
    GalleryApp app = galleryStorageIo.createGalleryApp(title, projectName, description, moreInfo, credit, projectId, userId);
    try {
      storeAIA(app.getGalleryAppId(),projectId, projectName);
    } catch (IOException e) {
      deleteApp(app.getGalleryAppId());
      throw e;
    }
    // see if there is a new image for the app. If so, its in cloud using projectId, need to move
    // to cloud using gallery id
    setGalleryAppImage(app);

    // put meta data in search index
    GallerySearchIndex.getInstance().indexApp(app);
    return app;
  }
  /**
   * update a gallery app
   * @param app info about app being updated
   * @param newImage  true if the user has submitted a new image
   */
  @Override
  public void updateApp(GalleryApp app, boolean newImage) throws IOException {
    updateAppSource(app.getGalleryAppId(),app.getProjectId(),app.getProjectName());
    updateAppMetadata(app);
    if (newImage)
      setGalleryAppImage(app);
  }
  /**
   * update a gallery app's meta data
   * @param app info about app being updated
   *
   */
  @Override
  public void updateAppMetadata(GalleryApp app) {
    final String userId = userInfoProvider.getUserId();
    galleryStorageIo.updateGalleryApp(app.getGalleryAppId(), app.getTitle(), app.getDescription(), app.getMoreInfo(), app.getCredit(), userId);
    // put meta data in search index
    GallerySearchIndex.getInstance().indexApp(app);
  }

  /**
   * update a gallery app's source (aia)
   * @param galleryId id of gallery app to be updated
   * @param projectId id of project so we can grab source
   * @param projectName name of project, this is name in new aia
   */
  @Override
  public void updateAppSource (long galleryId, long projectId, String projectName) throws IOException {
     storeAIA(galleryId,projectId, projectName);
  }

  /**
   * index all gallery apps (admin method)
   * @param count the max number of apps to index
   */
  @Override
  public void indexAll(int count) {
    List<GalleryApp> apps= getRecentApps(1,count).getApps();
    for (GalleryApp app:apps) {
      GallerySearchIndex.getInstance().indexApp(app);
    }
  }

  /**
   * Returns total number of galleryApps
   * @return number of galleryApps
   */
  @Override
  public Integer getNumApps() {
    return galleryStorageIo.getNumGalleryApps();
  }

  /**
   * Returns a wrapped class which contains list of most recently
   * updated galleryApps and total number of results in database
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  @Override
  public GalleryAppListResult getRecentApps(int start,int count) {
    return galleryStorageIo.getRecentGalleryApps(start,count);
  }

  /**
   * Returns a wrapped class which contains list of featured gallery app
   * @param start start index
   * @param count count number
   * @return list of gallery app
   */
  public GalleryAppListResult getFeaturedApp(int start, int count){
    return galleryStorageIo.getFeaturedApp(start, count);
  }

  /**
   * Returns a wrapped class which contains list of tutorial gallery app
   * @param start start index
   * @param count count number
   * @return list of gallery app
   */
  public GalleryAppListResult getTutorialApp(int start, int count){
    return galleryStorageIo.getTutorialApp(start, count);
  }

  /**
   * check if app is featured already
   * @param galleryId gallery id
   * @return true if featured, otherwise false
   */
  public boolean isFeatured(long galleryId){
    return galleryStorageIo.isFeatured(galleryId);
  }

  /**
   * check if app is tutorial already
   * @param galleryId gallery id
   * @return true if tutorial, otherwise false
   */
  public boolean isTutorial(long galleryId){
    return galleryStorageIo.isTutorial(galleryId);
  }

  /**
   * mark an app as featured
   * @param galleryId gallery id
   * @return true if successful
   */
  public boolean markAppAsFeatured(long galleryId){
    return galleryStorageIo.markAppAsFeatured(galleryId);
  }

  /**
   * mark an app as tutorial
   * @param galleryId gallery id
   * @return true if successful
   */
  public boolean markAppAsTutorial(long galleryId){
    return galleryStorageIo.markAppAsTutorial(galleryId);
  }

  /**
   * Returns a wrapped class which contains a list of galleryApps
   * by a particular developer and total number of results in database
   * @param userId id of the developer
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  @Override
  public GalleryAppListResult getDeveloperApps(String userId, int start,int count) {
    return galleryStorageIo.getDeveloperApps(userId, start,count);
  }

  /**
   * Returns a GalleryApp object for the given id
   * @param galleryId  gallery ID as received by
   *                   {@link #getRecentGalleryApps()}
   *
   * @return  gallery app object
   */
  @Override
  public GalleryApp getApp(long galleryId) {
    return galleryStorageIo.getGalleryApp(galleryId);
  }
  /**
   * Returns a wrapped class which contains a list of galleryApps and
   * total number of results in database
   * @param keywords keywords to search for
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  @Override
  public GalleryAppListResult findApps(String keywords, int start, int count) {
    return GallerySearchIndex.getInstance().find(keywords, start, count);
  }

  /**
   * Returns a wrapped class which contains a list of most downloaded
   * gallery apps and total number of results in database
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  @Override
  public GalleryAppListResult getMostDownloadedApps(int start, int count) {
    return galleryStorageIo.getMostDownloadedApps(start,count);
  }

  /**
   * Returns a wrapped class which contains a list of most liked
   * gallery apps and total number of results in database
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  @Override
  public GalleryAppListResult getMostLikedApps(int start, int count) {
    return galleryStorageIo.getMostLikedApps(start,count);
  }

  /**
   * Deletes a new gallery app
   * @param galleryId id of app to delete
   */
  @Override
  public void deleteApp(long galleryId) {
    // get rid of comments and app from database
    galleryStorageIo.deleteApp(galleryId);
    // remove the search index entry
    GallerySearchIndex.getInstance().unIndexApp(galleryId);
    // remove its image/aia from cloud
    deleteAIA(galleryId);
    deleteImage(galleryId);

  }
  /**
   * record fact that app was downloaded
   * @param galleryId id of app that was downloaded
   */
  @Override
  public void appWasDownloaded(long galleryId) {
    GallerySettings settings = loadGallerySettings();
    galleryStorageIo.incrementDownloads(galleryId);
  }
  /**
   * Returns the comments for an app
   * @param galleryId  gallery ID as received by
   *                   {@link #getRecentGalleryApps()}
   * @return  a list of comments
   */

  @Override
  public List<GalleryComment> getComments(long galleryId) {
    return galleryStorageIo.getComments(galleryId);
  }

  /**
   * publish a comment for a gallery app
   * @param galleryId the id of the app
   * @param comment the comment
   */
  @Override
  public long publishComment(long galleryId, String comment) {
    final String userId = userInfoProvider.getUserId();
    return galleryStorageIo.addComment(galleryId, userId, comment);
  }

  /**
   * increase likes for a gallery app
   * @param galleryId the id of the app
   * @return num of like
   */
  @Override
  public int increaseLikes(long galleryId) {
    final String userId = userInfoProvider.getUserId();
    return galleryStorageIo.increaseLikes(galleryId, userId);
  }

  /**
   * decrease likes for a gallery app
   * @param galleryId the id of the app
   * @return num of like
   */
  @Override
  public int decreaseLikes(long galleryId) {
    final String userId = userInfoProvider.getUserId();
    return galleryStorageIo.decreaseLikes(galleryId, userId);
  }

  /**
   * get num of likes for a gallery app
   * @param galleryId the id of the app
   */
  @Override
  public int getNumLikes(long galleryId) {
    final String userId = userInfoProvider.getUserId();
    return galleryStorageIo.getNumLikes(galleryId);
  }

  /**
   * check if an app is liked by a user
   * @param galleryId the id of the app
   */
  @Override
  public boolean isLikedByUser(long galleryId) {
    final String userId = userInfoProvider.getUserId();
    return galleryStorageIo.isLikedByUser(galleryId, userId);
  }

  /**
   * salvage the gallery app by given galleryId
   */
  @Override
  public void salvageGalleryApp(long galleryId) {
    galleryStorageIo.salvageGalleryApp(galleryId);
  }

  /**
   * adds a report (flag) to a gallery app
   * @param galleryId id of gallery app that was commented on
   * @param report report
   * @return the id of the new report
   */
  @Override
  public long addAppReport(GalleryApp app, String reportText) {
    final String reporterId = userInfoProvider.getUserId();
    String offenderId = app.getDeveloperId();
    return galleryStorageIo.addAppReport(reportText, app.getGalleryAppId(), offenderId,reporterId);
  }

  /**
  * gets recent reports
  * @param start start index
  * @param count number to retrieve
  * @return the list of reports
  */
  @Override
  public GalleryReportListResult getRecentReports(int start, int count) {
    return galleryStorageIo.getAppReports(start,count);

  }
  /**
  * gets existing reports
  * @param start start index
  * @param count number to retrieve
  * @return the list of reports
  */
  @Override
  public GalleryReportListResult getAllAppReports(int start, int count){
    return galleryStorageIo.getAllAppReports(start,count);
  }

  /**
   * check if an app is reprted by a user
   * @param galleryId the id of the app
   */
  @Override
  public boolean isReportedByUser(long galleryId) {
    final String userId = userInfoProvider.getUserId();
    return galleryStorageIo.isReportedByUser(galleryId, userId);
  }
  /**
   * save attribution for a gallery app
   * @param galleryId the id of the app
   * @param attributionId the id of the attribution app
   * @return num of like
   */
  @Override
  public long saveAttribution(long galleryId, long attributionId) {
    final String userId = userInfoProvider.getUserId();
    return galleryStorageIo.saveAttribution(galleryId, attributionId);
  }
  /**
   * get the attribution id for a gallery app
   * @param galleryId the id of the app
   * @return attribution id
   */
  @Override
  public long remixedFrom(long galleryId) {
    final String userId = userInfoProvider.getUserId();
    return galleryStorageIo.remixedFrom(galleryId);
  }
  /**
   * get the children ids of an app
   * @param galleryId the id of the app
   * @return list of children gallery app
   */
  @Override
  public List<GalleryApp> remixedTo(long galleryId) {
    return galleryStorageIo.remixedTo(galleryId);
  }
  /**
   * mark an report as resolved
   * @param reportId the id of the app
   */
  @Override
  public boolean markReportAsResolved(long reportId, long galleryId) {
    return galleryStorageIo.markReportAsResolved(reportId, galleryId);
  }

  /**
   * deactivate app
   * @param galleryId the id of the gallery app
   */
  @Override
  public boolean deactivateGalleryApp(long galleryId) {
    return galleryStorageIo.deactivateGalleryApp(galleryId);
  }
  /**
   * check if gallery app is Activated
   * @param galleryId the id of the gallery app
   */
  @Override
  public boolean isGalleryAppActivated(long galleryId){
    return galleryStorageIo.isGalleryAppActivated(galleryId);
  }

  /**
   * store aia file on cloud server
   * @param galleryId gallery id
   * @param projectId project id
   * @param projectName project name
   */
  private void storeAIA(long galleryId, long projectId, String projectName) throws IOException {

    final String userId = userInfoProvider.getUserId();
    // build the aia file name using the ai project name and code stolen
    // from DownloadServlet to normalize...
    String aiaName = StringUtils.normalizeForFilename(projectName) + ".aia";
    // grab the data for the aia file using code from DownloadServlet
    RawFile aiaFile = null;
    byte[] aiaBytes= null;
    ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(userId,
      projectId, true, false, aiaName, false, false, true);
    aiaFile = zipFile.getRawFile();
    aiaBytes = aiaFile.getContent();
    LOG.log(Level.INFO, "aiaFile numBytes:"+aiaBytes.length);
    // now stick the aia file into the gcs

    //String galleryKey = GalleryApp.getSourceKey(galleryId);//String.valueOf(galleryId);
    GallerySettings settings = loadGallerySettings();
    String galleryKey = settings.getSourceKey(galleryId);
    // setup cloud
    GcsService gcsService = GcsServiceFactory.createGcsService();

    //GcsFilename filename = new GcsFilename(GalleryApp.GALLERYBUCKET, galleryKey);
    GcsFilename filename = new GcsFilename(settings.getBucket(), galleryKey);

    GcsFileOptions options = new GcsFileOptions.Builder().mimeType("application/zip")
      .acl("public-read").cacheControl("no-cache").addUserMetadata("title", aiaName).build();
    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
    writeChannel.write(ByteBuffer.wrap(aiaBytes));

    // Now finalize
    writeChannel.close();
  }
  /**
   * delete aia file based on given gallery id
   * @param galleryId gallery id
   */
  private void deleteAIA(long galleryId) {
    try {
      //String galleryKey = GalleryApp.getSourceKey(galleryId);
      GallerySettings settings = loadGallerySettings();
      String galleryKey = settings.getSourceKey(galleryId);

      // setup cloud
      GcsService gcsService = GcsServiceFactory.createGcsService();
      //GcsFilename filename = new GcsFilename(GalleryApp.GALLERYBUCKET, galleryKey);
      GcsFilename filename = new GcsFilename(settings.getBucket(), galleryKey);
      gcsService.delete(filename);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.INFO, "FAILED GCS delete");
      e.printStackTrace();
    }
  }
  private void deleteImage(long galleryId) {
    try {
      //String galleryKey = GalleryApp.getImageKey(galleryId);
      GallerySettings settings = loadGallerySettings();
      String galleryKey = settings.getSourceKey(galleryId);
      // setup cloud
      GcsService gcsService = GcsServiceFactory.createGcsService();
      //GcsFilename filename = new GcsFilename(GalleryApp.GALLERYBUCKET, galleryKey);
      GcsFilename filename = new GcsFilename(settings.getBucket(), galleryKey);
      gcsService.delete(filename);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.INFO, "FAILED GCS delete");
      e.printStackTrace();
    }
  }

  /**
   * when an app is published/updated, we need to move the image
   * that was temporarily uploaded into projects/projectid/image
   * into the gallery image
   * @param app gallery app
   */
  private void setGalleryAppImage(GalleryApp app) {
    // best thing would be if GCS has a mv op, we can just do that.
    // don't think that is there, though, so for now read one and write to other
    // First, read the file from projects name
    boolean lockForRead = false;
    //String projectImageKey = app.getProjectImageKey();
    GallerySettings settings = loadGallerySettings();
    String projectImageKey = settings.getProjectImageKey(app.getProjectId());
    try {
      GcsService gcsService = GcsServiceFactory.createGcsService();
      //GcsFilename filename = new GcsFilename(GalleryApp.GALLERYBUCKET, projectImageKey);
      GcsFilename filename = new GcsFilename(settings.getBucket(), projectImageKey);
      GcsInputChannel readChannel = gcsService.openReadChannel(filename, 0);
      InputStream gcsis = Channels.newInputStream(readChannel);

      byte[] buffer = new byte[8000];
      int bytesRead = 0;
      ByteArrayOutputStream bao = new ByteArrayOutputStream();

      while ((bytesRead = gcsis.read(buffer)) != -1) {
        bao.write(buffer, 0, bytesRead);
      }
      // close the project image file
      readChannel.close();

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
      //String galleryKey = app.getImageKey();
      String galleryKey = settings.getImageKey(app.getGalleryAppId());

      //GcsFilename outfilename = new GcsFilename(GalleryApp.GALLERYBUCKET, galleryKey);
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


  /**
   * Send an email to user
   * @param senderId id of user sending this email
   * @param receiverId id of user receiving this email
   * @param receiverEmail receiver of email
   * @param title title of email
   * @param body body of email
   */
  @Override
  public long sendEmail(String senderId, String receiverId, String receiverEmail, String title, String body) {
    GallerySettings settings = loadGallerySettings();
    return galleryStorageIo.sendEmail(senderId, receiverId, settings.getAdminEmail(), receiverEmail, title, body);
  }

  /**
   * Get email based on emailId
   * @param emailId id of the email
   * @return Email email
   */
  @Override
  public Email getEmail(long emailId) {
    return galleryStorageIo.getEmail(emailId);
  }

  /**
   * check if ready to send app stats to user
   * @param userId
   * @param galleryId
   * @param adminEmail
   * @param currentHost
   */
  public boolean checkIfSendAppStats(String userId, long galleryId, String adminEmail, String currentHost){
    return galleryStorageIo.checkIfSendAppStats(userId, galleryId, adminEmail, currentHost);
  }

  /**
   * Store moderation actions based on actionType
   * @param reportId
   * @param galleryId
   * @param emailId
   * @param moderatorId
   * @param actionType
   */
  public void storeModerationAction(long reportId, long galleryId, long emailId, String moderatorId, int actionType, String moderatorName, String emailPreview){
    galleryStorageIo.storeModerationAction(reportId, galleryId, emailId, moderatorId, actionType, moderatorName, emailPreview);
  }

  /**
   * Get moderation actions based on given reportId
   * @param reportId
   */
  public List<GalleryModerationAction> getModerationActions(long reportId){
    return galleryStorageIo.getModerationActions(reportId);
  }

  /**
    * It will return a dev server serving url for given image url
    * @param url image url
    */
   @Override
   public String getBlobServingUrl(String url) {
     BlobKey bk = BlobstoreServiceFactory.getBlobstoreService().createGsBlobKey(url);
     String u = null;
     try {
         u = ImagesServiceFactory.getImagesService().getServingUrl(bk);
     } catch (Exception IllegalArgumentException) {
         LOG.info("Could not read blob");
     }
     return u;
   }

}
