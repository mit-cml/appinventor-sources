package com.google.appinventor.client.editor;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.privacy.PrivacyEditorService;
import com.google.appinventor.shared.rpc.privacy.PrivacyEditorServiceAsync;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public final class PrivacyEditor extends Composite {
  
  // UI elements
  private final VerticalPanel pVertPanel;
  private final Button backButton;
  private final HTML optinText;
  private final CheckBox optInCheckbox;
  private final HTML previewTitle;
  private final HTML preview;
  
  // Project elements
  protected final ProjectRootNode projectRootNode;
  protected final long projectId;
  protected final Project project;
  protected final ProjectEditor projectEditor;
  
  public PrivacyEditor(ProjectRootNode projectRootNode) {
    this.projectRootNode = projectRootNode;
    projectId = projectRootNode.getProjectId();
    project = Ode.getInstance().getProjectManager().getProject(projectId);
    projectEditor = Ode.getInstance().getEditorManager().getOpenProjectEditor(projectId);
    
    // Initialize UI
    pVertPanel = new VerticalPanel();      
    pVertPanel.setSpacing(20);
    
    backButton = new Button(MESSAGES.backToDevButton());
    backButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Ode.getInstance().switchToDesignView();
      }
    });
    pVertPanel.add(backButton);
    
    optinText = new HTML("<b>This is where you can opt in or out of the automatic privacy description generation feature. To opt in, please check the box below:</b>");
    pVertPanel.add(optinText);
    
    optInCheckbox = new CheckBox(MESSAGES.optIntoPrivacyNoticeCheckbox()) {
      @Override
      protected void onLoad() {
        // onLoad is called immediately after a widget becomes attached to the browser's document.
        boolean showPreview = Boolean.parseBoolean(
            projectEditor.getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_ATTACH_PRIVACY_DESCRIPTION));
        optInCheckbox.setValue(showPreview);
      }
    };
    optInCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
        projectEditor.changeProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_ATTACH_PRIVACY_DESCRIPTION,
            isChecked ? "True" : "False");
        // Update preview window as needed
        PrivacyEditorServiceAsync privacySvc = Ode.getInstance().getPrivacyEditorService();
        AsyncCallback<String> callback = new AsyncCallback<String>() {
          public void onFailure(Throwable caught) {
            // TODO: Do something with errors
          }
          public void onSuccess(String result) {
            if (optInCheckbox.getValue()) {
              preview.setHTML(result);
            } else {
              preview.setHTML("");
            }
          }
        };
        privacySvc.getPrivacyHTML(projectId, callback);
      }
    });
    pVertPanel.add(optInCheckbox);
    
    previewTitle = new HTML("<h3>Generated Privacy Description Preview</h3>");
    pVertPanel.add(previewTitle);
    
    preview = new HTML();
    preview.addStyleName("privacy-HTML");
    
    // Get PrivacyEditorService proxy
    PrivacyEditorServiceAsync privacySvc = Ode.getInstance().getPrivacyEditorService();
    
    // Set up callback object
    AsyncCallback<String> callback = new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        // TODO: Do something with errors
      }
      public void onSuccess(String result) {
        if (optInCheckbox.getValue()) {
          preview.setHTML(result);
        } else {
          preview.setHTML("");
        }
      }
    };
    
    // Make the call to get the preview of privacy notice
    privacySvc.getPrivacyHTML(projectId, callback);
    
    pVertPanel.add(preview);
    
    initWidget(pVertPanel);
  }
}
