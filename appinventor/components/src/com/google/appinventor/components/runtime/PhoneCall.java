// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.PhoneCallUtil;

import android.content.Context;

/**
 * Component for making a phone call to a programatically-specified number.
 *
 * TODO(markf): Note that the initial carrier for Android phones only supports 3 participants
 *              in a conference call, so that's all that the current implementation of this
 *              component supports.  In the future we can generalize this to more participants.
 *
 * @author markf@google.com (Mark Friedman)
 */
@DesignerComponent(version = YaVersion.PHONECALL_COMPONENT_VERSION,
    description = "<p>A non-visible component that makes a phone call to " +
    "the number specified in the <code>PhoneNumber</code> property, which " +
    "can be set either in the Designer or Blocks Editor. The component " +
    "has a <code>MakePhoneCall</code> method, enabling the program to launch " +
    "a phone call.</p>" +
    "<p>Often, this component is used with the <code>ContactPicker</code> " +
    "component, which lets the user select a contact from the ones stored " +
    "on the phone and sets the <code>PhoneNumber</code> property to the " +
    "contact's phone number.</p>" +
    "<p>To directly specify the phone number (e.g., 650-555-1212), set " +
    "the <code>PhoneNumber</code> property to a Text with the specified " +
    "digits (e.g., \"6505551212\").  Dashes, dots, and parentheses may be " +
    "included (e.g., \"(650)-555-1212\") but will be ignored; spaces may " +
    "not be included.</p>",
    category = ComponentCategory.SOCIAL,
    nonVisible = true,
    iconName = "images/phoneCall.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.CALL_PHONE")
public class PhoneCall extends AndroidNonvisibleComponent implements Component {

  private String phoneNumber;
  private final Context context;

  /**
   * Creates a Phone Call component.
   *
   * @param container container, component will be placed in
   */
  public PhoneCall(ComponentContainer container) {
    super(container.$form());
    context = container.$context();
    PhoneNumber("");
  }

  /**
   * PhoneNumber property getter method.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String PhoneNumber() {
    return phoneNumber;
  }

  /**
   * PhoneNumber property setter method: sets a phone number to call.
   *
   * @param phoneNumber a phone number to call
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void PhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  /**
   * Makes a phone call using the number in the PhoneNumber property.
   */
  @SimpleFunction
  public void MakePhoneCall() {
    PhoneCallUtil.makePhoneCall(context, phoneNumber);
  }
}
