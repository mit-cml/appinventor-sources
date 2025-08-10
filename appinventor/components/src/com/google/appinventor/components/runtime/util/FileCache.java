package com.google.appinventor.components.runtime.util;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileCache {
  public File cacheDir;
  private final HashMap<String, CompletableFuture<Void>> fileMap = new HashMap<>();
  private static final Logger LOG = Logger.getLogger(FileCache.class.getName());

  /**
   * Creates a new FileCache instance with the specified cache directory. If the directory doesn't
   * exist, it will be created.
   *
   * @param cacheDir the directory to use for caching files
   */
  public FileCache(File cacheDir) {
    this.cacheDir = cacheDir;
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
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
  public CompletableFuture<Void> registerFile(final String path, final String url) {
    final File file = new File(cacheDir, path);
    if (!file.exists()) {
      CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
        @Override
        public void run() {
          try {
            FileUtil.downloadUrlToFile(url, file.getAbsolutePath());
            fileMap.remove(path);
          } catch (Exception error) {
            LOG.log(Level.SEVERE, "Exception downloading file to cache", error);
          }
        }
      });
      fileMap.put(path, future);
      return future;
    }
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Retrieves a file from the cache at the specified path. If the file is currently being
   * downloaded, this method will wait for the download to complete.
   * 
   * @param path the relative path of the file within the cache directory
   * @return a CompletableFuture containing the File if it exists and is ready, or a failed future
   *         with an exception if the file doesn't exist or download failed
   */
  public CompletableFuture<File> getFile(String path) {
    File file = new File(cacheDir, path);
    if (!file.exists()) {
      return CompletableFuture.failedFuture(new Exception("File does not exist: " + path));
    } else if (fileMap.containsKey(path)) {
      try {
        fileMap.get(path).get();
      } catch (Exception e) {
        return CompletableFuture.failedFuture(e);
      }
      return CompletableFuture.completedFuture(file);
    } else {
      return CompletableFuture.completedFuture(file);
    }
  }

  /**
   * Recursively deletes a folder and all its contents. This is a helper method used by
   * resetCache().
   * 
   * @param folder the folder to delete
   */
  private void deleteFolder(File folder) {
    if (folder.isDirectory()) {
      for (File file : folder.listFiles()) {
        deleteFolder(file);
      }
    }
    folder.delete();
  }

  /**
   * Resets the cache by deleting all cached files and clearing the internal file map. This will
   * remove the entire cache directory and recreate it as an empty directory.
   */
  public void resetCache() {
    if (cacheDir.exists()) {
      deleteFolder(cacheDir);
    }
    fileMap.clear();
  }
}
