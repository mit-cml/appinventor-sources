// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;


public class ComponentRenameWizard extends Wizard{

    private final static Ode ode = Ode.getInstance();

    private LabeledTextBox renameTextBox;
    private final List<ProjectNode> nodes;

    private String defaultTypeName;
    private String defaultName;
    private long destinationProjectId;

    private class RenameComponentCallback extends OdeAsyncCallback<Void> {
        @Override
        public void onSuccess(Void result) {
            if (nodes.isEmpty())  return;
            Project project = ode.getProjectManager().getProject(destinationProjectId);
            if (project == null) {
                return;
            }
            YoungAndroidComponentsFolder componentsFolder = ((YoungAndroidProjectNode) project.getRootNode()).getComponentsFolder();
            YaProjectEditor projectEditor = (YaProjectEditor) ode.getEditorManager().getOpenProjectEditor(destinationProjectId);
            if (projectEditor == null) {
                return;
            }
            for (ProjectNode node : nodes) {
                project.addNode(componentsFolder, node);
                if (node.getName().equals("component.json") && StringUtils.countMatches(node.getFileId(), "/") == 3) {
                    projectEditor.addComponent(node, null);
                }
            }
        }
    }




    protected ComponentRenameWizard(final String defaultTypeName, long projectId, final List<ProjectNode> compNodes) {
        super(MESSAGES.componentRenameWizardCaption(), true, false);

        this.defaultTypeName = defaultTypeName;
        this.defaultName = getDefaultName(defaultTypeName);
        this.destinationProjectId = projectId;
        this.nodes = compNodes;

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
                    ode.getComponentService().renameImportedComponent(defaultTypeName, newName, destinationProjectId, new RenameComponentCallback());

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
                ode.getComponentService().deleteImportedComponent(defaultTypeName, destinationProjectId, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {

                    }

                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
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
