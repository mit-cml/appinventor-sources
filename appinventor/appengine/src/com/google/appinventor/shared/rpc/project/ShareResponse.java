package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ShareResponse implements IsSerializable {

  public enum Status {
    /**
     * The SHARED status indicates that a project was successfully shared on the server.
     */
    SHARED,

    /**
     * The SELF_SHARE status indicates an error where the user attempts to share a project with
     * themselves.
     */
    SELF_SHARE,

    /**
     * The UNKNOWN_USER status indicates an error where the provided email address does not
     * identify a user in the system.
     */
    UNKNOWN_USER,

    /**
     * The ALREADY_SHARED status indicates an error where the project has already been shared with
     * the user identified by the provided email address.
     */
    ALREADY_SHARED,

    /**
     * The user ID provided in the RPC is invalid.
     */
    INVALID_USER,

    /**
     * The user is not authorized to share the project.
     */
    UNAUTHORIZED
  }

  private Status status;
  private long projectId;
  private String email;

  public ShareResponse(Status status, long projectId, String email) {
    this.status = status;
    this.projectId = projectId;
    this.email = email;
  }

  /**
   * Convenience constructor when returning an error status.
   * @param status The failure status code for the sharing operation.
   */
  public ShareResponse(Status status) {
    this.status = status;
  }

  // Protected constructor for GWT RPC
  protected ShareResponse() {
  }

  public Status getStatus() {
    return status;
  }

  public long getProjectId() {
    return projectId;
  }

  public String getEmail() {
    return email;
  }
}