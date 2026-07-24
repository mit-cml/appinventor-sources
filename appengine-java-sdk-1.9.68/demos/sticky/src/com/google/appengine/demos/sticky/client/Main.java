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

package com.google.appengine.demos.sticky.client;

import com.google.appengine.demos.sticky.client.model.Model;
import com.google.appengine.demos.sticky.client.model.RetryTimer;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;

/**
 * The entry point for the Sticky application.
 *
 */
public class Main extends RetryTimer implements EntryPoint, Model.LoadObserver,
    Model.StatusObserver {

  /**
   * An aggregated image bundle will auto-sprite all the images in the
   * application.
   */
  public interface Images extends HeaderView.Images {
  }

  /**
   * Provides Ui to notify the user of model based events. These include tasks
   * (like loading a surface) and also errors (like lost communication to the
   * server).
   */
  private static class StatusView extends SimplePanel {
    private final DivElement taskStatusElement;

    private final DivElement errorStatusElement;

    public StatusView() {
      final Document document = Document.get();
      final Element element = getElement();
      taskStatusElement = element.appendChild(document.createDivElement());
      errorStatusElement = element.appendChild(document.createDivElement());
      errorStatusElement.setInnerText("No response from server");

      setStyleName("status-view");
      taskStatusElement.setClassName("status-view-task");
      errorStatusElement.setClassName("status-view-error");

      hideErrorStatus();
      hideTaskStatus();
    }

    /**
     * Hides the Ui for server communication lost errors.
     */
    public void hideErrorStatus() {
      UIObject.setVisible(errorStatusElement, false);
    }

    /**
     * Hides the task status Ui.
     */
    public void hideTaskStatus() {
      UIObject.setVisible(taskStatusElement, false);
    }

    /**
     * Displays the Ui for server communication lost errors.
     */
    public void showErrorStatus() {
      UIObject.setVisible(errorStatusElement, true);
    }

    /**
     * Displays the UI for a task status.
     *
     * @param text
     *          the text to be displayed
     */
    public void showTaskStatus(String text) {
      taskStatusElement.setInnerText(text);
      UIObject.setVisible(taskStatusElement, true);
    }
  }

  private final StatusView status = new StatusView();

  public void onModelLoaded(Model model) {
    status.hideTaskStatus();
    status.hideErrorStatus();

    final Images images = GWT.create(Images.class);

    Window.enableScrolling(false);

    final RootPanel root = RootPanel.get();
    new HeaderView(images, root, model);
    root.add(new SurfaceView(model));
  }

  public void onModelLoadFailed() {
    retryLater();
    status.showErrorStatus();
  }

  public void onModuleLoad() {
    RootPanel.get().add(status);
    status.showTaskStatus("Loading");
    Model.load(this, this);
  }

  public void onServerCameBack() {
    status.hideErrorStatus();
  }

  public void onServerWentAway() {
    status.showErrorStatus();
  }

  public void onTaskFinished() {
    status.hideTaskStatus();
  }

  public void onTaskStarted(String description) {
    status.showTaskStatus(description);
  }

  @Override
  protected void retry() {
    Model.load(this, this);
  }
}
