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

public class ListViewImageSingleTextAdapter extends ListAdapterWithRecyclerView {

  private int textMainColor;
  private float textMainSize;
  private String textMainFont;
  private int imageWidth;
  private int imageHeight;

  public ListViewImageSingleTextAdapter(ComponentContainer container, List<Object> data,
      int textMainColor, float textMainSize, String textMainFont, int textDetailColor,
      float textDetailSize, String textDetailFont, int backgroundColor, int selectionColor,
      int radius, int imageWidth, int imageHeight) {
    super(container, data, backgroundColor, selectionColor, radius);
    this.container = container;
    this.textMainColor = textMainColor;
    this.textMainSize = textMainSize;
    this.textMainFont = textMainFont;
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

    LinearLayout linearLayout1 = new LinearLayout(container.$context());
    LinearLayout.LayoutParams layoutParamslinear1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    linearLayout1.setLayoutParams(layoutParamslinear1);
    linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
    linearLayout1.setGravity(Gravity.CENTER_VERTICAL);

    linearLayout1.addView(imageView);
    linearLayout1.addView(textViewFirst);
    cardView.addView(linearLayout1);

    return new ImageSingleTextRvViewHolder(cardView, idCard, idFirst, idImage);
  }

  @Override
  public void onBindViewHolder(RvViewHolder holder, int position) {
    ImageSingleTextRvViewHolder imageSingleTextHolder = (ImageSingleTextRvViewHolder) holder;
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
    String imageName = "";
    if (dictItem.containsKey(Component.LISTVIEW_KEY_IMAGE)) {
      imageName = dictItem.get(Component.LISTVIEW_KEY_IMAGE).toString();
    }
    imageSingleTextHolder.textViewFirst.setText(first);
    try {
      Drawable drawable = MediaUtil.getBitmapDrawable(container.$form(), imageName);
      ViewUtil.setImage(imageSingleTextHolder.imageView, drawable);
    } catch (IOException ioe) {
      Log.e(LOG_TAG, "onBindViewHolder Unable to load image " + imageName + ": " + ioe.getMessage());
    }
    updateCardViewColor(imageSingleTextHolder.cardView, position);
  }
 
  public class ImageSingleTextRvViewHolder extends RvViewHolder {

    public TextView textViewFirst;
    public ImageView imageView;
    public CardView cardView;

    public ImageSingleTextRvViewHolder(View view, int idCard, int idFirst, int idImage) {
      super(view);
      cardView = (CardView) view.findViewById(idCard);
      textViewFirst = (TextView) view.findViewById(idFirst);
      imageView = (ImageView) view.findViewById(idImage);
    }  
  }
}
