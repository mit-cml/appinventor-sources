// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.palette;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.TranslationDesignerPallete;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.simple.palette.SimpleComponentDescriptor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidAccelerometerSensitivityChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidAlignmentChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidAssetSelectorPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidBooleanPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidButtonShapeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidColorChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidComponentSelectorPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidDefaultURLPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidFontTypefaceChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidHorizontalAlignmentChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoNxtSensorPortChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoEv3SensorPortChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoEv3ColorSensorModeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoEv3UltrasonicSensorModeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoEv3GyroSensorModeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidScreenAnimationChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidScreenOrientationChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidSensorDistIntervalChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidSensorTimeIntervalChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidSizingChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidToastLengthChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidVerticalAlignmentChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidTextReceivingPropertyEditor;
import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.client.widgets.properties.CountryChoicePropertyEditor;
import com.google.appinventor.client.widgets.properties.FloatPropertyEditor;
import com.google.appinventor.client.widgets.properties.IntegerPropertyEditor;
import com.google.appinventor.client.widgets.properties.LanguageChoicePropertyEditor;
import com.google.appinventor.client.widgets.properties.NonNegativeFloatPropertyEditor;
import com.google.appinventor.client.widgets.properties.NonNegativeIntegerPropertyEditor;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.client.widgets.properties.ScalingChoicePropertyEditor;
import com.google.appinventor.client.widgets.properties.StringPropertyEditor;
import com.google.appinventor.client.widgets.properties.TextPropertyEditor;
import com.google.appinventor.client.widgets.properties.TextAreaPropertyEditor;
import com.google.appinventor.client.wizards.ComponentImportWizard;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface.PropertyDefinition;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel showing Simple components which can be dropped onto the Young Android
 * visual designer panel.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidPalettePanel extends Composite implements SimplePalettePanel, ComponentDatabaseChangeListener {

  // Component database: information about components (including their properties and events)
  private final SimpleComponentDatabase COMPONENT_DATABASE;

  // Associated editor
  private final YaFormEditor editor;

  private final Map<ComponentCategory, PaletteHelper> paletteHelpers;

  private final StackPanel stackPalette;
  private final Map<ComponentCategory, VerticalPanel> categoryPanels;
  // store Component Type along with SimplePaleteItem to enable removal of components
  private final Map<String, SimplePaletteItem> simplePaletteItems;

  private DropTargetProvider dropTargetProvider;

  /**
   * Creates a new component palette panel.
   *
   * @param editor parent editor of this panel
   */
  public YoungAndroidPalettePanel(YaFormEditor editor) {
    this.editor = editor;
    COMPONENT_DATABASE = SimpleComponentDatabase.getInstance(editor.getProjectId());

    stackPalette = new StackPanel();

    paletteHelpers = new HashMap<ComponentCategory, PaletteHelper>();
    // If a category has a palette helper, add it to the paletteHelpers map here.
    paletteHelpers.put(ComponentCategory.LEGOMINDSTORMS, new LegoPaletteHelper());

    categoryPanels = new HashMap<ComponentCategory, VerticalPanel>();
    simplePaletteItems = new HashMap<String, SimplePaletteItem>();

    for (ComponentCategory category : ComponentCategory.values()) {
      if (showCategory(category)) {
        VerticalPanel categoryPanel = new VerticalPanel();
        categoryPanel.setWidth("100%");
        categoryPanels.put(category, categoryPanel);
        stackPalette.add(categoryPanel,
            TranslationDesignerPallete.getCorrespondingString(category.getName()));
      }
    }

    initExtensionPanel();

    stackPalette.setWidth("100%");
    initWidget(stackPalette);
  }

  private static boolean showCategory(ComponentCategory category) {
    if (category == ComponentCategory.UNINITIALIZED) {
      return false;
    }
    if (category == ComponentCategory.INTERNAL &&
        !AppInventorFeatures.showInternalComponentsCategory()) {
      return false;
    }
    return true;
  }

  /**
   * Loads all components to be shown on this palette.  Specifically, for
   * each component (except for those whose category is UNINITIALIZED, or
   * whose category is INTERNAL and we're running on a production server,
   * or who are specifically marked as not to be shown on the palette),
   * this creates a corresponding {@link SimplePaletteItem} with the passed
   * {@link DropTargetProvider} and adds it to the panel corresponding to
   * its category.
   *
   * @param dropTargetProvider provider of targets that palette items can be
   *                           dropped on
   */
  @Override
  public void loadComponents(DropTargetProvider dropTargetProvider) {
    this.dropTargetProvider = dropTargetProvider;
    for (String component : COMPONENT_DATABASE.getComponentNames()) {
      this.addComponent(component);
    }
  }

  public void loadComponents() {
    for (String component : COMPONENT_DATABASE.getComponentNames()) {
      this.addComponent(component);
    }
  }

  @Override
  public void configureComponent(MockComponent mockComponent) {
    String componentType = mockComponent.getType();

    // Configure properties
    for (PropertyDefinition property : COMPONENT_DATABASE.getPropertyDefinitions(componentType)) {
      mockComponent.addProperty(property.getName(), property.getDefaultValue(),
              ComponentsTranslation.getPropertyName(property.getCaption()),
              createPropertyEditor(property.getEditorType()));
      /*OdeLog.log("Property Caption: " + property.getCaption() + ", "
          + TranslationComponentProperty.getName(property.getCaption()));*/
    }
  }

  /**
   *  Loads a single Component to Palette. Used for adding Components.
   */
  @Override
  public void addComponent(String componentTypeName) {
    String helpString = COMPONENT_DATABASE.getHelpString(componentTypeName);
    String categoryDocUrlString = COMPONENT_DATABASE.getCategoryDocUrlString(componentTypeName);
    String categoryString = COMPONENT_DATABASE.getCategoryString(componentTypeName);
    Boolean showOnPalette = COMPONENT_DATABASE.getShowOnPalette(componentTypeName);
    Boolean nonVisible = COMPONENT_DATABASE.getNonVisible(componentTypeName);
    Boolean external = COMPONENT_DATABASE.getComponentExternal(componentTypeName);
    ComponentCategory category = ComponentCategory.valueOf(categoryString);
    if (showOnPalette && showCategory(category)) {
      SimplePaletteItem item = new SimplePaletteItem(
          new SimpleComponentDescriptor(componentTypeName, editor, helpString,
              categoryDocUrlString, showOnPalette, nonVisible, external),
            dropTargetProvider);
      simplePaletteItems.put(componentTypeName, item);
      addPaletteItem(item, category);
    }
  }

  public void removeComponent(String componentTypeName) {
    String categoryString = COMPONENT_DATABASE.getCategoryString(componentTypeName);
    ComponentCategory category = ComponentCategory.valueOf(categoryString);
    removePaletteItem(simplePaletteItems.get(componentTypeName), category);
  }

  /*
   * Creates a new property editor.
   */
  private PropertyEditor createPropertyEditor(String editorType) {
    if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT)) {
      return new YoungAndroidHorizontalAlignmentChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_VERTICAL_ALIGNMENT)) {
      return new YoungAndroidVerticalAlignmentChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_ASSET)) {
      return new YoungAndroidAssetSelectorPropertyEditor(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_BLUETOOTHCLIENT)) {
      return new YoungAndroidComponentSelectorPropertyEditor(editor,
          // Pass the set of component types that will be shown in the property editor,
          // in this case, just "BluetoothClient".
          Collections.singleton("BluetoothClient"));
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN)) {
      return new YoungAndroidBooleanPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_BUTTON_SHAPE)) {
      return new YoungAndroidButtonShapeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_COLOR)) {
      return new YoungAndroidColorChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_COMPONENT)) {
      return new YoungAndroidComponentSelectorPropertyEditor(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_FLOAT)) {
      return new FloatPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_INTEGER)) {
      return new IntegerPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_SENSOR_PORT)) {
      return new YoungAndroidLegoEv3SensorPortChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_COLOR_SENSOR_MODE)) {
      return new YoungAndroidLegoEv3ColorSensorModeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_ULTRASONIC_SENSOR_MODE)) {
      return new YoungAndroidLegoEv3UltrasonicSensorModeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_GYRO_SENSOR_MODE)) {
      return new YoungAndroidLegoEv3GyroSensorModeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_NXT_SENSOR_PORT)) {
      return new YoungAndroidLegoNxtSensorPortChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_NXT_GENERATED_COLOR)) {
      return new YoungAndroidColorChoicePropertyEditor(
          YoungAndroidColorChoicePropertyEditor.NXT_GENERATED_COLORS);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT)) {
      return new NonNegativeFloatPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER)) {
      return new NonNegativeIntegerPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SCREEN_ORIENTATION)) {
      return new YoungAndroidScreenOrientationChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SCREEN_ANIMATION)) {
      return new YoungAndroidScreenAnimationChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SENSOR_DIST_INTERVAL)) {
      return new YoungAndroidSensorDistIntervalChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SENSOR_TIME_INTERVAL)) {
      return new YoungAndroidSensorTimeIntervalChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_STRING)) {
      return new StringPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXTALIGNMENT)) {
      return new YoungAndroidAlignmentChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA)) {
      return new TextAreaPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TOAST_LENGTH)) {
      return new YoungAndroidToastLengthChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE)) {
      return new YoungAndroidFontTypefaceChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY)) {
      return new YoungAndroidBooleanPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXT_RECEIVING)) {
      return new YoungAndroidTextReceivingPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_ACCELEROMETER_SENSITIVITY)) {
      return new YoungAndroidAccelerometerSensitivityChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXT_TO_SPEECH_COUNTRIES)) {
      return new CountryChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXT_TO_SPEECH_LANGUAGES)) {
      return new LanguageChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SIZING)) {
      return new YoungAndroidSizingChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SCALING)) {
      return new ScalingChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_FIREBASE_URL)) {
      return new YoungAndroidDefaultURLPropertyEditor("DEFAULT");
    } else {
      return new TextPropertyEditor();
    }
  }

  /*
   * Adds a component entry to the palette.
   */
  private void addPaletteItem(SimplePaletteItem component, ComponentCategory category) {
    VerticalPanel panel = categoryPanels.get(category);
    PaletteHelper paletteHelper = paletteHelpers.get(category);
    if (paletteHelper != null) {
      paletteHelper.addPaletteItem(panel, component);
    } else {
      panel.add(component);
    }
  }

  private void removePaletteItem(SimplePaletteItem component, ComponentCategory category) {
    VerticalPanel panel = categoryPanels.get(category);
    panel.remove(component);
  }

  private void initExtensionPanel() {
    Anchor addComponentAnchor = new Anchor("Import extension");
    addComponentAnchor.setStylePrimaryName("ode-ExtensionAnchor");
    addComponentAnchor.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ComponentImportWizard().center();
      }
    });

    categoryPanels.get(ComponentCategory.EXTENSION).add(addComponentAnchor);
    categoryPanels.get(ComponentCategory.EXTENSION).setCellHorizontalAlignment(
        addComponentAnchor, HasHorizontalAlignment.ALIGN_CENTER);
  }

  @Override
  public void onComponentTypeAdded(List<String> componentTypes) {
    for (String componentType : componentTypes) {
      this.addComponent(componentType);
    }
  }

  @Override
  public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
    boolean result = true;
    for (String componentType : componentTypes) {
      this.removeComponent(componentType);
    }
    return result;
  }

  @Override
  public void onComponentTypeRemoved(Map<String, String> componentTypes) {

  }

  @Override
  public void onResetDatabase() {
    reloadComponents();
  }

  @Override
  public void clearComponents() {
    for (ComponentCategory category : categoryPanels.keySet()) {
      VerticalPanel panel = categoryPanels.get(category);
      panel.clear();
    }
  }

  @Override
  public void reloadComponents() {
    clearComponents();
    loadComponents();
  }

}
