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
namespace google\appengine\api\cloud_storage;

use google\appengine\BlobstoreServiceError\ErrorCode;
use google\appengine\CreateEncodedGoogleStorageKeyRequest;
use google\appengine\CreateEncodedGoogleStorageKeyResponse;
use google\appengine\CreateUploadURLRequest;
use google\appengine\CreateUploadURLResponse;
use google\appengine\ImagesDeleteUrlBaseRequest;
use google\appengine\ImagesDeleteUrlBaseResponse;
use google\appengine\ImagesGetUrlBaseRequest;
use google\appengine\ImagesGetUrlBaseResponse;
use google\appengine\ImagesServiceError;
use google\appengine\ext\cloud_storage_streams\CloudStorageClient;
use google\appengine\ext\cloud_storage_streams\CloudStorageStreamWrapper;
use google\appengine\GetDefaultGcsBucketNameRequest;
use google\appengine\GetDefaultGcsBucketNameResponse;
use google\appengine\runtime\ApiProxy;
use google\appengine\runtime\ApplicationError;
use google\appengine\util\ArrayUtil;
use google\appengine\util\StringUtil;

/**
 * CloudStorageTools allows the user to create and serve data with
 * <a href="http://cloud.google.com/products/cloud-storage">Google Cloud Storage
 * </a>.
 */
final class CloudStorageTools {
  const GS_PREFIX = 'gs://';
  const BLOB_KEY_HEADER = "X-AppEngine-BlobKey";
  const BLOB_RANGE_HEADER = "X-AppEngine-BlobRange";
  const MAX_IMAGE_SERVING_SIZE = 1600;

  // The GCS endpoint path when running in the dev appserver.
  const LOCAL_ENDPOINT = "/_ah/gcs";

  // The storage host when running in production
  // - The subdomain format is more secure but does not work for HTTPS if the
  //   bucket name contains ".". This is becuase the wildcard SSL certificate
  //   used by GCS can only validate one level of subdomain.
  // - The path format is less secure and should only be used for the specific
  //   case when the subdomain format fails.
  const PRODUCTION_HOST_SUBDOMAIN_FORMAT = "%s.storage.googleapis.com";
  const PRODUCTION_HOST_PATH_FORMAT = "storage.googleapis.com";

  // The GCS filename format (bucket, object).
  const GS_FILENAME_FORMAT = "gs://%s/%s";

  // The maximum value in seconds for the upload URL timeout option.
  const MAX_URL_EXPIRY_TIME_SECONDS = 86400;  // 24 hours

  // The keyword to be replaced by app's default bucket in GCS filename.
  const GS_DEFAULT_BUCKET_KEYWORD = '#default#';

  // The key to use when caching the default GCS bucket name in APC.
  const GS_DEFAULT_BUCKET_APC_KEY = '__DEFAULT_GCS_BUCKET_NAME__';

  /**
   * The list of options that can be supplied to createUploadUrl.
   * @see CloudStorageTools::createUploadUrl()
   * @var array
   *
   * @access private
   */
  private static $create_upload_url_options = ['gs_bucket_name',
      'max_bytes_per_blob', 'max_bytes_total', 'url_expiry_time_seconds'];

  /**
   * The list of options that can be suppied to serve.
   * @var array
   *
   * @access private
   */
  private static $serve_options = ['content_type', 'save_as', 'start', 'end',
      'use_range'];

  /**
   * The list of default options for the method getImageServingUrl()
   * @var array
   *
   * @access private
   */
  private static $get_image_serving_url_default_options = [
      'crop'       => false,
      'secure_url' => false,
      'size'       => null,
  ];

  /**
   * Workaround for the 'Cannot modify header information' problem when
   * trying to send headers from unit tests. If set, then $send_header is
   * expected to be a closure that accepts a key, value pair where key is the
   * header name, and value is the header value.
   *
   * @access private
   */
  private static $send_header = null;

