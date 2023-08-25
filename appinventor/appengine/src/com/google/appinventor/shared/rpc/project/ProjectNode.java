// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Project nodes are used for the structural representation of a project. This
 * is the superclass for all project nodes.
 *
 * <p>The node can directly represent files or folders in the filesystem on
 * the backend, as well as 'virtual' files or folders. An example for a
 * virtual folder is a package node which maps to a series of nested folders.
 *
 * <p>Project nodes will be returned by the backend via RPC to the client
 * in the browser. Therefore they must be serializable. If there are any fields
 * that cannot be serialized, they must be marked as transient.
 *
 */
public abstract class ProjectNode implements Serializable, IsSerializable {

  // For serialization
  private static final long serialVersionUID = -6903337206811923033L;

  // Name of project node (does not need to match the name of the file/folder it represents)
  private String name;

  // ID to identify the file/folder represented by the project node on the backend
  private String fileId;

  // Parent node of this node, {@code null} for the root project node
  private ProjectNode parent;

  // Children of this node (corresponds to files in folder on backend)
  private List<ProjectNode> children;

  /**
   * Default constructor (for serialization only).
   * Unfortunately this will prevent any fields from being marked as final!
   */
  public ProjectNode() {
  }

  /**
   * Creates a new project node.
   *
   * @param name  project node name (can be different from file or folder
   *              represented by this node)
   * @param fileId  ID to identify the file/folder represented by the project
   *                node on the backend (can be {@code null} for 'virtual'
   *                folders)
   */
  public ProjectNode(String name, String fileId) {
    this.name = name;
    this.fileId = fileId;
  }

  /**
   * Adds another project node as a child to this project node.
   *
   * @param child  new child to be added to this node
   */
  public void addChild(ProjectNode child) {
    if (children == null) {
      children = new ArrayList<ProjectNode>();
    }
    children.add(child);
    child.setParent(this);
  }

  /**
   * Removes an existing child node from this project node.
   *
   * @param child  child to be removed from this node
   */
  public void removeChild(ProjectNode child) {
    Preconditions.checkNotNull(children);
    children.remove(child);
  }

  /**
   * Sets a project node to be the parent of this project node.
   *
   * @param node  parent node for this node
   */
  protected void setParent(ProjectNode node) {
    parent = node;
  }

  /**
   * Returns the project node's parent.
   *
   * @return  parent project node ({@code null} for root node)
   */
  public ProjectNode getParent() {
    return parent;
  }

  /**
   * Returns the root project node for this project node. This method can be
   * called on any project node within the project hierarchy.
   *
   * @return  root node of the associated project
   */
  public ProjectRootNode getProjectRoot() {
    return parent.getProjectRoot();
  }

  /**
   * Returns the ID of the project associated with this node. This method can be
   * called on any project node within the project hierarchy.
   *
   * @return  ID of the associated project
   */
  public long getProjectId() {
    return getProjectRoot().getProjectId();
  }

  /**
   * Returns the project type of the nodes in this project (all nodes within
   * a project share the same project type).
   *
   * <p>Note that {@link ProjectRootNode} subclasses need to override this
   * method!
   *
   * @return  type of the associated project
   */
  public String getProjectType() {
    return getProjectRoot().getProjectType();
  }

  /**
   * Returns the name of this project node.
   *
   * @return  name of this node (doesn't have to be the same as the name of
   *          the file/folder represented by this node)
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the full name of this project node.
   *
   * @return  full name of this node
   */
  public String getFullName() {
    // The fileId is the full name of the node.
    return fileId;
  }

  /**
   * Returns the ID for this project node.
   *
   * @return  ID to identify the file/folder represented by the project node
   *          on the backend (can be {@code null} for 'virtual' folders)
   */
  public String getFileId() {
    return fileId;
  }

  /**
   * Returns an iterable for the children of this node.
   *
   * @return iterable
   */
  public Iterable<ProjectNode> getChildren() {
    List<ProjectNode> result = children;
    if (result == null) {
      result = Collections.emptyList();
    }
    return result;
  }

  /**
   * Indicates whether a node is a source node.
   *
   * @return  {@code true} for source nodes, {@code false} otherwise
   */
  protected boolean isSourceNode() {
    return false;
  }

  /**
   * Recursively looks for a project node with the given file ID.
   *
   * @param fileId  file ID to look for
   * @return  found project node or {@code null}
   */
   public ProjectNode findNode(String fileId) {
    if (fileId.equals(getFileId())) {
      return this;
    }

    if (children != null) {
      for (ProjectNode child : children) {
        ProjectNode found = child.findNode(fileId);
        if (found != null) {
          return found;
        }
      }
    }

    return null;
  }

  /**
   * Recursively looks for a project node with the given type.
   *
   * @param type  class of node to look for
   * @return  found project node or {@code null}
   */
   public ProjectNode findNode(Class<?> type) {
     if (getClass().equals(type)) {
       return this;
     }

     if (children != null) {
       for (ProjectNode child : children) {
         ProjectNode found = child.findNode(type);
         if (found != null) {
           return found;
         }
       }
     }

     return null;
   }

  /**
   * Recursively collects all source nodes.
   *
   * @param bucket  container to collect found source nodes in
   */
  protected void findSourceNodes(List<ProjectNode> bucket) {
    if (children != null) {
      for (ProjectNode child : children) {
        if (child.isSourceNode()) {
          bucket.add(child);
        }
        child.findSourceNodes(bucket);
      }
    }
  }

  /**
   * Used to rename a node.
   *
   * <p>Note that this will not cause a rename in the storage system!
   *
   * @param newName  new name
   */
  public void setName(String newName) {
    name = newName;
  }
}
