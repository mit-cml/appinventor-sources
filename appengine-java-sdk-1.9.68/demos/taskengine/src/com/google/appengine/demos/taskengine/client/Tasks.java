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

import com.google.appengine.demos.taskengine.client.TaskList.TaskRow;
import com.google.appengine.demos.taskengine.shared.Task;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * 'Task Engine' is a mobile sample App that demonstrates using GWT at a lower
 * level and closer to the underlying DOM.
 *
 * If your Application is simple enough, you may be able to get by without
 * leveraging GWT's built in widgets and just use the core DOM API. For smaller
 * Apps, this may buy you some savings in code size. In the end however, there
 * is a trade off with regards to code complexity and managability. The more
 * complex the App is, the more you may find yourself re-inventing stuff that
 * GWT already has built in. Applications like those more often than not will
 * benefit all around (size and development effort) from using GWT's built in
 * Widgets.
 */
public class Tasks implements EntryPoint {
  /**
   * This class provides Application level controls like page switching, and
   * exposes the RPC service. We pass an instance of this object around so that
   * the various components of our App can have access to the functionality.
   */
  public class Controller {

    /**
     * Adds a new task to the task list UI and persist it to the server.
     *
     * @param task
     */
    public void addNewTask(Task task) {
      persistTask(taskList.addTaskToUi(task));
      goToTaskList();
    }

    /**
     * Deletes all tasks from the server that have been checked off in the UI as
     * completed.
     */
    public void deleteCompletedTasks() {
      String[] tasksToDelete = taskList.getCompletedTaskIdsAndMoveToPending();
      if (tasksToDelete.length > 0) {
        boolean shouldDelete = DomUtils.getWindow().confirm(
            "Delete Completed Tasks?");
        if (shouldDelete) {
          api.deleteTasks(tasksToDelete, new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {
              taskList.movePendingTasksBackToCompleted();
            }

            public void onSuccess(String result) {
              if (result == null) {
                onFailure(null);
              }
              taskList.confirmDeletion();
            }

          });
        }
      }
    }

    /**
     * Transitions to the {@link TaskDetails} page.
     */
    public void goToTaskDetails() {
      uiPages.doPageTransition(taskDetails.getPageIndex());
    }

    /**
     * Transitions to the {@link TaskList} page.
     */
    public void goToTaskList() {
      uiPages.doPageTransition(taskList.getPageIndex());
    }

    /**
     * Loads a task in the TaskDetails page.
     *
     * @param task the {@link Task} for the task to be loaded.
     */
    public void loadTask(Task task) {
      taskDetails.view(task);
    }

    /**
     * Saves a {@link Task} for a given {@link TaskRow} to the server. This is
     * used to both update existing tasks, and to save new ones. It updates the
     * UI for the TaskRow after getting a response from the server.
     *
     * @param row the {@link TaskRow} for the task we want to save.
     */
    public void persistTask(final TaskRow row) {
      api.persistTask(row.getTaskData(), new AsyncCallback<String>() {

        public void onFailure(Throwable caught) {
          DomUtils.getWindow().alert(
              "Failed to save task to server. Try so re-save it.");
        }

        public void onSuccess(String result) {
          if (result == null) {
            onFailure(null);
          } else {
            row.setRowAsPersisted(result);
          }
        }

      });
    }

    /**
     * Updates the task information on the server and transitions back to the
     * {@link TaskList}.
     *
     * @param task the {@link Task} for the updated task
     * @param oldPriority the old value for the priority of the task
     */
    public void updateTask(Task task, int oldPriority) {
      persistTask(taskList.updateTask(task, oldPriority));
      goToTaskList();
    }
  }

  /**
   * Our resources used in the sample.
   *
   * {@link ControlBar.Resources} is an {@link ImmutableResourceBundle} (IRB).
   * IRB allows us to have a programmatic interface with static resources used
   * in this sample, like CSS styles and Images. Images specified here (or in
   * the inheritance chain) are automatically combined into a single sprite,
   * with corresponding CSS automatically generated to display each individual
   * image piece through cropping.
   *
   */
  public interface Resources extends TaskList.Resources, TaskDetails.Resources {
  }

  private final TasksApiAsync api = GWT.create(TasksApi.class);;
  private final Resources resources = GWT.create(Resources.class);
  private TaskDetails taskDetails;
  private TaskList taskList;
  private PageTransitionPanel uiPages;

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    StyleInjector.injectAtEnd(resources.taskDetailsCss().getText()
        + resources.taskListCss().getText()
        + resources.controlBarCss().getText()
        + resources.labelMatrixCss().getText());

    uiPages = new PageTransitionPanel(Document.get().getBody());
    Controller controller = new Controller();

    ControlBar.Controls taskListControls = TaskList.createControls(controller,
        resources);
    taskList = new TaskList(uiPages, taskListControls, controller, resources);

    ControlBar.Controls taskDetailsControls = TaskDetails.createControls(
        controller, resources);
    taskDetails = new TaskDetails(uiPages, taskDetailsControls, controller,
        resources);

    loadEntireTaskList();

    uiPages.doResize();

    DeferredCommand.defer(new DeferredCommand() {
      @Override
      public void onExecute() {
        uiPages.doResize();
      }
    }, 100);
  }

  /**
   * Fetches all tasks from the server.
   */
  private void loadEntireTaskList() {
    DeferredCommand.defer(new DeferredCommand() {
      @Override
      public void onExecute() {
        if (!taskList.isLoggedIn()) {
          taskList.notifyNotLoggedIn(null);
        }
      }
    }, 5000);

    api.getTaskList(new AsyncCallback<Task[]>() {

      public void onFailure(Throwable caught) {
        if (caught == null) {
          api.getLoginUrl(new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {
              taskList.notifyNotLoggedIn(null);
            }

            public void onSuccess(String loginUrl) {
              taskList.notifyNotLoggedIn(loginUrl);
            }

          });
        } else {
          taskList.notifyNotLoggedIn(null);
        }
      }

      public void onSuccess(Task[] tasks) {
        if (tasks == null) {
          onFailure(null);
        } else {
          Task metaTask = tasks[0];
          String userEmail = metaTask.getEmail();
          String logoutUrl = metaTask.getDetails();
          taskList.setUserLoggedIn(userEmail, logoutUrl);

          for (int i = 1, n = tasks.length; i < n; i++) {
            Task task = tasks[i];
            taskList.addTaskToUi(task).setRowAsPersisted(task.getId());
          }
        }
      }

    });
  }
}
