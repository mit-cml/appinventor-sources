// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import androidx.core.view.ViewCompat;

import androidx.recyclerview.widget.RecyclerView;

import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;
import com.google.appinventor.components.runtime.util.YailDictionary;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class ListAdapterWithRecyclerView
    extends RecyclerView.Adapter<ListAdapterWithRecyclerView.RvViewHolder> implements Filterable {
  private static final String LOG_TAG = "ListAdapterRecyclerView";

  private ClickListener clickListener;

  private int textMainColor;
  private float textMainSize;
  private int textDetailColor;
  private float textDetailSize;
  private String textMainFont;
  private String textDetailFont;
  private int layoutType;
  private int backgroundColor;
  private int selectionColor;
  private int imageHeight;
  private int imageWidth;
  private float radius;
  private List<Object> items = new ArrayList<>();
  private List<Object> originalItems = new ArrayList<>();
  private List<Integer> originalPositions = new ArrayList<>();
  protected final ComponentContainer container;
  private List<Integer> selectedItems = new ArrayList<>();
  private String lastQuery = "";

  protected final Filter filter = new Filter() {
    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
      lastQuery = charSequence.toString().toLowerCase();
      FilterResults results = new FilterResults();
      List<Object> filteredList = new ArrayList<>();
      originalPositions = new ArrayList<>();
      if (lastQuery == null || lastQuery.length() == 0) {
        filteredList = new ArrayList<>(originalItems);
        items = new ArrayList<>(originalItems);
      } else {
        for (int index = 0; index < originalItems.size(); index++) {
          Object item = originalItems.get(index);
          String filterString;
          if (item instanceof YailDictionary) {
            if (((YailDictionary) item).containsKey(Component.LISTVIEW_KEY_MAIN_TEXT)) {
              Object o = ((YailDictionary) item).get(Component.LISTVIEW_KEY_DESCRIPTION);
              filterString = ((YailDictionary) item).get(Component.LISTVIEW_KEY_MAIN_TEXT).toString();
              if (o != null) {
                filterString += " " + o.toString();
              }
            } else {
              filterString = item.toString();
            }
          } else {
            filterString = item.toString();
          }
          if (filterString.toLowerCase().contains(lastQuery)) {
            filteredList.add(item);
            originalPositions.add(index);
          }
        }
      }
      results.count = filteredList.size();
      results.values = filteredList;
      return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
      items = new ArrayList<>((List<Object>) filterResults.values);
      clearSelections();
      notifyDataSetChanged();
      // We store the original data in the originalItems variable
      // We store the original item indexes in the originalPositions variable
      // We have eliminated hiding/showing CardView to improve performance
    }
  };

  public ListAdapterWithRecyclerView(ComponentContainer container, List<Object> data,
      int layoutType, int textMainColor, int textDetailColor, float textMainSize,
      float textDetailSize, String textMainFont, String textDetailFont, int backgroundColor,
      int selectionColor, int imageWidth, int imageHeight, int radius) {
    this.container = container;
    this.layoutType = layoutType;
    this.textMainColor = textMainColor;
    this.textDetailColor = textDetailColor;
    this.textMainSize = textMainSize;
    this.textDetailSize = textDetailSize;
    this.textMainFont = textMainFont;
    this.textDetailFont = textDetailFont;
    this.backgroundColor = backgroundColor;
    this.selectionColor = selectionColor;
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    this.radius = (float) radius;
    updateData(data);
  }

  public void updateData(List<Object> newItems) {
    this.originalItems = newItems;
    if (originalPositions.isEmpty()) {
      this.items = new ArrayList<>(newItems);
    } else {
      filter.filter(lastQuery);
    }
    clearSelections();
  }

  @Override
  public RvViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
    CardView cardView = new CardView(container.$context());
    cardView.setContentPadding(15, 15, 15, 15);
    cardView.setPreventCornerOverlap(false);
    cardView.setMaxCardElevation(3f);
    cardView.setCardBackgroundColor(backgroundColor);
    cardView.setRadius(radius);
    cardView.setCardElevation(0.0f);
    ViewCompat.setElevation(cardView, 0);

    cardView.setClickable(true);
    final int idCard = ViewCompat.generateViewId();
    cardView.setId(idCard);

    CardView.LayoutParams params1 = new CardView.LayoutParams(CardView.LayoutParams.FILL_PARENT, CardView.LayoutParams.WRAP_CONTENT);
    params1.setMargins(0, 0, 0, 0);

    // All layouts have a textview containing MainText
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

    final int idImages;
    if (layoutType == Component.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT || layoutType == Component.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
      // Create ImageView for layouts containing an image
      ImageView imageView = new ImageView(container.$context());
      idImages = ViewCompat.generateViewId();
      imageView.setId(idImages);
      LinearLayout.LayoutParams layoutParamsImage = new LinearLayout.LayoutParams(imageWidth, imageHeight);
      layoutParamsImage.setMargins(0, 0, 15, 0);
      imageView.setLayoutParams(layoutParamsImage);
      linearLayout1.setGravity(Gravity.CENTER_VERTICAL);
      linearLayout1.addView(imageView);
    } else {
      idImages = -1;
    }

    final int idSecond;
    if (layoutType == Component.LISTVIEW_LAYOUT_SINGLE_TEXT || layoutType == Component.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
      // All layouts containing just MainText
      linearLayout1.addView(textViewFirst);
      idSecond = -1;
    } else {
      // All layouts containing MainText and DetailText
      TextView textViewSecond = new TextView(container.$context());
      idSecond = ViewCompat.generateViewId();
      textViewSecond.setId(idSecond);
      LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      textViewSecond.setTextSize(textDetailSize);
      TextViewUtil.setFontTypeface(container.$form(), textViewSecond, textDetailFont, false, false);
      textViewSecond.setTextColor(textDetailColor);
      if (layoutType == Component.LISTVIEW_LAYOUT_TWO_TEXT || layoutType == Component.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT) {
        textViewSecond.setLayoutParams(layoutParams2);

        LinearLayout linearLayout2 = new LinearLayout(container.$context());
        LinearLayout.LayoutParams layoutParamslinear2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        linearLayout2.setLayoutParams(layoutParamslinear2);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);

        linearLayout2.addView(textViewFirst);
        linearLayout2.addView(textViewSecond);
        linearLayout1.addView(linearLayout2);

      } else if (layoutType == Component.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR) {
        // Unlike the other two text layouts, linear does not wrap
        layoutParams2.setMargins(50, 0, 0, 0);
        textViewSecond.setLayoutParams(layoutParams2);
        textViewSecond.setMaxLines(1);
        textViewSecond.setEllipsize(null);

        linearLayout1.addView(textViewFirst);
        linearLayout1.addView(textViewSecond);
      }
    }
    cardView.setLayoutParams(params1);
    cardView.addView(linearLayout1);

    return new RvViewHolder(cardView, idCard, idFirst, idSecond, idImages);
  }

  @Override
  public void onBindViewHolder(final RvViewHolder holder, int position) {
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
    if (layoutType == Component.LISTVIEW_LAYOUT_SINGLE_TEXT) {
      holder.textViewFirst.setText(first);
    } else if (layoutType == Component.LISTVIEW_LAYOUT_TWO_TEXT) {
      holder.textViewFirst.setText(first);
      holder.textViewSecond.setText(second);
    } else if (layoutType == Component.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR) {
      holder.textViewFirst.setText(first);
      holder.textViewSecond.setText(second);
    } else if (layoutType == Component.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
      Drawable drawable = new BitmapDrawable();
      try {
        drawable = MediaUtil.getBitmapDrawable(container.$form(), imageName);
      } catch (IOException ioe) {
        Log.e(LOG_TAG, "onBindViewHolder Unable to load image " + imageName + ": " + ioe.getMessage());
      }
      holder.textViewFirst.setText(first);
      ViewUtil.setImage(holder.imageVieww, drawable);
    } else if (layoutType == Component.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT) {
      Drawable drawable = new BitmapDrawable();
      try {
        drawable = MediaUtil.getBitmapDrawable(container.$form(), imageName);
      } catch (IOException ioe) {
        Log.e(LOG_TAG, "onBindViewHolder Unable to load image " + imageName + ": " + ioe.getMessage());
      }
      holder.textViewFirst.setText(first);
      holder.textViewSecond.setText(second);
      ViewUtil.setImage(holder.imageVieww, drawable);
    } else {
      Log.e(LOG_TAG, "onBindViewHolder Layout not recognized: " + layoutType);
    }
    if (selectedItems.contains(position)) {
        holder.cardView.setCardBackgroundColor(selectionColor);
    } else {
        holder.cardView.setCardBackgroundColor(backgroundColor);
    }
  }

  @Override
  public int getItemCount() {
    return (items.size());
  }

  public void toggleSelection(int position) {
    if(!originalPositions.isEmpty()) {
      position = originalPositions.indexOf(position);
    }
    if (selectedItems.contains(position)) {
      return;
    }
    if (!selectedItems.isEmpty()) {
      int oldPosition = selectedItems.get(0);
      selectedItems.clear();
      notifyItemChanged(oldPosition);
    }
    selectedItems.add(position);
    notifyItemChanged(position);
  }

  public void changeSelections(int position) {
    if(!originalPositions.isEmpty()) {
      position = originalPositions.indexOf(position);
    }
    if (selectedItems.contains(position)) {
      selectedItems.remove(Integer.valueOf(position));
    } else {
      selectedItems.add(position);
    }
    notifyItemChanged(position);
  }

  public void clearSelections() {
    selectedItems.clear();
  }

  class RvViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView textViewFirst;
    public TextView textViewSecond;
    public ImageView imageVieww;
    public CardView cardView;

    public RvViewHolder(View view, int idCard, int idFirst, int idSecond, int idImages) {
      super(view);

      view.setOnClickListener(this);

      cardView = view.findViewById(idCard);
      textViewFirst = view.findViewById(idFirst);

      if (idSecond != -1) {
        textViewSecond = view.findViewById(idSecond);
      }
      if (idImages != -1) {
        imageVieww = view.findViewById(idImages);
      }
    }
    @Override
    public void onClick(View v) {
      int position = getAdapterPosition();

      if (!originalPositions.isEmpty()) {
        position = originalPositions.get(position);
      }
      clickListener.onItemClick(position, v);
    }
  }

  public void setOnItemClickListener(ClickListener clickListener) {
    this.clickListener = clickListener;
  }

  public interface ClickListener {
    void onItemClick(int position, View v);
  }

  @Override
  public Filter getFilter() {
    return filter;
  }
}
