// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.blockseditor.jsonp;

import com.google.appinventor.common.testutils.TestUtils;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Map;


/**
 * Unit tests for {@link Util}.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class UtilTest extends TestCase {

  private URLConnection mockConn = EasyMock.createMock(URLConnection.class);
  private static final String TESTING_SOURCE_PATH = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/blockseditor/tests/com/google/appinventor/blockseditor/jsonp/testing_source_files/";


  public void testDownloadZipFile() throws FileNotFoundException, IOException {
    File zipFile = new File(TESTING_SOURCE_PATH + "zipfile.zip");
    InputStream zipInput = new FileInputStream(zipFile);
    assertNotNull(zipInput);
    
    EasyMock.expect(mockConn.getInputStream()).andReturn(zipInput);
    EasyMock.replay(mockConn);
    
    Map<String,String> zipFiles = Util.downloadZipFile(mockConn);
    // check that we got back the files we expect
    assertTrue(zipFiles.containsKey("assets/cat.wav"));
    assertTrue(zipFiles.containsKey("assets/kitty.jpg"));
    assertEquals(2, zipFiles.size());
    // check that we were able to give the unzipped files their proper names
    assertTrue(zipFiles.get("assets/cat.wav").contains("assets/cat.wav"));
    assertTrue(zipFiles.get("assets/kitty.jpg").contains("assets/kitty.jpg"));
    // check that we extracted the file contents that we expect
    assertTrue(filesEqual(TESTING_SOURCE_PATH + "assets/cat.wav", zipFiles.get("assets/cat.wav")));
    assertTrue(filesEqual(TESTING_SOURCE_PATH + "assets/kitty.jpg", 
        zipFiles.get("assets/kitty.jpg")));
    EasyMock.verify(mockConn);
  }
  
  private String basename(String path) {
    return path.substring(path.lastIndexOf('/') + 1);
  }
  
  private boolean filesEqual(String filePath1, String filePath2) throws IOException {
    File file1 = new File(filePath1);
    File file2 = new File(filePath2);
    InputStream inp1 = new FileInputStream(file1);
    InputStream inp2 = new FileInputStream(file2);
    int b1;
    int b2;
    while ((b1 = inp1.read()) == (b2 = inp2.read()) && b1 != -1);
    if (b1 != -1 || b2 != -1) {
      return false;
    }
    return true;
  }
}
