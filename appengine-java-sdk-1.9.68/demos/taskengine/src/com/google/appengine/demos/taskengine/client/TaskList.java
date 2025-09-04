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
import com.google.appengine.demos.taskengine.client.DomUtils.EventRemover;
import com.google.appengine.demos.taskengine.client.Tasks.Controller;
import com.google.appengine.demos.taskengine.shared.Label;
import com.google.appengine.demos.taskengine.shared.Task;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The main UI for our Application. This is the list of tasks.
 */
public class TaskList extends Page {

  /**
   * Styles for this Widget. CssResource styles are compiled, minified and
   * injected into the compiled output for this application. Fewer round trips
   * since everything is included in the JavaScript :)!
   */
  public interface Css extends CssResource {
    String checkBoxContainer();

    String checked();

    String garbage();

    String plus();

    String taskRow();

    String taskRowPersisted();

    String title();

    String unChecked();

    String user();
  }

  /**
   * Resources for this Widget.
   *
   */
  public interface Resources extends ControlBar.Resources {
    @Source("resources/checkBox.png")
    ImageResource checkBox();

    @Source("resources/check.png")
    ImageResource checkMark();

    @Source("resources/garbage.png")
    ImageResource garbage();

    @Source("resources/plus.png")
    ImageResource plus();

    @Source("resources/TaskList.css")
    TaskList.Css taskListCss();
  }

  /**
   * This class wraps TaskData and its associated DOM elements. This class
   * controls the rendering of Task Data to the UI.
   */
  public class TaskRow extends Widget {
    private final DivElement checkMark;
    private final Task data;
    private List<EventRemover> removers = new ArrayList<EventRemover>();

    private final DivElement titleElem;

    public TaskRow(Element parentElem, Task data) {
      super(parentElem);
      this.data = data;
      Element myElem = getElement();
      TaskList.Css css = resources.taskListCss();
      myElem.setClassName(css.taskRow());
      titleElem = Document.get().createDivElement();
      titleElem.setClassName(css.title());
      DivElement rightMask = Document.get().createDivElement();
      rightMask.setClassName(css.checkBoxContainer());
      checkMark = Document.get().createDivElement();
      rightMask.appendChild(checkMark);

      myElem.appendChild(titleElem);
      myElem.appendChild(rightMask);

      renderTask();

      hookEventListeners();
    }

    public Task getTaskData() {
      return data;
    }

    public void removeFromList() {
      for (int i = 0, n = removers.size(); i < n; i++) {
        removers.get(i).remove();
      }
      removers.clear();

      taskRowMap.remove(data.getId());
      getTaskListContainerElement(data.getLabelPriority()).removeChild(
          getElement());
    }

    public void renderTask() {
      titleElem.getStyle().setProperty("borderColor",
          Label.chooseColor(data.getLabelPriority()));
      titleElem.setInnerText(data.getTitle());
      if (data.isFinished()) {
        checkMark.setClassName(resources.taskListCss().checked());
      } else {
        checkMark.setClassName(resources.taskListCss().unChecked());
      }
    }

    public void setRowAsNotPersisted() {
      getElement().setClassName(resources.taskListCss().taskRow());
    }

    public void setRowAsPersisted(String id) {
      getElement().setClassName(
          resources.taskListCss().taskRow() + " "
              + resources.taskListCss().taskRowPersisted());
      taskRowMap.remove(data.getId());
      data.setId(id);
      taskRowMap.put(id, this);
    }

    private void hookEventListeners() {
      DomUtils.addEventListener("click", titleElem, new EventListener() {

        public void onBrowserEvent(Event event) {
          controller.loadTask(data);
          controller.goToTaskDetails();
        }

      });

      DomUtils.addEventListener("click", checkMark, new EventListener() {

        public void onBrowserEvent(Event event) {
          if (data.isFinished()) {
            data.setFinished(false);
            completedTasks.remove(TaskRow.this);
          } else {
            data.setFinished(true);
            completedTasks.add(TaskRow.this);
          }
          renderTask();
          controller.persistTask(TaskRow.this);
          event.stopPropagation();
        }

      });
    }
  }

  /**
   * Creates the controls to be added to a TaskList.
   */
  public static Controls createControls(final Controller controller,
      TaskList.Resources resources) {
    TaskList.Css css = resources.taskListCss();

    Controls controls = new Controls(resources);
    controls.addControl(css.plus(), new EventListener() {

      public void onBrowserEvent(Event event) {
        controller.loadTask(null);
        controller.goToTaskDetails();
      }

    });

    controls.addControl(css.garbage(), new EventListener() {

      public void onBrowserEvent(Event event) {
        controller.deleteCompletedTasks();
      }

    });

    return controls;
  }

  private final List<TaskRow> completedTasks = new ArrayList<TaskRow>();
  private final Controller controller;
  private boolean isLoggedIn = false;
  private final AnchorElement logoutLink;
  private final DivElement notUrgentImportantTasks;
  private final DivElement notUrgentNotImportantTasks;
  private final TaskList.Resources resources;
  private final HashMap<String, TaskRow> taskRowMap =
    new HashMap<String, TaskRow>();
  private final List<TaskRow> tasksPendingDeleteConfirmation =
    new ArrayList<TaskRow>();
  private final DivElement urgentImportantTasks;
  private final DivElement urgentNotImportantTasks;

  private final DivElement userEmail;

