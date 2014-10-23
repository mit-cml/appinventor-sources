// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.scripts;

import com.google.appinventor.components.common.HtmlEntities;
import com.google.appinventor.components.common.YaVersion;
import com.google.common.base.Charsets;  // for Charsets.US_ASCII
import com.google.common.io.Resources;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.FileObject;

/**
 * Processes component source files to generate an XML file to specify a
 * language to Codeblocks.
 *
 * @author spertus@google.com (Ellen Spertus)
 */
public final class LangDefXmlGenerator extends ComponentProcessor {
  // Where to write results.
  private static final String OUTPUT_FILE_NAME = "ya_lang_def.xml";

  // Where to find templates for XML output
  private static  final String TEMPLATE_PATH = "templates/";

  // Templates read from disk
  private static final String OUTPUT_HEADER;
  private static final String COMPONENT_HEADER_START;
  private static final String COMPONENT_HEADER_PER_EVENT;
  private static final String COMPONENT_HEADER_PER_PROPERTY;
  private static final String COMPONENT_HEADER_PER_METHOD;
  private static final String COMPONENT_HEADER_END;
  private static final String EVENTS_HEADER;
  private static final String EVENT_HEADER_START;
  private static final String EVENT_HEADER_PER_PARAMETER;
  private static final String EVENT_PER_PARAMETER;
  private static final String EVENT_HEADER_END;
  private static final String EVENT_FOOTER;
  private static final String METHODS_HEADER;
  private static final String METHOD_FOOTER;
  private static final String METHOD_HEADER_END;
  private static final String METHOD_HEADER_PER_PARAMETER;
  private static final String METHOD_HEADER_RETURN_TYPE;
  private static final String METHOD_HEADER_START;
  private static final String METHOD_PER_PARAMETER1;
  private static final String METHOD_PER_PARAMETER2;
  private static final String METHOD_RETURN_TYPE;
  private static final String OUTPUT_FOOTER;

  static {
    try {
      // Load templates that will be used in outputResults().
      OUTPUT_HEADER = fileToString("OUTPUT_HEADER.txt");
      COMPONENT_HEADER_START = fileToString("COMPONENT_HEADER_START.txt");
      COMPONENT_HEADER_PER_EVENT = fileToString("COMPONENT_HEADER_PER_EVENT.txt");
      COMPONENT_HEADER_PER_PROPERTY = fileToString("COMPONENT_HEADER_PER_PROPERTY.txt");
      COMPONENT_HEADER_PER_METHOD = fileToString("COMPONENT_HEADER_PER_METHOD.txt");
      COMPONENT_HEADER_END = fileToString("COMPONENT_HEADER_END.txt");
      EVENTS_HEADER = fileToString("EVENTS_HEADER.txt");
      EVENT_HEADER_START = fileToString("EVENT_HEADER_START.txt");
      EVENT_HEADER_PER_PARAMETER = fileToString("EVENT_HEADER_PER_PARAMETER.txt");
      EVENT_PER_PARAMETER = fileToString("EVENT_PER_PARAMETER.txt");
      EVENT_HEADER_END = fileToString("EVENT_HEADER_END.txt");
      EVENT_FOOTER = fileToString("EVENT_FOOTER.txt");
      METHODS_HEADER = fileToString("METHODS_HEADER.txt");
      METHOD_FOOTER = fileToString("METHOD_FOOTER.txt");
      METHOD_HEADER_END = fileToString("METHOD_HEADER_END.txt");
      METHOD_HEADER_PER_PARAMETER = fileToString("METHOD_HEADER_PER_PARAMETER.txt");
      METHOD_HEADER_RETURN_TYPE = fileToString("METHOD_HEADER_RETURN_TYPE.txt");
      METHOD_HEADER_START = fileToString("METHOD_HEADER_START.txt");
      METHOD_PER_PARAMETER1 = fileToString("METHOD_PER_PARAMETER1.txt");
      METHOD_PER_PARAMETER2 = fileToString("METHOD_PER_PARAMETER2.txt");
      METHOD_RETURN_TYPE = fileToString("METHOD_RETURN_TYPE.txt");
      OUTPUT_FOOTER = fileToString("OUTPUT_FOOTER.txt");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // Reads a (template) file into a string.
  private static String fileToString(String filename) throws IOException {
    return Resources.toString(LangDefXmlGenerator.class.getResource(TEMPLATE_PATH + filename),
        Charsets.US_ASCII);
  }

  @Override
  protected void outputResults() throws IOException {
    // Begin writing output file.
    FileObject src = createOutputFileObject(OUTPUT_FILE_NAME);
    Writer writer = src.openWriter();
    writer.write(String.format(OUTPUT_HEADER, YaVersion.YOUNG_ANDROID_VERSION,
        YaVersion.BLOCKS_LANGUAGE_VERSION));

    // Components are already sorted.
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();
      outputComponent(writer, component);
    }

    writer.write(OUTPUT_FOOTER);

    // Close output file.
    writer.flush();
    writer.close();
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());
  }

