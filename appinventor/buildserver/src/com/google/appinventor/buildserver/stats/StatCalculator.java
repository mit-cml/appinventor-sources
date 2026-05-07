// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.stats;

import com.google.appinventor.buildserver.stats.SimpleStatReporter.BuildStats;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * StatCalculator computes descriptive statistics over a collection of stats gathered by a
 * {@link StatReporter}. It computes the min, mean, max, and standard deviation of the time
 * spent building along with each subprocess reported to the reporter.
 *
 * @author Evan W. Patton (ewpatton@mit.edu)
 */
public class StatCalculator {
  public static class Stats {
    private double minTime = Double.POSITIVE_INFINITY;
    private double avgTime = 0;
    private double maxTime = Double.NEGATIVE_INFINITY;
    private double stdev = 0;
    private int count = 0;
    private boolean avgComputed = false;

    private final Map<String, Stats> stats = new LinkedHashMap<>();

    private Stats() {
    }

    public double getMinTime() {
      return minTime;
    }

    public double getAvgTime() {
      return avgTime;
    }

    public double getMaxTime() {
      return maxTime;
    }

    public double getStdev() {
      return stdev;
    }

    /**
     * Get the stats associated with a specific stage of the build.
     *
     * @param name the stage name
     * @return the stats associated with the stage
     */
    public Stats getStageStats(String name) {
      Stats child = stats.get(name);
      if (child == null) {
        child = new Stats();
        stats.put(name, child);
      }
      return child;
    }

    public Collection<String> getStageNames() {
      return Collections.unmodifiableSet(stats.keySet());
    }

    private void update(double duration) {
      minTime = Math.min(minTime, duration);
      avgTime += duration;
      maxTime = Math.max(maxTime, duration);
      count++;
    }

    private void updateStdev(double duration) {
      if (!avgComputed) {
        avgTime /= count;
        avgComputed = true;
      }
      stdev += Math.pow(duration - avgTime, 2.0);
    }

    private void finalizeComputation() {
      stdev /= count;
      stdev = Math.sqrt(stdev);
    }
  }

  /**
   * Compute some descriptive statistics over a collection of build reports.
   *
   * @param reports the reports to compute statistics for
   * @return descriptive statistics for the reports
   */
  public Stats computeStats(Collection<BuildStats> reports) {
    Stats stats = new Stats();
    for (BuildStats report : reports) {
      stats.update(report.getDuration());
      for (Map.Entry<String, Long> stage : report.getStages().entrySet()) {
        stats.getStageStats(stage.getKey()).update(stage.getValue());
      }
    }
    for (BuildStats report : reports) {
      stats.updateStdev(report.getDuration());
      for (Map.Entry<String, Long> stage : report.getStages().entrySet()) {
        stats.getStageStats(stage.getKey()).updateStdev(stage.getValue());
      }
    }
    for (BuildStats report : reports) {
      stats.finalizeComputation();
      for (String stage : report.getStages().keySet()) {
        stats.getStageStats(stage).finalizeComputation();
      }
    }
    return stats;
  }
}
