// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.widgets.properties.TextPropertyEditor;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;

/**
 * Property editor for the Android package name of an app. An empty value means that the
 * package name generated from the project is used; it is shown as the placeholder.
 */
public class YoungAndroidPackageNamePropertyEditor extends TextPropertyEditor {

  public YoungAndroidPackageNamePropertyEditor(DesignerEditor editor) {
    if (editor != null) {
      String qualifiedFormName = YoungAndroidSourceNode.getQualifiedName(editor.getFileId());
      textEdit.getElement().setAttribute("placeholder",
          StringUtils.getPackageName(qualifiedFormName));
    }
  }

  @Override
  protected void validate(String text) throws InvalidTextException {
    if (!text.isEmpty() && !StringUtils.isValidPackageName(text)) {
      throw new InvalidTextException(MESSAGES.invalidPackageNameError(text));
    }
  }
}
