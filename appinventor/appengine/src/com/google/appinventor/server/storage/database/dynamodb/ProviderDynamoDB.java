package com.google.appinventor.server.storage.database.dynamodb;

import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.ErrorUtils;
import com.google.appinventor.server.storage.database.DatabaseService;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.user.SplashConfig;
import com.google.appinventor.shared.rpc.user.User;
import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughputExceededException;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class ProviderDynamoDB extends DatabaseService {
  private static final Logger LOG = Logger.getLogger(ProviderDynamoDB.class.getName());

  private static final int MAX_JOB_RETRIES = 10;
  private static final long BASE_DELAY_MS = 25;     // Start with 25ms
  private static final long MAX_DELAY_MS = 5000;   // Cap at 5 seconds
  private static final double JITTER_FACTOR = 0.1;  // 10% jitter

  private static final Random RANDOM = new Random();

  private static final Flag<String> ENDPOINT = Flag.createFlag("database.ddb.endpoint", null);
  private static final Flag<String> TABLES_REGION = Flag.createFlag("filesystem.s3.tablesregion", null);
  private static final Flag<String> ACCESS_KEY_ID = Flag.createFlag("database.ddb.accesskeyid", null);
  private static final Flag<String> SECRET_ACCESS_KEY = Flag.createFlag("database.ddb.secretaccesskey", null);

  private final DynamoDbTable<StoredData.UserData> userDataTable;
  private final DynamoDbTable<StoredData.ProjectData> projectDataTable;
  private final DynamoDbTable<StoredData.UserProjectData> userProjectDataTable;
  private final DynamoDbTable<StoredData.WhiteListData> whiteListDataTable;
  private final DynamoDbTable<StoredData.FeedbackData> feedbackDataTable;
  private final DynamoDbTable<StoredData.NonceData> nonceDataTable;
  private final DynamoDbTable<StoredData.CorruptionRecord> corruptionRecordTable;
  private final DynamoDbTable<StoredData.SplashData> splashDataTable;
  private final DynamoDbTable<StoredData.PWData> pwDataTable;
  private final DynamoDbTable<StoredData.Backpack> backpackTable;
  private final DynamoDbTable<StoredData.AllowedTutorialUrls> allowedTutorialUrlsTable;
  private final DynamoDbTable<StoredData.AllowedIosExtensions> allowedIosExtensionsTable;

  public ProviderDynamoDB() {
    final DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
        .dynamoDbClient(createDynamoDbClient())
        .build();

    userDataTable = enhancedClient.table("UserData", TableSchema.fromBean(StoredData.UserData.class));
    projectDataTable = enhancedClient.table("ProjectData", TableSchema.fromBean(StoredData.ProjectData.class));
    userProjectDataTable = enhancedClient.table("UserProjectData", TableSchema.fromBean(StoredData.UserProjectData.class));
    whiteListDataTable = enhancedClient.table("WhiteListData", TableSchema.fromBean(StoredData.WhiteListData.class));
    feedbackDataTable = enhancedClient.table("FeedbackData", TableSchema.fromBean(StoredData.FeedbackData.class));
    nonceDataTable = enhancedClient.table("NonceData", TableSchema.fromBean(StoredData.NonceData.class));
    corruptionRecordTable = enhancedClient.table("CorruptionRecord", TableSchema.fromBean(StoredData.CorruptionRecord.class));
    splashDataTable = enhancedClient.table("SplashData", TableSchema.fromBean(StoredData.SplashData.class));
    pwDataTable = enhancedClient.table("PWData", TableSchema.fromBean(StoredData.PWData.class));
    backpackTable = enhancedClient.table("Backpack", TableSchema.fromBean(StoredData.Backpack.class));
    allowedTutorialUrlsTable = enhancedClient.table("AllowedTutorialUrls", TableSchema.fromBean(StoredData.AllowedTutorialUrls.class));
    allowedIosExtensionsTable = enhancedClient.table("AllowedIosExtensions", TableSchema.fromBean(StoredData.AllowedIosExtensions.class));
  }

  private DynamoDbClient createDynamoDbClient() {
    String accessKeyId = ACCESS_KEY_ID.get();
    String secretAccessKey = SECRET_ACCESS_KEY.get();
    String region = TABLES_REGION.get();
    String endpoint = ENDPOINT.get();

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

    DynamoDbClientBuilder clientBuilder = DynamoDbClient.builder()
        .credentialsProvider(credentialsProvider);

    if (region != null && !region.isEmpty()) {
      clientBuilder = clientBuilder.region(Region.of(region));
    }

    if (endpoint != null && !endpoint.isEmpty()) {
      clientBuilder = clientBuilder.endpointOverride(URI.create(endpoint));
    }

    return clientBuilder.build();
  }


  @Override
  public User findOrCreateUser(final String userId, final String email, final boolean requireTos) {
    final AtomicReference<StoredData.UserData> finalUserData = new AtomicReference<>();

    final String emailLower = email == null ? null : email.toLowerCase();

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.UserData userData = userDataTable.getItem(partitionKey(userId));
          if (userData == null) { // Attempt to find them by email
            LOG.info("Did not find userId " + userId);
            if (emailLower != null) {
              SdkIterable<Page<StoredData.UserData>> pagedResult = userDataTable.index("EmailIndex")
                  .query(QueryConditional.keyEqualTo(k -> k.partitionValue(emailLower)));

              userData = getItemIndex(pagedResult);
            }

            if (userData == null) { // No joy, create it.
              userData = createUser(userId, emailLower);
            }
          } else if (emailLower != null && !emailLower.equals(userData.getEmail())) {
            userData.setEmail(emailLower);
            userDataTable.updateItem(UpdateItemEnhancedRequest.builder(StoredData.UserData.class)
                .item(userData)
                .build());
          }

          finalUserData.set(userData);
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }

    final User user = new User(userId, email, false, false, null);
    final StoredData.UserData userData = finalUserData.get();
    user.setUserId(userData.getId());
    user.setUserEmail(userData.getEmail());
    user.setUserTosAccepted(userData.getTosAccepted() || !requireTos);
    user.setIsAdmin(userData.getIsAdmin());
    user.setSessionId(userData.getSessionId());
    user.setPassword(userData.getPassword());

    return user;
  }

  private StoredData.UserData createUser(String userId, String email) {
    StoredData.UserData userData = new StoredData.UserData();
    userData.setId(userId);
    userData.setTosAccepted(false);
    userData.setSettings("");
    // This might be null intentionally, because DDB does not like empty strings
    //   on global secondary indexes, but they are fine with nulls...
    userData.setEmail(email);

    userDataTable.putItem(userData);

    return userData;
  }

  @Override
  public User getUserFromEmail(final String email, final boolean create) {
    final String emailLower = email.toLowerCase();
    String newId = UUID.randomUUID().toString();
    // First try lookup using entered case (which will be the case for Google Accounts)
    SdkIterable<Page<StoredData.UserData>> pagedResult = userDataTable.index("EmailIndex")
        .query(QueryConditional.keyEqualTo(k -> k.partitionValue(emailLower)));

    StoredData.UserData user = getItemIndex(pagedResult);
    if (user == null) {       // Finally, create it (in lower case)
      LOG.info("getUserFromEmail: attempt failed using " + emailLower);

      if (create) {
        user = createUser(newId, email);
      }
    }

    if (user == null) {
      // Only happens when create is false and we didn't find it
      return null;
    }

    User retUser = new User(user.getId(), email, user.getTosAccepted(), false, user.getSessionId());
    retUser.setPassword(user.getPassword());
    return retUser;
  }

  @Override
  public void setTosAccepted(final String userId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.UserData userData = userDataTable.getItem(partitionKey(userId));
          if (userData != null) {
            userData.setTosAccepted(true);
            userDataTable.updateItem(userData);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void setUserEmail(final String userId, final String email) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.UserData userData = userDataTable.getItem(partitionKey(userId));
          if (userData != null) {
            userData.setEmail(email.toLowerCase());
            userDataTable.updateItem(userData);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void setUserSessionId(final String userId, final String sessionId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.UserData userData = userDataTable.getItem(partitionKey(userId));
          if (userData != null) {
            userData.setSessionId(sessionId);
            userDataTable.updateItem(userData);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void setUserPassword(final String userId, final String password) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.UserData userData = userDataTable.getItem(partitionKey(userId));
          if (userData != null) {
            userData.setPassword(password);
            userDataTable.updateItem(userData);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public String loadUserDataSettings(final String userId) {
    final AtomicReference<String> settings = new AtomicReference<>("");

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.UserData userData = userDataTable.getItem(partitionKey(userId));
          if (userData != null) {
            settings.set(userData.getSettings());
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }

    return settings.get();
  }

  @Override
  public void storeUserDataSettings(final String userId, final String settings) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.UserData userData = userDataTable.getItem(partitionKey(userId));
          if (userData != null) {
            userData.setSettings(settings);
            userData.setVisited(Instant.now()); // Indicate that this person was active now
            userDataTable.updateItem(userData);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void storeCorruptionRecord(String userId, long projectId, String fileId, String message) {
    final StoredData.CorruptionRecord data = new StoredData.CorruptionRecord();
    data.setTimestamp(Instant.now());
    data.setId(UUID.randomUUID().toString());
    data.setUserId(userId);
    data.setFileId(fileId);
    data.setProjectId(projectId);
    data.setMessage(message);

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          corruptionRecordTable.putItem(data);
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
  }

  @Override
  public boolean isEmailAddressInAllowlist(final String emailAddress) {
    StoredData.WhiteListData data = whiteListDataTable.getItem(partitionKey(emailAddress.toLowerCase()));
    return data != null;
  }

  @Override
  public void storeFeedbackData(final String notes, final String foundIn, final String faultData,
                                final String comments, final String datestamp, final String email, final String projectId) {
    StoredData.FeedbackData data = new StoredData.FeedbackData();
    data.setId(UUID.randomUUID().toString());
    data.setNotes(notes);
    data.setFoundIn(foundIn);
    data.setFaultData(faultData);
    data.setComments(comments);
    data.setDatestamp(datestamp);
    data.setEmail(email.toLowerCase());
    data.setProjectId(projectId);

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          feedbackDataTable.putItem(data);
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
  }

  @Override
  public void storeNonce(final String nonceValue, final String userId, final long projectId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.NonceData data = nonceDataTable.getItem(partitionKey(nonceValue));
          if (data == null) {
            data = new StoredData.NonceData();
            data.setNonce(nonceValue);
          }
          data.setUserId(userId);
          data.setProjectId(projectId);
          data.setTimestamp(Instant.now());
          data.setValidUntil(data.getTimestamp().plusMillis(PWDATA_EXPIRATION_TIME_MS));
          nonceDataTable.putItem(data);
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
  }

  @Override
  public Nonce getNonceByValue(String nonceValue) {
    StoredData.NonceData data = nonceDataTable.getItem(partitionKey(nonceValue));
    if (data == null) {
      return null;
    }

    if (data.getValidUntil().isBefore(Instant.now())) {
      // Expired, manually delete it as TTL cleanup did not trigger yet
      nonceDataTable.deleteItem(data);
      return null;
    }

    return new Nonce(nonceValue, data.getUserId(), data.getProjectId(), new Date(data.getTimestamp().toEpochMilli()));
  }

  @Override
  public void cleanupNonces() {
    // We do not perform manual cleanup of old PWData records in DDB, as it is taken by automatic TTL
    throw new UnsupportedOperationException("cleanupNonces is not supported in DynamoDB");
  }

  @Override
  public String createPWData(final String email) {
    final String uuid = UUID.randomUUID().toString();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          final StoredData.PWData pwData = new StoredData.PWData();
          pwData.setId(uuid);
          pwData.setEmail(email.toLowerCase());
          pwData.setTimestamp(Instant.now());
          pwData.setValidUntil(pwData.getTimestamp().plusMillis(PWDATA_EXPIRATION_TIME_MS));
          pwDataTable.putItem(pwData);
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
    return uuid;
  }

  @Override
  public String getPWData(final String uid) {
    final AtomicReference<StoredData.PWData> result = new AtomicReference<>(null);
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          result.set(pwDataTable.getItem(partitionKey(uid)));
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }

    final StoredData.PWData pwData = result.get();
    if (pwData == null) {
      return null;
    }
    if (pwData.getValidUntil().isBefore(Instant.now())) {
      // Expired, manually delete it as TTL cleanup did not trigger yet
      pwDataTable.deleteItem(pwData);
      return null;
    }

    return pwData.getEmail();
  }

  @Override
  public void cleanupPWDatas() {
    // We do not perform manual cleanup of old PWData records in DDB, as it is taken by automatic TTL
    throw new UnsupportedOperationException("cleanupPWDatas is not supported in DynamoDB");
  }

  @Override
  public String getBackpack(final String backPackId) {
    final AtomicReference<StoredData.Backpack> result = new AtomicReference<>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.Backpack backPack = backpackTable.getItem(partitionKey(backPackId));
          if (backPack != null) {
            result.set(backPack);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }

    StoredData.Backpack backpack = result.get();
    if (backpack != null) {
      return backpack.getContent();
    } else {
      return "[]";              // No shared backpack, return an empty backpack
    }
  }

  @Override
  public void storeBackpack(String backPackId, String content) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          final StoredData.Backpack backPack = new StoredData.Backpack();
          backPack.setId(backPackId);
          backPack.setContent(content);
          backpackTable.putItem(backPack);
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
  }

  @Override
  public boolean assertUserIdOwnerOfProject(final String userId, final long projectId) {
    final AtomicReference<Boolean> ownsProject = new AtomicReference<>(false);
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.UserProjectData data = userProjectDataTable.getItem(partitionAndSortKey(userId, projectId));
          if (data != null) {  // User doesn't have the corresponding project.
            ownsProject.set(true);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }

    return ownsProject.get();
  }

  @Override
  public String getAllowedIosExtensions(final Long allowedIosExtensionsId) {
    final AtomicReference<String> result = new AtomicReference<>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.AllowedIosExtensions iosSettingsData = allowedIosExtensionsTable.getItem(partitionKey(allowedIosExtensionsId));
          if (iosSettingsData != null) {
            result.set(iosSettingsData.getAllowedExtensions());
          } else {
            StoredData.AllowedIosExtensions firstIosSettingsData = new StoredData.AllowedIosExtensions();
            firstIosSettingsData.setId(allowedIosExtensionsId);
            firstIosSettingsData.setAllowedExtensions("[]");
            allowedIosExtensionsTable.putItem(firstIosSettingsData);
            result.set(firstIosSettingsData.getAllowedExtensions());
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }

    return result.get();
  }

  @Override
  public SplashConfig getSplashConfig(final Long splashConfigId) {
    final AtomicReference<SplashConfig> result = new AtomicReference<>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          // Fixed key because only one record
          StoredData.SplashData sd = splashDataTable.getItem(partitionKey(splashConfigId));
          SplashConfig splashConfig;
          if (sd == null) {   // If we don't have Splash Data, create it
            StoredData.SplashData firstSd = new StoredData.SplashData(); // We do this so cacheing works
            firstSd.setId(splashConfigId);
            firstSd.setVersion(0);                   // on future calls
            firstSd.setContent("<b>Welcome to MIT App Inventor</b>");
            firstSd.setWidth(350);
            firstSd.setHeight(100);
            splashDataTable.putItem(firstSd);
            splashConfig = new SplashConfig(0, firstSd.getWidth(), firstSd.getHeight(), firstSd.getContent());
          } else {
            splashConfig = new SplashConfig(sd.getVersion(), sd.getWidth(), sd.getHeight(), sd.getContent());
          }
          result.set(splashConfig);
        }
      });             // No transaction, Objectify will cache
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
    return result.get();
  }

  @Override
  public boolean isProjectInTrash(final Long projectId) {
    StoredData.ProjectData projectData = projectDataTable.getItem(partitionKey(projectId));
    if (projectData == null) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(null, projectId),
          new IllegalArgumentException("Project " + projectId + " doesn't exist"));
    }

    return projectData.isProjectMovedToTrash();
  }

  @Override
  public void deleteUser(final String userId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          userDataTable.deleteItem(partitionKey(userId));
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }

  }

  @Override
  public String getAllowedTutorialUrls(final Long allowedTutorialUrlsId) {
    final AtomicReference<String> result = new AtomicReference<>("[]");
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.AllowedTutorialUrls allowedUrls = allowedTutorialUrlsTable.getItem(partitionKey(allowedTutorialUrlsId));
          if (allowedUrls != null) { // This shouldn't be
            result.set(allowedUrls.getAllowedUrls());
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }

    return result.get();
  }

  private <T> T getItemIndex(SdkIterable<Page<T>> pagedResult) {
    AtomicReference<T> result = new AtomicReference<>();

    pagedResult.stream().forEach(page -> page.items().stream()
        .limit(1)
        .forEach(result::set));

    return result.get();
  }

  @VisibleForTesting
  private abstract static class JobRetryHelper {
    private IOException exception = null;

    public abstract void run() throws DynamoException, IOException;

    /*
     * Called before retrying the job. Note that the underlying datastore
     * still has the transaction active, so restrictions about operations
     * over multiple entity groups still apply.
     */
    public void onNonFatalError() {
      // Default is to do nothing
    }

    public void onIOException(IOException error) {
      exception = error;
    }

    public IOException getIOException() {
      return exception;
    }
  }

  /**
   * AWS recommended exponential backoff with jitter
   * Formula: min(base * 2^attempt + jitter, maxDelay)
   */
  private static long calculateBackoffDelay(int attempt) {
    // Exponential backoff: 25ms, 50ms, 100ms, 200ms, 400ms, 800ms, 1600ms, 3200ms, 6400ms, 12800ms, 25600ms
    long exponentialDelay = BASE_DELAY_MS * (1L << (attempt - 1));

    // Apply jitter (±10% random variation)
    double jitter = (RANDOM.nextDouble() - 0.5) * 2 * JITTER_FACTOR;
    long delayWithJitter = Math.round(exponentialDelay * (1 + jitter));

    // Cap at maximum delay
    return Math.min(delayWithJitter, MAX_DELAY_MS);
  }

  @VisibleForTesting
  private void runJobWithRetries(JobRetryHelper job) throws DynamoException {
    int tries = 0;
    while (tries <= MAX_JOB_RETRIES) {
      try {
        job.run();
        break;
      } catch (ProvisionedThroughputExceededException ex) {
        job.onNonFatalError();
        LOG.log(Level.WARNING, "Throughput exceeded", ex);
        try {
          Thread.sleep(calculateBackoffDelay(tries));
        } catch (InterruptedException ignored) {
          // We don't care if sleep is interrupted
        }
      } catch (ConditionalCheckFailedException ex) {
        job.onNonFatalError();
        LOG.log(Level.WARNING, "Optimistic concurrency failure", ex);
      } catch (DynamoException oe) {
        String message = oe.getMessage();
        if (message != null &&
            (message.startsWith("Blocks") || message.startsWith("User Al"))) { // This one is fatal!
          throw oe;
        }

        // maybe this should be a fatal error? I think only thing
        // that creates this exception is this method.
        job.onNonFatalError();
      } catch (DynamoDbException e) {
        LOG.severe("Found non-retryable DynamoDbException: " + e.getMessage());
        throw e;
      } catch (IOException e) {
        job.onIOException(e);
        break;
      }

      tries++;
    }

    if (tries > MAX_JOB_RETRIES) {
      throw new DynamoException("Couldn't finish job after max retries");
    }
  }

  private Key partitionKey(final String str) {
    return Key.builder().partitionValue(str).build();
  }

  private Key partitionKey(final Long lng) {
    return Key.builder().partitionValue(lng).build();
  }

  private Key partitionAndSortKey(final String str1, final Long lng2) {
    return Key.builder().partitionValue(str1).sortValue(lng2).build();
  }
}
