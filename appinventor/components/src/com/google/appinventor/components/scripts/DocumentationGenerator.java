// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import com.google.appinventor.components.common.ComponentCategory;

import javax.tools.Diagnostic;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * <p>Generates user-level HTML documentation for Young Android components into a
 * single html file.</p>
 *
 * <p>Historical note: This was once an abstract superclass of multiple documentation
 * generators (one single-page, one multi-page).  If there is a need to make it so
 * again, change the private output methods back to protected, and override them as
 * desired in concrete subclasses.</p>
 *
 * @author spertus@google.com (Ellen Spertus)
 */
public class DocumentationGenerator extends ComponentProcessor {
  private static final String OUTPUT_FILE_NAME = "component-doc.html";

  /**
   * Returns string introducing a component.
   */
  private String getComponentOutputString(String componentName, String componentDescription) {
    return String.format("\n<h2 id=\"%1$s\">%1$s</h2>\n\n" +
        "<p>%2$s</p>\n\n",
        componentName, componentDescription);
  }

  /**
   * Returns string describing a given property.
   */
  private String getPropertyDefinition(String name, String description,
      boolean isUserVisible, boolean isReadable, boolean isWritable) {

    if (!isUserVisible) {
      return String.format("  <dt><code>%s</code> (designer only)</dt>\n  <dd>%s</dd>\n",
          name, description);
    }
    else if (isReadable && !isWritable) {
      return String.format("  <dt><code><em>%s</em></code></dt>\n  <dd>%s</dd>\n",
          name, description);
    }
    return String.format("  <dt><code>%s</code></dt>\n  <dd>%s</dd>\n",
        name, description);
  }

  /**
   * Returns string to summarize a specific event for a component.
   */
  private String getEventDefinition(String name, String parameters, String description) {
    return String.format("  <dt><code>%s(%s)</code></dt>\n  <dd>%s</dd>\n",
                         name, parameters, description);
  }

  /**
   * Returns string to summarize a specific method for a component.
   * If the method is void, <code>returnType</code> will be the empty string.
   */
  private String getMethodDefinition(String name, String parameters, String returnType,
      String description) {
    return String.format("  <dt><code>%s%s%s(%s)</code></dt>\n  <dd>%s</dd>\n",
        returnType, returnType.isEmpty() ? "" : " ",
        name, parameters, description);
  }

  protected final void outputResults() throws IOException {
    // Begin writing output file.
    FileObject src = createOutputFileObject(OUTPUT_FILE_NAME);
    Writer writer = src.openWriter();

    // Output table at top showing components by category.
    outputCategories(writer);

    // Components are already sorted.
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();
      outputComponent(writer, component);
    }

