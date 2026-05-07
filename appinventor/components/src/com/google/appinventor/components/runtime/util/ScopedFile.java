// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.Form;
import java.io.File;
import java.net.URI;

public class ScopedFile {
  private final FileScope scope;
  private final String fileName;

  public ScopedFile(FileScope scope, String fileName) {
    if (fileName.startsWith("//")) {
      // Promote files beginning with "//" to Asset scope
      scope = FileScope.Asset;
      fileName = fileName.substring(2);
    } else if (!fileName.startsWith("/") && scope == FileScope.Legacy) {
      // Legacy file access without a starting slash went to the app's private directory
      scope = FileScope.Private;
    } else if (fileName.startsWith("/") && scope != FileScope.Legacy) {
      fileName = fileName.substring(1);
    }
    this.scope = scope;
    this.fileName = fileName;
  }

  public FileScope getScope() {
    return scope;
  }

  public String getFileName() {
    return fileName;
  }

  @Override
  public int hashCode() {
    return scope.hashCode() * 37 + fileName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ScopedFile) {
      ScopedFile other = (ScopedFile) obj;
      if (this.scope != other.scope) {
        return false;
      }
      if (fileName == null && other.fileName == null) {
        return true;
      } else if (fileName == null || other.fileName == null) {
        return false;
      } else {
        return fileName.equals(other.fileName);
      }
    } else {
      return false;
    }
  }

  public File resolve(Form form) {
    return new File(URI.create(FileUtil.resolveFileName(form, fileName, scope)));
  }

  @Override
  public String toString() {
    return "ScopedFile{"
        + "scope=" + scope
        + ", fileName='" + fileName + '\''
        + '}';
  }
}
