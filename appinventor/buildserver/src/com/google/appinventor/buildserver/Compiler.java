// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import com.google.appinventor.buildserver.context.CompilerContext;
import com.google.appinventor.buildserver.context.Paths;
import com.google.appinventor.buildserver.interfaces.Task;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executor class for the YAIL compiler. Callable implementation
 * allows running different threads easier.
 *
 * <p>Will recursively accept {@link Task} extended classes
 * and build them to produce an output. It uses a builder
 * style pattern, where receives build information, and then
 * {@link Task} can be added.</p>
 *
 * @see CompilerContext
 *
 * @author diego@barreiro.xyz (Diego Barreiro)
 */
public class Compiler<P extends Paths, T extends CompilerContext<P>> implements Callable<Boolean> {
  private static final Logger LOG = Logger.getLogger(Compiler.class.getName());
  private final List<Class<? extends Task<? super T>>> tasks;
  private T context;
  private String ext = BuildType.APK_EXTENSION;

  // The Builder class will construct an Executor object on which Task's
  // can be added.
  public static class Builder<P extends Paths, T extends CompilerContext<P>> {
    private T context;
    private String ext;

    public Builder() {
    }

    // Passes the previously constructed ExecutorContext with all
    // build info.
    public Builder<P, T> withContext(T context) {
      this.context = context;
      return this;
    }

    /**
     * Specifies the build type (the extension output actually).
     * Only accepts the one specified in BuildType annotation.
     */
    public Builder<P, T> withType(String ext) {
      if (ext == null || !ext.equals(BuildType.APK_EXTENSION)
          && !ext.equals(BuildType.AAB_EXTENSION)) {
        System.out.println("[ERROR] BuildType '" + ext + "' is not supported!");
      } else {
        this.ext = ext;
      }
      return this;
    }

    /**
     * Constructs the Executor object, making sure all needed attributes are passed.
     *
     * @return a new Compiler configured by the builder
     */
    public Compiler<P, T> build() {
      if (context == null) {
        System.out.println("[ERROR] ExecutorContext was not provided to Executor");
        return null;
      } else if (ext == null) {
        this.ext = BuildType.APK_EXTENSION;
        System.out.println("[WARN] No BuildType specified; using BuildType.APK_EXTENSION");
      }

      Compiler<P, T> compiler = new Compiler<>();
      compiler.context = context;
      compiler.ext = ext;
      return compiler;
    }
  }

  // Actually, constructor is private, as it will always be
  // built using the Executor.Builder.
  private Compiler() {
    this.tasks = new ArrayList<>();
  }

  /**
   * Adds a new Task to the build.
   */
  public Compiler<P, T> add(Class<? extends Task<? super T>> task) {
    assert task != null;
    this.tasks.add(task);
    return this;
  }

  // "Main" method that returns either true or false, depending
  // on result.
  @Override
  public Boolean call() {
    // Initializes progress to 0.
    context.getReporter().setProgress(0);
    context.getStatReporter().startBuild(this);
    int numTasks = this.tasks.size();

    // If no tasks, we technically have successfully built everything.
    if (numTasks == 0) {
      context.getReporter().warn("No tasks were executed");
      context.getReporter().setProgress(100);
      return true;
    }

    for (int i = 0; i < numTasks; i++) {
      // We accept Classes, but not initialized ones.
      Class<? extends Task<?>> task = this.tasks.get(i);
      String taskName = task.getSimpleName();

      // We try to initialize a Task instance.
      Object taskObject;
      try {
        taskObject = task.newInstance();
      } catch (IllegalAccessException | InstantiationException e) {
        LOG.log(Level.SEVERE, "Could not create new task " + taskName, e);
        context.getReporter().error("Could not create new task " + taskName);
        return false;
      }

      // Task's will have an annotation to make sure they only run in
      // the specified build type. If no annotation present, we throw
      // a warning.
      if (task.isAnnotationPresent(BuildType.class)) {
        BuildType buildType = task.getAnnotation(BuildType.class);
        switch (ext) {
          case BuildType.AAB_EXTENSION:
            if (!buildType.aab()) {
              context.getReporter().error("Task " + taskName + " does not support builds on AABs!");
              return false;
            }
            break;
          default:
          case BuildType.APK_EXTENSION:
            if (!buildType.apk()) {
              context.getReporter().error("Task " + taskName + " does not support builds on APKs!");
              return false;
            }
            break;
        }
      } else {
        context.getReporter().warn("Task " + taskName + " does not contain build type targets!");
      }

      // Get the current time to know the time needed to execute it.
      context.getReporter().taskStart(taskName);
      context.getStatReporter().nextStage(this, task.getSimpleName());
      long start = System.currentTimeMillis();

      // And then invoke the execute(ExecutorContext) method to run the Task.
      TaskResult result;
      try {
        Method execute = task.getMethod("execute", CompilerContext.class);
        result = (TaskResult) execute.invoke(taskObject, context);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        context.getReporter().taskError(-1);
        LOG.log(Level.SEVERE, "Error running task " + task, e);
        return false;
      }
      double endTime = (System.currentTimeMillis() - start) / 1000.0;

      // Make sure result is success, else we'll throw an error and don't run
      // more tasks.
      if (result == null || !result.isSuccess()) {
        context.getReporter().error(result == null || result.getError() == null
            ? "Unknown exception" : result.getError().getMessage(), true);
        context.getReporter().taskError(endTime);
        return false;
      }

      // Update progress depending on the number of steps.
      context.getReporter().setProgress(((i + 1) * 100) / numTasks);
      context.getReporter().taskSuccess(endTime);
    }
    return true;
  }

  @Override
  public String toString() {
    return "Compiler{"
        + "tasks=" + tasks
        + ", context=" + context
        + ", ext='" + ext + '\''
        + '}';
  }
}
