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

/**
 * Cloud Storage Delete Client handles deleting objects from buckets.
 */
final class CloudStorageDeleteClient extends CloudStorageClient {
  /**
   * Delete the object from the Cloud Storage Bucket.
   */
  public function delete() {
    $token_header = $this->getOAuthTokenHeader(parent::WRITE_SCOPE);
    if ($token_header === false) {
      trigger_error("Unable to acquire OAuth token.", E_USER_WARNING);
      return false;
    }

    $http_response = $this->makeHttpRequest($this->url,
                                            "DELETE",
                                            $token_header);

    if ($http_response === false) {
      return false;
    }

    // Unlinking the file also removes it from the stat cache.
    // See: http://php.net/manual/en/function.clearstatcache.php
    clearstatcache(true, $this->filename);

    if ($http_response['status_code'] === HttpResponse::NO_CONTENT) {
      return true;
    } else {
      trigger_error($this->getErrorMessage($http_response['status_code'],
                                           $http_response['body']),
                    E_USER_WARNING);
    }

    return false;
  }
}
