// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.utils;

import com.google.common.collect.Lists;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * Tests for {@link JavaWebStart}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class JavaWebStartTest extends TestCase {
  private IMocksControl control;
  private HttpServletRequest req;
  private UserInfoProvider userProvider;

  @Override
  protected void setUp() throws Exception {
    control = EasyMock.createControl();
    req = control.createMock(HttpServletRequest.class);
    EasyMock.expect(req.getScheme()).andReturn("http").anyTimes();
    EasyMock.expect(req.getServerName()).andReturn("ode.google.com").anyTimes();
    EasyMock.expect(req.getServerPort()).andReturn(80).anyTimes();
    userProvider = control.createMock(UserInfoProvider.class);
    EasyMock.expect(userProvider.getUserId()).andReturn("100").anyTimes();
    control.replay();
  }

  public void testGetWebStartBaseUrl() throws Exception {
    assertEquals("http://ode.google.com:80/ode2/webstartfile/",
        JavaWebStart.getWebStartBaseUrl(req));
  }

  public void testGenerateJnlpFile() throws Exception {
    List<String> jarFiles = Lists.newArrayList();
    jarFiles.add("SomeProgram_deploy.jar");
    List<String> args = Lists.newArrayList();
    args.add("one");
    args.add("two");
    args.add("three");
    String jnlp = JavaWebStart.generateJnlpFile("http://www.somedomain.com:80/webstart/",
        "Some Title", "Some Description", "1.6+", "1024m", "com.google.devtools.ode.SomeProgram",
        jarFiles, args);
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<jnlp spec=\"1.0+\" codebase=\"http://www.somedomain.com:80/webstart/\">\n" +
        "   <information>\n" +
        "      <title>Some Title</title>\n" +
        "      <vendor>MIT Center for Mobile Learning</vendor>\n" +
        "      <description>Some Description</description>\n" +
        "   </information>\n" +
        "   <security>\n" +
        "      <all-permissions/>\n" +
        "   </security>\n" +
        "   <update check=\"always\" policy=\"always\"/>\n" +
        "   <resources>\n" +
        "      <j2se version=\"1.6+\" max-heap-size=\"1024m\" java-vm-args=\"\"/>\n" +
        "      <jar href=\"SomeProgram_deploy.jar\"/>\n" +
        "   </resources>\n" +
        "   <application-desc main-class=\"com.google.devtools.ode.SomeProgram\">\n" +
        "      <argument>one</argument>\n" +
        "      <argument>two</argument>\n" +
        "      <argument>three</argument>\n" +
        "   </application-desc>\n" +
        "</jnlp>\n", jnlp);
  }
}