  /**
   * Object names may contain characters that need to be percent-encoded when
   * building the URL. All characters allowed for bucket name are URL-safe. See
   * https://developers.google.com/storage/docs/bucketnaming#requirements for
   * more details.
   *
   * @access private
   */
  private static $url_path_translation_map = [
      ' ' => '%20',
      '#' => '%23',
      '%' => '%25',
      '?' => '%3F',
  ];

  /**
   * Create an absolute URL that can be used by a user to asynchronously upload
   * a large blob. Upon completion of the upload, a callback is made to the
   * specified URL.
   *
   * @param string $success_path A relative URL which will be invoked after the
   * user successfully uploads a blob.
   * @param mixed[] $options A key value pair array of upload options. Valid
   * options are:<ul>
   * <li>'max_bytes_per_blob': integer The value of the largest size that any
   * one uploaded blob may be. Default value: unlimited.
   * <li>'max_bytes_total': integer The value that is the total size that sum of
   * all uploaded blobs may be. Default value: unlimited.
   * <li>'gs_bucket_name': string The name of a Google Cloud Storage
   *   bucket that the blobs should be uploaded to. Not specifying a value
   *   will result in the blob being uploaded to the application's default
   *   bucket.
   * <li>'url_expiry_time_seconds': integer The number of seconds that the
   *   generated URL can be used for to upload files to Google Cloud Storage.
   *   Once this timeout expires, the URL is no longer valid and any attempts
   *   to upload using the URL will fail. Must be a positive integer, maximum
   *   value is one day (86400 seconds). Default Value: 600 seconds.
   * </ul>
   * @return string The upload URL.
   *
   * @throws \InvalidArgumentException If $success_path is not valid, or one of
   * the options is not valid.
   * @throws CloudStorageException Thrown when there is a failure using the
   * blobstore service.
   */
  public static function createUploadUrl($success_path, $options = array()) {
    $req = new CreateUploadURLRequest();
    $resp = new CreateUploadURLResponse();

    if (!is_string($success_path)) {
      throw new \InvalidArgumentException('$success_path must be a string');
    }

    $req->setSuccessPath($success_path);
    $max_upload_size_ini = self::getUploadMaxFileSizeInBytes();

    if (array_key_exists('max_bytes_per_blob', $options)) {
      $val = $options['max_bytes_per_blob'];
      if (!is_int($val)) {
        throw new \InvalidArgumentException(
            'max_bytes_per_blob must be an integer');
      }
      if ($val < 1) {
        throw new \InvalidArgumentException(
            'max_bytes_per_blob must be positive.');
      }
      $req->setMaxUploadSizePerBlobBytes($val);
    } else if ($max_upload_size_ini > 0) {
      $req->setMaxUploadSizePerBlobBytes($max_upload_size_ini);
    }

    if (array_key_exists('max_bytes_total', $options)) {
      $val = $options['max_bytes_total'];
      if (!is_int($val)) {
        throw new \InvalidArgumentException(
            'max_bytes_total must be an integer');
      }
      if ($val < 1) {
        throw new \InvalidArgumentException(
            'max_bytes_total must be positive.');
      }
      $req->setMaxUploadSizeBytes($val);
    }

    if (array_key_exists('url_expiry_time_seconds', $options)) {
      $val = $options['url_expiry_time_seconds'];
      if (!is_int($val)) {
        throw new \InvalidArgumentException(
            'url_expiry_time_seconds must be an integer');
      }
      if ($val < 1) {
        throw new \InvalidArgumentException(
            'url_expiry_time_seconds must be positive.');
      }
      if ($val > self::MAX_URL_EXPIRY_TIME_SECONDS) {
        throw new \InvalidArgumentException(
            'url_expiry_time_seconds must not exceed ' .
            self::MAX_URL_EXPIRY_TIME_SECONDS);
      }
      $req->setUrlExpiryTimeSeconds($val);
    }

    if (array_key_exists('gs_bucket_name', $options)) {
      $val = $options['gs_bucket_name'];
      if (!is_string($val)) {
        throw new \InvalidArgumentException('gs_bucket_name must be a string');
      }
      $req->setGsBucketName($val);
    } else {
      $bucket = self::getDefaultGoogleStorageBucketName();

      if (!$bucket) {
        throw new \InvalidArgumentException(
            'Application does not have a default Cloud Storage Bucket, ' .
            'gs_bucket_name must be specified');
      }
      $req->setGsBucketName($bucket);
    }

    $extra_options = array_diff(array_keys($options),
                                self::$create_upload_url_options);

    if (!empty($extra_options)) {
      throw new \InvalidArgumentException('Invalid options supplied: ' .
          htmlspecialchars(implode(',', $extra_options)));
    }

    try {
      ApiProxy::makeSyncCall('blobstore', 'CreateUploadURL', $req, $resp);
    } catch (ApplicationError $e) {
      throw self::applicationErrorToException($e);
    }
    return $resp->getUrl();
  }

