// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.MockBall;
import com.google.appinventor.client.editor.simple.components.MockButton;
import com.google.appinventor.client.editor.simple.components.MockCanvas;
import com.google.appinventor.client.editor.simple.components.MockCheckBox;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContactPicker;
import com.google.appinventor.client.editor.simple.components.MockDatePicker;
import com.google.appinventor.client.editor.simple.components.MockEmailPicker;
import com.google.appinventor.client.editor.simple.components.MockHorizontalArrangement;
import com.google.appinventor.client.editor.simple.components.MockImage;
import com.google.appinventor.client.editor.simple.components.MockImagePicker;
import com.google.appinventor.client.editor.simple.components.MockImageSprite;
import com.google.appinventor.client.editor.simple.components.MockLabel;
import com.google.appinventor.client.editor.simple.components.MockListView;
import com.google.appinventor.client.editor.simple.components.MockListPicker;
import com.google.appinventor.client.editor.simple.components.MockTimePicker;
import com.google.appinventor.client.editor.simple.components.MockNonVisibleComponent;
import com.google.appinventor.client.editor.simple.components.MockPasswordTextBox;
import com.google.appinventor.client.editor.simple.components.MockPhoneNumberPicker;
import com.google.appinventor.client.editor.simple.components.MockRadioButton;
import com.google.appinventor.client.editor.simple.components.MockSlider;
import com.google.appinventor.client.editor.simple.components.MockTableArrangement;
import com.google.appinventor.client.editor.simple.components.MockTextBox;
import com.google.appinventor.client.editor.simple.components.MockVerticalArrangement;
import com.google.appinventor.client.editor.simple.components.MockVideoPlayer;
import com.google.appinventor.client.editor.simple.components.MockWebViewer;
import com.google.appinventor.client.editor.simple.components.MockSpinner;
import com.google.common.collect.Maps;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import java.util.Map;

/**
 * Descriptor for components on the component palette panel.
 * This class is immutable.
 *
 */
public final class SimpleComponentDescriptor {

  // Component display name
  private final String name;

  private final SimpleEditor editor;

  // Help information to display for component
  private final String helpString;

  // Goro documentation category URL piece
  private final String categoryDocUrlString;

  // Whether to show the component on the palette
  private final boolean showOnPalette;

  // Whether the component has a visual representation in the app's UI
  private final boolean nonVisible;

  // an instantiated mockcomponent is currently necessary in order to
  // to get the image, category, and description
  private MockComponent cachedMockComponent = null;

  // Component database: information about components (including their properties and events)
  private static final SimpleComponentDatabase COMPONENT_DATABASE =
      SimpleComponentDatabase.getInstance();

  /* We keep a static map of image names to images in the image bundle so
   * that we can avoid making individual calls to the server for static image
   * that are already in the bundle. This is purely an efficiency optimization
   * for mock non-visible components.
   */
  private static final Images images = Ode.getImageBundle();
  private static final Map<String, ImageResource> bundledImages = Maps.newHashMap();
  private static boolean imagesInitialized = false;

