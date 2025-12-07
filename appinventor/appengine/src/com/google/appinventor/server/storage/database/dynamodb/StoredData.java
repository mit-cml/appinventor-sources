package com.google.appinventor.server.storage.database.dynamodb;

import com.google.appinventor.server.storage.FileDataRoleEnum;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;


final class StoredData {
  @DynamoDbBean
  public static final class UserData {
    private String id;

    private String email;

    private String settings;

    private boolean tosAccepted;
    private boolean isAdmin;

    // TODO: This is indexed in Datastore, but DDB does not support queries on partition keys
    private Instant visited;

    private String name;
    private String link;
    private int type;
    private String sessionId;
    private String password;

    public UserData() {}

    @DynamoDbPartitionKey
    @DynamoDbAttribute("UserId")
    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "EmailIndex")
    @DynamoDbAttribute("Email")
    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    @DynamoDbAttribute("Settings")
    public String getSettings() {
      return settings;
    }

    public void setSettings(String settings) {
      this.settings = settings;
    }

    @DynamoDbAttribute("TosAccepted")
    public Boolean getTosAccepted() {
      return tosAccepted;
    }

    public void setTosAccepted(Boolean tosAccepted) {
      this.tosAccepted = tosAccepted;
    }

    @DynamoDbAttribute("IsAdmin")
    public Boolean getIsAdmin() {
      return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
      this.isAdmin = isAdmin;
    }

    @DynamoDbAttribute("Visited")
    public Instant getVisited() {
      return visited;
    }

    public void setVisited(Instant visited) {
      this.visited = visited;
    }

    @DynamoDbAttribute("Name")
    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @DynamoDbAttribute("Link")
    public String getLink() {
      return link;
    }

    public void setLink(String link) {
      this.link = link;
    }

    @DynamoDbAttribute("Type")
    public Integer getType() {
      return type;
    }

    public void setType(Integer type) {
      this.type = type;
    }

    @DynamoDbAttribute("SessionId")
    public String getSessionId() {
      return sessionId;
    }

    public void setSessionId(String sessionId) {
      this.sessionId = sessionId;
    }

