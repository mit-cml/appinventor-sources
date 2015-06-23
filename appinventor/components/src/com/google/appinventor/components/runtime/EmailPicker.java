// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.widget.AutoCompleteTextView;

/**
 * Text box using auto-completion to pick out an email address from contacts.
 *
 * @author sharon@google.com (Sharon Perl)
 */


@DesignerComponent(version = YaVersion.EMAILPICKER_COMPONENT_VERSION,
    description = "An EmailPicker is a kind of text box.  " +
    "If the user begins entering the name " +
    "or email address of a contact, the phone will show a dropdown menu of " +
    "choices that complete the entry.  If there are many contacts, the " +
    "dropdown can take several seconds to appear, and can show intermediate results " +
    "while the matches are being computed. <p>The initial contents " +
    "of the text box and the contents< after user entry is in the <code>Text</code> " +
    "property.  If the <code>Text</code> property is initially empty, " +
    "the contents of the <code>Hint</code> property will be faintly shown " +
    "in the text box as a hint to the user.</p>\n " +
    "<p>Other properties affect the appearance of the text box " +
    "(<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and " +
    "whether it can be used (<code>Enabled</code>).</p>\n" +
    "<p>Text boxes like this are usually used with <code>Button</code> " +
    "components, with the user clicking on the button when text entry is " +
    "complete.",
    category = ComponentCategory.SOCIAL)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.READ_CONTACTS")
public class EmailPicker extends TextBoxBase {

  private final EmailAddressAdapter addressAdapter;

  /**
   * Create a new EmailPicker component.
   *
   * @param container the parent container.
   */
  public EmailPicker(ComponentContainer container) {
    super(container, new AutoCompleteTextView(container.$context()));
    addressAdapter = new EmailAddressAdapter(container.$context());
    ((AutoCompleteTextView) super.view).setAdapter(addressAdapter);
  }

  /**
   * Event raised when this component is selected for input, such as by
   * the user touching it.
   */
  @SimpleEvent
  @Override
  public void GotFocus() {

//     Note(halabelson):  I am commenting out this test.  Android provider.Constacts was
//     deprecated in Donut, but email picking still seems to work on newer versions of the SDK.
//     If there's a phone where it does not work, we'll get the error at PuntContactSelection
//     Note that there is still a general problem with contact picking on Motoblur.
//
//    if (SdkLevel.getLevel() > SdkLevel.LEVEL_DONUT) {
//      container.$form().dispatchErrorOccurredEvent(this, "EmailAddress",
//          ErrorMessages.ERROR_FUNCTIONALITY_NOT_SUPPORTED_EMAIL_PICKER);
//    }

    EventDispatcher.dispatchEvent(this, "GotFocus");
  }
}
