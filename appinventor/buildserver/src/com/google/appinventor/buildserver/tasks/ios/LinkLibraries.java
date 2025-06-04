// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2023 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@BuildType(ipa = true, asc = true)
public class LinkLibraries implements IosTask {
  public static final Map<String, String> LIB_MAP = new HashMap<>();
  public static final List<String> BASE_LIBS = Collections.unmodifiableList(Arrays.asList(
      "Foundation",
      "UIKit",
      "QuartzCore",
      "AlamoFire"
  ));
  public static final Map<String, String[]> COMPONENT_MAP = new HashMap<>();

  private static void register(String component, String... frameworks) {
    COMPONENT_MAP.put(component, frameworks);
  }

  static {
    LIB_MAP.put("osmdroid.jar", "MapKit");
    LIB_MAP.put("jts.jar", "geos");
    register("LocationSensor", "CoreLocation");
    register("Map", "CoreLocation", "AlamoFire", "GEOSwift", "MapKit");
  }

  @Override
  public TaskResult execute(IosCompilerContext context) {
    return TaskResult.generateSuccess();
  }
}
