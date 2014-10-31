// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.util.Log;

import com.google.appinventor.components.runtime.NearField;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Helper methods for calling methods added in Gingerbread (2.3, API level 9).
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class GingerbreadUtil {
  private GingerbreadUtil() {
  }

  /**
   * Creates a new CookieManager instance.
   */
  public static CookieHandler newCookieManager() {
    return new CookieManager();
  }

  /**
   * Clears the cookies in the given cookie handler. Cookies can only be cleared if the
   * cookieHandler is a CookieManager with a non-null CookieStore.
   *
   * @param cookieHandler the cookie handler where cookies should be cleared
   * @return true if cookies were cleared; false otherwise
   */
  public static boolean clearCookies(CookieHandler cookieHandler) {
    if (cookieHandler instanceof CookieManager) {
      CookieManager cookieManager = (CookieManager) cookieHandler;
      CookieStore cookieStore = cookieManager.getCookieStore();
      if (cookieStore != null) {
        cookieStore.removeAll();
        return true;
      }
    }
    return false;
  }

  /**
   * Creates a new NfcAdapter instance.
   */
  public static NfcAdapter newNfcAdapter(Context context) {
    return NfcAdapter.getDefaultAdapter(context);
  }

  public static void enableNFCWriteMode(Activity activity, NfcAdapter nfcAdapter, String textToWrite){
    NdefRecord textRecord = createTextRecord(textToWrite,true);
    NdefMessage msg = new NdefMessage(new NdefRecord[] { textRecord });
    nfcAdapter.enableForegroundNdefPush(activity, msg);
  }

  public static void disableNFCAdapter(Activity activity, NfcAdapter nfcAdapter){
    nfcAdapter.disableForegroundNdefPush(activity);
  }

  public static NdefRecord createTextRecord(String payload, boolean encodeInUtf8) {
    byte[] langBytes = Locale.getDefault().getLanguage().getBytes(Charset.forName("US-ASCII"));
    Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
    byte[] textBytes = payload.getBytes(utfEncoding);
    int utfBit = encodeInUtf8 ? 0 : (1 << 7);
    char status = (char) (utfBit + langBytes.length);
    byte[] data = new byte[1 + langBytes.length + textBytes.length];
    data[0] = (byte) status;
    System.arraycopy(langBytes, 0, data, 1, langBytes.length);
    System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
    NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
        NdefRecord.RTD_TEXT, new byte[0], data);
    return record;
  }

  public static void resolveNFCIntent(Intent intent, NearField nfc){
    String action = intent.getAction();
    //activity.setIntent(new Intent()); // Consume this intent.  Is this the right thing?
    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
      // When a tag is discovered we send it to the service to be save. We
      // include a PendingIntent for the service to call back onto. This
      // will cause this activity to be restarted with onNewIntent(). At
      // that time we read it from the database and view it.
      // We'll keep this database code in here for now, but it's useless, because we
      // can use AppInventor higher level operations to manipulate the tag data.

      if (nfc.ReadMode()) {

        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage[] msgs;
        if (rawMsgs != null) {
          msgs = new NdefMessage[rawMsgs.length];
          for (int i = 0; i < rawMsgs.length; i++) {
            msgs[i] = (NdefMessage) rawMsgs[i];
          }
        } else {
          // Unknown tag type
          // For now, just ignore it. Later we might want to signal an error to the
          // app user.
          byte[] empty = new byte[] {};
          NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
          NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
          msgs = new NdefMessage[] {msg};
        }
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        //the substring chops off the two language encoding bits at the beginning
        String message = new String(payload).substring(3);
        nfc.TagRead(message);
      } else {
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NdefMessage msg = null;
        if(nfc.WriteType() == 1) {
          NdefRecord textRecord = createTextRecord(nfc.TextToWrite(),true);
          msg = new NdefMessage(new NdefRecord[] { textRecord });
        }
        if (writeNFCTag(msg, detectedTag)) {
          nfc.TagWritten();       
        }
      }  
    } else {
      Log.e("nearfield", "Unknown intent " + intent);
    }
  }

  /*
  * Writes an NdefMessage to a NFC tag
  */
  public static boolean writeNFCTag(NdefMessage message, Tag tag) {
    int size = message.toByteArray().length;
    try {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            ndef.connect();
            if (!ndef.isWritable()) {
                return false;
            }
            if (ndef.getMaxSize() < size) {
                return false;
            }
            ndef.writeNdefMessage(message);
            return true;
        } else {
            NdefFormatable format = NdefFormatable.get(tag);
            if (format != null) {
                try {
                    format.connect();
                    format.format(message);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            } else {
                return false;
            }
        }
    } catch (Exception e) {
        return false;
    }
  }
}
