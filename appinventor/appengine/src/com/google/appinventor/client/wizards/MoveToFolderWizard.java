package com.google.appinventor.client.wizards;

import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.appinventor.client.Ode.MESSAGES;

public class MoveToFolderWizard extends Wizard {

  // UI element for project name
  private Tree folderTree;
  private VerticalPanel page;

  /**
   * Creates a new folder wizard.
   */
  public MoveToFolderWizard() {
    super(MESSAGES.chooseFolderWizardCaption(), true, false);

    // Initialize the UI
    setStylePrimaryName("ode-DialogBox");

    folderTree = new Tree();
    folderTree.setStylePrimaryName("gwt-Tree");
    Set<String> folderSet = ProjectListBox.getProjectListBox().getProjectList().getFolders();
    folderSet.remove(null);
    ArrayList<String> folderList = new ArrayList<>(folderSet);

    Collections.sort(folderList);
    HashMap<String, TreeItem> treeBuilder = new HashMap<>();
    Set<String> leftovers = new TreeSet<>();
    TreeItem root = new TreeItem();
    root.addStyleName("gwt-TreeItem");
    root.setText(MESSAGES.myProjectsTabName());
    root.setTitle(null);

    for (String folderPath : folderList) {
      if (folderPath != null) {
        TreeItem node = treeBuilder.get(folderPath);
        if (node == null) {
          int parentLength = folderPath.lastIndexOf("/");
          if (parentLength == -1) {
            // This is a top folder
            node = root.addTextItem(folderPath);
            node.setTitle(folderPath);
            node.addStyleName("gwt-TreeItem");
            treeBuilder.put(folderPath, node);
          } else {
            String holder = folderPath.substring(0, parentLength);
            TreeItem parentNode = treeBuilder.get(holder);
            if (parentNode == null) {
              // TODO: Throw error. Since the set is sorted, this shouldn't happen.
              leftovers.add(folderPath);
            } else {
              node = parentNode.addTextItem(folderPath.substring(folderPath.lastIndexOf("/") + 1));
              node.setTitle(folderPath);
              node.addStyleName("gwt-TreeItem");
              treeBuilder.put(folderPath, node);
            }
          }
        }
      }
    }

    page = new VerticalPanel();
    root.setState(true);  // Set tree to expanded
    folderTree.addItem(root);
    page.add(folderTree);
    addPage(page);
    page.setHeight("280px");
    enableOkButton();

    initFinishCommand(new Command() {
                        @Override
                        public void execute() {
                          String destinationPath = folderTree.getSelectedItem().getTitle();
                          if (destinationPath.length() == 0)  // Looks luke null might be converted to empty
                            destinationPath = null;
                          List<Project> selectedProjects =
                              ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
                          for (Project project : selectedProjects) {
                            ProjectListBox.getProjectListBox().getProjectList().handleProjectMove(project.getProjectId(),
                                destinationPath);
                          }
                          List<String> selectedFolders =
                              ProjectListBox.getProjectListBox().getProjectList().getSelectedFolders();
                          for (String folder : selectedFolders) {
                            ProjectListBox.getProjectListBox().getProjectList().handleFolderMove(folder,
                                destinationPath);
                          }
                          ProjectListBox.getProjectListBox().getProjectList().refreshTable(false);
                        }
                      }

    );
  }

  @Override
  public void show() {
    super.show();
    this.center();
    super.setPagePanelHeight(300);
  }
}
