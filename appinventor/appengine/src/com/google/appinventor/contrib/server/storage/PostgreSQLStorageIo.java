// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.contrib.server.storage;

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
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StoredData.FileData;
import com.google.appinventor.server.storage.StoredData.PWData;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.math.BigInteger;
import java.io.FileNotFoundException;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.beans.PropertyVetoException;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

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

  private final ComboPooledDataSource cpds;

  public PostgreSQLStorageIo() {
    // Setup connection
    try {
      this.cpds = new ComboPooledDataSource();
      this.cpds.setDriverClass("org.postgresql.Driver");
      this.cpds.setJdbcUrl(jdbcUrl.get());
      this.cpds.setUser(jdbcUser.get());
      this.cpds.setPassword(jdbcPassword.get());
      this.cpds.setMaxStatements(180);
      this.cpds.setAutoCommitOnClose(false);
    } catch (PropertyVetoException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot setup database connection pool", e);
    }

    // Initialize database
    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);
        try (Statement stmt = conn.createStatement()) {
          stmt.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");
          stmt.execute("CREATE TABLE IF NOT EXISTS account (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  email TEXT UNIQUE," +
                       "  name TEXT NOT NULL CHECK (name <> '')," +
                       "  link TEXT," +
                       "  emailFrequency INT NOT NULL CHECK (emailFrequency >= 0)," +
                       "  tosAccepted BOOLEAN DEFAULT FALSE NOT NULL," +
                       "  isAdmin BOOLEAN DEFAULT FALSE NOT NULL," +
                       "  isReadOnly BOOLEAN DEFAULT FALSE NOT NULL," +
                       "  type INT NOT NULL," +
                       "  sessionId TEXT," +
                       "  password TEXT CHECK (password <> '')," +
                       "  backPackId TEXT," +
                       "  settings TEXT NOT NULL DEFAULT ''," +
                       "  visited TIMESTAMPTZ" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS project (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  userId BIGINT NOT NULL REFERENCES account ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  name TEXT NOT NULL CHECK (name <> '')," +
                       "  link TEXT," +
                       "  type TEXT," +
                       "  settings TEXT NOT NULL DEFAULT ''," +
                       "  creationDate TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                       "  modifiedDate TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                       "  history TEXT," +
                       "  galleryId BIGINT," +
                       "  attributionId BIGINT" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS projectFile (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  projectId BIGINT NOT NULL REFERENCES project ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  userId BIGINT NOT NULL REFERENCES account ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  fileName TEXT NOT NULL," +
                       "  role TEXT NOT NULL," +
                       "  content BYTEA NOT NULL DEFAULT ''," +
                       "  UNIQUE (projectId, userId, fileName)" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS userFile (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  userId BIGINT NOT NULL REFERENCES account ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  fileName TEXT NOT NULL," +
                       "  content BYTEA NOT NULL DEFAULT ''," +
                       "  UNIQUE(userId, fileName)" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS tempFile (" +
                       "  id BIGINT PRIMARY KEY," +
                       "  content BYTEA" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS corruptionReport (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  userId BIGINT NOT NULL," +
                       "  projectId BIGINT NOT NULL," +
                       "  fileName TEXT NOT NULL," +
                       "  message TEXT NOT NULL," +
                       "  timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS ipAddress (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  key TEXT UNIQUE NOT NULL," +
                       "  address TEXT NOT NULL" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS whitelist (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  email TEXT UNIQUE NOT NULL" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS feedback (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  notes TEXT," +
                       "  foundIn TEXT," +
                       "  faultData TEXT," +
                       "  comments TEXT," +
                       "  datestamp TEXT," +
                       "  email TEXT," +
                       "  projectId TEXT" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS nonce (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  nonce TEXT UNIQUE NOT NULL," +
                       "  userId BIGINT NOT NULL REFERENCES account ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  projectId BIGINT NOT NULL REFERENCES project ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS pwData (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  uuid UUID UNIQUE NOT NULL," +
                       "  email TEXT NOT NULL," +
                       "  timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS backpack (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  content TEXT NOT NULL" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS buildStatus (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  host TEXT NOT NULL," +
                       "  userId BIGINT NOT NULL," +
                       "  projectId BIGINT NOT NULL," +
                       "  progress INT," +
                       "  UNIQUE(host, userId, projectId)" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS misc (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  key TEXT UNIQUE NOT NULL," +
                       "  value TEXT" +
                       ")");
          stmt.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS account_lower_email_index ON account (lower(email))");
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS account_email_index ON account (email)");
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS account_lower_email_index ON account (lower(email))");
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS projectFile_index ON projectFile (projectId, userId, fileName)");
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS userFile_index ON userFile (userId, fileName)");
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS ipAddress_key_index ON ipAddress (key)");
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS whitelist_email_index ON whitelist (email)");
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS whitelist_lower_email_index ON whitelist (lower(email))");
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS nonce_nonce_index ON nonce (nonce)");
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS buildStatus_index ON buildStatus (host, userId, projectId)");
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS pwData_uuid_index ON pwData (uuid)");
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS misc_key_index ON misc (key)");
        }
        conn.commit();

      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, String.format("Failed to initialize database with url=\"%s\" user=\"%s\"", jdbcUrl.get(), jdbcUser.get()), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public User getUser(@Nonnull String strUserId) {
    long userId = this.encodeUserId(strUserId);
    User user = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM account WHERE id = ?")) {
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
              conn.rollback();
              throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
            }
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public User getUser(@Nonnull String strUserId, @Nonnull String email) {
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
  public User getUserFromEmail(@Nonnull String email) {
    User user = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM account WHERE lower(email) = lower(?)")) {
          qstmt.setString(1, email);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            long userId = rs.getLong("id");
            String strUserId = this.decodeUserId(userId);
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
          user = this.createUser(email, User.USER, false, null, conn);
        }
        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "email=" + email, e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    return user;
  }

  /**
   * Sets the stored email address for user with id userId
   *
   */
  @Override
  public void setUserEmail(@Nonnull String strUserId, @Nonnull String email) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET email = ? WHERE id = ?")) {
          stmt.setString(1, email);
          stmt.setLong(2, userId);
          ret = stmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        // One case is violation on email uniqueness. We fail it anyway.
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
    }
  }

  /**
   * Sets that the user has accepted the terms of service.
   *
   * @param userId user id
   */
  @Override
  public void setTosAccepted(@Nonnull String strUserId) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET tosAccepted = ? WHERE id = ?")) {
          stmt.setBoolean(1, true);
          stmt.setLong(2, userId);
          ret = stmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
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
  public void setUserSessionId(@Nonnull String strUserId, @Nonnull String sessionId) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET sessionId = ? WHERE id = ?")) {
          stmt.setString(1, sessionId);
          stmt.setLong(2, userId);
          ret = stmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
    }
  }

  /**
   * Sets the user's hashed password.
   *
   * @param userId user id
   * @param hashed password
   */
  @Override
  public void setUserPassword(@Nonnull String strUserId, @Nullable String password) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    // Empty password is not allowed in database, we simply remove it.
    if (password.equals("")) {
      password = null;
    }

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET password = ? WHERE id = ?")) {
          stmt.setString(1, password);
          stmt.setLong(2, userId);
          ret = stmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
    }
  }

  /**
   * Returns a string with the user's settings.
   *
   * @param userId user id
   * @return settings
   */
  @Override
  public String loadSettings(@Nonnull String strUserId) {
    long userId = this.encodeUserId(strUserId);
    String settings = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("SELECT settings FROM account WHERE id = ?")) {
          stmt.setLong(1, userId);
          ResultSet rs = stmt.executeQuery();

          if (rs.next()) {
            settings = rs.getString("settings");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (settings == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
    }
    return settings;
  }

  /**
   * Sets the stored name for user with id userId
   *
   */
  @Override
  public void setUserName(@Nonnull String strUserId, @Nonnull String name) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET name = ? WHERE id = ?")) {
          stmt.setString(1, name);
          stmt.setLong(2, userId);
          ret = stmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
    }
  }

  /**
   * Returns a string with the user's name.
   *
   * @param userId user id
   * @return name
   */
  @Override
  public String getUserName(@Nonnull String strUserId) {
    long userId = this.encodeUserId(strUserId);
    String name = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("SELECT name WHERE userId = ?")) {
          stmt.setLong(1, userId);
          ResultSet rs = stmt.executeQuery();

          if (rs.next()) {
            name = rs.getString("name");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (name == null) {        // name is not nullable
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
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
  public String getUserLink(@Nonnull String strUserId) {
    long userId = this.encodeUserId(strUserId);
    String link = null;
    boolean found = false;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("SELECT link WHERE userId = ?")) {
          stmt.setLong(1, userId);
          ResultSet rs = stmt.executeQuery();

          if (rs.next()) {
            link = rs.getString("link");
            found = true;
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (!found) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
    }
    return link;
  }

  /**
   * Sets the stored link for user with id userId
   *
   */
  @Override
  public void setUserLink(@Nonnull String strUserId, @Nonnull String link) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET link = ? WHERE id = ?")) {
          stmt.setString(1, link);
          stmt.setLong(2, userId);
          ret = stmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
    }
  }

  /**
   * Returns the email notification frequency
   *
   * @param userId user id
   * @return emailFrequency email frequency
   */
  @Override
  public int getUserEmailFrequency(@Nonnull String strUserId) {
    long userId = this.encodeUserId(strUserId);
    Integer freq = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("SELECT emailFrequency WHERE userId = ?")) {
          stmt.setLong(1, userId);
          ResultSet rs = stmt.executeQuery();

          if (rs.next()) {
            freq = rs.getInt("emailFrequency");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (freq == null) {        // emailFrequency is not nullable
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
    }
    return freq;
  }

  /**
   * Sets the stored email notification frequency for user with id userId
   *
   */
  @Override
  public void setUserEmailFrequency(@Nonnull String strUserId, int emailFrequency) {
    if (emailFrequency < 0) {
      throw new IllegalArgumentException("emailFrequency should be non-negative, but get " + emailFrequency);
    }

    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET emailFrequency = ? WHERE id = ?")) {
          stmt.setInt(1, emailFrequency);
          stmt.setLong(2, userId);
          ret = stmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
    }
  }

  /**
   * Stores a string with the user's settings.
   *
   * @param userId user ID
   * @param settings user's settings
   */
  @Override
  public void storeSettings(@Nonnull String strUserId, @Nonnull String settings) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET settings = ?, visited = CURRENT_TIMESTAMP WHERE id = ?")) {
          stmt.setString(1, settings);
          stmt.setLong(2, userId);
          ret = stmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
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
  public long createProject(@Nonnull String strUserId, @Nonnull Project project, @Nonnull String projectSettings) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    String name = project.getProjectName();
    String type = project.getProjectType();
    String history = project.getProjectHistory();
    long galleryId = UserProject.NOTPUBLISHED;
    long attributionId = UserProject.FROMSCRATCH;
    Long projectId = null;             // Defined after insertion

    try (Connection conn = this.cpds.getConnection()) {
      // Insert project data
      try {
        conn.setAutoCommit(false);
        try (PreparedStatement ustmt = conn.prepareStatement(
               "INSERT INTO project (userId, name, type, history, galleryId, attributionId, settings) VALUES (?, ?, ?, ?, ?, ?, ?)",
               Statement.RETURN_GENERATED_KEYS)) {
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
              if (keys.next()) {
                projectId = keys.getLong(1);
              }
            }
          }
        }

        if (projectId == null) {
          conn.rollback();
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), new RuntimeException("Unknown database error"));
        }

        // Save files
        try {
          for (TextFile file : project.getSourceFiles()) {
            FileData.RoleEnum role = FileData.RoleEnum.SOURCE;
            String fileName = file.getFileName();
            byte[] content = file.getContent().getBytes(StorageUtil.DEFAULT_CHARSET);
            this.createProjectFile(projectId, userId, role, fileName, content, conn);
          }
        } catch (UnsupportedEncodingException e) {
          conn.rollback();
          throw CrashReport.createAndLogError(LOG, null, "Cannot decode file content", e);
        }

        for (RawFile file : project.getRawSourceFiles()) {
          FileData.RoleEnum role = FileData.RoleEnum.SOURCE;
          String fileName = file.getFileName();
          byte[] content = file.getContent();
          this.createProjectFile(projectId, userId, role, fileName, content, conn);
        }

        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public void deleteProject(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        // Corresponding entries in projectFile table will be automatically remove by ON DELETE CASCADE
        try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM project WHERE id = ? AND userId = ?")) {
          ustmt.setLong(1, projectId);
          ustmt.setLong(2, userId);
          ustmt.executeUpdate();     // Don't care about result
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }
  }

  /**
   * Returns an array with the user's projects.
   *
   * @param userId  user ID
   * @return  list of projects
   */
  @Override
  public List<Long> getProjects(@Nonnull String strUserId) {
    long userId = this.encodeUserId(strUserId);
    List<Long> ret = new ArrayList<>();

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT id FROM project WHERE userId = ?")) {
          qstmt.setLong(1, userId);
          ResultSet rs = qstmt.executeQuery();

          while (rs.next()) {
            ret.add(rs.getLong("id"));
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public void setProjectGalleryId(@Nonnull final String strUserId, final long projectId,final long galleryId) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement("UPDATE project SET galleryId = ?, modifiedDate = CURRENT_TIMESTAMP WHERE projectId = ? AND userId = ?")) {
          ustmt.setLong(1, galleryId);
          ustmt.setLong(2, projectId);
          ustmt.setLong(3, userId);
          ret = ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
    }
  }

  /**
   * sets a projects attribution id when it is opened from a gallery project
   * @param userId a user Id (the request is made on behalf of this user)*
   * @param projectId project ID
   * @param attributionId attribution ID
   */
  @Override
  public void setProjectAttributionId(@Nonnull final String strUserId, final long projectId,final long attributionId) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement("UPDATE project SET attributionId = ?, modifiedDate = CURRENT_TIMESTAMP WHERE projectId = ? AND userId = ?")) {
          ustmt.setLong(1, attributionId);
          ustmt.setLong(2, projectId);
          ustmt.setLong(3, userId);
          ret = ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
    }
  }

  /**
   * Returns a string with the project settings.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId project ID
   * @return settings
   */
  @Override
  public String loadProjectSettings(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);
    String settings = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT settings FROM project WHERE id = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            settings = rs.getString("settings");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (settings == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
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
  public void storeProjectSettings(@Nonnull String strUserId, long projectId, @Nonnull String settings) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement("UPDATE project SET settings = ?, modifiedDate = CURRENT_TIMESTAMP WHERE id = ? AND userId = ?")) {
          ustmt.setString(1, settings);
          ustmt.setLong(2, projectId);
          ustmt.setLong(3, userId);
          ret = ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
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
  public String getProjectType(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);
    String type = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT type FROM project WHERE id = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            type = rs.getString("type");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (type == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
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
  public UserProject getUserProject(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);
    UserProject proj = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM project WHERE id = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            String name = rs.getString("name");
            String type = rs.getString("type");
            Timestamp creationDate = rs.getTimestamp("creationDate");
            Timestamp modifiedDate = rs.getTimestamp("modifiedDate");
            long attributionId = rs.getLong("attributionId");
            long galleryId = rs.getLong("galleryId");

            proj = new UserProject(
              projectId,
              name,
              type,
              creationDate.getTime(),
              modifiedDate.getTime(),
              galleryId,
              attributionId
              );
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (proj == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
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
  public List<UserProject> getUserProjects(@Nonnull String strUserId, @Nonnull List<Long> projectIds) {
    long userId = this.encodeUserId(strUserId);
    List<UserProject> ret = new ArrayList<>();

    // degradation case
    if (projectIds.size() == 0) {
      return ret;
    }

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        // Construct sql statement (Quick and dirty way for Java <= 7)
        StringBuilder sql = new StringBuilder("SELECT * FROM project WHERE userId = ? AND id IN (?");
        for (int cnt = 1; cnt < projectIds.size(); cnt += 1) {
          sql.append(", ?");
        }
        sql.append(")");

        try (PreparedStatement qstmt = conn.prepareStatement(sql.toString())) {
          qstmt.setLong(1, userId);
          for (int idx = 0; idx < projectIds.size(); idx += 1) {
            qstmt.setLong(idx + 2, projectIds.get(idx));
          }
          ResultSet rs = qstmt.executeQuery();

          while (rs.next()) {
            UserProject proj = null;
            long projectId = rs.getLong("id");
            String name = rs.getString("name");
            String type = rs.getString("type");
            Timestamp creationDate = rs.getTimestamp("creationDate");
            Timestamp modifiedDate = rs.getTimestamp("modifiedDate");
            long attributionId = rs.getLong("attributionId");
            long galleryId = rs.getLong("galleryId");

            proj = new UserProject(
              projectId,
              name,
              type,
              creationDate.getTime(),
              modifiedDate.getTime(),
              galleryId,
              attributionId
              );
            ret.add(proj);
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public String getProjectName(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);
    String name = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT name FROM project WHERE id = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            name = rs.getString("name");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (name == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
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
  public long getProjectDateModified(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);
    Timestamp modifiedDate = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT modifiedDate FROM project WHERE id = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            modifiedDate = rs.getTimestamp("modifiedDate");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (modifiedDate == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
    }
    return modifiedDate.getTime();
  }

  /**
   * Returns the specially formatted list of project history.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   *
   * @return String specially formatted history
   */
  @Override
  public String getProjectHistory(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);
    String history = null;
    boolean found = false;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT history FROM project WHERE id = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            history = rs.getString("history");
            found = true;
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (!found) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
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
  public long getProjectDateCreated(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);
    Timestamp creationDate = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT creationDate FROM project WHERE id = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            creationDate = rs.getTimestamp("creationDate");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (creationDate == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
    }
    return creationDate.getTime();
  }

  // Non-project-specific file management

  /**
   * Adds file IDs to the user's list of non-project-specific files.
   *
   * @param userId a user Id (the request is made on behalf of this user)
   * @param fileNames list of file IDs to add to the projects source file list
   */
  @Override
  public void addFilesToUser(@Nonnull String strUserId, String... fileNames) {
    long userId = this.encodeUserId(strUserId);

    // Degradation case
    if (fileNames.length == 0) {
      return;
    }

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO userFile (userId, fileName) VALUES (?, ?) ON CONFLICT (userId, fileName) DO NOTHING")) {
          for (String fileName : fileNames) {
            ustmt.setLong(1, userId);
            ustmt.setString(2, fileName);
            ustmt.addBatch();
          }

          ustmt.executeBatch();     // We don't care about the result
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }
  }

  /**
   * Returns a list of non-project-specific files for a user.
   *
   * @param userId a user Id
   * @return list of source file ID
   */
  @Override
  public List<String> getUserFiles(@Nonnull String strUserId) {
    long userId = this.encodeUserId(strUserId);
    List<String> ret = new ArrayList<String>();

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT fileName FROM userFile WHERE userId = ?")) {
          qstmt.setLong(1, userId);
          ResultSet rs = qstmt.executeQuery();

          while (rs.next()) {
            ret.add(rs.getString("fileName"));
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public void uploadUserFile(@Nonnull String strUserId, @Nonnull String fileName, @Nonnull String content, @Nonnull String encoding) {
    byte[] contentBytes;
    try {
      contentBytes = content.getBytes(content);
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot decode file content", e);
    }
    this.uploadRawUserFile(strUserId, fileName, contentBytes);
  }

  /**
   * Uploads a non-project-specific file.
   *
   * @param userId user ID
   * @param content file content
   * @param fileName file name
   */
  @Override
  public void uploadRawUserFile(@Nonnull String strUserId, @Nonnull String fileName, @Nonnull byte[] content) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement(
               "INSERT INTO userFile (userId, fileName, content) VALUES (?, ?, ?) ON CONFLICT (userId, fileName) DO UPDATE SET content = ?"
               )) {
          ustmt.setLong(1, userId);
          ustmt.setString(2, fileName);
          ustmt.setBytes(3, content);
          ustmt.setBytes(4, content);
          ret = ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), new RuntimeException("Unknown database error"));
    }
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
  public String downloadUserFile(@Nonnull String strUserId, @Nonnull String fileName, @Nonnull String encoding) {
    long userId = this.encodeUserId(strUserId);
    byte[] contentBytes = this.downloadRawUserFile(strUserId, fileName);
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
  public byte[] downloadRawUserFile(@Nonnull String strUserId, @Nonnull String fileName) {
    long userId = this.encodeUserId(strUserId);
    byte[] ret = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT content FROM userFile WHERE userId = ? AND fileName = ?")) {
          qstmt.setLong(1, userId);
          qstmt.setString(2, fileName);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            ret = rs.getBytes("content");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), new RuntimeException("Unknown database error"));
    }
    return ret;
  }

  /**
   * Deletes a user file.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param fileName  file ID
   */
  @Override
  public void deleteUserFile(@Nonnull String strUserId, @Nonnull String fileName) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM userFile WHERE userId = ? AND fileName = ?")) {
          ustmt.setLong(1, userId);
          ustmt.setString(2, fileName);
          ret = ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, null, fileName), new RuntimeException("Unknown database error"));
    }
  }

  // File management

  /**
   * Returns the maximum allowed job size in bytes.
   *
   * @return int maximum job size in bytes
   */
  public int getMaxJobSizeBytes() {
    // TODO It maybe dynamically determined by configs. We set to 5 MiB now.
    return 5 * 1024 * 1024;
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
  public void addSourceFilesToProject(@Nonnull String strUserId, long projectId, boolean changeModDate, String...fileNames) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    // Degradation case
    if (fileNames.length == 0) {
      return;
    }

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);
        if (changeModDate) {
          this.updateProjectModifiedDate(projectId, userId, conn);
        }

        // Create files
        this.bulkCreateProjectFile(projectId, userId, FileData.RoleEnum.SOURCE, fileNames, conn);
        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }
  }

  /**
   * add file IDs to the project's list of output files.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileNames  list of file IDs to add to the projects output file list
   */
  @Override
  public void addOutputFilesToProject(@Nonnull String strUserId, long projectId, String...fileNames) {
    long userId = this.encodeUserId(strUserId);

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);
        this.bulkCreateProjectFile(projectId, userId, FileData.RoleEnum.TARGET, fileNames, conn);
        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public void removeSourceFilesFromProject(@Nonnull String strUserId, long projectId, boolean changeModDate, String...fileNames) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      // Update project modified date
      try {
        conn.setAutoCommit(false);
        if (changeModDate) {
          this.updateProjectModifiedDate(projectId, userId, conn);
        }
        this.bulkDeleteProjectFile(projectId, userId, FileData.RoleEnum.SOURCE, fileNames, conn);
        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }
  }

  /**
   * Removes file IDs from the project's list of output files.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileNames  list of file IDs to add to the projects source file list
   */
  @Override
  public void removeOutputFilesFromProject(@Nonnull String strUserId, long projectId, String...fileNames) {
    long userId = this.encodeUserId(strUserId);

    // Degradation case
    if (fileNames.length == 0) {
      return;
    }

    try (Connection conn = this.cpds.getConnection()) {

      try {
        conn.setAutoCommit(false);
        this.bulkDeleteProjectFile(projectId, userId, FileData.RoleEnum.TARGET, fileNames, conn);
        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public List<String> getProjectSourceFiles(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);
    List<String> ret = new ArrayList<String>();

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT fileName FROM projectFile WHERE projectId = ? AND userId = ? AND role = ?")) {
          String roleString = FileData.RoleEnum.SOURCE.name();
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          qstmt.setString(3, roleString);
          ResultSet rs = qstmt.executeQuery();

          while (rs.next()) {
            ret.add(rs.getString("fileName"));
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public List<String> getProjectOutputFiles(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);
    List<String> ret = new ArrayList<String>();

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT fileName FROM projectFile WHERE projectId = ? AND userId = ? AND role = ?")) {
          String roleString = FileData.RoleEnum.TARGET.name();
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          qstmt.setString(3, roleString);
          ResultSet rs = qstmt.executeQuery();

          while (rs.next()) {
            ret.add(rs.getString("fileName"));
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public long getProjectGalleryId(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);
    Long galleryId = null;
    boolean found = false;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT galleryId FROM project WHERE id = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            galleryId = rs.getLong("galleryId");
            found = true;
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (!found) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
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

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT attributionId FROM project WHERE projectId = ?")) {
          qstmt.setLong(1, projectId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            attributionId = rs.getLong("attributionId");
            found = true;
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (!found) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, projectId, null), new RuntimeException("Unknown database error"));
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
  public long uploadFile(long projectId, @Nonnull String fileName, @Nonnull String strUserId, @Nonnull String content, @Nonnull String encoding)
    throws BlocksTruncatedException {

    long userId = this.encodeUserId(strUserId);
    long ts;

    byte[] contentBytes;
    try {
      contentBytes = content.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding, " + makeErrorMsg(null, projectId, fileName), e);
    }

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);
        ts = this.updateProjectFileContent(projectId, userId, fileName, contentBytes, false, conn);
        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
      }

      return ts;
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }
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
  public long uploadFileForce(long projectId, @Nonnull String fileName, @Nonnull String strUserId, @Nonnull String content, @Nonnull String encoding)  {
    long userId = this.encodeUserId(strUserId);
    long ts;

    byte[] contentBytes;
    try {
      contentBytes = content.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding," + makeErrorMsg(null, projectId, fileName), e);
    }

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);
        ts = this.updateProjectFileContent(projectId, userId, fileName, contentBytes, true, conn);
        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
      }

      return ts;
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }
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
  public long uploadRawFile(long projectId, @Nonnull String fileName, @Nonnull String strUserId, boolean force, @Nonnull byte[] content) {

    long userId = this.encodeUserId(strUserId);
    long ts;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);
        ts = this.updateProjectFileContent(projectId, userId, fileName, content, false, conn);
        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public long uploadRawFileForce(long projectId, @Nonnull String fileName, @Nonnull String strUserId, @Nonnull byte[] content) {
    long userId = this.encodeUserId(strUserId);
    long ts;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);
        ts = this.updateProjectFileContent(projectId, userId, fileName, content, true, conn);
        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public long deleteFile(@Nonnull String strUserId, long projectId, @Nonnull String fileName) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;
    long ts;

    try (Connection conn = this.cpds.getConnection()) {

      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM projectFile WHERE projectId = ? AND userId = ? AND fileName = ?")) {
          ustmt.setLong(1, projectId);
          ustmt.setLong(2, userId);
          ustmt.setString(3, fileName);
          ret = ustmt.executeUpdate();
        }

        if (ret == 0) {
          conn.rollback();
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), new RuntimeException("Unknown database error"));
        }

        ts = this.updateProjectModifiedDate(projectId, userId, conn);
        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public String downloadFile(@Nonnull String strUserId, long projectId, @Nonnull String fileName, @Nonnull String encoding) {
    long userId = this.encodeUserId(strUserId);
    byte[] contentBytes = this.downloadProjectFile(projectId, userId, fileName);
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
  public void recordCorruption(@Nonnull String strUserId, long projectId, @Nonnull String fileName, @Nonnull String message) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO corruptionReport (userId, projectId, fileName, message) VALUES (?, ?, ?, ?)")) {
          ustmt.setLong(1, userId);
          ustmt.setLong(2, projectId);
          ustmt.setString(3, fileName);
          ustmt.setString(4, message);
          ret = ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), new RuntimeException("Unknown database error"));
    }
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
  public byte[] downloadRawFile(@Nonnull String strUserId, long projectId, @Nonnull String fileName) {
    long userId = this.encodeUserId(strUserId);
    byte[] contentBytes = this.downloadProjectFile(projectId, userId, fileName);
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
  public String uploadTempFile(@Nonnull byte [] content) throws IOException {
    Long fileId = null;
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement(
               "INSERT INTO tempFile (content) VALUES (?)",
               Statement.RETURN_GENERATED_KEYS)) {
          ustmt.setBytes(1, content);
          ret = ustmt.executeUpdate();

          if (ret > 0) {
            try (ResultSet keys = ustmt.getGeneratedKeys()) {
              if (keys.next()) {
                fileId = keys.getLong(1);
              }
            }
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Failed to create temp file", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (fileId == null) {
      throw CrashReport.createAndLogError(LOG, null, "Database error in uploadTempFile()", new RuntimeException("Unknown database error"));
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
  public InputStream openTempFile(@Nonnull String fileName) throws IOException {

    if (!fileName.substring(0, 8).equals("__TEMP__")) {
      throw new IllegalArgumentException("fileName argument should start with \"__TEMP__\", but get \"" + fileName + "\"");
    }

    // Get file id
    long fileId = new BigInteger(fileName.substring(8), 16).longValue();
    InputStream stream = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT content FROM tempFile WHERE id = ?")) {
          qstmt.setLong(1, fileId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            stream = rs.getBinaryStream("content");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, null, fileName), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (stream == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, null, fileName), new RuntimeException("Unknown database error"));
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
  public void deleteTempFile(@Nonnull String fileName) throws IOException {

    // Get file id
    if (!fileName.substring(0, 8).equals("__TEMP__")) {
      throw new IllegalArgumentException("fileName argument should start with \"__TEMP__\", but get \"" + fileName + "\"");
    }
    long fileId = new BigInteger(fileName.substring(8), 16).longValue();
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM tempFile WHERE id = ?")) {
          ustmt.setLong(1, fileId);
          ret = ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Failed to delete temp file " + fileName, e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, null, fileName), new RuntimeException("Unknown database error"));
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

    try (Connection conn = this.cpds.getConnection()) {

      try {
        conn.setAutoCommit(false);

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
            conn.rollback();
            throw CrashReport.createAndLogError(LOG, null, "Database error in getCurrentMotd()", new RuntimeException("Unknown database error"));
          }
        }

        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in getCurrentMotd()", e);
      }

      if (motd == null) {
        throw CrashReport.createAndLogError(LOG, null, "Database error in getCurrentMotd()", new RuntimeException("Unknown database error"));
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
    @Nonnull String strUserId,
    long projectId,
    boolean includeProjectHistory,
    boolean includeAndroidKeystore,
    @Nullable String zipName,
    final boolean includeYail,
    final boolean includeScreenShots,
    final boolean forGallery,
    final boolean fatalError) throws IOException {

    class FileRow {
      String fileName;
      FileData.RoleEnum role;
      byte[] content;

      FileRow (String _fileName, FileData.RoleEnum _role, byte[] _content) {
        fileName = _fileName;
        role = _role;
        content = _content;
      }
    }

    long userId = this.encodeUserId(strUserId);
    List<FileRow> files = new ArrayList<FileRow>();
    boolean projectFound = false;
    String projectName = null;
    String projectHistory = null;
    byte[] keystoreContent = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        // Get project
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM project WHERE id = ? AND userId = ?")) {
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
          conn.rollback();
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Canont find project"));
        }

        // Find project files
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM projectFile WHERE projectId = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);

          ResultSet rs = qstmt.executeQuery();
          while (rs.next()) {

            // Check role field integrity
            FileData.RoleEnum role;
            try {
              role = FileData.RoleEnum.valueOf(rs.getString("role"));
            } catch (IllegalArgumentException e) {
              throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
            }

            FileRow fr = new FileRow(rs.getString("fileName"), role, rs.getBytes("content"));

            // Kick out some files
            if (fr.fileName.startsWith("assets/external_comps") && forGallery) {
              conn.rollback();
              throw new IOException("FATAL Error, external component in gallery app");
            }
            if (!fr.role.equals(FileData.RoleEnum.SOURCE)) {
              continue;
            }
            if (fr.fileName.equals(FileExporter.REMIX_INFORMATION_FILE_PATH)) {
              // Skip legacy remix history files that were previous stored with the project
              continue;
            }
            if (!includeScreenShots && fr.fileName.startsWith("screenshots")) {
              continue;
            }

            // We intentionally remove *.yail files for AI1 compatibility
            // TODO remove it in the future
            if (!includeYail && fr.fileName.endsWith(".yail")) {
              continue;
            }

            files.add(fr);
          }
        }

        // Find Android keystore file
        if (includeAndroidKeystore) {
          try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM userFile WHERE userId = ? AND fileName = ? AND content IS NOT NULL AND length(content) > 0")) {
            qstmt.setLong(1, userId);
            qstmt.setString(2, StorageUtil.ANDROID_KEYSTORE_FILENAME);
            ResultSet rs = qstmt.executeQuery();

            if (rs.next()) {
              keystoreContent = rs.getBytes("content");
            }
          }
        }

        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    // Check if project found

    // Kick out non-file case
    if (files.size() == 0) {
      throw new IllegalStateException("No files to download");
    }

    // Create zip file
    // TODO Should we store in FS instead in-memory buffer?
    int fileCount = 0;

    ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
    final ZipOutputStream zipStream = new ZipOutputStream(zipFile);
    zipStream.setComment("Built with MIT App Inventor");

    for (FileRow fr : files) { // project files
      byte[] content = fr.content != null ? fr.content : new byte[0];
      zipStream.putNextEntry(new ZipEntry(fr.fileName));
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

    assert projectFound && projectName != null;
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
  public String findUserByEmail(@Nonnull String email) throws NoSuchElementException {
    Long userId = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        // Case-sensitive search
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM account WHERE email = ?")) {
          qstmt.setString(1, email);
          ResultSet rs = qstmt.executeQuery();
          if (rs.next()) {
            userId = rs.getLong("id");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in findUserByEmail() with email=" + email, e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (userId == null) {
      throw new NoSuchElementException("Couldn't find a user with email: " + email);
    }
    String strUserId = this.decodeUserId(userId);
    return strUserId;
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
  public String findIpAddressByKey(@Nonnull String key) {
    String addr = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT address FROM ipAddress WHERE key = ?")) {
          qstmt.setString(1, key);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            addr = rs.getString("address");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in findIpAddressByKey()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public void storeIpAddressByKey(@Nonnull String key, @Nonnull String ipAddress) {
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement(
               "INSERT INTO ipAddress (key, address) VALUES (?, ?) ON CONFLICT (key) DO UPDATE SET address = ?"
               )) {
          ustmt.setString(1, key);
          ustmt.setString(2, ipAddress);
          ustmt.setString(3, ipAddress);
          ret = ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in storeIpAddressByKey()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, String.format("key=%s, ipAddress=%s", key, ipAddress), new RuntimeException("Unknown database error"));
    }
  }

  @Override
  public boolean checkWhiteList(@Nonnull String email) {
    boolean pass = false;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        // Try to get existing user
        User user = null;
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT COUNT(1) FROM whitelist WHERE lower(email) = lower(?)")) {
          qstmt.setString(1, email);
          ResultSet rs = qstmt.executeQuery();
          pass = rs.next();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in checkWhiteList()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    return pass;
  }

  @Override
  public void storeFeedback(
    @Nonnull final String notes,
    @Nonnull final String foundIn,
    @Nonnull final String faultData,
    @Nonnull final String comments,
    @Nonnull final String datestamp,
    @Nonnull final String email,
    @Nonnull final String projectId) {

    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

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
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in storeFeedback()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, "Error in storeFeedback()", new RuntimeException("Unknown database error"));
    }
  }

  @Override
  public Nonce getNoncebyValue(@Nonnull String nonceValue) {
    Nonce nonce = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM nonce WHERE nonce = ?")) {
          qstmt.setString(1, nonceValue);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            long userId = rs.getLong("userId");
            long projectId = rs.getLong("projectId");
            Timestamp ts = rs.getTimestamp("timestamp");

            Date dt = new Date(ts.getTime());
            String strUserId = this.decodeUserId(userId);
            nonce = new Nonce(nonceValue, strUserId, projectId, dt);
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in getNonceByValue()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    return nonce;
  }

  @Override
  public void storeNonce(@Nonnull final String nonceValue, @Nonnull final String strUserId, final long projectId) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement(
               "INSERT INTO nonce (projectId, userId, nonce) VALUES (?, ?, ?) ON CONFLICT (nonce) DO UPDATE SET projectId = ?, userId = ?"
               )) {
          ustmt.setLong(1, projectId);
          ustmt.setLong(2, userId);
          ustmt.setString(3, nonceValue);
          ustmt.setLong(4, projectId);
          ustmt.setLong(5, userId);

          ret = ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in storeNonce()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
    }
  }

  // Cleanup expired nonces
  @Override
  public void cleanupNonces() {
    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement(
               "DELETE FROM nonce WHERE timestamp < (CURRENT_TIMESTAMP - INTERVAL '3 hour')"
               )) {
          ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in cleanupNonces()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }
  }

  // Check to see if user needs projects upgraded (moved to GCS)
  // if so, add task to task queue
  @Override
  public void checkUpgrade(@Nonnull String strUserId) {
    // Do nothing here
  }

  // called by the task queue to actually upgrade user's projects
  @Override
  public void doUpgrade(@Nonnull String strUserId) {
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

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

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
          try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO misc (key, value) VALUES (?, ?) ON CONFLICT (key) DO UPDATE SET value = ?")) {
            String configString = config.toString();
            ustmt.setString(1, SPLASH_CONFIG_KEY);
            ustmt.setString(2, configString);
            ustmt.setString(3, configString);
            ret = ustmt.executeUpdate();
          }

          if (ret == 0) {
            conn.rollback();
            throw CrashReport.createAndLogError(LOG, null, "Database error in getSplashConfig()", new RuntimeException("Unknown database error"));
          }

          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in getSplashConfig()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    return result;
  }

  @Override
  public PWData createPWData(@Nonnull String email) {
    int ret = 0;
    Long id = null;
    String uuid = null;
    Timestamp ts = null;

    try (Connection conn = this.cpds.getConnection()) {

      // It has (very) low probability to conflicts on colliding uuid.
      // If you have bad luck. We fail it anyway.
      try {
        conn.setAutoCommit(false);
        try (PreparedStatement ustmt = conn.prepareStatement(
               "INSERT INTO pwData (uuid, email) VALUES (uuid_generate_v4(), ?) ON CONFLICT (uuid) DO UPDATE SET uuid = uuid_generate_v4()",
               Statement.RETURN_GENERATED_KEYS)) {
          ustmt.setString(1, email);
          ret = ustmt.executeUpdate();
          if (ret > 0) {
            try (ResultSet keys = ustmt.getGeneratedKeys()) {
              if (keys.next()) {
                id = keys.getLong(1);
              }
            }
          }
        }

        // Get timestamp
        if (id != null) {
          try (PreparedStatement qstmt = conn.prepareStatement("SELECT uuid::text AS uuid, timestamp FROM pwData WHERE id = ?")) {
            qstmt.setLong(1, id);
            ResultSet rs = qstmt.executeQuery();

            if (rs.next()) {
              uuid = rs.getString("uuid");
              ts = rs.getTimestamp("timestamp");
            }
          }
        }

        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in createPWData()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    // Finalize
    if (id == null) {
      throw CrashReport.createAndLogError(LOG, null, "Database error in createPWData()", new RuntimeException("Unknown database error"));
    }

    PWData result = new PWData();
    result.id = uuid;
    result.email = email;
    result.timestamp = new Date(ts.getTime());
    return result;
  }

  @Override
  public PWData findPWData(@Nonnull String uuid) {
    PWData ret = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM pwData WHERE uuid::text = lower(?)")) {
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
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in findPWData()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    return ret;
  }

  @Override
  public void cleanuppwdata() {
    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement(
               "DELETE FROM pwData WHERE timestamp < (CURRENT_TIMESTAMP - INTERVAL '1 day')"
               )) {
          ustmt.executeUpdate();    // Don't care about the result
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in cleanuppwdata()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }
  }

  // Routines for user admin interface

  @Override
  public List<AdminUser> searchUsers(@Nonnull String partialEmail) {
    List<AdminUser> result = new ArrayList<AdminUser>();

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM account WHERE email >= ? LIMIT 20")) {
          qstmt.setString(1, partialEmail);
          ResultSet rs = qstmt.executeQuery();

          while (rs.next()) {
            long userId = rs.getLong("id");
            String strUserId = this.decodeUserId(userId);
            Timestamp visitedTs = rs.getTimestamp("visited");
            Date visitedDt = visitedTs != null ? new Date(visitedTs.getTime()) : null;
            AdminUser user = new AdminUser(
              strUserId,
              rs.getString("name"),
              rs.getString("email"),
              rs.getBoolean("tosAccepted"),
              rs.getBoolean("isAdmin"),
              rs.getInt("type") == User.MODERATOR,
              visitedDt);
            result.add(user);
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in searchUsers()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    return result;
  }

  @Override
  public void storeUser(@Nonnull AdminUser user) throws AdminInterfaceException {
    // The implementation has distinct behavior from ObjectifyStorageIo.storeUser()
    // We require non-null user.getId() and existince of this user.

    String strUserId = user.getId();
    Long userId = strUserId != null ? this.encodeUserId(strUserId) : null;

    String newPassword = user.getPassword();
    if (newPassword.equals("")) {
      newPassword = null;
    }
    String newEmail = user.getEmail();
    boolean newIsAdmin = user.getIsAdmin();
    int newType = user.getIsModerator() ? User.MODERATOR : User.USER;

    try (Connection conn = this.cpds.getConnection()) {

      try {
        conn.setAutoCommit(false);

        if (userId != null) {
          // If it conflicts on email, we fail it anyway.
          int ret = 0;
          try (PreparedStatement ustmt = conn.prepareStatement("UPDATE account SET email = ?, isAdmin = ?, type = ?, password = COALESCE(?, password) WHERE id = ?")) {
            ustmt.setString(1, newEmail);
            ustmt.setBoolean(2, newIsAdmin);
            ustmt.setInt(3, newType);
            ustmt.setString(4, newPassword);
            ustmt.setLong(5, userId);
            ret = ustmt.executeUpdate();
          }

          if (ret == 0) {
            conn.rollback();
            throw new AdminInterfaceException("Cannot find user with userId = " + strUserId);
          }
        } else {
          this.createUser(newEmail, newType, newIsAdmin, newPassword, conn);
        }

        conn.commit();
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in storeUser()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public String downloadBackpack(@Nonnull String backPackId) {
    String ret = "[]";

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT content FROM backpack WHERE id = ?")) {
          qstmt.setString(1, backPackId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            ret = rs.getString("content");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in downloadBackpack()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
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
  public void uploadBackpack(@Nonnull String backPackId, @Nonnull String content) {
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO backpack (id, content) VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET content = ?")) {
          ustmt.setString(1, backPackId);
          ustmt.setString(2, content);
          ustmt.setString(3, content);
          ret = ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in uploadBackpack()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, String.format("backPackId=?", backPackId), new RuntimeException("Unknown database error"));
    }
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
  public void storeBuildStatus(@Nonnull String strUserId, long projectId, int progress) {
    long userId = this.encodeUserId(strUserId);
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement ustmt = conn.prepareStatement(
               "INSERT INTO buildStatus (host, userId, projectId, progress) VALUES (?, ?, ?, ?) ON CONFLICT (host, userId, projectId) DO UPDATE SET progress = ?"
               )) {
          ustmt.setString(1, HOST_ID);
          ustmt.setLong(2, userId);
          ustmt.setLong(3, projectId);
          ustmt.setInt(4, progress);
          ustmt.setInt(5, progress);
          ret = ustmt.executeUpdate();
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in storeBuildStatus()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (ret == 0) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
    }
  }

  @Override
  public int getBuildStatus(@Nonnull String strUserId, long projectId) {
    long userId = this.encodeUserId(strUserId);
    Integer ret = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT progress FROM buildStatus WHERE projectId = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            ret = rs.getInt("progress");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "Database error in getBuildStatus()", e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    return ret != null ? ret : 50;
  }

  private User createUser(@Nonnull String email, int type, boolean isAdmin, @Nonnull String password, Connection conn) {

    // Insert new user
    Long userId = null;
    String name = User.getDefaultName(email);
    int emailFrequency = User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY;
    boolean tosAccepted = false;
    String nonEmptyPassword = password == null || password.equals("") ? null : password;
    int ret = 0;

    try {
      try (PreparedStatement ustmt = conn.prepareStatement(
             "INSERT INTO account (name, type, emailFrequency, tosAccepted, isAdmin, email, password) VALUES (?, ?, ?, ?, ?, ?, ?)",
             Statement.RETURN_GENERATED_KEYS)) {
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
      }

      if (userId == null) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, "email=" + email, new RuntimeException("Unknown database error"));
      }
    } catch (SQLException e) {
      // One possible case is that two servers insert users of the same email.
      // We fail one of them.
      try {
        conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, "Database error in createUser()", e);
    }

    String strUserId = this.decodeUserId(userId);
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
    @Nonnull FileData.RoleEnum role,
    @Nonnull String fileName,
    @Nonnull byte[] content,
    Connection conn) {

    int ret = 0;
    String roleString = role.name();

    try (PreparedStatement ustmt = conn.prepareStatement(
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
      if (ret == 0) {
        conn.rollback();
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), new RuntimeException("Unknown database error"));
      }
    } catch (SQLException e) {
      try {
        conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
    }
  }

  private void bulkCreateProjectFile(
    long projectId,
    long userId,
    @Nonnull FileData.RoleEnum role,
    @Nonnull String[] fileNames,
    Connection conn) {
    // We expect this block work when fileNames is empty

    String roleString = role.name();

    try (PreparedStatement ustmt = conn.prepareStatement(
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
    } catch (SQLException e) {
      try {
        conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      String strUserId = this.decodeUserId(userId);
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }
  }

  private void bulkDeleteProjectFile(
    long projectId,
    long userId,
    @Nonnull FileData.RoleEnum role,
    @Nonnull String[] fileNames,
    Connection conn) {
    String roleString = role.name();
    // We expect this block work when fileNames is empty

    try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM projectFile WHERE projectId = ? AND userId = ? AND fileName = ?")) {
      for (String fname : fileNames) {
        ustmt.setLong(1, projectId);
        ustmt.setLong(2, userId);
        ustmt.setString(3, fname);
        ustmt.addBatch();
      }

      ustmt.executeBatch();     // We don't care about the result
    } catch (SQLException e) {
      try {
        conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }

      String strUserId = this.decodeUserId(userId);
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }
  }

  private long updateProjectFileContent(
    long projectId,
    long userId,
    @Nonnull String fileName,
    @Nonnull byte[] content,
    boolean force,
    Connection conn) {

    int ret = 0;
    long time;

    // Update project modified date
    try (PreparedStatement ustmt = conn.prepareStatement("UPDATE projectFile SET content = ? WHERE projectId = ? AND userId = ? AND fileName = ?")) {
      ustmt.setBytes(1, content);
      ustmt.setLong(2, projectId);
      ustmt.setLong(3, userId);
      ustmt.setString(4, fileName);
      ret = ustmt.executeUpdate();

      // Check if file is found
      if (ret == 0) {
        if (fileName.endsWith(".yail") || fileName.endsWith(".png")) {
          // <Screen>.yail files are missing when user converts AI1 project to AI2
          // instead of blowing up, just create a <Screen>.yail file
          // TODO remove it in the future
          this.createProjectFile(projectId, userId, FileData.RoleEnum.SOURCE, fileName, content, conn);

        } else {
          conn.rollback();
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), new RuntimeException("Unknown database error"));
        }
      }

      time = this.updateProjectModifiedDate(projectId, userId, conn);

    } catch (SQLException e) {
      try {
        conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
    }

    return time;
  }

  private byte[] downloadProjectFile(long projectId, long userId, @Nonnull String fileName) {
    byte[] contentBytes = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT content FROM projectFile WHERE projectId = ? AND userId = ? AND fileName = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          qstmt.setString(3, fileName);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            contentBytes = rs.getBytes("content");
          }
          conn.commit();
        }
      } catch (SQLException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackExc) {
          throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
        }
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), e);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot connect to database", e);
    }

    if (contentBytes == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, fileName), new FileNotFoundException(fileName));
    }

    return contentBytes;
  }

  private long updateProjectModifiedDate(long projectId, long userId, Connection conn) {
    int ret = 0;
    Timestamp modifiedTs = null;

    try {
      try (PreparedStatement ustmt = conn.prepareStatement("UPDATE project SET modifiedDate = CURRENT_TIMESTAMP WHERE id = ? AND userId = ?")) {
        ustmt.setLong(1, projectId);
        ustmt.setLong(2, userId);
        ret = ustmt.executeUpdate();
      }

      if (ret > 0) {              // If timestamp update suceeds
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT modifiedDate FROM project WHERE id = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            modifiedTs = rs.getTimestamp("modifiedDate");
          }
        }
      }

      if (modifiedTs == null) {             // No such project found
        conn.rollback();
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), new RuntimeException("Unknown database error"));
      }
    } catch (SQLException e) {
      try {
        conn.rollback();
      } catch (SQLException rollbackExc) {
        throw CrashReport.createAndLogError(LOG, null, "Rollback error", rollbackExc);
      }
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(userId, projectId, null), e);
    }

    return modifiedTs.getTime();
  }

  private long encodeUserId(@Nonnull String strUserId) {
    try {
      return new BigInteger(strUserId, 16).longValue();
    } catch (NumberFormatException e) {
      throw CrashReport.createAndLogError(LOG, null, "encoded userId = " + strUserId, e);
    }
  }

  private String decodeUserId(long userId) {
    return Long.toHexString(userId);
  }

  private String makeErrorMsg(
    @Nullable final Long userId,
    @Nullable final Long projectId,
    @Nullable final String fileName) {

    if (userId == null && projectId == null && fileName == null) {
      throw new IllegalArgumentException("It's not allowed to set all params to null");
    }

    String strUserId = userId != null ? this.decodeUserId(userId) : null;

    String userIdToken = strUserId != null ? ("userId=\"" + strUserId + "\"") : null;
    String projectIdToken = projectId != null ? ("projectId=\"" + projectId + "\"") : null;
    String fileNameIdToken = fileName != null ? ("fileName=\"" + fileName + "\"") : null;

    // No String.join() function in JDK 1.7. We do it manually.
    String ret = null;
    ret = userIdToken != null ? userIdToken : ret;
    ret = projectIdToken != null ? (ret != null ? (ret + ", " + projectIdToken) : projectIdToken) : ret;
    ret = fileName != null ? (ret != null ? (ret + ", " + fileName) : fileName) : ret;

    return ret;
  }
}
