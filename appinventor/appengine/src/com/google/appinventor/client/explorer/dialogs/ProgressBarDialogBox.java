package com.google.appinventor.client.explorer.dialogs;

import com.google.appinventor.client.explorer.commands.MiniProgressBar;
import com.google.appinventor.client.explorer.commands.ShowProgressBarCommand;
import com.google.appinventor.client.widgets.DialogBox;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * A dialog containing a progress bar. This was originally defined in
 * {@link ShowProgressBarCommand}, but has utility in other parts of App Inventor so it was moved
 * to its own class.
 */
public class ProgressBarDialogBox extends DialogBox {
  private static final String LOADING_ASSETS_ACTION = "LoadingAssetsAction";
  // 0 means just initialize, 1 means click once, 2 means click twice
  private int progressBarShow = 0;
  private Button dismissButton = new Button(MESSAGES.dismissButton());
  private HTML warningLabel;
  private MiniProgressBar mpb = new MiniProgressBar(0, 100, 0);
  private String serviceName;

  //constructor
  public ProgressBarDialogBox(String serviceName, ProjectNode projectNode) {
    super(false, true);
    this.serviceName = serviceName;
    setStylePrimaryName("ode-DialogBox");
    setText(projectNode.getName() + " " + MESSAGES.ProgressBarFor());

    //click handler for the mini html buttons
    ClickHandler buttonHandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
        progressBarShow++;
      }
    };

    //declare the ok button
    dismissButton.addClickHandler(buttonHandler);
    FlowPanel buttonPanel = new FlowPanel();
    buttonPanel.setStyleName("ode-HorizontalLayout");
    buttonPanel.addStyleName("ode-DialogButtons");
    buttonPanel.addStyleName("ode-DialogButtons-centered");
    dismissButton.setVisible(false); // we don't need the button unless we get an error

    //warning label
    warningLabel = new HTML("");
    warningLabel.setWordWrap(true);
    warningLabel.setWidth("60em");  // set width to get the text to wrap

    //warning panel
    FlowPanel warningPanel = new FlowPanel();
    warningPanel.setStyleName("ode-HorizontalLayout");
    warningPanel.add(warningLabel);

    // button panel
    buttonPanel.add(dismissButton);

    //content panel
    FlowPanel contentPanel = new FlowPanel();
    contentPanel.setStyleName("ode-VerticalLayout");
    contentPanel.add(mpb);
    contentPanel.add(warningPanel);
    contentPanel.add(buttonPanel);
    setWidget(contentPanel);
  }

  public void clear() {
    warningLabel.setHTML("");
  }

  public void setProgress(int progress, String message) {
    mpb.setProgress(progress);
    warningLabel.setHTML(message);
  }

  public void showDismissButton() {
    dismissButton.setVisible(true);
  }

  public int getProgressBarShow() {
    return progressBarShow;
  }
}
