// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import junit.framework.TestCase;

import org.json.JSONObject;

/**
 * Tests that the dynamic registration request the tool sends to a platform
 * carries the LTI tool configuration a platform needs to register App Inventor.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiRegisterServletTest extends TestCase {

  private static final String TOOL_CONFIG =
      "https://purl.imsglobal.org/spec/lti-tool-configuration";

  /** The registration request carries the tool endpoints and both message types. */
  public void testToolRegistrationShape() {
    JSONObject reg = LtiRegisterServlet.toolRegistration(
        "https://tool.example.org/lti/login",
        "https://tool.example.org/lti/launch",
        "https://tool.example.org/lti/jwks",
        "tool.example.org",
        "App Inventor");
    assertEquals("https://tool.example.org/lti/login", reg.getString("initiate_login_uri"));
    assertEquals("https://tool.example.org/lti/jwks", reg.getString("jwks_uri"));
    assertEquals("private_key_jwt", reg.getString("token_endpoint_auth_method"));
    assertEquals("https://tool.example.org/lti/launch",
        reg.getJSONArray("redirect_uris").getString(0));

    JSONObject toolConfig = reg.getJSONObject(TOOL_CONFIG);
    assertEquals("tool.example.org", toolConfig.getString("domain"));
    assertEquals("https://tool.example.org/lti/launch", toolConfig.getString("target_link_uri"));
    // The tool advertises both a resource link launch and Deep Linking.
    assertEquals(2, toolConfig.getJSONArray("messages").length());
    assertEquals("LtiResourceLinkRequest",
        toolConfig.getJSONArray("messages").getJSONObject(0).getString("type"));
    assertEquals("LtiDeepLinkingRequest",
        toolConfig.getJSONArray("messages").getJSONObject(1).getString("type"));
  }

  /** A configuration URL must sit at or under the issuer, per Dynamic Registration 3.4. */
  public void testConfigUrlMustMatchIssuer() {
    assertTrue(LtiRegisterServlet.configBelongsToIssuer(
        "https://lms.example.org/.well-known/openid-configuration", "https://lms.example.org"));
    assertTrue(LtiRegisterServlet.configBelongsToIssuer(
        "https://lms.example.org/lti/config/849", "https://lms.example.org"));
    assertFalse("a different host is a different issuer",
        LtiRegisterServlet.configBelongsToIssuer(
            "https://attacker.example/openid.json", "https://lms.example.org"));
    assertFalse("a subdomain is not the issuer",
        LtiRegisterServlet.configBelongsToIssuer(
            "https://lti.lms.example.org/config", "https://lms.example.org"));
    assertFalse("a different scheme does not match",
        LtiRegisterServlet.configBelongsToIssuer(
            "http://lms.example.org/config", "https://lms.example.org"));
    // A path scoped issuer, the configuration must sit at or under the issuer path.
    assertTrue(LtiRegisterServlet.configBelongsToIssuer(
        "https://lms.example.org/tenant-a/.well-known/openid-configuration",
        "https://lms.example.org/tenant-a"));
    assertFalse("a sibling path on the shared origin is not the issuer",
        LtiRegisterServlet.configBelongsToIssuer(
            "https://lms.example.org/attacker/config", "https://lms.example.org/tenant-a"));
    assertFalse("a path sharing only a segment prefix is not the issuer",
        LtiRegisterServlet.configBelongsToIssuer(
            "https://lms.example.org/tenant-abc/config", "https://lms.example.org/tenant-a"));
  }
}
