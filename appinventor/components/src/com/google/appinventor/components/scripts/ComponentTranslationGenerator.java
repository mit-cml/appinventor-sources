// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import java.io.IOException;
import java.io.Writer;
import java.util.TreeSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.FileObject;

public final class ComponentTranslationGenerator extends ComponentProcessor {
  // Where to write results.
  private static final String OUTPUT_FILE_NAME = "ComponentsTranslation.java";

  private void outputComponent(ComponentInfo component, StringBuilder sb) {
    if (component.getExternal()) { // Avoid adding entries for external components
      return;
    }
    Map<String, Parameter> parameters = new LinkedHashMap<String, Parameter>();
    sb.append("\n\n/* Component: " + component.name + " */\n\n");
    sb.append("    map.put(\"COMPONENT-" + component.name + "\", MESSAGES." +
        Character.toLowerCase(component.name.charAt(0)) + component.name.substring(1) +
        "ComponentPallette());\n\n");
    sb.append("    map.put(\"" + component.name + "-helpString\", MESSAGES." +
        component.name + "HelpStringComponentPallette());\n\n");
    sb.append("\n\n/* Properties */\n\n");
    for (Property prop : component.properties.values()) {
      String propertyName = prop.name;
      if (prop.isUserVisible()
          || component.designerProperties.containsKey(propertyName)
          || prop.isDeprecated() // [lyn, 2015/12/30] For deprecated AI2 blocks (but not AI1 blocks)
                                 // must translate property names so they can be displayed in bad blocks.
          ) {
        sb.append("    map.put(\"PROPERTY-" + propertyName + "\", MESSAGES." + propertyName + "Properties());\n");
      }
    }
    sb.append("\n\n/* Events */\n\n");
    for (Event event : component.events.values()) {
      String propertyName = event.name;
      if (event.userVisible
          || event.deprecated // [lyn, 2015/12/30] For deprecated AI2 blocks (but not AI1 blocks)
                              // must translate property names so they can be displayed in bad blocks.
          ) {
        sb.append("    map.put(\"EVENT-" + propertyName + "\", MESSAGES." + propertyName + "Events());\n");
        for (Parameter parameter : event.parameters) {
          parameters.put(parameter.name, parameter);
        }
      }
    }
    sb.append("\n\n/* Methods */\n\n");
    for (Method method : component.methods.values()) {
      String propertyName = method.name;
      if (method.userVisible
          || method.deprecated // [lyn, 2015/12/30] For deprecated AI2 blocks (but not AI1 blocks)
                               // must translate property names so they can be displayed in bad blocks.
          ) {
        sb.append("    map.put(\"METHOD-" + propertyName + "\", MESSAGES." + propertyName + "Methods());\n");
        for (Parameter parameter : method.parameters) {
          parameters.put(parameter.name, parameter);
        }
      }
    }
    sb.append("\n\n/* Parameters */\n\n");
    for (Parameter parameter : parameters.values()) {
      sb.append("    map.put(\"PARAM-" + parameter.name + "\", MESSAGES." +
          Character.toLowerCase(parameter.name.charAt(0)) + parameter.name.substring(1) +
          "Params());\n");
    }
  }

  private void outputCategory(String category, StringBuilder sb) {
    // santize the category name
    String[] parts = category.split(" ");
    sb.append("    map.put(\"CATEGORY-" + category + "\", MESSAGES." + parts[0].replaceAll("[^A-Za-z0-9]", "").toLowerCase());
    for (int i = 1; i < parts.length; i++) {
      String lower = parts[i].replaceAll("[^A-Za-z0-9]", "").toLowerCase();
      sb.append(Character.toUpperCase(lower.charAt(0)));
      sb.append(lower.substring(1));
    }
    sb.append("ComponentPallette());\n");
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
    sb.append("    String value = getName(\"PROPERTY-\" + key);\n");
    sb.append("    if(value == null) return key;\n");
    sb.append("    return value;\n");
    sb.append("  }\n");
    sb.append("\n");
    sb.append("  public static String getMethodName(String key) {\n");
    sb.append("    String value = getName(\"METHOD-\" + key);\n");
    sb.append("    if(value == null) return key;\n");
    sb.append("    return value;\n");
    sb.append("  }\n");
    sb.append("\n");
    sb.append("  public static String getEventName(String key) {\n");
    sb.append("    String value = getName(\"EVENT-\" + key);\n");
    sb.append("    if(value == null) return key;\n");
    sb.append("    return value;\n");
    sb.append("  }\n");
    sb.append("\n");
    sb.append("  public static String getComponentName(String key) {\n");
    sb.append("    String value = getName(\"COMPONENT-\" + key);\n");
    sb.append("    if(value == null) return key;\n");
    sb.append("    return value;\n");
    sb.append("  }\n");
    sb.append("\n");
    sb.append("  public static String getCategoryName(String key) {\n");
    sb.append("    String value = getName(\"CATEGORY-\" + key);\n");
    sb.append("    if(value == null) return key;\n");
    sb.append("    return value;\n");
    sb.append("  }\n");
    sb.append("\n");
    sb.append("  public static String getComponentHelpString(String key) {\n");
    sb.append("    String value = getName(key + \"-helpString\");\n");
    sb.append("    if(value == null) return key;\n");
    sb.append("    return value;\n");
    sb.append("  }\n");
    sb.append("  public static HashMap<String, String> map() {\n");
    sb.append("    HashMap<String, String> map = new HashMap<String, String>();\n");

    // Components are already sorted.
    Set<String> categories = new TreeSet<String>();
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();
      outputComponent(component, sb);
      categories.add(component.getCategory());
    }
    sb.append("\n\n    /* Categories */\n\n");
    for (String category : categories) {
      outputCategory(category, sb);
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