  /**
   * Returns a URL that serves an image.
   *
   * @param string $gs_filename The name of the Google Cloud Storage object to
   * serve. In the format gs://bucket_name/object_name
   *
   * @param mixed[] $options Array of additional options for serving the object.
   * Valid options are:
   * <ul>
   * <li>'crop': boolean Whether the image should be cropped.  If set to true, a
   *   size must also be supplied. Default value: false.
   * <li>'secure_url': boolean Whether to request an https URL. Default value:
   *   false.
   * <li>'size': integer The size of the longest dimension of the resulting
   * image. Size must be in the range 0 to 1600, with 0 specifying the size of
   * the original image. The aspect ratio is preserved unless 'crop' is
   * specified.
   * </ul>
   * @return string The image serving URL.
   *
   * @throws \InvalidArgumentException if any of the arguments are not valid.
   * @throws CloudStorageException If there was a problem contacting the
   * service.
   */
  public static function getImageServingUrl($gs_filename, $options = []) {
    $blob_key = self::createGsKey($gs_filename);
    if (!is_array($options)) {
      throw new \InvalidArgumentException('$options must be an array. ' .
          'Actual type: ' . gettype($options));
    }

    $extra_options = array_diff(array_keys($options), array_keys(
         self::$get_image_serving_url_default_options));
    if (!empty($extra_options)) {
      throw new \InvalidArgumentException('Invalid options supplied: ' .
          htmlspecialchars(implode(',', $extra_options)));
    }
    $options = array_merge(self::$get_image_serving_url_default_options,
                           $options);

    # Validate options.
    if (!is_bool($options['crop'])) {
      throw new \InvalidArgumentException(
          '$options[\'crop\'] must be a boolean. ' .
          'Actual type: ' . gettype($options['crop']));
    }
    if ($options['crop'] && is_null($options['size'])) {
      throw new \InvalidArgumentException(
          '$options[\'size\'] must be set because $options[\'crop\'] is true.');
    }
    if (!is_null($options['size'])) {
      $size = $options['size'];
      if (!is_int($size)) {
        throw new \InvalidArgumentException(
            '$options[\'size\'] must be an integer. ' .
            'Actual type: ' . gettype($size));
      }
      if ($size < 0 || $size > self::MAX_IMAGE_SERVING_SIZE) {
        throw new \InvalidArgumentException(
            '$options[\'size\'] must be >= 0 and <= ' .
            self::MAX_IMAGE_SERVING_SIZE .  '. Actual value: ' . $size);
      }
    }
    if (!is_bool($options['secure_url'])) {
      throw new \InvalidArgumentException(
          '$options[\'secure_url\'] must be a boolean. ' .
          'Actual type: ' . gettype($options['secure_url']));
    }

    $req = new ImagesGetUrlBaseRequest();
    $resp = new ImagesGetUrlBaseResponse();
    $req->setBlobKey($blob_key);
    $req->setCreateSecureUrl($options['secure_url']);

    try {
      ApiProxy::makeSyncCall('images',
                             'GetUrlBase',
                             $req,
                             $resp);
    } catch (ApplicationError $e) {
      throw self::imagesApplicationErrorToException($e);
    }
    $url = $resp->getUrl();
    if (!is_null($options['size'])) {
      $url .= ('=s' . $options['size']);
      if ($options['crop']) {
        $url .= '-c';
      }
    }
    return $url;
  }

