// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 - 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.util.LruCache;

import android.view.View;
import android.view.ViewGroup;

import android.widget.Filter;
import android.widget.Filterable;

import androidx.cardview.widget.CardView;

import androidx.core.view.ViewCompat;

import androidx.recyclerview.widget.RecyclerView;

import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.YailDictionary;

import java.io.IOException;

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

  // Cache of decoded image drawables keyed by image name. Lazily created the first time an image is
  // loaded, so text-only list layouts never allocate it. Without it, onBindViewHolder re-decodes
  // (and re-scales) each image from disk on the UI thread every time a row scrolls into view, which
  // makes image lists scroll jerkily. Sized to a fraction of the app's available memory.
  private LruCache<String, Drawable> imageCache;

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
      // Keep the selection while the selected item is still on screen, and drop it only when the
      // filter hides it, so the user never ends up with a selection they cannot see. Selection is
      // stored against original item indexes, so it survives filtering otherwise.
      if (!lastQuery.isEmpty()) {
        selectedItems.retainAll(originalPositions);
      }
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

  /**
   * Returns the drawable for the given image name, decoding it via MediaUtil on first use and
   * caching the result. Subsequent binds while scrolling reuse the cached drawable instead of
   * re-decoding the image from disk on the UI thread, which is what made image lists scroll
   * jerkily.
   *
   * @param imageName the asset/image name from the list item, or empty when there is no image
   * @return the cached or freshly decoded drawable, or null when imageName is empty
   * @throws IOException if the image cannot be loaded
   */
  protected Drawable getImageDrawable(String imageName) throws IOException {
    if (imageName == null || imageName.isEmpty()) {
      return null;
    }
    if (imageCache == null) {
      // Use up to 1/8 of the available memory for the decoded-image cache.
      final int cacheSizeKb = (int) (Runtime.getRuntime().maxMemory() / 1024 / 8);
      imageCache = new LruCache<String, Drawable>(cacheSizeKb) {
        @Override
        protected int sizeOf(String key, Drawable drawable) {
          if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
              return bitmap.getByteCount() / 1024;
            }
          }
          return 1;
        }
      };
    }
    Drawable cached = imageCache.get(imageName);
    if (cached != null) {
      return cached;
    }
    Drawable drawable = MediaUtil.getBitmapDrawable(container.$form(), imageName);
    if (drawable != null) {
      imageCache.put(imageName, drawable);
    }
    return drawable;
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

  /**
   * Returns the display row showing the given original item index, or -1 when that item is
   * currently filtered out.
   */
  protected int toDisplayPosition(int originalPosition) {
    return lastQuery.isEmpty() ? originalPosition : originalPositions.indexOf(originalPosition);
  }

  /**
   * Returns the original item index behind the given display row.
   */
  protected int toOriginalPosition(int displayPosition) {
    return lastQuery.isEmpty() ? displayPosition : originalPositions.get(displayPosition);
  }

  /**
   * Returns whether the item at the given original index is currently shown, that is, whether it
   * survives the active filter. With no filter every item is shown.
   */
  public boolean isVisible(int originalPosition) {
    return lastQuery.isEmpty() || originalPositions.contains(originalPosition);
  }

  /**
   * Refreshes the row showing the given original item index, if it is currently visible.
   */
  private void notifyOriginalChanged(int originalPosition) {
    int displayPosition = toDisplayPosition(originalPosition);
    if (displayPosition >= 0) {
      notifyItemChanged(displayPosition);
    }
  }

  protected void updateCardViewColor(CardView cardView, int position) {
    // Selection is stored against original item indexes, so map the display row first.
    if (selectedItems.contains(toOriginalPosition(position))) {
      cardView.setCardBackgroundColor(selectionColor);
    } else {
      cardView.setCardBackgroundColor(backgroundColor);
    }
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  /**
   * Selects the item at the given original index, replacing any previous selection.
   */
  public void toggleSelection(int position) {
    if (selectedItems.contains(position)) {
      return;
    }
    if (!selectedItems.isEmpty()) {
      int oldPosition = selectedItems.get(0);
      selectedItems.clear();
      notifyOriginalChanged(oldPosition);
    }
    selectedItems.add(position);
    notifyOriginalChanged(position);
  }

  /**
   * Toggles the item at the given original index, used when MultiSelect is enabled.
   */
  public void changeSelections(int position) {
    if (selectedItems.contains(position)) {
      selectedItems.remove(Integer.valueOf(position));
    } else {
      selectedItems.add(position);
    }
    notifyOriginalChanged(position);
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
      clickListener.onItemClick(toOriginalPosition(getAdapterPosition()), v);
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
