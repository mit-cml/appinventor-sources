// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
    description = "<p>A text box in which a user can begin entering an email " +
    "address of a contact and be offered auto-completion.  The initial value " +
    "of the box and the value after user entry is in the <code>Text</code> " +
    "property.  If the <code>Text</code> property is initially empty, " +
    "the contents of the <code>Hint</code> property will be faintly shown " +
    "in the text box as a hint to the user.</p> " +
    "<p>Other properties affect the appearance of the text box " +
    "(<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and " +
    "whether it can be used (<code>Enabled</code>).</p>" +
    "<p>Text boxes are usually used with the <code>Button</code> " +
    "component, with the user clicking on the button when text entry is " +
    "complete.</p>",
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
    if (SdkLevel.getLevel() > SdkLevel.LEVEL_DONUT) {
      container.$form().dispatchErrorOccurredEvent(this, "GotFocus",
          ErrorMessages.ERROR_FUNCTIONALITY_NOT_SUPPORTED_EMAIL_PICKER);
    }
    EventDispatcher.dispatchEvent(this, "GotFocus");
  }
}
