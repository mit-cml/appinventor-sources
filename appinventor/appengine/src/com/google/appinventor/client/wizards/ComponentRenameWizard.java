package com.google.appinventor.client.wizards;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;


public class ComponentRenameWizard extends Wizard{

    private final static Ode ode = Ode.getInstance();

    private LabeledTextBox renameTextBox;

    private String defaultTypeName;
    private String defaultName;

    private static class RenameComponentCallback extends OdeAsyncCallback<List<ProjectNode>> {
        @Override
        public void onSuccess(List<ProjectNode> compNodes) {
            if (compNodes.isEmpty())  return;
            long projectId = ode.getCurrentYoungAndroidProjectId();
            Project project = ode.getProjectManager().getProject(projectId);
            YoungAndroidComponentsFolder componentsFolder = ((YoungAndroidProjectNode) project.getRootNode()).getComponentsFolder();
            YaProjectEditor projectEditor = (YaProjectEditor) ode.getEditorManager().getOpenProjectEditor(projectId);

            for (ProjectNode node : compNodes) {
                project.addNode(componentsFolder,node);
                if (node.getName().endsWith(".json") && StringUtils.countMatches(node.getFileId(), "/") == 3) {
                    projectEditor.addComponent(node, null);
                }
            }

        }
    }




    protected ComponentRenameWizard(String defaultTypeName) {
        super(MESSAGES.componentRenameWizardCaption(), true, false);

        this.defaultTypeName = defaultTypeName;
        this.defaultName = getDefaultName(defaultTypeName);

        setStylePrimaryName("ode-DialogBox");

        renameTextBox = new LabeledTextBox(MESSAGES.componentNameLabel());
        renameTextBox.getTextBox().addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                int keyCode = event.getNativeKeyCode();
                if (keyCode == KeyCodes.KEY_ENTER) {
                    handleOkClick();
                } else if (keyCode == KeyCodes.KEY_ESCAPE) {
                    handleCancelClick();
                }
            }
        });

        renameTextBox.setText(defaultName);

        VerticalPanel page = new VerticalPanel();

        page.add(renameTextBox);
        addPage(page);

        // Create finish command (rename Extension)
        initFinishCommand(new Command() {
            @Override
            public void execute() {
                String newName = renameTextBox.getText();

                if (TextValidators.checkNewComponentName(newName)) {
                    // Call RenameImportedComponent

                } else {
                    show();
                    center();
                    renameTextBox.setFocus(true);
                    renameTextBox.selectAll();
                    return;
                }
            }
        });
        // Create cancel command (delete component files)
        initCancelCommand(new Command() {
            @Override
            public void execute() {

            }
        });
    }

    private static String getDefaultName(String defaultTypeName) {
        return defaultTypeName.substring(defaultTypeName.lastIndexOf('.') + 1);
    }



    @Override
    public void show() {
        super.show();
        int width = 320;
        int height = 40;
        this.center();

        setPixelSize(width, height);
        super.setPagePanelHeight(40);

        DeferredCommand.addCommand(new Command() {
            public void execute() {
                renameTextBox.setFocus(true);
            }
        });
    }
}
