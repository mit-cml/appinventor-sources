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
import com.google.appengine.demos.taskengine.client.Tasks.Controller;
import com.google.appengine.demos.taskengine.shared.Task;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 * This is UI for viewing the details of a task or adding a new task.
 */
public class TaskDetails extends Page {

  /**
   * Styles for TaskDetails.
   */
  public interface Css extends CssResource {
    String back();

    String field();

    String fieldGroup();

    String label();

    String taskDetails();
  }

  /**
   * Resources for TaskDetails.
   */
  public interface Resources extends ControlBar.Resources,
      LabelMatrix.Resources {
    @Source("resources/back.png")
    ImageResource back();

    @Source("resources/TaskDetails.css")
    TaskDetails.Css taskDetailsCss();
  }

  /**
   * Creates the controls to be added to a TaskDetails.
   */
  public static Controls createControls(final Controller controller,
      TaskDetails.Resources resources) {
    TaskDetails.Css css = resources.taskDetailsCss();

    Controls controls = new Controls(resources);
    controls.addControl(css.back(), new EventListener() {
      public void onBrowserEvent(Event event) {
        controller.goToTaskList();
      }
    });

    return controls;
  }

  private final ButtonElement cancelButton;
  private final Controller controller;
  private Task currentTask;
  private final TextAreaElement detailsField;
  private final LabelMatrix labelMatrix;
  private final TaskDetails.Resources resources;
  private final ButtonElement saveButton;
  private final InputElement titleField;

  protected TaskDetails(PageTransitionPanel parent, Controls controls,
      Controller controller, TaskDetails.Resources resources) {
    super(parent, controls, resources);
    this.resources = resources;
    this.controller = controller;
    Element contentElem = getContentContainer();
    contentElem.setClassName(resources.taskDetailsCss().taskDetails());

    titleField = Document.get().createElement("input").cast();
    contentElem.appendChild(createLabelledFieldGroup("*Title:", titleField));

    labelMatrix = new LabelMatrix(getContentContainer(), resources);
    contentElem.appendChild(createLabelledFieldGroup("*Label:",
        labelMatrix.getElement()));

    detailsField = Document.get().createTextAreaElement();
    contentElem.appendChild(createLabelledFieldGroup("Details:", detailsField));

    saveButton = Document.get().createPushButtonElement();
    saveButton.getStyle().setPropertyPx("marginLeft", 15);

    cancelButton = Document.get().createPushButtonElement();
    cancelButton.getStyle().setPropertyPx("marginLeft", 75);
    cancelButton.setInnerText("Cancel");

    contentElem.appendChild(cancelButton);
    contentElem.appendChild(saveButton);

    hookEventListeners();
  }

  /**
   * Brings focus to the titleField.
   */
  public void setFocus() {
    titleField.focus();
  }

  /**
   * Displays the details of a Task. If no task is specified then we assume we
   * are creating a new Task.
   *
   * @param task the {@link Task} we are viewing the details of. If this is
   *          null then we assume we are creating a new Task.
   */
  public void view(Task task) {
    currentTask = task;
    if (task != null) {
      saveButton.setInnerText("Save Task");
      populateFields();
    } else {
      saveButton.setInnerText("Add Task");
      resetFields();
    }
  }

  private DivElement createLabelledFieldGroup(String labelText, Element field) {
    DivElement fieldGroup = Document.get().createDivElement();
    fieldGroup.setClassName(resources.taskDetailsCss().fieldGroup());

    DivElement label = Document.get().createDivElement();
    label.setInnerText(labelText);

    label.setClassName(resources.taskDetailsCss().label());
    field.setClassName(resources.taskDetailsCss().field());

    fieldGroup.appendChild(label);
    fieldGroup.appendChild(field);

    return fieldGroup;
  }

  private void hookEventListeners() {
    DomUtils.addEventListener("click", cancelButton, new EventListener() {

      public void onBrowserEvent(Event event) {
        controller.goToTaskList();
      }

    });

    DomUtils.addEventListener("click", saveButton, new EventListener() {
      public void onBrowserEvent(Event event) {
        if (currentTask == null) {
          currentTask = new Task("", titleField.getValue(),
              detailsField.getValue(), labelMatrix.getCurrentLabelPriority(),
              false);
          if (validateFields(currentTask)) {
            controller.addNewTask(currentTask);
          } else {
            currentTask = null;
          }
        } else {
          int oldPriority = currentTask.getLabelPriority();
          currentTask.setTitle(titleField.getValue());
          currentTask.setDetails(detailsField.getValue());
          currentTask.setLabelPriority(labelMatrix.getCurrentLabelPriority());
          if (validateFields(currentTask)) {
            controller.updateTask(currentTask, oldPriority);
          }
        }
      }
    });
  }

  private void populateFields() {
    titleField.setValue(currentTask.getTitle());
    detailsField.setValue(currentTask.getDetails());
    labelMatrix.setLabelPriority(currentTask.getLabelPriority());
  }

  private void resetFields() {
    titleField.setValue("");
    detailsField.setValue("");
    labelMatrix.setLabelPriority(-1);
    labelMatrix.showColorChooser();
  }

  private boolean validateFields(Task potentialTask) {
    if (potentialTask.getTitle().equals("")
        || potentialTask.getLabelPriority() < 0) {
      DomUtils.getWindow().alert("Title and Label must be filled in.");
      return false;
    } else {
      return true;
    }
  }
}
