// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.google.appinventor.buildserver.Compiler;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.context.IosPaths;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.tasks.common.BuildFactory;

public class IosBuildFactory extends BuildFactory<IosPaths, IosCompilerContext> {
  private final boolean forAppStore;

  protected IosBuildFactory(boolean forAppStore) {
    super(forAppStore ? "asc" : "ipa");
    this.forAppStore = forAppStore;
  }

  @Override
  public String getExtension() {
    return "ipa";  // App Store also uses IPA
  }

  @Override
  public Class<IosCompilerContext> getContextClass() {
    return IosCompilerContext.class;
  }

  @Override
  protected void prepareBuild(Compiler<IosPaths, IosCompilerContext> compiler) {
    super.prepareBuild(compiler);
    if (forAppStore) {
      compiler.add(ValidateAppStoreCredentials.class);
    }
    compiler.add(ExtractPlayerApp.class);
    compiler.add(ExtractProvisioningPlist.class);
  }

  @Override
  protected void prepareAppIcon(Compiler<IosPaths, IosCompilerContext> compiler) {
    compiler.add(PrepareAppIcon.class);
  }

  @Override
  protected void prepareMetadata(Compiler<IosPaths, IosCompilerContext> compiler) {
    compiler.add(CreateEntitlements.class);
    compiler.add(CreateInfoPlist.class);
  }

  @Override
  protected void compileSources(Compiler<IosPaths, IosCompilerContext> compiler) {
    compiler.add(CompileLaunchScreen.class);
    compiler.add(BuildAia.class);
    compiler.add(LinkLibraries.class);
  }

  @Override
  protected void signApp(Compiler<IosPaths, IosCompilerContext> compiler) {
    compiler.add(SignPackage.class);
  }

  @Override
  protected void createOutputBundle(Compiler<IosPaths, IosCompilerContext> compiler) {
    compiler.add(CreateIpa.class);
    if (!forAppStore) {
      // Create a manifest file and other resources for QR code
      compiler.add(CreateManifest.class);
    } else {
      compiler.add(UploadPackage.class);
    }
  }

  @SuppressWarnings("unused")
  public static void install() {
    register(BuildType.IPA_EXTENSION, new IosBuildFactory(false));
    register(BuildType.ASC_EXTENSION, new IosBuildFactory(true));
  }
}
