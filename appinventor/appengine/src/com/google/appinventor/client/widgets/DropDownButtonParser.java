// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.uibinder.elementparsers.ElementParser;
import com.google.gwt.uibinder.rebind.FieldWriter;
import com.google.gwt.uibinder.rebind.UiBinderWriter;
import com.google.gwt.uibinder.rebind.XMLElement;

@SuppressWarnings("unused")
public class DropDownButtonParser implements ElementParser {
  @Override
  public void parse(XMLElement elem, final String fieldName, JClassType type,
      final UiBinderWriter writer) throws UnableToCompleteException {
    elem.consumeChildElements(new XMLElement.Interpreter<Boolean>() {
      @Override
      public Boolean interpretElement(XMLElement child)
          throws UnableToCompleteException {
        if (child.getLocalName().equals("hr")) {
          writer.addStatement("%1$s.addItem(null);", fieldName);
        } else {
          FieldWriter childField = writer.parseElementToField(child);
          writer.addStatement("%1$s.addItem(%2$s);", fieldName,
              childField.getNextReference());
        }
        return true;
      }
    });
  }
}
