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

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.user.client.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a UI component that corresponds to the bar at the top of the
 * Application with the logo and some controls.
 */
public class ControlBar extends Widget {

  /**
   * The set of buttons/controls that are added to the ControlBar.
   */
  public static class Controls {
    private final ControlBar.Resources resources;
    List<DivElement> controls = new ArrayList<DivElement>();

    public Controls(ControlBar.Resources resources) {
      this.resources = resources;
    }

    /**
     * Creates a control styled by a specified CSS class selector name. Performs
     * the inputed action on click.
     *
     * @param cssClassName
     * @param action
     */
    public void addControl(String cssClassName, EventListener action) {
      DivElement control = Document.get().createDivElement();
      control.setClassName(resources.controlBarCss().control() + " "
          + cssClassName);
      DomUtils.addEventListener("click", control, action);
      controls.add(control);
    }

    public void hideControls() {
      for (int i = 0, n = controls.size(); i < n; i++) {
        controls.get(i).getStyle().setProperty("display", "none");
      }
    }

    public void showControls() {
      for (int i = 0, n = controls.size(); i < n; i++) {
        controls.get(i).getStyle().setProperty("display", "");
      }
    }

    private void attach(ControlBar controlBar) {
      Element controlBarElement = controlBar.getElement();
      for (int i = 0, n = controls.size(); i < n; i++) {
        controlBarElement.appendChild(controls.get(i));
      }
    }
  }

  /**
   * Class selectors for parts we want to style.
   */
  public interface Css extends CssResource {
    String control();

    String controlBar();

    String logo();
  }

  /**
   * Images and CssResources for the ControlBar.
   */
  public interface Resources extends ClientBundle {
    @Source("resources/ControlBar.css")
    Css controlBarCss();

    @Source("resources/logo.png")
    ImageResource logo();

    @Source("resources/topBarBg.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource topBarBg();
  }

  private final Controls controls;

  /**
   * Constructor that enforces "must know parent at construction time" rule.
   *
   * @param parentElement the DOM element we want to attach to
   * @param controls the controls we want to include
   */
  public ControlBar(Element parentElement, ControlBar.Resources resources,
      Controls controls) {
    super(parentElement);
    this.controls = controls;
    Element myBaseElem = getElement();
    myBaseElem.setClassName(resources.controlBarCss().controlBar());

    DivElement logo = Document.get().createDivElement();
    logo.setClassName(resources.controlBarCss().logo());
    getElement().appendChild(logo);

    parentElement.appendChild(myBaseElem);
    controls.attach(this);
  }

  public void enableControls() {
    controls.showControls();
  }

  public void disableControls() {
    controls.hideControls();
  }
}