  private static void initBundledImages() {
    bundledImages.put("images/accelerometersensor.png", images.accelerometersensor());
    bundledImages.put("images/nearfield.png", images.nearfield());
    bundledImages.put("images/activityStarter.png", images.activitystarter());
    bundledImages.put("images/barcodeScanner.png", images.barcodeScanner());
    bundledImages.put("images/bluetooth.png", images.bluetooth());
    bundledImages.put("images/camera.png", images.camera());
    bundledImages.put("images/camcorder.png", images.camcorder());
    bundledImages.put("images/clock.png", images.clock());
    bundledImages.put("images/fusiontables.png", images.fusiontables());
    bundledImages.put("images/gameClient.png", images.gameclient());
    bundledImages.put("images/locationSensor.png", images.locationSensor());
    bundledImages.put("images/notifier.png", images.notifier());
    bundledImages.put("images/legoMindstormsNxt.png", images.legoMindstormsNxt());
    bundledImages.put("images/orientationsensor.png", images.orientationsensor());
    bundledImages.put("images/pedometer.png", images.pedometerComponent());
    bundledImages.put("images/phoneip.png", images.phonestatusComponent());
    bundledImages.put("images/phoneCall.png", images.phonecall());
    bundledImages.put("images/player.png", images.player());
    bundledImages.put("images/soundEffect.png", images.soundeffect());
    bundledImages.put("images/soundRecorder.png", images.soundRecorder());
    bundledImages.put("images/speechRecognizer.png", images.speechRecognizer());
    bundledImages.put("images/textToSpeech.png", images.textToSpeech());
    bundledImages.put("images/texting.png", images.texting());
    bundledImages.put("images/datePicker.png", images.datePickerComponent());
    bundledImages.put("images/timePicker.png", images.timePickerComponent());
    bundledImages.put("images/tinyDB.png", images.tinyDB());
    bundledImages.put("images/file.png", images.file());
    bundledImages.put("images/tinyWebDB.png", images.tinyWebDB());
    bundledImages.put("images/twitter.png", images.twitterComponent());
    bundledImages.put("images/voting.png", images.voting());
    bundledImages.put("images/web.png", images.web());
    bundledImages.put("images/sharing.png", images.sharingComponent());
    bundledImages.put("images/spinner.png", images.spinner());
    bundledImages.put("images/listView.png", images.listview());
    imagesInitialized = true;
  }

  /**
   * Creates a new component descriptor.
   *
   * @param name  component display name
   */
  public SimpleComponentDescriptor(String name,
                                   SimpleEditor editor,
                                   String helpString,
                                   String categoryDocUrlString,
                                   boolean showOnPalette,
                                   boolean nonVisible) {
    this.name = name;
    this.editor = editor;
    this.helpString = helpString;
    this.categoryDocUrlString = categoryDocUrlString;
    this.showOnPalette = showOnPalette;
    this.nonVisible = nonVisible;
  }

  /**
   * Returns the display name of the component.
   *
   * @return component display name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the help string for the component.  For more detail, see
   * javadoc for
   * {@link com.google.appinventor.client.editor.simple.ComponentDatabase#getHelpString(String)}.
   *
   * @return helpful message about the component
   */
  public String getHelpString() {
    return helpString;
  }

  /**
   * Returns the categoryDocUrl string for the component.  For more detail, see
   * javadoc for
   * {@link com.google.appinventor.client.editor.simple.ComponentDatabase#getCategoryDocUrlString(String)}.
   *
   * @return helpful message about the component
   */
  public String getCategoryDocUrlString() {
    return categoryDocUrlString;
  }

  /**
   * Returns whether this component should be shown on the palette.  For more
   * detail, see javadoc for
   * {@link com.google.appinventor.client.editor.simple.ComponentDatabase#getHelpString(String)}.
   *
   * @return whether the component should be shown on the palette
   */
  public boolean getShowOnPalette() {
    return showOnPalette;
  }

  /**
   * Returns whether this component is visible in the app's UI.  For more
   * detail, see javadoc for
   * {@link com.google.appinventor.client.editor.simple.ComponentDatabase#getHelpString(String)}.
   *
   * @return whether the component is non-visible
   */
  public boolean getNonVisible() {
    return nonVisible;
  }

  /**
   * Returns an image for display on the component palette.
   *
   * @return  image for component
   */
  public Image getImage() {
    if (nonVisible) {
      return getImageFromPath(COMPONENT_DATABASE.getIconName(name));
    } else {
      return getCachedMockComponent(name, editor).getIconImage();
    }
  }

  /**
   * Returns a draggable image for the component. Used when dragging a
   * component from the palette onto the form.
   *
   * @return  draggable widget for component
   */
  public Widget getDragWidget() {
    return createMockComponent(name, editor);
  }

