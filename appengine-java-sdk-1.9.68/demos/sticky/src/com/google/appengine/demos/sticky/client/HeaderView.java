/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.demos.sticky.client;

import com.google.appengine.demos.sticky.client.model.Author;
import com.google.appengine.demos.sticky.client.model.Model;
import com.google.appengine.demos.sticky.client.model.Note;
import com.google.appengine.demos.sticky.client.model.Surface;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;

/**
 * A widget that displays the Ui associated with the header of the application.
 * This includes buttons for adding notes, bring up the surface list and
 * information about the current surface and user.
 *
 */
public class HeaderView extends FlowPanel implements Model.DataObserver,
    Surface.Observer {

  /**
   * Declaration of image bundle resources used in this widget.
   */
  public interface Images extends SurfaceListView.Images {

    @Resource("header-add-author-button-hv.gif")
    AbstractImagePrototype headerAddAuthorButtonHv();

    @Resource("header-add-author-button-up.gif")
    AbstractImagePrototype headerAddAuthorButtonUp();

    @Resource("header-add-button-dn.gif")
    AbstractImagePrototype headerAddButtonDn();

    @Resource("header-add-button-hv.gif")
    AbstractImagePrototype headerAddButtonHv();

    @Resource("header-add-button-up.gif")
    AbstractImagePrototype headerAddButtonUp();

    @Resource("header-surfaces-button-dn.gif")
    AbstractImagePrototype headerSurfacesButtonDn();

    @Resource("header-surfaces-button-hv.gif")
    AbstractImagePrototype headerSurfacesButtonHv();

    @Resource("header-surfaces-button-up.gif")
    AbstractImagePrototype headerSurfacesButtonUp();
  }

  /**
   * Encapsulates the views and behavior associated with the Ui to add an
   * author.
   */
  private class EditController implements BlurHandler, KeyPressHandler,
      ClickHandler {

    /**
     * A view displayed after a user submitted an author's email address, but
     * before the {@link Model} responds with success or failure. If the server
     * reports a failure, the {@link PendingAuthorView} is also used to display
     * that error.
     */
    private class PendingAuthorView extends SimplePanel implements
        ClickHandler, Model.SuccessCallback {
      private final String name;

      /**
       * Constructor.
       *
       * @param name
       *          the name to display while the model saves the change on the
       *          server
       */
      public PendingAuthorView(String name) {
        super(Document.get().createSpanElement());
        setStyleName("header-new-author");
        this.name = name;
        final int index = name.indexOf('@');
        if (index < 0) {
          setName(name);
          showError("invalid, click to fix.");
        } else {
          setName(name.substring(0, index));
          model.addAuthorToSurface(model.getSelectedSurface(), name, this);
        }
      }

      public void onClick(ClickEvent event) {
        removePendingAuthorView(this);
        edit(name);
      }

      public void onResponse(boolean success) {
        if (success) {
          removePendingAuthorView(this);
          button.setVisible(true);
        } else {
          showError("not found, click to fix.");
        }
      }

      private void setName(String name) {
        getElement().setInnerText(", " + name);
      }

      private void showError(String message) {
        final SpanElement errorElement = getElement().appendChild(
            Document.get().createSpanElement());
        errorElement.setClassName("header-new-author-error");
        final Style errorStyle = errorElement.getStyle();
        errorStyle.setProperty("color", "#800");
        errorStyle.setProperty("paddingLeft", "2px");
        errorStyle.setProperty("fontSize", "80%");
        errorElement.setInnerText(message);
        addHandler(this, ClickEvent.getType());
        sinkEvents(Event.ONCLICK);
      }
    }

    private final PushButton button;

    private final TextBox textBox = new TextBox();

    private final Model model;

    private boolean hasUserData;

    private boolean editMode;

    /**
     * Constructor.
     *
     * @param model
     *          the model to use for persisting changes
     * @param images
     *          a bundle of images to be used for internal widgets
     * @param styleName
     *          the style name to be applied to the text box
     */
    public EditController(Model model, Images images, String styleName) {
      this.model = model;
      button = Buttons.createPushButtonWithImageStates(images
          .headerAddAuthorButtonUp().createImage(), images
          .headerAddAuthorButtonHv().createImage(), "header-add-author-button",
          this);
      textBox.addBlurHandler(this);
      textBox.addKeyPressHandler(this);
      textBox.setVisible(false);
      textBox.setStyleName(styleName);
    }

    /**
     * Displays the text box and transitons the controller to edit mode.
     *
     */
    public void edit() {
      textBox.setText("Enter user's email address");
      hasUserData = false;
      textBox.setStyleName("header-author-edit-nodata");
      enterEditMode();
    }

    /**
     * Displays the text box and transitions the controller to edit mode.
     *
     * @param contents
     *          the initial contents for the text box
     */
    public void edit(String contents) {
      textBox.setText(contents);
      hasUserData = true;
      textBox.setStyleName("header-author-edit");
      enterEditMode();
    }

    /**
     * Gets the add button.
     *
     * @return
     */
    public PushButton getAddButton() {
      return button;
    }

    /**
     * Gets the text box for entry of author email address.
     *
     * @return
     */
    public TextBox getTextBox() {
      return textBox;
    }

    public void onBlur(BlurEvent event) {
      if (editMode) {
        commit();
      }
    }

    public void onClick(ClickEvent event) {
      button.setVisible(false);
      edit();
    }

    public void onKeyPress(KeyPressEvent event) {
      final char charCode = event.getCharCode();
      switch (charCode) {
      case KeyCodes.KEY_ENTER:
        commit();
        break;
      case KeyCodes.KEY_ESCAPE:
        cancel();
        break;
      default:
        hasUserData = true;
        textBox.setStyleName("header-author-edit");
      }
    }

    private void cancel() {
      resetTextBox();
      button.setVisible(true);
    }

    private void commit() {
      final String value = textBox.getValue().trim();
      if (hasUserData && value.length() > 0) {
        createPendingAuthorView(value);
        resetTextBox();
      } else {
        cancel();
      }
    }

    private PendingAuthorView createPendingAuthorView(String name) {
      final Element parentElement = textBox.getElement().getParentElement()
          .cast();
      final Element element = parentElement.insertBefore(
          Document.get().createSpanElement(), textBox.getElement()).cast();
      final PendingAuthorView view = new PendingAuthorView(name);
      HeaderView.this.add(view, element);
      return view;
    }

    private void enterEditMode() {
      textBox.setVisible(true);
      textBox.setFocus(true);
      textBox.selectAll();
      editMode = true;
    }

    private void removePendingAuthorView(PendingAuthorView view) {
      final Element pendingElement = view.getElement().getParentElement()
          .cast();
      HeaderView.this.remove(view);
      pendingElement.getParentElement().removeChild(pendingElement);
    }

    private void resetTextBox() {
      textBox.setValue("");
      textBox.setVisible(false);
      editMode = false;
    }
  }

  /**
   * A simple view to display the current user's name and a signout link.
   */
  private static class LoginInfoView extends FlowPanel implements ClickHandler {
    private final SpanElement userElem;

    private final AnchorElement linkElem;

    /**
     * @param author
     *          the current author
     * @param logoutUrl
     *          a url that can be used to logout
     */
    public LoginInfoView(Author author, String logoutUrl) {
      assert author != null;
      final Element element = getElement();
      userElem = element.appendChild(Document.get().createSpanElement());
      linkElem = element.appendChild(Document.get().createAnchorElement());

      element.setId("login-info");

      userElem.setId("login-info-name");
      userElem.setInnerText(author.getName());

      linkElem.setId("login-info-link");
      linkElem.setInnerText("sign out");
      linkElem.setHref(logoutUrl);
    }

    public void onClick(ClickEvent event) {
      Window.alert("click");
    }
  }

  /**
   * Encapsulates the views and behaviors associated with displaying and closing
   * the {@link SurfaceListView} and keeping the associated {@link ToggleButton}
   * in sync.
   */
  private static class SurfaceListViewController implements ClickHandler,
      SurfaceListView.Observer {

    private final ToggleButton button;

    private final SurfaceListView view;

    /**
     * @param model
     *          the model to which the enclosed {@link SurfaceListView} will
     *          communicate.
     * @param images
     *          a bundle of images used for internal Ui elements
     * @param styleName
     *          a style name for the enclosed {@link ToggleButton}
     */
    public SurfaceListViewController(Model model, Images images,
        String styleName) {
      button = Buttons.createToggleButtonWithImageStates(images
          .headerSurfacesButtonUp().createImage(), images
          .headerSurfacesButtonHv().createImage(), images
          .headerSurfacesButtonDn().createImage(), styleName, this);
      view = new SurfaceListView(images, model, this);
    }

    /**
     * Gets the button that is used to show and hide the surface list view.
     *
     * @return
     */
    public ToggleButton getButton() {
      return button;
    }

    /**
     * Gets the surface list view.
     *
     * @return
     */
    public SurfaceListView getSurfaceListView() {
      return view;
    }

    public void onClick(ClickEvent event) {
      view.setVisible(button.isDown());
    }

    public void onHide() {
      button.setDown(false);
    }

    public void onShow() {
    }
  }

  private static int NOTE_DEFAULT_X = 100;

  private static int NOTE_DEFAULT_Y = 100;

  private static int NOTE_DEFAULT_WIDTH = 300;

  private static int NOTE_DEFAULT_HEIGHT = 250;

  private final SpanElement surfaceNameElement = Document.get()
      .createSpanElement();

  private final SpanElement authorNamesElement = Document.get()
      .createSpanElement();

  private final EditController editController;

  /**
   * @param parent
   *          the parent for this widget
   * @param model
   *          the model to which the Ui will bind itself
   */
  public HeaderView(Images images, RootPanel parent, final Model model) {
    parent.add(this);

    final SurfaceListViewController controller = new SurfaceListViewController(
        model, images, "spc-button");
    parent.add(controller.getSurfaceListView());

    final Element elem = getElement();
    elem.setId("header");

    add(Buttons.createPushButtonWithImageStates(images.headerAddButtonUp()
        .createImage(), images.headerAddButtonHv().createImage(), images
        .headerAddButtonDn().createImage(), "add-button", new ClickHandler() {
      public void onClick(ClickEvent event) {
        model.createNote(NOTE_DEFAULT_X, NOTE_DEFAULT_Y, NOTE_DEFAULT_WIDTH,
            NOTE_DEFAULT_HEIGHT);
      }
    }));

    add(controller.getButton());

    editController = new EditController(model, images, "header-author-edit");

    final Surface surface = model.getSelectedSurface();
    surface.addObserver(this);
    attachTitleView(images, surface);

    add(new LoginInfoView(model.getCurrentAuthor(), model.getLogoutUrl()));
    model.addDataObserver(this);
  }

  public void onNoteCreated(Note note) {
  }

  public void onSurfaceCreated(Surface surface) {
  }

  public void onSurfaceNotesReceived(Note[] notes) {
  }

  public void onSurfaceSelected(Surface nowSelected, Surface wasSelected) {
    if (wasSelected != null) {
      wasSelected.removeObserver(this);
    }

    nowSelected.addObserver(this);
    updateTitleView(nowSelected);
  }

  public void onSurfacesReceived(Surface[] surfaces) {
  }

  public void onUpdate(Surface surface) {
    updateTitleView(surface);
  }

  private void attachTitleView(Images images, Surface surface) {
    final Element titleElement = getElement().appendChild(
        Document.get().createDivElement()).cast();
    titleElement.setId("header-title");

    surfaceNameElement.setId("header-name");
    authorNamesElement.setId("header-authors");

    titleElement.appendChild(surfaceNameElement);
    titleElement.appendChild(authorNamesElement);

    add(editController.getTextBox(), titleElement);
    add(editController.getAddButton(), titleElement);
    updateTitleView(surface);
  }

  private void updateTitleView(Surface surface) {
    surfaceNameElement.setInnerText(surface.getTitle());
    authorNamesElement.setInnerText("w/ " + surface.getAuthorNamesAsString());
  }
}