  /**
   * Deletes an image serving URL that was created using getImageServingUrl.
   *
   * @param string $gs_filename The name of the Google Cloud Storage object
   * that has an existing URL to delete. In the format
   * gs://bucket_name/object_name
   *
   * @throws \InvalidArgumentException if any of the arguments are not valid.
   * @throws CloudStorageException If there was a problem contacting the
   * service.
   */
  public static function deleteImageServingUrl($gs_filename) {
    $blob_key = self::createGsKey($gs_filename);
    $req = new ImagesDeleteUrlBaseRequest();
    $resp = new ImagesDeleteUrlBaseResponse();
    $req->setBlobKey($blob_key);

    try {
      ApiProxy::makeSyncCall('images',
                             'DeleteUrlBase',
                             $req,
                             $resp);
    } catch (ApplicationError $e) {
      throw self::imagesApplicationErrorToException($e);
    }
  }

  /**
   * Get the public URL for a Google Cloud Storage filename.
   *
   * @param string $gs_filename The Google Cloud Storage filename, in the
   * format gs://bucket_name/object_name.
   * @param boolean $use_https If True then return a HTTPS URL. Note that the
   * development server ignores this argument and returns only HTTP URLs.
   *
   * @return string The public URL.
   *
   * @throws \InvalidArgumentException if the filename is not in the correct
   * format or $use_https is not a boolean.
   */
  public static function getPublicUrl($gs_filename, $use_https) {
    if (!is_bool($use_https)) {
      throw new \InvalidArgumentException(
          'Parameter $use_https must be boolean but was ' .
          typeOrClass($use_https));
    }

    if (!self::parseFilename($gs_filename, $bucket, $object)) {
      throw new \InvalidArgumentException(
          sprintf('Invalid Google Cloud Storage filename: %s',
                  htmlspecialchars($gs_filename)));
    }

    if (self::isDevelServer()) {
      $scheme = 'http';
      $host = getenv('HTTP_HOST');
      $path = sprintf('%s/%s%s', self::LOCAL_ENDPOINT, $bucket, $object);
    } else {
      // Use path format for HTTPS URL when the bucket name contains "." to
      // avoid SSL certificate validation issue.
      if ($use_https && strpos($bucket, '.') !== false) {
        $host = self::PRODUCTION_HOST_PATH_FORMAT;
        $path = sprintf('/%s%s', $bucket, $object);
      } else {
        $host = sprintf(self::PRODUCTION_HOST_SUBDOMAIN_FORMAT, $bucket);
        $path = strlen($object) > 0 ? $object : '/';
      }

      $scheme = $use_https ? 'https' : 'http';
    }

    return sprintf('%s://%s%s',
                   $scheme,
                   $host,
                   strtr($path, self::$url_path_translation_map));
  }

  /**
   * Get the filename of a Google Cloud Storage object.
   *
   * @param string $bucket The Google Cloud Storage bucket name.
   * @param string $object The Google Cloud Stroage object name.
   *
   * @return string The filename in the format gs://bucket_name/object_name.
   *
   * @throws \InvalidArgumentException if bucket or object name is invalid.
   */
  public static function getFilename($bucket, $object) {
    if (self::validateBucketName($bucket) === false) {
      throw new \InvalidArgumentException(
          sprintf('Invalid cloud storage bucket name \'%s\'',
                  htmlspecialchars($bucket)));
    }

    if (self::validateObjectName($object) === false) {
      throw new \InvalidArgumentException(
          sprintf('Invalid cloud storage object name \'%s\'',
                  htmlspecialchars($object)));
    }

    return sprintf(self::GS_FILENAME_FORMAT, $bucket, $object);
  }

