// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 - 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.View;
import android.view.ViewGroup;

import android.widget.Filter;
import android.widget.Filterable;

import androidx.cardview.widget.CardView;

import androidx.core.view.ViewCompat;

import androidx.recyclerview.widget.RecyclerView;

import com.google.appinventor.components.runtime.util.YailDictionary;

import java.util.ArrayList;
import java.util.List;

public abstract class ListAdapterWithRecyclerView
    extends RecyclerView.Adapter<ListAdapterWithRecyclerView.RvViewHolder> implements Filterable {
  protected static final String LOG_TAG = ListView.LOG_TAG;
  
  protected ClickListener clickListener;

  protected int backgroundColor;
  protected int selectionColor;
  protected float radius;
  protected List<Object> items = new ArrayList<>();
  protected List<Object> originalItems = new ArrayList<>();
  protected List<Integer> originalPositions = new ArrayList<>();
  protected ComponentContainer container;
  protected List<Integer> selectedItems = new ArrayList<>();
  protected String lastQuery = "";

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
      int backgroundColor, int selectionColor, float radius) {
    this.container = container;
    this.backgroundColor = backgroundColor;
    this.radius = radius;
    this.selectionColor = selectionColor;
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

  protected CardView createCardView(ViewGroup parent) {
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

    CardView.LayoutParams params1 = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
    params1.setMargins(0, 0, 0, 0);
    cardView.setLayoutParams(params1);
    return cardView;
  }

  protected void updateCardViewColor(CardView cardView, int position) {
    if (selectedItems.contains(position)) {
      cardView.setCardBackgroundColor(selectionColor);
    } else {
      cardView.setCardBackgroundColor(backgroundColor);
    }
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  public void toggleSelection(int position) {
    if (!originalPositions.isEmpty()) {
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
    if (!originalPositions.isEmpty()) {
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

  abstract class RvViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public RvViewHolder(View view) {
      super(view);
      view.setOnClickListener(this);
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
