package com.google.appinventor.client.views.projects;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeMessages;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.folder.FolderManagerEventListener;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;

import java.util.ArrayList;
import java.util.List;

public class ProjectsList extends Composite implements ProjectsFolder,
      FolderManagerEventListener {
  interface ProjectsListUiBinder extends UiBinder<FlowPanel, ProjectsList> {}
  private static final ProjectsListUiBinder UI_BINDER = GWT.create(ProjectsListUiBinder.class);

  @UiField FlowPanel container;
  @UiField FlowPanel header;
  @UiField CheckBox checkBox;

  @UiField(provided=true)
  Resources.ProjectsListStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.projectsListStyleDark() : Resources.INSTANCE.projectsListStyleLight();

  private List<ProjectsFolder> folderListItems;
  private List<ListItem> projectListItems;
  private List<Folder> selectedFolders;
  private List<Project> selectedProjects;
  private Folder rootFolder;
  private boolean trashList;

  private SelectionChangeHandler selectionChangeHandler;

  public ProjectsList() {
    folderListItems = new ArrayList<ProjectsFolder>();
    projectListItems = new ArrayList<ListItem>();
    selectedFolders = new ArrayList<Folder>();
    selectedProjects = new ArrayList<Project>();
    style.ensureInjected();
    initWidget(UI_BINDER.createAndBindUi(this));
    Ode.getInstance().getFolderManager().addFolderManagerEventListener(this);
  }

  public void setSelectionChangeHandler(SelectionChangeHandler handler) {
    this.selectionChangeHandler = handler;
  }

  public void setForTrash(boolean forTrash) {
    trashList = forTrash;
    if (trashList) {
      setFolder(Ode.getInstance().getFolderManager().getTrashFolder());
    } else {
      setFolder(Ode.getInstance().getFolderManager().getGlobalFolder());
    }
    if (rootFolder != null) {
      refresh();
    }
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  @UiHandler("checkBox")
  void toggleItemSelection(ClickEvent e) {
    setSelected(checkBox.getValue());
  }

  // FolderManagerEventListener implementation
  @Override
  public void onFolderAdded(Folder folder) {
    refresh();
  }

  @Override
  public void onFolderRemoved(Folder folder) {
    refresh();
  }

  @Override
  public void onFolderRenamed(Folder folder) {
    refresh();
  }

  @Override
  public void onFoldersLoaded() {
    setForTrash(trashList);
  }

  // ProjectsFolder implementation

  @Override
  public void setFolder(Folder root) {
    rootFolder = root;
    refresh();
  }

  @Override
  public Folder getFolder() {
    return rootFolder;
  }

  @Override
  public List<Project> getSelectedProjects() {
    return selectedProjects;
  }

  @Override
  public List<Folder> getSelectedFolders() {
    return selectedFolders;
  }

  @Override
  public void refresh() {
    OdeLog.log("Got call to refresh");
    container.clear();
    container.add(header);
    projectListItems.clear();
    folderListItems.clear();

    for (final Folder folder : rootFolder.getChildFolders()) {
      if ("*trash*".equals(folder.getName())) continue;
      ProjectsFolderImpl item = createProjectsFolder(folder);
      folderListItems.add(item);
      container.add(item);
    }

    for(final Project project : rootFolder.getProjects()) {
      ListItem item = createListItem(project);
      projectListItems.add(item);
      container.add(item);
    }
  }

  @Override
  public void setSelected(boolean selectionState) {
    checkBox.setValue(selectionState);
    selectedProjects.clear();
    selectedFolders.clear();
    for(ListItem item : projectListItems) {
      item.setSelected(selectionState);
      if(selectionState) {
        selectedProjects.add(item.getProject());
      }
    }
    for(ProjectsFolder item : folderListItems) {
      item.setSelected(selectionState);
      if(selectionState) {
        selectedFolders.add(item.getFolder());
      }
    }
    if(selectionChangeHandler != null) selectionChangeHandler.onSelectionChange(selectedProjects.size(), selectedFolders.size());
  }

  public abstract static class SelectionChangeHandler {
    public abstract void onSelectionChange(int selectedProjects, int selectedFoders);
  }

  private ListItem createListItem(final Project project) {
    ListItem projectListItem = new ListItem(project, trashList, 0, new ListItem.ItemSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {
        if(selected) {
          selectedProjects.add(project);
        } else {
          selectedProjects.remove(project);
        }
        checkBox.setValue(false);
        // if(selectedProjects.size() == projects.size()) {
        //   checkBox.setValue(true);
        // }
        if(selectionChangeHandler != null) selectionChangeHandler.onSelectionChange(selectedProjects.size(), selectedFolders.size());
      }
    });

    if(!trashList) {
      projectListItem.setClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent e) {
          if(!project.isInTrash())
            Ode.getInstance().openYoungAndroidProjectInDesigner(project);
        }
      });
    }

    return projectListItem;
  }

  private ProjectsFolderImpl createProjectsFolder(final Folder folder) {
    ProjectsFolderImpl projectsFolder = new ProjectsFolderImpl(folder, trashList, 0, new ProjectsFolderImpl.ItemSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {}
    });
    return projectsFolder;
  }
}