  /**
   * Parse and extract the bucket and object names from the supplied filename.
   *
   * @param string $filename The filename in the format gs://bucket_name or
   * gs://bucket_name/object_name.
   * @param string &$bucket The extracted bucket.
   * @param string &$object The extracted object. Can be null if the filename
   * contains only bucket name.
   *
   * @return bool true if the filename is successfully parsed, false otherwise.
   */
  public static function parseFilename($filename, &$bucket, &$object) {
    $bucket = null;
    $object = null;

    // $filename may contain nasty characters like # and ? that can throw off
    // parse_url(). It is best to do a manual parse here.
    $gs_prefix_len = strlen(self::GS_PREFIX);
    if (!StringUtil::startsWith($filename, self::GS_PREFIX)) {
      return false;
    }

    $first_slash_pos = strpos($filename, '/', $gs_prefix_len);
    if ($first_slash_pos === false) {
      $bucket = substr($filename, $gs_prefix_len);
    } else {
      $bucket = substr($filename, $gs_prefix_len,
          $first_slash_pos - $gs_prefix_len);
      // gs://bucket_name/ is treated the same as gs://bucket_name where
      // $object should be set to null.
      if ($first_slash_pos != strlen($filename) - 1) {
        $object = substr($filename, $first_slash_pos);
      }
    }

    if (strlen($bucket) == 0) {
      return false;
    }

    // Substitute default bucket name.
    if (ini_get('google_app_engine.gcs_default_keyword')) {
      if ($bucket === self::GS_DEFAULT_BUCKET_KEYWORD) {
        $bucket = self::getDefaultGoogleStorageBucketName();
        if (!$bucket) {
          throw new \InvalidArgumentException(
              'Application does not have a default Cloud Storage Bucket, ' .
              'must specify a bucket name');
        }
      }
    }

    // Validate bucket & object names.
    if (self::validateBucketName($bucket) === false) {
      trigger_error(sprintf('Invalid cloud storage bucket name \'%s\'',
          $bucket), E_USER_ERROR);
      return false;
    }

    if (isset($object) && self::validateObjectName($object) === false) {
      trigger_error(sprintf('Invalid cloud storage object name \'%s\'',
          $object), E_USER_ERROR);
      return false;
    }

    return true;
  }

  /**
   * Validate the bucket name according to the rules stated at
   * https://developers.google.com/storage/docs/bucketnaming.
   *
   * @param string $bucket_name The Google Cloud Storage bucket name.
   *
   * @access private
   */
  private static function validateBucketName($bucket_name) {
    $valid_bucket_regex = '/^[a-z0-9]+[a-z0-9\.\-_]+[a-z0-9]+$/';
    if (preg_match($valid_bucket_regex, $bucket_name) === 0) {
      return false;
    }

    if (strpos($bucket_name, 'goog') === 0) {
      return false;
    }

    if (filter_var($bucket_name, FILTER_VALIDATE_IP) !== false) {
      return false;
    }

    $parts = explode('.', $bucket_name);
    if (count($parts) > 1) {
      if (strlen($bucket_name) > 222) {
        return false;
      }
    }

    foreach ($parts as $part) {
      if (strlen($part) > 63) {
        return false;
      }
    }

    return true;
  }

  /**
   * Validate the object name according to the rules stated at
   * https://developers.google.com/storage/docs/bucketnaming.
   *
   * @param string $object_name The Google Cloud Stroage object name.
   *
   * @access private
   */
  private static function validateObjectName($object_name) {
    $invalid_object_regex = "/[\n\r\v\f]/";
    if (preg_match($invalid_object_regex, $object_name) === 1) {
      return false;
    }

    return true;
  }

  /**
   * Create a blob key for a Google Cloud Storage file.
   *
   * @param string $filename The google cloud storage filename, in the format
   * gs://bucket_name/object_name
   *
   * @return string A blob key for this filename that can be used in other API
   * calls.
   *
   * @throws \InvalidArgumentException if the filename is not in the correct
   * format.
   * @throws CloudStorageException If there was a problem contacting the
   * service.
   * @deprecated This method will be made private in the next version.
   *
   * @access private
   */
  private static function createGsKey($filename) {
    if (!self::parseFilename($filename, $bucket, $object) || !$object) {
      throw new \InvalidArgumentException(
          sprintf('Invalid Google Cloud Storage filename: %s',
                  htmlspecialchars($filename)));
    }

    $gs_filename = sprintf('/gs/%s%s', $bucket, $object);

    $request = new CreateEncodedGoogleStorageKeyRequest();
    $response = new CreateEncodedGoogleStorageKeyResponse();

    $request->setFilename($gs_filename);

    try {
      ApiProxy::makeSyncCall('blobstore',
                             'CreateEncodedGoogleStorageKey',
                             $request,
                             $response);
    } catch (ApplicationError $e) {
      throw self::applicationErrorToException($e);
    }

    return $response->getBlobKey();
  }

