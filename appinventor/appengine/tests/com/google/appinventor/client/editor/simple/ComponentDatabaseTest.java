// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

import com.google.common.io.Files;
import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.properties.json.ServerJsonParser;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface.PropertyDefinition;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

/**
 * Checks basic functionality of the component descriptor database.
 *
 */
public class ComponentDatabaseTest extends TestCase {

  private static final String COMPONENT_DESCRIPTOR_FILE =
      "/build/components/simple_components.json";

  /**
   * Checks whether the component database was correctly initialized.
   */
  public void testComponentDatabase() throws IOException {

    // Load component descriptor file
    String componentDescriptorSource = Files.toString(
        new File(TestUtils.APP_INVENTOR_ROOT_DIR + COMPONENT_DESCRIPTOR_FILE),
        Charset.forName("UTF8"));

    // Parse the data file and check the existence of some key components
    final ComponentDatabase componentDatabase = new ComponentDatabase(
        new ServerJsonParser().parse(componentDescriptorSource).asArray());
    Set<String> components = componentDatabase.getComponentNames();
    assertTrue(components.contains("Button"));
    assertTrue(components.contains("Label"));
    assertTrue(components.contains("TextBox"));

    // Check some properties defined for the TextBox component
    List<PropertyDefinition> properties = componentDatabase.getPropertyDefinitions("TextBox");
    assertEquals("boolean", find(properties, "Enabled").getEditorType());
    assertEquals("non_negative_float", find(properties, "FontSize").getEditorType());
    assertEquals("string", find(properties, "Hint").getEditorType());
  }

  /*
   * Finds the property definition for the property with the given name.
   */
  private static PropertyDefinition find(List<PropertyDefinition> properties, String name) {
    for (PropertyDefinition property : properties) {
      if (property.getName().equals(name)) {
        return property;
      }
    }

    fail("Couldn't find property \"" + name + '"');
    // Will never get here...
    return null;
  }
}
