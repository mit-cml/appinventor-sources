// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.widgets.ClonedWidget;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.client.widgets.dnd.DragSourcePanel;
import com.google.appinventor.client.widgets.dnd.DragSourceSupport;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Wizard for selecting the origin of image sprites using a draggable marker.
 *
 */
public class MarkOriginWizard extends Wizard {

  /**
   * Interface for callback to execute after origin is selected.
   */
  public interface OriginSelectedCallback {
    /**
     * Will be invoked after origin is selected. (User clicks 'OK' in the dialog)
     *
     * @param value The value of the origin property.
     */
    void onSelected(String value);
  }

  // Widget for holding sprite image
  private final Image spriteImage;
  // Draggable Image widget to hold the marker
  private final DragImage marker;
  // Drag enabled panel to contain the marker and the sprite image
  private final DragPanel imageContainer;

  // The unit coordinates of the origin wrt top - left corner
  private double u;
  private double v;

  // The height and width of the marker
  private final int markerHeight = 30;
  private final int markerWidth = 30;

  /**
   * Creates a new mark origin wizard.
   * @param imageUrl the URL to the image of the image sprite
   * @param origin the value of the origin property of the image sprite
   * @param callback the callback to execute after selecting origin
   */
  public MarkOriginWizard(String imageUrl, String origin, final OriginSelectedCallback callback) {
    super(MESSAGES.markOriginWizardCaption(), true, false);

    u = Double.parseDouble(getUFromOrigin(origin));
    v = Double.parseDouble(getVFromOrigin(origin));

    // HTMLPanel that wraps the sprite image so that it renders properly.
    // GWT dialog uses HTML tables, which will attempt to resize to the content size. Using an image
    // without an enclosing panel thus causes problems.
    final HTMLPanel spriteImagePanel = new HTMLPanel("");
    spriteImagePanel.setSize("50vw", "50vh");

    spriteImage = new Image(imageUrl);
    spriteImage.getElement().getStyle().setProperty("border", "1px solid black");
    spriteImage.getElement().getStyle().setProperty("box-sizing", "border-box");
    spriteImagePanel.add(spriteImage);

    // align the spriteImage to the center of the panel
    spriteImagePanel.getElement().getStyle().setProperty("display", "flex");
    spriteImagePanel.getElement().getStyle().setProperty("justifyContent", "center");
    spriteImagePanel.getElement().getStyle().setProperty("alignItems", "center");
    spriteImagePanel.getElement().getStyle().setProperty("padding", markerHeight + "px "
                    + markerWidth / 2 + "px 0px " + markerWidth / 2  + "px");

    // allow spriteImage to get as big as possible but not bigger
    spriteImage.getElement().getStyle().setProperty("maxWidth", "100%");
    spriteImage.getElement().getStyle().setProperty("maxHeight", "100%");

    spriteImage.addLoadHandler(new LoadHandler() {
      @Override
      public void onLoad(LoadEvent loadEvent) {
        // left - most x coordinate of spriteImage
        int leftX = (spriteImagePanel.getOffsetWidth() - spriteImage.getWidth()) / 2;
        // top - most y coordinate of spriteImage
        int topY = (spriteImagePanel.getOffsetHeight() - spriteImage.getHeight()) / 2;

        // coordinates of the top - left corner of the marker
        int xMarker = leftX + (int) (u * spriteImage.getWidth());
        int yMarker = topY + (int) (v * spriteImage.getHeight());

        imageContainer.setWidgetPosition(marker, xMarker, yMarker);

        // make sure marker does not go out of the image when dragging
        marker.constraintToBounds(spriteImage.getAbsoluteLeft(), spriteImage.getAbsoluteTop(),
                spriteImage.getWidth(), spriteImage.getHeight());
      }
    });

    DragPanel.OnDropCallback onDropCallback = new DragPanel.OnDropCallback() {
      @Override
      public void onDrop(int x, int y) {
        // left - most x coordinate of spriteImage
        int leftX = (spriteImagePanel.getOffsetWidth() - spriteImage.getWidth()) / 2;
        // top - most y coordinate of spriteImage
        int topY = (spriteImagePanel.getOffsetHeight() - spriteImage.getHeight()) / 2;

        u = ((double) (x - leftX)) / spriteImage.getWidth();
        // make sure u is in the correct range
        if (u < 0) {
          u = 0;
        } else if (u > 1) {
          u = 1;
        }

        v = ((double) (y - topY)) / spriteImage.getHeight();
        // make sure v is in the correct range
        if (v < 0) {
          v = 0;
        } else if (v > 1) {
          v = 1;
        }
      }
    };

    imageContainer = new DragPanel(onDropCallback);

    Image markerImg = new Image(Ode.getImageBundle().marker());
    markerImg.setSize("30px", "30px");

    markerImg.getElement().getStyle().setProperty("marginTop", "-" + markerHeight + "px");
    markerImg.getElement().getStyle().setProperty("marginLeft", "-" + markerWidth / 2 + "px");

    // for marker u = 0.5 and v = 1.0 as the marker's pointer is the middle of the bottom edge
    marker = new DragImage(markerImg, imageContainer);

    imageContainer.add(spriteImagePanel);
    imageContainer.add(marker);
    Button reset = new Button(MESSAGES.resetButton());
    reset.addClickHandler((event) -> {
      finishApply(callback, "(0.0, 0.0)");
      hide();
    });
    getButtonPanel().insert(reset, 1);

    addPage(imageContainer);

    initFinishCommand(new Command() {
      @Override
      public void execute() {
        finishApply(callback, "(" + u + ", " + v + ")");
      }
    });
  }