  protected TaskList(PageTransitionPanel parent, Controls controls,
      Controller controller, TaskList.Resources resources) {
    super(parent, controls, resources);
    this.controller = controller;
    this.resources = resources;

    urgentNotImportantTasks = Document.get().createDivElement();
    urgentImportantTasks = Document.get().createDivElement();
    notUrgentNotImportantTasks = Document.get().createDivElement();
    notUrgentImportantTasks = Document.get().createDivElement();
    Element container = getContentContainer();
    container.appendChild(urgentImportantTasks);
    container.appendChild(notUrgentImportantTasks);
    container.appendChild(urgentNotImportantTasks);
    container.appendChild(notUrgentNotImportantTasks);

    userEmail = Document.get().createDivElement();
    userEmail.getStyle().setProperty("display", "inline-block");
    userEmail.setInnerText("Loading...");
    logoutLink = Document.get().createAnchorElement();
    DivElement userInfoContainer = Document.get().createDivElement();
    userInfoContainer.appendChild(userEmail);
    userInfoContainer.appendChild(logoutLink);
    userInfoContainer.setClassName(resources.taskListCss().user());
    container.appendChild(userInfoContainer);
  }

  /**
   * Adds a task to our TaskList UI.
   *
   * @param task the task to be added
   * @return returns the {@link TaskRow} that was attached to the UI
   */
  public TaskRow addTaskToUi(Task task) {
    Element container = getTaskListContainerElement(task.getLabelPriority());
    TaskRow row = new TaskRow(container, task);

    if (row.getTaskData().isFinished()) {
      completedTasks.add(row);
    }
    return row;
  }

  /**
   * Removes the tasks pending deletion.
   */
  public void confirmDeletion() {
    for (int i = 0, n = tasksPendingDeleteConfirmation.size(); i < n; i++) {
      TaskRow row = tasksPendingDeleteConfirmation.get(i);
      row.removeFromList();
    }
    tasksPendingDeleteConfirmation.clear();
  }

  /**
   * Gets the tasks currently marked for completion and moves them over to
   * pending delete confirmation.
   *
   * @return the tasks currently marked for completion
   */
  public String[] getCompletedTaskIdsAndMoveToPending() {
    String[] tasks = new String[completedTasks.size()];
    int i = 0;
    while (!completedTasks.isEmpty()) {
      TaskRow row = completedTasks.remove(0);
      row.setRowAsNotPersisted();
      tasks[i] = row.getTaskData().getId();
      tasksPendingDeleteConfirmation.add(row);
      i++;
    }
    return tasks;
  }

  public boolean isLoggedIn() {
    return isLoggedIn;
  }

  /**
   * Moves tasks that were pending deletion back over to pending. This is called
   * in the case where we fail to complete a delete due to
   * network/authentication issues.
   */
  public void movePendingTasksBackToCompleted() {
    int i = 0;
    while (!tasksPendingDeleteConfirmation.isEmpty()) {
      TaskRow row = tasksPendingDeleteConfirmation.remove(0);
      completedTasks.add(row);
      i++;
    }
  }

  /**
   * Method invoked if our RPC times out, returns an error, or if we are not
   * logged in.
   *
   * @param loginUrl server generated sign in url, or <code>null</code> if we
   *          time out or have an RPC error.
   */
  public void notifyNotLoggedIn(String loginUrl) {
    getControlBar().disableControls();
    if (loginUrl != null) {
      userEmail.setInnerText("Please ");
      logoutLink.setHref(loginUrl);
      logoutLink.setInnerText(" signin.");
    } else {
      userEmail.setInnerText("Network slow :(, please wait or");
      logoutLink.setHref("javascript:location.reload(true);");
      logoutLink.setInnerText(" Try Refresh");
    }
  }

  /**
   * Sets the logout link and displays the currently signed in user.
   *
   * @param userEmailStr the email address for the currently signed in user.
   * @param logoutUrl the logout url.
   */
  public void setUserLoggedIn(String userEmailStr, String logoutUrl) {
    isLoggedIn = true;
    userEmail.setInnerText(userEmailStr + " | ");
    logoutLink.setHref(logoutUrl);
    logoutLink.setInnerText(" logout");

    getControlBar().enableControls();
  }

  /**
   * Updates the UI to reflect that the underlying task has been updated.
   *
   * @param task the {@link Task} that has been updated.
   * @param oldPriority the old priority level of the task.
   * @return returns the updated {@link TaskRow}.
   */
  public TaskRow updateTask(Task task, int oldPriority) {
    TaskRow row = taskRowMap.get(task.getId());
    assert (row != null);
    if (oldPriority != task.getLabelPriority()) {
      Element container = getTaskListContainerElement(oldPriority);
      container.removeChild(row.getElement());
      container = getTaskListContainerElement(task.getLabelPriority());
      container.appendChild(row.getElement());
    }
    row.setRowAsNotPersisted();
    row.renderTask();
    return row;
  }

  private Element getTaskListContainerElement(int priorityLevel) {
    switch (priorityLevel) {
      case Label.NOT_URGENT_IMPORTANT:
        return notUrgentImportantTasks;
      case Label.NOT_URGENT_NOT_IMPORTANT:
        return notUrgentNotImportantTasks;
      case Label.URGENT_IMPORTANT:
        return urgentImportantTasks;
      case Label.URGENT_NOT_IMPORTANT:
        return urgentNotImportantTasks;
      default:
        return urgentImportantTasks;
    }
  }
}
