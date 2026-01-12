// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import com.google.appinventor.buildserver.context.Resources;
import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.components.runtime.Clock;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import kawa.standard.Scheme;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

@PowerMockIgnore({"gnu.*", "kawa.*"})
@PrepareForTest({Clock.class})
@RunWith(PowerMockRunner.class)
public class YailAndroidTest {
  private Scheme scheme;

  private static final String YAIL_SCHEME_TESTS =
      TestUtils.windowsToUnix(TestUtils.APP_INVENTOR_ROOT_DIR) +
      "/buildserver/tests/com/google/appinventor/buildserver/YailEvalTest.scm";

  @Before
  public void setUp() throws Exception {
    scheme = new Scheme();
    String yailRuntimeLibrary = new Resources().getResource(Resources.YAIL_RUNTIME);
    yailRuntimeLibrary = TestUtils.windowsToUnix(yailRuntimeLibrary);
    try {
      scheme.eval("(load \"" + yailRuntimeLibrary + "\")");
      scheme.eval("(load \"" + YAIL_SCHEME_TESTS + "\")");
      scheme.eval("(set! *testing* #t)");
    } catch (Exception e) {
      throw e;
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
  }

  @Test
  public void testFormatDateTime() throws Throwable {
    // Unfortunately, call-component-method requires an actual component, even though
    // FormatDateTime method is static. This means we have to mock out a Clock component so that
    // method lookup can be performed by Kawa.
    Clock c = PowerMock.createMock(Clock.class);
    scheme.define("*test-clock*", c);
    String output = scheme.eval("(begin (add-to-current-form-environment 'Clock1 *test-clock*)"
        + "(call-component-method 'Clock1 'FormatDateTime"
        + " (*list-for-runtime* 0  \"MM/dd/yyyy hh:mm:ss a\") '(InstantInTime text)))")
        .toString();
    Calendar expected = Calendar.getInstance();
    expected.setTimeInMillis(0);
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
    assertEquals(sdf.format(expected.getTime()), output);
  }
}
