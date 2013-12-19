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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
  private final FileExporter fileExporter = new FileExporterImpl();

  /**
   * Creates a new gallery app
   * UserProject is the AI Project
   * @param title gallery title of app
   * @param description description of app
   * NEED TO DEAL WITH IMAGE
   *
   * @return a {@link GalleryApp} for new galleryApp
   */
  @Override
  public long publishApp(long projectId, String title, String projectName, String description) {
    final String userId = userInfoProvider.getUserId();
    long galleryId = galleryStorageIo.createGalleryApp(title, projectName, description, projectId, userId);
    storeAIA(galleryId,projectId, projectName);
    return galleryId;
  }
   /**
   * Returns an array of gallery Apps
   *
   * @return gallery apps found by the back-end
   */
  @Override
  public List<GalleryApp> getRecentApps(int start,int count) {
    return galleryStorageIo.getRecentGalleryApps(start,count);
 
  }
  @Override
  public GalleryApp getApp(long galleryId) {
    return galleryStorageIo.getGalleryApp(galleryId);
  }

  @Override
  public List<GalleryApp> findApps(String keywords, int start, int count) {
    return null;
  }
  @Override
  public List<GalleryApp> getMostDownloadedApps(int start, int count) {
    return galleryStorageIo.getMostDownloadedApps(start,count);
  }
  @Override
  public void deleteApp(long galleryId) {
  }
  
  @Override
  public void appWasDownloaded(long galleryId, long projectId) {
    galleryStorageIo.incrementDownloads(galleryId);
  }

  private void storeImage(InputStream is, long galleryId) {
    
  }
  
  private void storeAIA(long galleryId, long projectId, String projectName) {
   
    final String userId = userInfoProvider.getUserId();
    // build the aia file name using the ai project name and code stolen
    // from DownloadServlet to normalize...
    String aiaName= StringUtils.normalizeForFilename(projectName) + ".aia";
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
      // convert galleryId to a string, we'll use this for the key in gcs
      String galleryKey = String.valueOf(galleryId);
      LOG.log(Level.SEVERE, "GALLERYKEY IS "+galleryKey);    
      // set up the cloud file (options)
      FileService fileService = FileServiceFactory.getFileService();
      GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
      .setBucket("galleryai2")
      .setKey(galleryKey)
      .setAcl("public-read")
      // what should the mime type be?
      .setMimeType("text/html")
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
  
  

}
