package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.PrivacyEditor;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PrivacyViewerBox extends Box {
  // Singleton privacy viewer box instance
  private static final PrivacyViewerBox INSTANCE = new PrivacyViewerBox();

  /**
   * Return the privacy singleton viewer box.
   *
   * @return  privacy viewer box
   */
  public static PrivacyViewerBox getViewerBox() {
    return INSTANCE;
  }

  /**
   * Creates new empty viewer box.
   */
  private PrivacyViewerBox() {
    super(MESSAGES.privacyViewerBoxCaption(),
        600,    // height
        false,  // minimizable
        false); // removable
  }
  
  /**
   * Shows the privacy notice content associated with the given project in the privacy editor viewer.
   *
   * @param projectRootNode  the root node of the project to show in the privacy editor viewer
   */
  public void show(ProjectRootNode projectRootNode) {
    setContent(new PrivacyEditor(projectRootNode));
  }
}
