// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.blockseditor.youngandroid;

import com.google.appinventor.blockseditor.jsonp.AsyncJsonpRequestHandler;
import com.google.appinventor.blockseditor.jsonp.HttpServer;
import com.google.appinventor.common.youngandroid.YaHttpServerConstants;
import com.google.common.collect.Maps;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;

import openblocks.yacodeblocks.FeedbackReporter;
import openblocks.yacodeblocks.LoadException;
import openblocks.yacodeblocks.SaveException;
import openblocks.yacodeblocks.WorkspaceController;
import openblocks.yacodeblocks.YailGenerationException;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Test {@link YaHttpServerMain}.
 *
 * <p> {@code YaHttpServerMain} implements the handlers for an http server that
 * runs on the client's machine and communicates with the App Inventor code
 * running in the client's browser. It also communicates with the App Inventor
 * server running in Google's cloud. We test it here by mocking the various
 * components that {@code YaHttpServerMain} interacts with, as follows:
 * <dl>
 * <dt>blocks editor
 * <dd>The {@link WorkspaceController} class (the interface to
 * the blocks editor) is mocked with {@link EasyMock}
 * <dt>App Inventor server
 * <dd>We create a mock version (hand-coded) of the class
 * that produces {@link HttpURLConnection} given URL paths, used by {@code
 * YaHttpServerMain}. The mocked class returns a mock {@link HttpURLConnection}
 * object (created via {@code EasyMock}). Although {@code YaHttpServerMain}
 * normally creates a new {@code HttpURLConnection} each time it accesses the
 * App Inventor server, we return the same static object each time, so that we
 * can have a handle on it to check that the expected calls on the connection
 * are occurring. The one drawback to the current scheme is that we can't check
 * the association between the request (POST vs. GET) and the URL. We just check
 * the set of requests and the set of URLs.
 * <dt>client browser
 * <dd>We use the {@link HttpServer#getHandler(String)} method
 * on the underlying HttpServer to get the set of handlers registered by {@code
 * YaHttpServerMain}, and then invoke those directly in the tests
 * </dl>
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class YaHttpServerMainTest extends TestCase {

  private HttpServer server;
  private WorkspaceController codeblocksMock;
  private YaHttpServerMain.ServerConnection mockServerConn;
  private HttpURLConnection mockURLconn;
  private Map<String, AsyncJsonpRequestHandler> handlers = Maps.newHashMap();
  private Map<String, String> parameters;
  private static final String baseUrl = "/yatest/";
  private static final String savePortUrl = "/yatest/saveport";
  private static final String formPath = "test.scm";
  private static final String yailPath = "test.yail";
  private static final String codeblocksSourcePath = "test.blk";
  private static final String assetsPath = "assets_file.zip";
  private static final String projectName = "test";
  private static final Map<String, String> assetFiles = Maps.newHashMap();
  private static final String formPropsString = "#||##|\n$JSON\n{\"Source\":\"Form\"}\n|#\n";
  private static final byte[] formProps = formPropsString.getBytes();
  private static final String blocksSourceString = "some non-empty string\n";
  private static final byte[] blocksSource = blocksSourceString.getBytes();

  public YaHttpServerMainTest() {
    FeedbackReporter.testingMode = true;
    server = new HttpServer();
    mockServerConn = EasyMock.createMock(YaHttpServerMain.ServerConnection.class);
    mockURLconn = EasyMock.createMock(HttpURLConnection.class);
    codeblocksMock = EasyMock.createMock(WorkspaceController.class);
    YaHttpServerMain main =
        new YaHttpServerMain(server, baseUrl, savePortUrl, codeblocksMock, mockServerConn);
    handlers.put(YaHttpServerConstants.GENERATE_YAIL,
        (AsyncJsonpRequestHandler) server.getHandler(YaHttpServerConstants.GENERATE_YAIL));
    handlers.put(YaHttpServerConstants.LOAD_FORM,
        (AsyncJsonpRequestHandler) server.getHandler(YaHttpServerConstants.LOAD_FORM));
    handlers.put(YaHttpServerConstants.RELOAD_PROPERTIES,
        (AsyncJsonpRequestHandler) server.getHandler(YaHttpServerConstants.RELOAD_PROPERTIES));
    handlers.put(YaHttpServerConstants.CLEAR_CODEBLOCKS,
        (AsyncJsonpRequestHandler) server.getHandler(YaHttpServerConstants.CLEAR_CODEBLOCKS));
    handlers.put(YaHttpServerConstants.SAVE_CODEBLOCKS_SOURCE,
        (AsyncJsonpRequestHandler) server.getHandler(YaHttpServerConstants.SAVE_CODEBLOCKS_SOURCE));
    handlers.put(YaHttpServerConstants.SYNC_PROPERTY,
        (AsyncJsonpRequestHandler) server.getHandler(YaHttpServerConstants.SYNC_PROPERTY));
  }

  @Override
  protected void setUp() throws Exception {
    FeedbackReporter.testingMode = true;
    parameters = Maps.newHashMap();
  }

  public void testGenerateYail_success() throws Throwable {
    // This method tests GENERATE_YAIL, but first we have to load the form.
    expectLoadFormSuccess();

    // generate the yail code in blocks editor
    expect(codeblocksMock.wrapProjectYailForAPK(EasyMock.matches(yailPath)))
      .andReturn("non-empty string").once();

    EasyMock.replay(codeblocksMock, mockURLconn, mockServerConn);

    assertTrue(invokeLoadForm());

    // test by invoking the YaHttpServerMain handler for GENERATE_YAIL
    assertTrue(Boolean.parseBoolean(handlers.get(YaHttpServerConstants.GENERATE_YAIL)
        .getResponseValueAsync(parameters)));

    EasyMock.verify(codeblocksMock, mockURLconn, mockServerConn);
  }

  // GENERATE_YAIL failure case (getYail throws an exception).
  public void testGenerateYail_failure() throws Throwable {
    // This method tests GENERATE_YAIL, but first we have to load the form.
    expectLoadFormSuccess();

    // Record expected behavior of blocks editor
    expect(codeblocksMock.wrapProjectYailForAPK(EasyMock.matches(yailPath)))
      .andThrow(new YailGenerationException("yail generation failed")).once();

    EasyMock.replay(codeblocksMock, mockURLconn, mockServerConn);

    assertTrue(invokeLoadForm());

    // test by invoking the YaHttpServerMain handler for GENERATE_YAIL
    assertFalse(Boolean.parseBoolean(handlers.get(YaHttpServerConstants.GENERATE_YAIL)
        .getResponseValueAsync(parameters)));

    EasyMock.verify(codeblocksMock, mockURLconn, mockServerConn);
  }

  private void expectLoadFormSuccess() throws Throwable {
    // Record expected behavior at blocks editor and App Inventor server interfaces.

    // read the blocks source from server
    expect(mockServerConn.getConnection(baseUrl + codeblocksSourcePath)).andReturn(mockURLconn);
    expect(mockURLconn.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK);
    expect(mockURLconn.getInputStream()).andReturn(new ByteArrayInputStream(blocksSource));
    expect(mockURLconn.getContentEncoding()).andReturn(null);

    // read the form properties from server
    expect(mockServerConn.getConnection(baseUrl + formPath)).andReturn(mockURLconn);
    expect(mockURLconn.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK);
    expect(mockURLconn.getInputStream()).andReturn(new ByteArrayInputStream(formProps));
    expect(mockURLconn.getContentEncoding()).andReturn(null);

    // read the assets from the server
    expect(mockServerConn.getConnection(baseUrl + assetsPath)).andReturn(mockURLconn);
    expect(mockURLconn.getResponseCode()).andReturn(HttpURLConnection.HTTP_NO_CONTENT);

    codeblocksMock.loadSourceAndProperties(codeblocksSourcePath, blocksSourceString,
        formPropsString, assetFiles, projectName);
  }

  private boolean invokeLoadForm() throws Throwable {
    // test by invoking the YaHttpServerMain handler for LOAD_FORM
    parameters.put(YaHttpServerConstants.FORM_PROPERTIES_PATH, formPath);
    parameters.put(YaHttpServerConstants.PROJECT_NAME, projectName);
    parameters.put(YaHttpServerConstants.ASSET_PATH, assetsPath);
    boolean result = Boolean.parseBoolean(handlers.get(YaHttpServerConstants.LOAD_FORM)
        .getResponseValueAsync(parameters));
    parameters.clear();
    return result;
  }

  // LOAD_FORM success case where blocks source is non-empty
  public void testLoadFormNonEmpty_success() throws Throwable {
    expectLoadFormSuccess();

    EasyMock.replay(codeblocksMock, mockURLconn, mockServerConn);

    assertTrue(invokeLoadForm());

    EasyMock.verify(codeblocksMock, mockURLconn, mockServerConn);
  }

  // LOAD_FORM success case where blocks source is empty
  public void testLoadFormEmpty_success() throws Throwable {
    // read the blocks source from server
    expect(mockServerConn.getConnection(baseUrl + codeblocksSourcePath)).andReturn(mockURLconn);
    expect(mockURLconn.getResponseCode()).andReturn(HttpURLConnection.HTTP_NO_CONTENT);
    expect(mockURLconn.getInputStream()).andReturn(new ByteArrayInputStream(new byte[0]));
    expect(mockURLconn.getContentEncoding()).andReturn(null);

    // read the form properties from server
    expect(mockServerConn.getConnection(baseUrl + formPath)).andReturn(mockURLconn);
    expect(mockURLconn.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK);
    expect(mockURLconn.getInputStream()).andReturn(new ByteArrayInputStream(formProps));
    expect(mockURLconn.getContentEncoding()).andReturn(null);

    // read the assets from the server
    expect(mockServerConn.getConnection(baseUrl + assetsPath)).andReturn(mockURLconn);
    expect(mockURLconn.getResponseCode()).andReturn(HttpURLConnection.HTTP_NO_CONTENT);

    codeblocksMock.loadSourceAndProperties(codeblocksSourcePath, "",
        formPropsString, assetFiles, projectName);

    EasyMock.replay(codeblocksMock, mockURLconn, mockServerConn);

    assertTrue(invokeLoadForm());

    EasyMock.verify(codeblocksMock, mockURLconn, mockServerConn);
  }

  public void testLoadFormCodeblocks_failure() throws Throwable {
    // read the blocks source from server
    expect(mockServerConn.getConnection(baseUrl + codeblocksSourcePath)).andReturn(mockURLconn);
    expect(mockURLconn.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK);
    expect(mockURLconn.getInputStream()).andReturn(new ByteArrayInputStream(blocksSource));
    expect(mockURLconn.getContentEncoding()).andReturn(null);

    // read the form properties from server
    expect(mockServerConn.getConnection(baseUrl + formPath)).andReturn(mockURLconn);
    expect(mockURLconn.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK);
    expect(mockURLconn.getInputStream()).andReturn(new ByteArrayInputStream(formProps));
    expect(mockURLconn.getContentEncoding()).andReturn(null);

    // read the assets from the server
    expect(mockServerConn.getConnection(baseUrl + assetsPath)).andReturn(mockURLconn);
    expect(mockURLconn.getResponseCode()).andReturn(HttpURLConnection.HTTP_NO_CONTENT);

    codeblocksMock.loadSourceAndProperties(codeblocksSourcePath, blocksSourceString,
        formPropsString, assetFiles, projectName);
    EasyMock.expectLastCall().andThrow(new LoadException("load failed"));

    EasyMock.replay(codeblocksMock, mockURLconn, mockServerConn);

    assertFalse(invokeLoadForm());

    EasyMock.verify(codeblocksMock, mockURLconn, mockServerConn);
  }

  public void testReloadProperties_success() throws Throwable {
    // This method tests RELOAD_PROEPRTIES, but first we have to load the form.
    expectLoadFormSuccess();

    // read the form properties from server
    expect(mockServerConn.getConnection(baseUrl + formPath)).andReturn(mockURLconn);
    expect(mockURLconn.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK);
    expect(mockURLconn.getInputStream()).andReturn(new ByteArrayInputStream(formProps));
    expect(mockURLconn.getContentEncoding()).andReturn(null);

    codeblocksMock.loadProperties(formPropsString);
    EasyMock.replay(codeblocksMock, mockURLconn, mockServerConn);

    assertTrue(invokeLoadForm());

    // test by invoking the YaHttpServerMain handler for RELOAD_PROPERTIES
    assertTrue(Boolean.parseBoolean(handlers.get(YaHttpServerConstants.RELOAD_PROPERTIES)
        .getResponseValueAsync(parameters)));

    EasyMock.verify(codeblocksMock, mockURLconn, mockServerConn);
  }

  public void testReloadProperties_failure() throws Throwable {
    // This method tests RELOAD_PROEPRTIES, but first we have to load the form.
    expectLoadFormSuccess();

    // read the form properties from server
    expect(mockServerConn.getConnection(baseUrl + formPath)).andReturn(mockURLconn);
    expect(mockURLconn.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK);
    expect(mockURLconn.getInputStream()).andReturn(new ByteArrayInputStream(formProps));
    expect(mockURLconn.getContentEncoding()).andReturn(null);

    codeblocksMock.loadProperties(formPropsString);
    EasyMock.expectLastCall().andThrow(new LoadException("load failed"));
    EasyMock.replay(codeblocksMock, mockURLconn, mockServerConn);

    assertTrue(invokeLoadForm());

    // test by invoking the YaHttpServerMain handler for RELOAD_PROPERTIES
    assertFalse(Boolean.parseBoolean(handlers.get(YaHttpServerConstants.RELOAD_PROPERTIES)
        .getResponseValueAsync(parameters)));

    EasyMock.verify(codeblocksMock, mockURLconn, mockServerConn);
  }

  public void testClearCodeblocks_success() throws Throwable {
    codeblocksMock.loadFreshWorkspace("", null);
    EasyMock.replay(codeblocksMock, mockURLconn, mockServerConn);

    // test by invoking the YaHttpServerMain handler for CLEAR_CODEBLOCKS
    assertTrue(Boolean.parseBoolean(handlers.get(YaHttpServerConstants.CLEAR_CODEBLOCKS)
        .getResponseValueAsync(parameters)));

    EasyMock.verify(codeblocksMock, mockURLconn, mockServerConn);
  }

  public void testSaveCodeblocksSource_success() throws Throwable {
    // This method tests SAVE_CODEBLOCKS_SOURCE, but first we have to load the form.
    expectLoadFormSuccess();

    codeblocksMock.persistCodeblocksSourceFile(true);
    EasyMock.replay(codeblocksMock, mockURLconn, mockServerConn);

    assertTrue(invokeLoadForm());

    // test by invoking the YaHttpServerMain handler for SAVE_CODEBLOCKS_SOURCE
    assertTrue(Boolean.parseBoolean(handlers.get(YaHttpServerConstants.SAVE_CODEBLOCKS_SOURCE)
        .getResponseValueAsync(parameters)));

    EasyMock.verify(codeblocksMock, mockURLconn, mockServerConn);
  }

  public void testSaveCodeblocksSource_failure() throws Throwable {
    // This method tests SAVE_CODEBLOCKS_SOURCE, but first we have to load the form.
    expectLoadFormSuccess();

    codeblocksMock.persistCodeblocksSourceFile(true);
    EasyMock.expectLastCall().andThrow(new SaveException("save failed"));
    EasyMock.replay(codeblocksMock, mockURLconn, mockServerConn);

    assertTrue(invokeLoadForm());

    // test by invoking the YaHttpServerMain handler for SAVE_CODEBLOCKS_SOURCE
    assertFalse(Boolean.parseBoolean(handlers.get(YaHttpServerConstants.SAVE_CODEBLOCKS_SOURCE)
        .getResponseValueAsync(parameters)));

    EasyMock.verify(codeblocksMock, mockURLconn, mockServerConn);
  }

  public void testSyncProperty_success() throws Throwable {
    // This method tests SYNC_PROPERTY, but first we have to load the form.
    expectLoadFormSuccess();

    String componentName = "Button1";
    String componentType = "Button";
    String propertyName = "Text";
    String propertyValue = "idontcare";
    expect(codeblocksMock.syncProperty(componentName, componentType, propertyName, propertyValue))
      .andReturn(true);
    EasyMock.replay(codeblocksMock, mockURLconn, mockServerConn);

    assertTrue(invokeLoadForm());

    // test by invoking the YaHttpServerMain handler for SYNC_PROPERTY
    parameters.put(YaHttpServerConstants.COMPONENT_NAME, componentName);
    parameters.put(YaHttpServerConstants.COMPONENT_TYPE, componentType);
    parameters.put(YaHttpServerConstants.PROPERTY_NAME, propertyName);
    parameters.put(YaHttpServerConstants.PROPERTY_VALUE, propertyValue);
    assertTrue(Boolean.parseBoolean(handlers.get(YaHttpServerConstants.SYNC_PROPERTY)
        .getResponseValueAsync(parameters)));

    EasyMock.verify(codeblocksMock, mockURLconn, mockServerConn);
  }
}
