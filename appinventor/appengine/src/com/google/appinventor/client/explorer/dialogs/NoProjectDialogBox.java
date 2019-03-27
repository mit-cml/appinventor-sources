package com.google.appinventor.client.explorer.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.wizards.TemplateUploadWizard;
import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class NoProjectDialogBox extends DialogBox {

    private static NoProjectDialogBoxUiBinder uiBinder =
    GWT.create(NoProjectDialogBoxUiBinder.class);

    interface NoProjectDialogBoxUiBinder extends UiBinder<Widget, NoProjectDialogBox> {};

    @UiField Button closeDialogBox;
    @UiField Button goToPurr;
    @UiField Button goToTalk;
    @UiField Button goToYR;

    public NoProjectDialogBox() {
        this.setStylePrimaryName("ode-noDialogDiv");
        add(uiBinder.createAndBindUi(this));
        this.center();
        this.setAnimationEnabled(true);

        closeDialogBox.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                closeDialogBox();
            }
        });

        goToPurr.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                goToPurrTutorial();
            }
        });

        goToTalk.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                goToTalkTutorial();
            }
        });

        goToYR.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                goToYRTutorial();
            }
        });

    }

    void closeDialogBox() {
        this.hide();
    }

    void goToPurrTutorial() {
        this.hide();

        NewProjectCommand callbackCommand = new NewProjectCommand() {
            @Override
            public void execute(Project project) {
                Ode.getInstance().openYoungAndroidProjectInDesigner(project);
            }
        };

        new TemplateUploadWizard().createProjectFromExistingZip("HelloPurr", callbackCommand);
    }

    void goToTalkTutorial() {
        this.hide();

        NewProjectCommand callbackCommand = new NewProjectCommand() {
            @Override
            public void execute(Project project) {
                Ode.getInstance().openYoungAndroidProjectInDesigner(project);
            }
        };

        TemplateUploadWizard.openProjectFromTemplate("http://appinventor.mit.edu/yrtoolkit/yr/aiaFiles/talk_to_me/TalkToMe.asc", callbackCommand);
    }

    void goToYRTutorial() {
        this.hide();

        NewProjectCommand callbackCommand = new NewProjectCommand() {
            @Override
            public void execute(Project project) {
                Ode.getInstance().openYoungAndroidProjectInDesigner(project);
            }
        };

        TemplateUploadWizard.openProjectFromTemplate("http://appinventor.mit.edu/yrtoolkit/yr/aiaFiles/hello_bonjour/translate_tutorial.asc", callbackCommand);
    };
}
