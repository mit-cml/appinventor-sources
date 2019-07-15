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

public class ArrayAdapterSingleText {

  private int textSize, textColor;
  private ComponentContainer container;
  private ArrayList<JSONObject> currentItems;
  private ArrayList<JSONObject> filterCurrentItems;

  public ArrayAdapterSingleText (int textSize, int textColor, ComponentContainer container,
      ArrayList<JSONObject> currentItems) {
    this.textSize = textSize;
    this.textColor = textColor;
    this.container = container;
    this.currentItems = new ArrayList<>(currentItems);
    this.filterCurrentItems = new ArrayList<>(currentItems);
  }

  public ArrayAdapter<JSONObject> createAdapter() {
    ArrayAdapter<JSONObject> itemAdapter = new ArrayAdapter<JSONObject>(container.$context(),
        android.R.layout.simple_list_item_1, currentItems) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView text1 = view.findViewById(android.R.id.text1);

        JSONObject row = filterCurrentItems.get(position);
        String val1 = row.has("Text1") ? row.getString("Text1") : "";

        text1.setText(val1);
        text1.setTextColor(textColor);
        text1.setTextSize(textSize);

        return view;
      }

      @Override
      public Filter getFilter() {
        Filter filter = new Filter() {
          @Override
          protected FilterResults performFiltering(CharSequence charSequence) {
            String filterQuery = charSequence.toString().toLowerCase();
            FilterResults results = new FilterResults();

            if(filterQuery == null || filterQuery.length() == 0) {
              ArrayList<JSONObject> arrayList = new ArrayList<>(currentItems);
              results.count = arrayList.size();
              results.values = arrayList;
            } else {
              ArrayList<JSONObject> arrayList = new ArrayList<>(currentItems);
              ArrayList<JSONObject> filteredList = new ArrayList<>();
              for(int i = 0; i < arrayList.size(); ++i) {
                JSONObject object = arrayList.get(i);
                if (object.has("Text1") &&
                    object.getString("Text1").toLowerCase().contains(filterQuery)) {
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
            filterCurrentItems = (ArrayList<JSONObject>) filterResults.values;
            clear();
            for(int i = 0; i < filterCurrentItems.size(); ++i) {
              add(filterCurrentItems.get(i));
            }
          }
        };
        return filter;
      }
    };
    return itemAdapter;
  }
}
