// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.project;

/**
 * Interface for projects that have an Assets folder.
 *
 * @param <T> the type of FolderNode
 *
 * @author markf@google.com (Mark Friedman)
 * @author lizlooney@google.com (Liz Looney)
 */
public interface HasAssetsFolder<T extends FolderNode> {
  T getAssetsFolder();
}