  /**
   * Instantiates the corresponding mock component.
   *
   * @return  mock component
   */
  public MockComponent createMockComponentFromPalette() {
    MockComponent mockComponent = createMockComponent(name, editor);
    mockComponent.onCreateFromPalette();
    return mockComponent;
  }

  /**
   * Gets cached mock component; creates if necessary.
   */
  private MockComponent getCachedMockComponent(String name, SimpleEditor editor) {
    if (cachedMockComponent == null) {
      cachedMockComponent = createMockComponent(name, editor);
    }
    return cachedMockComponent;
  }

  public static Image getImageFromPath(String iconPath) {
    if (!imagesInitialized) {
      initBundledImages();
    }
    if (bundledImages.containsKey(iconPath)) {
      return new Image(bundledImages.get(iconPath));
    } else {
      return new Image(iconPath);
    }
  }

  /**
   * Instantiates mock component by name.
   */
  public static MockComponent createMockComponent(String name, SimpleEditor editor) {
    if (COMPONENT_DATABASE.getNonVisible(name)) {
      return new MockNonVisibleComponent(editor, name,
          getImageFromPath(COMPONENT_DATABASE.getIconName(name)));
    } else if (name.equals(MockButton.TYPE)) {
      return new MockButton(editor);
    } else if (name.equals(MockCanvas.TYPE)) {
      return new MockCanvas(editor);
    } else if (name.equals(MockCheckBox.TYPE)) {
      return new MockCheckBox(editor);
    } else if (name.equals(MockImage.TYPE)) {
      return new MockImage(editor);
    } else if (name.equals(MockLabel.TYPE)) {
      return new MockLabel(editor);
    } else if (name.equals(MockListView.TYPE)) {
      return new MockListView(editor);
    } else if (name.equals(MockSlider.TYPE)) {
        return new MockSlider(editor);
    } else if (name.equals(MockPasswordTextBox.TYPE)) {
      return new MockPasswordTextBox(editor);
    } else if (name.equals(MockRadioButton.TYPE)) {
      return new MockRadioButton(editor);
    } else if (name.equals(MockTextBox.TYPE)) {
      return new MockTextBox(editor);
    } else if (name.equals(MockContactPicker.TYPE)) {
      return new MockContactPicker(editor);
    } else if (name.equals(MockPhoneNumberPicker.TYPE)) {
      return new MockPhoneNumberPicker(editor);
    } else if (name.equals(MockEmailPicker.TYPE)) {
      return new MockEmailPicker(editor);
    } else if (name.equals(MockListPicker.TYPE)) {
      return new MockListPicker(editor);
    } else if (name.equals(MockDatePicker.TYPE)) {
      return new MockDatePicker(editor);
    } else if (name.equals(MockTimePicker.TYPE)) {
      return new MockTimePicker(editor);
    } else if (name.equals(MockHorizontalArrangement.TYPE)) {
      return new MockHorizontalArrangement(editor);
    } else if (name.equals(MockVerticalArrangement.TYPE)) {
      return new MockVerticalArrangement(editor);
    } else if (name.equals(MockTableArrangement.TYPE)) {
      return new MockTableArrangement(editor);
    } else if (name.equals(MockImageSprite.TYPE)) {
      return new MockImageSprite(editor);
    } else if (name.equals(MockBall.TYPE)) {
      return new MockBall(editor);
    } else if (name.equals(MockImagePicker.TYPE)) {
      return new MockImagePicker(editor);
    } else if (name.equals(MockVideoPlayer.TYPE)) {
      return new MockVideoPlayer(editor);
    } else if (name.equals(MockWebViewer.TYPE)) {
      return new MockWebViewer(editor);
    } else if (name.equals(MockSpinner.TYPE)) {
      return new MockSpinner(editor);
    } else {
      // TODO(user): add 3rd party mock component proxy here
      throw new UnsupportedOperationException("unknown component: " + name);
    }
  }
}
