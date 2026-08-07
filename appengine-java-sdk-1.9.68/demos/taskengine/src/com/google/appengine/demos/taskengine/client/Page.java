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

import com.google.appengine.demos.taskengine.client.ControlBar.Controls;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

/**
 * A screen in the UI.
 */
public abstract class Page extends Widget {
  private final DivElement contentElement;
  private final ControlBar controlBar;
  private int pageIndex = 0;

  public Page(PageTransitionPanel parent, Controls controls,
      ControlBar.Resources resources) {
    super(parent.getContainerElement());
    Element myElem = getElement();

    controlBar = new ControlBar(myElem, resources, controls);
    contentElement = Document.get().createDivElement();
    myElem.appendChild(contentElement);
    parent.addPage(this);
    myElem.getStyle().setProperty("cssFloat", "left");
  }

  public DivElement getContentContainer() {
    return contentElement;
  }

  public ControlBar getControlBar() {
    return controlBar;
  }

  public int getPageIndex() {
    return pageIndex;
  }

  public void setPageIndex(int index) {
    this.pageIndex = index;
  }

  public void setWidth(int newWidth) {
    getElement().getStyle().setPropertyPx("width", newWidth);
  }
}
