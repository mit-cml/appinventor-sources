// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc;

/**
 * Class representing the response from an upload request.
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author kerr@google.com (Debby Wallach)
 */
public class UploadResponse {

  public enum Status {
    SUCCESS,
    NOT_PROJECT_ARCHIVE,
    FILE_TOO_LARGE,
    NOT_INVITE_CSV,
    NOT_AN_INVITER,
    IO_EXCEPTION;
  }

  private final Status status;
  private int count;
  private String info;
  private long modificationDate = 0;

  private static final String DELIM = "#DELIM#";

  // Because Chrome extensions can insert extra html tags into the response from the
  // UploadServlet, we need begin and end delimiters to bracket the UploadResponse.
  // Note - Internet Explorer sometimes changes <pre> to <PRE> and </pre> to </PRE>. It sometimes
  // changes <PRE> to <pre> and </PRE> to </pre>, which I find hysterical!
  // To work around this crazy browser behavior, we don't put <pre> and </pre> tags in these
  // constants, which are used for parsing in extractUploadResponse(). However, the <pre> and
  // </pre> tags are still used in formatAsHtml().
  private static final String BEGIN = "[UPLOAD RESPONSE BEGIN]";
  private static final String END = "[UPLOAD RESPONSE END]";

  public UploadResponse(Status status) {
    this(status, 0, "");
  }

  public UploadResponse(Status status, int count, String info) {
    this.status = status;
    this.count = count;
    this.info = info;
  }

  public UploadResponse(Status status, long modificationDate) {
    this.status = status;
    this.count = 0;
    this.info = "";
    this.modificationDate = modificationDate;
  }

  public Status getStatus() {
    return status;
  }

  public int getCount() {
    return count;
  }

  public String getInfo() {
    return info;
  }

  public long getModificationDate() {
    return modificationDate;
  }

  @Override
  public String toString() {
    // If another field is inserted here, don't forget to update valueOf below.
    return status.toString() + DELIM +
        count + DELIM +
        modificationDate + DELIM +
        // Since info is a string and can contain more delimiters, it must be last.
        // If you need to add more parts, please add them before info.
        info;
  }

  public static UploadResponse valueOf(String text) {
    // There are 4 parts: status, count, modificationDate, and info, separated by delimiter.
    // Since info is a string and can contain more delimiters, it must be last.
    int maxParts = 4; // If you modify this value, please update the comment above.
    // We pass maxParts to split so that the last element in the parts array will contain the rest
    // of the text.
    String[] parts = text.split(DELIM, maxParts);
    UploadResponse uploadResponse = new UploadResponse(Status.valueOf(parts[0]));
    if (parts.length > 1) {
      uploadResponse.count = Integer.parseInt(parts[1]);
    }
    if (parts.length > 2) {
      uploadResponse.modificationDate = Long.parseLong(parts[2]);
    }

    // Remember, since the info may contain more delimiters, it must be last.
    // If you need to add more parts, please add them before info.

    int infoIndex = maxParts - 1; // info must be last!
    if (parts.length > infoIndex) {
      uploadResponse.info = parts[infoIndex];
    }
    return uploadResponse;
  }

  /**
   * Formats this UploadResponse so it can be written to the UploadServlet
   * response.
   */
  public String formatAsHtml() {
    return "<pre>" + BEGIN + toString() + END + "</pre>";
  }

  /**
   * Extracts the UploadResponse from the results of a FormSubmitCompleteEvent.
   *
   * @param results the results of a FormSubmitCompleteEvent
   * @return an UploadResponse, or null if the results could not be parsed
   */
  public static UploadResponse extractUploadResponse(String results) {
    int beginIndex = results.indexOf(BEGIN);
    if (beginIndex == -1) {
      return null;
    }
    beginIndex += BEGIN.length();
    int endIndex = results.indexOf(END, beginIndex);
    if (endIndex == -1) {
      return null;
    }
    String s = results.substring(beginIndex, endIndex);
    try {
      return valueOf(s);
    } catch (RuntimeException e) {
      return null;
    }
  }
}
