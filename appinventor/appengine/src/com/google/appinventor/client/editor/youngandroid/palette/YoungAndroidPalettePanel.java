// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.youngandroid.palette;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.simple.palette.SimpleComponentDescriptor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidAlignmentChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidAssetSelectorPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidBooleanPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidColorChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidComponentSelectorPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidFontTypefaceChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoNxtSensorPortChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidScreenOrientationChoicePropertyEditor;
import com.google.appinventor.client.widgets.properties.FloatPropertyEditor;
import com.google.appinventor.client.widgets.properties.IntegerPropertyEditor;
import com.google.appinventor.client.widgets.properties.NonNegativeFloatPropertyEditor;
import com.google.appinventor.client.widgets.properties.NonNegativeIntegerPropertyEditor;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.client.widgets.properties.StringPropertyEditor;
import com.google.appinventor.client.widgets.properties.TextPropertyEditor;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface.PropertyDefinition;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel showing Simple components which can be dropped onto the Young Android
 * visual designer panel.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidPalettePanel extends Composite implements SimplePalettePanel {

  // Component database: information about components (including their properties and events)
  private static final SimpleComponentDatabase COMPONENT_DATABASE =
      SimpleComponentDatabase.getInstance();

  // Associated editor
  private final YaFormEditor editor;

  private final StackPanel stackPalette;
  private final Map<ComponentCategory, VerticalPanel> categoryPanels;

  /**
   * Creates a new component palette panel.
   *
   * @param editor    parent editor of this panel
   */
  public YoungAndroidPalettePanel(YaFormEditor editor) {
    this.editor = editor;

    stackPalette = new StackPanel();

    categoryPanels = new HashMap<ComponentCategory, VerticalPanel>();

    for (ComponentCategory category : ComponentCategory.values()) {
      if (showCategory(category)) {
        VerticalPanel categoryPanel = new VerticalPanel();
        categoryPanel.setWidth("100%");
        categoryPanels.put(category, categoryPanel);
        stackPalette.add(categoryPanel, category.getName());
      }
    }

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
   * @param dropTargetProvider  provider of targets that palette items can be
   *                            dropped on
   */
  @Override
  public void loadComponents(DropTargetProvider dropTargetProvider) {
    for (String component : COMPONENT_DATABASE.getComponentNames()) {
      String categoryString = COMPONENT_DATABASE.getCategoryString(component);
      String helpString = COMPONENT_DATABASE.getHelpString(component);
      String categoryDocUrlString = COMPONENT_DATABASE.getCategoryDocUrlString(component);
      Boolean showOnPalette = COMPONENT_DATABASE.getShowOnPalette(component);
      Boolean nonVisible = COMPONENT_DATABASE.getNonVisible(component);
      ComponentCategory category = ComponentCategory.valueOf(categoryString);
      if (showOnPalette && showCategory(category)) {
        addPaletteItem(new SimplePaletteItem(
            new SimpleComponentDescriptor(component, editor, helpString,
                categoryDocUrlString, showOnPalette, nonVisible),
            dropTargetProvider),
                       category);
      }
    }
  }

  @Override
  public void configureComponent(MockComponent mockComponent) {
    String componentType = mockComponent.getType();

    // Configure properties
    for (PropertyDefinition property : COMPONENT_DATABASE.getPropertyDefinitions(componentType)) {
      mockComponent.addProperty(property.getName(), property.getDefaultValue(),
          property.getCaption(), createPropertyEditor(property.getEditorType()));
    }
  }

  /*
   * Creates a new property editor.
   */
  private PropertyEditor createPropertyEditor(String editorType) {
    // TODO(user): use constants
    if (editorType.equals("asset")) {
      return new YoungAndroidAssetSelectorPropertyEditor(editor);
    } else if (editorType.equals("BluetoothClient")) {
      return new YoungAndroidComponentSelectorPropertyEditor(editor,
          Collections.singleton("BluetoothClient"));
    } else if (editorType.equals("boolean")) {
      return new YoungAndroidBooleanPropertyEditor();
    } else if (editorType.equals("color")) {
      return new YoungAndroidColorChoicePropertyEditor();
    } else if (editorType.equals("component")) {
      return new YoungAndroidComponentSelectorPropertyEditor(editor);
    } else if (editorType.equals("float")) {
      return new FloatPropertyEditor();
    } else if (editorType.equals("integer")) {
      return new IntegerPropertyEditor();
    } else if (editorType.equals("lego_nxt_sensor_port")) {
      return new YoungAndroidLegoNxtSensorPortChoicePropertyEditor();
    } else if (editorType.equals("lego_nxt_generated_color")) {
      return new YoungAndroidColorChoicePropertyEditor(
          YoungAndroidColorChoicePropertyEditor.NXT_GENERATED_COLORS);
    } else if (editorType.equals("non_negative_float")) {
      return new NonNegativeFloatPropertyEditor();
    } else if (editorType.equals("non_negative_integer")) {
      return new NonNegativeIntegerPropertyEditor();
    } else if (editorType.equals("screen_orientation")) {
      return new YoungAndroidScreenOrientationChoicePropertyEditor();
    } else if (editorType.equals("string")) {
      return new StringPropertyEditor();
    } else if (editorType.equals("textalignment")) {
      return new YoungAndroidAlignmentChoicePropertyEditor();
    } else if (editorType.equals("typeface")) {
      return new YoungAndroidFontTypefaceChoicePropertyEditor();
    } else {
      return new TextPropertyEditor();
    }
  }

  /*
   * Adds a component entry to the palette.
   */
  private void addPaletteItem(SimplePaletteItem component, ComponentCategory category) {
    VerticalPanel panel = categoryPanels.get(category);
    panel.add(component);
  }
}
