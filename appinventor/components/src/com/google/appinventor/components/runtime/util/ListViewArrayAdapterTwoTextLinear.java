// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.appinventor.components.runtime.ComponentContainer;

import java.util.List;

/**
 * Adapter class to populate ListView with two lines of text in a single main line
 */
public class ListViewArrayAdapterTwoTextLinear {

  private int textSize, detailTextSize, textColor, detailTextColor;
  private static ComponentContainer container;
  private List<YailDictionary> currentItems;
  private List<YailDictionary> filterCurrentItems;

  private ArrayAdapter<YailDictionary> itemAdapter;
  private final Filter filter;

  public ListViewArrayAdapterTwoTextLinear(int textSize, int detailTextSize, int textColor, int detailTextColor,
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
        filterCurrentItems = (List<YailDictionary>) filterResults.values;
        itemAdapter.clear();
        for (Object o : filterCurrentItems) {
          itemAdapter.add((YailDictionary) o);
        }
      }
    };
  }

  /**
   * method to create view for each row of ListView
   *
   * @return parent container containing two textViews
   */
  private View createView(){
    LinearLayout linearLayout = new LinearLayout(container.$context());
    linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
    linearLayout.setId(1);
    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
    linearLayout.setPadding(15,15,15,15);
    TextView textView1 = new TextView(container.$context());
    textView1.setPadding(10,10,10, 10);
    textView1.setGravity(Gravity.LEFT);
    textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    textView1.setId(2);
    TextView textView2 = new TextView(container.$context());
    textView2.setPadding(10,10,10, 10);
    textView2.setGravity(Gravity.RIGHT);
    textView2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    textView2.setId(3);
    linearLayout.addView(textView1);
    linearLayout.addView(textView2);
    return linearLayout;
  }

  public ArrayAdapter<YailDictionary> createAdapter() {
    itemAdapter = new ArrayAdapter<YailDictionary>(container.$context(), 0, currentItems) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View view = createView();

        TextView text1 = view.findViewById(2);
        TextView text2 = view.findViewById(3);

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
