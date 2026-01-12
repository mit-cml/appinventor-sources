// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MarkdownDocumentationGenerator extends ComponentProcessor {

  private static final String COLOR_TYPE = "color";

  @Override
  protected void outputResults() throws IOException {

    Map<String, String> categoryNames = new HashMap<>();
    Map<String, StringBuilder> categoryDocs = new HashMap<>();
    Map<String, Map<String, ComponentInfo>> sortedComponents = new HashMap<>();

    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();
      if (!categoryDocs.containsKey(component.getCategoryString())) {
        categoryDocs.put(component.getCategoryString(), new StringBuilder());
      }
      if (!sortedComponents.containsKey(component.getCategoryString())) {
        sortedComponents.put(component.getCategoryString(), new TreeMap<String, ComponentInfo>());
      }
      categoryNames.put(component.getCategoryString(), component.getCategory());
      sortedComponents.get(component.getCategoryString()).put(component.displayName, component);
    }

    for (String category : categoryDocs.keySet()) {
      try (BufferedWriter out =
               new BufferedWriter(getOutputWriter(category.toLowerCase() + ".md"))) {
        out.write("---\nlayout: documentation\ntitle: ");
        out.write(categoryNames.get(category));
        out.write("\n---\n\n");
        out.write("[&laquo; Back to index](index.html)\n");
        out.write("# ");
        out.write(categoryNames.get(category));
        out.write("\n\nTable of Contents:\n");
        for (Map.Entry<String, ComponentInfo> entry : sortedComponents.get(category).entrySet()) {
          out.write("\n* [");
          out.write(entry.getKey());
          out.write("](");
          out.write("#");
          out.write(entry.getKey());
          out.write(")");
        }
        for (Map.Entry<String, ComponentInfo> entry : sortedComponents.get(category).entrySet()) {
          String name = entry.getKey();
          out.write("\n\n## ");
          out.write(name);
          out.write("  {#" + name + "}\n\n");
          ComponentInfo info = entry.getValue();
          out.write(info.getLongDescription(info));
          out.write("\n\n");
          outputProperties(name, info, out);
          outputEvents(name, info, out);
          outputMethods(name, info, out);
        }
        out.write("\n");
      }
    }
  }

  private void outputProperties(String name, ComponentInfo info, Writer out) throws IOException {
    out.write(String.format("%n%n### Properties  {#%s-Properties}%n%n{:.properties}", name));
    boolean didOutput = false;
    for (Property property : info.properties.values()) {
      if (isPropertyHidden(info, property)) {
        continue;
      }
      out.write(String.format("%n%n{:id=\"%s.%s\" %s} *%s*%n: ", name, property.name,
          getPropertyClass(info, property), property.name));
      out.write(property.getLongDescription(info));
      didOutput = true;
    }
    if (!didOutput) {
      out.write("\nNone\n");
    }
  }

  private boolean isPropertyHidden(ComponentInfo info, Property property) {
    return (isFeatureHidden(info, property) && !info.designerProperties.containsKey(property.name))
        || (info.displayName.equals("Screen") && property.name.equals("ActionBar"))
        || (info.displayName.equals("FirebaseDB") && property.name.equals("DefaultURL"))
        || (info.displayName.equals("CloudDB") && property.name.equals("DefaultRedisServer"));
  }

  private boolean isFeatureHidden(ComponentInfo info, Feature feature) {
    return !feature.isUserVisible() || feature.isDeprecated();
  }

  private String getPropertyClass(ComponentInfo info, Property property) {
    String cls = property.isColor() ? COLOR_TYPE : property.getYailType();
    if (!property.isWritable()) {
      cls += " .ro";
    }
    if (!property.isReadable()) {
      cls += " .wo";
    }
    if (!info.designerProperties.containsKey(property.name)) {
      cls += " .bo";
    }
    if (!property.isUserVisible() && info.designerProperties.containsKey(property.name)) {
      cls += " .do";
    }
    return "." + cls;
  }

  private void outputEvents(String name, ComponentInfo info, Writer out) throws IOException {
    out.write(String.format("%n%n### Events  {#%s-Events}%n%n{:.events}", name));
    boolean didOutput = false;
    for (Event event : info.events.values()) {
      if (isFeatureHidden(info, event)) {
        continue;
      }
      out.write(String.format("%n%n{:id=\"%s.%s\"} %s(%s)%n: ", name, event.name, event.name,
          formatParameters(event.parameters)));
      out.write(event.getLongDescription(info));
      didOutput = true;
    }
    if (!didOutput) {
      out.write("\nNone\n");
    }
  }

  private void outputMethods(String name, ComponentInfo info, Writer out) throws IOException {
    out.write(String.format("%n%n### Methods  {#%s-Methods}%n%n{:.methods}", name));
    boolean didOutput = false;
    for (Method method : info.methods.values()) {
      if (isFeatureHidden(info, method)) {
        continue;
      }
      String returnType = "";
      if (method.getReturnType() != null) {
        returnType = method.isColor() ? COLOR_TYPE : method.getYailReturnType();
        returnType = " returns " + returnType;
      }
      out.write(String.format("%n%n{:id=\"%s.%s\" class=\"method%s\"} <i/> %s(%s)%n: ", name,
          method.name, returnType, method.name, formatParameters(method.parameters)));
      out.write(method.getLongDescription(info));
      didOutput = true;
    }
    if (!didOutput) {
      out.write("\nNone\n");
    }
  }

  private String formatParameters(Collection<Parameter> parameters) {
    StringBuilder sb = new StringBuilder();
    String separator = "";
    for (Parameter param : parameters) {
      sb.append(separator);
      sb.append(String.format("*%s*{:.%s}", param.name,
          param.color ? COLOR_TYPE : param.getYailType()));
      separator = ",";
    }
    return sb.toString();
  }
}
