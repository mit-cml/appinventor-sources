// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import org.json.JSONObject;
import org.json.JSONException;
import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.Motd;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.admin.AdminUser;
import com.google.appinventor.shared.rpc.AdminInterfaceException;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.user.SplashConfig;
import com.google.appinventor.server.FileExporter;
import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.server.storage.StoredData.FileData;
import com.google.appinventor.server.storage.StoredData.PWData;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Savepoint;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import javax.annotation.Nullable;

/**
 * Interface of methods to simplify access to the storage systems.
 *
 * In all of the methods below that take a user id, it should be a string
 * that uniquely identifies the logged-in user and will continue to do so
 * indefinitely. It is up to the caller to choose the source of user ids.
 *
 */
public class PostgreSQLStorageIo implements StorageIo {
  private static final Flag<Boolean> requireTos = Flag.createFlag("require.tos", false);
  private static final Flag<String> jdbcUrl = Flag.createFlag("jdbc.url", null);
  private static final Flag<String> jdbcUser = Flag.createFlag("jdbc.user", null);
  private static final Flag<String> jdbcPassword = Flag.createFlag("jdbc.password", null);
  private static final Logger LOG = Logger.getLogger(PostgreSQLStorageIo.class.getName());
  private static final String HOST_ID = String.format(
    "%s-%s-%s-%s",
    GitBuildId.GIT_BUILD_VERSION,
    GitBuildId.GIT_BUILD_FINGERPRINT,
    GitBuildId.ANT_BUILD_DATE,
    UUID.randomUUID().toString()
    );

  private Connection conn;