    // Close output file
    writer.flush();
    writer.close();
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());
  }

  /**
   * Outputs all information for the given component, including its
   * name, description, properties, events, and methods.
   *
   * @param writer the destination for the page
   * @param component the component to document
   */
  private void outputComponent(Writer writer, ComponentInfo component) throws IOException {
    // Output component name and description.
    writer.write(getComponentOutputString(component.name,
                                          component.description));

    // Output properties, events, and methods.
    outputProperties(writer, component);
    outputEvents(writer, component);
    outputMethods(writer, component);
  }

  // Output table showing categories (e.g., "Sensors") and components in them.
  // This hardcodes a two-column table as output with certain categories in the
  // left column and certain other categories in the right column.
  private void outputCategories(Writer writer)
      throws java.io.IOException {
    // Output table header
    writer.write("<table style=\"border-color: rgb(136, 136, 136); border-width: 0px; " +
                 "border-collapse: collapse;\" border=\"0\" bordercolor=\"#888888\" " +
                 "cellpadding=\"5\" cellspacing=\"5\">\n");
    writer.write("<tbody valign=\"top\">\n");
    writer.write("<tr>\n");

    // Specify which categories are in which output column.
    final ComponentCategory[][] categories = {
      // Column one categories
      {
        ComponentCategory.USERINTERFACE,
        ComponentCategory.LAYOUT,
        ComponentCategory.MEDIA,
        ComponentCategory.ANIMATION,
        ComponentCategory.SOCIAL
      },
      // Column two categories
      {
        ComponentCategory.STORAGE,
        ComponentCategory.CONNECTIVITY,
        ComponentCategory.SENSORS,
        ComponentCategory.LEGOMINDSTORMS,
        //ComponentCategory.EXPERIMENTAL
      }
    };

    // Output the body of the table.
    for (int column = 0; column < java.lang.reflect.Array.getLength(categories); column++) {
      writer.write("<td>");
      for (int row = 0; row < java.lang.reflect.Array.getLength(categories[column]); row++) {
        // Output the category header.
        String categoryName = categories[column][row].getName();
        writer.write(String.format("<b><font size=\"5\">%s</font></b>\n<ul>\n",
                                   categoryName.replace("\u00AE", "&copy;")));
        // Output the components with this category.  This algorithm for getting
        // components by category has poor complexity performance but is probably
        // more efficient in practice than maintaining a hash table mapping
        // categories to components.
        for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
          ComponentInfo component = entry.getValue();
          if (categoryName.equals(component.getCategory())) {
            writer.write(String.format("  <li><a href=\"#%1$s\">%1$s</a></li>\n",
                                       component.name));
          }
        }
        writer.write("</ul>\n");
      }
      writer.write("</td>\n");
      // For aesthetic purposes, output empty column between category columns.
      writer.write("<td style=\"width: 60px;\"><b><font size=\"5\"><br/></font></b></td>");
    }

    // Write table footer
    writer.write("</tr>\n</tbody>\n</table>\n");
  }

  private void outputProperties(Writer writer, ComponentInfo component)
      throws java.io.IOException {
    writer.write("<h3>Properties</h3>\n");
    // Only list properties that are user-visible or designer properties.
    boolean definitionWritten = false;
    for (Map.Entry<String, Property> entry : component.properties.entrySet()) {
      Property property = entry.getValue();
      if (property.isUserVisible() || component.designerProperties.containsKey(property.name)) {
        if (!definitionWritten) {
          writer.write("<dl>\n");
        }
        writer.write(getPropertyDefinition(property.name,
                                           property.getDescription(),
                                           property.isUserVisible(),
                                           property.isReadable(),
                                           property.isWritable()));
        definitionWritten = true;
      }
    }
    if (definitionWritten) {
      writer.write("</dl>\n\n");
    } else {
      writer.write("none\n\n");
    }
  }

  private void outputEvents(Writer writer, ComponentInfo component) throws java.io.IOException {
    writer.write("<h3>Events</h3>\n");
    // Only list events that are user-visible.
    boolean definitionWritten = false;
    for (Map.Entry<String, Event> entry : component.events.entrySet()) {
      Event event = entry.getValue();
      if (event.userVisible) {
        if (!definitionWritten) {
          writer.write("<dl>\n");
        }
        writer.write(getEventDefinition(event.name, event.toParameterString(), event.description));
        definitionWritten = true;
      }
    }
    if (definitionWritten) {
      writer.write("</dl>\n\n");
    } else {
      writer.write("none\n\n");
    }
  }

  private void outputMethods(Writer writer, ComponentInfo component) throws java.io.IOException {
    writer.write("<h3>Methods</h3>\n");
    // Only list methods that are user-visible.
    boolean definitionWritten = false;
    for (Map.Entry<String, Method> entry : component.methods.entrySet()) {
      Method method = entry.getValue();
      if (method.userVisible) {
        if (!definitionWritten) {
          writer.write("<dl>\n");
        }
        String returnType = method.getReturnType();
        writer.write(getMethodDefinition(
            method.name, method.toParameterString(),
            returnType == null ? "" : javaTypeToYailType(returnType),
            method.description));
        definitionWritten = true;
      }
    }
    if (definitionWritten) {
      writer.write("</dl>\n\n");
    } else {
      writer.write("none\n\n");
    }
  }
}