  private void outputComponent(Writer writer, ComponentInfo component) throws IOException {
    // Output header of component
    writer.write(String.format(COMPONENT_HEADER_START,
                               component.displayName,
                               formatDescription(component.getHelpDescription()),
                               component.getVersion()));

    // Events are already sorted.
    int eventCount = 0;
    for (Map.Entry<String, Event> entry : component.events.entrySet()) {
      Event event = entry.getValue();
      // Only output user-visible events.
      if (event.userVisible) {
        writer.write(String.format(COMPONENT_HEADER_PER_EVENT,
                                   component.displayName, event.name, ++eventCount));
      }
    }

    // Properties are already sorted.
    int propertyCount = 0;
    for (Map.Entry<String, Property> entry : component.properties.entrySet()) {
      Property property = entry.getValue();
      // Output properties that are not user-visible, but mark them as invisible instead of using
      // their RW string.
      writer.write(String.format(COMPONENT_HEADER_PER_PROPERTY,
                                 property.name,
                                 property.isUserVisible() ? property.getRwString() : "invisible",
                                 javaTypeToYailType(property.getType()),
                                 ++propertyCount,
                                 formatDescription(property.getDescription())));
    }

    // Methods are already sorted.
    int methodCount = 0;
    for (Map.Entry<String, Method> entry : component.methods.entrySet()) {
      Method method = entry.getValue();
      // Only output user-visible methods.
      if (method.userVisible) {
        writer.write(String.format(COMPONENT_HEADER_PER_METHOD,
                                   component.displayName,
                                   method.name,
                                   ++methodCount));
      }
    }

    // Output end of component header.
    writer.write(COMPONENT_HEADER_END);

    // Output detailed information about each event.
    if (!component.events.isEmpty()) {
      writer.write(EVENTS_HEADER);
      for (Map.Entry<String, Event> entry : component.events.entrySet()) {
        Event event = entry.getValue();
        // Note that we write out the block genus definitions for events
        // with userVisible==false just in case there are old projects
        // that contain those blocks that need to be corrected by the user.
        writer.write(String.format(EVENT_HEADER_START,
                                   component.displayName,
                                   event.name,
                                   formatDescription(event.description)));
        for (Parameter parameter : event.parameters) {
          writer.write(String.format(EVENT_HEADER_PER_PARAMETER, parameter.name));
        }
        writer.write(EVENT_HEADER_END);
        int parameterCount = 0;
        for (Parameter parameter : event.parameters) {
          writer.write(String.format(EVENT_PER_PARAMETER,
                                     parameter.name, ++parameterCount));
        }
        writer.write(EVENT_FOOTER);
      }
    }

    // Output detailed information about each method.
    if (!component.methods.isEmpty()) {
      writer.write(METHODS_HEADER);
      for (Map.Entry<String, Method> entry : component.methods.entrySet()) {
        Method method = entry.getValue();
        // write two versions of the method.  One for specific component instances and once for the
        // component type (which includes an argument socket for the component object).
        writeMethod(writer, component, method, false);
        writeMethod(writer, component, method, true);

      }
    }
  }

