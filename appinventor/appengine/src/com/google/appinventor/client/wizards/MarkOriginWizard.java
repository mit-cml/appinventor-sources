package com.google.appinventor.client.wizards;

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

  /**
   * Creates a new mark origin wizard.
   * @param imageUrl the URL to the image of the image sprite
   * @param origin the value of the origin property of the image sprite
   * @param callback the callback to execute after selecting origin
   */
  public MarkOriginWizard(String imageUrl, String origin, final OriginSelectedCallback callback) {
    super(Ode.MESSAGES.markOriginWizardCaption(), true, false);

    u = Double.parseDouble(getUFromOrigin(origin));
    v = Double.parseDouble(getVFromOrigin(origin));

    // HTMLPanel that wraps the sprite image so that it renders properly.
    // GWT dialog uses HTML tables, which will attempt to resize to the content size. Using an image
    // without an enclosing panel thus causes problems.
    final HTMLPanel spriteImagePanel = new HTMLPanel("");
    spriteImagePanel.setSize("50vw", "50vh");

    spriteImage = new Image(imageUrl);
    spriteImagePanel.add(spriteImage);

    // align the spriteImage to the center of the panel
    spriteImagePanel.getElement().getStyle().setProperty("display", "flex");
    spriteImagePanel.getElement().getStyle().setProperty("justifyContent", "center");
    spriteImagePanel.getElement().getStyle().setProperty("alignItems", "center");

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
        int xMarker = marker.getLeftX(leftX + (int) (u * spriteImage.getWidth()));
        int yMarker = marker.getTopY(topY + (int) (v * spriteImage.getHeight()));

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

        u = ((double) (marker.getOriginX(x) - leftX)) / spriteImage.getWidth();
        // make sure u is in the correct range
        if (u < 0) {
          u = 0;
        } else if (u > 1) {
          u = 1;
        }

        v = ((double) (marker.getOriginY(y) - topY)) / spriteImage.getHeight();
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

    // for marker u = 0.5 and v = 1.0 as the marker's pointer is the middle of the bottom edge
    marker = new DragImage(markerImg, imageContainer, 0.5, 1.0);

    imageContainer.add(spriteImagePanel);
    imageContainer.add(marker);

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
       * @param x x - coordinate of the dropped widget
       * @param y y - coordinate of the dropped widget
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
   *  A draggable image.
   *
   */
  public static class DragImage extends DragSourcePanel {

    DropTarget target;

    // The unit coordinates for the origin of the image
    double u;
    double v;

    // width and height of the image
    int width;
    int height;

    /**
     * Create a drag image with the specified image and drop target.
     * @param image The image which is made draggable
     * @param target The possible drop target for this drag source
     */
    public DragImage(Image image, DropTarget target) {
      this(image, target, 0, 0);
    }

    /**
     * Create a drag image with the specified image and drop target.
     * @param image The image which is made draggable
     * @param target The possible drop target for this drag source
     * @param u The x unit coordinate of the origin of the image
     * @param v The y unit coordinate of the origin of the image
     */
    public DragImage(Image image, DropTarget target, double u, double v) {
      super();
      this.add(image);
      width = image.getWidth();
      height = image.getHeight();
      this.target = target;
      this.u = u;
      this.v = v;
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

    public int getLeftX(int xOrigin) {
      return xOrigin - (int) (width * u);
    }

    public int getTopY(int yOrigin) {
      return yOrigin - (int) (height * v);
    }

    public int getOriginX(int xLeft) {
      return xLeft + (int) (width * u);
    }

    public int getOriginY(int yTop) {
      return yTop + (int) (height * v);
    }
  }
}
