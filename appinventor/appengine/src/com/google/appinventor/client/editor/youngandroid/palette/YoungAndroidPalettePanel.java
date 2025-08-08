// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.palette;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.palette.AbstractPalettePanel;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.wizards.ComponentImportWizard;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Panel showing Simple components which can be dropped onto the Young Android
 * visual designer panel.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidPalettePanel extends AbstractPalettePanel<SimpleComponentDatabase, YaFormEditor> {

  private static final Logger LOG = Logger.getLogger(YoungAndroidPalettePanel.class.getName());

  private static final Map<String, ImageResource> bundledImages;
  static {
    Images images = Ode.getImageBundle();
    bundledImages = new HashMap<String, ImageResource>();
    bundledImages.put("images/accelerometersensor.png", images.accelerometersensor());
    bundledImages.put("images/ball.png", images.ball());
    bundledImages.put("images/button.png", images.button());
    bundledImages.put("images/canvas.png", images.canvas());
    bundledImages.put("images/imageSprite.png", images.imageSprite());
    bundledImages.put("images/imagePicker.png", images.imagepicker());
    bundledImages.put("images/videoPlayer.png", images.videoplayer());
    bundledImages.put("images/horizontal.png", images.horizontal());
    bundledImages.put("images/vertical.png", images.vertical());
    bundledImages.put("images/table.png", images.table());
    bundledImages.put("images/checkbox.png", images.checkbox());
    bundledImages.put("images/image.png", images.image());
    bundledImages.put("images/label.png", images.label());
    bundledImages.put("images/listPicker.png", images.listpicker());
    bundledImages.put("images/passwordtextbox.png", images.passwordtextbox());
    bundledImages.put("images/slider.png", images.slider());
    bundledImages.put("images/switch.png", images.toggleswitch());
    bundledImages.put("images/textbox.png", images.textbox());
    bundledImages.put("images/webviewer.png", images.webviewer());
    bundledImages.put("images/contactPicker.png", images.contactpicker());
    bundledImages.put("images/emailPicker.png", images.emailpicker());
    bundledImages.put("images/phoneNumberPicker.png", images.phonenumberpicker());
    bundledImages.put("images/lightsensor.png", images.lightsensor());
    bundledImages.put("images/barometer.png", images.barometer());
    bundledImages.put("images/thermometer.png", images.thermometer());
    bundledImages.put("images/hygrometer.png", images.hygrometer());
    bundledImages.put("images/gyroscopesensor.png", images.gyroscopesensor());
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
    bundledImages.put("images/legoMindstormsEv3.png", images.legoMindstormsEv3());
    bundledImages.put("images/orientationsensor.png", images.orientationsensor());
    bundledImages.put("images/pedometer.png", images.pedometerComponent());
    bundledImages.put("images/phoneip.png", images.phonestatusComponent());
    bundledImages.put("images/phoneCall.png", images.phonecall());
    bundledImages.put("images/player.png", images.player());
    bundledImages.put("images/soundEffect.png", images.soundeffect());
    bundledImages.put("images/soundRecorder.png", images.soundRecorder());
    bundledImages.put("images/speechRecognizer.png", images.speechRecognizer());
    bundledImages.put("images/spreadsheet.png", images.spreadsheet());
    bundledImages.put("images/textToSpeech.png", images.textToSpeech());
    bundledImages.put("images/texting.png", images.texting());
    bundledImages.put("images/datePicker.png", images.datePickerComponent());
    bundledImages.put("images/timePicker.png", images.timePickerComponent());
    bundledImages.put("images/tinyDB.png", images.tinyDB());
    bundledImages.put("images/file.png", images.file());
    bundledImages.put("images/tinyWebDB.png", images.tinyWebDB());
    bundledImages.put("images/firebaseDB.png", images.firebaseDB());
    bundledImages.put("images/twitter.png", images.twitterComponent());
    bundledImages.put("images/voting.png", images.voting());
    bundledImages.put("images/web.png", images.web());
    bundledImages.put("images/mediastore.png", images.mediastore());
    bundledImages.put("images/sharing.png", images.sharingComponent());
    bundledImages.put("images/spinner.png", images.spinner());
    bundledImages.put("images/listView.png", images.listview());
    bundledImages.put("images/translator.png", images.translator());
    bundledImages.put("images/yandex.png", images.yandex());
    bundledImages.put("images/proximitysensor.png", images.proximitysensor());
    bundledImages.put("images/extension.png", images.extension());
    bundledImages.put("images/cloudDB.png", images.cloudDB());
    bundledImages.put("images/map.png", images.map());
    bundledImages.put("images/marker.png", images.marker());
    bundledImages.put("images/circle.png", images.circle());
    bundledImages.put("images/linestring.png", images.linestring());
    bundledImages.put("images/polygon.png", images.polygon());
    bundledImages.put("images/featurecollection.png", images.featurecollection());
    bundledImages.put("images/rectangle.png", images.rectangle());
    bundledImages.put("images/recyclerView.png", images.recyclerview());
    bundledImages.put("images/navigation.png", images.navigationComponent());
    bundledImages.put("images/arduino.png", images.arduino());
    bundledImages.put("images/magneticSensor.png", images.magneticSensor());
    bundledImages.put("images/chart.png", images.chart());
    bundledImages.put("images/chartData.png", images.chartData2D());
    bundledImages.put("images/dataFile.png", images.dataFile());
    bundledImages.put("images/circularProgress.png", images.circularProgress());
    bundledImages.put("images/linearProgress.png", images.linearProgress());
    bundledImages.put("images/regression.png", images.regression());
    bundledImages.put("images/anomaly.png", images.anomalyDetection());
    bundledImages.put("images/filepicker.png", images.file());
    bundledImages.put("images/trendline.png", images.trendline());
    bundledImages.put("images/chatbot.png", images.chatbot());
    bundledImages.put("images/paintpalette.png", images.paintPalette());
  }

  /**
   * Creates a new component palette panel.
   */
  public YoungAndroidPalettePanel(YaFormEditor editor) {
    super(editor, new YoungAndroidComponentFactory(editor), ComponentCategory.USERINTERFACE, ComponentCategory.LAYOUT, ComponentCategory.MEDIA,
        ComponentCategory.ANIMATION, ComponentCategory.MAPS, ComponentCategory.SENSORS, ComponentCategory.SOCIAL, ComponentCategory.STORAGE,
        ComponentCategory.CONNECTIVITY, ComponentCategory.LEGOMINDSTORMS, ComponentCategory.EXPERIMENTAL, ComponentCategory.EXTENSION, ComponentCategory.INTERNAL);

    // If a category has a palette helper, add it to the paletteHelpers map here.
    paletteHelpers.put(ComponentCategory.LEGOMINDSTORMS, new LegoPaletteHelper());
  }

  /**
   * Set the filter (if any) for filtering components in the palette. If null
   * is provided for {@code filter}, all components will be shown (identity filter).
   * @param filter A filter instance used for filtering.
   * @param selectFirst If true, selects the first valid stack after applying the filter.
   */
  public void setFilter(Filter filter, boolean selectFirst) {
    this.filter = filter == null ? IDENTITY : filter;
    reloadComponents();
    if (selectFirst) {
      stackPalette.show(0);
    }
  }

  @Override
  public VerticalPanel addComponentCategory(ComponentCategory category) {
    VerticalPanel panel = super.addComponentCategory(category);
    if (category == ComponentCategory.EXTENSION) {
      initExtensionPanel(panel);
    }
    return panel;
  }

  private void initExtensionPanel(VerticalPanel panel) {
    Anchor addComponentAnchor = new Anchor(MESSAGES.importExtensionMenuItem());
    addComponentAnchor.setStylePrimaryName("ode-ExtensionAnchor");
    addComponentAnchor.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ComponentImportWizard().center();
      }
    });

    panel.add(addComponentAnchor);
    panel.setCellHorizontalAlignment(addComponentAnchor, HasHorizontalAlignment.ALIGN_CENTER);
  }
}
