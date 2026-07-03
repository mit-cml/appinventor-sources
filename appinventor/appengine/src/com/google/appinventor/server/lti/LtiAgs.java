// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.storage.StoredData;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.Instant;

import org.json.JSONObject;

/**
 * Assignment and Grade Services (AGS) grade passback for the LTI tool. Mints an
 * OAuth2 client credentials access token using a private key JWT client
 * assertion, then posts a score to the line item that came with the launch.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiAgs {

  private static final String SCORE_SCOPE =
      "https://purl.imsglobal.org/spec/lti-ags/scope/score";
  private static final String ASSERTION_TYPE =
      "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
  private static final long TOKEN_TTL_SECONDS = 300;

  private LtiAgs() {}

  /**
   * Records that the given platform user has submitted, leaving the grade for
   * the teacher to assign in the LMS. Posts to the AGS line item with
   * activityProgress Submitted and gradingProgress PendingManual (no score), so
   * the teacher grades it in the LMS and the student then views that grade.
   */
  static void postSubmission(String issuer, String lineItemUrl, String ltiUserSub)
      throws Exception {
    StoredData.LtiPlatformData platform = LtiConfig.platform(issuer);
    if (platform == null) {
      throw new IllegalStateException("No registered LTI platform for issuer " + issuer);
    }
    String token = accessToken(platform, SCORE_SCOPE);
    JSONObject score = new JSONObject()
        .put("userId", ltiUserSub)
        .put("activityProgress", "Submitted")
        .put("gradingProgress", "PendingManual")
        .put("timestamp", Instant.now().toString());
    LtiHttp.postJsonWithBearer(scoresUrl(lineItemUrl), score.toString(), token,
        "application/vnd.ims.lis.v1.score+json");
  }

  /** Gets an AGS access token via the client credentials private key JWT flow. */
  private static String accessToken(StoredData.LtiPlatformData platform, String scope)
      throws Exception {
    PrivateKey key = LtiJwt.loadPrivateKey(LtiConfig.privateKeyFile());
    long now = System.currentTimeMillis() / 1000L;
    JSONObject header = new JSONObject()
        .put("alg", "RS256").put("typ", "JWT").put("kid", LtiConfig.KID);
    JSONObject claims = new JSONObject()
        .put("iss", platform.clientId)
        .put("sub", platform.clientId)
        .put("aud", platform.tokenEndpoint)
        .put("iat", now)
        .put("exp", now + TOKEN_TTL_SECONDS)
        .put("jti", LtiState.random());
    String assertion = LtiJwt.sign(header, claims, key);
    String body = "grant_type=client_credentials"
        + "&client_assertion_type=" + enc(ASSERTION_TYPE)
        + "&client_assertion=" + enc(assertion)
        + "&scope=" + enc(scope);
    String resp = LtiHttp.postForm(platform.tokenEndpoint, body);
    return new JSONObject(resp).getString("access_token");
  }

  /** Inserts /scores into the line item path, before any query string. */
  private static String scoresUrl(String lineItemUrl) {
    int q = lineItemUrl.indexOf('?');
    if (q < 0) {
      return lineItemUrl + "/scores";
    }
    return lineItemUrl.substring(0, q) + "/scores" + lineItemUrl.substring(q);
  }

  private static String enc(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
}
