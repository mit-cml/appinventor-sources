// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.stats;

import com.google.appinventor.buildserver.Compiler;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * SimpleStatReporter stores build performance measurements in memory. It saves the last
 * {@link #MAX_STATS} entries (currently 1000), evicting the oldest entries first.
 *
 * @author Evan W. Patton (ewpatton@mit.edu)
 */
public class SimpleStatReporter implements StatReporter {

  private static final int MAX_STATS = 1000;
  private static final Logger LOG = Logger.getLogger(SimpleStatReporter.class.getCanonicalName());

  /**
   * BuildStats encapsulate all of the measurements of a particular build, including the
   * timing measurements of all of the build's subprocesses.
   */
  public static class BuildStats {
    private final long start = System.currentTimeMillis();
    private final Map<String, Long> stages = new LinkedHashMap<>();
    private long end;
    private long duration;
    private long last = start;

    private BuildStats() {
      // Not instantiable outside this class
    }

    public long getStart() {
      return start;
    }

    public long getEnd() {
      return end;
    }

    public long getDuration() {
      return duration;
    }

    public Map<String, Long> getStages() {
      return Collections.unmodifiableMap(stages);
    }
  }

  private final Map<Compiler, BuildStats> activeBuilds = new HashMap<>();
  private final Map<Compiler, String> currentStages = new HashMap<>();

  private final Deque<BuildStats> successfulBuilds = new LinkedList<>();
  private final Deque<BuildStats> failedBuilds = new LinkedList<>();
  private final Deque<BuildStats> orderedBuilds = new LinkedList<>();

  @Override
  public void startBuild(Compiler compiler) {
    activeBuilds.put(compiler, new BuildStats());
  }

  @Override
  public void nextStage(Compiler compiler, String newStage) {
    BuildStats stats;
    String previousStage;
    synchronized (this) {
      stats = activeBuilds.get(compiler);
      previousStage = currentStages.get(compiler);
    }
    if (stats == null) {
      LOG.warning("Got compiler with uninitialized stats object");
      return;
    }
    if (previousStage != null) {
      stats.stages.put(previousStage, System.currentTimeMillis() - stats.last);
    }
    currentStages.put(compiler, newStage);
    stats.last = System.currentTimeMillis();
  }

  @Override
  public void stopBuild(Compiler compiler, boolean success) {
    BuildStats stats;
    String previousStage;
    synchronized (this) {
      stats = activeBuilds.remove(compiler);
      previousStage = currentStages.remove(compiler);
    }
    if (stats == null) {
      LOG.warning("Got compiler with uninitialized stats object");
      return;
    }
    stats.end = System.currentTimeMillis();
    stats.duration = stats.end - stats.start;
    if (previousStage != null) {
      stats.stages.put(previousStage, stats.end - stats.last);
    }
    queueAndExpire(stats, success ? successfulBuilds : failedBuilds);
    queueAndExpire(stats, orderedBuilds);
  }

  public Collection<BuildStats> getSuccessStats() {
    return Collections.unmodifiableCollection(successfulBuilds);
  }

  public Collection<BuildStats> getFailureStats() {
    return Collections.unmodifiableCollection(failedBuilds);
  }

  public Collection<BuildStats> getOrderedStats() {
    return Collections.unmodifiableCollection(orderedBuilds);
  }

  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
  private void queueAndExpire(BuildStats stats, final Deque<BuildStats> target) {
    synchronized (target) {
      if (target.size() == MAX_STATS) {
        target.pollFirst();
      }
      target.push(stats);
    }
  }
}
