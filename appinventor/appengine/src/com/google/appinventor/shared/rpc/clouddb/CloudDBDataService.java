// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.clouddb;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

/**
 * GWT RPC service interface for reading and writing CloudDB data from the designer.
 */
@RemoteServiceRelativePath(ServerLayout.CLOUDDB_DATA_SERVICE)
public interface CloudDBDataService extends RemoteService {

  /**
   * Fetches all tag/value entries stored under the given projectId prefix.
   *
   * @param projectId   the CloudDB ProjectID (used as the Redis key prefix)
   * @param token       the CloudDB auth token (from the component's Token property)
   * @param redisServer hostname, or {@code "DEFAULT"} to use the server-configured default
   * @param redisPort   TCP port (typically 6381)
   * @param useSSL      whether to use TLS for the Redis connection
   * @return list of tag/value pairs; empty list if no data exists for this projectId
   * @throws Exception if the Redis connection or auth fails
   */
  List<DataEntry> getEntries(String projectId, String token, String redisServer,
      int redisPort, boolean useSSL) throws Exception;

  /**
   * Creates or overwrites a single tag/value entry (Redis SET).
   *
   * @param projectId   the CloudDB ProjectID (Redis key prefix)
   * @param token       the CloudDB auth token
   * @param redisServer hostname, or {@code "DEFAULT"}
   * @param redisPort   TCP port
   * @param useSSL      whether to use TLS
   * @param tag         the key name (must not be blank)
   * @param value       the JSON-encoded value string
   * @throws Exception if the Redis connection or auth fails, or tag is blank
   */
  void setEntry(String projectId, String token, String redisServer,
      int redisPort, boolean useSSL, String tag, String value) throws Exception;

  /**
   * Deletes the entry with the given tag (Redis DEL).
   *
   * @param projectId   the CloudDB ProjectID (Redis key prefix)
   * @param token       the CloudDB auth token
   * @param redisServer hostname, or {@code "DEFAULT"}
   * @param redisPort   TCP port
   * @param useSSL      whether to use TLS
   * @param tag         the key name to delete
   * @throws Exception if the Redis connection or auth fails
   */
  void deleteEntry(String projectId, String token, String redisServer,
      int redisPort, boolean useSSL, String tag) throws Exception;
}
