// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import javax.tools.Diagnostic;

public class SwiftOptionHelperGenerator extends ComponentProcessor {

  /**
   * Outputs the Swift code for the OptionListHelper class.
   */
  @Override
  protected void outputResults() {
    try (Writer writer = getOutputWriter("OptionHelper.swift")) {
      PrintWriter pw = new PrintWriter(writer);
      pw.println("// -*- mode: java; c-basic-offset: 2; -*-");
      pw.println("// Copyright © 2025 MIT, All rights reserved");
      pw.println("// Released under the Apache License, Version 2.0");
      pw.println("// https://www.apache.org/licenses/LICENSE-2.0");
      pw.println("// This file is auto-generated. Do not edit!");
      pw.println();
      pw.println("import Foundation");
      pw.println();
      pw.println("@objc public class OptionHelper: NSObject {");
      pw.println("  private static let STRING_LOOKUP_TABLE: [String: [String: (String) -> AnyObject?]] = [");
      outputLookupTable(pw, "java.lang.String");
      pw.println("  ]");
      pw.println();
      pw.println("  private static let INTEGER_LOOKUP_TABLE: [String: [String: (Int32) -> AnyObject?]] = [");
      outputLookupTable(pw, "java.lang.Integer");
      pw.println("  ]");
      pw.println();
      pw.println("  @objc public static func optionListFromValue(_ component: Component, _ functionName: String, _ value: AnyObject) -> AnyObject? {");
      pw.println("    if let v = value as? Int32 {");
      pw.println("      guard let lookupTable = INTEGER_LOOKUP_TABLE[String(describing: type(of: component))] else {");
      pw.println("        return value");
      pw.println("      }");
      pw.println("      guard let lookupFunction = lookupTable[functionName] else {");
      pw.println("        return value");
      pw.println("      }");
      pw.println("      return lookupFunction(v)");
      pw.println("    } else if let v = value as? String {");
      pw.println("      guard let lookupTable = STRING_LOOKUP_TABLE[String(describing: type(of: component))] else {");
      pw.println("        return value");
      pw.println("      }");
      pw.println("      guard let lookupFunction = lookupTable[functionName] else {");
      pw.println("        return value");
      pw.println("      }");
      pw.println("      return lookupFunction(v)");
      pw.println("    } else {");
      pw.println("      return value");
      pw.println("    }");
      pw.println("  }");
      pw.println("}");
      pw.flush();
    } catch (IOException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
    }
  }

  /**
   * Outputs the lookup table for the given type. Note that we have to separate the tables by type
   * because Swift does not support function reflection based on parameter type.
   *
   * @param pw the PrintWriter to output to
   * @param typeName the underlying type name to output the table for
   */
  private void outputLookupTable(PrintWriter pw, String typeName) {
    boolean firstComponent = true;
    for (ComponentInfo component : components.values()) {
      if (!componentHasOptionProperties(component, typeName)) {
        continue;
      }
      if (firstComponent) {
        firstComponent = false;
      } else {
        pw.println(",");
      }
      pw.print("    \"");
      pw.print(component.name);
      pw.println("\": [");
      boolean first = true;
      for (Property prop : component.properties.values()) {
        if (!isOptionList(prop.getHelperKey())) {
          continue;
        }
        if (!hasUnderlyingType(prop.getHelperKey(), typeName)) {
          continue;
        }
        if (first) {
          first = false;
        } else {
          pw.println(",");
        }
        outputEntry(pw, prop.name, prop.getHelperKey());
      }
      for (Method method : component.methods.values()) {
        if (!isOptionList(method.getReturnHelperKey())) {
          continue;
        }
        if (!hasUnderlyingType(method.getReturnHelperKey(), typeName)) {
          continue;
        }
        if (first) {
          first = false;
        } else {
          pw.println(",");
        }
        outputEntry(pw, method.name, method.getReturnHelperKey());
      }
      pw.print("\n    ]");
    }
    pw.println();
  }

  /**
   * Outputs a single entry in the lookup table.
   *
   * @param pw the PrintWriter to output to
   * @param functionName the function name
   * @param helperKey the HelperKey for the OptionList
   */
  private void outputEntry(PrintWriter pw, String functionName, HelperKey helperKey) {
    pw.print("      \"");
    pw.print(functionName);
    pw.print("\": ");
    OptionList items = optionLists.get((String) helperKey.getKey());
    pw.print(items.getTagName());
    pw.print(".fromUnderlyingValue(_:)");
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean hasUnderlyingType(HelperKey key, String typeName) {
    if (key == null) {
      return false;
    }
    HelperType helperType = key.getType();
    if (helperType == HelperType.OPTION_LIST) {
      OptionList optionList = optionLists.get((String) key.getKey());
      if (optionList != null) {
        return optionList.getUnderlyingType().toString().equals(typeName);
      }
    }
    return false;
  }

  private static boolean isOptionList(HelperKey key) {
    return key != null && key.getType() == HelperType.OPTION_LIST;
  }

  private boolean componentHasOptionProperties(ComponentInfo component, String typeName) {
    for (Property prop : component.properties.values()) {
      if (isOptionList(prop.getHelperKey()) && hasUnderlyingType(prop.getHelperKey(), typeName)) {
        return true;
      }
    }
    for (Method method : component.methods.values()) {
      if (isOptionList(method.getReturnHelperKey()) && hasUnderlyingType(method.getReturnHelperKey(), typeName)) {
        return true;
      }
    }
    return false;
  }
}
