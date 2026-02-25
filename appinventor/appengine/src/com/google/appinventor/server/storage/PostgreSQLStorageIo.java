// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2012, 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import org.json.JSONObject;
import org.json.JSONException;
import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
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

import com.google.appinventor.shared.util.AccountUtil;

import com.google.appinventor.server.FileExporter;
import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StoredData.FileData;
import com.google.appinventor.server.storage.StoredData.PWData;
import com.google.appinventor.server.util.LicenseConfig;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import javax.sql.DataSource;

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
  private static final Flag<String> jdbcReadOnlyUrl = Flag.createFlag("jdbc.readOnlyUrl", jdbcUrl.get());
  private static final Logger LOG = Logger.getLogger(PostgreSQLStorageIo.class.getName());
  private static final String HOST_ID = String.format(
    "%s-%s-%s-%s",
    GitBuildId.GIT_BUILD_VERSION,
    GitBuildId.GIT_BUILD_FINGERPRINT,
    GitBuildId.ANT_BUILD_DATE,
    UUID.randomUUID().toString()
    );
  private final String DATABASE_ERROR = "Database Error";

  private final ComboPooledDataSource cpds;

  private final String DEFAULT_ALLOWED_IOS_EXTENSIONS = "[\"edu.mit.appinventor.ble\",\"com.bbc.microbit.profile\",\"edu.mit.appinventor.ai.personalimageclassifier\",\"edu.mit.appinventor.ai.personalaudioclassifier\",\"edu.mit.appinventor.ai.posenet\",\"edu.mit.appinventor.ai.facemesh\",\"edu.mit.appinventor.ai.teachablemachine\",\"fun.microblocks.microblocks\"]";

  public PostgreSQLStorageIo() {
    // Setup connection
    try {
      this.cpds = new ComboPooledDataSource();
      this.cpds.setDriverClass("org.postgresql.Driver");
      this.cpds.setJdbcUrl(jdbcUrl.get());
      this.cpds.setUser(jdbcUser.get());
      this.cpds.setPassword(jdbcPassword.get());
      this.cpds.setMaxStatements(180);
      this.cpds.setIdleConnectionTestPeriod(60);
      this.cpds.setAutoCommitOnClose(false);
    } catch (PropertyVetoException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot setup database connection pool", e);
    }

    // Initialize database
    try (Connection conn = this.cpds.getConnection()) {
      try {
        conn.setAutoCommit(false);
        try (Statement stmt = conn.createStatement()) {
          stmt.execute("DO $$ BEGIN " +
                       "  CREATE TYPE project_kind AS ENUM ('YoungAndroid'); " +
                       "EXCEPTION " +
                       "  WHEN duplicate_object THEN null; " +
                       "END $$;");
          stmt.execute("DO $$ BEGIN " +
                       "  CREATE TYPE file_role AS ENUM ('SOURCE', 'TARGET', 'TEMPORARY'); " +
                       "EXCEPTION " +
                       "  WHEN duplicate_object THEN null; " +
                       "END $$;");
          stmt.execute("CREATE TABLE IF NOT EXISTS account (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  uuid UUID UNIQUE NOT NULL," +
                       "  email TEXT UNIQUE," +
                       "  tosAccepted BOOLEAN DEFAULT FALSE NOT NULL," +
                       "  isAdmin BOOLEAN DEFAULT FALSE NOT NULL," +
                       "  isReadOnly BOOLEAN DEFAULT FALSE NOT NULL," +
                       "  sessionId TEXT," +
                       "  password TEXT CHECK (password <> '')," +
                       "  backPackId TEXT," +
                       "  settings TEXT NOT NULL DEFAULT ''," +
                       "  visited TIMESTAMPTZ," +
                       "  account_created TIMESTAMPTZ" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS project (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  userId BIGINT NOT NULL REFERENCES account ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  name TEXT NOT NULL CHECK (name <> '')," +
                       "  type project_kind," +
                       "  settings TEXT NOT NULL DEFAULT ''," +
                       "  creationDate TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                       "  modifiedDate TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                       "  builtDate TIMESTAMPTZ," +
                       "  trashflag BOOLEAN DEFAULT FALSE NOT NULL," +
                       "  history TEXT" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS projectFile (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  projectId BIGINT NOT NULL REFERENCES project ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  userId BIGINT NOT NULL REFERENCES account ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  fileName TEXT NOT NULL," +
                       "  role file_role NOT NULL," +
                       "  hash TEXT," + // MD5 Hash of content iff it is an asset. content will be null
                       "  content BYTEA," +
                       "  ts TIMESTAMPTZ," +
                       "  UNIQUE (projectId, userId, fileName)" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS assetFile (" +
                       " id BIGSERIAL PRIMARY KEY," +
                       " hash TEXT UNIQUE NOT NULL," +
                       " modifiedDate TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                       " content BYTEA" +
                       ")");

          stmt.execute("CREATE TABLE IF NOT EXISTS userFile (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  userId BIGINT NOT NULL REFERENCES account ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  fileName TEXT NOT NULL," +
                       "  content BYTEA NOT NULL DEFAULT ''," +
                       "  UNIQUE(userId, fileName)" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS tempFile (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  fileName TEXT NOT NULL," +
                       "  modifiedDate TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP," +
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
                       "  strUserId UUID NOT NULL REFERENCES account(uuid) ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  projectId BIGINT NOT NULL REFERENCES project ON DELETE CASCADE ON UPDATE CASCADE," +
                       "  timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                       ")");
          stmt.execute("CREATE TABLE IF NOT EXISTS pwData (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  uuid TEXT UNIQUE NOT NULL," +
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
          stmt.execute("CREATE TABLE IF NOT EXISTS splashconfig (" +
                       "  id BIGSERIAL PRIMARY KEY," +
                       "  version INTEGER," +
                       "  content TEXT," +
                       "  width INTEGER," +
                       "  height INTEGER," +
                       "  active BOOLEAN" +
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
          stmt.executeUpdate("CREATE INDEX IF NOT EXISTS projectFile_target_index  ON projectfile (role, substring(filename, position('.' in filename))) WHERE role = 'TARGET'");

          stmt.executeUpdate(
            "DO $DO$" +
            " BEGIN" +
            "    CREATE FUNCTION update_settings(input_uuid UUID, new_settings text) RETURNS boolean AS $$" +
            "       DECLARE" +
            "            old_settings text;" +
            "       BEGIN" +
            "            SELECT settings INTO old_settings FROM account WHERE uuid = input_uuid;" +
            "            IF old_settings = '' THEN" +
            "               UPDATE account SET settings = new_settings, account_created = CURRENT_TIMESTAMP, visited = CURRENT_TIMESTAMP WHERE uuid = input_uuid;" +
            "            ELSE" +
            "               UPDATE account SET settings = new_settings, visited = CURRENT_TIMESTAMP  WHERE uuid = input_uuid;" +
            "            END IF;" +
            "            RETURN true;" +
            "       END;" +
            "       $$ LANGUAGE plpgsql;" +
            " EXCEPTION" +
            "    WHEN duplicate_function THEN" +
            "    null;" +
            " END; $DO$");
          stmt.executeUpdate(
            "DO $DO$" +
            "  BEGIN" +
            "    CREATE FUNCTION do_gc() RETURNS boolean AS $$" +
            "      BEGIN" +
            "        CREATE TEMP TABLE gc1 AS SELECT hash FROM assetfile;" +
            "        DELETE FROM gc1 WHERE hash IN (SELECT hash FROM projectfile WHERE hash IS NOT NULL);" +
            "        DELETE FROM assetfile WHERE hash IN (SELECT hash FROM gc1) AND modifieddate < now() - interval '24 hours';" +
            "        DROP TABLE gc1;" +
            "        RETURN true;" +
            "      END;" +
            "      $$ LANGUAGE plpgsql;" +
            "  EXCEPTION" +
            "      WHEN duplicate_function THEN" +
            "      null;" +
            "" +
            "  END;" +
            "$DO$");
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
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  // User management

  /**
   * Returns user data given user id. If the user data for the given id
   * doesn't already exist in the storage, WE RETURN NULL. This is different
   * than our ObjectifyStorageIo equivalent.
   *
   * @param userId unique user id
   * @return user data
   */
  @Override
  public User getUser(@Nonnull String strUserId) {
    User user = null;
    boolean ok = false;

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM account WHERE uuid = ?::UUID")) {
        qstmt.setString(1, strUserId);
        ResultSet rs = qstmt.executeQuery();

        // We assume single result due to unique constraint on userId and email
        if (rs.next()) {
          user = new User(
            strUserId,
            rs.getString("email"),
            rs.getBoolean("tosAccepted"),
            rs.getBoolean("isAdmin"),
            rs.getString("sessionId")
            );
          if (user == null) {
            // Here we have distinct behavior from ObjectifyStorageIo.getUser()
            // We do not create user.
            // throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, null), new RuntimeException("Unknown database error"));
            // Return null instead of throwing an error
            return null;
          }
          if (!requireTos.get()) { // If we do not require TOS, fake it
            user.setUserTosAccepted(true);
          }
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getUser");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    return getUserFromEmail(email, true);
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
  public User getUserFromEmail(@Nonnull String email, boolean create) {
    User user = null;
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM account WHERE lower(email) = lower(?)")) {
        qstmt.setString(1, email);
        ResultSet rs = qstmt.executeQuery();

        if (rs.next()) {
          user = new User(
            rs.getString("uuid"),
            rs.getString("email"),
            rs.getBoolean("tosAccepted"),
            rs.getBoolean("isAdmin"),
            rs.getString("sessionId")
            );
          user.setPassword(rs.getString("password"));
        }
        if (user == null) {
          if (create) {
            user = privateCreateUser(email, false, null, conn);
          } else {
            return null;
          }
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getUser");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return user;
  }

  /**
   * Sets the stored email address for user with id userId
   *
   */
  @Override
  public void setUserEmail(@Nonnull String strUserId, @Nonnull String email) {
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);

      try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET email = ? WHERE uuid = ?::UUID")) {
        stmt.setString(1, email);
        stmt.setString(2, strUserId);
        int ret = stmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "setUserEmail");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  /**
   * Sets that the user has accepted the terms of service.
   *
   * @param userId user id
   */
  @Override
  public void setTosAccepted(@Nonnull String strUserId) {
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET tosAccepted = ? WHERE uuid = ?::UUID")) {
        stmt.setBoolean(1, true);
        stmt.setString(2, strUserId);
        int ret = stmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "setTosAccepted");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET sessionId = ? WHERE uuid = ?::UUID")) {
        stmt.setString(1, sessionId);
        stmt.setString(2, strUserId);
        int ret = stmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "setUserSessionId");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    // Empty password is not allowed in database, we simply remove it.
    if (password.equals("")) {
      password = null;
    }
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET password = ? WHERE uuid = ?::UUID")) {
        stmt.setString(1, password);
        stmt.setString(2, strUserId);
        int ret = stmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "setUserPassword");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement stmt = conn.prepareStatement("SELECT settings FROM account WHERE uuid = ?::UUID")) {
        stmt.setString(1, strUserId);
        ResultSet rs = stmt.executeQuery();
        String settings = null;
        if (rs.next()) {
          settings = rs.getString("settings");
        }
        if (settings == null) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
        return settings;
      } finally {
        doFinish(conn, ok, "loadSettings");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement stmt = conn.prepareStatement("SELECT update_settings(?::UUID, ?)")) {
          stmt.setString(1, strUserId);
          stmt.setString(2, settings);
          ResultSet rs = stmt.executeQuery();
          boolean ret = false;
          if (rs.next()) {
            ret = rs.getBoolean("update_settings");
          }
          if (!ret) {
            throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, null), new RuntimeException("Unknown database error"));
          }
          ok = true;
      } catch (SQLException e) {
        throw CrashReport.createAndLogError(LOG, null, "Error preparing statement", e);
      } finally {
        doFinish(conn, ok, "storeSettings");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    long userId;
    int ret = 0;
    String name = project.getProjectName();
    String type = project.getProjectType();
    String history = project.getProjectHistory();
    Long projectId = null;             // Defined after insertion
    try (Connection conn = this.cpds.getConnection()) {
      // Insert project data
      doSetAutoCommit(conn, false);
      try {
        userId = getUserId(strUserId, conn, false);
        try (PreparedStatement ustmt = conn.prepareStatement(
            "INSERT INTO project (userId, name, type, history, settings) VALUES (?, ?, ?::project_kind, ?, ?)",
            Statement.RETURN_GENERATED_KEYS)) {
          ustmt.setLong(1, userId);
          ustmt.setString(2, name);
          ustmt.setString(3, type);
          ustmt.setString(4, history);
          ustmt.setString(5, projectSettings);

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
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, null), new RuntimeException("Unknown database error"));
        }

        // Save files
        try {
          for (TextFile file : project.getSourceFiles()) {
            FileData.RoleEnum role = FileData.RoleEnum.SOURCE;
            String fileName = file.getFileName();
            byte[] content = file.getContent().getBytes(StorageUtil.DEFAULT_CHARSET);
            createProjectFile(projectId, userId, role, fileName, content, conn);
          }
        } catch (UnsupportedEncodingException e) {
          throw CrashReport.createAndLogError(LOG, null, "Cannot decode file content", e);
        }

        for (RawFile file : project.getRawSourceFiles()) {
          FileData.RoleEnum role = FileData.RoleEnum.SOURCE;
          String fileName = file.getFileName();
          byte[] content = file.getContent();
          createProjectFile(projectId, userId, role, fileName, content, conn);
        }

        ok = true;
      } finally {
        doFinish(conn, ok, "creatProject");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    long userId;
    boolean ok = false;

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      userId = getUserId(strUserId, conn, false);

      // Corresponding entries in projectFile table will be automatically remove by ON DELETE CASCADE
      try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM project WHERE id = ? AND userId = ?")) {
        ustmt.setLong(1, projectId);
        ustmt.setLong(2, userId);
        ustmt.executeUpdate();     // Don't care about result
        ok = true;
      } finally {
        doFinish(conn, ok, "deleteProject");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    List<Long> ret = new ArrayList<>();
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT id FROM project WHERE userId = ?")) {
        qstmt.setLong(1, userId);
        ResultSet rs = qstmt.executeQuery();

        while (rs.next()) {
          ret.add(rs.getLong("id"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getProjects");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return ret;
  }

  /**
   * Returns an array with the user's project's names.
   *
   * @param userId  user ID
   * @return  list of project names
   */
  @Override
  public List<String> getProjectNames(@Nonnull String strUserId) {
    List<String> ret = new ArrayList<>();
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT name FROM project WHERE userId = ?")) {
        qstmt.setLong(1, userId);
        ResultSet rs = qstmt.executeQuery();

        while (rs.next()) {
          ret.add(rs.getString("name"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getProjectNames");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return ret;
  }

  /**
   * sets a projects name
   * @param userId a user Id (the request is made on behalf of this user)*
   * @param projectId project ID
   * @param newName New name for project
   */
  @Override
  public void setProjectName(@Nonnull final String strUserId, final long projectId,final String newName) {
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);

      try (PreparedStatement ustmt = conn.prepareStatement("UPDATE project SET name = ?, modifiedDate = CURRENT_TIMESTAMP WHERE projectId = ? AND userId = ?")) {
        ustmt.setString(1, newName);
        ustmt.setLong(2, projectId);
        ustmt.setLong(3, userId);
        int ret = ustmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "setProjectName");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    String settings = null;
    try (Connection conn = this.cpds.getConnection()) {
      conn.setAutoCommit(false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT settings FROM project WHERE id = ? AND userId = ?")) {
        qstmt.setLong(1, projectId);
        qstmt.setLong(2, userId);
        ResultSet rs = qstmt.executeQuery();

        if (rs.next()) {
          settings = rs.getString("settings");
        }
        if (settings == null) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "loadProjectSettings");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement ustmt = conn.prepareStatement("UPDATE project SET settings = ?, modifiedDate = CURRENT_TIMESTAMP WHERE id = ? AND userId = ?")) {
        ustmt.setString(1, settings);
        ustmt.setLong(2, projectId);
        ustmt.setLong(3, userId);
        int ret = ustmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "storeProjectSettings");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    String type = null;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT type FROM project WHERE id = ? AND userId = ?")) {
        qstmt.setLong(1, projectId);
        qstmt.setLong(2, userId);
        ResultSet rs = qstmt.executeQuery();
        if (rs.next()) {
          type = rs.getString("type");
        }
        if (type == null) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getProjectType");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    UserProject proj = null;
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM project WHERE id = ? AND userId = ?")) {
        qstmt.setLong(1, projectId);
        qstmt.setLong(2, userId);
        ResultSet rs = qstmt.executeQuery();
        if (rs.next()) {
          String name = rs.getString("name");
          String type = rs.getString("type");
          Timestamp creationDate = rs.getTimestamp("creationDate");
          Timestamp modifiedDate = rs.getTimestamp("modifiedDate");

          proj = new UserProject(
            projectId,
            name,
            type,
            creationDate.getTime(),
            modifiedDate.getTime(),
            0,                // builtDate
            false             // moveToSTrashFlag
            );
        }
        if (proj == null) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getUserProject");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    List<UserProject> ret = new ArrayList<>();
    boolean ok = false;

    // degradation case
    if (projectIds.size() == 0) {
      return ret;
    }

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
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
          Timestamp builtDate = rs.getTimestamp("builtDate");
          boolean trashflag = rs.getBoolean("trashflag");
          long returnBuiltDate = 0;
          if (builtDate != null) {
            returnBuiltDate = builtDate.getTime();
          }
          proj = new UserProject(
            projectId,
            name,
            type,
            creationDate.getTime(),
            modifiedDate.getTime(),
            returnBuiltDate,
            trashflag
            );
          ret.add(proj);
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getUserProjects");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    String name = null;
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {

      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT name FROM project WHERE id = ? AND userId = ?")) {
        qstmt.setLong(1, projectId);
        qstmt.setLong(2, userId);
        ResultSet rs = qstmt.executeQuery();

        if (rs.next()) {
          name = rs.getString("name");
        }
        if (name == null) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getProjectName");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return name;
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
    boolean ok = false;
    String history = null;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);

      try (PreparedStatement qstmt = conn.prepareStatement("SELECT history FROM project WHERE id = ? AND userId = ?")) {
        qstmt.setLong(1, projectId);
        qstmt.setLong(2, userId);
        ResultSet rs = qstmt.executeQuery();
        if (rs.next()) {
          history = rs.getString("history");
        }
        ok = true;
        if (history == null) {
          return "";
        }
      } finally {
        doFinish(conn, ok, "getProjectHistory");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return history;
  }

  @Override
  public String getProjectUserId(long projectId) {
    boolean ok = false;
    String strUserId = null;
    long accountId = 0;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT userId FROM project WHERE id = ?")) {
        qstmt.setLong(1, projectId);
        ResultSet rs = qstmt.executeQuery();
        if (rs.next()) {
          accountId = rs.getInt("userId");
        }
        if (accountId == 0) {
          return null;          // Didn't find them
        }
        PreparedStatement qstmt1 = conn.prepareStatement("SELECT uuid FROM account where id = ?");
        qstmt1.setLong(1, accountId);
        ResultSet rs1 = qstmt1.executeQuery();
        if (rs1.next()) {
          strUserId = rs1.getString("uuid");
        }
        if (strUserId == null) {
          return null;          // Didn't find them
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getProjectUserId");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return strUserId;
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
    boolean ok = false;
    // JIS: Not sure we even need this function, it should probably just be a no-op

    // Degradation case
    if (fileNames.length == 0) {
      return;
    }

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO userFile (userId, fileName) VALUES (?, ?) ON CONFLICT (userId, fileName) DO NOTHING")) {
        for (String fileName : fileNames) {
          ustmt.setLong(1, userId);
          ustmt.setString(2, fileName);
          ustmt.addBatch();
        }
        ustmt.executeBatch();     // We don't care about the result
        ok = true;
      } finally {
        doFinish(conn, ok, "addFilesToUser");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    List<String> ret = new ArrayList<String>();

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);

      try (PreparedStatement qstmt = conn.prepareStatement("SELECT fileName FROM userFile WHERE userId = ?")) {
        qstmt.setLong(1, userId);
        ResultSet rs = qstmt.executeQuery();

        while (rs.next()) {
          ret.add(rs.getString("fileName"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getUserFiles");
      }

    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
      contentBytes = content.getBytes(encoding);
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
    public void uploadRawUserFile(@Nonnull String strUserId, @Nonnull String fileName, @Nonnull byte[] content) {
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement ustmt = conn.prepareStatement(
          "INSERT INTO userFile (userId, fileName, content) VALUES (?, ?, ?) ON CONFLICT (userId, fileName) DO UPDATE SET content = ?"
          )) {
        ustmt.setLong(1, userId);
        ustmt.setString(2, fileName);
        ustmt.setBytes(3, content);
        ustmt.setBytes(4, content);
        long ret = ustmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, fileName), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "uploadRawUserFile");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    byte[] contentBytes = downloadRawUserFile(strUserId, fileName);
    String content = null;

    try {
      content = contentBytes != null ? new String(contentBytes, encoding) : null;
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, fileName), e);
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
    boolean ok = false;
    byte[] ret = null;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);

      try (PreparedStatement qstmt = conn.prepareStatement("SELECT content FROM userFile WHERE userId = ? AND fileName = ?")) {
        qstmt.setLong(1, userId);
        qstmt.setString(2, fileName);
        ResultSet rs = qstmt.executeQuery();

        if (rs.next()) {
          ret = rs.getBytes("content");
        }
        if (ret == null) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, fileName), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "downloadRawUserFile");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);

      try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM userFile WHERE userId = ? AND fileName = ?")) {
        ustmt.setLong(1, userId);
        ustmt.setString(2, fileName);
        int ret = ustmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, fileName), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "deleteUserFile");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    int ret = 0;

    // Degradation case
    if (fileNames.length == 0) {
      return;
    }

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try {
        long userId = getUserId(strUserId, conn, false);

        if (changeModDate) {
          updateProjectModifiedDate(projectId, userId, conn);
        }
        // Create files
        bulkCreateProjectFile(projectId, userId, FileData.RoleEnum.SOURCE, fileNames, conn);
        ok = true;
      } finally {
        doFinish(conn, ok, "addSourceFilesToProject");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);
        bulkCreateProjectFile(projectId, userId, FileData.RoleEnum.TARGET, fileNames, conn);
        ok = true;
      } finally {
        doFinish(conn, ok, "addOutputFilesToProject");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;

    try (Connection conn = this.cpds.getConnection()) {
      // Update project modified date
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);

        if (changeModDate) {
          updateProjectModifiedDate(projectId, userId, conn);
        }
        bulkDeleteProjectFile(projectId, userId, FileData.RoleEnum.SOURCE, fileNames, conn);
        ok = true;
      } finally {
        doFinish(conn, ok, "removeSourceFilesFromProject");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;

    // Degradation case
    if (fileNames.length == 0) {
      return;
    }
    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);

        bulkDeleteProjectFile(projectId, userId, FileData.RoleEnum.TARGET, fileNames, conn);
        ok = true;
      } finally {
        doFinish(conn, ok, "removeOutputFilesFromProject");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    List<String> ret = new ArrayList<String>();

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);

      try (PreparedStatement qstmt = conn.prepareStatement("SELECT fileName FROM projectFile WHERE projectId = ? AND userId = ? AND role = ?::file_role")) {
        String roleString = FileData.RoleEnum.SOURCE.name();
        qstmt.setLong(1, projectId);
        qstmt.setLong(2, userId);
        qstmt.setString(3, roleString);
        ResultSet rs = qstmt.executeQuery();

        while (rs.next()) {
          ret.add(rs.getString("fileName"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getProjectSourceFiles");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    List<String> ret = new ArrayList<String>();

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);

      try (PreparedStatement qstmt = conn.prepareStatement("SELECT fileName FROM projectFile WHERE projectId = ? AND userId = ? AND role = ?::file_role")) {
        String roleString = FileData.RoleEnum.TARGET.name();
        qstmt.setLong(1, projectId);
        qstmt.setLong(2, userId);
        qstmt.setString(3, roleString);
        ResultSet rs = qstmt.executeQuery();

        while (rs.next()) {
          ret.add(rs.getString("fileName"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getProjectOutputFiles");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return ret;
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
    boolean ok = false;
    long ts;

    byte[] contentBytes;
    try {
      contentBytes = content.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding, " + makeErrorMsg(null, null, projectId, fileName), e);
    }

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);
        ts = updateProjectFileContent(projectId, userId, fileName, contentBytes, false, conn);
        ok = true;
      } finally {
        doFinish(conn, ok, "uploadFile");
      }
      return ts;
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    long ts;

    byte[] contentBytes;
    try {
      contentBytes = content.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding," + makeErrorMsg(null, null, projectId, fileName), e);
    }

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);
        ts = updateProjectFileContent(projectId, userId, fileName, contentBytes, true, conn);
        ok = true;
      } finally {
        doFinish(conn, ok, "uploadFileForce");
      }
      return ts;
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    long ts;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);
        ts = updateProjectFileContent(projectId, userId, fileName, content, false, conn);
        ok = true;
      } finally {
        doFinish(conn, ok, "uploadRawFile");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    long ts;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);
        ts = updateProjectFileContent(projectId, userId, fileName, content, true, conn);
        ok = true;
      } finally {
        doFinish(conn, ok, "uploadRawFileForce");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    int ret = 0;
    long ts;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);

        try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM projectFile WHERE projectId = ? AND userId = ? AND fileName = ?")) {
          ustmt.setLong(1, projectId);
          ustmt.setLong(2, userId);
          ustmt.setString(3, fileName);
          ret = ustmt.executeUpdate();
        }

        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, fileName), new RuntimeException("Unknown database error"));
        }

        ts = updateProjectModifiedDate(projectId, userId, conn);
        ok = true;
      } finally {
        doFinish(conn, ok, "deleteFile");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    byte[] contentBytes = downloadProjectFile(projectId, strUserId, fileName);
    String content = null;

    try {
      content = new String(contentBytes, encoding);
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding, " + makeErrorMsg(strUserId, null, projectId, fileName), e);
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
    boolean ok = false;
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);

        try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO corruptionReport (userId, projectId, fileName, message) VALUES (?, ?, ?, ?)")) {
          ustmt.setLong(1, userId);
          ustmt.setLong(2, projectId);
          ustmt.setString(3, fileName);
          ustmt.setString(4, message);
          ret = ustmt.executeUpdate();
          if (ret == 0) {
            throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, fileName), new RuntimeException("Unknown database error"));
          }
          ok = true;
        }
      } finally {
        doFinish(conn, ok, "recordCorruption");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    byte[] contentBytes = downloadProjectFile(projectId, strUserId, fileName);
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
    int ret = 0;
    boolean ok = false;
    String fileName;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        fileName = "__TEMP__/" + UUID.randomUUID().toString();
        try (PreparedStatement ustmt = conn.prepareStatement(
            "INSERT INTO tempFile (fileName, content) VALUES (?, ?)")) {
          ustmt.setString(1, fileName);
          ustmt.setBytes(2, content);
          ret = ustmt.executeUpdate();
          if (ret != 1) {
            throw CrashReport.createAndLogError(LOG, null, "Database error in uploadTempFile()", new RuntimeException("Unknown database error"));
          }
          ok = true;
        }
      } finally {
        doFinish(conn, ok, "uploadTempFile");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
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
    boolean ok = false;
    InputStream stream = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT content FROM tempFile WHERE fileName = ?")) {
          qstmt.setString(1, fileName);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            stream = rs.getBinaryStream("content");
          }
          if (stream == null) {
            throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, null, null, fileName), new RuntimeException("Unknown database error"));
          }
          ok = true;
        }
      } finally {
        doFinish(conn, ok, "openTempFile");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);

        try (PreparedStatement ustmt = conn.prepareStatement("DELETE FROM tempFile WHERE fileName = ?")) {
          ustmt.setString(1, fileName);
          int ret = ustmt.executeUpdate();
          if (ret == 0) {
            throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, null, null, fileName), new RuntimeException("Unknown database error"));
          }
          ok = true;
        }
      } finally {
        doFinish(conn, ok, "deleteTempFile");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  /**
   *  Exports project files as a zip archive
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param includeProjectHistory  whether or not to include the project history
   * @param includeAndroidKeystore  whether or not to include the Android keystore
   * @param zipName  the name of the zip file, if a specific one is desired
   * @param fatalError set true to cause missing GCS file to throw exception
   * @param includeYail            include any yail files in the project
   * @param includeScreenShots     include any screen shots stored with the project
   * @param forGallery             flag to indicate we are exporting for the gallery
   * @param fatalError             Signal a fatal error if a file is not found
   * @param forAppStore            true if the export is for an App Store build
   * @param locallyCachedApp       true if we are providing cached app to Companion
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
    final boolean fatalError,
    final boolean forAppStore,
    final boolean locallyCachedApp
  ) throws IOException {
    class FileRow {
      String fileName;
      FileData.RoleEnum role;
      byte[] content;

      FileRow (String _fileName, FileData.RoleEnum _role, byte[] _content) {
        fileName = _fileName;
        role = _role;
        content = _content;
      }

      void setContent(byte[] _content) {
        content = _content;
      }

    }

    List<FileRow> files = new ArrayList<FileRow>();
    boolean projectFound = false;
    String projectName = null;
    String projectHistory = null;
    byte[] keystoreContent = null;
    byte[] appStoreCredentials = null;
    boolean ok = false;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);

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
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, null), new RuntimeException("Canont find project"));
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
              throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, null), e);
            }

            FileRow fr = new FileRow(rs.getString("fileName"), role, rs.getBytes("content"));
            String hash = rs.getString("hash");
            if (hash != null) { // Asset File
              try (PreparedStatement qstmt1 = conn.prepareStatement("SELECT content FROM assetFile where hash = ?")) {
                qstmt1.setString(1, hash);
                ResultSet rs1 = qstmt1.executeQuery();
                if (rs1.next()) {
                  fr.setContent(rs1.getBytes("content"));
                }
              }
            }

            // Kick out some files
            if (fr.fileName.startsWith("assets/external_comps") && forGallery) {
              throw new IOException("FATAL Error, external component in gallery app");
            }
            if (!fr.role.equals(FileData.RoleEnum.SOURCE)) {
              continue;
            }


            String fileName = fr.fileName;
            if (fileName.equals(FileExporter.REMIX_INFORMATION_FILE_PATH) ||
                (fileName.startsWith("screenshots") && !includeScreenShots) ||
                (fileName.startsWith("src/") && fileName.endsWith(".yail") && !includeYail) ||
                (fileName.startsWith("src/") && fileName.endsWith(".bky") && locallyCachedApp) ||
                (fileName.startsWith("src/") && fileName.endsWith(".scm") && locallyCachedApp)) {
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

        // Find Apple App Store Credentials
        if (forAppStore) {
          try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM userFile WHERE userId = ? AND fileName = ? AND content IS NOT NULL AND length(content) > 0")) {
            qstmt.setLong(1, userId);
            qstmt.setString(2, StorageUtil.APPSTORE_CREDENTIALS_FILENAME);
            ResultSet rs = qstmt.executeQuery();

            if (rs.next()) {
              appStoreCredentials = rs.getBytes("content");
            }
          }
        }

        ok = true;
      } finally {
        doFinish(conn, ok, "exportProjectSourceZip");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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

    if (appStoreCredentials != null) { // App Store Credenials
      zipStream.putNextEntry(new ZipEntry(StorageUtil.APPSTORE_CREDENTIALS_FILENAME));
      zipStream.write(appStoreCredentials, 0, appStoreCredentials.length);
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
    boolean ok = false;
    String strUserId = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);

        // Case-sensitive search
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM account WHERE email = ?")) {
          qstmt.setString(1, email);
          ResultSet rs = qstmt.executeQuery();
          if (rs.next()) {
            strUserId = rs.getString("strUserId");
          }
          if (strUserId == null) {
            throw new NoSuchElementException("Couldn't find a user with email: " + email);
          }
          ok = true;
        }
      } finally {
        doFinish(conn, ok, "findUserByEmail");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
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
    boolean ok = false;
    String addr = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT address FROM ipAddress WHERE key = ?")) {
          qstmt.setString(1, key);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            addr = rs.getString("address");
          }
          ok = true;
        }
      } finally {
        doFinish(conn, ok, "findIpAddressByKey");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement ustmt = conn.prepareStatement(
          "INSERT INTO ipAddress (key, address) VALUES (?, ?) ON CONFLICT (key) DO UPDATE SET address = ?"
          )) {
        ustmt.setString(1, key);
        ustmt.setString(2, ipAddress);
        ustmt.setString(3, ipAddress);
        ret = ustmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, String.format("key=%s, ipAddress=%s", key, ipAddress), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "storeIpAddressByKey");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  @Override
  public boolean checkWhiteList(@Nonnull String email) {
    boolean ok = false;
    boolean pass = false;

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      // Try to get existing user
      User user = null;
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT COUNT(1) FROM whitelist WHERE lower(email) = lower(?)")) {
        qstmt.setString(1, email);
        ResultSet rs = qstmt.executeQuery();
        pass = rs.next();
        ok = true;
      } finally {
        doFinish(conn, ok, "checkWhiteList");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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

    boolean ok = false;
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);

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
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, "Error in storeFeedback()", new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "storeFeedback");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  @Override
  public Nonce getNoncebyValue(@Nonnull String nonceValue) {
    boolean ok = false;
    Nonce nonce = null;

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);

      try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM nonce WHERE nonce = ?")) {
        qstmt.setString(1, nonceValue);
        ResultSet rs = qstmt.executeQuery();

        if (rs.next()) {
          String strUserId = rs.getString("strUserId");
          long projectId = rs.getLong("projectId");
          Timestamp ts = rs.getTimestamp("timestamp");

          Date dt = new Date(ts.getTime());
          nonce = new Nonce(nonceValue, strUserId, projectId, dt);
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getNoncebyValue");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return nonce;
  }

  @Override
  public void storeNonce(@Nonnull final String nonceValue, @Nonnull final String strUserId, final long projectId) {
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement ustmt = conn.prepareStatement(
        "INSERT INTO nonce (projectId, strUserId, nonce) VALUES (?, ?::UUID, ?) ON CONFLICT (nonce) DO UPDATE SET projectId = ?, strUserId = ?::UUID"
          )) {
        ustmt.setLong(1, projectId);
        ustmt.setString(2, strUserId);
        ustmt.setString(3, nonceValue);
        ustmt.setLong(4, projectId);
        ustmt.setString(5, strUserId);

        int ret = ustmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "storeNonce");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  /**
   * Cleanup old Nonces *and* cleanup old apk files that can no longer be downloaded
   *
   * This code used to only be used to cleanup the nonces (used as part of the download
   * link for packaged apk files). However, we also cleanup 3 expired APK files, oldest
   * ones first.
   */
  @Override
  public void cleanupNonces() {
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);

      try (PreparedStatement ustmt = conn.prepareStatement(
          "DELETE FROM nonce WHERE timestamp < (CURRENT_TIMESTAMP - INTERVAL '3 hour')"
          )) {
        ustmt.executeUpdate();
        ok = true;
      } finally {
        doFinish(conn, ok, "cleanupNonces");
      }
      ok = false;
      try (PreparedStatement ustmt = conn.prepareStatement(
        "WITH rows as (SELECT id FROM projectfile WHERE role = 'TARGET' " +
        "AND ts < (CURRENT_TIMESTAMP - INTERVAL '3 hour') ORDER BY ts LIMIT 3) " +
        "DELETE FROM projectfile WHERE id IN (SELECT id FROM rows);"
          )) {
        ustmt.executeUpdate();
        ok = true;
      } finally {
        doFinish(conn, ok, "cleanupNonces");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  // Retrieve the current Splash Screen Version
  @Override
  public SplashConfig getSplashConfig() {
    boolean ok = false;
    final String SPLASH_CONFIG_KEY = "splash_config";
    final int DEFAULT_VERSION = 0;
    final String DEFAULT_CONTENT = "<b>Welcome to MIT App Inventor</b>";
    final int DEFAULT_WIDTH = 350;
    final int DEFAULT_HEIGHT = 100;

    SplashConfig result = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);

        try (PreparedStatement qstmt = conn.prepareStatement("SELECT version,width,height,content FROM splashconfig WHERE active = true order by id DESC")) {
          ResultSet rs = qstmt.executeQuery();
          if (rs.next()) {
            int version = rs.getInt("version");
            int width = rs.getInt("width");
            int height = rs.getInt("height");
            String content = rs.getString("content");
            result = new SplashConfig(version, width, height, content);
          }
        }

        // No active Splash Screen
        if (result == null) {
          result = new SplashConfig(DEFAULT_VERSION, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_CONTENT);
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getSplashConfig");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return result;
  }

  @Override
  public PWData createPWData(@Nonnull String email) {
    boolean ok = false;
    Long id = null;
    String uuid = UUID.randomUUID().toString();
    Timestamp ts = null;

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        PreparedStatement ustmt = conn.prepareStatement(
          "INSERT INTO pwData (uuid, email) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        ustmt.setString(1, uuid);
        ustmt.setString(2, email);
        int ret = ustmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, "Error storing PWData", new RuntimeException("Database Error storing PWData"));
        }
        ResultSet keys = ustmt.getGeneratedKeys();
        if (keys.next()) {
          id = keys.getLong(1);
        }

        // Get timestamp
        if (id != null) {
          try (PreparedStatement qstmt = conn.prepareStatement("SELECT timestamp FROM pwData WHERE id = ?")) {
            qstmt.setLong(1, id);
            ResultSet rs = qstmt.executeQuery();
            if (rs.next()) {
              ts = rs.getTimestamp("timestamp");
            }
          }
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "createPWData");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    PWData ret = null;

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
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
        ok = true;
      } finally {
        doFinish(conn, ok, "findPWData");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return ret;
  }

  @Override
  public void cleanuppwdata() {
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement ustmt = conn.prepareStatement(
          "DELETE FROM pwData WHERE timestamp < (CURRENT_TIMESTAMP - INTERVAL '1 day')"
          )) {
        ustmt.executeUpdate();    // Don't care about the result
        ok = true;
      } finally {
        doFinish(conn, ok, "cleanuppwdata");
      }

    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  // Routines for user admin interface

  @Override
  public List<AdminUser> searchUsers(@Nonnull String partialEmail) {
    boolean ok = false;
    List<AdminUser> result = new ArrayList<AdminUser>();

    try {
      DataSource readOnlySource = DataSources.unpooledDataSource(jdbcReadOnlyUrl.get(), jdbcUser.get(), jdbcPassword.get());

      try (Connection conn = readOnlySource.getConnection()) {
        doSetAutoCommit(conn, false);
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM account WHERE email ~* ? ORDER BY email LIMIT 20")) {
          qstmt.setString(1, partialEmail);
          ResultSet rs = qstmt.executeQuery();
          while (rs.next()) {
            String strUserId = rs.getString("uuid");
            Timestamp visitedTs = rs.getTimestamp("visited");
            Date visitedDt = visitedTs != null ? new Date(visitedTs.getTime()) : null;
            AdminUser user = new AdminUser(
              strUserId,
              "",
              rs.getString("email"),
              rs.getBoolean("tosAccepted"),
              rs.getBoolean("isAdmin"),
              visitedDt);
            result.add(user);
          }
          ok = true;
        } finally {
          doFinish(conn, ok, "searchUsers");
        }
      } catch (SQLException e) {
        throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
      }

    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Cannot setup readonly connection", e);
    }
    return result;
  }

  @Override
  public void storeUser(@Nonnull AdminUser user) throws AdminInterfaceException {
    // The implementation has distinct behavior from ObjectifyStorageIo.storeUser()
    // We require non-null user.getId() and existince of this user.
    boolean ok = false;

    String strUserId = user.getId();

    String newPassword = user.getPassword();
    if (newPassword.equals("")) {
      newPassword = null;
    }
    String newEmail = user.getEmail();
    boolean newIsAdmin = user.getIsAdmin();

    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, true);
        if (userId != 0) {
          // If it conflicts on email, we fail it anyway.
          int ret = 0;
          PreparedStatement ustmt = conn.prepareStatement("UPDATE account SET email = ?, isAdmin = ?, password = COALESCE(?, password) WHERE id = ?");
          ustmt.setString(1, newEmail);
          ustmt.setBoolean(2, newIsAdmin);
          ustmt.setString(3, newPassword);
          ustmt.setLong(4, userId);
          ret = ustmt.executeUpdate();
          if (ret == 0) {
            throw new AdminInterfaceException("Cannot find user with userId = " + strUserId);
          }
        } else {
          privateCreateUser(newEmail, newIsAdmin, newPassword, conn);
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "storeUser");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    String ret = "[]";

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT content FROM backpack WHERE id = ?")) {
        qstmt.setString(1, backPackId);
        ResultSet rs = qstmt.executeQuery();

        if (rs.next()) {
          ret = rs.getString("content");
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "downloadBackpack");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    int ret = 0;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement ustmt = conn.prepareStatement("INSERT INTO backpack (id, content) VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET content = ?")) {
        ustmt.setString(1, backPackId);
        ustmt.setString(2, content);
        ustmt.setString(3, content);
        ret = ustmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, String.format("backPackId=?", backPackId), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "uploadBackpack");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
    boolean ok = false;
    int ret = 0;

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement ustmt = conn.prepareStatement(
          "INSERT INTO buildStatus (host, userId, projectId, progress) VALUES (?, ?, ?, ?) ON CONFLICT (host, userId, projectId) DO UPDATE SET progress = ?"
          )) {
        ustmt.setString(1, HOST_ID);
        ustmt.setLong(2, userId);
        ustmt.setLong(3, projectId);
        ustmt.setInt(4, progress);
        ustmt.setInt(5, progress);
        ret = ustmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "storeBuildStatus");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  @Override
  public int getBuildStatus(@Nonnull String strUserId, long projectId) {
    boolean ok = false;
    Integer ret = null;

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);

      try (PreparedStatement qstmt = conn.prepareStatement("SELECT progress FROM buildStatus WHERE host = ? AND projectId = ? AND userId = ?")) {
        qstmt.setString(1, HOST_ID);
        qstmt.setLong(2, projectId);
        qstmt.setLong(3, userId);
        ResultSet rs = qstmt.executeQuery();

        if (rs.next()) {
          ret = rs.getInt("progress");
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "getBuildStatus");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return ret != null ? ret : 50;
  }

  @Override
  public List<String> getTutorialsUrlAllowed() {
    // Rather then store these in the database or the filesystem (or CEPH) we just
    // hard code them here for now
    return new ArrayList<> (Arrays.asList(
        "http://appinventor.mit.edu/",
        "https://appinventor.mit.edu/",
        "http://appinv.us/",
        "http://templates.appinventor.mit.edu/"));
  }

  @Override
  public long getProjectDateBuilt(final String userId, final long projectId) {
    return getProjectDates(userId, projectId, "built");
  }

  @Override
  public long getProjectDateModified(final String userId, final long projectId) {
    return getProjectDates(userId, projectId, "modified");
  }

  @Override
  public String getIosExtensionsConfig() {
    return getMisc("ios_extensions_allowed", DEFAULT_ALLOWED_IOS_EXTENSIONS);
  }

  @Override
  public boolean deleteAccount(String strUserId) {
    boolean ok = false;
    boolean oktodelete = false;
    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT count(*) FROM project WHERE userId = ? and trashflag = false")) {
          qstmt.setLong(1, userId);
          ResultSet rs = qstmt.executeQuery();
          if (rs.next()) {
            long count = rs.getLong(1);
            if (count == 0) {
              oktodelete = true;
            }
          }
        }
        if (oktodelete) {
          try (PreparedStatement qstmt = conn.prepareStatement("DELETE from userFile WHERE userId = ?")) {
            qstmt.setLong(1, userId);
            int ret = qstmt.executeUpdate(); // We don't care if it fails
          }
          try (PreparedStatement qstmt = conn.prepareStatement("DELETE from account WHERE id = ?")) {
            qstmt.setLong(1, userId);
            int ret = qstmt.executeUpdate(); // We don't care if it fails, maybe we should?
          }
          ok = true;
          return true;
        } else {
          ok = true;
          return false;
        }
      } catch (SQLException e) {
        throw CrashReport.createAndLogError(LOG, null, "Error deleting user " + strUserId, e);
      } finally {
        doFinish(conn, ok, "deleteAccount");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Error making database connection (deleteAccount)", e);
    }
  }

  @Override
  public void assertUserHasProject(String userId, long projectId) {
    // We are a no-op in this storage backend as this cannot happen here
  }

  @Override
  public User createAnonymousAccount() {
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        String strAnonId;
        // Verify that we have a unique account
        long count = 0;
        while (true) {
          strAnonId = AccountUtil.generateAccountId();
          try (PreparedStatement qstmt = conn.prepareStatement("SELECT email FROM account WHERE email = ?")) {
            qstmt.setString(1, strAnonId);
            ResultSet rs = qstmt.executeQuery();
            if (rs.next()) {
              count++;
              if (count > 100) {
                throw CrashReport.createAndLogError(LOG, null, "Create Anon Account cannot find an ID!", new RuntimeException("Cannot find account id"));
              }
              continue;           // We already have an account with this id
            }
          }
          break;                // We found an ID we can use
        }

        String strUserId = UUID.randomUUID().toString();

        try (PreparedStatement stmt = conn.prepareStatement("INSERT into account (uuid, email) values (?::UUID, ?)")) {
          stmt.setString(1, strUserId);
          stmt.setString(2, strAnonId);
          int ret = stmt.executeUpdate();
          if (ret == 0) {
            throw CrashReport.createAndLogError(LOG, null, "Failed to store anonymous account", new RuntimeException("Failed to store anonymous account"));
          }
          ok = true;
          return new User(
            strUserId,
            strAnonId,
            false,
            false,
            null);
        }
      } finally {
        doFinish(conn, ok, "createAnonymousAccount");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unable to open database", e);
    }
  }

  // TBD
  @Override
  public void setLicenseConfig(LicenseConfig conf) {
  }

  /**
   * Get the license configuration of the server.
   *
   * We currently are not using any licensing code, it is commented out in Ode.
   * So we just return this fixed value so we don't get null pointer faults etc.
   *
   * return the license configuration
   */
  public LicenseConfig getLicenseConfig() {
    return new LicenseConfig("1E5F9C2B6250", "860c5124-6547-40a9-b748-f1b38018a88f", "");
  }

  @Override
  public void setMoveToTrashFlag(final String strUserId, final long projectId, boolean flag) {
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement stmt = conn.prepareStatement("UPDATE project SET trashflag = ? WHERE id = ? and userId = ?")) {
        stmt.setBoolean(1, flag);
        stmt.setLong(2, projectId);
        stmt.setLong(3, userId);
        int ret = stmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, null, null), new RuntimeException("Unknown database error"));
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "setMoveToTrashFlag");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  @Override
  public void createUser(String userId, String email) throws UserAlreadyExistsException {
    boolean ok = false;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try {
  PreparedStatement ustmt = conn.prepareStatement(
    "INSERT INTO account (uuid, email) values (?::UUID, ?)");
        ustmt.setString(1, userId);
        ustmt.setString(2, email);

        ustmt.executeUpdate();
        ok = true;
      } finally {
        doFinish(conn, ok, "createUser");
      }
    } catch (SQLException e) {
      if (e.getSQLState().equals("23505")) { // 23505 = unique_violation (from PostgreSQL Docs version 17)
        throw new UserAlreadyExistsException("User Already Exists");
      } else {
        throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
      }
    }
  }

  // Note: methods that accept a Connection argument are all called by
  // callers who have a pending transaction and are inside a try
  // .. finally block. the caller will then handle whether or not to
  // commit the transaction or roll it back on error. When we throw a
  // crash report it will cause our caller to rollback the
  // transaction.

  private User privateCreateUser(@Nonnull String email, boolean isAdmin, @Nonnull String password, Connection conn) {
    // Insert new user
    Long userId = null;
    boolean tosAccepted = false;
    String nonEmptyPassword = password == null || password.equals("") ? null : password;
    int ret = 0;

    String strUserId = UUID.randomUUID().toString();
    try {
      try (PreparedStatement ustmt = conn.prepareStatement(
        "INSERT INTO account (uuid, tosAccepted, isAdmin, email, password) VALUES (?::UUID, ?, ?, ?, ?)",
        Statement.RETURN_GENERATED_KEYS)) {
        ustmt.setString(1, strUserId);
        ustmt.setBoolean(2, tosAccepted);
        ustmt.setBoolean(3, isAdmin);
        ustmt.setString(4, email);
        ustmt.setString(5, nonEmptyPassword);
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
        throw CrashReport.createAndLogError(LOG, null, "email=" + email, new RuntimeException("Unknown database error"));
      }
    } catch (SQLException e) {
      // One possible case is that two servers insert users of the same email.
      // We fail one of them.
      throw CrashReport.createAndLogError(LOG, null, "Database error in privateCreateUser()", e);
    }

    User user = new User(
      strUserId,
      email,
      tosAccepted,
      isAdmin,
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
    String hash = null;

    if (isAsset(fileName)) {
      MessageDigest md;
      try {
        md = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
        throw CrashReport.createAndLogError(LOG, null, "Cannot find MD5 Message Digest Algorithm!", e);
      }
      byte [] digest = md.digest(content);
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02X", b));
      }
      hash = sb.toString();
      try (PreparedStatement qstmt = conn.prepareStatement(
          "SELECT id from assetFile where hash = ?")) {
        qstmt.setString(1, hash);
        ResultSet rs = qstmt.executeQuery();
        if (rs.next()) {
          long id = rs.getLong(1);
          PreparedStatement stmt = conn.prepareStatement("UPDATE assetFile set modifiedDate = CURRENT_TIMESTAMP WHERE id = ?");
          stmt.setLong(1, id);
          stmt.executeUpdate();
        } else {
          PreparedStatement stmt = conn.prepareStatement("INSERT INTO assetFile (hash, content) values (?, ?) ON CONFLICT(hash) DO NOTHING");
          stmt.setString(1, hash);
          stmt.setBytes(2, content);
          stmt.executeUpdate();
        }
      } catch (SQLException e) {
        throw CrashReport.createAndLogError(LOG, null, "Error creating assetfile", e);
      }
      content = null;           // Because it is now in the assetFile table
    }

    try (PreparedStatement ustmt = conn.prepareStatement(
      "INSERT INTO projectFile (projectId, userId, role, fileName, hash, content, ts) VALUES (?, ?, ?::file_role, ?, ?, ?, CURRENT_TIMESTAMP) ON CONFLICT (projectId, userId, fileName) DO UPDATE SET role = ?::file_role, content = ?, hash = ?, ts = CURRENT_TIMESTAMP"
           )) {
      ustmt.setLong(1, projectId);
      ustmt.setLong(2, userId);
      ustmt.setString(3, roleString);
      ustmt.setString(4, fileName);
      ustmt.setString(5, hash);
      ustmt.setBytes(6, content);
      ustmt.setString(7, roleString);
      ustmt.setBytes(8, content);
      ustmt.setString(9, hash);

      ret = ustmt.executeUpdate();
      if (ret == 0) {
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, userId, projectId, fileName), new RuntimeException("Unknown database error"));
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
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
        "INSERT INTO projectFile (projectId, userId, role, fileName) VALUES (?, ?, ?::file_role, ?) ON CONFLICT (projectId, userId, fileName) DO NOTHING"
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
      String strUserId = "<unknown>"; // Don't bother to make a call to find it out.
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, userId, projectId, null), e);
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
      String strUserId = "<unknown>"; // Don't bother to make a call to find it out.
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, userId, projectId, null), e);
    }
  }

  private long updateProjectFileContent(
    long projectId,
    long userId,
    @Nonnull String fileName,
    @Nonnull byte[] content,
    boolean force,
    Connection conn) throws SQLException {

    int ret = 0;
    long time;

    if (isAsset(fileName)) {
      MessageDigest md;
      try {
        md = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
        throw CrashReport.createAndLogError(LOG, null, "Cannot find MD5 Message Digest Algorithm!", e);
      }
      byte [] digest = md.digest(content);
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02X", b));
      }
      String hash = sb.toString();
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT id FROM assetFile where hash = ?")) {
        qstmt.setString(1, hash);
        ResultSet rs = qstmt.executeQuery();
        if (rs.next()) {        // file already exists, update it modification time
          long id = rs.getLong(1);
          PreparedStatement stmt = conn.prepareStatement("UPDATE assetFile set modifiedDate = CURRENT_TIMESTAMP WHERE id = ?");
          stmt.setLong(1, id);
          stmt.executeUpdate();
        } else {
          // the ON CONFLICT clause is in case we have a race with two processes/threads adding the same asset.
          PreparedStatement stmt = conn.prepareStatement("INSERT INTO assetFile (hash, content) values (?, ?) ON CONFLICT (hash) DO NOTHING");
          stmt.setString(1, hash);
          stmt.setBytes(2, content);
          stmt.executeUpdate();
        }
      }
      try (PreparedStatement ustmt = conn.prepareStatement("UPDATE projectFile SET hash = ? WHERE projectId = ? AND userId = ? AND fileName = ?")) {
        ustmt.setString(1, hash);
        ustmt.setLong(2, projectId);
        ustmt.setLong(3, userId);
        ustmt.setString(4, fileName);
        ret = ustmt.executeUpdate();
        if (ret == 0) {
            throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, userId, projectId, fileName), new RuntimeException("Unknown database error"));
        }
      }
    } else {
      try (PreparedStatement ustmt = conn.prepareStatement("UPDATE projectFile SET content = ?, ts = CURRENT_TIMESTAMP WHERE projectId = ? AND userId = ? AND fileName = ?")) {
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
            createProjectFile(projectId, userId, FileData.RoleEnum.SOURCE, fileName, content, conn);

          } else {
            throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, userId, projectId, fileName), new RuntimeException("Unknown database error"));
          }
        }
      }
    }
    // Update project modified date
    time = updateProjectModifiedDate(projectId, userId, conn);
    return time;
  }

  private byte[] downloadProjectFile(long projectId, String strUserId, @Nonnull String fileName) {
    boolean ok = false;
    byte[] contentBytes = null;

    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      long userId = getUserId(strUserId, conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT hash, content FROM projectFile WHERE projectId = ? AND userId = ? AND fileName = ?")) {
        qstmt.setLong(1, projectId);
        qstmt.setLong(2, userId);
        qstmt.setString(3, fileName);
        ResultSet rs = qstmt.executeQuery();

        if (rs.next()) {
          String hash = rs.getString("hash");
          contentBytes = rs.getBytes("content");
          if (contentBytes == null) {
            if (hash == null) {
              throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, fileName), new FileNotFoundException(fileName));
            }
            PreparedStatement qstmt1 = conn.prepareStatement("SELECT content from assetFile WHERE hash = ?");
            qstmt1.setString(1, hash);
            rs = qstmt1.executeQuery();
            if (rs.next()) {
              contentBytes = rs.getBytes("content");
            }
          }
        }
        ok = true;
      } finally {
        doFinish(conn, ok, "downloadProjectFile");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }

    if (contentBytes == null) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(strUserId, null, projectId, fileName), new FileNotFoundException(fileName));
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
        throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, userId, projectId, null), new RuntimeException("Unknown database error"));
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, userId, projectId, null), e);
    }
    return modifiedTs.getTime();
  }

  @Override
  public long updateProjectBuiltDate(final String strUserId, final long projectId, final long builtDate) {
    // Note: we ignore the builtDate argument. Turns out our caller always sets it to the current
    // date/time, so we just let Postgres deal with it!
    boolean ok = false;
    long retVal = 0;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try {
        long userId = getUserId(strUserId, conn, false);
        retVal = updateProjectBuiltDate(projectId, userId, conn);
        ok = true;
      } finally {
        doFinish(conn, ok, "updateProjectBuiltDate");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
    return retVal;
  }

  private long updateProjectBuiltDate(long projectId, long userId, Connection conn) {
    int ret = 0;
    Timestamp builtTs = null;

    try {
      try (PreparedStatement ustmt = conn.prepareStatement("UPDATE project SET builtDate = CURRENT_TIMESTAMP WHERE id = ? AND userId = ?")) {
        ustmt.setLong(1, projectId);
        ustmt.setLong(2, userId);
        ret = ustmt.executeUpdate();
      }

      if (ret > 0) {              // If timestamp update suceeds
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT builtDate FROM project WHERE id = ? AND userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();

          if (rs.next()) {
            builtTs = rs.getTimestamp("builtDate");
          }
          if (builtTs == null) {             // No such project found
            throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, userId, projectId, null), new RuntimeException("Unknown database error"));
          }
        }
      }

    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, makeErrorMsg(null, userId, projectId, null), e);
    }
    return builtTs.getTime();
  }

  private long getProjectDates(String strUserId, long projectId, String field ) {
    boolean ok = false;
    UserProject proj = null;
    try (Connection conn = this.cpds.getConnection()) {
      try {
        doSetAutoCommit(conn, false);
        long userId = getUserId(strUserId, conn, false);
        try (PreparedStatement qstmt = conn.prepareStatement("SELECT * FROM project WHERE id = ? and userId = ?")) {
          qstmt.setLong(1, projectId);
          qstmt.setLong(2, userId);
          ResultSet rs = qstmt.executeQuery();
          ok = true;            // At this point, we are going to commit no matter
          if (rs.next()) {      // what happens below
            if (field.equals("modified")) {
              Timestamp modifiedDate = rs.getTimestamp("modifiedDate");
              return modifiedDate.getTime();
            } else if (field.equals("built")) {
              Timestamp builtDate = rs.getTimestamp("builtDate");
              if (builtDate == null) {
                return 0;
              } else
                return builtDate.getTime();
            } else {
              return 0;         // XXX
            }
          } else {
            return 0;           // XXX
          }
        }
      } finally {
        doFinish(conn, ok, "getProjectDate");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  private String makeErrorMsg(
    @Nullable final String strUserId,
    @Nullable final Long id,
    @Nullable final Long projectId,
    @Nullable final String fileName) {

    if (strUserId == null && projectId == null && fileName == null) {
      throw new IllegalArgumentException("It's not allowed to set all params to null");
    }

    String userIdToken = strUserId != null ? ("strUserId=\"" + strUserId + "\"") : null;
    String idToken = id != null ? ("id=\"" + id + "\"") : null;
    String projectIdToken = projectId != null ? ("projectId=\"" + projectId + "\"") : null;
    String fileNameIdToken = fileName != null ? ("fileName=\"" + fileName + "\"") : null;

    // No String.join() function in JDK 1.7. We do it manually.
    String ret = null;
    ret = userIdToken != null ? userIdToken : ret;
    ret = idToken != null ? (ret != null ? (ret + ", " + idToken) : idToken) : ret;
    ret = projectIdToken != null ? (ret != null ? (ret + ", " + projectIdToken) : projectIdToken) : ret;
    ret = fileName != null ? (ret != null ? (ret + ", " + fileName) : fileName) : ret;

    return ret;
  }

  private long getUserId(String strUserId, Connection conn, boolean retzeroifnone) {
    try (PreparedStatement qstmt = conn.prepareStatement("SELECT id FROM account WHERE uuid = ?::UUID")) {
      qstmt.setString(1, strUserId);
      ResultSet rs = qstmt.executeQuery();
      if (rs.next()) {
        return rs.getLong("id");
      } else {
        if (retzeroifnone) {
          return 0;
        }
        throw CrashReport.createAndLogError(LOG, null, "No such user " + strUserId, null);
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, String.format("Failed to get userId for %s", strUserId), e);
    }
  }

  private void doFinish(Connection conn, boolean commit, String methodName) {
    try {
      if (commit == true) {
        conn.commit();
      } else {
        conn.rollback();
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, "Could not commit/rollback transaction: Method = " + " methodName", e);
    }
  }

  private void doSetAutoCommit(Connection conn, boolean value) {
    try {
      conn.setAutoCommit(value);
    } catch (SQLException e) {
      doFinish(conn, false, "doSetAutoCommit");
      throw CrashReport.createAndLogError(LOG, null, "Could not set auto commit to " + value, e);
    }
  }

  private String getMisc(String key, String initialValue) {
    boolean ok = false;
    String result = null;
    try (Connection conn = this.cpds.getConnection()) {
      doSetAutoCommit(conn, false);
      try (PreparedStatement qstmt = conn.prepareStatement("SELECT value FROM misc WHERE key = ?")) {
        qstmt.setString(1, key);
        ResultSet rs = qstmt.executeQuery();
        if (rs.next()) {
          result = rs.getString("value");
          ok = true;
          return result;
        }
      } finally {
        doFinish(conn, ok, "getMisc");
      }
      // If we get here, there was no entry for the key,
      // we’ll return the initial value, but first we’ll insert
      // that initialValue as the value of key in the database
      ok = false;
      try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO misc (key, value) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
        stmt.setString(1, key);
        stmt.setString(2, initialValue);
        int ret = stmt.executeUpdate();
        if (ret == 0) {
          throw CrashReport.createAndLogError(LOG, null, "Database Error in getMisc()", new RuntimeException("Unknown database error"));
        }
          ok = true;
        return initialValue;
      } finally {
        doFinish(conn, ok, "getMisc");
      }
    } catch (SQLException e) {
      throw CrashReport.createAndLogError(LOG, null, DATABASE_ERROR, e);
    }
  }

  private boolean isAsset(String fileName) {
    if (fileName.startsWith("assets/")) {
      return true;
    } else {
      return false;
    }
  }
}
