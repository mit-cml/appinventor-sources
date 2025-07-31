// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.YAIL_FILE_EXTENSION;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.Project;
import com.google.appinventor.buildserver.Signatures;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.context.AndroidPaths;
import com.google.appinventor.buildserver.context.CompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.Execution;
import com.google.appinventor.buildserver.util.ExecutorUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Compiles screen source files written in YAIL to Java class files.
 */
@BuildType(apk = true, aab = true)
public class GenerateClasses implements AndroidTask {
  CompilerContext<AndroidPaths> context;

  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    this.context = context;

    if (!this.compileRClasses()) {
      return TaskResult.generateError("Could not compile R classes");
    }

    try {
      List<Project.SourceDescriptor> sources = context.getProject().getSources();
      List<String> sourceFileNames = Lists.newArrayListWithCapacity(sources.size());
      List<String> classFileNames = Lists.newArrayListWithCapacity(sources.size());
      boolean userCodeExists = false;
      for (Project.SourceDescriptor source : sources) {
        String sourceFileName = source.getFile().getAbsolutePath();
        context.getReporter().info("Source File: " + sourceFileName);
        int srcIndex = sourceFileName.indexOf(File.separator + ".." + File.separator + "src"
            + File.separator);
        String sourceFileRelativePath = sourceFileName.substring(srcIndex + 8);
        String classFileName = (context.getPaths().getClassesDir().getAbsolutePath()
            + File.separator + sourceFileRelativePath)
            .replace(YAIL_FILE_EXTENSION, ".class");

        // Check whether user code exists by seeing if a left parenthesis exists at the beginning of
        // a line in the file
        // TODO(user): Replace with more robust test of empty source file.
        if (!userCodeExists) {
          try (Reader fileReader = new FileReader(sourceFileName)) {
            while (fileReader.ready()) {
              int c = fileReader.read();
              if (c == '(') {
                userCodeExists = true;
                break;
              }
            }
          }
        }
        sourceFileNames.add(sourceFileName);
        classFileNames.add(classFileName);
      }

      if (!userCodeExists) {
        return TaskResult.generateError("No user code exists");
      }

      // Construct the class path including component libraries (jars)
      StringBuilder classpath = new StringBuilder(context.getResources().getKawaRuntime());
      classpath.append(File.pathSeparator);
      classpath.append(context.getResources().getAcraRuntime());
      classpath.append(File.pathSeparator);
      classpath.append(context.getResources().getSimpleAndroidRuntimeJar());
      classpath.append(File.pathSeparator);

      for (String jar : context.getResources().getSupportJars()) {
        classpath.append(context.getResource(jar));
        classpath.append(File.pathSeparator);
      }

      // attach the jars of external comps
      Set<String> addedExtJars = new HashSet<String>();
      for (String type : context.getExtCompTypes()) {
        String sourcePath = ExecutorUtils.getExtCompDirPath(
            type, context.getProject(), context.getExtTypePathCache())
            + context.getResources().getSimpleAndroidRuntimeJarPath();
        if (!addedExtJars.contains(sourcePath)) {
          // don't add multiple copies for bundled extensions
          classpath.append(sourcePath);
          classpath.append(File.pathSeparator);
          addedExtJars.add(sourcePath);
        }
      }

      // Add component library names to classpath
      for (String type : context.getComponentInfo().getLibsNeeded().keySet()) {
        for (String lib : context.getComponentInfo().getLibsNeeded().get(type)) {
          String sourcePath = "";
          String pathSuffix = context.getResources().getRuntimeFilesDir() + lib;

          if (context.getSimpleCompTypes().contains(type)) {
            sourcePath = context.getResource(pathSuffix);
          } else if (context.getExtCompTypes().contains(type)) {
            sourcePath = ExecutorUtils.getExtCompDirPath(
                type, context.getProject(), context.getExtTypePathCache()) + pathSuffix;
          } else {
            context.getReporter().error("Found a lost component", true);
            return TaskResult.generateError("Error while generating classes");
          }

          context.getComponentInfo().getUniqueLibsNeeded().add(sourcePath);

          classpath.append(sourcePath);
          classpath.append(File.pathSeparator);
        }
      }

      // Add dependencies for classes.jar in any AAR libraries
      for (File classesJar : context.getComponentInfo().getExplodedAarLibs().getClasses()) {
        if (classesJar != null) {  // true for optimized AARs in App Inventor libs
          final String abspath = classesJar.getAbsolutePath();
          context.getComponentInfo().getUniqueLibsNeeded().add(abspath);
          classpath.append(abspath);
          classpath.append(File.pathSeparator);
        }
      }
      if (context.getComponentInfo().getExplodedAarLibs().size() > 0) {
        classpath.append(context.getComponentInfo().getExplodedAarLibs().getOutputDirectory()
            .getAbsolutePath());
        classpath.append(File.pathSeparator);
      }

      classpath.append(context.getResources().getAndroidRuntime());

      context.getReporter().info("Libraries Classpath = " + classpath);

      String yailRuntime = context.getResources().getYailRuntime();
      List<String> kawaCommandArgs = Lists.newArrayList();
      int mx = context.getChildProcessRam() - 200;
      Collections.addAll(kawaCommandArgs,
          System.getProperty("java.home") + "/bin/java",
          "-Dfile.encoding=UTF-8",
          "-mx" + mx + "M",
          "-cp", classpath.toString(),
          "kawa.repl",
          "-f", yailRuntime,
          "-d", context.getPaths().getClassesDir().getAbsolutePath(),
          "-P", Signatures.getPackageName(context.getProject().getMainClass()) + ".",
          "-C");
      // TODO(lizlooney) - we are currently using (and have always used) absolute paths for the
      // source file names. The resulting .class files contain references to the source file names,
      // including the name of the tmp directory that contains them. We may be able to avoid that
      // by using source file names that are relative to the project root and using the project
      // root as the working directory for the Kawa compiler process.
      kawaCommandArgs.addAll(sourceFileNames);
      kawaCommandArgs.add(yailRuntime);
      String[] kawaCommandLine = kawaCommandArgs.toArray(new String[0]);

      // Capture Kawa compiler stderr. The ODE server parses out the warnings and errors and adds
      // them to the protocol buffer for logging purposes. (See
      // buildserver/ProjectBuilder.processCompilerOutout.
      ByteArrayOutputStream kawaOutputStream = new ByteArrayOutputStream();
      boolean kawaSuccess;
      synchronized (context.getResources().getSyncKawaOrDx()) {
        kawaSuccess = Execution.execute(null, kawaCommandLine,
            System.out, new PrintStream(kawaOutputStream), Execution.Timeout.MEDIUM);
      }
      if (!kawaSuccess) {
        context.getReporter().error("Kawa compile has failed.", true);
      }
      String kawaOutput = kawaOutputStream.toString();
      context.getReporter().getSystemOut().print(kawaOutput);

      // Check that all of the class files were created.
      // If they weren't, return with an error.
      for (String classFileName : classFileNames) {
        File classFile = new File(classFileName);
        if (!classFile.exists()) {
          String screenName = classFileName.substring(classFileName.lastIndexOf('/') + 1,
              classFileName.lastIndexOf('.'));
          return TaskResult.generateError("Can't find class file for Screen '" + screenName + "'");
        }
      }
    } catch (IOException e) {
      return TaskResult.generateError(e);
    }

    return TaskResult.generateSuccess();
  }

  @VisibleForTesting
  boolean compileRClasses() {
    if (context.getComponentInfo().getExplodedAarLibs().isEmpty()) {
      return true;  // nothing to see here
    }
    int error;
    try {
      error = context.getComponentInfo().getExplodedAarLibs().writeRClasses(
          context.getPaths().getClassesDir(),
          Signatures.getPackageName(context.getProject().getMainClass()),
          context.getResources().getAppRTxt()
      );
    } catch (IOException | InterruptedException e) {
      context.getReporter().error("Error while compiling R classes", true);
      return false;
    }
    if (error != 0) {
      context.getReporter().error("Compile R Classes returned E=" + error, true);
      return false;
    }
    return true;
  }
}
