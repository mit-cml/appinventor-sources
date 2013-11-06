// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.blockseditor.jsonp;

import com.google.appinventor.common.jsonp.JsonpConstants;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import junit.framework.TestCase;

import java.util.Map;


/**
 * Unit tests for {@link HttpServer}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class HttpServerTest extends TestCase {
  private static final int REQUIRED_SECRET = 217;

  private Map<String, String> createValidParameters() {
    return ImmutableMap.of(
        JsonpConstants.OUTPUT, JsonpConstants.REQUIRED_OUTPUT_VALUE,
        JsonpConstants.CALLBACK, JsonpConstants.REQUIRED_CALLBACK_VALUE,
        JsonpConstants.ID, "jr_123",
        JsonpConstants.SECRET, Integer.toString(REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithValidParameters() throws Exception {
    Map<String, String> parameters = createValidParameters();
    assertTrue(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithMissingOutputParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.remove(JsonpConstants.OUTPUT);
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithEmptyOutputParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.put(JsonpConstants.OUTPUT, "");
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithIllegalOutputParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.put(JsonpConstants.OUTPUT, "illegal");
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithMissingCallbackParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.remove(JsonpConstants.CALLBACK);
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithEmptyCallbackParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.put(JsonpConstants.CALLBACK, "");
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithIllegalCallbackParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.put(JsonpConstants.CALLBACK, "illegal");
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithMissingIdParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.remove(JsonpConstants.ID);
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithEmptyIdParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.put(JsonpConstants.ID, "");
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithMissingSecretParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.remove(JsonpConstants.SECRET);
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithEmptySecretParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.put(JsonpConstants.SECRET, "");
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithNonNumericSecretParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.put(JsonpConstants.SECRET, "illegal");
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }

  public void testValidateRequestParametersWithIncorrectSecretParameter() throws Exception {
    Map<String, String> parameters = Maps.newHashMap(createValidParameters());
    parameters.put(JsonpConstants.SECRET, "712");
    assertFalse(HttpServer.validateRequestParameters(parameters, REQUIRED_SECRET));
  }
}
