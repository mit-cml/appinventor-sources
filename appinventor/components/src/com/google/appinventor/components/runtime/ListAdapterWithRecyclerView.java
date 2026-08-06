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

public abstract class ListAdapterWithRecyclerView
    extends RecyclerView.Adapter<ListAdapterWithRecyclerView.RvViewHolder> implements Filterable {
  protected static final String LOG_TAG = ListView.LOG_TAG;

  protected ClickListener clickListener;

  protected int backgroundColor;
  protected int selectionColor;
  protected float radius;
  // The model owns the data, the filter, and the selection; this adapter is only a view over it.
  protected final ListDataModel model;
  protected ComponentContainer container;

  protected final Filter filter = new Filter() {
    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
      // Background thread: compute the filtered view without touching state the UI thread reads.
      ListDataModel.FilterResult computed = model.computeFilter(charSequence.toString());
      FilterResults results = new FilterResults();
      results.values = computed;
      results.count = computed.size();
      return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
      // UI thread: apply the finished result and refresh, back to back, so the row count and the
      // data never disagree.
      model.commitFilter((ListDataModel.FilterResult) filterResults.values);
      notifyDataSetChanged();
    }
  };

  public ListAdapterWithRecyclerView(ComponentContainer container, ListDataModel model,
      int backgroundColor, int selectionColor, float radius) {
    this.container = container;
    this.model = model;
    this.backgroundColor = backgroundColor;
    this.radius = radius;
    this.selectionColor = selectionColor;
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

  /**
   * Refreshes the row showing the given original item index, if it is currently visible.
   */
  private void notifyOriginalChanged(int originalPosition) {
    int displayPosition = model.toDisplayPosition(originalPosition);
    if (displayPosition >= 0) {
      notifyItemChanged(displayPosition);
    }
  }

  protected void updateCardViewColor(CardView cardView, int position) {
    // position is a display row; map it back to the original index the selection is keyed by.
    if (model.isSelected(model.toOriginalPosition(position))) {
      cardView.setCardBackgroundColor(selectionColor);
    } else {
      cardView.setCardBackgroundColor(backgroundColor);
    }
  }

  @Override
  public int getItemCount() {
    return model.visibleSize();
  }

  /**
   * Selects the item at the given original index, replacing any previous selection.
   */
  public void toggleSelection(int position) {
    if (model.isSelected(position)) {
      return;
    }
    int oldPosition = model.firstSelection();
    model.selectSingle(position);
    if (oldPosition >= 0) {
      notifyOriginalChanged(oldPosition);
    }
    notifyOriginalChanged(position);
  }

  /**
   * Toggles the item at the given original index, used when MultiSelect is enabled.
   */
  public void changeSelections(int position) {
    model.toggleSelection(position);
    notifyOriginalChanged(position);
  }

  public void clearSelections() {
    model.clearSelection();
  }

  abstract class RvViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public RvViewHolder(View view) {
      super(view);
      view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      clickListener.onItemClick(model.toOriginalPosition(getAdapterPosition()), v);
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
