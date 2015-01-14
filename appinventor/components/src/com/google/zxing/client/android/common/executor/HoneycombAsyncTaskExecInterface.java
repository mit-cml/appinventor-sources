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

/* import android.annotation.TargetApi; */
import android.os.AsyncTask;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
/**
 * On Honeycomb and later, {@link AsyncTask} returns to serial execution by default which is undesirable.
 * This calls Honeycomb-only APIs to request parallel execution.
 *
 * For MIT App Inventor we have to use reflection because we are linked with the Froyo (2.2)
 * version of the android libraries.
 */
/* @TargetApi(11) */
public final class HoneycombAsyncTaskExecInterface implements AsyncTaskExecInterface {

  @Override
  public <T> void execute(AsyncTask<T,?,?> task, T... args) {
    try {
	Field tpe = AsyncTask.class.getField("THREAD_POOL_EXECUTOR");
	Method emethod = task.getClass().getMethod("executeOnExecutor");
	emethod.invoke(task, tpe, args);
    } catch (Exception e) {
	e.printStackTrace();
    }
  }
}
