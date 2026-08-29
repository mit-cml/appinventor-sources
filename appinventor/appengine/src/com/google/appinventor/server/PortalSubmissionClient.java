// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.tokens.Token;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.Duration;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Notifies the classroom portal that a student submitted an assignment from
 * inside the App Inventor editor.
 *
 * Deliberately does not use RetryableHttpClient (unlike S3Access): a blind
 * retry here risks creating a duplicate submission if the first attempt
 * actually succeeded on the portal but the acknowledgment was lost in
 * transit. A failure here should be surfaced to the caller as a failure,
 * not silently retried.
 */
public class PortalSubmissionClient {

  // Thrown for a well-formed, expected rejection (currently: team assignment
  // submission, which isn't supported yet) whose message is meant to be shown
  // to the student as-is, unlike a generic IOException whose message is an
  // internal detail.
  public static class PortalRejectionException extends IOException {
    public PortalRejectionException(String message) {
      super(message);
    }
  }

  // Status the portal's /api/ai/submit route uses specifically for "this is a
  // team assignment, not supported here yet" -- distinct from a generic 404
  // (no matching project at all) so the student sees a specific message.
  private static final int TEAM_NOT_SUPPORTED_STATUS = 422;

  private static final Flag<String> portalUrl = Flag.createFlag("portal.url", "");

  // HTTP/1.1 is forced explicitly: this is a plain cleartext, server-to-server POST where
  // HTTP/2 buys nothing, and HttpClient's default (HTTP_2, negotiated opportunistically) can
  // hang for the full request timeout against some HTTP/1.1-only servers (observed against a
  // Vite/Node dev server) instead of falling back cleanly.
  private static final HttpClient CLIENT = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_1_1)
    .connectTimeout(Duration.ofSeconds(10))
    .build();

  private PortalSubmissionClient() {
  }

  /**
   * @param projectOwnerId the App Inventor project owner uuid (the portal's
   *        projectVersions.projectOwnerId for this student/assignment pairing)
   * @param projectId the project being submitted
   * @throws PortalRejectionException for a well-formed, expected rejection
   *         (currently: team assignment submission) with a message meant to
   *         be shown to the student as-is
   * @throws IOException if the portal is unreachable or returns any other
   *         non-200 status
   */
  public static void submitAssignment(String projectOwnerId, long projectId)
    throws IOException, InterruptedException {
    String url = portalUrl.get();
    if (url.isEmpty()) {
      throw new IllegalStateException("portal.url is not configured");
    }
    String token = Token.makeSubmitAssignmentToken(projectOwnerId, projectId);
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(url + "/api/ai/submit?stoken=" + token))
      .timeout(Duration.ofSeconds(15))
      .POST(HttpRequest.BodyPublishers.noBody())
      .build();
    HttpResponse<String> response;
    try {
      response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException e) {
      // Connection-level failures (DNS, refused, timeout, ...) commonly have a
      // null getMessage() -- java.net.ConnectException/UnresolvedAddressException
      // both do -- which would otherwise surface as the unhelpful "null".
      throw new IOException("Could not reach portal at " + url + ": " + describeException(e), e);
    }
    if (response.statusCode() == 200) {
      return;
    }
    if (response.statusCode() == TEAM_NOT_SUPPORTED_STATUS) {
      throw new PortalRejectionException(extractMessage(response.body()));
    }
    throw new IOException("Portal submit failed: " + response.statusCode()
      + " " + response.body());
  }

  // The portal's error() responses are JSON: {"message": "..."}. Fall back to
  // the raw body if it's not in that shape for any reason.
  private static String extractMessage(String body) {
    try {
      return new JSONObject(body).getString("message");
    } catch (JSONException e) {
      return body;
    }
  }

  // Many connection-level exceptions (ConnectException, UnresolvedAddressException,
  // ...) have a null getMessage() by design in the JDK. Walk the cause chain for
  // the first non-null message; if none exists anywhere, fall back to the
  // deepest cause's class name, which is usually still a useful diagnostic
  // (e.g. "UnresolvedAddressException" points straight at a DNS/config problem).
  private static String describeException(Throwable e) {
    Throwable current = e;
    Throwable deepest = e;
    while (current != null) {
      if (current.getMessage() != null) {
        return current.getMessage();
      }
      deepest = current;
      current = current.getCause();
    }
    return deepest.getClass().getSimpleName();
  }

}
