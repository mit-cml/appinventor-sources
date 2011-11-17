// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.util.Log;

/**
 * Component enabling a user to select a contact's phone number.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author markf@google.com (Mark Friedman)
 */
@DesignerComponent(version = YaVersion.PHONENUMBERPICKER_COMPONENT_VERSION,
    description = "<p>A button that, when clicked on, displays a list of " +
    "the contacts' phone numbers to choose among. After the user has made a " +
    "selection, the following properties will be set to information about " +
    "the chosen contact: <ul>" +
    "<li> <code>ContactName</code>: the contact's name </li> "  +
    "<li> <code>PhoneNumber</code>: the contact's phone number </li> " +
    "<li> <code>EmailAddress</code>: the contact's email address </li> " +
    "<li> <code>Picture</code>: the name of the file containing the contact's " +
    "image, which can be used as a <code>Picture</code> property value for " +
    "the <code>Image</code> or <code>ImageSprite</code> component.</li></ul>" +
    "</p><p>Other properties affect the appearance of the button " +
    "(<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and " +
    "whether it can be clicked on (<code>Enabled</code>).</p>" +
    "<p>Picking is not supported on all phones.  If it fails, this component will " +
    "show a notification.  This default error behavior can be overridden with the " +
    "Screen.ErrorOccurred event handler.</p>",
    category = ComponentCategory.SOCIAL)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.READ_CONTACTS")
public class PhoneNumberPicker extends ContactPicker {

  private static final String[] PROJECTION = {
    Contacts.PeopleColumns.NAME,
    Contacts.PhonesColumns.NUMBER,
    Contacts.Phones.PERSON_ID,
    Contacts.People.PRIMARY_EMAIL_ID,
  };
  private static final int NAME_INDEX = 0;
  private static final int NUMBER_INDEX = 1;
  private static final int PERSON_INDEX = 2;
  private static final int EMAIL_INDEX = 3;

  private String phoneNumber;

  /**
   * Create a new ContactPicker component.
   *
   * @param container the parent container.
   */
  public PhoneNumberPicker(ComponentContainer container) {
    super(container, Contacts.Phones.CONTENT_URI);
  }

  /**
   * PhoneNumber property getter method.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String PhoneNumber() {
    return ensureNotNull(phoneNumber);
  }

  /**
   * Callback method to get the result returned by the contact picker activity
   *
   * @param requestCode a code identifying the request.
   * @param resultCode a code specifying success or failure of the activity
   * @param data the returned data, in this case an Intent whose data field
   *        contains the contact's content provider Uri.
   */
  // TODO(halabelson): Rework how the content selection is done to make this overlap
  // more with Contact Picker.  Note that the two components use different intents, so that
  // the returned URIs are different (contacts/people vs, contacts/phones)
  // This really should be fixed by updating the way we handle contacts.   See the comments
  // on checkUri in ContactPicker.

  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
      Log.i("PhoneNumberPicker", "received intent is " + data);
      Uri phoneUri = data.getData();
      if (checkContactUri(phoneUri, "//contacts/phones")) {
        // This test is not good enough.  The lookup code below does not work with
        // Motorola Blur (Droid Global), even though the URI has the correct form.
        // Hopefully, moving to the new contact scheme will solve this problem.
        Cursor cursor = null;
        try {
          cursor = activityContext.getContentResolver().query(phoneUri,
              PROJECTION, null, null, null);
          if (cursor.moveToFirst()) {
            contactName = guardCursorGetString(cursor, NAME_INDEX);
            phoneNumber = guardCursorGetString(cursor, NUMBER_INDEX);
            int contactId = cursor.getInt(PERSON_INDEX);
            Uri cUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, contactId);
            contactPictureUri = cUri.toString();
            String emailId = guardCursorGetString(cursor, EMAIL_INDEX);
            emailAddress = getEmailAddress(emailId);
            Log.i("PhoneNumberPicker",
                "Contact name = " + contactName + ", phone number = " + phoneNumber +
                ", emailAddress = " + emailAddress + ", contactPhotoUri = " +  contactPictureUri);
          }
        } catch (Exception e) {
          // There was an exception in trying to compute the cursor from the activity context.
          // It's bad form to catch an arbitrary exception, but if there is an error here
          // it's unclear what's going on.
          puntContactSelection(ErrorMessages.ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER);
        } finally {
          if (cursor != null) {
            cursor.close();
          }
        }
      } // ends if (checkContactUri ...
      AfterPicking();
    }  //ends if (requestCode ....
  }


}
