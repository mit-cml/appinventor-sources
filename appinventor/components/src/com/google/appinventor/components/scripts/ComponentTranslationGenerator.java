// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;

public final class ComponentTranslationGenerator extends ComponentProcessor {
  // Where to write results.
  private static final String OUTPUT_FILE_NAME = "ComponentTranslationTable.java";
  private static final String AUTOGEN_OUTPUT_FILE_NAME = "ComponentInfoTranslations.java";
  private static final String AUTOGEN_PROPERTY_FILE_NAME = "ComponentPropertyTranslations.java";
  private static final String AUTOGEN_METHOD_FILE_NAME = "ComponentMethodTranslations.java";
  private static final String AUTOGEN_EVENT_FILE_NAME = "ComponentEventTranslations.java";

  private Map<String, String> tooltipProperties = new TreeMap<>();
  private Map<String, String> tooltipMethods = new TreeMap<>();
  private Map<String, String> tooltipEvents = new TreeMap<>();
  private Map<String, Set<String>> tooltipComponent = new TreeMap<>();
  private Set<String> collisionKeys = new HashSet<>();
  private Set<String> writtenKeys = new HashSet<>();

  private void outputComponent(ComponentInfo component, Set<String> outProperties,
      Set<String> outMethods, Set<String> outEvents, StringBuilder sb) {
    if (component.getExternal()) { // Avoid adding entries for external components
      return;
    }
    Map<String, Parameter> parameters = new LinkedHashMap<String, Parameter>();
    sb.append("\n\n/* Component: " + component.getName() + " */\n\n");
    sb.append("    map.put(\"COMPONENT-" + component.getName() + "\", MESSAGES."
        + Character.toLowerCase(component.getName().charAt(0)) + component.getName().substring(1)
        + "ComponentPallette());\n\n");
    sb.append("    map.put(\"" + component.getName() + "-helpString\", MESSAGES."
        + component.getName() + "HelpStringComponentPallette());\n\n");
    sb.append("\n\n/* Properties */\n\n");
    for (Property prop : component.properties.values()) {
      String propertyName = prop.name;
      if (prop.isUserVisible()
          || component.designerProperties.containsKey(propertyName)
          || prop.isDeprecated() // [lyn, 2015/12/30] For deprecated AI2 blocks (but not AI1 blocks)
                                 // must translate property names so they can be displayed in bad blocks.
          ) {
        String key = "PROPERTY-" + propertyName;
        if (writtenKeys.contains(key)) {
          continue;
        }
        sb.append("    map.put(\"" + key + "\", MESSAGES." + propertyName + "Properties());\n");
        outProperties.add(propertyName);
        writtenKeys.add(key);
      }
    }

    sb.append("\n\n/* Events */\n\n");
    for (Event event : component.events.values()) {
      String propertyName = event.name;
      if (event.userVisible
          || event.deprecated // [lyn, 2015/12/30] For deprecated AI2 blocks (but not AI1 blocks)
                              // must translate property names so they can be displayed in bad blocks.
          ) {
        String key = "EVENT-" + propertyName;
        if (!writtenKeys.contains(key)) {
          sb.append("    map.put(\"" + key + "\", MESSAGES." + propertyName + "Events());\n");
        }
        for (Parameter parameter : event.parameters) {
          parameters.put(parameter.name, parameter);
        }
        outEvents.add(propertyName);
        writtenKeys.add(key);
      }
    }

    sb.append("\n\n/* Methods */\n\n");
    for (Method method : component.methods.values()) {
      String propertyName = method.name;
      if (method.userVisible
          || method.deprecated // [lyn, 2015/12/30] For deprecated AI2 blocks (but not AI1 blocks)
                               // must translate property names so they can be displayed in bad blocks.
          ) {
        String key = "METHOD-" + propertyName;
        if (!writtenKeys.contains(key)) {
          sb.append("    map.put(\"" + key + "\", MESSAGES." + propertyName + "Methods());\n");
        }
        for (Parameter parameter : method.parameters) {
          parameters.put(parameter.name, parameter);
        }
        outMethods.add(propertyName);
        writtenKeys.add(key);
      }
    }

    sb.append("\n\n/* Parameters */\n\n");
    // TODO: Instead of compiling the names here, can we just create a list instead of a map?
    ArrayList<String> names = new ArrayList();
    for (Parameter parameter : parameters.values()) {
      names.add(parameter.name);
    }
    // This special case adds the notAlreadyHandled parameter, which is the second parameter for the
    // generic event handlers. Since it's not explicitly declared in any event handler, we add it
    // here for internationalization.
    names.add("notAlreadyHandled");
    for (String name : names) {
      String key = "PARAM-" + name;
      if (writtenKeys.contains(key)) {
        continue;
      }
      sb.append("    map.put(\"").append(key).append("\", MESSAGES.")
          .append(Character.toLowerCase(name.charAt(0))).append(name.substring(1))
          .append("Params());\n");
      writtenKeys.add(key);
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

  private void outputPropertyCategory(String category, StringBuilder sb) {
    sb.append("    map.put(\"CATEGORY-");
    sb.append(category);
    sb.append("\", MESSAGES.");
    sb.append(category);
    sb.append("PropertyCategory());\n");
  }

  private void outputComponentAutogen(ComponentInfo component,
      Map<String, Property> outProperties, Map<String, Method> outMethods,
      Map<String, Event> outEvents, Map<String, Parameter> outParams,
      StringBuilder sb) {
    sb.append("  @DefaultMessage(\"");
    sb.append(component.getName());
    sb.append("\")\n");
    sb.append("  @Description(\"\")\n");
    sb.append("  String ");
    sb.append(Character.toLowerCase(component.getName().charAt(0)));
    sb.append(component.getName().substring(1));
    sb.append("ComponentPallette();\n\n");
    sb.append("  @DefaultMessage(\"");
    sb.append(sanitize(component.description));
    sb.append("\")\n");
    sb.append("  @Description(\"\")\n");
    sb.append("  String ");
    sb.append(component.getName());
    sb.append("HelpStringComponentPallette();\n\n");
    for (Property property : component.properties.values()) {
      if (property.isUserVisible() || component.designerProperties.containsKey(property.name) ||
          property.isDeprecated()) {
        outProperties.put(property.name, property);
      }
    }
    for (Method method : component.methods.values()) {
      if (method.userVisible || method.deprecated) {
        outMethods.put(method.name, method);
        for (Parameter p : method.parameters) {
          String name = Character.toLowerCase(p.name.charAt(0)) + p.name.substring(1);
          outParams.put(name, p);
        }
      }
    }
    for (Event event : component.events.values()) {
      if (event.userVisible || event.deprecated) {
        outEvents.put(event.name, event);
        for (Parameter p : event.parameters) {
          String name = Character.toLowerCase(p.name.charAt(0)) + p.name.substring(1);
          outParams.put(name, p);
        }
      }
    }
  }

  private void outputPropertyAutogen(Property property, StringBuilder sb) {
    sb.append("  @DefaultMessage(\"");
    sb.append(sanitize(property.name));
    sb.append("\")\n");
    sb.append("  @Description(\"\")\n");
    sb.append("  String ");
    sb.append(property.name);
    sb.append("Properties();\n\n");
  }

  private void outputMethodAutogen(Method method, StringBuilder sb) {
    sb.append("  @DefaultMessage(\"");
    sb.append(sanitize(method.name));
    sb.append("\")\n");
    sb.append("  @Description(\"\")\n");
    sb.append("  String ");
    sb.append(method.name);
    sb.append("Methods();\n\n");
  }

  private void outputEventAutogen(Event event, StringBuilder sb) {
    sb.append("  @DefaultMessage(\"");
    sb.append(sanitize(event.name));
    sb.append("\")\n");
    sb.append("  @Description(\"\")\n");
    sb.append("  String ");
    sb.append(event.name);
    sb.append("Events();\n\n");
  }

  private void outputParameterAutogen(Parameter parameter, StringBuilder sb) {
    sb.append("  @DefaultMessage(\"");
    sb.append(sanitize(parameter.name));
    sb.append("\")\n");
    sb.append("  @Description(\"\")\n");
    sb.append("  String ");
    sb.append(Character.toLowerCase(parameter.name.charAt(0)));
    sb.append(parameter.name.substring(1));
    sb.append("Params();\n\n");
  }

  private void outputCategoryAutogen(String category, StringBuilder sb) {
    String[] parts = category.split(" ");
    sb.append("  @DefaultMessage(\"");
    sb.append(category);
    sb.append("\")\n");
    sb.append("  @Description(\"\")\n");
    sb.append("  String ");
    sb.append(parts[0].replaceAll("[^A-Za-z0-9]", "").toLowerCase());
    for (int i = 1; i < parts.length; i++) {
      String lower = parts[i].replaceAll("[^A-Za-z0-9]", "").toLowerCase();
      sb.append(Character.toUpperCase(lower.charAt(0)));
      sb.append(lower.substring(1));
    }
    sb.append("ComponentPallette();\n\n");
  }

  private void outputPropertyCategoryAutogen(String category, StringBuilder sb) {
    sb.append("  @DefaultMessage(\"");
    sb.append(category);
    sb.append("\")\n");
    sb.append("  @Description(\"\")\n");
    sb.append("  String ");
    sb.append(category);
    sb.append("PropertyCategory();\n\n");
  }

  private String sanitize(String input) {
    return input.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\\\\", "\\\\\\\\")
        .replaceAll("\"", "\\\\\"").replaceAll("'", "''").replaceAll("[ \t]+", " ").trim();
  }

  private void storeTooltip(ComponentInfo component, String name, String suffix,
      String description, Map<String, String> tooltipMap) {
    String key = name + suffix;
    String value = tooltipMap.get(key);
    if (collisionKeys.contains(key)) {
      // Already detected a collision
      key = component.getName() + "__" + key;
      tooltipMap.put(key, description);
    } else if (value == null) {
      // This is the first observation of this key
      tooltipMap.put(key, description);
      Set<String> components = new HashSet<>();
      components.add(component.getName());
      tooltipComponent.put(key, components);
    } else if (!value.equals(description)) {
      // Descriptions don't match == collision!
      collisionKeys.add(key);
      for (String componentName : tooltipComponent.get(key)) {
        tooltipMap.put(componentName + "__" + key, value);
      }
      key = component.getName() + "__" + key;
      tooltipMap.put(key, description);
    } else {
      // Two (or more) components have the exact same description. Technically not a collision, but
      // we need to do some bookkeeping in case a collision is detected with another component.
      tooltipComponent.get(key).add(component.getName());
    }
  }

  private void computeTooltipMap(ComponentInfo component) {
    for (Property property : component.properties.values()) {
      storeTooltip(component, property.name, "PropertyDescriptions", property.getDescription(), tooltipProperties);
    }
    for (Method method : component.methods.values()) {
      storeTooltip(component, method.name, "MethodDescriptions", method.description, tooltipMethods);
    }
    for (Event event : component.events.values()) {
      storeTooltip(component, event.name, "EventDescriptions", event.description, tooltipEvents);
    }
  }

  protected void outputAutogenOdeMessages() throws IOException {
    Set<String> categories = new TreeSet<>();
    Map<String, Property> properties = new TreeMap<>();
    Map<String, Method> methods = new TreeMap<>();
    Map<String, Event> events = new TreeMap<>();
    Map<String, Parameter> parameters = new TreeMap<>();
    StringBuilder sb_info = new StringBuilder();
    StringBuilder sb_properties = new StringBuilder();
    StringBuilder sb_methods = new StringBuilder();
    StringBuilder sb_events = new StringBuilder();
    sb_info.append("// THIS FILE IS AUTOMATICALLY GENERATED DURING COMPILATION.\n");
    sb_info.append("// DO NOT EDIT THIS FILE. ANY CHANGES WILL BE OVERWRITTEN.\n\n");
    sb_info.append("package com.google.appinventor.client.editor.simple.components.i18n;\n\n");
    sb_info.append("import com.google.gwt.i18n.client.Messages;\n\n");
    sb_properties.append(sb_info);
    sb_methods.append(sb_info);
    sb_events.append(sb_info);

    sb_info.append("public interface ComponentInfoTranslations extends Messages {\n");
    sb_properties.append("public interface ComponentPropertyTranslations extends Messages {\n");
    sb_methods.append("public interface ComponentMethodTranslations extends Messages {\n");
    sb_events.append("public interface ComponentEventTranslations extends Messages {\n");
    sb_info.append("\n  /* Components */\n");
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();
      outputComponentAutogen(component, properties, methods, events, parameters, sb_info);
      computeTooltipMap(component);
      categories.add(component.getCategory());
    }
    sb_info.append("\n  /* Component Categories */\n");
    for (String category : categories) {
      outputCategoryAutogen(category, sb_info);
    }
    sb_properties.append("\n  /* Options */\n");
    for (Map.Entry<String, OptionList> entry : optionLists.entrySet()) {
      OptionList optionList = entry.getValue();
      outputOptionListAutogen(optionList, sb_properties);
    }
    for (String key : collisionKeys) {
      tooltipProperties.remove(key);
      tooltipEvents.remove(key);
      tooltipMethods.remove(key);
    }
    sb_properties.append("\n  /* Properties */\n");
    for (Map.Entry<String, Property> entry : properties.entrySet()) {
      outputPropertyAutogen(entry.getValue(), sb_properties);
    }
    sb_properties.append("\n  /* Property Descriptions */\n");
    for (Map.Entry<String, String> entry : tooltipProperties.entrySet()) {
      sb_properties.append("  @DefaultMessage(\"");
      sb_properties.append(sanitize(entry.getValue()));
      sb_properties.append("\")\n");
      sb_properties.append("  @Description(\"\")\n");
      sb_properties.append("  String ");
      sb_properties.append(entry.getKey());
      sb_properties.append("();\n\n");
    }
    sb_properties.append("\n  /* Property Categories */\n");
    outputPropertyCategoryAutogen("Appearance", sb_properties);
    outputPropertyCategoryAutogen("Behavior", sb_properties);
    outputPropertyCategoryAutogen("Unspecified", sb_properties);

    sb_methods.append("\n  /* Methods */\n");
    for (Map.Entry<String, Method> entry : methods.entrySet()) {
      outputMethodAutogen(entry.getValue(), sb_methods);
    }
    sb_methods.append("\n  /* Method Descriptions */\n");
    for (Map.Entry<String, String> entry : tooltipMethods.entrySet()) {
      sb_methods.append("  @DefaultMessage(\"");
      sb_methods.append(sanitize(entry.getValue()));
      sb_methods.append("\")\n");
      sb_methods.append("  @Description(\"\")\n");
      sb_methods.append("  String ");
      sb_methods.append(entry.getKey());
      sb_methods.append("();\n\n");
    }
    sb_methods.append("\n  /* Parameters */\n");
    parameters.put("notAlreadyHandled", new Parameter("notAlreadyHandled", null));
    for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
      outputParameterAutogen(entry.getValue(), sb_methods);
    }
    sb_events.append("\n  /* Events */\n");
    for (Map.Entry<String, Event> entry : events.entrySet()) {
      outputEventAutogen(entry.getValue(), sb_events);
    }
    sb_events.append("\n  /* Event Descriptions */\n");
    for (Map.Entry<String, String> entry : tooltipEvents.entrySet()) {
      sb_events.append("  @DefaultMessage(\"");
      sb_events.append(sanitize(entry.getValue()));
      sb_events.append("\")\n");
      sb_events.append("  @Description(\"\")\n");
      sb_events.append("  String ");
      sb_events.append(entry.getKey());
      sb_events.append("();\n\n");
    }

    sb_info.append("}\n");
    sb_properties.append("}\n");
    sb_methods.append("}\n");
    sb_events.append("}\n");
    FileObject src = createOutputFileObject(AUTOGEN_OUTPUT_FILE_NAME);
    Writer writer = src.openWriter();
    writer.write(sb_info.toString());
    writer.flush();
    writer.close();
    messager.printMessage(Kind.NOTE, "Wrote file " + src.toUri());

    FileObject src2 = createOutputFileObject(AUTOGEN_PROPERTY_FILE_NAME);
    Writer writer2 = src2.openWriter();
    writer2.write(sb_properties.toString());
    writer2.flush();
    writer2.close();
    messager.printMessage(Kind.NOTE, "Wrote file " + src2.toUri());

    FileObject src3 = createOutputFileObject(AUTOGEN_METHOD_FILE_NAME);
    Writer writer3 = src3.openWriter();
    writer3.write(sb_methods.toString());
    writer3.flush();
    writer3.close();
    messager.printMessage(Kind.NOTE, "Wrote file " + src3.toUri());

    FileObject src4 = createOutputFileObject(AUTOGEN_EVENT_FILE_NAME);
    Writer writer4 = src4.openWriter();
    writer4.write(sb_events.toString());
    writer4.flush();
    writer4.close();
    messager.printMessage(Kind.NOTE, "Wrote file " + src4.toUri());
  }

