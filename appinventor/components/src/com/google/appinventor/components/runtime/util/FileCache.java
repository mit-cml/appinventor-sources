package com.google.appinventor.components.runtime.util;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class FileCache {
  public File cacheDir;
  HashMap<String, CompletableFuture<Void>> fileMap = new HashMap<>();

  public FileCache(File cacheDir) {
    this.cacheDir = cacheDir;
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
  }

  public CompletableFuture<Void> registerFile(final String path, final String url) {
    final File file = new File(cacheDir, path);
    if (!file.exists()) {
      CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
        @Override
        public void run() {
          try {
            FileUtil.downloadUrlToFile(url, file.getAbsolutePath());
            fileMap.remove(path);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
      fileMap.put(path, future);
      return future;
    }
    return CompletableFuture.completedFuture(null);
  }

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

  private void deleteFolder(File folder) {
    if (folder.isDirectory()) {
      for (File file : folder.listFiles()) {
        deleteFolder(file);
      }
    }
    folder.delete();
  }

  public void resetCache() {
    if (cacheDir.exists()) {
      deleteFolder(cacheDir);
    }
    fileMap.clear();
  }
}
