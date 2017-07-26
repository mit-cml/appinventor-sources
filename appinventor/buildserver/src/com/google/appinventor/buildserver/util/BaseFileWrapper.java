// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import java.io.InputStream;
import java.io.OutputStream;

import com.android.io.IAbstractFile;
import com.android.io.IAbstractFolder;
import com.android.io.StreamException;

/**
 * BaseFileWrapper provides a stub implementation of Android's IAbstractFile interface.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public abstract class BaseFileWrapper implements IAbstractFile {

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getOsLocation() {
    return null;
  }

  @Override
  public boolean exists() {
    return false;
  }

  @Override
  public IAbstractFolder getParentFolder() {
    return null;
  }

  @Override
  public boolean delete() {
    return false;
  }

  @Override
  public InputStream getContents() throws StreamException {
    return null;
  }

  @Override
  public void setContents(InputStream source) throws StreamException {
  }

  @Override
  public OutputStream getOutputStream() throws StreamException {
    return null;
  }

  @Override
  public PreferredWriteMode getPreferredWriteMode() {
    return null;
  }

  @Override
  public long getModificationStamp() {
    return 0;
  }

}
