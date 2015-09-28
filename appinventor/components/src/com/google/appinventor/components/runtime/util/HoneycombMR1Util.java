// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.WebViewer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;

import java.io.InputStream;

/**
 * Helper methods for calling methods added in HONEYCOMB_MR1 (3.1, API level 12)
 *
 *
 */
public class HoneycombMR1Util {

  private HoneycombMR1Util() {
  }

  /**
   * Get the CONTENT_URI from ContactsContract to instantiate a ContactPicker.
   */
  public static Uri getContentUri() {
    return ContactsContract.Contacts.CONTENT_URI;
  }

  /**
   * Get the CONTENT_URI from Phone for PhoneNumberPicker.
   */
  public static Uri getPhoneContentUri() {
    return Phone.CONTENT_URI;
  }

    /**
   * Get Data.CONTENT_URI for EmailAddressAdapter.
   */
  public static Uri getDataContentUri() {
    return Data.CONTENT_URI;
  }

  /**
   * Get the CONTACT_PROJECTION for ContactPicker.
   */
  public static String[] getContactProjection() {
    String[] contactProjection = {
      ContactsContract.Contacts._ID,
      ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
      ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
      ContactsContract.Contacts.PHOTO_URI,
    };
    return contactProjection;
  }

  /**
   * Get the NAME_PROJECTION for PhoneNumberPicker.
   */
  public static String[] getNameProjection() {
    String[] nameProjection = {
      Data.CONTACT_ID,
      ContactsContract.Contacts.DISPLAY_NAME,
      ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
      Phone.NUMBER,
    };
    return nameProjection;
  }

  /**
   * Get the DATA_PROJECTION for ContactPicker and PhoneNumberPicker.
   */
  public static String[] getDataProjection() {
    String[] dataProjection = {
      Data.MIMETYPE,
      Email.ADDRESS,
      Email.TYPE,
      Phone.NUMBER,
      Phone.TYPE,
    };
    return dataProjection;
  }

  /**
   * Get the NEW_PROJECTION for EmailAddressAdapter.
   */
  public static String[] getEmailAdapterProjection() {
    String[] emailAdapterProjection = {
      Data._ID,
      Data.DISPLAY_NAME,
      Email.ADDRESS,
      Data.MIMETYPE,
    };
    return emailAdapterProjection;
  }

  /**
   * Get the Contacts._ID index for ContactPicker.
   */
  public static int getIdIndex(Cursor contactCursor) {
    return contactCursor.getColumnIndex(ContactsContract.Contacts._ID);
  }

  /**
   * Get the Data.CONTACT_ID index for PhoneNumberPicker.
   */
  public static int getContactIdIndex(Cursor contactCursor) {
    return contactCursor.getColumnIndex(Data.CONTACT_ID);
  }

  /**
   * Get the Contacts.DISPLAY_NAME index for ContactPicker.
   */
  public static int getNameIndex(Cursor contactCursor) {
    return contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
  }

  /**
   * Get the Contacts.PHOTO_THUMBNAIL_URI index for ContactPicker.
   */
  public static int getThumbnailIndex(Cursor contactCursor) {
    return contactCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
  }

  /**
   * Get the Contacts.PHOTO_URI index for ContactPicker.
   */
  public static int getPhotoIndex(Cursor contactCursor) {
    return contactCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI);
  }

  public static int getPhoneIndex(Cursor dataCursor) {
    return dataCursor.getColumnIndex(Phone.NUMBER);
  }

  public static int getEmailIndex(Cursor dataCursor) {
    return dataCursor.getColumnIndex(Email.ADDRESS);
  }

  public static int getMimeIndex(Cursor dataCursor) {
    return dataCursor.getColumnIndex(Data.MIMETYPE);
  }
  /**
   * Get Phone.CONTENT_ITEM_TYPE.
   */
  public static String getPhoneType() {
    return Phone.CONTENT_ITEM_TYPE;
  }

  /**
   * Get Email.CONTENT_ITEM_TYPE.
   */
  public static String getEmailType() {
    return Email.CONTENT_ITEM_TYPE;
  }

  /**
   * Get Data.DISPLAY_NAME for EmailAddressAdapter.
   */
  public static String getDisplayName() {
    return Data.DISPLAY_NAME;
  }

  /**
   * Get Email.ADDRESS for EmailAddressAdapter.
   */
  public static String getEmailAddress() {
    return Email.ADDRESS;
  }

  /**
   * Get Data.MIMETYPE for EmailAddressAdapter.
   */
  public static String getDataMimeType() {
    return Data.MIMETYPE;
  }

  /**
   * Gets the dataCursor for ContactPicker and PhoneNumberPicker (used for Email and Phone).
   */
  public static Cursor getDataCursor(String id, Activity activityContext, String[] dataProjection) {
    Cursor dataCursor = activityContext.getContentResolver().query(
        Data.CONTENT_URI,
        dataProjection,
        Data.CONTACT_ID + "=? AND (" + Data.MIMETYPE + "=? OR " + Data.MIMETYPE + "=?)",
        new String[] {id, Phone.CONTENT_ITEM_TYPE, Email.CONTENT_ITEM_TYPE},
        null);
    return dataCursor;
  }

  /**
   * Gets the contact photo InputStream for MediaUtil.
   */
  public static InputStream openContactPhotoInputStreamHelper(ContentResolver cr, Uri contactUri) {
    return ContactsContract.Contacts.openContactPhotoInputStream(cr, contactUri);
  }

  /**
   * Get Data.TIMES_CONTACTED for EmailAddressAdapter.
   */
  public static String getTimesContacted() {
    return Data.TIMES_CONTACTED;
  }

}
