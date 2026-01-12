// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import android.util.Log;

import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;
import com.google.appinventor.components.runtime.util.YailDictionary;

import java.io.IOException;
import java.util.List;

public class ListViewImageTwoTextVerticalAdapter extends ListAdapterWithRecyclerView {

  private int textMainColor;
  private float textMainSize;
  private String textMainFont;
  private int textDetailColor;
  private float textDetailSize;
  private String textDetailFont;
  private int imageWidth;
  private int imageHeight;

  public ListViewImageTwoTextVerticalAdapter(ComponentContainer container, List<Object> data,
      int textMainColor, float textMainSize, String textMainFont, int textDetailColor,
      float textDetailSize, String textDetailFont, int backgroundColor, int selectionColor,
      int radius, int imageWidth, int imageHeight) {
    super(container, data, backgroundColor, selectionColor, radius);
    this.container = container;
    this.textMainColor = textMainColor;
    this.textMainSize = textMainSize;
    this.textMainFont = textMainFont;
    this.textDetailColor = textDetailColor;
    this.textDetailSize = textDetailSize;
    this.textDetailFont = textDetailFont;    
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }  

  @Override
  public RvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    CardView cardView = createCardView(parent);
    final int idCard = cardView.getId();

    // ImageView
    ImageView imageView = new ImageView(container.$context());
    final int idImage = ViewCompat.generateViewId();
    imageView.setId(idImage);
    LinearLayout.LayoutParams layoutParamsImage =
            new LinearLayout.LayoutParams(imageWidth, imageHeight);
    layoutParamsImage.setMargins(0, 0, 15, 0);
    imageView.setLayoutParams(layoutParamsImage);

    // MainText
    TextView textViewFirst = new TextView(container.$context());
    final int idFirst = ViewCompat.generateViewId();
    textViewFirst.setId(idFirst);
    LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    textViewFirst.setLayoutParams(layoutParams1);
    textViewFirst.setTextSize(textMainSize);
    textViewFirst.setTextColor(textMainColor);
    TextViewUtil.setFontTypeface(container.$form(), textViewFirst, textMainFont, false, false);

    // DetailText
    TextView textViewSecond = new TextView(container.$context());
    final int idSecond = ViewCompat.generateViewId();
    textViewSecond.setId(idSecond);
    LinearLayout.LayoutParams layoutParams2 =
            new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    textViewSecond.setTextSize(textDetailSize);
    TextViewUtil.setFontTypeface(container.$form(), textViewSecond, textDetailFont, false, false);
    textViewSecond.setTextColor(textDetailColor);
    textViewSecond.setLayoutParams(layoutParams2);

    LinearLayout linearLayout2 = new LinearLayout(container.$context());
    LinearLayout.LayoutParams layoutParamslinear2 =
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
    linearLayout2.setLayoutParams(layoutParamslinear2);
    linearLayout2.setOrientation(LinearLayout.VERTICAL);

    linearLayout2.addView(textViewFirst);
    linearLayout2.addView(textViewSecond);

    LinearLayout linearLayout1 = new LinearLayout(container.$context());
    LinearLayout.LayoutParams layoutParamslinear1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    linearLayout1.setLayoutParams(layoutParamslinear1);
    linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
    linearLayout1.setGravity(Gravity.CENTER_VERTICAL);

    linearLayout1.addView(imageView);
    linearLayout1.addView(linearLayout2);
    cardView.addView(linearLayout1);

    return new ImageTwoTextRvViewHolder(cardView, idCard, idFirst, idSecond, idImage);
  }

  @Override
  public void onBindViewHolder(RvViewHolder holder, int position) {
    ImageTwoTextRvViewHolder imageTwoTextHolder = (ImageTwoTextRvViewHolder) holder;
    Object o = items.get(position);
    YailDictionary dictItem = new YailDictionary();
    if (o instanceof YailDictionary) {
      if (((YailDictionary) o).containsKey(Component.LISTVIEW_KEY_MAIN_TEXT)) {
        dictItem = (YailDictionary) o;
      } else {
        dictItem.put(Component.LISTVIEW_KEY_MAIN_TEXT, o.toString());
      }
    } else {
      dictItem.put(Component.LISTVIEW_KEY_MAIN_TEXT, o.toString());
    }
    String first = dictItem.get(Component.LISTVIEW_KEY_MAIN_TEXT).toString();
    String second = "";
    if (dictItem.containsKey(Component.LISTVIEW_KEY_DESCRIPTION)) {
      second = dictItem.get(Component.LISTVIEW_KEY_DESCRIPTION).toString();
    }
    String imageName = "";
    if (dictItem.containsKey(Component.LISTVIEW_KEY_IMAGE)) {
      imageName = dictItem.get(Component.LISTVIEW_KEY_IMAGE).toString();
    }
    imageTwoTextHolder.textViewFirst.setText(first);
    imageTwoTextHolder.textViewSecond.setText(second);
    try {
      Drawable drawable = MediaUtil.getBitmapDrawable(container.$form(), imageName);
      ViewUtil.setImage(imageTwoTextHolder.imageView, drawable);
    } catch (IOException ioe) {
      Log.e(LOG_TAG, "onBindViewHolder Unable to load image " + imageName + ": " + ioe.getMessage());
    }
    updateCardViewColor(imageTwoTextHolder.cardView, position);
  }
 
  public class ImageTwoTextRvViewHolder extends RvViewHolder {
    public TextView textViewFirst;
    public TextView textViewSecond;
    public ImageView imageView;
    public CardView cardView;

    public ImageTwoTextRvViewHolder(View view, int idCard, int idFirst, int idSecond, int idImage) {
        super(view);
        cardView = (CardView) view.findViewById(idCard);
        textViewFirst = (TextView) view.findViewById(idFirst);
        textViewSecond = (TextView) view.findViewById(idSecond);
        imageView = (ImageView) view.findViewById(idImage);
    }
  }
}
