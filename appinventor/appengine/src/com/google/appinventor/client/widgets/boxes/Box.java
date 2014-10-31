// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.boxes;

import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.ContextMenu;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONUtil;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Map;

/**
 * Abstract superclass for all boxes.
 *
 * <p>A box is a container widget. It automatically handles scrolling for
 * embedded widgets. Boxes can be resized, minimized and restored.
 *
 */
public abstract class Box extends HandlerPanel {

  /**
   * Describes a box in the context of a layout.
   */
  public static final class BoxDescriptor {
    // Field names for JSON encoding of box descriptors
    private static final String NAME_TYPE = "type";
    private static final String NAME_WIDTH = "width";
    private static final String NAME_HEIGHT = "height";
    private static final String NAME_MINIMIZED = "minimized";

    // Information needed to create a box in a layout
    private final String type;
    private final int width;
    private final int height;
    private final boolean minimized;

    /**
     * Creates a new box description.
     *
     * @param type  type of box
     * @param width  width of box in pixels
     * @param height  height of box in pixels if not minimized
     * @param minimized  indicates whether box is minimized
     */
    private BoxDescriptor(String type, int width, int height, boolean minimized) {
      this.type = type;
      this.width = width;
      this.height = height;
      this.minimized = minimized;
    }

    /**
     * Creates a new box description.
     *
     * @param type  type of box
     * @param width  width of box in pixels
     * @param height  height of box in pixels if not minimized
     * @param minimized  indicates whether box is minimized
     */
    public BoxDescriptor(Class<? extends Box> type, int width, int height, boolean minimized) {
      this(type.getName(), width, height, minimized);
    }

    /**
     * Returns the box type (for use by a {@link BoxRegistry}).
     *
     * @return  box type
     */
    public String getType() {
      return type;
    }

    /**
     * Encodes the box information into JSON format.
     */
    public String toJson() {
      return "{" +
            "\"" + NAME_TYPE + "\":" + JSONUtil.toJson(type) + "," +
            "\"" + NAME_WIDTH + "\":" + JSONUtil.toJson(width) + "," +
            "\"" + NAME_HEIGHT + "\":" + JSONUtil.toJson(height) + "," +
            "\"" + NAME_MINIMIZED + "\":" + JSONUtil.toJson(minimized) +
          "}";
    }

    /**
     * Creates a new box descriptor from a JSON object.
     *
     * @param object  box descriptor in JSON format
     */
    public static BoxDescriptor fromJson(JSONObject object) {
      Map<String, JSONValue> properties = object.getProperties();

      return new BoxDescriptor(JSONUtil.stringFromJsonValue(properties.get(NAME_TYPE)),
          JSONUtil.intFromJsonValue(properties.get(NAME_WIDTH)),
          JSONUtil.intFromJsonValue(properties.get(NAME_HEIGHT)),
          JSONUtil.booleanFromJsonValue(properties.get(NAME_MINIMIZED)));
    }

    /**
     * Returns the type of box from a JSON object.
     *
     * @param object  box descriptor in JSON format
     */
    public static String boxTypeFromJson(JSONObject object) {
      Map<String, JSONValue> properties = object.getProperties();

      return JSONUtil.stringFromJsonValue(properties.get(NAME_TYPE));
    }
  }

  /**
   * Control for resizing boxes.
   */
  private final class ResizeControl extends PopupPanel {

    /**
     * Creates a control to resize the box.
     */
    private ResizeControl() {
      super(false); // no autohide

      VerticalPanel buttonPanel = new VerticalPanel();
      buttonPanel.setSpacing(10);
      addControlButton(buttonPanel, "-", new Command() {
        @Override
        public void execute() {
          height = Math.max(100, height - 20);
          restoreHeight = height;
          onResize(width, height);
        }
      });
      addControlButton(buttonPanel, "+", new Command() {
        @Override
        public void execute() {
          height = height + 20;
          restoreHeight = height;
          onResize(width, height);
        }
      });
      addControlButton(buttonPanel, MESSAGES.done(), new Command() {
        @Override
        public void execute() {
          hide();
        }
      });
      add(buttonPanel);

      setModal(true);
      setStylePrimaryName("ode-BoxResizeControl");
    }

