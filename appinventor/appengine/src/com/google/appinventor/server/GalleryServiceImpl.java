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

import com.google.appinventor.server.project.CommonProjectService;
import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.GalleryStorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.FileDescriptor;
import com.google.appinventor.shared.rpc.project.FileDescriptorWithContent;
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
   * @param newProjectId id of newly created project
   */
  @Override
  public void appWasDownloaded(long galleryId, long projectId) {
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
    try {
      ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(userId,
            projectId, true, false, aiaName);
      aiaFile = zipFile.getRawFile();
    }
    catch (IOException e) {
      LOG.log(Level.INFO, "Unable to get aia file");
      e.printStackTrace();
    }
    // now stick the aia file into the gcs
    try {
    // NOTE: WE NEED TO UPDATE THIS AS ITS USING A TOBEDEPRECATED VERSION OF GCS
    //  see https://developers.google.com/appengine/docs/java/googlecloudstorageclient/migrate
    //   for migration details
      // convert galleryId to a string, we'll use this for the key in gcs
      String galleryKey = GalleryApp.getSourceKey(galleryId);//String.valueOf(galleryId);
      // set up the cloud file (options)
      FileService fileService = FileServiceFactory.getFileService();
      GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
      .setBucket(GalleryApp.GALLERYBUCKET)
      .setKey(galleryKey)
      .setAcl("public-read")
      // what should the mime type be?  it was .setMimeType("text/html")
      .setMimeType("application/zip")
      .setCacheControl("no-cache")
      // not sure if we're putting anything here for metadata
      .addUserMetadata("title", aiaName);
  
      AppEngineFile writableFile = fileService.createNewGSFile(optionsBuilder.build());
      // Open a channel to write to it
      boolean lock = true;
      FileWriteChannel writeChannel =
          fileService.openWriteChannel(writableFile, lock);
     
      byte[] aiaBytes = aiaFile.getContent();
      LOG.log(Level.INFO, "aiaFile numBytes:"+aiaBytes.length);
      writeChannel.write(ByteBuffer.wrap(aiaBytes));
    
      // Now finalize
      writeChannel.closeFinally();
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.INFO, "FAILED GCS");
      e.printStackTrace();
    }
  }

  private void deleteAIA(long galleryId) {
    try {
      FileService fileService = FileServiceFactory.getFileService();
      AppEngineFile file = new AppEngineFile(GalleryApp.getSourceURL(galleryId));
      // set up the cloud file (options)
      fileService.delete(file);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.INFO, "FAILED GCS delete");
      e.printStackTrace();
    }
  }
  private void deleteImage(long galleryId) {
    try {
      FileService fileService = FileServiceFactory.getFileService();
      AppEngineFile file = new AppEngineFile(GalleryApp.getImageURL(galleryId));
      // set up the cloud file (options)
      fileService.delete(file);
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
    String projectImagePath = app.getProjectImagePath();
    try {
      FileService fileService = FileServiceFactory.getFileService();
      AppEngineFile readableFile = new AppEngineFile(projectImagePath);
      FileReadChannel readChannel = fileService.openReadChannel(readableFile, false);
      LOG.log(Level.INFO, "#### in setGalleryAppImage, past readChannel");
      InputStream gcsis =Channels.newInputStream(readChannel);
      // ok, we don't want to send the gcs stream because it can time out as we
      // process the zip. We need to copy to a byte buffer first, then send a bytestream

      byte[] buffer = new byte[8000];
      int bytesRead = 0;
      ByteArrayOutputStream bao = new ByteArrayOutputStream();   
           
      while ((bytesRead = gcsis.read(buffer)) != -1) {
        bao.write(buffer, 0, bytesRead); 
      }
      // now we want to write to the gallery image
      //InputStream bais = new ByteArrayInputStream(bao.toByteArray());
      LOG.log(Level.INFO, "#### in newProjectFromGallery, past newInputStream");
      
      // close the project image file
      readChannel.close();

  
      String galleryKey = app.getImageKey();
      // set up the cloud file (options)

      GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
      .setBucket(GalleryApp.GALLERYBUCKET)
      .setKey(galleryKey)
      .setAcl("public-read")
      // what should the mime type be?  it was .setMimeType("text/html")
      .setMimeType("image/jpeg")
      .setCacheControl("no-cache");
      // not sure if we're putting anything here for metadata
  
      AppEngineFile writableFile = fileService.createNewGSFile(optionsBuilder.build());
      // Open a channel to write to it
      boolean lock = true;
      FileWriteChannel writeChannel =
          fileService.openWriteChannel(writableFile, lock);
     
     
      writeChannel.write(ByteBuffer.wrap(bao.toByteArray()));
    
      // Now finalize
      writeChannel.closeFinally();
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.INFO, "FAILED WRITING IMAGE TO GCS");
      e.printStackTrace();
    }
      

  }
}
