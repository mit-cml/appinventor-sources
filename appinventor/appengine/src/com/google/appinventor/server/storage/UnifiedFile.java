package com.google.appinventor.server.storage;

public final class UnifiedFile {
  private final String fileName;
  private final byte[] content;
  private String filesystemName = null;

  UnifiedFile(String fileName, byte[] content) {
    this.fileName = fileName;
    this.content = content;
  }

  public String getFileName() {
    return fileName;
  }

  public byte[] getContent() {
    return content;
  }

  public String getFilesystemName() {
    return filesystemName;
  }

  public void setFilesystemName(String filesystemName) {
    this.filesystemName = filesystemName;
  }
}
