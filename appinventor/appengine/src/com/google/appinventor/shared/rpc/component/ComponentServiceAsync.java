// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

import com.google.appinventor.shared.rpc.project.ProjectNode;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface ComponentServiceAsync {

  /**
   * @see ComponentService#getComponentInfos()
   */
  void getComponentInfos(AsyncCallback<List<ComponentInfo>> callback);

  /**
   * @see ComponentService#importComponentToProject(ComponentInfo, long, String)
   */
  void importComponentToProject(ComponentInfo info, long projectId, String folderPath, AsyncCallback<List<ProjectNode>> callback);

}