  /**
   * Serve a Google Cloud Storage file as the response.
   *
   * @param string $gs_filename The name of the Google Cloud Storage object to
   * serve.
   * @param mixed[] $options Array of additional options for serving the object.
   * <ul>
   *   <li>'content_type': string Content-Type to override when known.
   *   <li>'save_as': string Filename for Content-Disposition header.
   *   <li>'start': int Start index of content-range to send.
   *   <li>'end': int End index of content-range to send. End index is
   *   inclusive.
   *   <li>'use_range': boolean Use provided content range from the request's
   *   Range header. Mutually exclusive with start and end.
   * </ul>
   *
   * @throws \InvalidArgumentException If invalid options are supplied.
   */
  public static function serve($gs_filename, $options = []) {
    $extra_options = array_diff(array_keys($options), self::$serve_options);

    if (!empty($extra_options)) {
      throw new \InvalidArgumentException('Invalid options supplied: ' .
          htmlspecialchars(implode(',', $extra_options)));
    }

    // Determine the range to send
    $start = ArrayUtil::findByKeyOrNull($options, "start");
    $end = ArrayUtil::findByKeyOrNull($options, "end");
    $use_range = ArrayUtil::findByKeyOrNull($options, "use_range");
    $request_range_header = ArrayUtil::findByKeyOrNull($_SERVER, "HTTP_RANGE");

    $range_header = self::checkRanges($start,
                                      $end,
                                      $use_range,
                                      $request_range_header);

    $save_as = ArrayUtil::findByKeyOrNull($options, "save_as");
    if (isset($save_as) && !is_string($save_as)) {
      throw new \InvalidArgumentException("Unexpected value for save_as.");
    }

    $blob_key = self::createGsKey($gs_filename);
    self::sendHeader(self::BLOB_KEY_HEADER, $blob_key);

    if (isset($range_header)) {
      self::sendHeader(self::BLOB_RANGE_HEADER, $range_header);
    }

    $content_type = ArrayUtil::findByKeyOrNull($options, "content_type");
    if (isset($content_type)) {
      self::sendHeader("Content-Type", $content_type);
    }

    if (isset($save_as)) {
      self::sendHeader("Content-Disposition", sprintf(
          "attachment; filename=%s", $save_as));
    }
  }

  /**
   * Return the name of the default Google Cloud Storage bucket for the
   * application, if one has been configured.
   *
   * @return string The bucket name, or an empty string if no bucket has been
   * configured.
   */
  public static function getDefaultGoogleStorageBucketName() {
    $success = false;
    $default_bucket_name = apc_fetch(self::GS_DEFAULT_BUCKET_APC_KEY, $success);
    if ($success) {
      return $default_bucket_name;
    }

    $request = new GetDefaultGcsBucketNameRequest();
    $response = new GetDefaultGcsBucketNameResponse();

    ApiProxy::makeSyncCall('app_identity_service',
                           'GetDefaultGcsBucketName',
                           $request,
                           $response);

    $default_bucket_name = $response->getDefaultGcsBucketName();
    if ($default_bucket_name) {
      apc_store(self::GS_DEFAULT_BUCKET_APC_KEY, $default_bucket_name);
    }
    return $default_bucket_name;
  }

  /**
   * This function is used for unit testing only, it allows replacement of the
   * send_header function that is used to set headers on the response.
   *
   * @param mixed $new_header_func The function to use to set response headers.
   * Set to null to use the inbuilt PHP method header().
   *
   * @access private
   */
  public static function setSendHeaderFunction($new_header_func) {
    self::$send_header = $new_header_func;
  }

