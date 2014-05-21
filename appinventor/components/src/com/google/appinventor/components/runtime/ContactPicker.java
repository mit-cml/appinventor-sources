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
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Component enabling a user to select a contact.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author markf@google.com (Mark Friedman)
 */
@DesignerComponent(version = YaVersion.CONTACTPICKER_COMPONENT_VERSION,
    description = "A button that, when clicked on, displays a list of " +
    "the contacts to choose among. After the user has made a " +
    "selection, the following properties will be set to information about " +
    "the chosen contact: <ul>\n" +
    "<li> <code>ContactName</code>: the contact's name </li>\n "  +
    "<li> <code>EmailAddress</code>: the contact's primary email address </li>\n " +
    "<li> <code>EmailAddressList</code>: a list of the contact's email addresses </li>\n " +
    "<li> <code>PhoneNumber</code>: the contact's primary phone number </li>\n " +
    "<li> <code>PhoneNumberList</code>: a list of the contact's phone numbers </li>\n " +
    "<li> <code>Picture</code>: the name of the file containing the contact's " +
    "image, which can be used as a <code>Picture</code> property value for " +
    "the <code>Image</code> or <code>ImageSprite</code> component.</li></ul>\n" +
    "</p><p>Other properties affect the appearance of the button " +
    "(<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and " +
    "whether it can be clicked on (<code>Enabled</code>).\n</p>" +
    "<p>Picking is not supported on all phones.  If it fails, this component will " +
    "show a notification.  The error behavior can be overridden with the " +
    "Screen.ErrorOccurred event handler.",
    category = ComponentCategory.SOCIAL)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.READ_CONTACTS")
public class ContactPicker extends Picker implements ActivityResultListener {

  private static final String[] CONTACT_PROJECTION = {
    ContactsContract.Contacts._ID,
    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
    ContactsContract.Contacts.PHOTO_URI,
  };

  private static final String[] DATA_PROJECTION = {
    Data.MIMETYPE,
    Email.ADDRESS,
    Email.TYPE,
    Phone.NUMBER,
    Phone.TYPE,
  };

  protected final Activity activityContext;
  private final Uri intentUri;

  protected String contactName;
  protected String emailAddress;
  protected String contactPictureUri;
  protected String phoneNumber;

  protected List emailAddressList;
  protected List phoneNumberList;

  /**
   * Create a new ContactPicker component.
   *
   * @param container the parent container.
   */
  public ContactPicker(ComponentContainer container) {
    this(container, ContactsContract.Contacts.CONTENT_URI);
  }

  protected ContactPicker(ComponentContainer container, Uri intentUri) {
    super(container);
    activityContext = container.$context();
    this.intentUri = intentUri;
  }

  /**
   * Picture URI for this contact, which can be
   * used to retrieve the contact's photo and other fields.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
      public String Picture() {
      return ensureNotNull(contactPictureUri);
    }

  /**
   * Name property getter method.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String ContactName() {
    return ensureNotNull(contactName);
  }

  /**
   * EmailAddress property getter method.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String EmailAddress() {
    // Note(halabelson):  I am commenting out this test.  Android provider.Constacts was
    // deprecated in Donut, but email picking still seems to work on newer versions of the SDK.
    // If there's a phone where it does not work, we'll get the error at PuntContactSelection
    // Note that there is still a general problem with contact picking on Motoblur.
    //    if (SdkLevel.getLevel() > SdkLevel.LEVEL_DONUT) {
    //      container.$form().dispatchErrorOccurredEvent(this, "EmailAddress",
    //          ErrorMessages.ERROR_FUNCTIONALITY_NOT_SUPPORTED_CONTACT_EMAIL);
    //    }
    return ensureNotNull(emailAddress);
  }

  /**
   * EmailAddressList property getter method.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public List EmailAddressList() {
    return ensureNotNull(emailAddressList);
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
   * PhoneNumberList property getter method.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public List PhoneNumberList() {
    return ensureNotNull(phoneNumberList);
  }

  @Override
  protected Intent getIntent() {
    return new Intent(Intent.ACTION_PICK, intentUri);
  }

  /**
   * Callback method to get the result returned by the contact picker activity
   *
   * @param requestCode a code identifying the request.
   * @param resultCode a code specifying success or failure of the activity
   * @param data the returned data, in this case an Intent whose data field
   *        contains the contact's content provider Uri.
   */
  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
      Log.i("ContactPicker", "received intent is " + data);
      Uri contactUri = data.getData();


