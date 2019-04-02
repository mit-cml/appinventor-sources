<?php
/**
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 */

namespace google\appengine\ext\cloud_storage_streams;

use google\appengine\util\StringUtil;

/**
 * Cloud Storage Url Stat Client handles stat() calls for objects and buckets.
 */
final class CloudStorageUrlStatClient extends CloudStorageClient {
  // Maximum number of keys to return when querying a bucket.
  const MAX_KEYS = 1000;

  private $quiet;
  private $prefix = null;
  private $next_marker = null;

  public function __construct($bucket, $object, $context, $flags) {
    parent::__construct($bucket, $object, $context);
    $this->quiet = ($flags & STREAM_URL_STAT_QUIET) == STREAM_URL_STAT_QUIET;
    if (isset($object)) {
      // Drop the leading '/' from the object name.
      $this->prefix = substr($object, 1);
    }
  }

  /**
   * The stat function uses GET requests to the bucket to try and determine if
   * the object is a 'file' or a 'directory', by listing the contents of the
   * bucket and then matching the results against the supplied object name.
   *
   * If a file ends with "/ then Google Cloud Console will show it as a 'folder'
   * in the UI tool, so we consider an object that ends in "/" as a directory
   * as well. For backward compatibility, we also treat files with the
   * "_$folder$" suffix as folders.
   */
  public function stat() {
    $prefix = $this->prefix;
    if (StringUtil::endsWith($prefix, parent::DELIMITER)) {
      $prefix = substr($prefix, 0, strlen($prefix) - 1);
    }

    if (ini_get("google_app_engine.enable_gcs_stat_cache") &&
        $this->tryGetFromStatCache($stat_result)) {
      return $stat_result;
    }

    if (isset($prefix)) {
      $result = $this->headObject($prefix);
      if ($result !== false) {
        $mode = parent::S_IFREG;
        $mtime = $result['mtime'];
        $size = $result['size'];
      } else {
        // Object doesn't exisit, check and see if it's a directory.
        do {
          $results = $this->listBucket($prefix);
          if (false === $results) {
            return false;
          }
          // If there are no results then we're done
          if (empty($results)) {
            return false;
          }
          // If there is an entry that contains object_name_$folder$ or
          // object_name/ then we have a 'directory'.
          $object_name_folder = $prefix . parent::FOLDER_SUFFIX;
          $object_name_delimiter = $prefix . parent::DELIMITER;
          foreach ($results as $result) {
            if ($result['name'] === $object_name_folder ||
                $result['name'] === $object_name_delimiter) {
              $mode = parent::S_IFDIR;
              break;
            }
          }
        } while (!isset($mode) && isset($this->next_marker));
      }
    } else {
      // We are now just checking that the bucket exists, as there was no
      // object prefix supplied
      $results = $this->listBucket();
      if ($results !== false) {
        $mode = parent::S_IFDIR;
      } else {
        return false;
      }
    }
    // If mode is not set, then there was no object that matched the criteria.
    if (!isset($mode)) {
      return false;
    }
    // If the app could stat the file, then it must be readable. As different
    // PHP internal APIs check the access mode, we'll set them all to readable.
    $mode |= parent::S_IRUSR | parent::S_IRGRP | parent::S_IROTH;

    if ($this->isBucketWritable($this->bucket_name)) {
      $mode |= parent::S_IWUSR | parent::S_IWGRP | parent::S_IWOTH;
    }

    $stat_args["mode"] = $mode;
    if (isset($mtime)) {
      $unix_time = strtotime($mtime);
      if ($unix_time !== false) {
        $stat_args["mtime"] = $unix_time;
      }
    }

    if (isset($size)) {
      $stat_args["size"] = intval($size);
    }
    $stat_result = $this->createStatArray($stat_args);
    $this->addToStatCache($stat_result);
    return $stat_result;
  }

