// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2023-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.common.testutils.TestUtils;
import java.io.File;
import org.junit.Test;

public class ExtractProvisioningPlistTest {

  @Test
  public void testSha1AdHoc() {
    IosCompilerContext context = new IosCompilerContext() {
      @Override
      public String getExt() {
        return "ipa";
      }
    };
    File appDir = new File(TestUtils.APP_INVENTOR_ROOT_DIR + "/buildserver/build/tests/");
    if (!appDir.exists()) {
      assertTrue(appDir.mkdirs());
    }
    context.getPaths().setAssetsDir(new File(TestUtils.APP_INVENTOR_ROOT_DIR + "/buildserver/tests/com/google/appinventor/buildserver/tasks/ios/"));
    context.getPaths().setAppDir(appDir);
    ExtractProvisioningPlist task = new ExtractProvisioningPlist();
    TaskResult result = task.execute(context);
    assertTrue(result.isSuccess());
    String sha1 = context.getCertificateSignature();
    assertEquals("728165C584BBCF2A62A1BD2600C7E4AC4A45EC09", sha1);
  }

  @Test
  public void testSha1AppStore() {
    IosCompilerContext context = new IosCompilerContext() {
      @Override
      public String getExt() {
        return "ips";
      }
    };
    File appDir = new File(TestUtils.APP_INVENTOR_ROOT_DIR + "/buildserver/build/tests/");
    if (!appDir.exists()) {
      assertTrue(appDir.mkdirs());
    }
    context.getPaths().setAssetsDir(new File(TestUtils.APP_INVENTOR_ROOT_DIR + "/buildserver/tests/com/google/appinventor/buildserver/tasks/ios/"));
    context.getPaths().setAppDir(appDir);
    ExtractProvisioningPlist task = new ExtractProvisioningPlist();
    TaskResult result = task.execute(context);
    assertTrue(result.isSuccess());
    String sha1 = context.getCertificateSignature();
    assertEquals("728165C584BBCF2A62A1BD2600C7E4AC4A45EC09", sha1);
  }
}
