// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions.Builder;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;

import com.google.appinventor.server.project.CommonProjectService;
import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.GalleryStorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.FileDescriptor;
import com.google.appinventor.shared.rpc.project.FileDescriptorWithContent;
import com.google.appinventor.shared.rpc.project.Message;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectService;
import com.google.appinventor.shared.rpc.project.GalleryService;

import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.common.collect.Lists;

import com.google.appinventor.server.storage.GalleryStorageIo;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryComment;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.common.utils.StringUtils;

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

  /**
   * Publishes a gallery app
   * @param projectId id of the project being published
   * @param projectName name of project
   * @param title title of new gallery app
   * @param description description of new gallery app
   * @return a {@link GalleryApp} for new galleryApp
   */
  @Override
  public GalleryApp publishApp(long projectId, String title, String projectName, String description) {
    final String userId = userInfoProvider.getUserId();
    GalleryApp app = galleryStorageIo.createGalleryApp(title, projectName, description, projectId, userId);
    storeAIA(app.getGalleryAppId(),projectId, projectName);
    // see if there is a new image for the app. If so, its in cloud using projectId, need to move
    // to cloud using gallery id

//    Logger.getAnonymousLogger().info("#######################");
//    Logger.getAnonymousLogger().info("This is URL for /gallery/app/#/aia:" + String.valueOf(app.getGalleryAppId()));
//    Logger.getAnonymousLogger().info("This is URL for /gallery/project/#/image:" + String.valueOf(projectId));
//    Logger.getAnonymousLogger().info("#######################");
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
  public void updateApp(GalleryApp app, boolean newImage) {
    updateAppMetadata(app);
    updateAppSource(app.getGalleryAppId(),app.getProjectId(),app.getProjectName());
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
    galleryStorageIo.updateGalleryApp(app.getGalleryAppId(), app.getTitle(), app.getDescription(),  userId);
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
  public void updateAppSource (long galleryId, long projectId, String projectName) {
     storeAIA(galleryId,projectId, projectName);
  }

  /**
   * index all gallery apps (admin method)
   * @param count the max number of apps to index
   */
  @Override
  public void indexAll(int count) {
    List<GalleryApp> apps= getRecentApps(1,count);
    for (GalleryApp app:apps) {
      GallerySearchIndex.getInstance().indexApp(app);
    }
  }


  /**
   * Returns a list of most recently updated galleryApps
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  @Override
  public List<GalleryApp> getRecentApps(int start,int count) {
    return galleryStorageIo.getRecentGalleryApps(start,count);
 
  }

  /**
   * Returns a list of galleryApps by a particular developer
   * @param userId id of the developer
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  @Override
  public List<GalleryApp> getDeveloperApps(String userId, int start,int count) {
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
   * Returns a list of galleryApps
   * @param keywords keywords to search for
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  @Override
  public List<GalleryApp> findApps(String keywords, int start, int count) {
    
    return GallerySearchIndex.getInstance().find(keywords);
  }

  /**
   * Returns a list of most downloaded gallery apps
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  @Override
  public List<GalleryApp> getMostDownloadedApps(int start, int count) {
    return galleryStorageIo.getMostDownloadedApps(start,count);
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
    // change its associated AI project so that its galleryId is reset to -1

  }
  /**
   * record fact that app was downloaded
   * @param galleryId id of app that was downloaded
   */
  @Override
  public void appWasDownloaded(long galleryId) {
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
   * adds a report (flag) to a gallery app
   * @param galleryId id of gallery app that was commented on
   * @param report report
   * @return the id of the new report
   */
  @Override
  public long addAppReport(long galleryId, String report) {
    final String userId = userInfoProvider.getUserId();
    return galleryStorageIo.addAppReport(galleryId, userId, report);
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


//  public void storeImage(InputStream is, long galleryId) {
//    
//  }
  
  private void storeAIA(long galleryId, long projectId, String projectName) {
   
    final String userId = userInfoProvider.getUserId();
    // build the aia file name using the ai project name and code stolen
    // from DownloadServlet to normalize...
    String aiaName = StringUtils.normalizeForFilename(projectName) + ".aia";
    // grab the data for the aia file using code from DownloadServlet
    RawFile aiaFile = null;
    byte[] aiaBytes= null;
    try {
      ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(userId,
            projectId, true, false, aiaName);
      aiaFile = zipFile.getRawFile();
      aiaBytes = aiaFile.getContent();
      LOG.log(Level.INFO, "aiaFile numBytes:"+aiaBytes.length);
    }
    catch (IOException e) {
      LOG.log(Level.INFO, "Unable to get aia file");
      e.printStackTrace();
    }
    // now stick the aia file into the gcs
    try {
      String galleryKey = GalleryApp.getSourceKey(galleryId);//String.valueOf(galleryId);

      // setup cloud
      GcsService gcsService = GcsServiceFactory.createGcsService();
      GcsFilename filename = new GcsFilename(GalleryApp.GALLERYBUCKET, galleryKey);
      GcsFileOptions options = new GcsFileOptions.Builder().mimeType("application/zip")
          .acl("public-read").cacheControl("no-cache").addUserMetadata("title", aiaName).build();
      GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
      writeChannel.write(ByteBuffer.wrap(aiaBytes));
    
      // Now finalize
      writeChannel.close();
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.INFO, "FAILED GCS");
      e.printStackTrace();
    }
  }

  private void deleteAIA(long galleryId) {
    try {
      String galleryKey = GalleryApp.getSourceKey(galleryId);
      // setup cloud
      GcsService gcsService = GcsServiceFactory.createGcsService();
      GcsFilename filename = new GcsFilename(GalleryApp.GALLERYBUCKET, galleryKey);
      gcsService.delete(filename);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.INFO, "FAILED GCS delete");
      e.printStackTrace();
    }
  }
  private void deleteImage(long galleryId) {
    try {
      String galleryKey = GalleryApp.getImageKey(galleryId);
      // setup cloud
      GcsService gcsService = GcsServiceFactory.createGcsService();
      GcsFilename filename = new GcsFilename(GalleryApp.GALLERYBUCKET, galleryKey);
      gcsService.delete(filename);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.INFO, "FAILED GCS delete");
      e.printStackTrace();
    }
  }
  /* when an app is published/updated, we need to move the image
   * that was temporarily uploaded into projects/projectid/image
   * into the gallery image
   */
  private void setGalleryAppImage(GalleryApp app) {
    // best thing would be if GCS has a mv op, we can just do that.
    // don't think that is there, though, so for now read one and write to other
    // First, read the file from projects name
    boolean lockForRead = false;
    String projectImageKey = app.getProjectImageKey();
    try {
      GcsService gcsService = GcsServiceFactory.createGcsService();
      GcsFilename filename = new GcsFilename(GalleryApp.GALLERYBUCKET, projectImageKey);
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

      // set up the cloud file (options)
      // After publish, copy the /projects/projectId image into /apps/appId
      String galleryKey = app.getImageKey();
      GcsFilename outfilename = new GcsFilename(GalleryApp.GALLERYBUCKET, galleryKey);
      GcsFileOptions options = new GcsFileOptions.Builder().mimeType("image/jpeg")
          .acl("public-read").cacheControl("no-cache").build();
      GcsOutputChannel writeChannel = gcsService.createOrReplace(outfilename, options);
      writeChannel.write(ByteBuffer.wrap(bao.toByteArray()));
    
      // Now finalize
      writeChannel.close();
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.INFO, "FAILED WRITING IMAGE TO GCS");
      e.printStackTrace();
    }
  }


  /**
   * Send a system message to user
   * @param senderId id of user sending this message
   * @param receiverId id of user receiving this message
   * @param message body of message
   */
  @Override
  public void sendMessageFromSystem(String senderId, String receiverId, String message) {
    LOG.info("### SEND MSG FROM SYSTEM");
//    final String userId = userInfoProvider.getUserId(); // gets current userId
    galleryStorageIo.sendMessage(senderId, receiverId, message);
  }

  /**
   * Send a system message to user
   * @param senderId    id of user sending this message
   * @param receiverId    id of user receiving this message
   * @param message   body of message
   */
  @Override
  public List<Message> getMessages(String receiverId) {
//    LOG.info("### GET MSGS");
//    final String userId = userInfoProvider.getUserId(); // gets current userId
    return galleryStorageIo.getMessages(receiverId);
  }

  /**
   * Tell the system to mark a specific message as read
   * @param timestamp   the timestamp serves as the key to identify message
   */
  @Override
  public void readMessage(long timestamp) {
//    LOG.info("### READ MSGS");
//    final String userId = userInfoProvider.getUserId(); // gets current userId
    galleryStorageIo.readMessage(timestamp);
  }

  /**
   * Tell the system to mark a specific app's stats as read
   * @param timestamp   the timestamp serves as the key to identify message
   */
  @Override
  public void appStatsWasRead(long appId) {
//    LOG.info("### READ MSGS");
//    final String userId = userInfoProvider.getUserId(); // gets current userId
    galleryStorageIo.appStatsWasRead(appId);
  }
}
