package com.google.appinventor.components.runtime;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.drawable.Drawable;
import java.util.*;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.runtime.util.ViewUtil;


@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
    "android.permission.READ_EXTERNAL_STORAGE")
public class ListAdapterWithRecyclerView extends RecyclerView.Adapter<ListAdapterWithRecyclerView.RvViewHolder> {

    private static final String TAG = "ListAdapterWithRecyclerView";

    private static ClickListener clickListener;

    private String[] firstItem;
    private String[] secondItem;
    public Boolean[] selection;
    private ArrayList<Drawable> images;
    private Context context;
    private int textMainColor;
    private int textMainSize;
    private int textDetailColor;
    private int textDetailSize; 
    private int layoutType;
    private int backgroundColor;
    private int selectionColor;
    private int imageHeight;
    private int imageWidth;

    public boolean isSelected=false;

    private int idFirst = -1, idSecond = -1, idImages = -1, idCard = 1;

    public ListAdapterWithRecyclerView(Context context,int size,String[] first,String[] second,ArrayList<Drawable> images,int textMainColor,int textDetailColor,int textMainSize,int textDetailSize,int layoutType,int backgroundColor,int selectionColor,int imageWidth,int imageHeight){
        this.firstItem = first;
        this.secondItem = second;
        this.images=images;
        this.context=context;
        this.textMainSize=textMainSize;
        this.textMainColor=textMainColor;
        this.textDetailColor=textDetailColor;
        this.textDetailSize=textDetailSize;
        this.layoutType=layoutType;
        this.backgroundColor=backgroundColor;
        this.selectionColor=selectionColor;
        this.imageHeight=imageHeight;
        this.imageWidth=imageWidth;

        this.selection = new Boolean[size];
        Arrays.fill(selection, Boolean.FALSE);
 }

    @Override
    public RvViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

      CardView cardView = new CardView(context);
      cardView.setUseCompatPadding(true);
      cardView.setContentPadding(30, 30, 30, 0);
      cardView.setPreventCornerOverlap(true);
      cardView.setCardElevation(2.1f);
      cardView.setRadius(0);
      cardView.setMaxCardElevation(3f);
      cardView.setBackgroundColor(backgroundColor);
      cardView.setClickable(isSelected);
      idCard = ViewCompat.generateViewId();
      cardView.setId(idCard);

      CardView.LayoutParams params1 = new CardView.LayoutParams(CardView.LayoutParams.FILL_PARENT, CardView.LayoutParams.WRAP_CONTENT);
      params1.setMargins(30, 30, 30, 30);

      ViewCompat.setElevation(cardView, 20);

      // All layouts have a textview containing MainText
      TextView textViewFirst = new TextView(context);
      idFirst = ViewCompat.generateViewId();
      textViewFirst.setId(idFirst);
      LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      layoutParams1.topMargin = 10;
      textViewFirst.setLayoutParams(layoutParams1);
      textViewFirst.setTextSize(textMainSize);
      textViewFirst.setTextColor(textMainColor);
      LinearLayout linearLayout1 = new LinearLayout(context);
      LinearLayout.LayoutParams layoutParamslinear1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      linearLayout1.setLayoutParams(layoutParamslinear1);
      linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

      if (layoutType == Component.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT || layoutType == Component.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
        // Create ImageView for layouts containing an image
        ImageView imageView = new ImageView(context);
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
        TextView textViewSecond = new TextView(context);
        idSecond = ViewCompat.generateViewId();
        textViewSecond.setId(idSecond);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewSecond.setTextSize(textDetailSize);
        textViewSecond.setTextColor(textDetailColor);
        if (layoutType == Component.LISTVIEW_LAYOUT_TWO_TEXT || layoutType == Component.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT) {
          layoutParams2.topMargin = 10;
          textViewSecond.setLayoutParams(layoutParams2);

          LinearLayout linearLayout2 = new LinearLayout(context);
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
         
            final int pos=position;

            holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    holder.onClick(v);
                    selection[pos]=!selection[pos];                   
                    if(selection[pos]){
                    holder.cardView.setBackgroundColor(selectionColor);
                    }else{
                    holder.cardView.setBackgroundColor(backgroundColor);
                    }
                }
            });
            if(layoutType==Component.LISTVIEW_LAYOUT_SINGLE_TEXT){
            String first =firstItem[position];
            holder.textViewFirst.setText(first);
            }else if(layoutType==Component.LISTVIEW_LAYOUT_TWO_TEXT){
            String first =firstItem[position];
            String second=secondItem[position];
            
            holder.textViewFirst.setText(first);
            holder.textViewSecond.setText(second);
            }else if(layoutType==Component.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR){
            String first =firstItem[position];
            String second=secondItem[position];

            holder.textViewFirst.setText(first);
            holder.textViewSecond.setText(second);
            }else if(layoutType==Component.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT){
            String first =firstItem[position];
            Drawable drawable = images.get(position);   
            ViewUtil.setImage(holder.imageVieww, drawable);

            holder.textViewFirst.setText(first);
            }
            else if(layoutType==Component.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT){
            String first =firstItem[position];
            String second=secondItem[position];
            Drawable drawable = images.get(position);   
            ViewUtil.setImage(holder.imageVieww, drawable);

            holder.textViewFirst.setText(first);
            holder.textViewSecond.setText(second);
            }
        }


    @Override
    public int getItemCount() {
        return (firstItem.length);
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
        clickListener.onItemClick(getAdapterPosition(), v);
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
          selectedItems += "," + firstItem[i];
        }
      }
      return selectedItems.length() > 0 ? selectedItems.substring(1) : "";
    }
};