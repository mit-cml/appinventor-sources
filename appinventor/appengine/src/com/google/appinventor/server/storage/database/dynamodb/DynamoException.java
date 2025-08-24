package com.google.appinventor.server.storage.database.dynamodb;

final class DynamoException extends Exception {

  public DynamoException(Exception e) {
    super();
    this.initCause(e);
  }

  public DynamoException(String msg) {
    super(msg);
  }
}
