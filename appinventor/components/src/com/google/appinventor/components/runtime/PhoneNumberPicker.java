// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.EclairUtil;
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
    "<p>Picking is not supported on all phones.  If it fails, this component will " +
    "show a notification.  This default error behavior can be overridden with the " +
    "Screen.ErrorOccurred event handler.",
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

      String desiredPhoneUri = "";
      if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR) {
        desiredPhoneUri = "//com.android.contacts/data";
      } else {
        desiredPhoneUri = "//contacts/phones";
      }

      if (checkContactUri(phoneUri, desiredPhoneUri)) {
        Cursor contactCursor = null;
        Cursor dataCursor = null;
        try {
          if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR) {
            NAME_PROJECTION = EclairUtil.getNameProjection();
            contactCursor = activityContext.getContentResolver().query(phoneUri,
                NAME_PROJECTION, null, null, null);
            String id = postEclairGetContactNameAndPicture(contactCursor);

            DATA_PROJECTION = EclairUtil.getDataProjection();
            dataCursor = EclairUtil.getDataCursor(id, activityContext, DATA_PROJECTION);
            postEclairGetContactEmailAndPhone(dataCursor);
          } else {
            contactCursor = activityContext.getContentResolver().query(phoneUri,
                PROJECTION, null, null, null);
            preEclairGetContactInfo(contactCursor);
          }
          // Since PhoneNumberPicker inherits from ContactPicker, this provides minimal
          // behavior for the phone and email list properties.
          phoneNumberList = phoneNumber.equals("") ? new ArrayList() : Arrays.asList(phoneNumber);
          emailAddressList = emailAddress.equals("") ? new ArrayList() : Arrays.asList(emailAddress);

          Log.i("PhoneNumberPicker",
              "Contact name = " + contactName + ", phone number = " + phoneNumber +
              ", emailAddress = " + emailAddress + ", contactPhotoUri = " +  contactPictureUri);
        } catch (Exception e) {
          // There was an exception in trying to compute the cursor from the activity context.
          // It's bad form to catch an arbitrary exception, but if there is an error here
          // it's unclear what's going on.
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
   * For versions before Eclair, we get all the contact info from the same table.
   */
  public void preEclairGetContactInfo(Cursor cursor) {
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
   * Assigns contactName and contactPictureUri for Eclair and up.
   * Returns id for getting emailAddress and phoneNumber.
   */
  public String postEclairGetContactNameAndPicture(Cursor contactCursor) {
    String id = "";
    if (contactCursor.moveToFirst()) {
      final int CONTACT_ID_INDEX = EclairUtil.getContactIdIndex(contactCursor);
      final int NAME_INDEX = EclairUtil.getNameIndex(contactCursor);
      final int PHOTO_INDEX = EclairUtil.getThumbnailIndex(contactCursor);

      id = guardCursorGetString(contactCursor, CONTACT_ID_INDEX);
      contactName = guardCursorGetString(contactCursor, NAME_INDEX);
      contactPictureUri = guardCursorGetString(contactCursor, PHOTO_INDEX);
    }
    return id;
  }

  /**
   * Assigns emailAddress, phoneNumber, emailAddressList, and phoneNumberList
   * for Eclair and up.
   */
  public void postEclairGetContactEmailAndPhone(Cursor dataCursor) {
    phoneNumber = "";
    emailAddress = "";
    if (dataCursor.moveToFirst()) {
      final int PHONE_INDEX = EclairUtil.getPhoneIndex(dataCursor);
      final int EMAIL_INDEX = EclairUtil.getEmailIndex(dataCursor);
      final int MIME_INDEX = EclairUtil.getMimeIndex(dataCursor);

      // Get the first (default) email and phone number associated with the contact.
      while (!dataCursor.isAfterLast()) {
        String type = guardCursorGetString(dataCursor, MIME_INDEX);
        if (type.contains(EclairUtil.getPhoneType()) && (phoneNumber == "")) {
          phoneNumber = guardCursorGetString(dataCursor, PHONE_INDEX);
        } else if (type.contains(EclairUtil.getEmailType()) && (emailAddress == "")) {
          emailAddress = guardCursorGetString(dataCursor, EMAIL_INDEX);
        }
        dataCursor.moveToNext();
      }
    }
  }
}
