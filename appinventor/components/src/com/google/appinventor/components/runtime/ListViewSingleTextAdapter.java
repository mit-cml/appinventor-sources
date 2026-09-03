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

public class ListViewSingleTextAdapter extends ListAdapterWithRecyclerView {

  public ListViewSingleTextAdapter(ComponentContainer container, ListDataModel model,
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
    LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    textViewFirst.setLayoutParams(layoutParams1);
    LinearLayout linearLayout1 = new LinearLayout(container.$context());
    LinearLayout.LayoutParams layoutParamslinear1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    linearLayout1.setLayoutParams(layoutParamslinear1);
    linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
    linearLayout1.addView(textViewFirst);
    cardView.addView(linearLayout1);

    return new SingleTextRvViewHolder(cardView, idCard, idFirst);
  }

  @Override
  public void onBindViewHolder(RvViewHolder holder, int position) {
    SingleTextRvViewHolder singleTextHolder = (SingleTextRvViewHolder) holder;
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
    singleTextHolder.textViewFirst.setText(first);

    styleMainText(singleTextHolder.textViewFirst);
    styleCardView(singleTextHolder.cardView, position);
  }
 
  public class SingleTextRvViewHolder extends RvViewHolder {

    public TextView textViewFirst;
    public CardView cardView;

    public SingleTextRvViewHolder(
        View view, int idCard, int idFirst) {
      super(view);
      cardView = (CardView) view.findViewById(idCard);
      textViewFirst = (TextView) view.findViewById(idFirst);
    }
  }
}