  /**
   * Perform a HEAD request on an object to get size & mtime info.
   */
  private function headObject($object_name) {
    $headers = $this->getOAuthTokenHeader(parent::READ_SCOPE);
    if ($headers === false) {
      if (!$this->quiet) {
        trigger_error("Unable to acquire OAuth token.", E_USER_WARNING);
      }
      return false;
    }

    $url = $this->createObjectUrl($this->bucket_name, $object_name);
    $http_response = $this->makeHttpRequest($url, "HEAD", $headers);
    if ($http_response === false) {
      if (!$this->quiet) {
        trigger_error('Unable to connect to the Cloud Storage Service.',
                      E_USER_WARNING);
      }
      return false;
    }

    $status_code = $http_response['status_code'];
    if (HttpResponse::OK !== $status_code) {
      if (!$this->quiet && HttpResponse::NOT_FOUND !== $status_code) {
        trigger_error($this->getErrorMessage($http_response['status_code'],
                                             $http_response['body']),
                      E_USER_WARNING);
      }
      return false;
    }

    $headers = $http_response['headers'];
    return ['size' => $this->getHeaderValue('x-goog-stored-content-length',
                                            $headers),
            'mtime' => $this->getHeaderValue('Last-Modified', $headers)];
  }

  /**
   * Perform a GET request on a bucket, with the optional $object_prefix. This
   * is similar to how CloudStorgeDirectoryClient works, except that it is
   * targeting a specific file rather than trying to enumerate of the files in
   * a given bucket with a common prefix.
   */
  private function listBucket($object_prefix = null) {
    $headers = $this->getOAuthTokenHeader(parent::READ_SCOPE);
    if ($headers === false) {
      if (!$this->quiet) {
        trigger_error("Unable to acquire OAuth token.", E_USER_WARNING);
      }
      return false;
    }

    $query_arr = [
        'delimiter' => parent::DELIMITER,
        'max-keys' => self::MAX_KEYS,
    ];
    if (isset($object_prefix)) {
      $query_arr['prefix'] = $object_prefix;
    }
    if (isset($this->next_marker)) {
      $query_arr['marker'] = $this->next_marker;
    }

    $url = $this->createObjectUrl($this->bucket_name);
    $query_str = http_build_query($query_arr);
    $http_response = $this->makeHttpRequest(sprintf("%s?%s", $url, $query_str),
                                            "GET",
                                            $headers);
    if ($http_response === false) {
      if (!$this->quiet) {
        trigger_error('Unable to connect to the Cloud Storage Service.',
                      E_USER_WARNING);
      }
      return false;
    }

    if (HttpResponse::OK !== $http_response['status_code']) {
      if (!$this->quiet) {
        trigger_error($this->getErrorMessage($http_response['status_code'],
                                             $http_response['body']),
                      E_USER_WARNING);
      }
      return false;
    }

    // Extract the files into the result array.
    $xml = simplexml_load_string($http_response['body']);

    if (isset($xml->NextMarker)) {
      $this->next_marker = (string) $xml->NextMarker;
    } else {
      $this->next_marker = null;
    }

    $results = [];
    foreach($xml->Contents as $content) {
      $results [] = [
          'name' => (string) $content->Key,
          'size' => (string) $content->Size,
          'mtime' => (string) $content->LastModified,
      ];
    }
    // Subdirectories will be returned in the CommonPrefixes section. Refer to
    // https://developers.google.com/storage/docs/reference-methods#getbucket
    foreach($xml->CommonPrefixes as $common_prefix) {
      $results[] = [
          'name' => (string) $common_prefix->Prefix,
      ];
    }
    return $results;
  }

  /**
   * Test if a given bucket is writable. We will cache results in memcache as
   * this is an expensive operation. This might lead to incorrect results being
   * returned for this call for a short period while the result remains in the
   * cache.
   */
  private function isBucketWritable($bucket) {
    $cache_key_name = sprintf(parent::WRITABLE_MEMCACHE_KEY_FORMAT, $bucket);
    $memcache = new \Memcache();
    $result = $memcache->get($cache_key_name);

    if ($result) {
      return $result['is_writable'];
    }

    // We determine if the bucket is writable by trying to start a resumable
    // upload. GCS will cleanup the abandoned upload after 7 days, and it will
    // not be charged to the bucket owner.
    $token_header = $this->getOAuthTokenHeader(parent::WRITE_SCOPE);
    if ($token_header === false) {
      return false;
    }
    $headers = array_merge(parent::$upload_start_header, $token_header);
    $url = parent::createObjectUrl($bucket, parent::WRITABLE_TEMP_FILENAME);
    $http_response = $this->makeHttpRequest($url,
                                            "POST",
                                            $headers);

    if ($http_response === false) {
      return false;
    }

    $status_code = $http_response['status_code'];
    $is_writable = $status_code == HttpResponse::CREATED;

    $memcache->set($cache_key_name,
                   ['is_writable' => $is_writable],
                   null,
                   $this->context_options['writable_cache_expiry_seconds']);
    return $is_writable;
  }
}
