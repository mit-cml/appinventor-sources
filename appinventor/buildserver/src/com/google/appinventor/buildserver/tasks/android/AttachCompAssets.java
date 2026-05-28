// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.ASSETS_FOLDER;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.util.ExecutorUtils;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * compiler.attachCompAssets()
 */

@BuildType(apk = true, aab = true)
public class AttachCompAssets implements AndroidTask {

  private static final String I18N_ASSET_DIRECTORY = "i18n";
  private static final String I18N_TRANSLATIONS_FILE = "translations.json";

  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    try {
      // Gather non-library assets to be added to apk's Asset directory.
      // The assets directory have been created before this.
      File mergedAssetDir = ExecutorUtils.createDir(context.getProject().getBuildDirectory(),
          ASSETS_FOLDER);

      // Copy component/extension assets to build/assets
      for (String type : context.getComponentInfo().getAssetsNeeded().keySet()) {
        for (String assetName : context.getComponentInfo().getAssetsNeeded().get(type)) {
          File targetDir = mergedAssetDir;
          String sourcePath;

          if (context.getSimpleCompTypes().contains(type)) {
            String pathSuffix = context.getResources().getRuntimeFilesDir() + assetName;
            sourcePath = context.getResource(pathSuffix);
          } else if (context.getExtCompTypes().contains(type)) {
            final String extCompDir = ExecutorUtils.getExtCompDirPath(type, context.getProject(),
                context.getExtTypePathCache());
            sourcePath = extCompDir + File.separator + ASSETS_FOLDER + File.separator + assetName;
            // If targetDir's location is changed here, you must update Form.java in components to
            // reference the new location. The path for assets in compiled apps is assumed to be
            // assets/EXTERNAL-COMP-PACKAGE/ASSET-NAME
            targetDir = ExecutorUtils.createDir(targetDir, new File(extCompDir).getName());
          } else {
            context.getReporter().error(
                "There was an unexpected error while processing assets", true);
            return TaskResult.generateError("Unknown asset type");
          }

          Files.copy(new File(sourcePath), new File(targetDir, assetName));
        }
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
      writeI18nTranslationsAsset(context, mergedAssetDir);
    } catch (IOException e) {
      context.getReporter().error("There was an unknown error while processing assets", true);
      return TaskResult.generateError(e);
    }

    return TaskResult.generateSuccess();
  }

  private void writeI18nTranslationsAsset(AndroidCompilerContext context, File mergedAssetDir)
    throws IOException {
    String translationsJson = context.getProject().getI18nTranslations();

    context.getReporter().info("i18n translations length = "
        + (translationsJson == null ? "null" : translationsJson.length()));

    if (translationsJson == null || translationsJson.trim().length() == 0) {
      return;
    }

    File i18nDir = ExecutorUtils.createDir(mergedAssetDir, I18N_ASSET_DIRECTORY);
    File translationsFile = new File(i18nDir, I18N_TRANSLATIONS_FILE);

    context.getReporter().info("Writing i18n translations asset to "
        + translationsFile.getAbsolutePath());

    Files.write(translationsJson.getBytes(StandardCharsets.UTF_8), translationsFile);
  }
}
