package com.google.appinventor.server.storage.database.dynamodb;

import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.ErrorUtils;
import com.google.appinventor.server.storage.FileDataRoleEnum;
import com.google.appinventor.server.storage.UnauthorizedAccessException;
import com.google.appinventor.server.storage.UnifiedFile;
import com.google.appinventor.server.storage.database.DatabaseAccessException;
import com.google.appinventor.server.storage.database.DatabaseService;
import com.google.appinventor.shared.rpc.AdminInterfaceException;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.admin.AdminUser;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.user.SplashConfig;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.base.Preconditions;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughputExceededException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


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

  private final DynamoDbEnhancedClient enhancedClient;
  private final DynamoDbTable<StoredData.UserData> userDataTable;
  private final DynamoDbTable<StoredData.ProjectData> projectDataTable;
  private final DynamoDbTable<StoredData.UserProjectData> userProjectDataTable;
  private final DynamoDbTable<StoredData.UserFileData> userFileDataTable;
  private final DynamoDbTable<StoredData.FileData> fileDataTable;
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
    enhancedClient = DynamoDbEnhancedClient.builder()
        .dynamoDbClient(createDynamoDbClient())
        .build();

    userDataTable = enhancedClient.table("UserData", TableSchema.fromBean(StoredData.UserData.class));
    projectDataTable = enhancedClient.table("ProjectData", TableSchema.fromBean(StoredData.ProjectData.class));
    userProjectDataTable = enhancedClient.table("UserProjectData", TableSchema.fromBean(StoredData.UserProjectData.class));
    userFileDataTable = enhancedClient.table("UserFileData", TableSchema.fromBean(StoredData.UserFileData.class));
    fileDataTable = enhancedClient.table("FileData", TableSchema.fromBean(StoredData.FileData.class));
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

  private StoredData.UserData findUserDataByEmail(final String emailLower) {
    SdkIterable<Page<StoredData.UserData>> pagedResult = userDataTable.index("EmailIndex")
        .query(QueryConditional.keyEqualTo(k -> k.partitionValue(emailLower)));

    return getItemIndex(pagedResult);
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
              userData = findUserDataByEmail(emailLower);
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
    StoredData.UserData user = findUserDataByEmail(emailLower);
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
  public List<AdminUser> searchUsers(final String partialEmail) {
    throw new UnsupportedOperationException("searchUsers is not supported in DynamoDB");
  }

  @Override
  public void storeUser(final AdminUser user) throws AdminInterfaceException {
    final String emailLower = user.getEmail().toLowerCase();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() throws DynamoException {
          StoredData.UserData userData = null;
          if (user.getId() != null) {
            userData = userDataTable.getItem(partitionKey(user.getId()));
          }

          if (userData != null) {
            userData.setEmail(emailLower);
            String password = user.getPassword();
            if (password != null && !password.isEmpty()) {
              userData.setPassword(user.getPassword());
            }
            userData.setIsAdmin(user.getIsAdmin());
            userDataTable.updateItem(userData);
          } else {            // New User
            StoredData.UserData tuser = findUserDataByEmail(emailLower);
            if (tuser != null) {
              // This is a total kludge, but we have to do things this way because of
              // how runJobWithRetries works
              throw new DynamoException("User Already exists = " + user.getEmail());
            }
            userData = new StoredData.UserData();
            userData.setId(UUID.randomUUID().toString());
            userData.setTosAccepted(false);
            userData.setSettings("");
            userData.setEmail(emailLower);
            if (!user.getPassword().isEmpty()) {
              userData.setPassword(user.getPassword());
            }
            userData.setIsAdmin(user.getIsAdmin());
            userDataTable.putItem(userData);
          }
        }
      });
    } catch (DynamoException e) {
      if (e.getMessage().startsWith("User Al")) {
        throw new AdminInterfaceException(e.getMessage());
      }
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
  }

  @Override
  public Long createProjectData(final Project project, final String projectSettings) throws DatabaseAccessException {
    final AtomicReference<Long> projectId = new AtomicReference<>(null);

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          Instant date = Instant.now();
          StoredData.ProjectData pd = new StoredData.ProjectData();
          UUID uuid = UUID.randomUUID();
          long id = Math.abs(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());
          pd.setId(id);
          pd.setDateCreated(date);
          pd.setDateModified(date);
          pd.setDateBuilt(null);
          pd.setHistory(project.getProjectHistory());
          pd.setName(project.getProjectName());
          pd.setSettings(projectSettings);
          pd.setType(project.getProjectType());

          projectDataTable.putItem(PutItemEnhancedRequest.builder(StoredData.ProjectData.class)
              .item(pd)
              // Ensure the Project ID is not taken, otherwise retry
              .conditionExpression(Expression.builder()
                  .expression("attribute_not_exists(ProjectId)")
                  .build())
              .build());

          projectId.set(pd.getId());
        }
      });
    } catch (DynamoException e) {
      throw new DatabaseAccessException(e);
    }

    return projectId.get();
  }

  @Override
  public void createUserProjectData(final String userId, final Long projectId, final String projectSettings) throws DatabaseAccessException {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.UserProjectData upd = new StoredData.UserProjectData();
          upd.setProjectId(projectId);
          upd.setSettings(projectSettings);
          upd.setState(StoredData.UserProjectData.StateEnum.OPEN);
          upd.setUserKey(userId);
          userProjectDataTable.putItem(upd);
        }
      });
    } catch (DynamoException e) {
      throw new DatabaseAccessException(e);
    }
  }

  @Override
  public void createProjectFileData(final String userId, final Long projectId, final FileDataRoleEnum role,
                                    final List<UnifiedFile> files) throws DatabaseAccessException {
    final List<StoredData.FileData> addedFiles = new ArrayList<>();

    for (UnifiedFile unifiedFile : files) {
      StoredData.FileData file = new StoredData.FileData();
      file.setProjectKey(projectId);
      file.setFileName(unifiedFile.getFileName());
      file.setRole(role);
      file.setUserId(userId);
      if (unifiedFile.getFilesystemName() != null) {
        file.setIsFilesystem(true);
        file.setFilesystemName(unifiedFile.getFilesystemName());
      } else {
        file.setContent(unifiedFile.getContent());
      }

      addedFiles.add(file);
    }

    // Batch Write only accepts up to 25 items, but let's add some buffer so set it to 20
    for (int i = 0; i < addedFiles.size(); i += 20) {
      int endIndex = Math.min(i + 20, addedFiles.size());
      List<StoredData.FileData> batch = addedFiles.subList(i, endIndex);

      try {
        runJobWithRetries(new JobRetryHelper() {
          @Override
          public void run() {
            WriteBatch.Builder<StoredData.FileData> writeBatchBuilder = WriteBatch.builder(StoredData.FileData.class)
                .mappedTableResource(fileDataTable);

            for (StoredData.FileData file : batch) {
              writeBatchBuilder.addPutItem(file);
            }

            enhancedClient.batchWriteItem(BatchWriteItemEnhancedRequest.builder()
                .writeBatches(writeBatchBuilder.build())
                .build());
          }
        });
      } catch (DynamoException e) {
        throw new DatabaseAccessException(e);
      }
    }
  }

  public void deleteUserProject(final String userId, final Long projectId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          // delete the UserProjectData object
          userProjectDataTable.deleteItem(partitionAndSortKey(userId, projectId));
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
  }

  public List<String> deleteProjectData(final String userId, final Long projectId) {
    // blobs associated with the project
    final List<String> blobKeys = new ArrayList<>();
    final List<String> gcsPaths = new ArrayList<>();

    try {
      // entity group
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          PageIterable<StoredData.FileData> fdq = fileDataTable.query(QueryConditional.keyEqualTo(Key.builder()
              .partitionValue(projectId)
              .build()));

          for (StoredData.FileData fd : fdq.items()) {
            if (fd.isFilesystem()) {
              gcsPaths.add(fd.getFilesystemName());
            }
            fileDataTable.deleteItem(fd);
          }

          // finally, delete the ProjectData object
          projectDataTable.deleteItem(partitionKey(projectId));
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }

    // We send the gcs paths to be deleted by the filesystem service
    return gcsPaths;
  }

  @Override
  public void setProjectMovedToTrashFlag(final String userId, final long projectId, final boolean flag) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.ProjectData projectData = projectDataTable.getItem(partitionKey(projectId));
          if (projectData != null) {
            projectData.setProjectMovedToTrash(flag);
            projectDataTable.putItem(projectData);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public List<Long> getProjectIdsByUser(final String userId) {
    final List<Long> projects = new ArrayList<>();

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          PageIterable<StoredData.UserProjectData> fdq = userProjectDataTable.query(QueryConditional.keyEqualTo(Key.builder()
              .partitionValue(userId)
              .build()));

          for (StoredData.UserProjectData upd : fdq.items()) {
            projects.add(upd.getProjectId());
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }

    return projects;
  }

  @Override
  public String getProjectSettings(final String userId, final long projectId) {
    final AtomicReference<String> settings = new AtomicReference<>("");

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.ProjectData pd = projectDataTable.getItem(partitionKey(projectId));
          if (pd != null) {
            settings.set(pd.getSettings());
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }

    return settings.get();
  }

  @Override
  public void setProjectSettings(final String userId, final long projectId, final String projectSettings) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.ProjectData pd = projectDataTable.getItem(partitionKey(projectId));
          if (pd != null) {
            pd.setSettings(projectSettings);
            projectDataTable.putItem(pd);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
  }

  @Override
  public UserProject getUserProject(final String userId, final long projectId) {
    final AtomicReference<StoredData.ProjectData> projectData = new AtomicReference<>(null);

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.ProjectData pd = projectDataTable.getItem(partitionKey(projectId));
          if (pd != null) {
            projectData.set(pd);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }

    final StoredData.ProjectData projectDataVal = projectData.get();
    if (projectDataVal == null) {
      return null;
    }

    return mapProjectDataToUserProject(projectId, projectDataVal);
  }

  @Override
  public List<UserProject> getUserProjects(final String userId, final List<Long> projectIds) {
    final List<StoredData.ProjectData> projectDatas = new ArrayList<>();

    final Set<Long> remainingProjectIds = new HashSet<>(projectIds);

    while (!remainingProjectIds.isEmpty()) {
      try {
        runJobWithRetries(new JobRetryHelper() {
          @Override
          public void run() {
            // DDB BatchGetItem allows up to 100 elements
            Set<Long> elementsToGet = remainingProjectIds.stream().limit(100).collect(Collectors.toSet());

            ReadBatch.Builder<StoredData.ProjectData> readBatchBuilder = ReadBatch.builder(StoredData.ProjectData.class)
                .mappedTableResource(projectDataTable);

            for (Long projectId : elementsToGet) {
              readBatchBuilder.addGetItem(Key.builder().partitionValue(projectId).build());
            }

            BatchGetItemEnhancedRequest batchRequest = BatchGetItemEnhancedRequest.builder()
                .readBatches(readBatchBuilder.build())
                .build();

            BatchGetResultPageIterable results = enhancedClient.batchGetItem(batchRequest);

            for (BatchGetResultPage page : results) {
              for (Key key : page.unprocessedKeysForTable(projectDataTable)) {
                // Key failed to be processed, so let's remove it from the pending-to-be-analyzed set
                elementsToGet.remove(Long.valueOf(key.partitionKeyValue().n()));
              }

              List<StoredData.ProjectData> pageItems = page.resultsForTable(projectDataTable);
              for (StoredData.ProjectData pd : pageItems) {
                elementsToGet.remove(pd.getId());
                projectDatas.add(pd);
                remainingProjectIds.remove(pd.getId());
              }
            }

            if (!elementsToGet.isEmpty()) {
              // We now remove the elements we did not find, as we know they don't exist
              LOG.info("Found non existent items!");
              for (Long projectId : elementsToGet) {
                remainingProjectIds.remove(projectId);
              }
            }
          }
        });
      } catch (DynamoException e) {
        throw CrashReport.createAndLogError(LOG, null,
            ErrorUtils.collectUserErrorInfo(userId), e);
      }
    }

    if (!remainingProjectIds.isEmpty()) {
      LOG.severe("Remaining projects is not 0!");
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserErrorInfo(userId), new DynamoException("Something went wrong!"));
    }

    List<UserProject> uProjects = new ArrayList<>();
    for (StoredData.ProjectData projectData : projectDatas) {
      uProjects.add(mapProjectDataToUserProject(projectData.getId(), projectData));
    }

    return uProjects;
  }

  @Override
  public String getProjectName(final String userId, final long projectId) {
    final AtomicReference<String> projectName = new AtomicReference<>("");

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.ProjectData pd = projectDataTable.getItem(partitionKey(projectId));
          if (pd != null) {
            projectName.set(pd.getName());
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }

    return projectName.get();
  }

  @Override
  public Long getProjectDateModified(final String userId, final long projectId) {
    final AtomicReference<Long> modDate = new AtomicReference<>(0L);

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.ProjectData pd = projectDataTable.getItem(partitionKey(projectId));
          if (pd != null) {
            modDate.set(pd.getDateModified().toEpochMilli());
          }
        }
      }); // Transaction not needed, and we want the caching we get if we don't
      // use them.
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
    return modDate.get();
  }

  @Override
  public Long getProjectDateBuilt(final String userId, final long projectId) {
    final AtomicReference<Long> builtDate = new AtomicReference<>(0L);
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.ProjectData pd = projectDataTable.getItem(partitionKey(projectId));
          if (pd != null) {
            builtDate.set(pd.getDateBuilt().toEpochMilli());
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
    return builtDate.get();
  }

  @Override
  public void setProjectBuiltDate(final String userId, final long projectId, final long builtDate) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.ProjectData pd = projectDataTable.getItem(partitionKey(projectId));
          if (pd != null) {
            pd.setDateBuilt(Instant.ofEpochMilli(builtDate));
            projectDataTable.putItem(pd);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
  }

  @Override
  public String getProjectHistory(final String userId, final long projectId) {
    final AtomicReference<String> projectHistory = new AtomicReference<>("");
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.ProjectData pd = projectDataTable.getItem(partitionKey(projectId));
          if (pd != null) {
            projectHistory.set(pd.getHistory());
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
    return projectHistory.get();
  }

  @Override
  public Long getProjectDateCreated(final String userId, final long projectId) {
    final AtomicReference<Long> dateCreated = new AtomicReference<>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.ProjectData pd = projectDataTable.getItem(partitionKey(projectId));
          if (pd != null) {
            dateCreated.set(pd.getDateCreated().toEpochMilli());
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
    return dateCreated.get();
  }

  @Override
  public void createUserFileData(final String userId, final String fileName) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.UserFileData ufd = createUserFile(userId, fileName);
          if (ufd != null) {
            userFileDataTable.putItem(ufd);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserErrorInfo(userId, fileName), e);
    }
  }

  /**
   * Creates a UserFileData object for the given userKey and fileName, if it
   * doesn't already exist. Returns the new UserFileData object, or null if
   * already existed. This method does not add the UserFileData object to the
   * datastore.
   */
  private StoredData.UserFileData createUserFile(String userKey, String fileName) {
    StoredData.UserFileData ufd = userFileDataTable.getItem(partitionAndSortKey(userKey, fileName));
    if (ufd == null) {
      ufd = new StoredData.UserFileData();
      ufd.setFileName(fileName);
      ufd.setUserKey(userKey);
      return ufd;
    }

    return null;
  }

  @Override
  public List<String> getUserFileNames(final String userId) {
    final List<String> fileList = new ArrayList<>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          PageIterable<StoredData.UserFileData> fdq = userFileDataTable.query(QueryConditional.keyEqualTo(Key.builder()
              .partitionValue(userId)
              .build()));

          for (StoredData.UserFileData ufd : fdq.items()) {
            fileList.add(ufd.getFileName());
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
    return fileList;
  }

  @Override
  public void uploadUserFile(final String userId, final String fileName, final byte[] content) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          Key key = partitionAndSortKey(userId, fileName);
          StoredData.UserFileData ufd = userFileDataTable.getItem(key);
          /*
           * We look for the UserFileData object for the given userId and fileName.
           * If it doesn't exit, we create it.
           *
           * SPECIAL CASE: If fileName == StorageUtil.USER_BACKBACK_FILENAME and the
           * content is "[]", we *delete* the file because the default value returned
           * if the file doesn't exist is "[]" (the JSON empty list). This is to reduce
           * the clutter of files for the case where someone doesn't have anything in
           * the backpack. We pay $$ for storage.
           */
          byte [] empty = new byte[] { (byte)0x5b, (byte)0x5d }; // "[]" in bytes
          if (ufd == null) {          // File doesn't exist
            if (fileName.equals(StorageUtil.USER_BACKPACK_FILENAME) &&
                Arrays.equals(empty, content)) {
              return;                 // Nothing to do
            }
            ufd = new StoredData.UserFileData();
            ufd.setFileName(fileName);
            ufd.setUserKey(userId);
          } else {
            if (fileName.equals(StorageUtil.USER_BACKPACK_FILENAME) &&
                Arrays.equals(empty, content)) {
              // Storing an empty backback, just delete the file
              userFileDataTable.deleteItem(key);
              return;
            }
          }
          ufd.setContent(content);
          userFileDataTable.putItem(ufd);
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId, fileName), e);
    }
  }

  @Override
  public byte[] getUserFile(final String userId, final String fileName) {
    final AtomicReference<byte[]> result = new AtomicReference<>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.UserFileData ufd = userFileDataTable.getItem(partitionAndSortKey(userId, fileName));
          if (ufd != null) {
            result.set(ufd.getContent());
          } else {
            throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId, fileName),
                new FileNotFoundException(fileName));
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId, fileName), e);
    }
    return result.get();
  }

  @Override
  public void deleteUserFile(final String userId, final String fileName) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          Key key = partitionAndSortKey(userId, fileName);
          if (userFileDataTable.getItem(key) != null) {
            userFileDataTable.deleteItem(key);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId, fileName), e);
    }
  }

  @Override
  public void addFileToProject(final String userId, final Long projectId, final FileDataRoleEnum role,
                               final boolean changeModDate, final String fileName) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.FileData fd = createProjectFile(projectId, role, fileName);
          fd.setUserId(userId);
          fileDataTable.putItem(fd);
          if (changeModDate) {
            updateProjectModDate(projectId);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName), e);
    }
  }

  private StoredData.FileData createProjectFile(Long projectKey, FileDataRoleEnum role, String fileName) {
    StoredData.FileData fd = fileDataTable.getItem(partitionAndSortKey(projectKey, fileName));
    if (fd == null) {
      fd = new StoredData.FileData();
      fd.setFileName(fileName);
      fd.setProjectKey(projectKey);
      fd.setRole(role);
    } else if (!fd.getRole().equals(role)) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectProjectErrorInfo(null, projectKey, fileName),
          new IllegalStateException("File role change is not supported"));
    }

    return fd;
  }

  private Long updateProjectModDate(long projectId) {
    Instant modDate = Instant.now();
    StoredData.ProjectData pd = projectDataTable.getItem(partitionKey(projectId));
    if (pd != null) {
      // Only update the ProjectData dateModified if it is more then a minute
      // in the future. Do this to avoid unnecessary datastore puts.
      if (modDate.isAfter(pd.getDateModified().plusSeconds(60))) {
        pd.setDateModified(modDate);
        projectDataTable.putItem(pd);
      } else {
        // return the (old) dateModified
        modDate = pd.getDateModified();
      }
      return modDate.toEpochMilli();
    } else {
      throw CrashReport.createAndLogError(LOG, null, null,
          new IllegalArgumentException("project " + projectId + " doesn't exist"));
    }
  }

  @Override
  public void removeFileFromProject(final String userId, final Long projectId, final FileDataRoleEnum role,
                                    final boolean changeModDate, final String fileName) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          Key key = partitionAndSortKey(projectId, fileName);
          StoredData.FileData fd = fileDataTable.getItem(key);
          if (fd != null) {
            if (fd.getRole().equals(role)) {
              fileDataTable.deleteItem(key);
            } else {
              throw CrashReport.createAndLogError(LOG, null,
                  ErrorUtils.collectProjectErrorInfo(null, projectId, fileName),
                  new IllegalStateException("File role change is not supported"));
            }
          }

          if (changeModDate) {
            updateProjectModDate(projectId);
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName), e);
    }
  }

  @Override
  public List<String> getProjectFiles(final String userId, final long projectId, FileDataRoleEnum role) {
    final List<String> fileList = new ArrayList<>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          PageIterable<StoredData.FileData> fdq = fileDataTable.query(QueryConditional.keyEqualTo(Key.builder()
              .partitionValue(projectId)
              .build()));

          for (StoredData.FileData fd : fdq.items()) {
            if (fd.getRole().equals(role)) {
              fileList.add(fd.getFileName());
            }
          }
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
    return fileList;
  }

  @Override
  public UploadProjectFileResult uploadProjectFile(final String userId, final long projectId, final String fileName,
                                                   final boolean force, final byte[] content, final Long backupThreshold, final String filesystemName) throws BlocksTruncatedException {
    final UploadProjectFileResult uploadProjectFileResult = new UploadProjectFileResult();

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() throws DynamoException {
          StoredData.FileData fd = fileDataTable.getItem(partitionAndSortKey(projectId, fileName));

          // <Screen>.yail files are missing when user converts AI1 project to AI2
          // instead of blowing up, just create a <Screen>.yail file
          if (fd == null && (fileName.endsWith(".yail") || (fileName.endsWith(".png")))){
            fd = createProjectFile(projectId, FileDataRoleEnum.SOURCE, fileName);
            fd.setUserId(userId);
          }

          Preconditions.checkState(fd != null);

          if (fd.getUserId() != null && !fd.getUserId().isEmpty()) {
            if (!fd.getUserId().equals(userId)) {
              throw CrashReport.createAndLogError(LOG, null,
                  ErrorUtils.collectUserProjectErrorInfo(userId, projectId),
                  new UnauthorizedAccessException(userId, projectId, null));
            }
          }

          if ((content.length < 125) && (fileName.endsWith(".bky"))) { // Likely this is an empty blocks workspace
            if (!force) {            // force is true if we *really* want to save it!
              checkForBlocksTruncation(fd); // See if we had previous content and throw and exception if so
            }
          }

          if (filesystemName != null) {
            fd.setIsFilesystem(true);
            fd.setFilesystemName(filesystemName);
            uploadProjectFileResult.fileRole = fd.getRole();
            // If the content was previously stored in the datastore, clear it out.
            fd.setContent(null);
          } else {
            if (fd.isFilesystem()) {     // Was a GCS file, must have gotten smaller
              uploadProjectFileResult.needsFilesystemDelete = true;
              fd.setIsFilesystem(false);
              fd.setFilesystemName(null);
            }
            // Note, Don't have to do anything if the file was in the
            // Blobstore and shrank because the code above (3 lines
            // into the function) already handles removing the old
            // contents from the Blobstore.
            fd.setContent(content);
          }

          if (backupThreshold != null) {
            Instant now = Instant.now();
            if (fd.getLastBackup().plusSeconds(backupThreshold).isBefore(now)) {
              uploadProjectFileResult.shouldDoFilesystemBackup = true;
              fd.setLastBackup(now);
            }
          }

          // Old file not marked with ownership, mark it now
          if (fd.getUserId() == null || fd.getUserId().isEmpty()) {
            fd.setUserId(userId);
          }
          fileDataTable.putItem(fd);
          uploadProjectFileResult.lastModifiedDate = updateProjectModDate(projectId);
        }
      }); // Use transaction for blobstore, otherwise we don't need one
      // and without one the caching code comes into play.
    } catch (DynamoException e) {
      if (e.getMessage().startsWith("Blocks")) { // Convert Exception
        throw new BlocksTruncatedException();
      }
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName), e);
    }

    return uploadProjectFileResult;
  }

  // We are called when our caller detects we are about to write a trivial (empty)
  // workspace. We check to see if previously the workspace was non-trivial and
  // if so, throw the BlocksTruncatedException. This will be passed through the RPC
  // layer to the client code which will put up a dialog box for the user to review
  // See Ode.java for more information
  private void checkForBlocksTruncation(StoredData.FileData fd) throws DynamoException {
    if (fd.isFilesystem() || fd.getContent().length > 120)
      throw new DynamoException("BlocksTruncated"); // Hack
    // I'm avoiding having to modify every use of runJobWithRetries to handle a new
    // exception, so we use this dodge.
  }

  @Override
  public DeleteProjectFileResult deleteProjectFile(final String userId, final long projectId, final String fileName) {
    final DeleteProjectFileResult deleteProjectFileResult = new DeleteProjectFileResult();

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          Key key = partitionAndSortKey(projectId, fileName);
          StoredData.FileData fileData = fileDataTable.getItem(key);
          if (fileData != null) {
            if (fileData.getUserId() != null && !fileData.getUserId().isEmpty()) {
              if (!fileData.getUserId().equals(userId)) {
                throw CrashReport.createAndLogError(LOG, null,
                    ErrorUtils.collectUserProjectErrorInfo(userId, projectId),
                    new UnauthorizedAccessException(userId, projectId, null));
              }
            }
            if (fileData.isFilesystem()) {
              deleteProjectFileResult.fileRole = fileData.getRole();
              deleteProjectFileResult.filesystemToDelete = fileData.getFilesystemName();
            }
          }
          fileDataTable.deleteItem(key);
          deleteProjectFileResult.lastModifiedDate = updateProjectModDate(projectId);
        }
      });
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName), e);
    }

    return deleteProjectFileResult;
  }

  @Override
  public GetProjectFileResult getProjectFile(final String userId, final long projectId, final String fileName) {
    final GetProjectFileResult getProjectFileResult = new GetProjectFileResult();

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run() {
          StoredData.FileData fd = fileDataTable.getItem(partitionAndSortKey(projectId, fileName));

          if (fd == null) {
            throw CrashReport.createAndLogError(LOG, null,
                ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName),
                new FileNotFoundException("No data for " + fileName));
          }

          if (fd.getUserId() != null && !fd.getUserId().isEmpty()) {
            if (!fd.getUserId().equals(userId)) {
              throw CrashReport.createAndLogError(LOG, null,
                  ErrorUtils.collectUserProjectErrorInfo(userId, projectId),
                  new UnauthorizedAccessException(userId, projectId, null));
            }
          }

          if (fd.isFilesystem()) {     // It's in the Cloud Store
            getProjectFileResult.fileRole = fd.getRole();
            getProjectFileResult.filesystemToRetrieve = fd.getFilesystemName();
          } else {
            if (fd.getContent() != null) {
              getProjectFileResult.content = fd.getContent();
            }
          }
        }
      }); // Transaction not needed
    } catch (DynamoException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName), e);
    }
    return getProjectFileResult;
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

  private UserProject mapProjectDataToUserProject(final Long projectId, final StoredData.ProjectData projectData) {
    return new UserProject(projectId, projectData.getName(),
        projectData.getType(), projectData.getDateCreated().toEpochMilli(),
        projectData.getDateModified().toEpochMilli(), projectData.getDateBuilt().toEpochMilli(),
        projectData.isProjectMovedToTrash());
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

  private Key partitionAndSortKey(final String str1, final String str2) {
    return Key.builder().partitionValue(str1).sortValue(str2).build();
  }

  private Key partitionAndSortKey(final Long lng1, final String str2) {
    return Key.builder().partitionValue(lng1).sortValue(str2).build();
  }
}
