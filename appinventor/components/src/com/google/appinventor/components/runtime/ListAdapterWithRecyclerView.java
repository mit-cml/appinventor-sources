package com.google.appinventor.components.runtime;

import android.graphics.drawable.BitmapDrawable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.drawable.Drawable;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import android.util.Log;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;


@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
        "android.permission.READ_EXTERNAL_STORAGE")
 public class ListAdapterWithRecyclerView extends RecyclerView.Adapter<ListAdapterWithRecyclerView.RvViewHolder> implements Filterable {
  private static final String LOG_TAG = "ListAdapterRecyclerView";

  private static ClickListener clickListener;

  public Boolean[] selection;
  public Boolean[] isVisible;
  private int textMainColor;
  private float textMainSize;
  private int textDetailColor;
  private float textDetailSize;
  private int textMainFont;
  private int textDetailFont;

  private int layoutType;
  private int backgroundColor;
  private int selectionColor;
  private int imageHeight;
  private int imageWidth;
  private CardView[] itemViews;
  private boolean multiSelect;
  private List<YailDictionary> items;
  private List<YailDictionary> filterItems;
  protected final ComponentContainer container;

  public boolean isSelected = false;

  private int idFirst = -1, idSecond = -1, idImages = -1, idCard = 1;
  public ListAdapterWithRecyclerView(ComponentContainer container, List<YailDictionary> items, int textMainColor, int textDetailColor, float textMainSize, float textDetailSize, int textMainFont, int textDetailFont, int layoutType, int backgroundColor, int selectionColor, int imageWidth, int imageHeight, boolean multiSelect) {
    this.items = items;
    this.container = container;
    this.textMainSize = textMainSize;
    this.textMainColor = textMainColor;
    this.textDetailColor = textDetailColor;
    this.textDetailSize = textDetailSize;
    this.textMainFont = textMainFont;
    this.textDetailFont = textDetailFont;
    this.layoutType = layoutType;
    this.backgroundColor = backgroundColor;
    this.selectionColor = selectionColor;
    this.imageHeight = imageHeight;
    this.imageWidth = imageWidth;
    this.itemViews = new CardView[items.size()];
    this.multiSelect = multiSelect;

    this.selection = new Boolean[items.size()];
    Arrays.fill(selection, Boolean.FALSE);
    this.isVisible = new Boolean[items.size()];
    Arrays.fill(isVisible, Boolean.TRUE);
  }

  public ListAdapterWithRecyclerView(ComponentContainer container, YailList stringItems, int textMainColor, float textMainSize, int textMainFont, int backgroundColor, int selectionColor) {
    // Legacy Support
    this.container = container;
    this.textMainSize = textMainSize;
    this.textMainColor = textMainColor;
    this.textMainFont = textMainFont;
    this.textDetailColor = textMainColor;
    this.textDetailSize = 0;
    this.textDetailFont = 0;
    this.layoutType = Component.LISTVIEW_LAYOUT_SINGLE_TEXT;
    this.backgroundColor = backgroundColor;
    this.selectionColor = selectionColor;
    this.imageHeight = 0;
    this.imageWidth = 0;
    this.multiSelect = false;
    this.itemViews = new CardView[stringItems.size()];
    this.selection = new Boolean[stringItems.size()];
    Arrays.fill(selection, Boolean.FALSE);
    this.isVisible = new Boolean[stringItems.size()];
    Arrays.fill(isVisible, Boolean.TRUE);

    // Build the list of strings into a list of dictionaries
    this.items = new ArrayList<>();
    // YailList is 1-indexed
    for(int i = 1; i <= stringItems.size(); ++i) {
      String itemString = YailList.YailListElementToString(stringItems.get(i));
      YailDictionary itemDict = new YailDictionary();
      itemDict.put(Component.LISTVIEW_KEY_MAIN_TEXT, itemString);
      this.items.add(itemDict);
    }
  }

  public void selectFromText(String text1) {
    for (int i = 0; i < itemViews.length; i++) {
      YailDictionary d = items.get(i);
      if (d.get(Component.LISTVIEW_KEY_MAIN_TEXT).toString() == text1) {
        selection[i] = true;
        itemViews[i].setBackgroundColor(selectionColor);
        break;
      }
    }
  }

  public void clearSelections() {
    Arrays.fill(selection, Boolean.FALSE);
    for (int i = 0; i < itemViews.length; i++) {
      itemViews[i].setBackgroundColor(backgroundColor);
    }
  }

  public void toggleSelection(int pos) {
    // With single select, clicked item becomes the only selected item
    Arrays.fill(selection, Boolean.FALSE);
    for (int i = 0; i < itemViews.length; i++) {
      // Views are created when they are displayed, so this list may not be fully populated.
      if (itemViews[i] != null) {
        itemViews[i].setBackgroundColor(backgroundColor);
      }
    }
    selection[pos] = true;
    itemViews[pos].setBackgroundColor(selectionColor);
  }

  public void changeSelections(int pos) {
    // With multi select, clicking an item toggles its selection status on and off
    selection[pos] = !selection[pos];
    if (selection[pos]) {
      itemViews[pos].setBackgroundColor(selectionColor);
    } else {
      itemViews[pos].setBackgroundColor(backgroundColor);
    }
  }

  @Override
  public RvViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
    CardView cardView = new CardView(container.$context());
    cardView.setUseCompatPadding(true);
    cardView.setContentPadding(10, 10, 10, 10);
    cardView.setPreventCornerOverlap(true);
    cardView.setCardElevation(2.1f);
    cardView.setRadius(0);
    cardView.setMaxCardElevation(3f);
    cardView.setBackgroundColor(backgroundColor);
    cardView.setClickable(isSelected);
    idCard = ViewCompat.generateViewId();
    cardView.setId(idCard);

    CardView.LayoutParams params1 = new CardView.LayoutParams(CardView.LayoutParams.FILL_PARENT, CardView.LayoutParams.WRAP_CONTENT);
    params1.setMargins(0, 0, 0, 0);

    ViewCompat.setElevation(cardView, 20);

    // All layouts have a textview containing MainText
    TextView textViewFirst = new TextView(container.$context());
    idFirst = ViewCompat.generateViewId();
    textViewFirst.setId(idFirst);
    LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    layoutParams1.topMargin = 10;
    textViewFirst.setLayoutParams(layoutParams1);
    textViewFirst.setTextSize(textMainSize);
    textViewFirst.setTextColor(textMainColor);
    TextViewUtil.setFontTypeface(textViewFirst, textMainFont, false, false);
    LinearLayout linearLayout1 = new LinearLayout(container.$context());
    LinearLayout.LayoutParams layoutParamslinear1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    linearLayout1.setLayoutParams(layoutParamslinear1);
    linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

    if (layoutType == Component.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT || layoutType == Component.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
      // Create ImageView for layouts containing an image
      ImageView imageView = new ImageView(container.$context());
      idImages = ViewCompat.generateViewId();
      imageView.setId(idImages);
      LinearLayout.LayoutParams layoutParamsImage = new LinearLayout.LayoutParams(imageWidth, imageHeight);
      imageView.setLayoutParams(layoutParamsImage);
      linearLayout1.addView(imageView);
    }

    if (layoutType == Component.LISTVIEW_LAYOUT_SINGLE_TEXT || layoutType == Component.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
      // All layouts containing just MainText
      linearLayout1.addView(textViewFirst);
    } else {
      // All layouts containing MainText and DetailText
      TextView textViewSecond = new TextView(container.$context());
      idSecond = ViewCompat.generateViewId();
      textViewSecond.setId(idSecond);
      LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      textViewSecond.setTextSize(textDetailSize);
      TextViewUtil.setFontTypeface(textViewSecond, textDetailFont, false, false);
      textViewSecond.setTextColor(textDetailColor);
      if (layoutType == Component.LISTVIEW_LAYOUT_TWO_TEXT || layoutType == Component.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT) {
        layoutParams2.topMargin = 10;
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
        layoutParams2.setMargins(50, 10, 0, 0);
        textViewSecond.setLayoutParams(layoutParams2);
        textViewSecond.setMaxLines(1);
        textViewSecond.setEllipsize(null);

        linearLayout1.addView(textViewFirst);
        linearLayout1.addView(textViewSecond);
      }
    }
    cardView.setLayoutParams(params1);
    cardView.addView(linearLayout1);

    return new RvViewHolder(cardView);
  }

  @Override
  public void onBindViewHolder(final RvViewHolder holder, int position) {

    holder.cardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        holder.onClick(v);
      }
    });

    YailDictionary dictItem = items.get(position);
    String first = dictItem.get(Component.LISTVIEW_KEY_MAIN_TEXT).toString();
    String second = "";
    if (dictItem.containsKey(Component.LISTVIEW_KEY_DESCRIPTION)) {
      second = dictItem.get("Text2").toString();
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
      String imageName = dictItem.get(Component.LISTVIEW_KEY_IMAGE).toString();
      Drawable drawable = new BitmapDrawable();
      try {
        drawable = MediaUtil.getBitmapDrawable(container.$form(), imageName);
      } catch (IOException ioe) {
        Log.e(LOG_TAG, "onBindViewHolder Unable to load image " + imageName + ": " + ioe.getMessage());
      }
      holder.textViewFirst.setText(first);
      ViewUtil.setImage(holder.imageVieww, drawable);
    } else if (layoutType == Component.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT) {
      String imageName = dictItem.get(Component.LISTVIEW_KEY_IMAGE).toString();
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
    if (selection[position]) {
      holder.cardView.setBackgroundColor(selectionColor);
    } else {
      holder.cardView.setBackgroundColor(backgroundColor);
    }
    if (!isVisible[position])
    {
      holder.cardView.setVisibility(View.GONE);
      holder.cardView.getLayoutParams().height = 0;
    }
    itemViews[position] = holder.cardView;
  }


  @Override
  public int getItemCount() {
    return (itemViews.length);
  }

  class RvViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView textViewFirst;
    public TextView textViewSecond;
    public ImageView imageVieww;
    public CardView cardView;

    public RvViewHolder(View view) {
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
      if (multiSelect) {
        changeSelections(position);
      } else {
        toggleSelection(position);
      }
      clickListener.onItemClick(position, v);
    }
  }

  public void setOnItemClickListener(ClickListener clickListener) {
    ListAdapterWithRecyclerView.clickListener = clickListener;
  }

  public interface ClickListener {
    void onItemClick(int position, View v);
  }

  public String getSelectedItems() {
    String selectedItems = new String();
    for (int i = 0; i < selection.length; ++i) {
      if (selection[i]) {
        YailDictionary dictItem = items.get(i);
        selectedItems += "," + dictItem.get(Component.LISTVIEW_KEY_MAIN_TEXT).toString();
      }
    }
    return selectedItems.length() > 0 ? selectedItems.substring(1) : "";
  }

  @Override
  public Filter getFilter() {
    Filter filter = new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {
        String filterQuery = charSequence.toString().toLowerCase();
        FilterResults results = new FilterResults();
        List<YailDictionary> filteredList = new ArrayList<>();

        if(filterQuery == null || filterQuery.length() == 0) {
          filteredList = new ArrayList<>(items);
        } else {
          for(int i = 0; i < items.size(); ++i) {
            YailDictionary itemDict = items.get(i);
            String filterString = itemDict.get(Component.LISTVIEW_KEY_MAIN_TEXT).toString() + " " + itemDict.get("Text2").toString();
            if (filterString.toLowerCase().contains(filterQuery)) {
              filteredList.add(itemDict);
            }
          }
        }
        results.count = filteredList.size();
        results.values = filteredList;
        return results;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        filterItems = (List<YailDictionary>) filterResults.values;
        // Usually GUI objects take up no screen space when set to invisible, but setting a CardView object to invisible
        // was displaying an empty object. Therefore, set the height to 0 as well.
        // Setting visibility on individual entries will keep the selected index(ices) the same regardless of filter.
        if (filterItems.size() == 0) {
          Arrays.fill(isVisible, Boolean.TRUE);
        } else {
          for (int i = 0; i < items.size(); ++i) {
            if (filterItems.size() > 0 && filterItems.contains(items.get(i))) {
              isVisible[i] = true;
              if (itemViews[i] != null) {
                itemViews[i].setVisibility(View.VISIBLE);
                itemViews[i].getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
              }
            } else {
              isVisible[i] = false;
              if (itemViews[i] != null) {
                itemViews[i].setVisibility(View.GONE);
                itemViews[i].getLayoutParams().height = 0;
              }
            }
          }
        }
      }
    };
    return filter;
  }
};