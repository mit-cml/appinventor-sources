// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import static com.google.appinventor.client.Ode.MESSAGES;

public class DeleteAccountAction implements Command {
  private static final String SIGNOUT_URL = "/ode/_logout";
  @Override
  public void execute() {
    if(ProjectListBox.getProjectListBox().getProjectList().getMyProjectsCount() > 0) {
      Ode.getInstance().genericWarning(MESSAGES.warnHasProjects());
    } else {
      Ode.getInstance().getUserInfoService().deleteAccount(
          new OdeAsyncCallback<String>(MESSAGES.accountDeletionFailed()) {
            @Override
            public void onSuccess(String delAccountUrl) {
              if (delAccountUrl.equals("")) {
                Ode.getInstance().genericWarning(MESSAGES.warnHasProjects());
              } else {
                if (delAccountUrl.equals("NONE")) {
                  Window.Location.replace(SIGNOUT_URL);
                } else {
                  Window.Location.replace(delAccountUrl);
                }
              }
            }
          });
    }
    return;
  }}