  private void finishApply(OriginSelectedCallback callback, String value) {
    callback.onSelected(value);
  }

  @Override
  public void show() {
    super.show();
    this.center();
  }

  private String getUFromOrigin(String text) {
    return text.substring(1, text.indexOf(","));
  }

  private String getVFromOrigin(String text) {
    return text.substring(text.indexOf(",") + 2, text.length() - 1);
  }

  /**
   * Absolute Panel that allows draggable widgets to be dropped on it.
   *
   */
  public static class DragPanel extends AbsolutePanel implements DropTarget {

    /**
     * Interface for callback after a drag widget is dropped in the panel.
     *
     */
    public interface OnDropCallback {
      /**
       * Will be invoked after a drag widget is dropped in the panel.
       *
       * @param x x - coordinate of the dropped widget's top left corner
       * @param y y - coordinate of the dropped widget's top left corner
       */
      void onDrop(int x, int y);
    }
    
    final OnDropCallback callback;
    
    public DragPanel(final OnDropCallback callback) {
      this.callback = callback;
    }

    @Override
    public Widget getDropTargetWidget() {
      return this;
    }

    @Override
    public boolean onDragEnter(DragSource source, int x, int y) {
      return true;
    }

    @Override
    public void onDragContinue(DragSource source, int x, int y) {

    }

    @Override
    public void onDragLeave(DragSource source) {
      
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int offsetX, int offsetY) {
      setWidgetPosition((Widget) source, x - offsetX, y - offsetY);
      callback.onDrop(x - offsetX, y - offsetY);
    }
  }

  /**
   *  A draggable image that can be dragged on panels that implement {@code DropTarget}.
   *
   */
  public static class DragImage extends DragSourcePanel {

    DropTarget target;

    // width and height of the image
    int width;
    int height;

    /**
     * Create a drag image with the specified image and drop target. The origin of the image is
     * at the unit coordinates {@code u} and {@code v}.
     *
     * @param image The image which is made draggable
     * @param target The possible drop target for this drag source
     */
    public DragImage(Image image, DropTarget target) {
      super();
      this.add(image);
      width = image.getWidth();
      height = image.getHeight();
      this.target = target;
    }

    @Override
    public void onDragStart() {
    }

    @Override
    public Widget createDragWidget(int x, int y) {
      Widget w = new ClonedWidget(this);
      DragSourceSupport.configureDragWidgetToAppearWithCursorAt(w, x, y);
      setVisible(false);
      return w;
    }

    @Override
    public Widget getDragWidget() {
      return dragSourceSupport.getDragWidget();
    }

    @Override
    public DropTarget[] getDropTargets() {
      return new DropTarget[] {target};
    }

    @Override
    public void onDragEnd() {
      setVisible(true);
    }
  }
}
