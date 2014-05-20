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
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

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

/*  private static final String[] PROJECTION = {
    Contacts.PeopleColumns.NAME,
    Contacts.PhonesColumns.NUMBER,
    Contacts.Phones.PERSON_ID,
    Contacts.People.PRIMARY_EMAIL_ID,
  };*/

  private static final String[] NAME_PROJECTION = {
    Data.CONTACT_ID,
    ContactsContract.Contacts.DISPLAY_NAME,
    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
  };

  private static final String[] PHONE_PROJECTION = {
    Data.CONTACT_ID,
    Phone.NUMBER,
    Phone.TYPE,
  };

  private static final String[] EMAIL_PROJECTION = {
    Data.CONTACT_ID,
    Email.CONTACT_ID,
    Email.ADDRESS,
    Email.TYPE,
  };

/*  private static final int NAME_INDEX = 0;
  private static final int NUMBER_INDEX = 1;
  private static final int PERSON_INDEX = 2;
  private static final int EMAIL_INDEX = 3;
*/
  private String phoneNumber;

  /**
   * Create a new ContactPicker component.
   *
   * @param container the parent container.
   */
  public PhoneNumberPicker(ComponentContainer container) {
    //super(container, Contacts.Phones.CONTENT_URI);
    super(container, Phone.CONTENT_URI);
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
      if (checkContactUri(phoneUri, "//com.android.contacts/data")) {
        // This test is not good enough.  The lookup code below does not work with
        // Motorola Blur (Droid Global), even though the URI has the correct form.
        // Hopefully, moving to the new contact scheme will solve this problem.
        // update: did it solve the problem?
        Cursor contactCursor = null;
        Cursor phoneCursor = null;
        Cursor emailCursor = null;
        try {
          
          String id = "";
          
          contactCursor = activityContext.getContentResolver().query(phoneUri,
              NAME_PROJECTION, null, null, null);
          if (contactCursor.moveToFirst()) {
            final int ID_INDEX = contactCursor.getColumnIndex(Data.CONTACT_ID);
            final int NAME_INDEX = contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            final int PHOTO_INDEX = contactCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
            
            id = guardCursorGetString(contactCursor, ID_INDEX);
            contactName = guardCursorGetString(contactCursor, NAME_INDEX);
            contactPictureUri = guardCursorGetString(contactCursor, PHOTO_INDEX);
            
          }
          
          phoneCursor = activityContext.getContentResolver().query(phoneUri,
              PHONE_PROJECTION, null, null, null);
          //Get the first nonempty phone number
          if(phoneCursor.moveToFirst()){
            final int NUMBER_INDEX = phoneCursor.getColumnIndex(Phone.NUMBER);       
            phoneNumber = guardCursorGetString(phoneCursor, NUMBER_INDEX);
          }

          emailAddress = "";
          emailCursor = activityContext.getContentResolver().query(Email.CONTENT_URI, 
              EMAIL_PROJECTION, Data.CONTACT_ID + "=?", new String [] {id}, null);

          //Get the first nonempty email
          if(emailCursor.moveToFirst()){
            final int EMAIL_INDEX = emailCursor.getColumnIndex(Email.ADDRESS);
            
            while(!emailCursor.isAfterLast()){
              
              emailAddress = guardCursorGetString(emailCursor, EMAIL_INDEX);

              if(emailAddress != null && emailAddress.length() > 0){
                break;
              }

              emailCursor.moveToNext();        
            }
          }
          
          Log.i("PhoneNumberPicker",
              "Contact name = " + contactName + ", phone number = " + phoneNumber +
              ", emailAddress = " + emailAddress + ", contactPhotoUri = " +  contactPictureUri); 

        } catch (Exception e) {
          // There was an exception in trying to compute the cursor from the activity context.
          // It's bad form to catch an arbitrary exception, but if there is an error here
          // it's unclear what's going on.
          puntContactSelection(ErrorMessages.ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER);
        } finally {
          if(contactCursor != null) {
            contactCursor.close();
          }
          if(emailCursor != null){
            emailCursor.close();
          }
          if(phoneCursor != null){
            phoneCursor.close();
          }
        }
      } // ends if (checkContactUri ...
      AfterPicking();
    }  //ends if (requestCode ....
  }


}
