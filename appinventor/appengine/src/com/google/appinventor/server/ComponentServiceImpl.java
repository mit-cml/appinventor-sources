// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.shared.rpc.component.ComponentInfo;
import com.google.appinventor.shared.rpc.component.ComponentService;

import java.util.ArrayList;
import java.util.List;

public class ComponentServiceImpl extends OdeRemoteServiceServlet implements ComponentService {
  @Override
  public List<ComponentInfo> getComponentInfos() {
    ArrayList<ComponentInfo> infos = new ArrayList<ComponentInfo>();
    infos.add(new ComponentInfo("comp0", 0));
    infos.add(new ComponentInfo("comp0", 3));
    infos.add(new ComponentInfo("comp0", 7));
    infos.add(new ComponentInfo("comp1", 0));
    infos.add(new ComponentInfo("comp2", 2));
    infos.add(new ComponentInfo("comp2", 4));

    return infos;
  }
}
