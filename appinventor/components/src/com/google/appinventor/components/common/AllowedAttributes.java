// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.Arrays;
import java.util.List;

public class AllowedAttributes {
  private AllowedAttributes() {
    // nothing
  }

  // Returns allowed application attributes
  public static final List<String> getAllowedApplicationAttributes() {
    return Arrays.asList("allowTaskReparenting", "allowBackup", "allowClearUserData", "allowNativeHeapPointerTagging",
        "appCategory", "backupAgent", "backupInForeground", "banner", "dataExtractionRules", "description", "enabled",
        "enableOnBackInvokedCallback", "extractNativeLibs", "fullBackupContent", "fullBackupOnly", "gwpAsanMode",
        "hasCode", "hasFragileUserData", "hardwareAccelerated", "isGame", "isMonitoringTool", "killAfterRestore",
        "largeHeap", "logo", "manageSpaceActivity", "permission", "persistent", "process", "restoreAnyVersion",
        "requiredAccountType", "resizeableActivity", "restrictedAccountType", "supportsRtl", "taskAffinity",
        "testOnly", "uiOptions", "usesCleartextTraffic", "vmSafeMode");
  }
}