    @DynamoDbAttribute("Password")
    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }

  // Project properties
  @DynamoDbBean
  public static final class ProjectData {
    // Auto-generated unique project id
    private Long id;

    // Verbose project name
    private String name;

    // Project type. Currently Simple and YoungAndroid
    // TODO(user): convert to enum
    private String type;

    // Global project settings
    private String settings;

    // Date project created
    // TODO(): Make required
    private Instant dateCreated;

    // Date project last modified
    // TODO(): Make required
    private Instant dateModified;

    // Date project last built/exported as a runnable binary
    private Instant dateBuilt;

    // The specially formatted project history
    private String history;

    //adding a boolean variable to mark deleted project
    private boolean projectMovedToTrashFlag;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("ProjectId")
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    @DynamoDbAttribute("Name")
    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @DynamoDbAttribute("Type")
    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    @DynamoDbAttribute("Settings")
    public String getSettings() {
      return settings;
    }

    public void setSettings(String settings) {
      this.settings = settings;
    }

    @DynamoDbAttribute("DateCreated")
    public Instant getDateCreated() {
      return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
      this.dateCreated = dateCreated;
    }

    @DynamoDbAttribute("DateModified")
    public Instant getDateModified() {
      return dateModified;
    }

    public void setDateModified(Instant dateModified) {
      this.dateModified = dateModified;
    }

    @DynamoDbAttribute("DateBuilt")
    public Instant getDateBuilt() {
      return dateBuilt;
    }

    public void setDateBuilt(Instant dateBuilt) {
      this.dateBuilt = dateBuilt;
    }

    @DynamoDbAttribute("History")
    public String getHistory() {
      return history;
    }

    public void setHistory(String history) {
      this.history = history;
    }

    @DynamoDbAttribute("IsProjectMovedToTrash")
    public Boolean getIsProjectMovedToTrash() {
      return projectMovedToTrashFlag;
    }

    public void setProjectMovedToTrash(Boolean projectMovedToTrashFlag) {
      this.projectMovedToTrashFlag = projectMovedToTrashFlag;
    }
  }

  @DynamoDbBean
  public static final class UserProjectData {
    public enum StateEnum {
      CLOSED,
      OPEN,
      DELETED
    }

    // The user (parent's) key
    private String userKey;

    // The project id
    private long projectId;

    // State of the project relative to user
    // TODO(user): is this ever used?
    private StateEnum state;

    // User specific project settings
    // TODO(user): is this ever used?
    private String settings;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("UserId")
    public String getUserKey() {
      return userKey;
    }

    public void setUserKey(String userKey) {
      this.userKey = userKey;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("ProjectId")
    public Long getProjectId() {
      return projectId;
    }

    public void setProjectId(Long projectId) {
      this.projectId = projectId;
    }

    @DynamoDbAttribute("State")
    public StateEnum getState() {
      return state;
    }

    public void setState(StateEnum state) {
      this.state = state;
    }

    @DynamoDbAttribute("Settings")
    public String getSettings() {
      return settings;
    }

    public void setSettings(String settings) {
      this.settings = settings;
    }
  }

  // Non-project-specific files (tied to user)
  @DynamoDbBean
  public static final class UserFileData {
    // The user (parent's) key
    private String userKey;

    // The file name
    private String fileName;

    // File content, these are raw bytes. Note that Objectify automatically
    // converts byte[] to Blob.
    private byte[] content;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("UserId")
    public String getUserKey() {
      return userKey;
    }

    public void setUserKey(String userKey) {
      this.userKey = userKey;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("FileName")
    public String getFileName() {
      return fileName;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }

    @DynamoDbAttribute("Content")
    public byte[] getContent() {
      return content;
    }

    public void setContent(byte[] content) {
      this.content = content;
    }
  }

  // Project files
  // Note: FileData has to be Serializable so we can put it into
  //       memcache.
  @DynamoDbBean
  public static final class FileData {
    // Key of the project (parent) to which this file belongs
    private Long projectKey;

    // The file name
    private String fileName;

    // File role
    private FileDataRoleEnum role;

    // File content, these are raw bytes. Note that Objectify automatically
    // converts byte[] to an App Engine Datastore Blob (which is not the same thing as a Blobstore
    // Blob).  Consequently, if isBlob is true, the content field should be ignored and the data
    // should be retrieved from Blobstore.
    private byte[] content;

    // Is this file stored in the Google Cloud Store (GCS). If it is the gcsName will contain the
    // GCS file name (sans bucket).
    private boolean isFilesystem;

    // The GCS filename, sans bucket name
    private String filesystemName;

    // DateTime of last backup only used if GCS is enabled
    private Instant lastBackup;

    private String userId;              // The userId which owns this file
    // if null or the empty string, we haven't initialized
    // it yet

    @DynamoDbPartitionKey
    @DynamoDbAttribute("ProjectId")
    public Long getProjectKey() {
      return projectKey;
    }

    public void setProjectKey(Long projectKey) {
      this.projectKey = projectKey;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("FileName")
    public String getFileName() {
      return fileName;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }

    @DynamoDbAttribute("Role")
    public FileDataRoleEnum getRole() {
      return role;
    }

    public void setRole(FileDataRoleEnum role) {
      this.role = role;
    }

    @DynamoDbAttribute("Content")
    public byte[] getContent() {
      return content;
    }

    public void setContent(byte[] content) {
      this.content = content;
    }

    @DynamoDbAttribute("IsFilesystem")
    public Boolean getIsFilesystem() {
      return isFilesystem;
    }

    public void setIsFilesystem(Boolean filesystem) {
      isFilesystem = filesystem;
    }

    @DynamoDbAttribute("FilesystemName")
    public String getFilesystemName() {
      return filesystemName;
    }

    public void setFilesystemName(String filesystemName) {
      this.filesystemName = filesystemName;
    }

    @DynamoDbAttribute("LastBackup")
    public Instant getLastBackup() {
      return lastBackup;
    }

    public void setLastBackup(Instant lastBackup) {
      this.lastBackup = lastBackup;
    }

    @DynamoDbAttribute("UserId")
    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }
  }

  @DynamoDbBean
  public static final class WhiteListData {
    private String email;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("Email")
    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }
  }

  @DynamoDbBean
  public static final class FeedbackData {
    private String id;
    private String notes;
    private String foundIn;
    private String faultData;
    private String comments;
    private String datestamp;
    private String email;
    private String projectId;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("Id")
    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    @DynamoDbAttribute("Notes")
    public String getNotes() {
      return notes;
    }

    public void setNotes(String notes) {
      this.notes = notes;
    }

    @DynamoDbAttribute("FoundIn")
    public String getFoundIn() {
      return foundIn;
    }

    public void setFoundIn(String foundIn) {
      this.foundIn = foundIn;
    }

    @DynamoDbAttribute("FaultData")
    public String getFaultData() {
      return faultData;
    }

    public void setFaultData(String faultData) {
      this.faultData = faultData;
    }

    @DynamoDbAttribute("Comments")
    public String getComments() {
      return comments;
    }

    public void setComments(String comments) {
      this.comments = comments;
    }

    @DynamoDbAttribute("Datestamp")
    public String getDatestamp() {
      return datestamp;
    }

    public void setDatestamp(String datestamp) {
      this.datestamp = datestamp;
    }

    @DynamoDbAttribute("Email")
    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    @DynamoDbAttribute("ProjectId")
    public String getProjectId() {
      return projectId;
    }

    public void setProjectId(String projectId) {
      this.projectId = projectId;
    }

  }

  @DynamoDbBean
  public static final class NonceData {
    private String nonce;
    private String userId;
    private Long projectId;
    private Instant timestamp;
    private Instant validUntil;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("Nonce")
    public String getNonce() {
      return nonce;
    }

    public void setNonce(String nonce) {
      this.nonce = nonce;
    }

    @DynamoDbAttribute("UserId")
    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    @DynamoDbAttribute("ProjectId")
    public Long getProjectId() {
      return projectId;
    }

    public void setProjectId(Long projectId) {
      this.projectId = projectId;
    }

    @DynamoDbAttribute("Timestamp")
    public Instant getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
      this.timestamp = timestamp;
    }

    @DynamoDbAttribute("ValidUntil")
    public Instant getValidUntil() {
      return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
      this.validUntil = validUntil;
    }
  }

  @DynamoDbBean
  public static final class CorruptionRecord {
    private String id;
    // TODO: This is indexed in Datastore, but DDB does not support queries on partition keys
    private Instant timestamp;
    private String userId;
    private long projectId;
    private String fileId;
    private String message;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("Id")
    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    @DynamoDbAttribute("Timestamp")
    public Instant getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
      this.timestamp = timestamp;
    }

    @DynamoDbAttribute("UserId")
    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    @DynamoDbAttribute("ProjectId")
    public Long getProjectId() {
      return projectId;
    }

    public void setProjectId(Long projectId) {
      this.projectId = projectId;
    }

    @DynamoDbAttribute("FileId")
    public String getFileId() {
      return fileId;
    }

    public void setFileId(String fileId) {
      this.fileId = fileId;
    }

    @DynamoDbAttribute("Message")
    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }

  @DynamoDbBean
  public static final class SplashData {
    private Long id;
    private int version;
    private String content;
    private int height;
    private int width;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("SplashId")
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    @DynamoDbAttribute("Version")
    public Integer getVersion() {
      return version;
    }

    public void setVersion(Integer version) {
      this.version = version;
    }

    @DynamoDbAttribute("Content")
    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    @DynamoDbAttribute("Height")
    public Integer getHeight() {
      return height;
    }

    public void setHeight(Integer height) {
      this.height = height;
    }

    @DynamoDbAttribute("Width")
    public Integer getWidth() {
      return width;
    }

    public void setWidth(Integer width) {
      this.width = width;
    }
  }

  // Data Structure to keep track of url's emailed out for password
  // setting and reseting. The Id (which is a UUID) is part of the URL
  // that is mailed out.
  @DynamoDbBean
  public static final class PWData {
    private String id;              // "Secret" URL part

    // TODO: This is indexed in Datastore, but DDB does not support queries on partition keys
    private Instant timestamp; // So we know when to expire this objects
    private Instant validUntil;

    private String email;            // Email of account in question

    @DynamoDbPartitionKey
    @DynamoDbAttribute("Id")
    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    @DynamoDbAttribute("Timestamp")
    public Instant getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
      this.timestamp = timestamp;
    }

    @DynamoDbAttribute("Email")
    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    @DynamoDbAttribute("ValidUntil")
    public Instant getValidUntil() {
      return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
      this.validUntil = validUntil;
    }
  }

  // A Shared backpack. Shared backpacks are not associated with
  // any one user. Instead they are stored independent of projects
  // and users. At login time a shared backpack may be specified.
  // This requires an SSO Login from an external system to provide
  // it.
  @DynamoDbBean
  public static final class Backpack {
    private String id;
    private String content;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("Id")
    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    @DynamoDbAttribute("Content")
    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }
  }

  @DynamoDbBean
  public static final class AllowedTutorialUrls {
    // Unique Id - for now we expect there to be only 1 MotdData object.
    private Long id;

    // list of allowed Urls as JSON
    // we use JSON here to make it easier to hand edit via
    // datastore editing tools
    private String allowedUrls;

    public AllowedTutorialUrls() {
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("Id")
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    @DynamoDbAttribute("AllowedUrls")
    public String getAllowedUrls() {
      return allowedUrls;
    }

    public void setAllowedUrls(String allowedUrls) {
      this.allowedUrls = allowedUrls;
    }
  }

  @DynamoDbBean
  public static final class AllowedIosExtensions {
    // Unique Id - for now we expect there to be only 1 AllowedIosExtensions object.
    private Long id;

    // list of allowed extension packages as JSON
    private String allowedExtensions;

    public AllowedIosExtensions() {
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("Id")
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    @DynamoDbAttribute("AllowedExtensions")
    public String getAllowedExtensions() {
      return allowedExtensions;
    }

    public void setAllowedExtensions(String allowedExtensions) {
      this.allowedExtensions = allowedExtensions;
    }
  }

}
