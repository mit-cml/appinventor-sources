// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.youngandroid;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.junit.Test;

public class YoungAndroidProjectServiceTest extends TestCase {

  private static final String SCREEN1 = "src/appinventor/ai_test/TestProject/Screen1";
  private static final String SCREEN2 = "src/appinventor/ai_test/TestProject/Screen2";

  /* Testing the case of a null build URL */
  @Test
  public void testBuildErrorMsgDoesntThrowNPE() {
    YoungAndroidProjectService obj = new YoungAndroidProjectService(null);
    obj.buildErrorMsg("TestException", null, "userID", 0);
  }

  @Test
  public void testFindVestigialYailFilesFlagsYailWithoutScmOrBky() {
    List<String> files = Arrays.asList(
        "youngandroidproject/project.properties",
        SCREEN1 + ".scm", SCREEN1 + ".bky", SCREEN1 + ".yail",
        SCREEN2 + ".yail");
    assertEquals(Collections.singletonList(SCREEN2 + ".yail"),
        YoungAndroidProjectService.findVestigialYailFiles(files));
  }

  @Test
  public void testFindVestigialYailFilesKeepsCompleteScreens() {
    List<String> files = Arrays.asList(
        "youngandroidproject/project.properties",
        SCREEN1 + ".scm", SCREEN1 + ".bky", SCREEN1 + ".yail",
        SCREEN2 + ".scm", SCREEN2 + ".bky", SCREEN2 + ".yail");
    assertTrue(YoungAndroidProjectService.findVestigialYailFiles(files).isEmpty());
  }

  @Test
  public void testFindVestigialYailFilesKeepsYailWithOnlyScm() {
    List<String> files = Arrays.asList(SCREEN1 + ".scm", SCREEN1 + ".yail");
    assertTrue(YoungAndroidProjectService.findVestigialYailFiles(files).isEmpty());
  }

  @Test
  public void testFindVestigialYailFilesKeepsYailWithOnlyBky() {
    List<String> files = Arrays.asList(SCREEN1 + ".bky", SCREEN1 + ".yail");
    assertTrue(YoungAndroidProjectService.findVestigialYailFiles(files).isEmpty());
  }

  @Test
  public void testFindVestigialYailFilesIgnoresFilesOutsideSrc() {
    List<String> files = Arrays.asList("assets/external_comps/comp/comp.yail");
    assertTrue(YoungAndroidProjectService.findVestigialYailFiles(files).isEmpty());
  }

  @Test
  public void testFindVestigialYailFilesEmptyProject() {
    assertTrue(YoungAndroidProjectService.findVestigialYailFiles(
        Collections.<String>emptyList()).isEmpty());
  }
}
