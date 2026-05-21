// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import com.google.appinventor.components.runtime.ComponentContainer;

import java.util.List;

/**
 * Adapter class to populate ListView with single line of text
 */
public class ListViewArrayAdapterSingleText {

  private int textSize, textColor;
  private ComponentContainer container;
  private List<YailDictionary> filterCurrentItems;
  private List<YailDictionary> currentItems;

  private ArrayAdapter<YailDictionary> itemAdapter;
  private final Filter filter;

  public ListViewArrayAdapterSingleText(int textSize, int textColor, ComponentContainer container,
      List<YailDictionary> items) {
    this.textSize = textSize;
    this.textColor = textColor;
    this.container = container;
    this.currentItems = items;
    this.filterCurrentItems = items;

    filter = new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {
        String filterQuery = charSequence.toString().toLowerCase();
        FilterResults results = new FilterResults();

        if (filterQuery == null || filterQuery.length() == 0) {
          results.count = currentItems.size();
          results.values = currentItems;
        } else {
          for (YailDictionary item : currentItems) {
            filterCurrentItems.clear();
            if (item.get("Text1").toString().contains(filterQuery)) {
              filterCurrentItems.add(item);
            };
          }
          results.count = filterCurrentItems.size();
          results.values = filterCurrentItems;
        }
        return results;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        filterCurrentItems = (List<YailDictionary>) filterResults.values;
        itemAdapter.clear();
        if (filterCurrentItems != null) {
          for (int i = 0; i < filterCurrentItems.size(); ++i) {
            itemAdapter.add(filterCurrentItems.get(i));
          }
        }
      }
    };
  }

  public ArrayAdapter<YailDictionary> createAdapter() {
    itemAdapter = new ArrayAdapter<YailDictionary>(container.$context(),
        android.R.layout.simple_list_item_1, currentItems) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView text1 = view.findViewById(android.R.id.text1);

        YailDictionary row = filterCurrentItems.get(position);
        String val1 = ElementsUtil.toStringEmptyIfNull(row.get("Text1"));

        text1.setText(val1);
        text1.setTextColor(textColor);
        text1.setTextSize(textSize);

        return view;
      }

      /*
       * overriding getFilter() method to implement search functionality for the custom layout of ListView
       */
      @Override
      public Filter getFilter() {
        return filter;
      }
    };
    return itemAdapter;
  }
}
