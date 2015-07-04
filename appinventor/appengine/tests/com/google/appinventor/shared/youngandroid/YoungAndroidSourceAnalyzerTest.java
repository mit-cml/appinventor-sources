// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.youngandroid;

import com.google.appinventor.server.properties.json.ServerJsonParser;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONParser;

import junit.framework.TestCase;

/**
 * Unit tests for {@link YoungAndroidSourceAnalyzer}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidSourceAnalyzerTest extends TestCase {
  private static final JSONParser JSON_PARSER = new ServerJsonParser();
  private static final String ENCODED_FORM =
      "{\"YaVersion\":\"1\",\"Source\":\"Form\",\"Properties\":" +
      "{\"$Name\":\"Screen1\",\"$Type\":\"Form\",\"$Version\":\"1\"," +
      "\"Title\":\"Screen1\"}}";
  private static final String ENCODED_FORM_WITH_COMPONENTS =
      "{\"YaVersion\":\"1\",\"Source\":\"Form\",\"Properties\":" +
      "{\"$Name\":\"Screen1\",\"$Type\":\"Form\",\"$Version\":\"1\"," +
      "\"Title\":\"Screen1\"," +
      "\"$Components\":[" +
      "{\"$Name\":\"Button1\",\"$Type\":\"Button\",\"$Version\":\"1\"," +
      "\"Text\":\"Button1\"}," +
      "{\"$Name\":\"Button2\",\"$Type\":\"Button\",\"$Version\":\"1\"," +
      "\"Enabled\":\"False\",\"Text\":\"Button2\"}]}}";
  private static final String ENCODED_FORM_WITH_CONTAINER =
      "{\"YaVersion\":\"1\",\"Source\":\"Form\",\"Properties\":" +
      "{\"$Name\":\"Screen1\",\"$Type\":\"Form\",\"$Version\":\"1\"," +
      "\"Title\":\"Screen1\"," +
      "\"$Components\":[" +
      "{\"$Name\":\"HorizontalArrangement1\",\"$Type\":\"HorizontalArrangement\"," +
      "\"$Version\":\"1\",\"$Components\":[" +
      "{\"$Name\":\"ButtonCancel\",\"$Type\":\"Button\",\"$Version\":\"1\"," +
      "\"Text\":\"Cancel\"}," +
      "{\"$Name\":\"ButtonOk\",\"$Type\":\"Button\",\"$Version\":\"1\"," +
      "\"Text\":\"OK\"}]}]}}";
  private static final String OLD_COMPLETE_FILE_FORM =
      "\n" +
      "#|\n" +
      "$Properties\n" +
      "$YaVersion 1\n" +
      "$Source $Form\n" +
      "$Define Screen1 $As Form $Version 1\n" +
      "Title = \"Screen1\"\n" +
      "$End $Define\n" +
      "$End $Properties\n" +
      "\n" +
      "|#\n" +
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"YaVersion\":\"1\",\"Properties\":{\"$Version\":\"1\"," +
      "\"$Type\":\"Form\",\"$Name\":\"Screen1\",\"Title\":\"Screen1\"}}\n" +
      "|#";
  private static final String OLD_COMPLETE_FILE_FORM_WITH_COMPONENTS =
      "\n" +
      "#|\n" +
      "$Properties\n" +
      "$YaVersion 1\n" +
      "$Source $Form\n" +
      "$Define Screen1 $As Form $Version 1\n" +
      "Title = \"Screen1\"\n" +
      "$Define Button1 $As Button $Version 1\n" +
      "Text = \"Button1\"\n" +
      "$End $Define\n" +
      "$Define Button2 $As Button $Version 1\n" +
      "Enabled = False\n" +
      "Text = \"Button2\"\n" +
      "$End $Define\n" +
      "$End $Define\n" +
      "$End $Properties\n" +
      "\n" +
      "|#\n" +
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"YaVersion\":\"1\",\"Properties\":{\"$Version\":\"1\"," +
      "\"$Components\":[{\"Text\":\"Button1\",\"$Version\":\"1\",\"$Type\":\"Button\"," +
      "\"$Name\":\"Button1\"},{\"Enabled\":\"False\",\"Text\":\"Button2\"," +
      "\"$Version\":\"1\",\"$Type\":\"Button\",\"$Name\":\"Button2\"}],\"$Type\":\"Form\"," +
      "\"$Name\":\"Screen1\",\"Title\":\"Screen1\"}}\n" +
      "|#";
  private static final String OLD_COMPLETE_FILE_FORM_WITH_CONTAINER =
      "\n" +
      "#|\n" +
      "$Properties\n" +
      "$YaVersion 1\n" +
      "$Source $Form\n" +
      "$Define Screen1 $As Form $Version 1\n" +
      "Title = \"Screen1\"\n" +
      "$Define HorizontalArrangement1 $As HorizontalArrangement $Version 1\n" +
      "$Define ButtonCancel $As Button $Version 1\n" +
      "Text = \"Cancel\"\n" +
      "$End $Define\n" +
      "$Define ButtonOk $As Button $Version 1\n" +
      "Text = \"OK\"\n" +
      "$End $Define\n" +
      "$End $Define\n" +
      "$End $Define\n" +
      "$End $Properties\n" +
      "\n" +
      "|#\n" +
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"YaVersion\":\"1\",\"Properties\":{\"$Version\":\"1\"," +
      "\"$Components\":[{\"$Name\":\"HorizontalArrangement1\"," +
      "\"$Type\":\"HorizontalArrangement\"," +
      "\"$Version\":\"1\",\"$Components\":[{\"$Name\":\"ButtonCancel\",\"$Type\":\"Button\"," +
      "\"$Version\":\"1\",\"Text\":\"Cancel\"},{\"$Name\":\"ButtonOk\"," +
      "\"$Type\":\"Button\",\"$Version\":\"1\",\"Text\":\"OK\"}]}],\"$Type\":\"Form\"," +
      "\"$Name\":\"Screen1\",\"Title\":\"Screen1\"}}\n" +
      "|#";
  private static final String OLD_COMPLETE_FILE_FORM_WITH_NEWLINE_IN_PROPERTY =
      "\n" +
      "#|\n" +
      "$Properties\n" +
      "$YaVersion 1\n" +
      "$Source $Form\n" +
      "$Define Screen1 $As Form $Version 1\n" +
      "Title = \"Screen1\"\n" +
      "$Define Label1 $As Label $Version 1\n" +
      "Text = \"Text\n" +
      "for\n" +
      "Label1\"\n" +
      "$End $Define\n" +
      "$End $Define\n" +
      "$End $Properties\n" +
      "\n" +
      "|#\n" +
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"YaVersion\":\"1\",\"Properties\":{\"$Version\":\"1\"," +
      "\"$Components\":[{\"Text\":\"Text\\nfor\\nLabel1\",\"$Version\":\"1\"," +
      "\"$Type\":\"Label\",\"$Name\":\"Label1\"}],\"$Type\":\"Form\",\"$Name\":\"Screen1\"," +
      "\"Title\":\"Screen1\"}}\n" +
      "|#";
  private static final String COMPLETE_FILE_FORM =
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"YaVersion\":\"1\",\"Properties\":{\"$Version\":\"1\"," +
      "\"$Type\":\"Form\",\"$Name\":\"Screen1\",\"Title\":\"Screen1\"}}\n" +
      "|#";
  private static final String COMPLETE_FILE_FORM_WITH_COMPONENTS =
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"YaVersion\":\"1\",\"Properties\":{\"$Version\":\"1\"," +
      "\"$Components\":[{\"Text\":\"Button1\",\"$Version\":\"1\",\"$Type\":\"Button\"," +
      "\"$Name\":\"Button1\"},{\"Enabled\":\"False\",\"Text\":\"Button2\"," +
      "\"$Version\":\"1\",\"$Type\":\"Button\",\"$Name\":\"Button2\"}],\"$Type\":\"Form\"," +
      "\"$Name\":\"Screen1\",\"Title\":\"Screen1\"}}\n" +
      "|#";
  private static final String COMPLETE_FILE_FORM_WITH_CONTAINER =
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"YaVersion\":\"1\",\"Properties\":{\"$Version\":\"1\"," +
      "\"$Components\":[{\"$Name\":\"HorizontalArrangement1\"," +
      "\"$Type\":\"HorizontalArrangement\"," +
      "\"$Version\":\"1\",\"$Components\":[{\"$Name\":\"ButtonCancel\",\"$Type\":\"Button\"," +
      "\"$Version\":\"1\",\"Text\":\"Cancel\"},{\"$Name\":\"ButtonOk\"," +
      "\"$Type\":\"Button\",\"$Version\":\"1\",\"Text\":\"OK\"}]}],\"$Type\":\"Form\"," +
      "\"$Name\":\"Screen1\",\"Title\":\"Screen1\"}}\n" +
      "|#";
  private static final String COMPLETE_FILE_FORM_WITH_NEWLINE_IN_PROPERTY =
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"YaVersion\":\"1\",\"Properties\":{\"$Version\":\"1\"," +
      "\"$Components\":[{\"Text\":\"Text\\nfor\\nLabel1\",\"$Version\":\"1\"," +
      "\"$Type\":\"Label\",\"$Name\":\"Label1\"}],\"$Type\":\"Form\",\"$Name\":\"Screen1\"," +
      "\"Title\":\"Screen1\"}}\n" +
      "|#";

  public void testGenerateSourceFile() {
    // Check empty form
    String encodedProperties = ENCODED_FORM;
    JSONObject propertiesObject = JSON_PARSER.parse(encodedProperties).asObject();
    assertEquals("#|\n" +
        "$JSON\n" +
        propertiesObject.toJson() + "\n" +
        "|#",
        YoungAndroidSourceAnalyzer.generateSourceFile(propertiesObject));

    // Check form with multiple components on it
    encodedProperties = ENCODED_FORM_WITH_COMPONENTS;
    propertiesObject = JSON_PARSER.parse(encodedProperties).asObject();
    assertEquals("#|\n" +
        "$JSON\n" +
        propertiesObject.toJson() + "\n" +
        "|#",
        YoungAndroidSourceAnalyzer.generateSourceFile(propertiesObject));

    // Check form with nested components on it
    encodedProperties = ENCODED_FORM_WITH_CONTAINER;
    propertiesObject = JSON_PARSER.parse(encodedProperties).asObject();
    assertEquals("#|\n" +
        "$JSON\n" +
        propertiesObject.toJson() + "\n" +
        "|#",
        YoungAndroidSourceAnalyzer.generateSourceFile(propertiesObject));
  }

  public void testParseSourceFile() {
    doTestParseSourceFile(OLD_COMPLETE_FILE_FORM, OLD_COMPLETE_FILE_FORM_WITH_COMPONENTS,
        OLD_COMPLETE_FILE_FORM_WITH_CONTAINER, OLD_COMPLETE_FILE_FORM_WITH_NEWLINE_IN_PROPERTY);
    doTestParseSourceFile(COMPLETE_FILE_FORM, COMPLETE_FILE_FORM_WITH_COMPONENTS,
        COMPLETE_FILE_FORM_WITH_CONTAINER, COMPLETE_FILE_FORM_WITH_NEWLINE_IN_PROPERTY);
  }

  public void doTestParseSourceFile(String completeFileForm, String completeFileFormWithComponents,
      String completeFileFormWithContainer, String completeFileFormWithNewlineInProperty) {
    // Check empty form
    JSONObject propertiesObject = YoungAndroidSourceAnalyzer.parseSourceFile(completeFileForm,
        JSON_PARSER);
    assertEquals(3, propertiesObject.getProperties().size());
    assertEquals("1", propertiesObject.get("YaVersion").asString().getString());
    assertEquals("Form", propertiesObject.get("Source").asString().getString());
    JSONObject formProperties = propertiesObject.get("Properties").asObject();
    assertEquals(4, formProperties.getProperties().size());
    assertEquals("Screen1", formProperties.get("$Name").asString().getString());
    assertEquals("Form", formProperties.get("$Type").asString().getString());
    assertEquals("1", formProperties.get("$Version").asString().getString());
    assertEquals("Screen1", formProperties.get("Title").asString().getString());

    // Check form with multiple components on it
    propertiesObject = YoungAndroidSourceAnalyzer.parseSourceFile(
        completeFileFormWithComponents, JSON_PARSER);
    assertEquals(3, propertiesObject.getProperties().size());
    assertEquals("1", propertiesObject.get("YaVersion").asString().getString());
    assertEquals("Form", propertiesObject.get("Source").asString().getString());
    formProperties = propertiesObject.get("Properties").asObject();
    assertEquals(5, formProperties.getProperties().size());
    assertEquals("Screen1", formProperties.get("$Name").asString().getString());
    assertEquals("Form", formProperties.get("$Type").asString().getString());
    assertEquals("1", formProperties.get("$Version").asString().getString());
    assertEquals("Screen1", formProperties.get("Title").asString().getString());
    JSONArray components = formProperties.get("$Components").asArray();
    assertEquals(2, components.size());
    JSONObject button1Properties = components.get(0).asObject();
    assertEquals(4, button1Properties.getProperties().size());
    assertEquals("Button1", button1Properties.get("$Name").asString().getString());
    assertEquals("Button", button1Properties.get("$Type").asString().getString());
    assertEquals("1", button1Properties.get("$Version").asString().getString());
    assertEquals("Button1", button1Properties.get("Text").asString().getString());
    JSONObject button2Properties = components.get(1).asObject();
    assertEquals(5, button2Properties.getProperties().size());
    assertEquals("Button2", button2Properties.get("$Name").asString().getString());
    assertEquals("Button", button2Properties.get("$Type").asString().getString());
    assertEquals("1", button2Properties.get("$Version").asString().getString());
    assertEquals("False", button2Properties.get("Enabled").asString().getString());
    assertEquals("Button2", button2Properties.get("Text").asString().getString());


    // Check form with nested components on it
    propertiesObject = YoungAndroidSourceAnalyzer.parseSourceFile(
        completeFileFormWithContainer, JSON_PARSER);
    assertEquals(3, propertiesObject.getProperties().size());
    assertEquals("1", propertiesObject.get("YaVersion").asString().getString());
    assertEquals("Form", propertiesObject.get("Source").asString().getString());
    formProperties = propertiesObject.get("Properties").asObject();
    assertEquals(5, formProperties.getProperties().size());
    assertEquals("Screen1", formProperties.get("$Name").asString().getString());
    assertEquals("Form", formProperties.get("$Type").asString().getString());
    assertEquals("1", formProperties.get("$Version").asString().getString());
    assertEquals("Screen1", formProperties.get("Title").asString().getString());
    components = formProperties.get("$Components").asArray();
    assertEquals(1, components.size());
    JSONObject horizontalArrangement1Properties = components.get(0).asObject();
    assertEquals(4, horizontalArrangement1Properties.getProperties().size());
    assertEquals("HorizontalArrangement1",
        horizontalArrangement1Properties.get("$Name").asString().getString());
    assertEquals("HorizontalArrangement",
        horizontalArrangement1Properties.get("$Type").asString().getString());
    assertEquals("1", horizontalArrangement1Properties.get("$Version").asString().getString());
    JSONArray nestedComponents = horizontalArrangement1Properties.get("$Components").asArray();
    assertEquals(2, nestedComponents.size());
    JSONObject buttonCancelProperties = nestedComponents.get(0).asObject();
    assertEquals(4, buttonCancelProperties.getProperties().size());
    assertEquals("ButtonCancel", buttonCancelProperties.get("$Name").asString().getString());
    assertEquals("Button", buttonCancelProperties.get("$Type").asString().getString());
    assertEquals("1", buttonCancelProperties.get("$Version").asString().getString());
    assertEquals("Cancel", buttonCancelProperties.get("Text").asString().getString());
    JSONObject buttonOkProperties = nestedComponents.get(1).asObject();
    assertEquals(4, buttonOkProperties.getProperties().size());
    assertEquals("ButtonOk", buttonOkProperties.get("$Name").asString().getString());
    assertEquals("Button", buttonOkProperties.get("$Type").asString().getString());
    assertEquals("1", buttonOkProperties.get("$Version").asString().getString());
    assertEquals("OK", buttonOkProperties.get("Text").asString().getString());

    // Check form with a label that has a newline in its Text property
    propertiesObject = YoungAndroidSourceAnalyzer.parseSourceFile(
        completeFileFormWithNewlineInProperty, JSON_PARSER);
    assertEquals(3, propertiesObject.getProperties().size());
    assertEquals("1", propertiesObject.get("YaVersion").asString().getString());
    assertEquals("Form", propertiesObject.get("Source").asString().getString());
    formProperties = propertiesObject.get("Properties").asObject();
    assertEquals(5, formProperties.getProperties().size());
    assertEquals("Screen1", formProperties.get("$Name").asString().getString());
    assertEquals("Form", formProperties.get("$Type").asString().getString());
    assertEquals("1", formProperties.get("$Version").asString().getString());
    assertEquals("Screen1", formProperties.get("Title").asString().getString());
    components = formProperties.get("$Components").asArray();
    assertEquals(1, components.size());
    JSONObject label1Properties = components.get(0).asObject();
    assertEquals(4, label1Properties.getProperties().size());
    assertEquals("Label1", label1Properties.get("$Name").asString().getString());
    assertEquals("Label", label1Properties.get("$Type").asString().getString());
    assertEquals("1", label1Properties.get("$Version").asString().getString());
    assertEquals("Text\nfor\nLabel1", label1Properties.get("Text").asString().getString());
  }
}
