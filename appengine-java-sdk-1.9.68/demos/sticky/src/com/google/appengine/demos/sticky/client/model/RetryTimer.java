/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.demos.sticky.client.model;

import com.google.gwt.user.client.Timer;

/**
 * Provides a way to control retry-on-failure schemes. Clients override
 * {@link RetryTimer#retry()} to provide the task that needs to be performed on
 * a retry, then invocations of {@link RetryTimer#retryLater()} will schedule
 * retries on a progressively longer schedule.
 *
 */
public abstract class RetryTimer {
  private static final int MAX_RETRY_DELAY = 60000;

  private static final int MIN_RETRY_DELAY = 10000;

  private static final int RETRY_DELAY_GROWTH_RATE = 20000;

  private int retryCount;

  private Timer timer = new Timer() {
    @Override
    public void run() {
      retry();
    }
  };

  /**
   * Resets the internal counter (and the progressively lengthening retry
   * timer).
   */
  public void resetRetryCount() {
    retryCount = 0;
  }

  /**
   * Determine the amount of delay before another retry should be issued. The
   * delay after the first failure is {@link RetryTimer#MIN_RETRY_DELAY}
   * milliseconds. The delay is then increased by
   * {@link RetryTimer#RETRY_DELAY_GROWTH_RATE} milliseconds on each failure
   * until it reaches {@link RetryTimer#MAX_RETRY_DELAY} where it will remain
   * constant for any subsequent failures.
   *
   * @param count
   *          the number of failures that have occurred
   * @return the delay to use, in milliseconds
   */
  private int getRetryDelay(int count) {
    return Math.min(MAX_RETRY_DELAY, MIN_RETRY_DELAY + RETRY_DELAY_GROWTH_RATE
        * count);
  }

  /**
   * Clients override {@link RetryTimer#retry()} to provide the task that will
   * be retried.
   */
  protected abstract void retry();

  /**
   * Clients should call {@link RetryTimer#retryLater()} when they encounter
   * failure to schedule a retry.
   */
  protected void retryLater() {
    timer.schedule(getRetryDelay(retryCount++));
  }
}
