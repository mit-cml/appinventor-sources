// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.util.Log;
import android.Manifest;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.HoneycombMR1Util;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;

/**
 * Component enabling a user to select a contact.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author markf@google.com (Mark Friedman)
 * @author: Yifan(Evan) Li (for contact Uri)

 */
@DesignerComponent(version = YaVersion.CONTACTPICKER_COMPONENT_VERSION,
    description = "A button that, when clicked on, displays a list of " +
    "the contacts to choose among. After the user has made a " +
    "selection, the following properties will be set to information about " +
    "the chosen contact: <ul>\n" +
    "<li> <code>ContactName</code>: the contact's name </li>\n "  +
    "<li> <code>EmailAddress</code>: the contact's primary email address </li>\n " +
    "<li> <code>ContactUri</code>: the contact's URI on the device </li>\n"+
    "<li> <code>EmailAddressList</code>: a list of the contact's email addresses </li>\n " +
    "<li> <code>PhoneNumber</code>: the contact's primary phone number (on Later Android Verisons)</li>\n " +
    "<li> <code>PhoneNumberList</code>: a list of the contact's phone numbers (on Later Android Versions)</li>\n " +
    "<li> <code>Picture</code>: the name of the file containing the contact's " +
    "image, which can be used as a <code>Picture</code> property value for " +
    "the <code>Image</code> or <code>ImageSprite</code> component.</li></ul>\n" +
    "</p><p>Other properties affect the appearance of the button " +
    "(<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and " +
    "whether it can be clicked on (<code>Enabled</code>).\n</p>" +
    "<p>The ContactPicker component might not work on all phones. For " +
    "example, on Android systems before system 3.0, it cannot pick phone " +
    "numbers, and the list of email addresses will contain only one email.",
    category = ComponentCategory.SOCIAL)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.READ_CONTACTS")
public class ContactPicker extends Picker implements ActivityResultListener {

  private static String[] CONTACT_PROJECTION;
  private static String[] DATA_PROJECTION;
  private static final String[] PROJECTION = {
    Contacts.PeopleColumns.NAME,
    Contacts.People.PRIMARY_EMAIL_ID,
  };

  private static final int NAME_INDEX = 0;
  private static final int EMAIL_INDEX = 1;
  private static final int PHONE_INDEX = 2;

  protected final Activity activityContext;
  private final Uri intentUri;

  protected String contactName;
  protected String emailAddress;
  protected String contactUri;
  protected String contactPictureUri;
  protected String phoneNumber;

  protected List emailAddressList;
  protected List phoneNumberList;

  private boolean havePermission = false; // Do we have READ_CONTACTS permission?

  /**
   * Create a new ContactPicker component.
   *
   * @param container the parent container.
   */
  public ContactPicker(ComponentContainer container) {
    this(container, Contacts.People.CONTENT_URI);
  }

