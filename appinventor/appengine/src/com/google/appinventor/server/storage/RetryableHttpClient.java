// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class RetryableHttpClient {

  private final HttpClient client;
  private final int maxRetries;
  private final Duration delayBetweenRetries;

  public RetryableHttpClient(HttpClient client, int maxRetries, Duration delayBetweenRetries) {
    this.client = client;
    this.maxRetries = maxRetries;
    this.delayBetweenRetries = delayBetweenRetries;
  }

  /**
   * Sends the request with retry logic.
   */
  public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseHandler)
            throws IOException, InterruptedException {

    int attempts = 0;
    while (true) {
      try {
        attempts++;
        HttpResponse<T> response = client.send(request, responseHandler);

        // Check if the status code warrants a retry (e.g., 5xx errors)
        if (shouldRetry(response.statusCode()) && attempts <= maxRetries) {
          waitBeforeRetry(attempts);
          continue;
        }
        return response;

      } catch (IOException e) {
        if (attempts > maxRetries) {
          throw e; // Rethrow if we've exhausted retries
        }
        waitBeforeRetry(attempts);
      }
    }
  }

  private boolean shouldRetry(int statusCode) {
    // Retry on Server Errors (500, 502, 503, 504)
    return statusCode >= 500 && statusCode <= 599;
  }

  private void waitBeforeRetry(int attempt) throws InterruptedException {
    // Simple exponential backoff: delay * attempt
    long sleepMillis = delayBetweenRetries.toMillis() * attempt;
    Thread.sleep(sleepMillis);
  }

  // Delegate other methods if necessary (e.g., executor, cookieHandler)
  public HttpClient getInternalClient() {
    return this.client;
  }
}
