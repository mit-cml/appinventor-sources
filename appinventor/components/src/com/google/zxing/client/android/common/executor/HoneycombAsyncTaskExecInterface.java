/*
 * Copyright (C) 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.common.executor;

import android.os.AsyncTask;

/**
 * On Honeycomb and later, {@link AsyncTask} returns to serial execution by default which is undesirable.
 * This calls Honeycomb-only APIs to request parallel execution.
 *
 * <p>This used to go through reflection, with a comment explaining that App Inventor was linked
 * against the Froyo (2.2) android libraries. That has not been true for a long time: the minimum
 * SDK is 14 and the components are compiled against a current android.jar, so executeOnExecutor can
 * simply be called directly.
 *
 * <p>The reflection was also broken in two ways, and because the failure was caught and only
 * printed, the task was never executed at all. getMethod("executeOnExecutor") passed no parameter
 * types, so it looked for a zero-argument overload that does not exist and threw
 * NoSuchMethodException; and the subsequent invoke passed the Field object for THREAD_POOL_EXECUTOR
 * rather than the executor that field holds. The visible symptom was that AutoFocusManager never
 * rescheduled itself, so the camera focused once when the scanner opened and never again.
 */
public final class HoneycombAsyncTaskExecInterface implements AsyncTaskExecInterface {

  @Override
  public <T> void execute(AsyncTask<T,?,?> task, T... args) {
    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args);
  }
}
