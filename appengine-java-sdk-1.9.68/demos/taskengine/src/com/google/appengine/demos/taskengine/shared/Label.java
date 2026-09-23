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
package com.google.appengine.demos.taskengine.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Task Priority Labels.
 */
public abstract class Label implements IsSerializable {
  /**
   * Some simple contants. I don't use Enums in GWT code, simply because the
   * semantics of an Enum in java mean that it can compile to nothing less than
   * a proper class in JavaScript.
   * 
   * Since we are building an App and NOT an API, we can get away with just int
   * constants.
   */
  public static final int NOT_URGENT_IMPORTANT = 2;
  public static final int NOT_URGENT_NOT_IMPORTANT = 0;
  public static final int URGENT_IMPORTANT = 3;
  public static final int URGENT_NOT_IMPORTANT = 1;

  private static final String[] colors = {
      "#08db1c", "#f9f60d", "#fb9708", "#f81f34"};

  public static String chooseColor(int priority) {
    return colors[priority % colors.length];
  }

  private Label() {
  }
}
