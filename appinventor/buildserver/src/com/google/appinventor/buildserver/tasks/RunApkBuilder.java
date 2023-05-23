package com.google.appinventor.buildserver.tasks;

import com.android.sdklib.build.ApkBuilder;
import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.CompilerContext;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.interfaces.Task;

import java.io.File;

/**
 * compiler.runApkBuilder
 */
@BuildType(apk = true)
public class RunApkBuilder implements Task {
  @Override
  public TaskResult execute(CompilerContext context) {
    try {
      ApkBuilder apkBuilder = new ApkBuilder(
          context.getPaths().getDeployFile().getAbsolutePath(),
          context.getPaths().getTmpPackageName().getAbsolutePath(),
          context.getPaths().getTmpDir().getAbsolutePath() + File.separator + "classes.dex",
          null,
          context.getReporter().getSystemOut()
      );
      if (context.getResources().getDexFiles().size() > 1) {
        for (File f : context.getResources().getDexFiles()) {
          if (!f.getName().equals("classes.dex")) {
            apkBuilder.addFile(f, f.getName());
          }
        }
      }
      if (context.getComponentInfo().getNativeLibsNeeded().size() != 0) { // Need to add native libraries...
        apkBuilder.addNativeLibraries(context.getPaths().getLibsDir());
      }
      apkBuilder.sealApk();
    } catch (Exception e) {
      return TaskResult.generateError(e);
    }
    return TaskResult.generateSuccess();
  }
}
