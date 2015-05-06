// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.components.annotations.DesignerProperty;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.FileObject;

public final class ComponentTranslationGenerator extends ComponentProcessor {
  // Where to write results.
  private static final String OUTPUT_FILE_NAME = "ComponentsTranslation.java";

  private void outputComponent(ComponentInfo component, StringBuilder sb) {
    sb.append("\n\n/* Component: " + component.name + " */\n\n");
    sb.append("\n\n/* Properties */\n\n");
    for (Property prop : component.properties.values()) {
      String propertyName = prop.name;
      if (prop.isUserVisible() || component.designerProperties.containsKey(propertyName)) {
        sb.append("    map.put(\"PROPERTY-" + propertyName + "\", MESSAGES." + propertyName + "Properties());\n");
      }
    }
    sb.append("\n\n/* Events */\n\n");
    for (Event event : component.events.values()) {
      String propertyName = event.name;
      if (event.userVisible) {
        sb.append("    map.put(\"EVENT-" + propertyName + "\", MESSAGES." + propertyName + "Events());\n");
      }
    }
    sb.append("\n\n/* Methods */\n\n");
    for (Method method : component.methods.values()) {
      String propertyName = method.name;
      if (method.userVisible) {
        sb.append("    map.put(\"METHOD-" + propertyName + "\", MESSAGES." + propertyName + "Methods());\n");
      }
    }
  }

  @Override
  protected void outputResults() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("package com.google.appinventor.client;\n");
    sb.append("");
    sb.append("import java.util.HashMap;\n");
    sb.append("import java.util.Map;\n");
    sb.append("");
    sb.append("import static com.google.appinventor.client.Ode.MESSAGES;\n");
    sb.append("");
    sb.append("public class ComponentsTranslation {\n");
    sb.append("  public static Map<String, String> myMap = map();\n\n");
    sb.append("  private static String getName(String key) {\n");
    sb.append("    String value = myMap.get(key);\n");
    sb.append("    if (key == null) {\n");
    sb.append("      return \"**Missing key in ComponentsTranslations**\";\n");
    sb.append("    } else {\n");
    sb.append("      return value;\n");
    sb.append("    }\n");
    sb.append("  }\n\n");
    sb.append("  public static String getPropertyName(String key) {\n");
    sb.append("    return getName(\"PROPERTY-\" + key);\n");
    sb.append("  }\n");
    sb.append("\n");
    sb.append("  public static String getMethodName(String key) {\n");
    sb.append("    return getName(\"METHOD-\" + key);\n");
    sb.append("  }\n");
    sb.append("\n");
    sb.append("  public static String getEventName(String key) {\n");
    sb.append("    return getName(\"EVENT-\" + key);\n");
    sb.append("  }\n");
    sb.append("  public static HashMap<String, String> map() {\n");
    sb.append("    HashMap<String, String> map = new HashMap<String, String>();\n");

    // Components are already sorted.
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();
      outputComponent(component, sb);
    }
    sb.append("  return map;\n");
    sb.append("  }\n");
    sb.append("}\n");
    FileObject src = createOutputFileObject(OUTPUT_FILE_NAME);
    Writer writer = src.openWriter();
    writer.write(sb.toString());
    writer.flush();
    writer.close();
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());
  }

}
