package com.google.appinventor.server.storage.database;

public class DatabaseAccessException extends Exception {
  public DatabaseAccessException(Exception parent) {
    super(parent);
  }
}
