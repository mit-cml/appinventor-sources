// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A wizard controls a dialog box that cycles through a series of pages.
 * <p>
 * The user may decide to cancel the wizard before viewing or completing
 * all of the pages. In that case, the wizard is closed and no further
 * action is taken.
 * <p>
 * After all pages have been cycled through, the user may invoke
 * a finish command. Immediately before the command is invoked,
 * the wizard dialog will be closed automatically.
 *
 */
public abstract class Wizard extends DialogBox {
  // UI for button panel to switch between wizard pages
  private final HorizontalPanel buttonPanel;
  private final Button backButton;
  private final Button cancelButton;
  private final Button nextButton;
  private final Button okButton;

  // Wizard pages
  private final AbsolutePanel pagePanel;
  private final DeckPanel pageDeck;
  private int currentPageIndex;

  // Command to execute upon finishing the wizard (not executed on cancel)
  private Command finishCommand;

  // Command to execute upon canceling the wizard (can be null)
  private Command cancelCommand;

  // Indicates whether the browser area size should be considered when calculating the wizard size
  private final boolean adaptiveSizing;

  // Indicates modality of the wizard
  private final boolean modal;

  /**
   * Creates a new wizard.
   * <p>
   * Implementations are expected to build the wizard dialog in their
   * constructor. In particular, it is expected that
   * {@link #addPage(Panel)} and {@link #initFinishCommand(Command)}
   * will be called before the constructor terminates.
   *
   * @param title title displayed in wizard dialog box
   * @param modal indicates modality of the wizard
   * @param adaptiveSizing instead of using the minimal size for the
   *                       wizard also considers the size of the browser area
   */
  protected Wizard(String title, boolean modal, boolean adaptiveSizing) {
    // Initialize UI
    // TODO(lizlooney) - investigate using built-in modality support. The
    // reasons for not using it initially are no longer valid.
    super(false, false);

    this.modal = modal;
    this.adaptiveSizing = adaptiveSizing;

    setStylePrimaryName("ode-DialogBox");
    setText(title);

    ClickListener buttonListener = new ClickListener() {
      @Override
      public void onClick(Widget sender) {
        if (sender == cancelButton) {
          handleCancelClick();
        } else if (sender == nextButton) {
          showNextPage();
        } else if (sender == backButton) {
          showPreviousPage();
        } else if (sender == okButton) {
          handleOkClick();
        }
      }
    };
    cancelButton = new Button(MESSAGES.cancelButton());
    cancelButton.addClickListener(buttonListener);
    backButton = new Button(MESSAGES.backButton());
    backButton.addClickListener(buttonListener);
    nextButton = new Button(MESSAGES.nextButton());
    nextButton.addClickListener(buttonListener);
    okButton = new Button(MESSAGES.okButton());
    okButton.addClickListener(buttonListener);

    buttonPanel = new HorizontalPanel();
    buttonPanel.add(cancelButton);
    buttonPanel.add(backButton);
    buttonPanel.add(nextButton);
    buttonPanel.add(okButton);
    buttonPanel.setSize("100%", "24px");

    pageDeck = new DeckPanel();
    pageDeck.setSize("100%", "100%");

    pagePanel = new AbsolutePanel();
    pagePanel.add(pageDeck);
    pagePanel.setWidth("100%");

    VerticalPanel contentPanel = new VerticalPanel();
    contentPanel.add(pagePanel);
    contentPanel.add(buttonPanel);
    contentPanel.setSize("100%", "100%");

    add(contentPanel);
  }

  @Override
  public boolean onEventPreview(Event event) {
    // Always allow event if capturing is enabled
    if (DOM.getCaptureElement() != null) {
      return true;
    }

    // If this is a modal wizard then only allow it if the target element is a child of this wizard
    if (modal) {
      Element target = DOM.eventGetTarget(event);
      return (target != null && DOM.isOrHasChild(getElement(), target));
    } else {
      return super.onEventPreview(event);
    }
  }

