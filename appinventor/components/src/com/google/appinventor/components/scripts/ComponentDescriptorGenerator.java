// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.scripts;

import com.google.appinventor.components.annotations.DesignerProperty;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

/**
 * Tool to generate simple component descriptors as JSON.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class ComponentDescriptorGenerator extends ComponentProcessor {
  // Where to write results.
  private static final String OUTPUT_FILE_NAME = "simple_components.json";

  private static void outputComponent(ComponentInfo component, StringBuilder sb) {
    sb.append("{ \"name\": \"");
    sb.append(component.name);
    sb.append("\",\n  \"version\": \"");
    sb.append(component.getVersion());
    sb.append("\",\n  \"categoryString\": \"");
    sb.append(component.getCategoryString());
    sb.append("\",\n  \"helpString\": \"");
    // Internal double quotes get converted to single quotes to avoid
    // confusing JSON parser at other end.
    sb.append(component.getHelpDescription().replaceAll("\"", "'"));
    sb.append("\",\n  \"showOnPalette\": \"");
    sb.append(component.getShowOnPalette());
    sb.append("\",\n  \"nonVisible\": \"");
    sb.append(component.getNonVisible());
    sb.append("\",\n  \"iconName\": \"");
    sb.append(component.getIconName());
    sb.append("\",\n  \"properties\": [");
    String separator = "";
    for (Map.Entry<String, DesignerProperty> entry : component.designerProperties.entrySet()) {
      String propertyName = entry.getKey();
      DesignerProperty dp = entry.getValue();
      sb.append(separator);
      outputProperty(propertyName, dp, sb);
      separator = ",\n";
    }
    sb.append("],\n  \"events\": [");
    // We don't care about events in the designer, so the array is left empty.
    sb.append("]}\n");
  }

  private static void outputProperty(String propertyName, DesignerProperty dp, StringBuilder sb) {
    sb.append("{ \"name\": \"");
    sb.append(propertyName);
    sb.append("\", \"editorType\": \"");
    sb.append(dp.editorType());
    sb.append("\", \"defaultValue\": \"");
    sb.append(dp.defaultValue().replace("\"", "\\\""));
    sb.append("\"}");
  }

  @Override
  protected void outputResults() throws IOException {
    StringBuilder sb = new StringBuilder();

    sb.append('[');
    String separator = "";

    // Components are already sorted.
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();
      sb.append(separator);
      outputComponent(component, sb);
      separator = ",\n";
    }

    sb.append(']');

    FileObject src = createOutputFileObject(OUTPUT_FILE_NAME);
    Writer writer = src.openWriter();
    writer.write(sb.toString());
    writer.flush();
    writer.close();
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());
  }
}
