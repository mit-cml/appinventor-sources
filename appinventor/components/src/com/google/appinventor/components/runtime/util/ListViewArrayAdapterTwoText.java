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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class to populate ListView with two lines of text
 */
public class ListViewArrayAdapterTwoText {

  private int textSize, detailTextSize, textColor, detailTextColor;
  private ComponentContainer container;
  private List<YailDictionary> currentItems;
  private List<YailDictionary> filterCurrentItems;

  private ArrayAdapter<YailDictionary> itemAdapter;
  private final Filter filter;

  public ListViewArrayAdapterTwoText(int textSize, int detailTextSize, int textColor, int detailTextColor,
      ComponentContainer container, List<YailDictionary> items) {
    this.textSize = textSize;
    this.detailTextSize = detailTextSize;
    this.textColor = textColor;
    this.detailTextColor = detailTextColor;
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
          filterCurrentItems.clear();
          for (YailDictionary item : currentItems) {
            if (item.get("Text1").toString().concat(ElementsUtil.toStringEmptyIfNull(item.get("Text2"))).contains(charSequence)) {
              filterCurrentItems.add(item);
            }
          }
          results.count = filterCurrentItems.size();
          results.values = filterCurrentItems;
        }
        return results;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        filterCurrentItems.clear();
        itemAdapter.clear();
        for (YailDictionary item : filterCurrentItems) {
          itemAdapter.add(item);
        }
      }
    };
  }

  public ArrayAdapter<YailDictionary> createAdapter() {
    itemAdapter = new ArrayAdapter<YailDictionary>(container.$context(),
        android.R.layout.simple_list_item_2, android.R.id.text1, currentItems) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView text1 = view.findViewById(android.R.id.text1);
        TextView text2 = view.findViewById(android.R.id.text2);

        YailDictionary row = filterCurrentItems.get(position);
        String val1 = ElementsUtil.toStringEmptyIfNull(row.get("Text1"));
        String val2 = ElementsUtil.toStringEmptyIfNull(row.get("Text2"));

        text1.setText(val1);
        text2.setText(val2);

        text1.setTextColor(textColor);
        text2.setTextColor(detailTextColor);

        text1.setTextSize(textSize);
        text2.setTextSize(detailTextSize);

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