  protected void outputOptionList(OptionList optionList, StringBuilder sb) {
    String tagName = optionList.getTagName();
    sb.append("\n\n/* OptionList ");
    sb.append(tagName);
    sb.append(" */\n\n");

    // Translate tag.
    String lowerTagName = Character.toLowerCase(tagName.charAt(0))
        + tagName.substring(1);
    sb.append("     map.put(\"OPTIONLIST-")
      .append(tagName)
      .append("\", MESSAGES.")
      .append(lowerTagName)
      .append("OptionList());\n");

    // Translate options.
    for (Option option : optionList.asCollection()) {
      sb.append("    map.put(\"OPTION-")
        .append(tagName)
        .append(option.name)
        .append("\", MESSAGES.")
        .append(lowerTagName)
        .append(option.name)
        .append("Option());\n");
    }
  }

  private void outputOptionListAutogen(OptionList optionList, StringBuilder sb) {
    String tagName = optionList.getTagName();
    String lowerTagName = Character.toLowerCase(tagName.charAt(0))
        + tagName.substring(1);

    sb.append("  @DefaultMessage(\"")
      .append(sanitize(tagName))
      .append("\")\n")
      .append("  @Description(\"\")\n")
      .append("  String ")
      .append(lowerTagName)
      .append("OptionList();\n\n");

    for (Option option : optionList.asCollection()) {
      sb.append("  @DefaultMessage(\"")
        .append(sanitize(option.name))
        .append("\")\n")
        .append("  @Description(\"\")\n")
        .append("  String ")
        .append(lowerTagName)
        .append(option.name)
        .append("Option();\n\n");
    }
  }