  /**
   * {@inheritDoc}
   *
   * Subclasses may override this to perform additional actions
   * after the wizard is shown, such as explicitly setting the
   * initially focused widget. Remember to call {@code super.show()}
   * as the first action in such overriding methods.
   */
  @Override
  public void show() {
    ensureInited();

    // Wizard size (having it resize between page changes is quite annoying)
    int width = 480;
    int height = 320;
    if (adaptiveSizing) {
      width = Math.max(width, Window.getClientWidth() / 3);
      height = Math.max(height, Window.getClientHeight() / 2);
    }
    setPixelSize(width, height);

    super.show();

    if (pageDeck.getWidgetCount() == 1) {
      buttonPanel.remove(backButton);
      buttonPanel.remove(nextButton);
    }

    // Show first wizard page
    currentPageIndex = 0;
    showCurrentPage();
  }

  /**
   * Adds a new page to the end of the wizard.
   */
  protected void addPage(Panel page) {
    page = new ScrollPanel(page);
    pageDeck.add(page);
  }

  public void setPagePanelHeight(int height){
    pagePanel.setHeight(height+"px");
  }

  /**
   * Sets the command to be executed upon finish (not on cancel).
   * May only be invoked once.
   */
  protected void initFinishCommand(Command finishCommand) {
    if (this.finishCommand != null) {
      throw new IllegalStateException();
    }
    this.finishCommand = finishCommand;
  }

  /**
   * Sets the command to be executed upon cancel.
   * May be invoked no more than once.
   */
  protected void initCancelCommand(Command cancelCommand) {
    this.cancelCommand = cancelCommand;
  }

  /**
   * Ensures that this class is fully initialized.
   */
  private void ensureInited() {
    if (pageDeck.getWidgetCount() == 0 || finishCommand == null) {
      throw new IllegalStateException();
    }
  }

  /**
   * Invoked immediately after showing a new page
   *
   * @param pageNumber  number of page to be shown
   */
  protected void onPageInit(int pageNumber) {
  }

  /**
   * Invoked immediately before moving away from the current page.
   *
   * @param pageNumber  number of current page
   */
  protected void onPageFinish(int pageNumber) {
  }

  /**
   * Invoked immediately before closing the wizard.
   */
  protected void onHide() {
  }

  /**
   * Shows the previous page.
   */
  protected void showPreviousPage() {
    onPageFinish(currentPageIndex);
    currentPageIndex--;
    showCurrentPage();
    onPageInit(currentPageIndex);
  }

  /**
   * Shows the next page.
   */
  protected void showNextPage() {
    onPageFinish(currentPageIndex);
    currentPageIndex++;
    showCurrentPage();
    onPageInit(currentPageIndex);
  }

  protected final void handleCancelClick() {
    hideWizard();
    if (cancelCommand != null) {
      cancelCommand.execute();
    }
  }

  protected final void handleOkClick() {
    if (okButton.isEnabled()) {
      hideWizard();
      finishCommand.execute();
    }
  }

  protected void disableOkButton() {
    okButton.setEnabled(false);
  }

  protected void enableOkButton() {
    okButton.setEnabled(true);
  }

  /*
   * Hides the wizard.
   * Note that we are not overriding hide() because it is called by center() which can some
   * ugliness!
   */
  private void hideWizard() {
    onPageFinish(currentPageIndex);
    onHide();
    hide();
  }

  /*
   * Shows the wizard page for the currentPageIndex in the dialog box.
   */
  private void showCurrentPage() {
    // Enable back button if the current page is not the first page
    backButton.setEnabled(currentPageIndex > 0);

    // Enable next button if the current page is not the last page otherwise enable finish button
    boolean isLastPage = currentPageIndex == pageDeck.getWidgetCount() - 1;
    nextButton.setEnabled(!isLastPage);
    okButton.setEnabled(isLastPage);

    // Show page
    pageDeck.showWidget(currentPageIndex);

    // Because pages are embedded in scroll panels it is important to set the size of the page to
    // the current height of the content panel
    pageDeck.getWidget(currentPageIndex).setHeight(pagePanel.getOffsetHeight() + "px");
  }
}
