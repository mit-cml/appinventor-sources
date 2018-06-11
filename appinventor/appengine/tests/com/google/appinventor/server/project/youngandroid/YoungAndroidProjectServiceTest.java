// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.youngandroid;

import junit.framework.TestCase;
import org.junit.Test;

/* Testing the case of a null build URL */
public class YoungAndroidProjectServiceTest extends TestCase {
  @Test
  public void testBuildErrorMsgDoesntThrowNPE() {
  	YoungAndroidProjectService obj = new YoungAndroidProjectService(null); 
    obj.buildErrorMsg("TestException", null, "userID", 0); 
  }
}