      if (checkContactUri(contactUri, "//com.android.contacts/contact")) {
        Cursor contactCursor = null;
        Cursor dataCursor = null;
        try {
          contactCursor = activityContext.getContentResolver().query(contactUri,
              CONTACT_PROJECTION, null, null, null);

          if (contactCursor.moveToFirst()) {

            final int ID_INDEX = contactCursor.getColumnIndex(ContactsContract.Contacts._ID);
            final int NAME_INDEX = contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            final int THUMBNAIL_INDEX = contactCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
            final int PHOTO_INDEX = contactCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI);
            final int HAS_PHONE_NUMBER = contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

            String id = guardCursorGetString(contactCursor, ID_INDEX);
            contactName = guardCursorGetString(contactCursor, NAME_INDEX);
            contactPictureUri = guardCursorGetString(contactCursor, THUMBNAIL_INDEX);

            Log.i("ContactPicker", "photo_uri=" + guardCursorGetString(contactCursor, PHOTO_INDEX));

            dataCursor = activityContext.getContentResolver().query(
                Data.CONTENT_URI,
                DATA_PROJECTION,
                Data.CONTACT_ID + "=? AND (" + Data.MIMETYPE + "=? OR " + Data.MIMETYPE + "=?)",
                new String[] {id, Phone.CONTENT_ITEM_TYPE, Email.CONTENT_ITEM_TYPE},
                null);

            List<String> phoneListToStore = new ArrayList<String>();
            List<String> emailListToStore = new ArrayList<String>();
            phoneNumber = "";
            emailAddress = "";

            if (dataCursor.moveToFirst()) {
              final int PHONE_INDEX = dataCursor.getColumnIndex(Phone.NUMBER);
              final int EMAIL_INDEX = dataCursor.getColumnIndex(Email.ADDRESS);
              final int MIME_INDEX = dataCursor.getColumnIndex(Data.MIMETYPE);

              while (!dataCursor.isAfterLast()) {
                String type = guardCursorGetString(dataCursor, MIME_INDEX);
                if (type.contains(Phone.CONTENT_ITEM_TYPE)) {
                  phoneListToStore.add(guardCursorGetString(dataCursor, PHONE_INDEX));
                } else if (type.contains(Email.CONTENT_ITEM_TYPE)) {
                  emailListToStore.add(guardCursorGetString(dataCursor, EMAIL_INDEX));
                } else {
                  Log.i("ContactPicker", "Type mismatch: " + type +
                      " not " + Phone.CONTENT_ITEM_TYPE + 
                      " or " + Email.CONTENT_ITEM_TYPE);
                }
                dataCursor.moveToNext();
              }
            }

            if (!phoneListToStore.isEmpty()) {
              phoneNumber = phoneListToStore.get(0);
            }

            if (!emailListToStore.isEmpty()) {
              emailAddress = emailListToStore.get(0);
            }

            phoneNumberList = phoneListToStore;
            emailAddressList = emailListToStore;

            Log.i("ContactPicker",
                "Contact name = " + contactName + ", email address = " + emailAddress +
                ", phone number = " + phoneNumber + ", contactPhotoUri = " +  contactPictureUri);
          }

        } catch (Exception e) {
          // There was an exception in trying to extract the cursor from the activity context.
          // It's bad form to catch an arbitrary exception, but if there is an error here
          // it's unclear what's going on.
          Log.i("ContactPicker", "checkContactUri failed: D");
          puntContactSelection(ErrorMessages.ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER);
        } finally {
          if (contactCursor != null) {
            contactCursor.close();
          }
          if (dataCursor != null) {
            dataCursor.close();
          }
        }
      } // ends if (checkContactUri ...
      AfterPicking();
    }  //ends if (requestCode ....
  }

  // Check that the contact URI has the right form to permit the information to be
  // extracted and try to show a meaningful error notice to the end user of the app.
  // Sadly, different phones can produce different kinds of URIs.  You
  // can also get a different Uri depending on whether or not the user
  // does a search to get the contact, versus just picking it.  For example,
  // Motorola Global phones produce an intent whose data part is null.
  // Or using search on Nexus phones will produce a contact URI of the form
  // content://com.android.contacts/contact, whereas doing direct selection
  // produces a Uri have a specific required pattern that is
  // passed in as an argument.
  // TODO(halabelson): Create a better set of tests and/or generalize the extraction
  // methods to permit more URIs.
  // This should be done in conjunction with updating the way we handle contacts.

  protected boolean checkContactUri(Uri suspectUri, String requiredPattern) {
    Log.i("ContactPicker", "contactUri is " + suspectUri);
    if (suspectUri == null || (!("content".equals(suspectUri.getScheme())))) {
      Log.i("ContactPicker", "checkContactUri failed: A");
      puntContactSelection(
          ErrorMessages.ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER);
      return false;
    }
    String UriSpecific = suspectUri.getSchemeSpecificPart();

    if (!UriSpecific.startsWith(requiredPattern)) {
      Log.i("ContactPicker", "checkContactUri failed: C");
      Log.i("Contact Picker", suspectUri.getPath());
      puntContactSelection(ErrorMessages.ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER);
      return false;
    } else {
      return true;
    }
  }

  // set the (supposedly) extracted properties to the empty string and
  // report an error
  protected void puntContactSelection(int errorNumber) {
    contactName = "";
    emailAddress = "";
    contactPictureUri = "";
    container.$form().dispatchErrorOccurredEvent(this, "", errorNumber);
  }

  // If the selection returns null, this should be passed back as a
  // an empty string to prevent errors if the app tries to convert this
  // to a string. In some cases, getString can also throw an exception, for example,
  // in selecting the name for a contact where there is no name.
  // We also call ensureNotNull in the property selectors for ContactName, etc.
  // This would appear to be redundant, but in testing, there have been some mysterious
  // error conditions on some phones that permit nulls to sneak through from guardCursonGetString,
  // so we'll do the extra check.

  protected String guardCursorGetString(Cursor cursor, int index) {
    String result;

    try {
      result = cursor.getString(index);
    } catch (Exception e) {
      // It's bad practice to catch a general exception, but unfortunately,
      // the exception thrown is implementation dependent, according to the
      // Android documentation.
      result = "";
    }
    return ensureNotNull(result);
  }

  protected String ensureNotNull(String value) {
    if (value == null) {
      return "";
    } else {
      return value;
    }
  }

  protected List ensureNotNull(List value) {
    if (value == null) {
      return new ArrayList();
    } else {
      return value;
    }
  }
}
