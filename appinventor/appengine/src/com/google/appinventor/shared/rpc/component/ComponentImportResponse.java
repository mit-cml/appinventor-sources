// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Component Import Response to Send Failure Reasons
 */
public class ComponentImportResponse implements IsSerializable{

  public enum Status {
    IMPORTED,
    UPGRADED, //when the old component files were replaced with newer ones
    NOT_GCS,
    UNKNOWN_URL,
    FAILED
  }

  private Status status;
  private long projectId; // necessary to ensure right project
  private String componentType;  // Type of Component
  private List<ProjectNode> nodes; // Added Nodes

  public ComponentImportResponse(Status status, long projectId, String componentType, List<ProjectNode> nodes) {
    this.status = status;
    this.projectId = projectId;
    this.componentType = componentType;
    this.nodes = nodes;
  }

  public ComponentImportResponse(Status status) {
    this(status, 0, "", null);
  }

  private ComponentImportResponse() {
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public long getProjectId() {
    return projectId;
  }

  public void setProjectId(long projectId){
    this.projectId = projectId;
  }

  public String getComponentType() {
    return componentType;
  }

  public void setComponentType(String componentType) {
    this.componentType = componentType;
  }

  public List<ProjectNode> getNodes() {
    if (nodes == null) {
      nodes = new ArrayList<ProjectNode>();
    }
    return nodes;
  }

  public void setNodes(List<ProjectNode> nodes) {
    this.nodes = nodes;
  }
}
