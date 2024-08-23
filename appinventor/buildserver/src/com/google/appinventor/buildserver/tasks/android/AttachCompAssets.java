// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.YoungAndroidConstants;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.ExecutorUtils;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;


/**
 * compiler.attachCompAssets()
 */

@BuildType(apk = true, aab = true)
public class AttachCompAssets implements AndroidTask {
  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    try {
      // Gather non-library assets to be added to apk's Asset directory.
      // The assets directory have been created before this.
      File mergedAssetDir = ExecutorUtils.createDir(context.getProject().getBuildDirectory(),
          YoungAndroidConstants.ASSET_DIR_NAME);
      
      // Copy component/extension assets to build/assets
      copyAssets(context.getComponentInfo().getAssetsNeeded(), context, mergedAssetDir);

      // If isCompanion is false, we include the deferrableAssets in the APK/AAB package
      // so they don't need to be downloaded when the app starts.
      // If isCompanion is true, these assets are not included and will be downloaded as needed
      if (!context.isForCompanion()) {
          copyAssets(context.getComponentInfo().getDeferrableAssetsNeeded(), context, mergedAssetDir);
      }


      // Copy project assets to build/assets
      File[] assets = context.getProject().getAssetsDirectory().listFiles();
      if (assets != null) {
        for (File asset : assets) {
          if (asset.isFile()) {
            Files.copy(asset, new File(mergedAssetDir, asset.getName()));
          }
        }
      }
    } catch (IOException e) {
      context.getReporter().error("There was an unknown error while processing assets", true);
      return TaskResult.generateError(e);
    }

    return TaskResult.generateSuccess();
  }

  private void copyAssets(ConcurrentMap<String, Set<String>> assetsMap, AndroidCompilerContext context, File targetDir) throws IOException {
    for (String type : assetsMap.keySet()) {
        for (String assetName : assetsMap.get(type)) {
            String sourcePath;
            if (context.getSimpleCompTypes().contains(type)) {
                String pathSuffix = context.getResources().getRuntimeFilesDir() + assetName;
                sourcePath = context.getResource(pathSuffix);
            } else if (context.getExtCompTypes().contains(type)) {
                final String extCompDir = ExecutorUtils.getExtCompDirPath(type, context.getProject(), context.getExtTypePathCache());
                sourcePath = extCompDir + File.separator + YoungAndroidConstants.ASSET_DIR_NAME + File.separator + assetName;
                targetDir = ExecutorUtils.createDir(targetDir, new File(extCompDir).getName());
            } else {
                context.getReporter().error("There was an unexpected error while processing assets", true);
                throw new IOException("Unknown asset type");
            }
            Files.copy(new File(sourcePath), new File(targetDir, assetName));
        }
    }
}

}
