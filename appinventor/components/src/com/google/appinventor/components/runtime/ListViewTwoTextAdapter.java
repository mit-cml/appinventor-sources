// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;

import com.google.appinventor.components.runtime.util.YailDictionary;

public class ListViewTwoTextAdapter extends ListAdapterWithRecyclerView {

  public ListViewTwoTextAdapter(ComponentContainer container, ListDataModel model,
      ListViewStyle style) {
    super(container, model, style);
  }

  @Override
  public RvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    CardView cardView = createCardView(parent);
    final int idCard = cardView.getId();

    // MainText
    TextView textViewFirst = new TextView(container.$context());
    final int idFirst = ViewCompat.generateViewId();
    textViewFirst.setId(idFirst);
    LinearLayout.LayoutParams layoutParams1 =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    textViewFirst.setLayoutParams(layoutParams1);

    // DetailText
    TextView textViewSecond = new TextView(container.$context());
    final int idSecond = ViewCompat.generateViewId();
    textViewSecond.setId(idSecond);
    LinearLayout.LayoutParams layoutParams2 =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    textViewSecond.setLayoutParams(layoutParams2);

    LinearLayout linearLayout2 = new LinearLayout(container.$context());
    LinearLayout.LayoutParams layoutParamslinear2 =
        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
    linearLayout2.setLayoutParams(layoutParamslinear2);
    linearLayout2.setOrientation(LinearLayout.VERTICAL);

    linearLayout2.addView(textViewFirst);
    linearLayout2.addView(textViewSecond);

    LinearLayout linearLayout1 = new LinearLayout(container.$context());
    LinearLayout.LayoutParams layoutParamslinear1 =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    linearLayout1.setLayoutParams(layoutParamslinear1);
    linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
    linearLayout1.addView(linearLayout2);
    cardView.addView(linearLayout1);

    return new TwoTextRvViewHolder(cardView, idCard, idFirst, idSecond);
  }

  @Override
  public void onBindViewHolder(RvViewHolder holder, int position) {
    TwoTextRvViewHolder twoTextHolder = (TwoTextRvViewHolder) holder;
    Object o = model.getVisibleItem(position);
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
    twoTextHolder.textViewFirst.setText(first);
    twoTextHolder.textViewSecond.setText(second);

    styleMainText(twoTextHolder.textViewFirst);
    styleDetailText(twoTextHolder.textViewSecond);
    styleCardView(twoTextHolder.cardView, position);
  }

  public class TwoTextRvViewHolder extends RvViewHolder {

    public TextView textViewFirst;
    public TextView textViewSecond;
    public CardView cardView;

    public TwoTextRvViewHolder(
        View view, int idCard, int idFirst, int idSecond) {
      super(view);
      cardView = (CardView) view.findViewById(idCard);
      textViewFirst = (TextView) view.findViewById(idFirst);
      textViewSecond = (TextView) view.findViewById(idSecond);      
    }
  }
}