  /**
   * Get metadata from a Google Cloud Storage file pointer resource.
   *
   * @param resource $handle A Google Cloud Storage file pointer resource that
   * is typically created using fopen().
   *
   * @return array An array that maps metadata keys to values.
   *
   * @throws \InvalidArgumentException If $handler is not a Google Cloud Storage
   * file pointer resource.
   */
  public static function getMetaData($handle) {
    $wrapper = self::getStreamWrapperFromFileHandle($handle);
    return $wrapper->getMetaData();
  }

  /**
   * Get content type from a Google Cloud Storage file pointer resource.
   *
   * @param resource $handle A Google Cloud Storage file pointer resource that
   * is typically created using fopen().
   *
   * @return string The content type of the Google Cloud Storage object.
   *
   * @throws \InvalidArgumentException If $handler is not a Google Cloud Storage
   * file pointer resource.
   */
  public static function getContentType($handle) {
    $wrapper = self::getStreamWrapperFromFileHandle($handle);
    return $wrapper->getContentType();
  }

  /**
   * Retrieve the CloudStorageStreamWrapper instance from a Google Cloud Storage
   * file pointer resource.
   *
   * @param resource $handle A Google Cloud Storage file pointer resource that
   * is typically created using fopen().
   *
   * @return object The CloudStorageStreamWrapper instance.
   *
   * @throws \InvalidArgumentException If $handler is not a Google Cloud Storage
   * file pointer resource.
   *
   * @access private
   */
  private static function getStreamWrapperFromFileHandle($handle) {
    $wrapper = stream_get_meta_data($handle)['wrapper_data'];
    if (!$wrapper instanceof CloudStorageStreamWrapper) {
      throw new \InvalidArgumentException(
          '$handle must be a Google Cloud Storage file pointer resource');
    }

    return $wrapper;
  }

  /**
   * Convert an BlobstoreServiceError returned from an RPC call to an
   * exception.
   *
   * @param int $error A BlobstoreServiceError::ErrorCode error value.
   *
   * @return Exception The exception that corresponds to the error code.
   *
   * @access private
   */
  private static function applicationErrorToException($error) {
    switch($error->getApplicationError()) {
      case ErrorCode::URL_TOO_LONG:
        $result = new \InvalidArgumentException(
            'The upload URL supplied was too long.');
        break;
      case ErrorCode::PERMISSION_DENIED:
        $result = new CloudStorageException('Permission Denied');
        break;
      case ErrorCode::ARGUMENT_OUT_OF_RANGE:
        $result = new \InvalidArgumentException($error->getMessage());
        break;
      default:
        $result = new CloudStorageException(
            'Error Code: ' . $error->getApplicationError());
    }
    return $result;
  }

  /**
   * Convert an ImagesServiceError returned from an RPC call to an exception.
   *
   * @param int $error A ImagesServiceError::ErrorCode error value.
   *
   * @return Exception The exception that corresponds to the error code.
   *
   * @access private
   */
  private static function imagesApplicationErrorToException($error) {
    switch($error->getApplicationError()) {
      case ImagesServiceError\ErrorCode::UNSPECIFIED_ERROR:
        $result = new CloudStorageException('Unspecified error with image.');
        break;
      case ImagesServiceError\ErrorCode::BAD_TRANSFORM_DATA:
        $result = new CloudStorageException('Bad image transform data.');
        break;
      case ImagesServiceError\ErrorCode::NOT_IMAGE:
        $result = new CloudStorageException('Not an image.');
        break;
      case ImagesServiceError\ErrorCode::BAD_IMAGE_DATA:
        $result = new CloudStorageException('Bad image data.');
        break;
      case ImagesServiceError\ErrorCode::IMAGE_TOO_LARGE:
        $result = new CloudStorageException('Image too large.');
        break;
      case ImagesServiceError\ErrorCode::INVALID_BLOB_KEY:
        $result = new CloudStorageException('Invalid blob key for image.');
        break;
      case ImagesServiceError\ErrorCode::ACCESS_DENIED:
        $result = new CloudStorageException('Access denied to image.');
        break;
      case ImagesServiceError\ErrorCode::OBJECT_NOT_FOUND:
        $result = new CloudStorageException('Image object not found.');
        break;
      default:
        $result = new CloudStorageException(
            'Images Error Code: ' . $error->getApplicationError());
    }
    return $result;
  }

