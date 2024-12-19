// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.repackaged.org.json.XML;

public class XmlUtil {
  /**
   * Converts an XML string to a JSON string.
   *
   * Using this function requires that a component include <code>@UsesLibraries({"json.jar"})</code>
   * to get the XML conversion code.
   *
   * @param xmlText the source XML
   * @return JSON representation of the XML
   */
  public static String xmlToJson(String xmlText) {
    return XML.toJSONObject(xmlText).toString();
  }
}