  @Override
  protected void outputResults() throws IOException {
    outputAutogenOdeMessages();
    StringBuilder sb = new StringBuilder();
    sb.append("package com.google.appinventor.client.editor.simple.components.i18n;\n");
    sb.append("\n");
    sb.append("import java.util.HashMap;\n");
    sb.append("import java.util.Map;\n");
    sb.append("\n");
    sb.append("import com.google.gwt.core.client.GWT;\n");
    sb.append("\n");
    sb.append("public class ComponentTranslationTable {\n");
    sb.append("  private static final ComponentTranslations MESSAGES = GWT.create(ComponentTranslations.class);\n");
    sb.append("  public static Map<String, String> myMap = map();\n\n");
    sb.append("  private static String getName(String key) {\n");
    sb.append("    String value = myMap.get(key);\n");
    sb.append("    if (key == null) {\n");
    sb.append("      return \"**Missing key in ComponentTranslationTables**\";\n");
    sb.append("    } else {\n");
    sb.append("      return value;\n");
    sb.append("    }\n");
    sb.append("  }\n\n");
    sb.append("  public static String getPropertyName(String key) {\n");
    sb.append("    String value = getName(\"PROPERTY-\" + key);\n");
    sb.append("    if(value == null) return key;\n");
    sb.append("    return value;\n");
    sb.append("  }\n\n");
    sb.append("  public static String getPropertyDescription(String key) {\n");
    sb.append("    String value = getName(\"PROPDESC-\" + key);\n");
    sb.append("    if(value == null) return key;\n");
    sb.append("    return value;\n");
    sb.append("  }\n\n");
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
    Set<String> categories = new TreeSet<>();
    Set<String> properties = new TreeSet<>();
    Set<String> methods = new TreeSet<>();
    Set<String> events = new TreeSet<>();
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();
      outputComponent(component, properties, methods, events, sb);
      categories.add(component.getCategory());
    }
    for (Map.Entry<String, OptionList> entry : optionLists.entrySet()) {
      OptionList optionList = entry.getValue();
      outputOptionList(optionList, sb);
    }
    sb.append("\n\n    /* Descriptions */\n\n");
    for (String key : tooltipProperties.keySet()) {
      sb.append("    map.put(\"PROPDESC-");
      sb.append(key.replaceAll("__", "."));
      sb.append("\", MESSAGES.");
      sb.append(key);
      sb.append("());\n");
    }
    for (String key : tooltipMethods.keySet()) {
      sb.append("    map.put(\"METHODDESC-");
      sb.append(key.replaceAll("__", "."));
      sb.append("\", MESSAGES.");
      sb.append(key);
      sb.append("());\n");
    }
    for (String key : tooltipEvents.keySet()) {
      sb.append("    map.put(\"EVENTDESC-");
      sb.append(key.replaceAll("__", "."));
      sb.append("\", MESSAGES.");
      sb.append(key);
      sb.append("());\n");
    }
    sb.append("\n\n    /* Categories */\n\n");
    for (String category : categories) {
      outputCategory(category, sb);
    }
    outputPropertyCategory("Appearance", sb);
    outputPropertyCategory("Behavior", sb);
    outputPropertyCategory("Unspecified", sb);
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
