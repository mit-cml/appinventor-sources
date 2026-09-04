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

import com.google.appengine.demos.taskengine.shared.Task;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * API for interacting with our tasks backend.
 */
@RemoteServiceRelativePath("api")
public interface TasksApi extends RemoteService {
  /**
   * Deletes records for the specified tasks.
   * 
   * @param tasksToDelete the IDs of tasks to delete
   * @return the key of the last task deleted
   */
  String deleteTasks(String[] tasksToDelete);

  /**
   * Fetches login URL for users who are not logged in.
   * 
   * @return the login URL
   */
  String getLoginUrl();

  /**
   * Gets all tasks that have been persisted.
   * 
   * @return
   */
  Task[] getTaskList();
  
  /**
   * Saves a Task to disk and gets back a server generated key as confirmation
   * of the persistence.
   * 
   * @param task the TaskData for the task we want to persist
   * @return the JDO generated key for the task
   */
  String persistTask(Task task);
}
