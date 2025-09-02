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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

/**
 * Simple base class for things that will act like high level UI components.
 * This App is fairly simple though so not much is in here.
 */
public abstract class Widget {
  /**
   * This is the DOM Element that is the root for this Widget.
   */
  private final Element element;

  public Widget(Element parentElement) {
    element = Document.get().createDivElement();

    parentElement.appendChild(element);
  }

  public final Element getElement() {
    return element;
  }
}
