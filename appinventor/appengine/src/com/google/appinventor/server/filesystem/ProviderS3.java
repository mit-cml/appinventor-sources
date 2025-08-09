package com.google.appinventor.server.filesystem;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StoredDataRoleEnum;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;


public final class ProviderS3 extends FilesystemService {
  private static final Logger LOG = Logger.getLogger(ProviderS3.class.getName());

  private static final Flag<String> ENDPOINT = Flag.createFlag("filesystem.s3.endpoint", null);
  private static final Flag<Boolean> ENDPOINT_PATH_STYLE = Flag.createFlag("filesystem.s3.endpointpathstyle", false);
  private static final Flag<String> BUCKET_NAME = Flag.createFlag("filesystem.s3.bucketname", null);
  private static final Flag<String> BUCKET_REGION = Flag.createFlag("filesystem.s3.bucketregion", null);
  private static final Flag<String> ACCESS_KEY_ID = Flag.createFlag("filesystem.s3.accesskeyid", null);
  private static final Flag<String> SECRET_ACCESS_KEY = Flag.createFlag("filesystem.s3.secretaccesskey", null);

  private final S3Client s3Client;
  private final String bucketName;

  public ProviderS3() {
    this.bucketName = BUCKET_NAME.get();
    if (this.bucketName == null || this.bucketName.isEmpty()) {
      throw new IllegalStateException("S3 bucket name must be configured");
    }

    this.s3Client = createS3Client();
  }

  private S3Client createS3Client() {
    String accessKeyId = ACCESS_KEY_ID.get();
    String secretAccessKey = SECRET_ACCESS_KEY.get();
    String region = BUCKET_REGION.get();
    String endpoint = ENDPOINT.get();
    boolean endpointPathStyle = ENDPOINT_PATH_STYLE.get();

    AwsCredentialsProvider credentialsProvider;
    if (accessKeyId != null && !accessKeyId.isEmpty() &&
        secretAccessKey != null && !secretAccessKey.isEmpty()) {
      AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
      credentialsProvider = StaticCredentialsProvider.create(credentials);
      LOG.info("Using static AWS credentials");
    } else {
      credentialsProvider = DefaultCredentialsProvider.builder().build();
      LOG.info("Using default AWS credentials provider chain (session/role/profile credentials)");
    }

    S3ClientBuilder clientBuilder = S3Client.builder()
        .credentialsProvider(credentialsProvider);

    if (region != null && !region.isEmpty()) {
      clientBuilder = clientBuilder.region(Region.of(region));
    }

    if (endpoint != null && !endpoint.isEmpty()) {
      clientBuilder = clientBuilder.endpointOverride(URI.create(endpoint));
    }

    if (endpointPathStyle) {
      clientBuilder = clientBuilder.serviceConfiguration(
          S3Configuration.builder()
              .pathStyleAccessEnabled(true)
              .build()
      );
    }

    return clientBuilder.build();
  }

  @Override
  public int save(final StoredDataRoleEnum role, final String objectKey, final byte[] content) throws IOException {
    try {
      PutObjectRequest putRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(objectKey)
          .contentLength((long) content.length)
          .build();

      s3Client.putObject(putRequest, RequestBody.fromBytes(content));

      LOG.info("Successfully saved object: " + objectKey + " (" + content.length + " bytes)");
      return content.length;
    } catch (S3Exception e) {
      LOG.log(Level.SEVERE, "Failed to save object to S3: " + objectKey, e);
      throw new IOException("Failed to save object to S3", e);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error saving to S3: " + objectKey, e);
      throw new IOException("Failed to save object to S3", e);
    }
  }

  @Override
  public byte[] read(final StoredDataRoleEnum role, final String objectKey) throws IOException {
    try {
      GetObjectRequest getRequest = GetObjectRequest.builder()
          .bucket(bucketName)
          .key(objectKey)
          .build();

      byte[] data = s3Client.getObject(getRequest, ResponseTransformer.toBytes()).asByteArray();

      LOG.info("Successfully read object: " + objectKey + " (" + data.length + " bytes)");
      return data;

    } catch (NoSuchKeyException e) {
      LOG.warning("Object not found in S3: " + objectKey);
      return null;
    } catch (S3Exception e) {
      LOG.log(Level.SEVERE, "Failed to read object from S3: " + objectKey, e);
      throw new IOException("Failed to read object from S3", e);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error reading from S3: " + objectKey, e);
      throw new IOException("Failed to read object from S3", e);
    }
  }

  @Override
  public boolean delete(final StoredDataRoleEnum role, final String fileName) throws IOException {
    try {
      DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(fileName)
          .build();

      s3Client.deleteObject(deleteRequest);

      LOG.info("Successfully deleted object: " + fileName);
      return true;

    } catch (S3Exception e) {
      LOG.log(Level.SEVERE, "Failed to delete object from S3: " + fileName, e);
      throw new IOException("Failed to delete object from S3", e);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error deleting from S3: " + fileName, e);
      throw new IOException("Failed to delete object from S3", e);
    }
  }
}