  protected ContactPicker(ComponentContainer container, Uri intentUri) {
    super(container);
    activityContext = container.$context();

    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_HONEYCOMB_MR1 && intentUri.equals(Contacts.People.CONTENT_URI)) {
      this.intentUri = HoneycombMR1Util.getContentUri();
    } else if (SdkLevel.getLevel() >= SdkLevel.LEVEL_HONEYCOMB_MR1 && intentUri.equals(Contacts.Phones.CONTENT_URI)) {
      this.intentUri = HoneycombMR1Util.getPhoneContentUri();
    } else {
      this.intentUri = intentUri;
    }
  }

  @Override
  public void click() {
    if (!havePermission) {
      container.$form()
        .askPermission(Manifest.permission.READ_CONTACTS,
                       new PermissionResultHandler() {
                         @Override
                         public void HandlePermissionResponse(String permission, boolean granted) {
                           if (granted) {
                             ContactPicker.this.havePermission = true;
                             ContactPicker.this.click();
                           } else {
                             ContactPicker
                               .this.container.$form()
                               .dispatchErrorOccurredEvent(ContactPicker.this, "ContactPicker",
                                                           ErrorMessages.ERROR_NO_READ_CONTACTS_PERMISSION, "");
                           }
                         }
                       });
      return;
    }
    super.click();
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
    // Note(halabelson):  I am commenting out this test.  Android provider.Contacts was
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
   * "URI that specifies the location of the contact on the device.",
   */
  @SimpleProperty(description = "URI that specifies the location of the contact on the device.",
      category = PropertyCategory.BEHAVIOR)
  public String ContactUri() {
    return ensureNotNull(contactUri);
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

  /**
   *  return nothing, just call another activity which is view contact
   */
  @SimpleFunction(description = "view a contact via its URI")
  public void ViewContact(String uri) {
    if(contactUri != null){
        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(uri));
        if (intent.resolveActivity(this.activityContext.getPackageManager()) != null) {
            this.activityContext.startActivity(intent);
        }
    }
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
      Uri receivedContactUri = data.getData();

      // Pre- and post-Honeycomb need different URIs.
      String desiredContactUri = "";
      if (SdkLevel.getLevel() >= SdkLevel.LEVEL_HONEYCOMB_MR1) {
        desiredContactUri = "//com.android.contacts/contact";
      } else {
        desiredContactUri = "//contacts/people";
      }

      if (checkContactUri(receivedContactUri, desiredContactUri)) {
        Cursor contactCursor = null;
        Cursor dataCursor = null;
        try {
          if (SdkLevel.getLevel() >= SdkLevel.LEVEL_HONEYCOMB_MR1) {
            CONTACT_PROJECTION = HoneycombMR1Util.getContactProjection();
            contactCursor = activityContext.getContentResolver().query(receivedContactUri,
                CONTACT_PROJECTION, null, null, null);

            String id = postHoneycombGetContactNameAndPicture(contactCursor);

            DATA_PROJECTION = HoneycombMR1Util.getDataProjection();
            dataCursor = HoneycombMR1Util.getDataCursor(id, activityContext, DATA_PROJECTION);
            postHoneycombGetContactEmailAndPhone(dataCursor);

            //explicit set TextContactUri
            contactUri = receivedContactUri.toString();
          } else {
            contactCursor = activityContext.getContentResolver().query(receivedContactUri,
                PROJECTION, null, null, null);
            preHoneycombGetContactInfo(contactCursor, receivedContactUri);
          }
          Log.i("ContactPicker",
                "Contact name = " + contactName + ", email address = " + emailAddress + ",contact Uri = " + contactUri +
                ", phone number = " + phoneNumber + ", contactPhotoUri = " +  contactPictureUri);
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
    } // ends if (requestCode ...
  }

  /**
   * For versions before Honeycomb, we get all the contact info from the same table.
   */
  public void preHoneycombGetContactInfo(Cursor contactCursor, Uri theContactUri) {
    if (contactCursor.moveToFirst()) {
      contactName = guardCursorGetString(contactCursor, NAME_INDEX);
      String emailId = guardCursorGetString(contactCursor, EMAIL_INDEX);
      emailAddress = getEmailAddress(emailId);
      contactUri = theContactUri.toString();
      contactPictureUri = theContactUri.toString();
      emailAddressList = emailAddress.equals("") ? new ArrayList() : Arrays.asList(emailAddress);
    }
  }

  /**
   * Assigns contactName and contactPictureUri for Honeycomb and up.
   * Returns id for getting emailAddress and phoneNumber.
   */
  public String postHoneycombGetContactNameAndPicture(Cursor contactCursor) {
    String id = "";
    if (contactCursor.moveToFirst()) {
      final int ID_INDEX = HoneycombMR1Util.getIdIndex(contactCursor);
      final int NAME_INDEX = HoneycombMR1Util.getNameIndex(contactCursor);
      final int THUMBNAIL_INDEX = HoneycombMR1Util.getThumbnailIndex(contactCursor);
      final int PHOTO_INDEX = HoneycombMR1Util.getPhotoIndex(contactCursor);
      id = guardCursorGetString(contactCursor, ID_INDEX);
      contactName = guardCursorGetString(contactCursor, NAME_INDEX);
      contactPictureUri = guardCursorGetString(contactCursor, THUMBNAIL_INDEX);

      Log.i("ContactPicker", "photo_uri=" + guardCursorGetString(contactCursor, PHOTO_INDEX));
    }
    return id;
  }

  /**
   * Assigns emailAddress, phoneNumber, emailAddressList, and phoneNumberList
   * for Honeycomb and up.
   */
  public void postHoneycombGetContactEmailAndPhone(Cursor dataCursor) {
    phoneNumber = "";
    emailAddress = "";
    List<String> phoneListToStore = new ArrayList<String>();
    List<String> emailListToStore = new ArrayList<String>();

    if (dataCursor.moveToFirst()) {
      final int PHONE_INDEX = HoneycombMR1Util.getPhoneIndex(dataCursor);
      final int EMAIL_INDEX = HoneycombMR1Util.getEmailIndex(dataCursor);
      final int MIME_INDEX = HoneycombMR1Util.getMimeIndex(dataCursor);

      String phoneType = HoneycombMR1Util.getPhoneType();
      String emailType = HoneycombMR1Util.getEmailType();

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
    }

    if (!phoneListToStore.isEmpty()) {
      phoneNumber = phoneListToStore.get(0);
    }
    if (!emailListToStore.isEmpty()) {
      emailAddress = emailListToStore.get(0);
    }
    phoneNumberList = phoneListToStore;
    emailAddressList = emailListToStore;

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
      Log.i("ContactPicker", suspectUri.getPath());
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

  /**
   * Email address getter for pre-Honeycomb.
   */
  protected String getEmailAddress(String emailId) {
    int id;
    try {
      id = Integer.parseInt(emailId);
    } catch (NumberFormatException e) {
      return "";
    }

    String data = "";
    String where = "contact_methods._id = " + id;
    String[] projection = {
      Contacts.ContactMethods.DATA
    };
    Cursor cursor = activityContext.getContentResolver().query(
        Contacts.ContactMethods.CONTENT_EMAIL_URI,
        projection, where, null, null);
    try {
      if (cursor.moveToFirst()) {
        data = guardCursorGetString(cursor, 0);
      }
    } finally {
      cursor.close();
    }
    // this extra check for null might be redundant, but we given that there are mysterious errors
    // on some phones, we'll leave it in just to be extra careful
    return ensureNotNull(data);
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
