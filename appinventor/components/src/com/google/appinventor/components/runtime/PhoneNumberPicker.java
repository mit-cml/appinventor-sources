// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.HoneycombMR1Util;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.util.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Component enabling a user to select a contact's phone number.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author markf@google.com (Mark Friedman)
 */
@DesignerComponent(version = YaVersion.PHONENUMBERPICKER_COMPONENT_VERSION,
    description = "A button that, when clicked on, displays a list of " +
    "the contacts' phone numbers to choose among. After the user has made a " +
    "selection, the following properties will be set to information about " +
    "the chosen contact: <ul>\n" +
    "<li> <code>ContactName</code>: the contact's name </li>\n "  +
    "<li> <code>PhoneNumber</code>: the contact's phone number </li>\n " +
    "<li> <code>EmailAddress</code>: the contact's email address </li> " +
    "<li> <code>Picture</code>: the name of the file containing the contact's " +
    "image, which can be used as a <code>Picture</code> property value for " +
    "the <code>Image</code> or <code>ImageSprite</code> component.</li></ul>\n" +
    "</p><p>Other properties affect the appearance of the button " +
    "(<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and " +
    "whether it can be clicked on (<code>Enabled</code>).</p>\n" +
    "<p>The PhoneNumberPicker component may not work on all Android " +
    "devices. For example, on Android systems before system 3.0, the " +
    "returned lists of phone numbers and email addresses will be empty.\n",
    category = ComponentCategory.SOCIAL)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.READ_CONTACTS")
public class PhoneNumberPicker extends ContactPicker {

  private static String[] NAME_PROJECTION;
  private static String[] DATA_PROJECTION;
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
  private static final String LOG_TAG = "PhoneNumberPicker";

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
      Log.i(LOG_TAG, "received intent is " + data);
      Uri phoneUri = data.getData();

      String desiredPhoneUri = "";
      if (SdkLevel.getLevel() >= SdkLevel.LEVEL_HONEYCOMB_MR1) {
        desiredPhoneUri = "//com.android.contacts/data";
      } else {
        desiredPhoneUri = "//contacts/phones";
      }

      if (checkContactUri(phoneUri, desiredPhoneUri)) {
        Cursor contactCursor = null;
        Cursor dataCursor = null;
        try {
          if (SdkLevel.getLevel() >= SdkLevel.LEVEL_HONEYCOMB_MR1) {
            NAME_PROJECTION = HoneycombMR1Util.getNameProjection();
            contactCursor = activityContext.getContentResolver().query(phoneUri,
                NAME_PROJECTION, null, null, null);
            String id = postHoneycombGetContactNameAndPicture(contactCursor);

            DATA_PROJECTION = HoneycombMR1Util.getDataProjection();
            dataCursor = HoneycombMR1Util.getDataCursor(id, activityContext, DATA_PROJECTION);
            postHoneycombGetContactEmailsAndPhones(dataCursor);
          } else {
            contactCursor = activityContext.getContentResolver().query(phoneUri,
                PROJECTION, null, null, null);
            preHoneycombGetContactInfo(contactCursor);
          }

          Log.i(LOG_TAG,
              "Contact name = " + contactName + ", phone number = " + phoneNumber +
              ", emailAddress = " + emailAddress + ", contactPhotoUri = " +  contactPictureUri);
        } catch (Exception e) {
          // There was an exception in trying to compute the cursor from the activity context.
          // It's bad form to catch an arbitrary exception, but if there is an error here
          // it's unclear what's going on.
          Log.e(LOG_TAG, "Exception in resultReturned", e);
          puntContactSelection(ErrorMessages.ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER);
        } finally {
          if (contactCursor != null) {
            contactCursor.close();
          }
          if (dataCursor != null){
            dataCursor.close();
          }
        }
      } // ends if (checkContactUri ...
      AfterPicking();
    } // ends if (requestCode ...
  }


  /**
   * For versions before Honeycomb, we get all the contact info from the same table.
   */
  public void preHoneycombGetContactInfo(Cursor cursor) {
    if (cursor.moveToFirst()) {
      contactName = guardCursorGetString(cursor, NAME_INDEX);
      phoneNumber = guardCursorGetString(cursor, NUMBER_INDEX);
      int contactId = cursor.getInt(PERSON_INDEX);
      Uri cUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, contactId);
      contactPictureUri = cUri.toString();
      String emailId = guardCursorGetString(cursor, EMAIL_INDEX);
      emailAddress = getEmailAddress(emailId);
    }
  }

  /**
   * Assigns contactName and contactPictureUri for Honeycomb and up.
   * Returns id for getting emailAddress and phoneNumber.
   */
  public String postHoneycombGetContactNameAndPicture(Cursor contactCursor) {
    String id = "";
    if (contactCursor.moveToFirst()) {
      final int CONTACT_ID_INDEX = HoneycombMR1Util.getContactIdIndex(contactCursor);
      final int NAME_INDEX = HoneycombMR1Util.getNameIndex(contactCursor);
      final int PHOTO_INDEX = HoneycombMR1Util.getThumbnailIndex(contactCursor);
      final int PHONE_INDEX = HoneycombMR1Util.getPhoneIndex(contactCursor);
      phoneNumber = guardCursorGetString(contactCursor, PHONE_INDEX);
      id = guardCursorGetString(contactCursor, CONTACT_ID_INDEX);
      contactName = guardCursorGetString(contactCursor, NAME_INDEX);
      contactPictureUri = guardCursorGetString(contactCursor, PHOTO_INDEX);
    }
    return id;
  }

  /**
   * Assigns emailAddress, phoneNumber, emailAddressList, and phoneNumberList
   * for Honeycomb and up.
   */
  public void postHoneycombGetContactEmailsAndPhones(Cursor dataCursor) {
    List<String> phoneListToStore = new ArrayList<String>();
    List<String> emailListToStore = new ArrayList<String>();
    if (dataCursor.moveToFirst()) {
      final int PHONE_INDEX = HoneycombMR1Util.getPhoneIndex(dataCursor);
      final int EMAIL_INDEX = HoneycombMR1Util.getEmailIndex(dataCursor);
      final int MIME_INDEX = HoneycombMR1Util.getMimeIndex(dataCursor);

      String phoneType = HoneycombMR1Util.getPhoneType();
      String emailType = HoneycombMR1Util.getEmailType();

      // Get the first (default) email and phone number associated with the contact.
      while (!dataCursor.isAfterLast()) {
        String type = guardCursorGetString(dataCursor, MIME_INDEX);
        if (type.contains(phoneType)) {
          phoneListToStore.add(guardCursorGetString(dataCursor, PHONE_INDEX));
        } else if (type.contains(emailType)) {
          emailListToStore.add(guardCursorGetString(dataCursor, EMAIL_INDEX));
        } else {
          Log.i("ContactPicker", "Type mismatch: " + type +
              " not " + phoneType +
              " or " + emailType);
        }
        dataCursor.moveToNext();
      }
      phoneNumberList = phoneListToStore;
      emailAddressList = emailListToStore;
      if (!emailAddressList.isEmpty()) {
        emailAddress = (String) emailAddressList.get(0);
      } else {
        emailAddress = "";
      }
    }
  }
}
