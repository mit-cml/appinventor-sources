// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// This code is unreleased

package com.google.appinventor.client;

import com.google.appinventor.client.admin.AdminComparators;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.shared.rpc.AdminInterfaceException;
import com.google.appinventor.shared.rpc.admin.AdminUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A list of User elements used in the Admin interface
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public class AdminUserList extends Composite {
  private static final Logger LOG = Logger.getLogger(AdminUserList.class.getName());

  private enum SortField {
    NAME,
    VISITED,
  }
  private enum SortOrder {
    ASCENDING,
    DESCENDING,
  }

  // TODO: add these to OdeMessages.java

  private final List<AdminUser> adminUsers;
  private SortField sortField;
  private SortOrder sortOrder;

  // UI elements
  private final Grid table;
  private final Label nameSortIndicator;
  private final Label visitedSortIndicator;

  // Date Time Formatter
  static final DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();

  // Callback to fill in table with user objects
  private final OdeAsyncCallback<List<AdminUser>> searchCallback = new OdeAsyncCallback<List<AdminUser>>(
    "Ooops") {
    @Override
    public void onSuccess(List<AdminUser> newadminUsers) {
      adminUsers.clear();
      for (AdminUser user : newadminUsers) {
        adminUsers.add(user);
      }
      refreshTable(true);
      refreshSortIndicators();
    }

  };

  /**
   * Creates a new AdminUserList
   */
  public AdminUserList() {

    adminUsers = new ArrayList<AdminUser>();

    sortField = SortField.NAME;
    sortOrder = SortOrder.ASCENDING;;

    // Initialize UI
    table = new Grid(1, 4); // The table initially contains just the header row.
    table.addStyleName("ode-ProjectTable");
    table.setWidth("100%");
    table.setCellSpacing(0);
    nameSortIndicator = new Label("");
    visitedSortIndicator = new Label("");
    refreshSortIndicators();
    setHeaderRow();

    HorizontalPanel searchPanel = new HorizontalPanel();
    searchPanel.setSpacing(5);
    final LabeledTextBox searchText = new LabeledTextBox("Enter Email address (or partial)");
    Button searchButton = new Button("Search");
    searchPanel.add(searchText);
    searchPanel.add(searchButton);
    Button addUserButton = new Button("Add User");
    addUserButton.addClickListener(new ClickListener() {
        @Override
        public void onClick(Widget sender) {
          addUpdateUserDialog(null);
        }
      });
    searchPanel.add(addUserButton);

    searchButton.addClickListener(new ClickListener() {
        @Override
        public void onClick(Widget sender) {
          Ode.getInstance().getAdminInfoService().searchUsers(searchText.getText(), searchCallback);
        }
      });

    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");

    panel.add(searchPanel);
    panel.add(table);
    Button dismissButton = new Button("Dismiss");
    dismissButton.addClickListener(new ClickListener() {
        @Override
        public void onClick(Widget sender) {
          Ode.getInstance().switchToDesignView();
        }
      });
    panel.add(dismissButton);
    initWidget(panel);
  }

  /**
   * Adds the header row to the table.
   *
   */
  private void setHeaderRow() {
    table.getRowFormatter().setStyleName(0, "ode-ProjectHeaderRow");

    HorizontalPanel emailHeader = new HorizontalPanel();
    final Label emailHeaderLabel = new Label("User Email");
    emailHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    emailHeader.add(emailHeaderLabel);
    emailHeader.add(nameSortIndicator);
    table.setWidget(0, 0, emailHeader);

    HorizontalPanel uidHeader = new HorizontalPanel();
    final Label uidHeaderLabel = new Label("UID");
    uidHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    uidHeader.add(uidHeaderLabel);
    table.setWidget(0, 1, uidHeader);

    HorizontalPanel adminHeader = new HorizontalPanel();
    final Label adminHeaderLabel = new Label("isAdmin?");
    adminHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    adminHeader.add(adminHeaderLabel);
    table.setWidget(0, 2, adminHeader);

    HorizontalPanel visitedHeader = new HorizontalPanel();
    final Label visitedLabel = new Label("Visited");
    visitedLabel.addStyleName("ode-ProjectHeaderLabel");
    visitedHeader.add(visitedLabel);
    visitedHeader.add(visitedSortIndicator);
    table.setWidget(0, 3, visitedHeader);

    MouseDownHandler mouseDownHandler = new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent e) {
        SortField clickedSortField;
        if (e.getSource() == emailHeaderLabel || e.getSource() == nameSortIndicator) {
          clickedSortField = SortField.NAME;
        } else if (e.getSource() == visitedLabel || e.getSource() == visitedSortIndicator) {
          clickedSortField = SortField.VISITED;
        } else {
          return;
        }
        changeSortOrder(clickedSortField);
      }
    };
    emailHeaderLabel.addMouseDownHandler(mouseDownHandler);
    nameSortIndicator.addMouseDownHandler(mouseDownHandler);
    visitedLabel.addMouseDownHandler(mouseDownHandler);
    visitedSortIndicator.addMouseDownHandler(mouseDownHandler);
  }

  private void changeSortOrder(SortField clickedSortField) {
    if (sortField != clickedSortField) {
      sortField = clickedSortField;
      sortOrder = SortOrder.ASCENDING;
    } else {
      if (sortOrder == SortOrder.ASCENDING) {
        sortOrder = SortOrder.DESCENDING;
      } else {
        sortOrder = SortOrder.ASCENDING;
      }
    }
    refreshTable(true);
  }

  private void refreshSortIndicators() {
    String text = (sortOrder == SortOrder.ASCENDING)
        ? "\u25B2"      // up-pointing triangle
        : "\u25BC";     // down-pointing triangle
    switch (sortField) {
      case NAME:
        nameSortIndicator.setText(text);
        visitedSortIndicator.setText("");
        break;
    case VISITED:
        nameSortIndicator.setText("");
        visitedSortIndicator.setText(text);
        break;
    }
  }

  private class UserWidgets {
    final Label nameLabel;
    final Label uidLabel;
    final Label visitedLabel;
    final Label isAdminLabel;

    private UserWidgets(final AdminUser user) {
      nameLabel = new Label(user.getEmail());
      nameLabel.addStyleName("ode-ProjectNameLabel");
      uidLabel = new Label(user.getId());
      Date visited = user.getVisited();
      if (visited == null) {
        visitedLabel = new Label("<never>");
      } else {
        visitedLabel = new Label(dateTimeFormat.format(user.getVisited()));
      }
      boolean isAdmin = user.getIsAdmin();
      if (!isAdmin) {
        isAdminLabel = new Label("No");
      } else {
        isAdminLabel = new Label("Yes");
      }
      nameLabel.addMouseDownHandler(new MouseDownHandler() {
          @Override
          public void onMouseDown(MouseDownEvent e) {
            addUpdateUserDialog(user);
          }
        });
    }
  }

  // TODO(user): This method was made public so it can be called
  // directly from from Ode when the AdminUserList view is selected
  // from another view.
  public void refreshTable(boolean needToSort) {
    if (needToSort) {
      // Sort the projects.
      Comparator<AdminUser> comparator;
      switch (sortField) {
        default:
        case NAME:
          comparator = (sortOrder == SortOrder.ASCENDING)
            ? AdminComparators.COMPARE_BY_NAME_ASCENDING
            : AdminComparators.COMPARE_BY_NAME_DESCENDING;
          break;
        case VISITED:
          comparator = (sortOrder == SortOrder.ASCENDING)
            ? AdminComparators.COMPARE_BY_VISTED_DATE_ASCENDING
            : AdminComparators.COMPARE_BY_VISTED_DATE_DESCENDING;
          break;
      }
      Collections.sort(adminUsers, comparator);
    }

    refreshSortIndicators();

    // Refill the table.
    table.resize(1 + adminUsers.size(), 4);
    int row = 1;
    for (AdminUser user : adminUsers) {
      UserWidgets uw = new UserWidgets(user);
      table.setWidget(row, 0, uw.nameLabel);
      table.setWidget(row, 1, uw.uidLabel);
      table.setWidget(row, 2, uw.isAdminLabel);
      table.setWidget(row, 3, uw.visitedLabel);
      row++;
    }

  }

  private void addUpdateUserDialog(final AdminUser user) {
    boolean adding = true;
    if (user != null) {         // Adding a user
      adding = false;
    }
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setStylePrimaryName("ode-DialogBox");
    if (adding) {
      dialogBox.setText("Add User");
    } else {
      dialogBox.setText("Update User");
    }
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    final HTML message = new HTML("");
    message.setStyleName("DialogBox-message");
    final FlexTable userInfo = new FlexTable(); // Holds the username and password labels and boxes
    final Label userNameLabel = new Label("User Name");
    final TextBox userName = new TextBox();
    final Label passwordLabel = new Label("Password");
    final Label passwordLabel2 = new Label("Password (again)");
    final TextBox passwordBox = new TextBox();
    // We switch to the ones below if the hidePasswordCheckbox is selected
    // otherwise we use the passwordBox above for the password
    final PasswordTextBox passwordBox1 = new PasswordTextBox();
    final PasswordTextBox passwordBox2 = new PasswordTextBox();
    userInfo.setWidget(0, 0, userNameLabel);
    userInfo.setWidget(0, 1, userName);
    userInfo.setWidget(1, 0, passwordLabel);
    userInfo.setWidget(1, 1, passwordBox);

    final CheckBox isAdminBox = new CheckBox("Is Admin?");
    final CheckBox hidePasswordCheckbox = new CheckBox("Hide Password");
    final HorizontalPanel checkboxPanel = new HorizontalPanel();
    checkboxPanel.add(isAdminBox);
    checkboxPanel.add(hidePasswordCheckbox);

    VerticalPanel vPanel = new VerticalPanel();
    vPanel.add(message);
    vPanel.add(userInfo);
    vPanel.add(checkboxPanel);
    HorizontalPanel buttonPanel = new HorizontalPanel();
    Button okButton = new Button("OK");
    buttonPanel.add(okButton);
    hidePasswordCheckbox.addClickListener(new ClickListener() {
        @Override
        public void onClick(Widget sender) {
          if (hidePasswordCheckbox.isChecked()) { // We just asked to mask passwords
            userInfo.setWidget(1, 0, passwordLabel);
            userInfo.setWidget(1, 1, passwordBox1);
            userInfo.setWidget(2, 0, passwordLabel2);
            userInfo.setWidget(2, 1, passwordBox2);
          } else {              // Unchecked, passwords in the clear
            userInfo.setWidget(1, 0, passwordLabel);
            userInfo.setWidget(1, 1, passwordBox);
            userInfo.removeRow(2);
          }
        }
      });
    okButton.addClickListener(new ClickListener() {
        @Override
        public void onClick(Widget sender) {
          String password = passwordBox.getText();
          if (hidePasswordCheckbox.isChecked()) {
            password = passwordBox1.getText();
            String checkPassword = passwordBox2.getText();
            if (!checkPassword.equals(password)) {
              message.setHTML("<font color=red>Passwords do not match.</font>");
              return;
            }
          }
          String email = userName.getText();
          if (email.equals("")) {
            message.setHTML("<font color=red>You Must Supply a user name (email address)</font>");
            return;
          } else {
            // Work!!
            AdminUser nuser = user;
            if (nuser == null) {
              nuser = new AdminUser(null, email, email, false, isAdminBox.isChecked(), null);
            } else {
              nuser.setIsAdmin(isAdminBox.isChecked());
              nuser.setEmail(email);
            }
            nuser.setPassword(password);
            Ode.getInstance().getAdminInfoService().storeUser(nuser,
              new OdeAsyncCallback<Void> ("Oops") {
                @Override
                public void onSuccess(Void v) {
                  dialogBox.hide();
                }
                @Override
                public void onFailure(Throwable error) {
                  LOG.log(Level.SEVERE, "Exception updating user", error);
                  if (error instanceof AdminInterfaceException) {
                    ErrorReporter.reportError(error.getMessage());
                  } else {
                    super.onFailure(error);
                  }
                  dialogBox.hide();
                }
              });
          }
        }
      });
    Button cancelButton = new Button("Cancel");
    buttonPanel.add(cancelButton);
    cancelButton.addClickListener(new ClickListener() {
        @Override
        public void onClick(Widget sender) {
          dialogBox.hide();
        }
      });
    vPanel.add(buttonPanel);
    dialogBox.setWidget(vPanel);
    if (!adding) {
      isAdminBox.setChecked(user.getIsAdmin());
      userName.setText(user.getEmail());
    }
    // switchUserPanel -- Put up a button to permit us to
    // switch to the selected user, but readonly
    if (!adding) {
      HorizontalPanel switchUserPanel = new HorizontalPanel();
      Button switchButton = new Button("Switch to This User");
      switchButton.addClickListener(new ClickListener() {
          @Override
          public void onClick(Widget sender) {
            Ode.getInstance().setReadOnly();  // Must make sure we are read only.
                                              // When we call reloadWindow (below) the onClosing
                                              // handler in Ode will be called. It will attempt
                                              // to save the project settings. But by the time we
                                              // return below, we have switched accounts, so the settings
                                              // will be saved in the wrong account(!!). So we set the
                                              // read-only flag now!
            Ode.getInstance().getAdminInfoService().switchUser(user, new OdeAsyncCallback<Void>("Oops") {
                @Override
                public void onSuccess(Void v) {
                  Ode.getInstance().reloadWindow(false);
                }
              });
          }
        });
      switchUserPanel.add(switchButton);
      vPanel.add(switchUserPanel);
    }
    dialogBox.center();
    dialogBox.show();
  }
}

