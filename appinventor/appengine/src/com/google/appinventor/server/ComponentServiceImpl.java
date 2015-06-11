// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.shared.rpc.component.ComponentRecord;
import com.google.appinventor.shared.rpc.component.ComponentService;

import java.util.ArrayList;
import java.util.List;

public class ComponentServiceImpl extends OdeRemoteServiceServlet implements ComponentService {
  @Override
  public List<ComponentRecord> getComponentRecords() {
    ArrayList<ComponentRecord> records = new ArrayList<ComponentRecord>();
    records.add(new ComponentRecord("comp0", 0));
    records.add(new ComponentRecord("comp0", 3));
    records.add(new ComponentRecord("comp0", 7));
    records.add(new ComponentRecord("comp1", 0));
    records.add(new ComponentRecord("comp2", 2));
    records.add(new ComponentRecord("comp2", 4));

    return records;
  }
}
