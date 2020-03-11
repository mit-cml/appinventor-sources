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
  private List<JSONObject> currentItems;
  private List<JSONObject> filterCurrentItems;

  private ArrayAdapter<JSONObject> itemAdapter;
  private final Filter filter;

  public ListViewArrayAdapterTwoText(int textSize, int detailTextSize, int textColor, int detailTextColor,
      ComponentContainer container, List<JSONObject> items) {
    this.textSize = textSize;
    this.detailTextSize = detailTextSize;
    this.textColor = textColor;
    this.detailTextColor = detailTextColor;
    this.container = container;
    this.currentItems = new ArrayList<>(items);
    this.filterCurrentItems = new ArrayList<>(items);

    filter = new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {
        String filterQuery = charSequence.toString().toLowerCase();
        FilterResults results = new FilterResults();

        if (filterQuery == null || filterQuery.length() == 0) {
          List<JSONObject> arrayList = new ArrayList<>(currentItems);
          results.count = arrayList.size();
          results.values = arrayList;
        } else {
          List<JSONObject> arrayList = new ArrayList<>(currentItems);
          List<JSONObject> filteredList = new ArrayList<>();
          for (int i = 0; i < arrayList.size(); ++i) {
            JSONObject object = arrayList.get(i);
            if ((object.has("Text1") && object.getString("Text1").toLowerCase()
                .contains(charSequence.toString())) || (object.has("Text2")) && object.getString("Text2")
                .toLowerCase().contains(charSequence.toString())) {
              filteredList.add(object);
            }
          }

          results.count = filteredList.size();
          results.values = filteredList;
        }
        return results;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        filterCurrentItems = (List<JSONObject>) filterResults.values;
        itemAdapter.clear();
        for (int i = 0; i < filterCurrentItems.size(); ++i) {
          itemAdapter.add(filterCurrentItems.get(i));
        }
      }
    };
  }

  public ArrayAdapter<JSONObject> createAdapter() {
    itemAdapter = new ArrayAdapter<JSONObject>(container.$context(),
        android.R.layout.simple_list_item_2, android.R.id.text1, currentItems) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView text1 = view.findViewById(android.R.id.text1);
        TextView text2 = view.findViewById(android.R.id.text2);

        JSONObject row = filterCurrentItems.get(position);
        String val1 = row.has("Text1") ? row.getString("Text1") : "";
        String val2 = row.has("Text2") ? row.getString("Text2") : "";

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