  private void writeMethod(Writer writer, ComponentInfo component,
                           Method method, boolean forComponentObj) throws IOException {
    // Note that we write out the block genus definitions for methods
    // with userVisible==false just in case there are old projects
    // that contain those blocks that need to be corrected by the user.
    // Write header for method.
    String methodComponentId =
        forComponentObj ? "Type-" + component.displayName : component.displayName;
    writer.write(String.format(METHOD_HEADER_START,
                               methodComponentId,
                               method.name,
                               (method.getReturnType() == null) ? "command" : "function",
                               formatDescription(method.description)));

    if (forComponentObj) {
      // Add parameter for the component object.
      writer.write(String.format(METHOD_HEADER_PER_PARAMETER, "component"));
    }
    for (Parameter parameter : method.parameters) {
      writer.write(String.format(METHOD_HEADER_PER_PARAMETER, parameter.name));
    }
    if (method.getReturnType() != null) {
      writer.write(METHOD_HEADER_RETURN_TYPE);
    }
    writer.write(String.format(METHOD_HEADER_END,
                               forComponentObj ? "componentTypeMethod" : "componentMethod"));

    if (forComponentObj) {
      // Add a property indicating that this method is for component objects.
      writer.write("    <LangSpecProperty key=\"is-from-component-type\" value=\"true\"/>\n");
    }

    // Provide properties of return type and parameters.
    if (method.getReturnType() != null) {
      final String returnType = javaTypeToYailType(method.getReturnType());
      if ("any".equals(returnType)) {
        // If we just have a socket-exclude rule for the 'argument' type then any other type
        // can be returned.
        writer.write("    <LangSpecProperty key=\"type-exclude-1\" value=\"argument\"/>\n");
      } else {
        writer.write(String.format(METHOD_RETURN_TYPE, returnType));
      }
    }
    int allowCount = 0;
    int excludeCount = 0;
    if (forComponentObj) {
      // Allow 'value' type
      writer.write(String.format(METHOD_PER_PARAMETER1, "component", ++allowCount));
      // Allow 'component' type
      writer.write(String.format(METHOD_PER_PARAMETER2,
                                 "component",
                                 "component",
                                 ++allowCount));
    }
    for (Parameter parameter : method.parameters) {
      if (!"any".equals(javaTypeToYailType(parameter.type))) {
        // Indicates that "value" is acceptable.
        // Ben told me this will probably go away.
        writer.write(String.format(METHOD_PER_PARAMETER1, parameter.name, ++allowCount));
      }
    }
    for (Parameter parameter : method.parameters) {
      final String paramType = javaTypeToYailType(parameter.type);
      if (paramType.equals("any")) {
        // If we just have a socket-exclude rule for the 'argument' type then
        // any other type will be accepted
        writer.write(String.format(
            "    <LangSpecProperty key=\"socket-exclude-%2$d\" value=\"%1$s/argument\"/>\n",
            parameter.name,
            ++excludeCount));
      } else {
        // Provides the more specific type
        writer.write(String.format(METHOD_PER_PARAMETER2,
                                   parameter.name,
                                   paramType,
                                   ++allowCount));
      }
    }
    writer.write(METHOD_FOOTER);
  }

  /*
   * TODO(user): note that stripping the HTML out of the description text
   * leaves some of the description pretty unreadable. It would be nice
   * to be able to keep some formatting in the descriptions.
   */
  private static String formatDescription(String description) {
    // Replace <li> with " - "
    description = description.replace("<li>", " - ");

    // Replace <p> with " "
    description = description.replace("<p>", " ");

    // Replace <br> with " "
    description = description.replace("<br>", " ");

    // Remove other HTML tags.
    description = description.replaceAll("<[^>]+>", "");

    // Replace HTML entities
    description = HtmlEntities.decodeHtmlText(description);

    // Replace blanks lines with spaces.
    description = description.replaceAll("[\n\r]", " ");

    // Replace multiple spaces with a single space.
    description = description.replaceAll(" {2,}", " ");

    // Remove leading and trailing spaces.
    description = description.trim();

    // Escape special characters for XML.
    description = description.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("'", "&apos;")
        .replace("\"", "&quot;");

    return description;
  }
}
