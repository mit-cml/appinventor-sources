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
 * Adapter class to populate ListView with an image and two lines of text
 */
public class ListViewArrayAdapterImageTwoText {
  private int textSize, detailTextSize, textColor, detailTextColor, imageWidth, imageHeight;
  private ComponentContainer container;
  private List<YailDictionary> currentItems;
  private List<YailDictionary> filterCurrentItems;

  private ArrayAdapter<YailDictionary> itemAdapter;
  private final Filter filter;

  public ListViewArrayAdapterImageTwoText(int textSize, int detailTextSize, int textColor, int detailTextColor,
      int imageWidth, int imageHeight, ComponentContainer container, List<YailDictionary> items) {
    this.textSize = textSize;
    this.detailTextSize = detailTextSize;
    this.textColor = textColor;
    this.detailTextColor = detailTextColor;
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
   * @return parent container containing imageView and two textViews
   */
  private View createView() {
    LinearLayout linearLayout = new LinearLayout(container.$context());
    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
    linearLayout.setPadding(15,15,15,15);
    ImageView imageView = new ImageView(container.$context());
    imageView.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageHeight));
    imageView.setId(1);
    LinearLayout textLayout = new LinearLayout(container.$context());
    textLayout.setOrientation(LinearLayout.VERTICAL);
    textLayout.setPadding(10,10,10,10);
    TextView textView1 = new TextView(container.$context());
    textView1.setPadding(10,10,10, 10);
    textView1.setId(2);
    TextView textView2 = new TextView(container.$context());
    textView2.setPadding(10,10,10, 10);
    textView2.setId(3);
    textLayout.addView(textView1);
    textLayout.addView(textView2);
    linearLayout.addView(imageView);
    linearLayout.addView(textLayout);
    return linearLayout;
  }

  public void setImage(ImageView imageView, String imagePath) {
    Drawable drawable = null;
    if (imagePath != null) {
      try {
        drawable = MediaUtil.getBitmapDrawable(container.$form(), imagePath);
      } catch (IOException ioe) {
        Log.e("Image", "Unable to load " + imagePath);
      }
      ViewUtil.setImage(imageView, drawable);
    }
  }

  public ArrayAdapter<YailDictionary> createAdapter() {
    itemAdapter = new ArrayAdapter<YailDictionary>(container.$context(),
        android.R.layout.simple_list_item_2, android.R.id.text1, currentItems) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View view = createView();

        ImageView image = view.findViewById(1);
        TextView text1 = view.findViewById(2);
        TextView text2 = view.findViewById(3);

        YailDictionary row = filterCurrentItems.get(position);
        String imageVal = row.get("Image").toString();
        String val1 = row.get("Text1").toString();
        String val2 = ElementsUtil.toStringEmptyIfNull(row.get("Text2"));

        setImage(image, imageVal);
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
