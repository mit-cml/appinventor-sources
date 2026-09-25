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

public class ListViewTwoTextLinearAdapter extends ListAdapterWithRecyclerView {

  public ListViewTwoTextLinearAdapter(ComponentContainer container, ListDataModel model,
      ListViewStyle style) {
    super(container, model, style);
  }

  @Override
public RvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    CardView cardView = createCardView(parent);
    final int idCard = cardView.getId();

    // MainText — weight=1 gives it 50% of the row, enabling center/right alignment
    TextView textViewFirst = new TextView(container.$context());
    final int idFirst = ViewCompat.generateViewId();
    textViewFirst.setId(idFirst);
    LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    textViewFirst.setLayoutParams(layoutParams1);

    // DetailText — weight=1 gives it the remaining 50%, so it can never be pushed off-screen
    TextView textViewSecond = new TextView(container.$context());
    final int idSecond = ViewCompat.generateViewId();
    textViewSecond.setId(idSecond);
    LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    textViewSecond.setLayoutParams(layoutParams2);

    LinearLayout linearLayout1 = new LinearLayout(container.$context());
    LinearLayout.LayoutParams layoutParamslinear1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    linearLayout1.setLayoutParams(layoutParamslinear1);
    linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

    linearLayout1.addView(textViewFirst);
    linearLayout1.addView(textViewSecond);
    cardView.addView(linearLayout1);

    return new TwoTextLinearRvViewHolder(cardView, idCard, idFirst, idSecond);
}

  @Override
  public void onBindViewHolder(RvViewHolder holder, int position) {
    TwoTextLinearRvViewHolder twoTextHolder = (TwoTextLinearRvViewHolder) holder;
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

  public class TwoTextLinearRvViewHolder extends RvViewHolder {
    
    public TextView textViewFirst;
    public TextView textViewSecond;
    public CardView cardView;

    public TwoTextLinearRvViewHolder(
        View view, int idCard, int idFirst, int idSecond) {
      super(view);
      cardView = (CardView) view.findViewById(idCard);
      textViewFirst = (TextView) view.findViewById(idFirst);
      textViewSecond = (TextView) view.findViewById(idSecond);      
    }
  }
}
