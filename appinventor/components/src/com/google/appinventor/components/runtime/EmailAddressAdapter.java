// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.People;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * EmailAddressAdapter provides email address completion from contacts, 
 * for use as a list adapter.
 * 
 * <p>Note that most of this code was copied from 
 * partner/google/apps/Gmail/src/com/google/android/gm/EmailAddressAdapter.java
 * 
 * @author sharon@google.com (Sharon Perl)
 */

public class EmailAddressAdapter extends ResourceCursorAdapter {
  public static final int NAME_INDEX = 1;
  public static final int DATA_INDEX = 2;

  private static final String SORT_ORDER = People.TIMES_CONTACTED + " DESC, " + People.NAME;
  private ContentResolver contentResolver;

  private static final String[] PROJECTION = {
    ContactMethods._ID,     // 0
    ContactMethods.NAME,    // 1
    ContactMethods.DATA     // 2
  };

  public EmailAddressAdapter(Context context) {
    super(context, android.R.layout.simple_dropdown_item_1line, null);
    contentResolver = context.getContentResolver();
  }

  @Override
  public final String convertToString(Cursor cursor) {
    String name = cursor.getString(NAME_INDEX);
    String address = cursor.getString(DATA_INDEX);

    return new Rfc822Token(name, address, null).toString();
  }

  private final String makeDisplayString(Cursor cursor) {
    StringBuilder s = new StringBuilder();
    boolean flag = false;
    String name = cursor.getString(NAME_INDEX);
    String address = cursor.getString(DATA_INDEX);

    if (!TextUtils.isEmpty(name)) {
      s.append(name);
      flag = true;
    }

    if (flag) {
      s.append(" <");
    }

    s.append(address);

    if (flag) {
      s.append(">");
    }

    return s.toString();
  }

  @Override
  public final void bindView(View view, Context context, Cursor cursor) {
    ((TextView) view).setText(makeDisplayString(cursor));
  }

  @Override
  public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
    String where = null;

    if (constraint != null) {
      String filter = DatabaseUtils.sqlEscapeString(constraint.toString() + '%');

      StringBuilder s = new StringBuilder();
      s.append("(people.name LIKE ");
      s.append(filter);
      s.append(") OR (contact_methods.data LIKE ");
      s.append(filter);
      s.append(")");

      where = s.toString();
    }

    return contentResolver.query(ContactMethods.CONTENT_EMAIL_URI, PROJECTION, 
        where, null, SORT_ORDER);
  }
}

