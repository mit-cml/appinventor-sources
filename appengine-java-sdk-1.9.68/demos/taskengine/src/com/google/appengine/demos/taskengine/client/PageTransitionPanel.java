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

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Widget that handles transitioning between UI pages.
 */
public class PageTransitionPanel extends Widget {
  /**
   * Simple Animation class for doing those fancy page slide transitions.
   */
  private class TransitionAnimation extends Animation {
    private int newPageIndex;
    private int oldScrollLeft;
    private int oldScrollTop;
    private int scrollLeftDelta;
    private int scrollTopDelta;

    public void start(int newPageIndex) {
      this.newPageIndex = newPageIndex;
      int windowWidth = DomUtils.getWindow().getWidth();
      oldScrollLeft = getElement().getPropertyInt("scrollLeft");
      oldScrollTop = Document.get().getBody().getPropertyInt("scrollTop");
      scrollLeftDelta = (windowWidth * newPageIndex) - oldScrollLeft;
      scrollTopDelta = 0 - oldScrollTop;
      run(300);
    }

    @Override
    protected void onComplete() {
      currentPageIndex = newPageIndex;
      onUpdate(1);
    }

    @Override
    protected void onUpdate(double progress) {
      getElement().setPropertyInt("scrollLeft",
          oldScrollLeft + (int) (progress * scrollLeftDelta));
      Document.get().getBody().setPropertyInt("scrollTop",
          oldScrollTop + (int) (progress * scrollTopDelta));
    }
  }

  List<Page> pages = new ArrayList<Page>();
  private DivElement contentWrapper;
  private int currentPageIndex = 0;

  private final TransitionAnimation transitionAnimation;

  public PageTransitionPanel(Element parentElement) {
    super(parentElement);
    transitionAnimation = new TransitionAnimation();
    getElement().getStyle().setProperty("overflowX", "hidden");

    contentWrapper = Document.get().createDivElement();
    contentWrapper.getStyle().setPropertyPx("width", 4096);

    getElement().appendChild(contentWrapper);

    hookResizeListener();
  }

  public void addPage(Page page) {
    page.setPageIndex(pages.size());
    pages.add(page);
  }

  /**
   * Transitions between pages.
   *
   * @param newPageIndex the new page index we want to transition to
   */
  public void doPageTransition(int newPageIndex) {
    transitionAnimation.start(newPageIndex);
  }

  /**
   * Handles when we transition between portrait and landscape modes.
   */
  public void doResize() {
    int windowWidth = DomUtils.getWindow().getWidth();
    Element elem = getElement();
    elem.getStyle().setPropertyPx("width", windowWidth);

    for (int i = 0, n = pages.size(); i < n; i++) {
      pages.get(i).setWidth(windowWidth);
    }

    getElement().setPropertyInt("scrollLeft", windowWidth * currentPageIndex);
  }

  /**
   * Getter for the element that our Pages to attach to.
   *
   * @return the {@link Element} that we want {@link Page}s to attach to.
   */
  public Element getContainerElement() {
    return contentWrapper;
  }

  /**
   * Attaches an EventListener to the resize event on the Window.
   */
  private void hookResizeListener() {
    DomUtils.getWindow().addResizeListener(new EventListener() {
      public void onBrowserEvent(Event event) {
        doResize();
      }
    });
  }
}
