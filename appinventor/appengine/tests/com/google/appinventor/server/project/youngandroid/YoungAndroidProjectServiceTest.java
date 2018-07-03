// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.youngandroid;

import junit.framework.TestCase;
import org.junit.Test;

public class YoungAndroidProjectServiceTest extends TestCase {
	
  /* Testing the case of a null build URL */
  @Test
  public void testBuildErrorMsgDoesntThrowNPE() {
    YoungAndroidProjectService obj = new YoungAndroidProjectService(null);
    obj.buildErrorMsg("TestException", null, "userID", 0); 
  }
}
