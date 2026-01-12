// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.appinventor.components.runtime.ComponentContainer;

import java.io.IOException;
import java.util.List;

/**
 * Adapter class to populate ListView with an image and single line of text
 */
public class ListViewArrayAdapterImageSingleText {
  private int textSize, textColor, imageWidth, imageHeight;
  private ComponentContainer container;
  private List<YailDictionary> currentItems;
  private List<YailDictionary> filterCurrentItems;

  private ArrayAdapter<YailDictionary> itemAdapter;
  private final Filter filter;

  public ListViewArrayAdapterImageSingleText(int textSize, int textColor, int imageWidth, int imageHeight,
      ComponentContainer container, List<YailDictionary> items) {
    this.textSize = textSize;
    this.textColor = textColor;
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
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
        for (YailDictionary item : filterCurrentItems) {
          itemAdapter.add(item);
        }
      }
    };
  }

  /**
   * method to create view for each row of ListView
   *
   * @return parent container containing imageView and textView
   */
  private View createView() {
    LinearLayout linearLayout = new LinearLayout(container.$context());
    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
    linearLayout.setPadding(15,15,15,15);
    ImageView imageView = new ImageView(container.$context());
    imageView.setId(1);
    imageView.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageHeight));
    TextView textView = new TextView(container.$context());
    textView.setPadding(15,15,15, 15);
    textView.setId(2);
    linearLayout.addView(imageView);
    linearLayout.addView(textView);
    return linearLayout;
  }

  public void setImage(ImageView imageView, String imagePath) {
    Drawable drawable;
    drawable = null;
    if (imagePath != "") {
      try {
        drawable = MediaUtil.getBitmapDrawable(container.$form(), imagePath);
      } catch (IOException ioe) {
        Log.e("Image", "Unable to load " + imagePath);
      }
    }
    ViewUtil.setImage(imageView, drawable);
  }

  public ArrayAdapter<YailDictionary> createAdapter() {
    itemAdapter = new ArrayAdapter<YailDictionary>(container.$context(),0, currentItems) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View view = createView();

        ImageView image = view.findViewById(1);
        TextView text1 = view.findViewById(2);

        YailDictionary row = filterCurrentItems.get(position);
        String imageVal = ElementsUtil.toStringEmptyIfNull(row.get("Image"));
        String val1 = ElementsUtil.toStringEmptyIfNull(row.get("Text1"));

        setImage(image, imageVal);
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