  /**
   * Check that the values for a Range: header are valid.
   *
   * @param int $start The start value for the range header.
   * @param int $end The end value for the range header.
   * @param bool $use_range Use the range header supplied in $range_header after
   *     checking that is is valid.
   * @param string $range_header The requests range header.
   *
   * @returns mixed The string range header to use, or null if there is no range
   *     header to use.
   *
   * @throws \InvalidArgumentException If the range header values are invalid.
   *
   * @access private
   */
  private static function checkRanges($start, $end, $use_range, $range_header) {
    if ($end && !$start) {
      throw new \InvalidArgumentException(
        "May not specify an end range value without a start value.");
    }

    $use_indexes = isset($start);
    if ($use_indexes) {
      if (isset($end)) {
        if ($start > $end) {
          throw new \InvalidArgumentException(
              sprintf(
                  "Start range (%d) cannot be greater than the end range (%d).",
                  $start,
                  $end));
        }
        if ($start < 0) {
          throw new \InvalidArgumentException(
              sprintf("The start range (%d) cannot be less than 0.", $start));
        }
      }
      $range_indexes = self::serializeRange($start, $end);
    }

    // If both headers and index parameters are in use they must be the same.
    if ($use_range && $use_indexes) {
      if (strcmp($range_header, $range_indexes) != 0) {
        throw new \InvalidArgumentException(
            sprintf("May not provide non-equivalent range indexes and " .
                    "range headers: (header) %s != (indexes) %s.",
                    $range_header,
                    $range_indexes));
      }
    }

    if ($use_range && isset($range_header)) {
      return $range_header;
    } else if ($use_indexes) {
      return $range_indexes;
    } else {
      return null;
    }
  }

  /**
   * Create the value for a range header from start and end byte values.
   *
   * @param int $start The start byte for the range header.
   * @param mixed $end The end byte for the range header
   *
   * @return string The range header value.
   *
   * @access private
   */
  private static function serializeRange($start, $end) {
    if ($start < 0) {
      $range_str = sprintf('%d', $start);
    } else if (!isset($end)) {
      $range_str = sprintf("%d-", $start);
    } else {
      $range_str = sprintf("%d-%d", $start, $end);
    }
    return sprintf("bytes=%s", $range_str);
  }

  /**
   * SendHeader hook, used for testing.
   *
   * @param string $key The header key.
   * @param string $value The header value.
   *
   * @access private
   */
  private static function sendHeader($key, $value) {
    if (isset(self::$send_header)) {
      call_user_func(self::$send_header, $key, $value);
    } else {
      header(sprintf("%s: %s", $key, $value));
    }
  }

  /**
   * Convert the upload_max_filesize ini setting to a number of bytes.
   *
   * @return int The value of the upload_max_filesize in bytes.
   *
   * @access private
   */
  private static function getUploadMaxFileSizeInBytes() {
    $val = trim(ini_get('upload_max_filesize'));
    $unit = strtolower(substr($val, -1));
    switch ($unit) {
      case 'g':
        $val *= 1024;
        // Fall through
      case 'm':
        $val *= 1024;
        // Fall through
      case 'k':
        $val *= 1024;
        break;
    }
    return intval($val);
  }

  /**
   * Determine if the code is executing on the development server.
   *
   * @return bool True if running in the developement server, false otherwise.
   *
   * @access private
   */
  private static function isDevelServer() {
    $server_software = getenv("SERVER_SOFTWARE");
    $key = "Development";
    return strncmp($server_software, $key, strlen($key)) === 0;
  }
}
