// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;


public class HashFile {

  private String fileName;
  private String hash;
  private String timestamp;

  public HashFile() {
  }

  /**
   * Constructors for initialize HashFile object to be stored in the database
   */
  public HashFile(String fileName, String hash, Date time) {
    this.fileName = fileName;
    this.hash = hash;
    this.timestamp = formatTimestamp(time);
  }

  /**
   * Constructors for initialize HashFile object which we get from the database
   */
  public HashFile(String fileName, String hash, String timestamp) {
    this.fileName = fileName;
    this.hash = hash;
    this.timestamp = timestamp;
  }

  /**
   * Getter for file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Setter for file name
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Getter for hash
   */
  public String getHash() {
    return hash;
  }

  /**
   * Setter for has
   */
  public void setHash(String hash) {
    this.hash = hash;
  }

  /**
   * Getter for time stamp
   */
  public String getTimestamp() {
    return timestamp;
  }

  /**
   * Setter for time stamp (to be stored in database)
   */
  public void setTimestampInDb(Date time) {
    this.timestamp = formatTimestamp(time);
  }

  /**
   * Setter for time stamp (get from database)
   */
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Convert Date to String
   */
  public String formatTimestamp(Date timestamp) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    return dateFormat.format(timestamp);
  }
}
