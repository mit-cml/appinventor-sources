// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.util.HashDbInitialize.HashTable;
import android.database.sqlite.SQLiteDatabase;

public class FileCache {
  private final File cacheDir;
  private final HashDatabase hashDatabase;
  private final HashMap<String, FutureTask<Void>> fileMap = new HashMap<>();
  private static final Logger LOG = Logger.getLogger(FileCache.class.getName());

  /**
   * Creates a new FileCache instance with the specified cache directory. If the directory doesn't
   * exist, it will be created.
   *
   * @param cacheDir the directory to use for caching files
   */
  public FileCache(Form form) {
    this.cacheDir = new File(form.getCacheDir(), "file_cache");
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
    this.hashDatabase = new HashDatabase(form);
  }

  /**
   * Returns the cache directory.
   * 
   * @return The cache directory
   */
  public File getCacheDir() {
    return cacheDir;
  }

  /**
   * Registers a file for download from the specified URL to the cache. If the file doesn't exist in
   * the cache, it will be downloaded asynchronously.
   * 
   * @param path the relative path within the cache directory where the file formshould be stored
   * @param url the URL from which to download the file
   * @return a CompletableFuture that completes when the download is finished, or immediately if the
   *         file already exists
   */
  public Future<Void> registerFile(final String path, final String url) {
    synchronized (fileMap) {
      // If currently downloading, send that back
      if (fileMap.containsKey(path)) {
        return fileMap.get(path);
      }

      FutureTask<Void> task = new FutureTask<>(new Runnable() {
        @Override
        public void run() {
          final File file = new File(cacheDir, path);
          HttpURLConnection connection = null;
          try {
            HashFile hashFile = hashDatabase.getHashFile(path);

            // If there is no hash file, but the file still exists, there probably just wasn't an
            // ETAG, so we should just use the cache.
            if (hashFile != null || !file.exists()) {
              connection = (HttpURLConnection) new URL(url).openConnection();

              if (hashFile != null && file.exists()) {
                connection.addRequestProperty("If-None-Match", hashFile.getHash());
              }
              connection.setRequestMethod("GET");

              int responseCode = connection.getResponseCode();
              String fileHash = connection.getHeaderField("ETag");

              if (responseCode == 200) {
                BufferedInputStream in =
                    new BufferedInputStream(connection.getInputStream(), 0x1000);
                BufferedOutputStream out =
                    new BufferedOutputStream(new FileOutputStream(file), 0x1000);

                try {
                  while (true) {
                    int b = in.read();
                    if (b == -1) {
                      break;
                    }
                    out.write(b);
                  }
                  out.flush();
                } catch (IOException e) {
                  throw new IOException("Error downloading file: " + url, e);
                } finally {
                  out.close();
                }

                if (fileHash != null) {
                  Date timeStamp = new Date();
                  HashFile newHashFile = new HashFile(path, fileHash, timeStamp);
                  if (hashFile == null) {
                    hashDatabase.insertHashFile(newHashFile);
                  } else {
                    hashDatabase.updateHashFile(newHashFile);
                  }
                }
              }
            }
          } catch (Exception error) {
            LOG.log(Level.SEVERE, "Exception downloading file to cache", error);
          } finally {
            synchronized (fileMap) {
              if (connection != null) {
                connection.disconnect();
              }
              fileMap.remove(path);
            }
          }
        }
      }, null);
      fileMap.put(path, task);
      new Thread(task).start();
      return task;
    }
  }

  /**
   * Retrieves a file from the cache at the specified path. If the file is currently being
   * downloaded, this method will wait for the download to complete.
   * 
   * @param path the relative path of the file within the cache directory
   * @return a CompletableFuture containing the File if it exists and is ready, or a failed future
   *         with an exception if the file doesn't exist or download failed
   */
  public Future<File> getFile(String path) {
    File file = new File(cacheDir, path);
    synchronized (fileMap) {
      if (!file.exists()) {
        return new FutureTask<>(() -> {
          throw new Exception("File does not exist: " + path);
        });
      } else if (fileMap.containsKey(path)) {
        FutureTask<Void> task = fileMap.get(path);
        FutureTask<File> fileTask = new FutureTask<>(() -> {
          try {
            task.get();
            return new File(cacheDir, path);
          } catch (Exception e) {
            throw e;
          }
        });
        new Thread(fileTask).start();
        return fileTask;
      } else {
        return new FutureTask<>(() -> file);
      }
    }
  }

  /**
   * Resets the cache by deleting all cached files and clearing the internal file map. This will
   * remove the entire cache directory and recreate it as an empty directory.
   */
  public void resetCache() {
    if (cacheDir.exists()) {
      try {
        FileUtil.removeDirectory(cacheDir, true);

        SQLiteDatabase db = hashDatabase.getWritableDatabase();
        db.delete(HashTable.TABLE_NAME, null, null);
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "Error resetting cache", e);
      }
    }
    cacheDir.mkdirs();
    synchronized (fileMap) {
      fileMap.clear();
    }
  }
}
