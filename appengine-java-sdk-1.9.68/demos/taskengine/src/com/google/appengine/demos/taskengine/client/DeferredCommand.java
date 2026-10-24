/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.appengine.demos.taskengine.client;

/**
 * Simple mechanism for executing deferred Commands.
 */
public abstract class DeferredCommand {
  public static native void defer(DeferredCommand command, int timeOut) /*-{
    var func = function() {
      command.
      @com.google.appengine.demos.taskengine.client.DeferredCommand::onExecute()
      ();
    };

    $wnd.setTimeout(func,timeOut);
  }-*/;

  public abstract void onExecute();
}
