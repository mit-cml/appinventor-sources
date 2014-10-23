// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.youngandroid;

import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import com.google.appinventor.server.storage.StorageIo;
import junit.framework.TestCase;
import junitx.framework.ListAssert;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class YoungAndroidWebStartSupportTest extends TestCase {

  private IMocksControl control;
  private StorageIo storageIo;
  private UserInfoProvider userProvider;

  YoungAndroidWebStartSupport yaWebStart;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    control = EasyMock.createControl();
    storageIo = control.createMock(StorageIo.class);
    userProvider = control.createMock(UserInfoProvider.class);
    yaWebStart = new YoungAndroidWebStartSupport(storageIo, userProvider);
  }

  public void testGetAssetsZipFileSuccess() throws Exception {
    String userId = "1";
    long projectId = 2;
    byte[] data = {0, 0, 0};
    List<String> sourceFiles = new ArrayList<String>();
    sourceFiles.add("assets/kitty.png");
    sourceFiles.add("assets/meow.mp3");
    sourceFiles.add("src/appinventor/ai_user/HelloPurr/Screen1.blk");
    sourceFiles.add("src/appinventor/ai_user/HelloPurr/Screen1.scm");
    sourceFiles.add("youngandroidproject/project.properties");

    EasyMock.expect(storageIo.getProjectSourceFiles(userId, projectId))
      .andReturn(sourceFiles);
    EasyMock.expect(storageIo.downloadRawFile(EasyMock.eq(userId), EasyMock.eq(projectId),
        EasyMock.eq("assets/kitty.png"))).andReturn(data);
    EasyMock.expect(storageIo.downloadRawFile(EasyMock.eq(userId), EasyMock.eq(projectId),
        EasyMock.eq("assets/meow.mp3"))).andReturn(data);

    control.replay();

    List<String> zipEntries = getZipEntries(yaWebStart.getAssetsZipFile(userId, projectId));
    assert(zipEntries.contains("assets/meow.mp3"));
    assert(zipEntries.contains("assets/kitty.png"));
  }


  public void testGetAssetsZipFileBadAssets() throws Exception {
    String userId = "1";
    long projectId = 2;
    byte[] data = {0};
    List<String> sourceFiles = new ArrayList<String>();
    sourceFiles.add("assets/badkitty.png");
    sourceFiles.add("assets/meow.mp3");
    sourceFiles.add("src/appinventor/ai_user/HelloPurr/Screen1.blk");
    sourceFiles.add("src/appinventor/ai_user/HelloPurr/Screen1.scm");
    sourceFiles.add("youngandroidproject/project.properties");

    EasyMock.expect(storageIo.getProjectSourceFiles(userId, projectId))
      .andReturn(sourceFiles);
    EasyMock.expect(storageIo.downloadRawFile(EasyMock.eq(userId), EasyMock.eq(projectId),
        EasyMock.eq("assets/badkitty.png"))).andThrow(new RuntimeException());
    EasyMock.expect(storageIo.downloadRawFile(EasyMock.eq(userId), EasyMock.eq(projectId),
        EasyMock.eq("assets/meow.mp3"))).andReturn(data);

    control.replay();

    List<String> zipEntries = getZipEntries(yaWebStart.getAssetsZipFile(userId, projectId));
    ListAssert.assertContains(zipEntries, "assets/meow.mp3");
    ListAssert.assertContains(zipEntries, "badassets/badkitty.png");
  }

  private List<String> getZipEntries(byte[] zipContents) throws IOException {
    ByteArrayInputStream bs = new ByteArrayInputStream(zipContents);
    ZipInputStream zin = new ZipInputStream(bs);
    ZipEntry entry;
    List<String> entries = new ArrayList<String>();
    while ((entry = zin.getNextEntry()) != null) {
      entries.add(entry.getName());
    }
    return entries;
  }

}
