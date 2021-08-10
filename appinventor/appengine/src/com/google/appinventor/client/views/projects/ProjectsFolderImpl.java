package com.google.appinventor.client.views.projects;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import com.google.appinventor.client.components.Icon;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.project.Project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectsFolderImpl extends Composite implements ProjectsFolder {
  interface ProjectsFolderImplUiBinder extends UiBinder<FlowPanel, ProjectsFolderImpl> {}
  private static final ProjectsFolderImplUiBinder UI_BINDER = GWT.create(ProjectsFolderImplUiBinder.class);

  @UiField(provided=true)
  Resources.ProjectsFolderStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.projectsFolderStyleDark() : Resources.INSTANCE.projectsFolderStyleLight();

  @UiField FlowPanel container;
  @UiField FlowPanel childrenContainer;
  @UiField Label nameLabel;
  @UiField Label dateModifiedLabel;
  @UiField Label dateCreatedLabel;
  @UiField CheckBox checkBox;
  @UiField Icon toggleButton;

  // private ProjectsFolder parentFolder;
  private Folder folder;
  private ItemSelectionChangeHandler changeHandler;
  private ClickHandler clickHandler;
  private boolean isExpanded;
  private boolean isInTrash;
  private int depth;

  private List<ProjectsFolder> folderListItems;
  private List<ListItem> projectListItems;
  private List<Folder> selectedFolders;
  private List<Project> selectedProjects;

  public ProjectsFolderImpl() {
    folderListItems = new ArrayList<ProjectsFolder>();
    projectListItems = new ArrayList<ListItem>();
    selectedFolders = new ArrayList<Folder>();
    selectedProjects = new ArrayList<Project>();
    style.ensureInjected();
    initWidget(UI_BINDER.createAndBindUi(this));
  }

  public ProjectsFolderImpl(Folder folder, boolean isInTrash, int depth,
      ItemSelectionChangeHandler changeHandler) {
    this();
    DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();

    Date dateCreated = new Date(folder.getDateCreated());
    Date dateModified = new Date(folder.getDateModified());

    nameLabel.setText(folder.getName());
    dateModifiedLabel.setText(dateTimeFormat.format(dateModified));
    dateCreatedLabel.setText(dateTimeFormat.format(dateCreated));
    this.folder = folder;
    this.changeHandler = changeHandler;
    this.isInTrash = isInTrash;
    this.depth = depth;
    checkBox.getElement().setAttribute("style", "margin-right: " + (depth * 10) + "px");
    refresh();
  }

  public boolean isSelected() {
    return checkBox.getValue();
  }

  public void setClickHandler(ClickHandler handler) {
    this.clickHandler = handler;
  }

  public Folder getFolder() {
    return folder;
  }

  @Override
  public void setSelected(boolean selected) {
  }

  @Override
  public void refresh() {
    childrenContainer.clear();
    for (Folder childFolder : folder.getChildFolders()) {
      ProjectsFolderImpl item = createProjectsFolder(childFolder);
      folderListItems.add(item);
      childrenContainer.add(item);
    }
  }

  @Override
  public void setFolder(Folder folder) {

  }

  @Override
  public List<Project> getSelectedProjects() {
    return selectedProjects;
  }

  @Override
  public List<Folder> getSelectedFolders() {
    return selectedFolders;
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  @UiHandler("checkBox")
  void toggleItemSelection(ClickEvent e) {
    setSelected(checkBox.getValue());
    changeHandler.onSelectionChange(checkBox.getValue());
  }

  @UiHandler("toggleButton")
  void toggleExpandedState(ClickEvent e) {
    isExpanded = !isExpanded;
    if (isExpanded) {
      toggleButton.setIcon("expand_more");
      childrenContainer.addStyleName(style.childrenContainerExpanded());
      checkBox.addStyleName(style.selectionHidden());
    } else {
      toggleButton.setIcon("chevron_right");
      childrenContainer.removeStyleName(style.childrenContainerExpanded());
      checkBox.removeStyleName(style.selectionHidden());
    }
  }

  @UiHandler("nameLabel")
  void itemClicked(ClickEvent e) {
    toggleExpandedState(e);
  }

  private ProjectsFolderImpl createProjectsFolder(final Folder folder) {
    ProjectsFolderImpl projectsFolder = new ProjectsFolderImpl(folder, isInTrash, depth + 1, new ProjectsFolderImpl.ItemSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {}
    });
    return projectsFolder;
  }

  public static abstract class ItemSelectionChangeHandler {
    public abstract void onSelectionChange(boolean selected);
  }
}
