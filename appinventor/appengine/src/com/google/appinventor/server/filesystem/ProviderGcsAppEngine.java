package com.google.appinventor.server.filesystem;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.appidentity.AppIdentityServiceFailureException;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StoredDataRoleEnum;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ProviderGcsAppEngine extends FilesystemService {
  private static final boolean DEBUG = Flag.createFlag("appinventor.debugging", false).get();
  private static final String GCS_BUCKET_NAME;
  private static final String APK_BUCKET_NAME;

  private static final Logger LOG = Logger.getLogger(ProviderGcsAppEngine.class.getName());

  private final GcsService gcsService;

  static {
    String gcsBucketName = Flag.createFlag("gcs.bucket", "").get();
    if (gcsBucketName.isBlank()) { // Attempt to get default bucket
      // from AppIdentity Service
      AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
      try {
        gcsBucketName = appIdentity.getDefaultGcsBucketName();
      } catch (AppIdentityServiceFailureException e) {
        // We get this exception when we are running on an App Engine instance
        // created before App Engine version 1.9.0 and we have neither configured
        // the GCS bucket in appengine-web.xml or used the App Engine console to
        // create the default bucket. The Default Bucket is a better approach for
        // personal instances because they have a default free quota of 5 Gb (as
        // of 5/29/2015 when this code was written).
        throw new RuntimeException("You need to configure the default GCS Bucket for your App. " +
            "Follow instructions in the App Engine Developer's Documentation");
      }
      LOG.log(Level.INFO, "Default GCS Bucket Configured from App Identity: " + gcsBucketName);
    }

    GCS_BUCKET_NAME = gcsBucketName;
    APK_BUCKET_NAME = Flag.createFlag("gcs.apkbucket", gcsBucketName).get();
  }

  public ProviderGcsAppEngine() {
    RetryParams retryParams = new RetryParams.Builder().initialRetryDelayMillis(100)
        .retryMaxAttempts(10)
        .totalRetryPeriodMillis(10000).build();
    if (DEBUG) {
      LOG.log(Level.INFO, "RetryParams: getInitialRetryDelayMillis() = " + retryParams.getInitialRetryDelayMillis());
      LOG.log(Level.INFO, "RetryParams: getRequestTimeoutMillis() = " + retryParams.getRequestTimeoutMillis());
      LOG.log(Level.INFO, "RetryParams: getRetryDelayBackoffFactor() = " + retryParams.getRetryDelayBackoffFactor());
      LOG.log(Level.INFO, "RetryParams: getRetryMaxAttempts() = " + retryParams.getRetryMaxAttempts());
      LOG.log(Level.INFO, "RetryParams: getRetryMinAttempts() = " + retryParams.getRetryMinAttempts());
      LOG.log(Level.INFO, "RetryParams: getTotalRetryPeriodMillis() = " + retryParams.getTotalRetryPeriodMillis());
    }

    this.gcsService = GcsServiceFactory.createGcsService(retryParams);
  }

  @Override
  public int save(final StoredDataRoleEnum role, final String objectKey, final byte[] content) throws IOException {
    GcsOutputChannel outputChannel = gcsService.createOrReplace(new GcsFilename(getGcsBucketToUse(role), objectKey),
        GcsFileOptions.getDefaultInstance());
    int result = outputChannel.write(ByteBuffer.wrap(content));
    outputChannel.close();
    return result;
  }

  @Override
  public byte[] read(final StoredDataRoleEnum role, final String objectKey) throws IOException {
    GcsFilename gcsFileName = new GcsFilename(getGcsBucketToUse(role), objectKey);

    try {
      int fileSize = (int) gcsService.getMetadata(gcsFileName).getLength();
      ByteBuffer resultBuffer = ByteBuffer.allocate(fileSize);

      try (GcsInputChannel readChannel = gcsService.openReadChannel(gcsFileName, 0)) {
        int bytesRead = 0;
        while (bytesRead < fileSize) {
          bytesRead += readChannel.read(resultBuffer);
          if (bytesRead < fileSize) {
            if (DEBUG) {
              LOG.log(Level.INFO, "readChannel: bytesRead = " + bytesRead + " fileSize = " + fileSize);
            }
          }
        }
      }

      return resultBuffer.array();
    } catch (NullPointerException e) {
      LOG.log(Level.WARNING, "downloadrawfile: NPF recorded for " + objectKey);
      return new byte[0];
    }
  }

  @Override
  public boolean delete(final StoredDataRoleEnum role, final String fileName) throws IOException {
    return gcsService.delete(new GcsFilename(getGcsBucketToUse(role), fileName));
  }

  /**
   * Determine which GCS Bucket to use based on filename. In particular
   * APK files go in a bucket with a short TTL, because they are really
   * temporary files.
   *
   * @param role the kind of file to store
   * @return the bucket name to use
   */
  private static String getGcsBucketToUse(StoredDataRoleEnum role) {
    if (role == StoredDataRoleEnum.TARGET) {
      return APK_BUCKET_NAME;
    } else {
      return GCS_BUCKET_NAME;
    }
  }
}