    /**
     * Creates a button with a click handler which will execute the given command.
     */
    private void addControlButton(VerticalPanel panel, String caption, final Command command) {
      TextButton button = new TextButton(caption);
      button.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          command.execute();
        }
      });
      panel.add(button);
      panel.setCellHorizontalAlignment(button, VerticalPanel.ALIGN_CENTER);
    }
  }

  // Height of minimized box
  private static final int MINIMIZED_HEIGHT = 31;

  // Padding between header controls
  private static final int HEADER_CONTROL_PADDING = 2;

  // Constants for box decorations (note that these constants correspond to the box's style
  // definition)
  private static final int BOX_PADDING = 5;
  private static final int BOX_BORDER = 1;

  // UI elements
  private final SimplePanel body;
  private final Label captionLabel;
  private final HandlerPanel header;
  private final DockPanel headerContainer;
  private final ScrollPanel scrollPanel;
  private final PushButton minimizeButton;
  private final PushButton menuButton;

  // Indicates that the box height is changed through resize operations of the layout
  private boolean variableHeightBoxes;

  // Box dimensions
  private int width;
  private int height;

  // Height of non-minimized box
  private int restoreHeight;

  // Whether box should always begin minimized
  private boolean startMinimized;

  // Whether new captions should be highlighted
  private boolean highlightCaption;

  // Whether user has seen/acknowledged the new caption yet
  private boolean captionAlreadySeen = false;

  /**
   * Creates a new box.
   *
   * @param caption  box caption
   * @param height  box initial height in pixel
   * @param minimizable  indicates whether box can be minimized
   * @param removable  indicates whether box can be closed/removed
   * @param startMinimized indicates whether box should always start minimized
   * @param bodyPadding indicates whether box should have padding
   * @param highlightCaption indicates whether caption should be highlighted
   *                         until user has "seen" it (interacts with the box)
   */
  protected Box(String caption, int height, boolean minimizable, boolean removable,
      boolean startMinimized, boolean bodyPadding, boolean highlightCaption) {
    this.height = height;
    this.restoreHeight = height;
    this.startMinimized = startMinimized;
    this.highlightCaption = highlightCaption;

    captionLabel = new Label(caption, false);
    captionAlreadySeen = false;
    if (highlightCaption) {
      captionLabel.setStylePrimaryName("ode-Box-header-caption-highlighted");
    } else {
      captionLabel.setStylePrimaryName("ode-Box-header-caption");
    }
    header = new HandlerPanel();
    header.add(captionLabel);
    header.setWidth("100%");

    headerContainer = new DockPanel();
    headerContainer.setStylePrimaryName("ode-Box-header");
    headerContainer.setWidth("100%");
    headerContainer.add(header, DockPanel.LINE_START);

    Images images = Ode.getImageBundle();

    if (removable) {
      PushButton closeButton = Ode.createPushButton(images.boxClose(), MESSAGES.hdrClose(),
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              // TODO(user) - remove the box
              Window.alert("Not implemented yet!");
            }
          });
      headerContainer.add(closeButton, DockPanel.LINE_END);
      headerContainer.setCellWidth(closeButton,
          (closeButton.getOffsetWidth() + HEADER_CONTROL_PADDING) + "px");
    }

    if (!minimizable) {
      minimizeButton = null;
    } else {
      minimizeButton = Ode.createPushButton(images.boxMinimize(), MESSAGES.hdrMinimize(),
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              if (isMinimized()) {
                restore();
              } else {
                minimize();
              }
            }
          });
      headerContainer.add(minimizeButton, DockPanel.LINE_END);
      headerContainer.setCellWidth(minimizeButton,
          (minimizeButton.getOffsetWidth() + HEADER_CONTROL_PADDING) + "px");
    }

    if (minimizable || removable) {
      menuButton = Ode.createPushButton(images.boxMenu(), MESSAGES.hdrSettings(),
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              final ContextMenu contextMenu = new ContextMenu();
              contextMenu.addItem(MESSAGES.cmMinimize(), new Command() {
                @Override
                public void execute() {
                  if (! isMinimized()) {
                    minimize();
                  }
                }
              });
              contextMenu.addItem(MESSAGES.cmRestore(), new Command() {
                @Override
                public void execute() {
                  if (isMinimized()) {
                    restore();
                  }
                }
              });
              if (!variableHeightBoxes) {
                contextMenu.addItem(MESSAGES.cmResize(), new Command() {
                  @Override
                  public void execute() {
                    restore();
                    final ResizeControl resizeControl = new ResizeControl();
                    resizeControl.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                      @Override
                      public void setPosition(int offsetWidth, int offsetHeight) {
                        // SouthEast
                        int left = menuButton.getAbsoluteLeft() + menuButton.getOffsetWidth()
                            - offsetWidth;
                        int top = menuButton.getAbsoluteTop() + menuButton.getOffsetHeight();
                        resizeControl.setPopupPosition(left, top);
                      }
                    });
                  }
                });
              }
              contextMenu.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                  // SouthEast
                  int left = menuButton.getAbsoluteLeft() + menuButton.getOffsetWidth()
                      - offsetWidth;
                  int top = menuButton.getAbsoluteTop() + menuButton.getOffsetHeight();
                  contextMenu.setPopupPosition(left, top);
                }
              });
            }
          });
      headerContainer.add(menuButton, DockPanel.LINE_END);
      headerContainer.setCellWidth(menuButton,
          (menuButton.getOffsetWidth() + HEADER_CONTROL_PADDING) + "px");
    } else {
      menuButton = null;
    }

    body = new SimplePanel();
    body.setSize("100%", "100%");

    scrollPanel = new ScrollPanel();
    scrollPanel.setStylePrimaryName("ode-Box-body");
    if (bodyPadding) {
      scrollPanel.addStyleName("ode-Box-body-padding");
    }
    scrollPanel.add(body);

    FlowPanel boxContainer = new FlowPanel();
    boxContainer.setStyleName("ode-Box-content");
    boxContainer.add(headerContainer);
    boxContainer.add(scrollPanel);

    setStylePrimaryName("ode-Box");
    setWidget(boxContainer);
  }

  protected Box(String caption, int height, boolean minimizable, boolean removable,
      boolean startMinimized, boolean highlightCaption) {
    this(caption, height, minimizable, removable, startMinimized, true, highlightCaption);
  }

  protected Box(String caption, int height, boolean minimizable, boolean removable,
      boolean startMinimized) {
    this(caption, height, minimizable, removable, startMinimized, true, false);
  }

  protected Box(String caption, int height, boolean minimizable, boolean removable) {
    this(caption, height, minimizable, removable, false, true, false);
  }

  @Override
  public void clear() {
    body.clear();
  }

  /**
   * Sets the resizing behavior of the box.
   *
   * @param variableHeightBoxes  indicates whether the box height will be
   *                             updated upon layout resize operations
   */
  public void setVariableHeightBoxes(boolean variableHeightBoxes) {
    this.variableHeightBoxes = variableHeightBoxes;
  }

  /**
   * Shows the given widget in the box.
   *
   * @param w  widget to show
   */
  public void setContent(Widget w) {
    body.setWidget(w);
  }

  /**
   * Sets the given caption for the box.
   *
   * @param caption  box caption to show
   */
  public void setCaption(String caption) {
    if (highlightCaption) {
      captionLabel.setStylePrimaryName("ode-Box-header-caption-highlighted");
      captionAlreadySeen = false;
    }
    captionLabel.setText(caption);
  }

  /**
   * Returns the box header.
   *
   * @return  box header widget
   */
  Widget getHeader() {
    return header;
  }

  /**
   * Invoked upon resizing of the box by the layout. Box height will remain
   * unmodified.
   *
   * @see Layout#onResize(int, int)
   *
   * @param width  new column width for box in pixel
   */
  protected void onResize(int width) {
    onResize(width, height);
  }

  /**
   * Invoked upon resizing of the box by the layout.
   *
   * @see Layout#onResize(int, int)
   *
   * @param width  new column width for box in pixel
   * @param height  new column height for box in pixel
   */
  protected void onResize(int width, int height) {
    this.width = width;
    this.height = height;

    if (!isMinimized()) {
      restoreHeight = height;
    }

    setSize(this.width + "px", this.height + "px");

    // In order to get the correct size for the scroll panel we need to subtract the dimensions
    // of all decorations such as padding, borders, margin etc. It is also important to set the size
    // for the scroll panel in pixels, as this seems to be the only reliably working unit.
    // We subtract padding and border sizes from top and bottom as well as the height of the box
    // header.
    int w = getOffsetWidth() - 2 * (BOX_PADDING + BOX_BORDER);
    int h = getOffsetHeight() - 2 * (BOX_PADDING + BOX_BORDER) - headerContainer.getOffsetHeight();

    // On startup it can happen that we receive a window resize event before the boxes are attached
    // to the DOM. In that case, offset width and height are 0, we can safely abort because there
    // will soon be another resize event after the boxes are attached to the DOM.
    if (w > 0 && h > 0) {
      scrollPanel.setSize(w + "px", h + "px");
    }
  }

  /**
   * Restores the box layout.
   *
   * @param bd  box descriptor with layout settings of box
   */
  public void restoreLayoutSettings(BoxDescriptor bd) {
    restoreHeight = bd.height;
    height = bd.height;

    if (bd.minimized || startMinimized) {
      minimize();
    } else {
      restore();
    }
  }

  /**
   * Returns box layout settings.
   *
   * @return  box layout settings
   */
  public BoxDescriptor getLayoutSettings() {
    return new BoxDescriptor(getClass().getName(), width, restoreHeight, isMinimized());
  }

  /**
   * Indicates whether the box is minimized.
   */
  private boolean isMinimized() {
    return height != restoreHeight;
  }

  /**
   * Minimizes a box.
   */
  private void minimize() {
    scrollPanel.setVisible(false);
    minimizeButton.getUpFace().setImage(new Image(Ode.getImageBundle().boxRestore()));
    minimizeButton.setTitle(MESSAGES.hdrRestore());

    if (highlightCaption && captionAlreadySeen) {
      captionLabel.setStylePrimaryName("ode-Box-header-caption");
    }
    captionAlreadySeen = true;

    restoreHeight = height;
    height = MINIMIZED_HEIGHT;
    onResize(width, height);
  }

  /**
   * Restores a minimized box to its previous height.
   */
  private void restore() {
    minimizeButton.getUpFace().setImage(new Image(Ode.getImageBundle().boxMinimize()));
    minimizeButton.setTitle(MESSAGES.hdrMinimize());
    scrollPanel.setVisible(true);

    if (highlightCaption && captionAlreadySeen) {
      captionLabel.setStylePrimaryName("ode-Box-header-caption");
    }
    captionAlreadySeen = true;

    height = restoreHeight;
    onResize(width, height);
  }

  /**
   * Helper method for adding style elements (in particular the rounded corners).
   */
  private void appendDecorationElement(String styleClass) {
    Element element = DOM.createDiv();
    element.setClassName(styleClass);
    getElement().appendChild(element);
  }
}
