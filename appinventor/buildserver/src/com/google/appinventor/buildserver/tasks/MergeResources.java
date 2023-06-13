package com.google.appinventor.buildserver.tasks;

import com.android.ide.common.internal.AaptCruncher;
import com.android.ide.common.internal.PngCruncher;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.CompilerContext;
import com.google.appinventor.buildserver.ExecutorUtils;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.interfaces.Task;

import java.io.File;


/**
 * compiler.mergeResources()
 */
@BuildType(apk = true, aab = true)
public class MergeResources implements Task {
  @Override
  public TaskResult execute(CompilerContext context) {
    // these should exist from earlier build steps
    File intermediates = ExecutorUtils.createDir(context.getPaths().getBuildDir(), "intermediates");
    File resDir = ExecutorUtils.createDir(intermediates, "res");
    context.getPaths().setMergedResDir(ExecutorUtils.createDir(resDir, "merged"));
    context.getPaths().setTmpPackageName(new File(
        context.getPaths().getDeployDir().getAbsolutePath() + File.separator
            + context.getProject().getProjectName() + "._ap"));

    PngCruncher cruncher = new AaptCruncher(context.getResources().aapt(), null, null);
    if (!context.getComponentInfo().getExplodedAarLibs().mergeResources(
        context.getPaths().getMergedResDir(), context.getPaths().getResDir(), cruncher)) {
      return TaskResult.generateError("Could not merge resources");
    }
    return TaskResult.generateSuccess();
  }
}