  public PostgreSQLStorageIo() {
    // Create connection
    try {
      this.conn = DriverManager.getConnection(jdbcUrl.get(), jdbcUser.get(), jdbcPassword.get());
      this.conn.setAutoCommit(false);
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot setup database connection", e);
    }

    // Initialize database
    try (Statement stmt = this.conn.createStatement()) {
      stmt.execute("CREATE EXTENSION IF NOT EXISTS 'uuid-ossp'");
      stmt.execute("CREATE TABLE user IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  email VARCHAR UNIQUE," +
                   "  name VARCHAR NOT NULL CHECK (name <> '')," +
                   "  link VARCHAR," +
                   "  emailFrequency INT NOT NULL CHECK (emailFrequency >= 0)," +
                   "  tosAccepted BOOLEAN DEFAULT FALSE NOT NULL," +
                   "  isAdmin BOOLEAN DEFAULT FALSE NOT NULL," +
                   "  isReadOnly BOOLEAN DEFAULT FALSE NOT NULL," +
                   "  type INT NOT NULL," +
                   "  sessionId VARCHAR," +
                   "  password VARCHAR CHECK (password <> '')," +
                   "  backPackId VARCHAR," +
                   "  settings VARCHAR NOT NULL DEFAULT ''," +
                   "  visited TIMESTAMP WITH TIMEZONE" +
                   ")");
      stmt.execute("CREATE TABLE project IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  userId BIGINT NOT NULL REFERENCES user ON DELETE CASCADE ON UPDATE CASCADE," +
                   "  name VARCHAR NOT NULL CHECK (name <> '')," +
                   "  link VARCHAR," +
                   "  type VARCHAR," +
                   "  settings VARCHAR NOT NULL DEFAULT ''," +
                   "  dateCreated TIMESTAMP WITH TIMEZONE NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                   "  dateModified TIMESTAMP WITH TIMEZONE NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                   "  history VARCHAR," +
                   "  galleryId BIGINT," +
                   "  attributionId BIGINT" +
                   ")");
      stmt.execute("CREATE TABLE projectFile IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  projectId BIGINT NOT NULL REFERENCES project ON DELETE CASCADE ON UPDATE CASCADE," +
                   "  userId BIGINT NOT NULL REFERENCES user ON DELETE CASCADE ON UPDATE CASCADE," +
                   "  fileName VARCHAR NOT NULL," +
                   "  role VARCHAR NOT NULL," +
                   "  content VARCHAR," +
                   "  CONSTRAINT fileConst UNIQUE(projectId, userId, fileName)" +
                   ")");
      stmt.execute("CREATE TABLE userFile IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  userId BIGINT NOT NULL REFERENCES user ON DELETE CASCADE ON UPDATE CASCADE," +
                   "  fileName VARCHAR NOT NULL," +
                   "  content VARCHAR," +
                   "  CONSTRAINT fileConst UNIQUE(userId, fileName)" +
                   ")");
      stmt.execute("CREATE TABLE tempFile IF NOT EXISTS (" +
                   "  id BIGINT PRIMARY KEY," +
                   "  content BLOB" +
                   ")");
      stmt.execute("CREATE TABLE corruptionReport IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  userId BIGINT NOT NULL," +
                   "  projectId BIGINT NOT NULL," +
                   "  fileName VARCHAR NOT NULL," +
                   "  message VARCHAR NOT NULL," +
                   "  timestamp TIMESTAMP WITH TIMEZONE NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                   ")");
      stmt.execute("CREATE TABLE ipAddress IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  key VARCHAR UNIQUE NOT NULL," +
                   "  address VARCHAR NOT NULL" +
                   ")");
      stmt.execute("CREATE TABLE whitelist IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  email VARCHAR UNIQUE NOT NULL," +
                   ")");
      stmt.execute("CREATE TABLE feedback IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  notes VARCHAR," +
                   "  foundIn VARCHAR," +
                   "  faultData VARCHAR," +
                   "  comments VARCHAR," +
                   "  datestamp VARCHAR," +
                   "  email VARCHAR," +
                   "  projectId VARCHAR" +
                   ")");
      stmt.execute("CREATE TABLE nonce IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  nonce VARCHAR UNIQUE NOT NULL," +
                   "  userId BIGINT NOT NULL REFERENCES user ON DELETE CASCADE ON UPDATE CASCADE," +
                   "  projectId BIGINT NOT NULL REFERENCES project ON DELETE CASCADE ON UPDATE CASCADE," +
                   "  timestamp TIMESTAMP WITH TIMEZONE NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                   ")");
      stmt.execute("CREATE TABLE pwData IF NOT EXISTS (" +
                   "  uuid UUID PRIMARY KEY," +
                   "  email VARCHAR NOT NULL," +
                   "  timestamp TIMESTAMP WITH TIMEZONE NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                   ")");
      stmt.execute("CREATE TABLE backpack IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  content VARCHAR NOT NULL" +
                   ")");
      stmt.execute("CREATE TABLE buildStatus IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  host VARCHAR NOT NULL," +
                   "  userId BIGINT NOT NULL," +
                   "  projectId BIGINT NOT NULL," +
                   "  progress INT," +
                   "  CONSTRAINT buildConst UNIQUE(host, userId, projectId)" +
                   ")");
      stmt.execute("CREATE TABLE misc IF NOT EXISTS (" +
                   "  id BIGSERIAL PRIMARY KEY," +
                   "  key VARCHAR UNIQUE NOT NULL," +
                   "  value VARCHAR" +
                   ")");
      stmt.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS ON user (lower(email))");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ON user (userId)");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ON user (email)");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ON user (lower(email))");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ON projectFile (projectId, userId, fileName)");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ON userFile (userId, fileName)");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ON ipAddress (key)");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ON whitelist (email)");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ON whitelist (lower(email))");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ON nonce (nonce)");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ON buildStatus (host, userId, projectId)");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ON misc (key)");

      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, String.format("Failed to initialize database with url=\"%s\" user=\"%s\"", jdbcUrl, jdbcUser), e);
    }
  }

  // User management

  /**
   * Returns user data given user id. If the user data for the given id
   * doesn't already exist in the storage, it should be created.
   *
   * @param userId unique user id
   * @return user data
   */
  @Override
  public User getUser(String strUserId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    User user = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM user WHERE userId = ?")) {
      qstmt.setLong(1, userId);
      ResultSet rs = qstmt.executeQuery();

      // We assume single result due to unique constraint on userId and email
      if (rs.next()) {
        String newStrUserId = Long.toHexString(rs.getLong("id"));
        user = new User(
          newStrUserId,
          rs.getString("email"),
          rs.getString("name"),
          rs.getString("link"),
          rs.getInt("emailFrequency"),
          rs.getBoolean("tosAccepted"),
          rs.getBoolean("isAdmin"),
          rs.getInt("type"),
          rs.getString("sessionId")
          );
        if (user == null) {
          // Here we have distinct behavior from ObjectifyStorageIo.getUser()
          // We do not create user.
          this.conn.rollback();
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
        }
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }
    return user;
  }

  /**
   * Returns user data given user id. If the user data for the given id
   * doesn't already exist in the storage, it should be created. email
   * is the email address currently associated with this user. If it
   * doesn't match the stored email address (or if the user doesn't exist yet)
   * the stored email address will be updated to this one.
   *
   * @param userId unique user id
   * @return user data
   */
  @Override
  public User getUser(String strUserId, String email) {
    // Due to the colliding userId vulnerability, we omit the userId and query users merely by email
    // https://github.com/mit-cml/appinventor-sources/issues/1592
    return this.getUserFromEmail(email);
  }

  /**
   * Returns user data given user email address. If the user data for the given email
   * doesn't already exist in the storage, it should be created. email
   * is the email address currently associated with this user.
   *
   * @param user email address
   * @return user data
   */
  @Override
  public User getUserFromEmail(String email) {
    assert email != null;
    User user = null;

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try {
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM user WHERE lower(email) = lower(?)")) {
        qstmt.setString(1, email);
        ResultSet rs = qstmt.executeQuery();
        long userId = rs.getLong("userId");
        String strUserId = Long.toHexString(userId);

        if (rs.next()) {
          user = new User(
            strUserId,
            rs.getString("email"),
            rs.getString("name"),
            rs.getString("link"),
            rs.getInt("emailFrequency"),
            rs.getBoolean("tosAccepted"),
            rs.getBoolean("isAdmin"),
            rs.getInt("type"),
            rs.getString("sessionId")
            );
        }
      }

      if (user == null) {
        user = createUser(email, User.USER, false, null, save);
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "email=" + email, e);
    }

    return user;
  }

  /**
   * Sets the stored email address for user with id userId
   *
   */
  @Override
  public void setUserEmail(String strUserId, String email) {
    assert strUserId != null && email != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement stmt = this.conn.prepareStatement("UPDATE user SET email = ? WHERE userId = ?")) {
      stmt.setString(1, email);
      stmt.setLong(2, userId);
      ret = stmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      // One case is violation on email uniqueness. We fail it anyway.
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
  }

  /**
   * Sets that the user has accepted the terms of service.
   *
   * @param userId user id
   */
  @Override
  public void setTosAccepted(String strUserId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement stmt = this.conn.prepareStatement("UPDATE user SET tosAccepted = ? WHERE userId = ?")) {
      stmt.setBoolean(1, true);
      stmt.setLong(2, userId);
      ret = stmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
  }

  /**
   * Sets the user's session id value which is used to ensure only
   * one valid session exists for a user
   *
   * @param userId user id
   * @param sessionId the session id (uuid) value
   */
  @Override
  public void setUserSessionId(String strUserId, String sessionId) {
    assert strUserId != null && sessionId != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement stmt = this.conn.prepareStatement("UPDATE user SET sessionId = ? WHERE userId = ?")) {
      stmt.setString(1, sessionId);
      stmt.setLong(2, userId);
      ret = stmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
  }

  /**
   * Sets the user's hashed password.
   *
   * @param userId user id
   * @param hashed password
   */
  @Override
  public void setUserPassword(String strUserId, String password) {
    assert strUserId != null && password != null && !password.equals("");
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement stmt = this.conn.prepareStatement("UPDATE user SET password = ? WHERE userId = ?")) {
      stmt.setString(1, password);
      stmt.setLong(2, userId);
      ret = stmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
  }

  /**
   * Returns a string with the user's settings.
   *
   * @param userId user id
   * @return settings
   */
  @Override
  public String loadSettings(String strUserId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    String settings = null;

    try (PreparedStatement stmt = this.conn.prepareStatement("SELECT settings WHERE userId = ?")) {
      stmt.setLong(1, userId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        settings = rs.getString("settings");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (settings == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
    return settings;
  }

  /**
   * Sets the stored name for user with id userId
   *
   */
  @Override
  public void setUserName(String strUserId, String name) {
    assert strUserId != null && name != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement stmt = this.conn.prepareStatement("UPDATE user SET name = ? WHERE userId = ?")) {
      stmt.setString(1, name);
      stmt.setLong(2, userId);
      ret = stmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
  }

  /**
   * Returns a string with the user's name.
   *
   * @param userId user id
   * @return name
   */
  @Override
  public String getUserName(String strUserId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    String name = null;

    try (PreparedStatement stmt = this.conn.prepareStatement("SELECT name WHERE userId = ?")) {
      stmt.setLong(1, userId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        name = rs.getString("name");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (name == null) {        // name is not nullable
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
    return name;
  }

  /**
   * Returns a string with the user's name.
   *
   * @param userId user id
   * @return name
   */
  @Override
  public String getUserLink(String strUserId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    String link = null;
    boolean found = false;

    try (PreparedStatement stmt = this.conn.prepareStatement("SELECT link WHERE userId = ?")) {
      stmt.setLong(1, userId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        link = rs.getString("link");
        found = true;
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (!found) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
    return link;
  }

  /**
   * Sets the stored link for user with id userId
   *
   */
  @Override
  public void setUserLink(String strUserId, String link) {
    assert strUserId != null && link != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement stmt = this.conn.prepareStatement("UPDATE user SET link = ? WHERE userId = ?")) {
      stmt.setString(1, link);
      stmt.setLong(2, userId);
      ret = stmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
  }

  /**
   * Returns the email notification frequency
   *
   * @param userId user id
   * @return emailFrequency email frequency
   */
  @Override
  public int getUserEmailFrequency(String strUserId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    Integer freq = null;

    try (PreparedStatement stmt = this.conn.prepareStatement("SELECT emailFrequency WHERE userId = ?")) {
      stmt.setLong(1, userId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        freq = rs.getInt("emailFrequency");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (freq == null) {        // emailFrequency is not nullable
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
    return freq;
  }

  /**
   * Sets the stored email notification frequency for user with id userId
   *
   */
  @Override
  public void setUserEmailFrequency(String strUserId, int emailFrequency) {
    assert strUserId != null && emailFrequency >= 0;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement stmt = this.conn.prepareStatement("UPDATE user SET emailFrequency = ? WHERE userId = ?")) {
      stmt.setInt(1, emailFrequency);
      stmt.setLong(2, userId);
      ret = stmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
  }

  /**
   * Stores a string with the user's settings.
   *
   * @param userId user ID
   * @param settings user's settings
   */
  @Override
  public void storeSettings(String strUserId, String settings) {
    assert strUserId != null && settings != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement stmt = this.conn.prepareStatement("UPDATE user SET settings = ?, visited = CURRENT_TIMESTAMP WHERE userId = ?")) {
      stmt.setString(1, settings);
      stmt.setLong(2, userId);
      ret = stmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
    }
  }

  // Project management

  /**
   * Creates a new project and uploads the files.
   *
   * <p>
   * This is an atomic operation.
   *
   * @param userId user id
   * @param project project information
   * @param projectSettings project settings
   * @return project id
   */
  @Override
  public long createProject(String strUserId, Project project, String projectSettings) {
    assert strUserId != null && project != null && projectSettings != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    String name = project.getProjectName();
    String type = project.getProjectType();
    String history = project.getProjectHistory();
    long galleryId = UserProject.NOTPUBLISHED;
    long attributionId = UserProject.FROMSCRATCH;
    Long projectId = null;             // Defined after insertion

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    // Insert project data
    try {

      try (PreparedStatement ustmt = conn.prepareStatement(
             "INSERT INTO project (userId, name, type, history, galleryId, attributionId, settings) VALUES (?, ?, ?, ?, ?, ?, ?)"
             )) {
        ustmt.setLong(1, userId);
        ustmt.setString(2, name);
        ustmt.setString(3, type);
        ustmt.setString(4, history);
        ustmt.setLong(5, galleryId);
        ustmt.setLong(6, attributionId);
        ustmt.setString(7, projectSettings);

        ret = ustmt.executeUpdate();

        // Retrieve key
        if (ret > 0) {
          try (ResultSet keys = ustmt.getGeneratedKeys()) {
            assert keys.next();
            projectId = keys.getLong(1);
          }
        }
      }

      if (projectId == null) {
        this.conn.rollback(save);
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException(""));
      }

      // Save source files
      try {
        for (TextFile file : project.getSourceFiles()) {
          FileData.RoleEnum role = FileData.RoleEnum.SOURCE;
          String fileName = file.getFileName();
          byte[] content = file.getContent().getBytes(StorageUtil.DEFAULT_CHARSET);
          this.createProjectFile(projectId, userId, role, fileName, content, save);
        }
      } catch (UnsupportedEncodingException e) {
        this.conn.rollback(save);
        throw CrashReport.createAndLogError(LOG, null, "Cannot decode file content", e);
      }

      for (RawFile file : project.getRawSourceFiles()) {
        FileData.RoleEnum role = FileData.RoleEnum.SOURCE;
        String fileName = file.getFileName();
        byte[] content = file.getContent();
        this.createProjectFile(projectId, userId, role, fileName, content, save);
      }

      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }

    return projectId;
  }

  /**
   * Deletes a project and all its files.
   *
   * @param userId user ID
   * @param projectId project ID
   */
  @Override
  public void deleteProject(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);

    // Corresponding entries in projectFile table will be automatically remove by ON DELETE CASCADE
    try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM project WHERE projectId = ? AND userId = ?")) {
      ustmt.setLong(1, projectId);
      ustmt.setLong(2, userId);
      ustmt.executeUpdate();     // Don't care about result
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }
  }

  /**
   * Returns an array with the user's projects.
   *
   * @param userId  user ID
   * @return  list of projects
   */
  @Override
  public List<Long> getProjects(String strUserId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    List<Long> ret = new ArrayList<>();

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT projectId FROM project WHERE userId = ?")) {
      qstmt.setLong(1, userId);
      ResultSet rs = qstmt.executeQuery();

      while (rs.next()) {
        ret.add(rs.getLong("projectId"));
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    return ret;
  }

  /**
   * sets a projects gallery id when it is published
   * @param userId a user Id (the request is made on behalf of this user)*
   * @param projectId project ID
   * @param galleryId gallery ID
   */
  @Override
  public void setProjectGalleryId(final String strUserId, final long projectId,final long galleryId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement("UPDATE project SET galleryId = ?, dateModified = CURRENT_TIMESTAMP WHERE projectId = ? AND userId = ?")) {
      ustmt.setLong(1, galleryId);
      ustmt.setLong(2, projectId);
      ustmt.setLong(3, userId);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }
  }

  /**
   * sets a projects attribution id when it is opened from a gallery project
   * @param userId a user Id (the request is made on behalf of this user)*
   * @param projectId project ID
   * @param attributionId attribution ID
   */
  @Override
  public void setProjectAttributionId(final String strUserId, final long projectId,final long attributionId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement("UPDATE project SET attributionId = ?, dateModified = CURRENT_TIMESTAMP WHERE projectId = ? AND userId = ?")) {
      ustmt.setLong(1, attributionId);
      ustmt.setLong(2, projectId);
      ustmt.setLong(3, userId);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }
  }

  /**
   * Returns a string with the project settings.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId project ID
   * @return settings
   */
  @Override
  public String loadProjectSettings(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    String settings = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT settings FROM project WHERE projectId = ? AND userId = ?")) {
      qstmt.setLong(1, projectId);
      qstmt.setLong(2, userId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        settings = rs.getString("settings");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    if (settings == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }
    return settings;
  }

  /**
   * Stores a string with the project settings.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param settings  project settings
   */
  @Override
  public void storeProjectSettings(String strUserId, long projectId, String settings) {
    assert strUserId != null && settings != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement("UPDATE project SET settingsId = ?, dateModified = CURRENT_TIMESTAMP WHERE projectId = ? AND userId = ?")) {
      ustmt.setString(1, settings);
      ustmt.setLong(2, projectId);
      ustmt.setLong(3, userId);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }
  }

  /**
   * Returns the project type.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   *
   * @return  project type
   */
  @Override
  public String getProjectType(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    String type = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT type FROM project WHERE projectId = ? AND userId = ?")) {
      qstmt.setLong(1, projectId);
      qstmt.setLong(2, userId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        type = rs.getString("type");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    if (type == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }
    return type;
  }

  /**
   * Returns the ProjectData object complete.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   * @return new UserProject object
   */
  @Override
  public UserProject getUserProject(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    UserProject proj = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM project WHERE projectId = ? AND userId = ?")) {
      qstmt.setLong(1, projectId);
      qstmt.setLong(2, userId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        String name = rs.getString("name");
        String type = rs.getString("type");
        Timestamp dateCreated = rs.getTimestamp("dateCreated");
        Timestamp dateModified = rs.getTimestamp("dateModified");
        long attributionId = rs.getLong("attributionId");
        long galleryId = rs.getLong("galleryId");

        proj = new UserProject(
          projectId,
          name,
          type,
          dateCreated.getTime(),
          dateModified.getTime(),
          galleryId,
          attributionId
          );
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    if (proj == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }
    return proj;
  }

  /**
   * Bulk version of getUserProject.
   * @param userId a userId
   * @param projectIds a List of project ids
   * @return new List of UserProject objects
   */
  @Override
  public List<UserProject> getUserProjects(String strUserId, List<Long> projectIds) {
    assert strUserId != null && projectIds != null && projectIds.size() > 0;
    long userId = Long.parseLong(strUserId, 16);
    int nIds = projectIds.size();
    List<UserProject> ret = new ArrayList<>();

    // Construct sql statement (Quick and dirty way for Java <= 7)
    StringBuilder sql = new StringBuilder("SELECT * FROM project WHERE userId = ? AND projectId IN (?");
    for (int cnt = 1; cnt < nIds; cnt += 1) {
      sql.append(", ?");
    }
    sql.append(")");

    try (PreparedStatement qstmt = conn.prepareStatement(sql.toString())) {
      qstmt.setLong(1, userId);
      for (int idx = 0; idx < nIds; idx += 1) {
        qstmt.setLong(idx + 1, projectIds.get(idx));
      }

      ResultSet rs = qstmt.executeQuery();

      while (rs.next()) {
        UserProject proj = null;
        long projectId = rs.getLong("projectId");
        String name = rs.getString("name");
        String type = rs.getString("type");
        Timestamp dateCreated = rs.getTimestamp("dateCreated");
        Timestamp dateModified = rs.getTimestamp("dateModified");
        long attributionId = rs.getLong("attributionId");
        long galleryId = rs.getLong("galleryId");

        proj = new UserProject(
          projectId,
          name,
          type,
          dateCreated.getTime(),
          dateModified.getTime(),
          galleryId,
          attributionId
          );
        ret.add(proj);
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    if (ret.size() != projectIds.size()) {
      LOG.log(Level.WARNING, "getUserProjects(): Some project IDs are missing in database");
    }
    return ret;
  }

  /**
   * Returns a project name.
   *
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   * @return project name
   */
  @Override
  public String getProjectName(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    String name = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT name FROM project WHERE projectId = ? AND userId = ?")) {
      qstmt.setLong(1, projectId);
      qstmt.setLong(2, userId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        name = rs.getString("name");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    if (name == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }
    return name;
  }

  /**
   * Returns the date the project was last modified.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   *
   * @return long milliseconds
   */
  @Override
  public long getProjectDateModified(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    Timestamp dateModified = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT dateModified FROM project WHERE projectId = ? AND userId = ?")) {
      qstmt.setLong(1, projectId);
      qstmt.setLong(2, userId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        dateModified = rs.getTimestamp("dateModified");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    if (dateModified == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }
    return dateModified.getTime();
  }

  /**
   * Returns the specially formatted list of project history.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   *
   * @return String specially formatted history
   */
  @Override
  public String getProjectHistory(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    String history = null;
    boolean found = false;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT history FROM project WHERE projectId = ? AND userId = ?")) {
      qstmt.setLong(1, projectId);
      qstmt.setLong(2, userId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        history = rs.getString("history");
        found = true;
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    if (!found) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }
    return history;
  }

  // JIS XXX
  /**
   * Returns the date the project was created.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   *
   * @return long milliseconds
   */
  @Override
  public long getProjectDateCreated(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    Timestamp dateCreated = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT dateCreated FROM project WHERE projectId = ? AND userId = ?")) {
      qstmt.setLong(1, projectId);
      qstmt.setLong(2, userId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        dateCreated = rs.getTimestamp("dateCreated");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    if (dateCreated == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }
    return dateCreated.getTime();
  }

  // Non-project-specific file management

  /**
   * Adds file IDs to the user's list of non-project-specific files.
   *
   * @param userId a user Id (the request is made on behalf of this user)
   * @param fileNames list of file IDs to add to the projects source file list
   */
  @Override
  public void addFilesToUser(String strUserId, String... fileNames) {
    assert strUserId != null && fileNames.length > 0;
    long userId = Long.parseLong(strUserId, 16);

    try (PreparedStatement ustmt = this.conn.prepareStatement("INSERT INTO userFile (userId, fileName) VALUES (?, ?) ON CONFLICT (userId, fileName) DO NOTHING")) {
      for (String fileName : fileNames) {
        ustmt.setLong(1, userId);
        ustmt.setString(2, fileName);
        ustmt.addBatch();
      }

      ustmt.executeBatch();     // We don't care about the result
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }
  }

  /**
   * Returns a list of non-project-specific files for a user.
   *
   * @param userId a user Id
   * @return list of source file ID
   */
  @Override
  public List<String> getUserFiles(String strUserId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    List<String> ret = new ArrayList<String>();

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT fileName FROM userFile WHERE userId = ?")) {
      qstmt.setLong(1, userId);
      ResultSet rs = qstmt.executeQuery();

      while (rs.next()) {
        ret.add(rs.getString("fileName"));
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
    }

    return ret;
  }

  /**
   * Uploads a non-project-specific file.
   *
   * @param userId user ID
   * @param fileName file ID
   * @param content file content
   * @param encoding encoding of content
   */
  @Override
  public void uploadUserFile(String strUserId, String fileName, String content, String encoding) {
    byte[] contentBytes;
    try {
      contentBytes = content.getBytes(content);
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot decode file content", e);
    }
    uploadRawUserFile(strUserId, fileName, contentBytes);
  }

  /**
   * Uploads a non-project-specific file.
   *
   * @param userId user ID
   * @param content file content
   * @param fileName file name
   */
  @Override
  public void uploadRawUserFile(String strUserId, String fileName, byte[] content) {
    assert strUserId != null && fileName != null && content != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO userFile (userId, fileName, content) VALUES (?, ?, ?) ON CONFLICT (userId, fileName) DO UPDATE SET content = ?")) {
      ustmt.setLong(1, userId);
      ustmt.setString(2, fileName);
      ustmt.setBytes(3, content);
      ustmt.setBytes(4, content);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), e);
    }

    assert ret > 0;
  }

  /**
   * Downloads text user file data.
   *
   * @param userId a user Id
   * @param fileName file ID
   * @param encoding encoding of text file
   *
   * @return text file content
   */
  @Override
  public String downloadUserFile(String strUserId, String fileName, String encoding) {
    assert strUserId != null && fileName != null && encoding != null;
    long userId = Long.parseLong(strUserId, 16);
    byte[] contentBytes = downloadRawUserFile(strUserId, fileName);
    String content = null;

    try {
      content = contentBytes != null ? new String(contentBytes, encoding) : null;
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), e);
    }
    return content;
  }

  /**
   * downloads raw user file data.
   *
   * @param userId a user Id
   * @param fileName file name
   *
   * @return file content
   */
  @Override
  public byte[] downloadRawUserFile(String strUserId, String fileName) {
    assert strUserId != null && fileName != null;
    long userId = Long.parseLong(strUserId, 16);
    byte[] ret = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT content FROM userFile WHERE userId = ? AND fileName = ?")) {
      qstmt.setLong(1, userId);
      qstmt.setString(2, fileName);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        ret = rs.getBytes("content");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), e);
    }

    if (ret == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), new RuntimeException(""));
    }
    return ret;
  }

  /**
   * Deletes a user file.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param fileName  file ID
   */
  @Override
  public void deleteUserFile(String strUserId, String fileName) {
    assert strUserId != null && fileName != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM userFile WHERE userId = ? AND fileName = ?")) {
      ustmt.setLong(1, userId);
      ustmt.setString(2, fileName);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), new RuntimeException(""));
    }
  }

  // File management

  /**
   * Returns the maximum allowed job size in bytes.
   *
   * @return int maximum job size in bytes
   */
  public int getMaxJobSizeBytes() {
    throw new UnsupportedOperationException("Do not call getMaxJobSizeBytes()");
  }

  /**
   * Adds file IDs to the project's list of source files, updating the
   * modification date of the project if requested.  Note that no
   * modification date is returned.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param changeModDate  update the modification time for the project
   * @param fileNames  list of file IDs to add to the projects source file list
   */
  @Override
  public void addSourceFilesToProject(
    String strUserId,
    long projectId,
    boolean changeModDate,
    String...fileNames) {

    assert strUserId != null && fileNames.length > 0;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try {

      // Update project modified date
      try (PreparedStatement ustmt = this.conn.prepareStatement("UPDATE project SET dateModified = CURRENT_TIMESTAMP WHERE projectId = ? AND userId = ?")) {
        ustmt.setLong(1, projectId);
        ustmt.setLong(2, userId);

        ret = ustmt.executeUpdate();
      }

      if (ret == 0) {
        try {
          this.conn.rollback(save);
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Cannot find project"));
      }

      // Create files
      this.bulkCreateProjectFile(userId, projectId, FileData.RoleEnum.SOURCE, fileNames, save);
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }
  }

  /**
   * add file IDs to the project's list of output files.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileNames  list of file IDs to add to the projects output file list
   */
  @Override
  public void addOutputFilesToProject(String strUserId, long projectId, String...fileNames) {
    assert strUserId != null && fileNames.length > 0;
    long userId = Long.parseLong(strUserId, 16);

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try {
      this.bulkCreateProjectFile(userId, projectId, FileData.RoleEnum.TARGET, fileNames, save);
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }
  }

  /**
   * Removes file IDs from the project's list of source files, updating the
   * modification date of the project if requested.  Note that no
   * modification date is returned.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param changeModDate  update the modification time for the project
   * @param fileNames  list of file IDs to add to the projects source file list
   */
  @Override
  public void removeSourceFilesFromProject(
    String strUserId,
    long projectId,
    boolean changeModDate,
    String...fileNames) {

    assert strUserId != null && fileNames.length > 0;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    // Update project modified date
    try (PreparedStatement ustmt = this.conn.prepareStatement("UPDATE project SET dateModified = CURRENT_TIMESTAMP WHERE projectId = ? AND userId = ?")) {
      ustmt.setLong(1, projectId);
      ustmt.setLong(2, userId);
      ret = ustmt.executeUpdate();

      if (ret == 0) {
        this.conn.rollback(save);
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
      }

      this.bulkDeleteProjectFile(userId, projectId, FileData.RoleEnum.SOURCE, fileNames, save);
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }
  }

  /**
   * Removes file IDs from the project's list of output files.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileNames  list of file IDs to add to the projects source file list
   */
  @Override
  public void removeOutputFilesFromProject(String strUserId, long projectId, String...fileNames) {
    assert strUserId != null && fileNames.length > 0;
    long userId = Long.parseLong(strUserId, 16);

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try {
      this.bulkDeleteProjectFile(userId, projectId, FileData.RoleEnum.TARGET, fileNames, save);
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }
  }

  /**
   * Returns a list of source files for a project.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   *
   * @return  list of source file ID
   */
  @Override
  public List<String> getProjectSourceFiles(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    List<String> ret = new ArrayList<String>();

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT fileName FROM projectFile WHERE userId = ? AND projectId = ? AND role = ?")) {
      String roleString = FileData.RoleEnum.SOURCE.name();
      qstmt.setLong(1, userId);
      qstmt.setLong(2, projectId);
      qstmt.setString(3, roleString);
      ResultSet rs = qstmt.executeQuery();

      while (rs.next()) {
        ret.add(rs.getString("fileName"));
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    return ret;
  }

  /**
   * Returns a list of output files for a project.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   *
   * @return  list of output file ID
   */
  @Override
  public List<String> getProjectOutputFiles(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    List<String> ret = new ArrayList<String>();

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT fileName FROM projectFile WHERE userId = ? AND projectId = ? AND role = ?")) {
      String roleString = FileData.RoleEnum.TARGET.name();
      qstmt.setLong(1, userId);
      qstmt.setLong(2, projectId);
      qstmt.setString(3, roleString);
      ResultSet rs = qstmt.executeQuery();

      while (rs.next()) {
        ret.add(rs.getString("fileName"));
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    return ret;
  }

  /**
   * Returns the gallery id for a project.
   * @param projectId  project ID
   *
   * @return  list of output file ID
   */
  @Override
  public long getProjectGalleryId(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    Long galleryId = null;
    boolean found = false;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT galleryId FROM project WHERE projectId = ? AND userId = ?")) {
      qstmt.setLong(1, projectId);
      qstmt.setLong(2, userId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        galleryId = rs.getLong("galleryId");
        found = true;
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    if (!found) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException(""));
    }
    return galleryId != null ? galleryId : UserProject.NOTPUBLISHED;
  }

  /**
   * Returns the attribution id for a project-- the app it was copied/remixed from
   * @param projectId  project ID
   *
   * @return galleryId
   */
  @Override
  public long getProjectAttributionId(final long projectId) {
    Long attributionId = null;
    boolean found = false;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT attributionId FROM project WHERE projectId = ?")) {
      qstmt.setLong(1, projectId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        attributionId = rs.getLong("attributionId");
        found = true;
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, projectId, null), e);
    }

    if (!found) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, projectId, null), new RuntimeException(""));
    }
    return attributionId != null ? attributionId : UserProject.FROMSCRATCH;
  }

  /**
   * Uploads a file.
   * @param projectId  project ID
   * @param fileName  file name
   * @param userId the user who owns the file
   * @param content  file content
   * @param encoding encoding of content
   * @return modification date for project
   */
  @Override
  public long uploadFile(
    long projectId,
    String fileName,
    String strUserId,
    String content,
    String encoding)
    throws BlocksTruncatedException {

    assert strUserId != null && fileName != null && content != null && encoding != null;
    long userId = Long.parseLong(strUserId, 16);
    long ts;

    byte[] contentBytes;
    try {
      contentBytes = content.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding, " + makeErrorMsg(null, projectId, fileName), e);
    }

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try {
      ts = updateProjectFileContent(strUserId, projectId, fileName, contentBytes, false, save);
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
    }

    return ts;
  }

  /**
   * Uploads a file. -- This version uses "force" to write even a trivial workspace file
   * @param projectId  project ID
   * @param fileName  file ID
   * @param userId the user who owns the file
   * @param content  file content
   * @param encoding encoding of content
   * @return modification date for project
   */
  @Override
  public long uploadFileForce(
    long projectId,
    String fileName,
    String strUserId,
    String content,
    String encoding)  {

    assert strUserId != null && fileName != null && content != null && encoding != null;
    long userId = Long.parseLong(strUserId, 16);
    long ts;

    byte[] contentBytes;
    try {
      contentBytes = content.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding," + makeErrorMsg(null, projectId, fileName), e);
    }

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try {
      ts = updateProjectFileContent(strUserId, projectId, fileName, contentBytes, true, save);
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
    }

    return ts;
  }

  /**
   * Uploads a file.
   * @param projectId  project ID
   * @param fileName  file ID
   * @param userId the user who owns the file
   * @param force write file even if it is a trivial workspace
   * @param content  file content
   * @return modification date for project
   */
  @Override
  public long uploadRawFile(
    long projectId,
    String fileName,
    String strUserId,
    boolean force,
    byte[] content) {

    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    long ts;

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try {
      ts = updateProjectFileContent(strUserId, projectId, fileName, content, false, save);
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
    }

    return ts;
  }

  /**
   * Uploads a file. -- forces the save even with trivial workspace
   * @param projectId  project ID
   * @param fileName  file ID
   * @param userId the user who owns the file
   * @param content  file content
   * @return modification date for project
   */
  @Override
  public long uploadRawFileForce(long projectId, String fileName, String strUserId, byte[] content) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    long ts;

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try {
      ts = updateProjectFileContent(strUserId, projectId, fileName, content, true, save);
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
    }
    return ts;
  }

  /**
   * Deletes a file.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileName  file ID
   * @return modification date for project
   */
  @Override
  public long deleteFile(String strUserId, long projectId, String fileName) {
    assert strUserId != null && fileName != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;
    long ts;

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try {
      try (PreparedStatement ustmt = this.conn.prepareStatement("DELETE FROM projectFile WHERE projectId = ? AND userId = ? AND fileName = ?")) {
        ustmt.setLong(1, projectId);
        ustmt.setLong(2, userId);
        ustmt.setString(3, fileName);
        ret = ustmt.executeUpdate();
      }

      if (ret == 0) {
        this.conn.rollback(save);
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), new RuntimeException(""));
      }

      ts = updateProjectModifiedDate(projectId, userId, fileName, save);
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
    }

    return ts;
  }

  /**
   * Downloads text file data.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileName  file ID
   * @param encoding  encoding of text file
   *
   * @return  text file content
   */
  @Override
  public String downloadFile(String strUserId, long projectId, String fileName, String encoding) {
    assert strUserId != null && fileName != null && encoding != null;
    long userId = Long.parseLong(strUserId, 16);
    byte[] contentBytes = downloadProjectFile(userId, projectId, fileName);
    String content = null;

    try {
      content = new String(contentBytes, encoding);
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding, " + makeErrorMsg(userId, projectId, fileName), e);
    }
    return content;
  }

  /**
   * Records a "corruption" record so we can analyze if corruption is
   * happening.
   *
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param message The message from the exception on the client
   */
  @Override
  public void recordCorruption(String strUserId, long projectId, String fileName, String message) {
    assert strUserId != null && fileName != null && message != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO corruptionReport (userId, projectId, fileName, message) VALUES (?, ?, ?, ?)")) {
      ustmt.setLong(1, userId);
      ustmt.setLong(2, projectId);
      ustmt.setString(3, fileName);
      ustmt.setString(4, message);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
    }

    assert ret > 0;
  }

  /**
   * Downloads raw file data.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileName  file ID
   *
   * @return  file content
   */
  @Override
  public byte[] downloadRawFile(String strUserId, long projectId, String fileName) {
    assert strUserId != null && fileName != null;
    long userId = Long.parseLong(strUserId, 16);
    byte[] contentBytes = downloadProjectFile(userId, projectId, fileName);
    return contentBytes;
  }

  /**
   * Creates a temporary file with the given content and returns
   * its file name, which will always begin with __TEMP__
   * @param content the files content (bytes)
   *
   * @return fileName the temporary fileName
   */
  @Override
  public String uploadTempFile(byte [] content) throws IOException {
    assert content != null;
    Long fileId = null;
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO tempFile (content) VALUES (?)")) {
      ustmt.setBytes(1, content);
      ret = ustmt.executeUpdate();

      if (ret > 0) {
        try (ResultSet keys = ustmt.getGeneratedKeys()) {
          if (keys.next()) {
            fileId = keys.getLong(1);
          }
        }
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Failed to create temp file", e);
    }

    if (fileId == null) {
      throw CrashReport.createAndLogError(LOG, null, "Database error in uploadTempFile()", new RuntimeException(""));
    }

    String fileName = "__TEMP__" + Long.toHexString(fileId);
    return fileName;
  }

  /**
   * Open an input stream to a temp file.
   * Verifies it is a temp file by making sure the fileName
   * begins with __TEMP__
   *
   * @param fileName
   *
   * @return inputstream
   */
  @Override
  public InputStream openTempFile(String fileName) throws IOException {
    assert fileName != null;

    if (!fileName.substring(0, 8).equals("__TEMP__")) {
      throw new IllegalArgumentException("fileName argument should start with \"__TEMP__\", but get \"" + fileName + "\"");
    }

    // Get file id
    long fileId = Long.parseLong(fileName.substring(8), 16);
    InputStream stream = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT content FROM tempFile WHERE id = ?")) {
      qstmt.setLong(1, fileId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        stream = rs.getBinaryStream("content");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, null, fileName), e);
    }

    if (stream == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, null, fileName), new RuntimeException(""));
    }
    return stream;
  }

  /**
   * delete a temporary file.
   * Verify that it is a temporary file by making sure its fileName
   * starts with __TEMP__
   *
   * @param fileName
   */
  @Override
  public void deleteTempFile(String fileName) throws IOException {
    assert fileName != null;

    // Get file id
    if (!fileName.substring(0, 8).equals("__TEMP__")) {
      throw new IllegalArgumentException("fileName argument should start with \"__TEMP__\", but get \"" + fileName + "\"");
    }
    long fileId = Long.parseLong(fileName.substring(8), 16);
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM tempFile WHERE id = ?")) {
      ustmt.setLong(1, fileId);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Failed to delete temp file " + fileName, e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, null, fileName), new RuntimeException(""));
    }
  }

  // MOTD management

  /**
   * Returns the most recent motd.
   *
   * @return  motd
   */
  @Override
  public Motd getCurrentMotd() {
    final String MOTD_CONFIG_KEY = "motd_captain";
    final String DEFAULT_CAPTAIN = "Hello!";
    final String DEFAULT_CONTENT = "Welcome to the experimental App Inventor system from MIT. " +
      "This is still a prototype.  It would be a good idea to frequently back up " +
      "your projects to local storage.";
    Motd motd = null;

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try {
      String configText = null;
      boolean requireReset = false;

      // Query motd data
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT value FROM misc WHERE key = ?")) {
        qstmt.setString(1, MOTD_CONFIG_KEY);
        ResultSet rs = qstmt.executeQuery();
        if (rs.next()) {
          configText = rs.getString("value");
        }
      }

      // Decode JSON
      try {
        if (configText != null) {
          JSONObject config = new JSONObject(configText);
          String captain = config.getString("captain");
          String content = config.getString("content");
          motd = new Motd(0, captain, content); // 0 is dummy ID
        } else {
          requireReset = true;
        }
      } catch (JSONException e) {
        LOG.log(Level.WARNING, "Motd data in database is corrupted");
        requireReset = true;
      }

      // Run insertion if it's missing
      if (requireReset) {
        motd = new Motd(0, DEFAULT_CAPTAIN, DEFAULT_CONTENT); // 0 is dummy ID

        JSONObject config = new JSONObject();
        config.put("captain", DEFAULT_CAPTAIN);
        config.put("content", DEFAULT_CONTENT);

        int ret = 0;
        try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO misc (key, value) VALUES (?, ?) ON CONFLICT (key) DO UPDATE SET value = ?")) {
          String configString = config.toString();
          ustmt.setString(1, MOTD_CONFIG_KEY);
          ustmt.setString(2, configString);
          ustmt.setString(3, configString);
          ret = ustmt.executeUpdate();
        }

        if (ret == 0) {
          this.conn.rollback(save);
          throw CrashReport.createAndLogError(LOG, null, "Database error in getCurrentMotd()", new RuntimeException(""));
        }
      }

      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in getCurrentMotd()", e);
    }

    if (motd == null) {
      throw CrashReport.createAndLogError(LOG, null, "Database error in getCurrentMotd()", new RuntimeException(""));
    }

    return motd;
  }

  /**
   *  Exports project files as a zip archive
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param includeProjectHistory  whether or not to include the project history
   * @param includeAndroidKeystore  whether or not to include the Android keystore
   * @param zipName  the name of the zip file, if a specific one is desired
   * @param fatalError set true to cause missing GCS file to throw exception
   *
   * @return  project with the content as requested by params.
   */
  @Override
  public ProjectSourceZip exportProjectSourceZip(
    String strUserId,
    long projectId,
    boolean includeProjectHistory,
    boolean includeAndroidKeystore,
    @Nullable String zipName,
    final boolean includeYail,
    final boolean includeScreenShots,
    final boolean forGallery,
    final boolean fatalError) throws IOException {

    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    List<FileData> files = new ArrayList<FileData>();
    boolean projectFound = false;
    String projectName = null;
    String projectHistory = null;
    byte[] keystoreContent = null;

    try {
      // Get project
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM project WHERE projectId = ? AND userId = ?")) {
        qstmt.setLong(1, projectId);
        qstmt.setLong(2, userId);
        ResultSet rs = qstmt.executeQuery();

        if (rs.next()) {
          projectFound = true;
          projectName = rs.getString("name");
          projectHistory = rs.getString("history");
        }
      }

      // If something went wrong...
      if (!projectFound) {
        this.conn.rollback();
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Canont find project"));
      }

      // Find project files
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM projectFile WHERE projectId = ? AND userId = ?")) {
        qstmt.setLong(1, projectId);
        qstmt.setLong(2, userId);

        ResultSet rs = qstmt.executeQuery();
        while (rs.next()) {
          FileData fd = new FileData();
          fd.fileName = rs.getString("fileName");
          fd.role = FileData.RoleEnum.valueOf(rs.getString("fileName"));
          fd.content = rs.getBytes("content");

          // Kick out some files
          if (fd.fileName.startsWith("assets/external_comps") && forGallery) {
            this.conn.rollback();
            throw new IOException("FATAL Error, external component in gallery app");
          }
          if (fd.fileName.equals(FileExporter.REMIX_INFORMATION_FILE_PATH)) {
            // Skip legacy remix history files that were previous stored with the project
            continue;
          }
          if (!fd.role.equals(FileData.RoleEnum.SOURCE)) {
            continue;
          }
          if (!includeScreenShots && fd.fileName.startsWith("screenshots")) {
            continue;
          }
          if ( !includeYail && fd.fileName.endsWith(".yail")) {
            continue;
          }

          files.add(fd);
        }
      }

      // Find Android keystore file
      if (includeAndroidKeystore) {
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM userFile WHERE userId = ? AND fileName = ? AND content IS NOT NULL AND length(content) > 0")) {
          qstmt.setLong(1, userId);
          qstmt.setString(1, StorageUtil.ANDROID_KEYSTORE_FILENAME);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            keystoreContent = rs.getBytes("content");
          }
        }
      }

      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    // Check if project found

    // Kick out non-file case
    if (files.size() == 0) {
      throw new IllegalStateException("No files to download");
    }

    // Create zip file
    // TODO use temp file instead in-memory buffer
    int fileCount = 0;

    ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
    final ZipOutputStream zipStream = new ZipOutputStream(zipFile);
    zipStream.setComment("Built with MIT App Inventor");

    for (FileData fd : files) { // project files
      byte[] content = fd.content != null ? fd.content : new byte[0];
      zipStream.putNextEntry(new ZipEntry(fd.fileName));
      zipStream.write(content, 0, content.length);
      zipStream.closeEntry();
      fileCount += 1;
    }

    if (projectHistory != null && includeProjectHistory) { // project history
      byte[] content = projectHistory.getBytes(StorageUtil.DEFAULT_CHARSET);
      zipStream.putNextEntry(new ZipEntry(FileExporter.REMIX_INFORMATION_FILE_PATH));
      zipStream.write(content, 0, content.length);
      zipStream.closeEntry();
      fileCount += 1;
    }

    if (keystoreContent != null) { // Android keystore
      zipStream.putNextEntry(new ZipEntry(StorageUtil.ANDROID_KEYSTORE_FILENAME));
      zipStream.write(keystoreContent, 0, keystoreContent.length);
      zipStream.closeEntry();
      fileCount += 1;
    }

    // Finalize
    zipStream.close();

    assert projectName != null;
    if (zipName == null) {
      zipName = projectName + ".aia";
    }
    ProjectSourceZip projectSourceZip =
      new ProjectSourceZip(zipName, zipFile.toByteArray(), fileCount);
    projectSourceZip.setMetadata(projectName);
    return projectSourceZip;
  }

  /**
   * find a user's id given their email address. Note that this query is case
   * sensitive!
   *
   * @param email user's email address
   *
   * @return the user's id if found
   * @throws NoSuchElementException if we can't find a user with that exact
   *    email address
   */
  @Override
  public String findUserByEmail(String email) throws NoSuchElementException {
    assert email != null;
    Long userId = null;

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    // Case-sensitive search
    try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM user WHERE email = ?")) {
      qstmt.setString(1, email);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        userId = rs.getLong("userId");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in findUserByEmail() with email=" + email, e);
    }

    if (userId == null) {
      throw new NoSuchElementException("Couldn't find a user with email: " + email);
    }
    String strUserId = Long.toHexString(userId);
    return strUserId = null;
  }

  /**
   * Find a phone's IP address given the six character key. Used by the
   * RendezvousServlet. This is used only when memcache is unavailable.
   *
   * @param key the six character key
   * @return Ip Address as string or null if not found
   *
   */
  @Override
  public String findIpAddressByKey(String key) {
    assert key != null;
    String addr = null;

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT address FROM ipAddress WHERE key = ?")) {
      qstmt.setString(1, key);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        addr = rs.getString("address");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in findIpAddressByKey()", e);
    }

    return addr;
  }

  /**
   * Store a phone's IP address indexed by six character key. Used by the
   * RendezvousServlet. This is used only when memcache is unavailable.
   *
   * Note: Nothing currently cleans up these entries, but we have a
   * timestamp field which we update so a later process can recognize
   * and remove stale entries.
   *
   * @param key the six character key
   * @param ipAddress the IP Address of the phone
   *
   */
  @Override
  public void storeIpAddressByKey(String key, String ipAddress) {
    assert key != null && ipAddress != null;
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO ipAddress (key, address) VALUES (?, ?) ON CONFLICT (key) DO UPDATE address = ?")) {
      ustmt.setString(1, key);
      ustmt.setString(2, ipAddress);
      ustmt.setString(3, ipAddress);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in storeIpAddressByKey()", e);
    }

    assert ret > 0;
  }

  @Override
  public boolean checkWhiteList(String email) {
    assert email != null;
    boolean pass = false;

    // Try to get existing user
    User user = null;
    try (PreparedStatement qstmt = conn.prepareStatement("SELECT COUNT(1) FROM whitelist WHERE lower(email) = lower(?)")) {
      qstmt.setString(1, email);
      ResultSet rs = qstmt.executeQuery();
      pass = rs.next();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in checkWhiteList()", e);
    }
    return pass;
  }

  @Override
  public void storeFeedback(
    final String notes,
    final String foundIn,
    final String faultData,
    final String comments,
    final String datestamp,
    final String email,
    final String projectId) {

    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement(
           "INSERT INTO feedback (notes, foundId, faultData, comments, datestamp, email, projectId) VALUES (?, ?, ?, ?, ?, ?, ?)"
           )) {
      ustmt.setString(1, notes);
      ustmt.setString(2, foundIn);
      ustmt.setString(3, faultData);
      ustmt.setString(4, comments);
      ustmt.setString(5, datestamp);
      ustmt.setString(6, email);
      ustmt.setString(7, projectId);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in storeFeedback()", e);
    }
    assert ret > 0;
  }

  @Override
  public Nonce getNoncebyValue(String nonceValue) {
    Nonce nonce = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM nonce WHERE nonce = ?")) {
      qstmt.setString(1, nonceValue);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        long userId = rs.getLong("userId");
        long projectId = rs.getLong("projectId");
        Timestamp ts = rs.getTimestamp("timestamp");

        Date dt = new Date(ts.getTime());
        String strUserId = Long.toHexString(userId);
        nonce = new Nonce(nonceValue, strUserId, projectId, dt);
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in getNonceByValue()", e);
    }

    return nonce;
  }

  @Override
  public void storeNonce(final String nonceValue, final String strUserId, final long projectId) {
    assert strUserId != null && nonceValue != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement(
           "INSERT INTO nonce (userId, projectId, nonce) VALUES (?, ?, ?) ON CONFLICT (nonce) DO UPDATE userId = ?, projectId = ?"
           )) {
      ustmt.setLong(1, userId);
      ustmt.setLong(2, projectId);
      ustmt.setString(3, nonceValue);
      ustmt.setLong(4, userId);
      ustmt.setLong(5, projectId);

      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in storeNonce()", e);
    }
    assert ret > 0;
  }

  // Cleanup expired nonces
  @Override
  public void cleanupNonces() {
    try (PreparedStatement ustmt = conn.prepareStatement(
           "DELETE FROM nonce WHERE timestamp < (CURRENT_TIMESTAMP - INTERVAL '3 hour')"
           )) {
      ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in cleanupNonces()", e);
    }
  }

  // Check to see if user needs projects upgraded (moved to GCS)
  // if so, add task to task queue
  @Override
  public void checkUpgrade(String strUserId) {
    // Do nothing here
  }

  // called by the task queue to actually upgrade user's projects
  @Override
  public void doUpgrade(String strUserId) {
    // Do nothing here
  }

  // Retrieve the current Splash Screen Version
  @Override
  public SplashConfig getSplashConfig() {
    final String SPLASH_CONFIG_KEY = "splash_config";
    final int DEFAULT_VERSION = 0;
    final String DEFAULT_CONTENT = "<b>Welcome to MIT App Inventor</b>";
    final int DEFAULT_WIDTH = 350;
    final int DEFAULT_HEIGHT = 100;

    SplashConfig result = null;

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    try {
      String configText = null;
      JSONObject config = null;
      boolean requireReset = false;

      try (PreparedStatement qstmt = conn.prepareStatement("SELECT value FROM misc WHERE key = ?")) {
        qstmt.setString(1, SPLASH_CONFIG_KEY);
        ResultSet rs = qstmt.executeQuery();
        if (rs.next()) {
          configText = rs.getString("value");
        }
      }

      // Parse config
      try {
        if (configText != null) {
          config = new JSONObject(configText);
          int version = config.getInt("version");
          String content = config.getString("content");
          int width = config.getInt("width");
          int height = config.getInt("height");
          result = new SplashConfig(version, width, height, content);
        } else {
          requireReset = true;
        }
      } catch (JSONException e) {
        LOG.log(Level.WARNING, "The splash config in database is correupted");
        requireReset = true;
      }

      // Reset config if required
      if (requireReset) {
        config = new JSONObject();
        config.put("version", DEFAULT_VERSION);
        config.put("content", DEFAULT_CONTENT);
        config.put("width", DEFAULT_WIDTH);
        config.put("height", DEFAULT_HEIGHT);
        result = new SplashConfig(DEFAULT_VERSION, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_CONTENT);

        int ret = 0;
        try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO misc (key, value) VALUES (?, ?) ON CONFLICT (key) DO UPDATE value = ?")) {
          String configString = config.toString();
          ustmt.setString(1, SPLASH_CONFIG_KEY);
          ustmt.setString(2, configString);
          ustmt.setString(3, configString);
          ret = ustmt.executeUpdate();
        }

        if (ret == 0) {
          this.conn.rollback(save);
          throw CrashReport.createAndLogError(LOG, null, "Database error in getSplashConfig()", new RuntimeException(""));
        }

        this.conn.commit();
      }
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in getSplashConfig()", e);
    }
    return result;
  }

  @Override
  public StoredData.PWData createPWData(String email) {
    assert email != null;
    int ret = 0;
    String uuid = null;
    Timestamp ts = null;

    Savepoint save;
    try {
      save = this.conn.setSavepoint();
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot set save point", e);
    }

    // It has (very) low probability to conflicts on colliding uuid.
    // If you have bad luck. We fail it anyway.
    try {
      try (PreparedStatement ustmt = conn.prepareStatement(
             "INSERT INTO pwData (uuid, email) VALUES (uuid_generate_v4(), ?) ON CONFLICT (uuid) DO UPDATE SET uuid = uuid_generate_v4()"
             )) {
        ustmt.setString(1, email);
        ret = ustmt.executeUpdate();
        if (ret > 0) {
          try (ResultSet keys = ustmt.getGeneratedKeys()) {
            if (keys.next()) {
              uuid = keys.getString(1);
            }
          }
        }
      }

      // Get timestamp
      if (uuid != null) {
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT timestamp FROM pwData WHERE uuid = ?")) {
          qstmt.setString(1, uuid);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            ts = rs.getTimestamp("timestamp");
          }
        }
      }

      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in createPWData()", e);
    }

    // Finalize
    if (ts == null) {
      throw CrashReport.createAndLogError(LOG, null, "Database error in createPWData()", new RuntimeException(""));
    }
    PWData result = new PWData();
    result.id = uuid;
    result.email = email;
    result.timestamp = new Date(ts.getTime());
    return result;
  }

  @Override
  public StoredData.PWData findPWData(String uuid) {
    PWData ret = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM pwData WHERE uuid = ?")) {
      qstmt.setString(1, uuid);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        String id = rs.getString("id");
        String email = rs.getString("email");
        Timestamp ts = rs.getTimestamp("timestamp");
        Date dt = new Date(ts.getTime());

        ret = new PWData();
        ret.id = id;
        ret.email = email;
        ret.timestamp = dt;
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in findPWData()", e);
    }

    return ret;
  }

  @Override
  public void cleanuppwdata() {
    try (PreparedStatement ustmt = conn.prepareStatement(
           "DELETE FROM pwData WHERE timestamp < (CURRENT_TIMESTAMP - INTERVAL '1 day')"
           )) {
      ustmt.executeUpdate();    // Don't care about the result
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in cleanuppwdata()", e);
    }
  }

  // Routines for user admin interface

  @Override
  public List<AdminUser> searchUsers(String partialEmail) {
    List<AdminUser> ret = new ArrayList<AdminUser>();

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM user WHERE email LIKE CONCAT(?, '%') LIMIT 20")) {
      qstmt.setString(1, partialEmail);
      ResultSet rs = qstmt.executeQuery();

      while (rs.next()) {
        long userId = rs.getLong("userId");
        String strUserId = Long.toHexString(userId);
        Timestamp visitedTs = rs.getTimestamp("visited");
        Date visitedDt = new Date(visitedTs.getTime());
        AdminUser user = new AdminUser(
          strUserId,
          rs.getString("name"),
          rs.getString("email"),
          rs.getBoolean("tosAccepted"),
          rs.getBoolean("isAdmin"),
          rs.getInt("type") == User.MODERATOR,
          visitedDt);
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in searchUsers()", e);
    }

    return ret;
  }

  @Override
  public void storeUser(AdminUser user) throws AdminInterfaceException {
    // The implementation has distinct behavior from ObjectifyStorageIo.storeUser()
    // We require non-null user.getId() and existince of this user.

    assert user != null;
    int ret = 0;

    String strUserId = user.getId();
    long userId = Long.parseLong(strUserId, 16);
    String newPassword = user.getPassword();
    if (newPassword.equals("")) {
      newPassword = null;
    }
    String newEmail = user.getEmail();
    boolean newIsAdmin = user.getIsAdmin();
    int newType = user.getIsModerator() ? User.MODERATOR : User.USER;

    try (PreparedStatement ustmt = conn.prepareStatement("UPDATE user SET email = ?, isAdmin = ?, type = ?, password = COALESCE(?, password) WHERE userId = ?")) {
      ustmt.setString(1, newEmail);
      ustmt.setBoolean(2, newIsAdmin);
      ustmt.setInt(3, newType);
      ustmt.setString(4, newPassword);
      ustmt.setLong(5, userId);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      // If it conflicts on email, we fail it anyway.
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in storeUser()", e);
    }

    if (ret == 0) {
      throw new AdminInterfaceException("Cannot find user with userId = " + strUserId);
    }
  }

  /**
   * There are two kinds of backpacks. User backpacks, which are
   * stored with the user's personal files (which today is just the
   * backpack and the android keystore used for signing applications.
   * The second kind of backpack is a shared backpack. It is
   * identified by a uuid.  This code is associated with the shared
   * backpack. Shared backpacks are used when a person is logged in
   * via the SSO mechanism. It can optionally specify a backpack to
   * use. If it doesn't specify a backpack, then the normal user
   * specific version is used.
   *
   * @param backPackId uuid used to idenfity this backpack
   * @return the contents of the backpack as an XML encoded string
   */
  @Override
  public String downloadBackpack(String backPackId) {
    assert backPackId != null;
    String ret = "[]";

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT content FROM backpack WHERE id = ?")) {
      qstmt.setString(1, backPackId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        ret = rs.getString("content");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in downloadBackpack()", e);
    }

    return ret;
  }

  /**
   * Used to upload a shared backpack Note: This code will over-write
   * whatever contents is already stored in the the backpack. It is
   * the responsibility of our caller to merge contents if desired.
   *
   * @param backPackId The uuid of the shared backpack to store
   * @param String content the new contents of the backpack
   */
  @Override
  public void uploadBackpack(String backPackId, String content) {
    int ret = 0;
    try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO backpack (id, content) VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET content = ?")) {
      ustmt.setString(1, backPackId);
      ustmt.setString(2, content);
      ustmt.setString(3, content);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in uploadBackpack()", e);
    }

    assert ret > 0;
  }

  /**
   * Store the status of a pending build. We used to poll the buildserver
   * for the progress of a build. However that was never correct as while
   * polling you would likely wind up talking to a different buildserver
   * then you originally started with! So now the buildserver does a callback
   * to the server indicating progress. Here is where we store that
   * progress. The reason we do this in this module is because we have
   * different versions of storageio for our three (so far) backends, the
   * App Engine based version, the stand alone version and the "scale-able"
   * version. Each version will likely want to store this information in
   * a different fashion.
   *
   * Note: The App Engine version uses memcache and if memcache isn't
   * available (yes, it can be down!) then we cheat and just return
   * 50 (for 50%).
   *
   */
  @Override
  public void storeBuildStatus(String strUserId, long projectId, int progress) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO buildStatus (host, userId, projectId, progress) VALUES (?, ?, ?, ?) ON CONFLICT (userId, projectId) DO UPDATE SET progress = ?")) {
      ustmt.setString(1, HOST_ID);
      ustmt.setLong(2, userId);
      ustmt.setLong(3, projectId);
      ustmt.setInt(4, progress);
      ustmt.setInt(5, progress);
      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in storeBuildStatus()", e);
    }

    assert ret > 0;
  }

  @Override
  public int getBuildStatus(String strUserId, long projectId) {
    assert strUserId != null;
    long userId = Long.parseLong(strUserId, 16);
    Integer ret = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT progress FROM buildStatus WHERE userId = ? AND projectId = ?")) {
      qstmt.setLong(1, userId);
      qstmt.setLong(2, projectId);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        ret = rs.getInt("progress");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in getBuildStatus()", e);
    }

    return ret != null ? ret : 50;
  }

  private User createUser(String email, int type, boolean isAdmin, String password, Savepoint save) {
    assert email != null;

    // Insert new user
    Long userId = null;
    String name = User.getDefaultName(email);
    int emailFrequency = User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY;
    boolean tosAccepted = false;
    String nonEmptyPassword = password == null || password.equals("") ? null : password;
    int ret = 0;

    try (PreparedStatement ustmt = conn.prepareStatement(
           "INSERT INTO user (name, type, emailFrequency, tosAccepted, isAdmin, email, password) VALUES (?, ?, ?, ?, ?, ?, ?)"
           )) {
      ustmt.setString(1, name);
      ustmt.setInt(2, type);
      ustmt.setInt(3, emailFrequency);
      ustmt.setBoolean(4, tosAccepted);
      ustmt.setBoolean(5, isAdmin);
      ustmt.setString(6, email);
      ustmt.setString(7, nonEmptyPassword);
      ret = ustmt.executeUpdate();

      // Get userId
      if (ret > 0) {
        try (ResultSet keys = ustmt.getGeneratedKeys()) {
          if (keys.next()) {
            userId = keys.getLong(1);
          }
        }
      }
    } catch (SQLException e) {
      // One possible case is that two servers insert users of the same email.
      // We fail one of them.
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in createUser()", e);
    }

    if (userId == null) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "email=" + email, new RuntimeException(""));
    }

    String strUserId = Long.toHexString(userId);
    User user = new User(
      strUserId,
      email,
      name,
      null,                   // link
      emailFrequency,
      tosAccepted,
      isAdmin,
      type,
      null                    // sessionId
      );
    user.setPassword(nonEmptyPassword);
    return user;
  }

  private void createProjectFile(
    long projectId,
    long userId,
    FileData.RoleEnum role,
    String fileName,
    byte[] content,
    Savepoint save) {

    int ret = 0;
    String roleString = role.name();

    try (PreparedStatement ustmt = this.conn.prepareStatement(
           "INSERT INTO projectFile (projectId, userId, role, fileName, content) VALUES (?, ?, ?, ?, ?) ON CONFLICT (projectId, userId, fileName) DO UPDATE SET role = ?, content = ?"
           )) {
      ustmt.setLong(1, projectId);
      ustmt.setLong(2, userId);
      ustmt.setString(3, roleString);
      ustmt.setString(4, fileName);
      ustmt.setBytes(5, content);
      ustmt.setString(6, roleString);
      ustmt.setBytes(7, content);

      ret = ustmt.executeUpdate();
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
    }

    assert ret > 0;
  }

  private void bulkCreateProjectFile(
    long userId,
    long projectId,
    FileData.RoleEnum role,
    String[] fileNames,
    Savepoint save) {

    String roleString = role.name();

    try (PreparedStatement ustmt = this.conn.prepareStatement(
           "INSERT INTO projectFile (projectId, userId, role, fileName) VALUES (?, ?, ?, ?) ON CONFLICT (projectId, userId, fileName) DO NOTHING"
           )) {
      for (String fname : fileNames) {
        ustmt.setLong(1, projectId);
        ustmt.setLong(2, userId);
        ustmt.setString(3, roleString);
        ustmt.setString(4, fname);
        ustmt.addBatch();
      }

      ustmt.executeBatch();     // We don't care about the result
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      String strUserId = Long.toHexString(userId);
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

  }

  private void bulkDeleteProjectFile(
    long userId,
    long projectId,
    FileData.RoleEnum role,
    String[] fileNames,
    Savepoint save) {

    assert role != null && fileNames != null && fileNames.length > 0 && save != null;
    String roleString = role.name();

    try (PreparedStatement ustmt = this.conn.prepareStatement("DELETE FROM projectFile WHERE projectId = ? AND userId = ? AND fileName = ?")) {
      for (String fname : fileNames) {
        ustmt.setLong(1, projectId);
        ustmt.setLong(2, userId);
        ustmt.setString(3, fname);
        ustmt.addBatch();
      }

      ustmt.executeBatch();     // We don't care about the result
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }

      String strUserId = Long.toHexString(userId);
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

  }

  private long updateProjectFileContent(
    String strUserId,
    long projectId,
    String fileName,
    byte[] content,
    boolean force,
    Savepoint save) {

    assert strUserId != null && fileName != null && content != null && save != null;
    long userId = Long.parseLong(strUserId, 16);
    int ret = 0;

    // Update project modified date
    try (PreparedStatement ustmt = this.conn.prepareStatement("UPDATE projectFile SET content = ? WHERE projectId = ? AND userId = ? AND fileName = ?")) {
      ustmt.setBytes(1, content);
      ustmt.setLong(2, projectId);
      ustmt.setLong(3, userId);
      ustmt.setString(4, fileName);
      ret = ustmt.executeUpdate();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
    }

    if (ret == 0) {             // Fail if such file is not found
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), new RuntimeException(""));
    }

    long ts = updateProjectModifiedDate(userId, projectId, fileName, save);
    return ts;
  }

  private byte[] downloadProjectFile(long userId, long projectId, String fileName) {
    assert fileName != null;
    byte[] contentBytes = null;

    try (PreparedStatement qstmt = conn.prepareStatement("SELECT content FROM projectFile WHERE projectId = ? AND userId = ? AND fileName = ?")) {
      qstmt.setLong(1, userId);
      qstmt.setLong(2, projectId);
      qstmt.setString(3, fileName);
      ResultSet rs = qstmt.executeQuery();

      if (rs.next()) {
        contentBytes = rs.getBytes("content");
      }
      this.conn.commit();
    } catch (SQLException e) {
      try {
        this.conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
    }

    if (contentBytes == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), new RuntimeException(""));
    }
    return contentBytes;
  }

  private String makeErrorMsg(
    final Long userId,
    final Long projectId,
    final String fileName) {

    assert userId != null || projectId != null && fileName != null;
    String strUserId = userId != null ? Long.toHexString(userId) : null;

    String userIdToken = strUserId != null ? ("userId=\"" + strUserId + "\"") : null;
    String projectIdToken = strUserId != null ? ("projectId=\"" + strUserId + "\"") : null;
    String fileNameIdToken = strUserId != null ? ("fileName=\"" + fileName + "\"") : null;

    // No String.join() function in JDK 1.7. We do it manually.
    String ret = null;
    ret = userIdToken != null ? userIdToken : ret;
    ret = projectIdToken != null ? (ret != null ? (ret + ", " + projectIdToken) : projectIdToken) : ret;
    ret = fileName != null ? (ret != null ? (ret + ", " + fileName) : fileName) : ret;

    return ret;
  }

  long updateProjectModifiedDate(long userId, long projectId, String fileName, Savepoint save) {
    int ret = 0;
    Timestamp modifiedTs = null;

    try (PreparedStatement ustmt = this.conn.prepareStatement("UPDATE project SET modifiedDate = CURRENT_TIMESTAMP WHERE projectId = ? AND userId = ?")) {
      ustmt.setLong(1, projectId);
      ustmt.setLong(2, userId);
      ret = ustmt.executeUpdate();
    } catch (SQLException e) {
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
    }

    if (ret > 0) {              // If timestamp update suceeds
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT modifiedDate FROM project WHERE projectId = ? AND userId = ?")) {
        qstmt.setLong(1, projectId);
        qstmt.setLong(2, userId);
        ResultSet rs = qstmt.executeQuery();

        if (rs.next()) {
          modifiedTs = rs.getTimestamp("modifiedDate");
        }
      } catch (SQLException e) {
        try {
          this.conn.rollback(save);
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
      }
    }

    if (modifiedTs == null) {             // No such project found
      try {
        this.conn.rollback(save);
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), new RuntimeException(""));
    }

    return modifiedTs.getTime();
  }
}
