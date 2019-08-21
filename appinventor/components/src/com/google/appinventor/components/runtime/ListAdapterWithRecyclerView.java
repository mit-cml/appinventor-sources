


package com.google.appinventor.components.runtime;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.ViewCompat;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.text.Spannable;
import android.text.SpannableString;
//
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.drawable.Drawable;
import java.util.*;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.runtime.util.ViewUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;

import java.util.List;


@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
    "android.permission.READ_EXTERNAL_STORAGE")
public class ListAdapterWithRecyclerView extends RecyclerView.Adapter<ListAdapterWithRecyclerView.RvViewHolder> {

    private static final String TAG = "ListAdapterWithRecyclerView";

    private static ClickListener clickListener;

    private String[] firstItem;
    private String[] secondItem;
    private ArrayList<Drawable> images;
    private Context context;
    private int textColor;
    private int textSize; 
    private int layoutType;
    private int backgroundColor;
    private int selectionColor;
 //   private int selectionIndex;

    public boolean isSelected=false;

    private int idFirst,idSecond,idImages,idCard;

    public ListAdapterWithRecyclerView(Context context,String[] first,String[] second,ArrayList<Drawable> images,int textColor,int textSize,int layoutType,int backgroundColor,int selectionColor){//,int selectionIndex){
        this.firstItem = first;
        this.secondItem = second;   
        this.images=images;
        this.context=context;
        this.textSize=textSize;
        this.textColor=textColor;
        this.layoutType=layoutType;
        this.backgroundColor=backgroundColor;
        this.selectionColor=selectionColor;
      //  this.selectionIndex=selectionIndex;
    }

    @Override
    public RvViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        TextView textViewFirst=new TextView(context);
        TextView textViewSecond=new TextView(context);
        ImageView imageView = new ImageView(context);

        CardView cardView=new CardView(context);
        cardView.setUseCompatPadding(true);
        cardView.setContentPadding(30, 30, 30, 0);
        cardView.setPreventCornerOverlap(true);
        cardView.setCardElevation(2.1f);
        cardView.setRadius(0);
        cardView.setMaxCardElevation(3f);
        cardView.setBackgroundColor(backgroundColor);
        cardView.setClickable(isSelected);

        idFirst=View.generateViewId();
        idSecond=View.generateViewId();
        idImages=View.generateViewId();
        idCard=View.generateViewId();

        textViewFirst.setId(idFirst);
        textViewSecond.setId(idSecond);
        imageView.setId(idImages);
        cardView.setId(idCard);

        LinearLayout linearLayout1= new LinearLayout(context);
        LinearLayout.LayoutParams layoutParamslinear1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout1.setLayoutParams(layoutParamslinear1);
        linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout linearLayout2= new LinearLayout(context);
        LinearLayout.LayoutParams layoutParamslinear2 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
        linearLayout2.setLayoutParams(layoutParamslinear2);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams1.topMargin = 10;
    
        LinearLayout.LayoutParams layoutParams2=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.topMargin = 10;

        LinearLayout.LayoutParams layoutParamsImage = new LinearLayout.LayoutParams(200,200,1);
        layoutParamsImage.setMargins(5,25,5,25);
        imageView.setLayoutParams(layoutParamsImage);

        LinearLayout.LayoutParams params1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(30 ,30,30,30);

        cardView.setBackgroundColor(Color.parseColor("#E9E9E9"));

        ViewCompat.setElevation(cardView, 20);

        textViewFirst.setLayoutParams(layoutParams1);
        textViewSecond.setLayoutParams(layoutParams2);
     
        textViewFirst.setTextSize(textSize);
        textViewSecond.setTextSize(textSize);

        textViewFirst.setTextColor(textColor);
        textViewSecond.setTextColor(textColor);
      
      if(layoutType==0){
        
        linearLayout2.addView(textViewFirst);
        linearLayout1.addView(linearLayout2);
        cardView.setLayoutParams(params1);
        cardView.addView(linearLayout1);
        }else if(layoutType==1){
        
        linearLayout2.addView(textViewFirst);
        linearLayout2.addView(textViewSecond);
        linearLayout1.addView(linearLayout2);
        cardView.setLayoutParams(params1);
        cardView.addView(linearLayout1);
        }else if(layoutType==2){
        
        linearLayout2.addView(textViewFirst);
        linearLayout2.addView(textViewSecond);
        linearLayout1.addView(linearLayout2);
        cardView.setLayoutParams(params1);
        cardView.addView(linearLayout1);
        }else if(layoutType==3){
        
        linearLayout2.addView(textViewFirst);
        linearLayout1.addView(imageView);
        linearLayout1.addView(linearLayout2);
        cardView.setLayoutParams(params1);
        cardView.addView(linearLayout1);
        }
      else if(layoutType==4){

        linearLayout2.addView(textViewFirst);
        linearLayout2.addView(textViewSecond);
        linearLayout1.addView(imageView);
        linearLayout1.addView(linearLayout2);
        cardView.setLayoutParams(params1);
        cardView.addView(linearLayout1);
        }
     //   return new PersonViewHolder(cardView);
        return new RvViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final RvViewHolder holder, int position) {
         
         /*   if(position == selectionIndex){
                holder.cardView.setBackgroundColor(selectionColor);    
            }
            
           if(isSelected){
            holder.cardView.setBackgroundColor(selectionColor);}
            else{
            holder.cardView.setBackgroundColor(backgroundColor);}
            */
            holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.onClick(v);
                
                    if(isSelected){
                    holder.cardView.setBackgroundColor(selectionColor);
                    }else{
                    holder.cardView.setBackgroundColor(backgroundColor);
                    }
                isSelected=!isSelected;    
                }
            });
            
            if(layoutType==0){
            String first =firstItem[position];
            holder.textViewFirst.setText(first);
            }else if(layoutType==1){
            String first =firstItem[position];
            String second=secondItem[position];
            
            holder.textViewFirst.setText(first);
            holder.textViewSecond.setText(second);
            }else if(layoutType==2){
            String first =firstItem[position];
            String second=secondItem[position];
            
            holder.textViewFirst.setText(first);
            holder.textViewSecond.setText(second);
            }else if(layoutType==3){
            String first =firstItem[position];
            Drawable drawable = images.get(position);   
            ViewUtil.setImage(holder.imageVieww, drawable);

            holder.textViewFirst.setText(first);
            }
            else if(layoutType==4){
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

    class RvViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{//, View.OnLongClickListener{
        
        public TextView textViewFirst;
        public TextView textViewSecond;
        public ImageView imageVieww;
        public CardView cardView;

        public RvViewHolder(View view){
            super(view);

            view.setOnClickListener(this);
            //view.setOnLongClickListener(this);

            cardView=(CardView)view.findViewById(idCard);

            if(layoutType == 0){
            textViewFirst = (TextView)view.findViewById(idFirst);
            }
            else if(layoutType == 1){
            textViewFirst = (TextView)view.findViewById(idFirst);
            textViewSecond=(TextView)view.findViewById(idSecond);
            }
            else if(layoutType == 2){
            textViewFirst = (TextView)view.findViewById(idFirst);
            textViewSecond=(TextView)view.findViewById(idSecond);
            }
            else if(layoutType == 3){
            textViewFirst = (TextView)view.findViewById(idFirst);
            imageVieww = (ImageView)view.findViewById(idImages);
            }
            else if(layoutType == 4){
            textViewFirst = (TextView)view.findViewById(idFirst);
            textViewSecond=(TextView)view.findViewById(idSecond);
            imageVieww = (ImageView)view.findViewById(idImages);
            }
        }

         @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }

      /*  @Override
        public boolean onLongClick(View v) {
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }*/

    }

    public void setOnItemClickListener(ClickListener clickListener) {
        ListAdapterWithRecyclerView.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    //    void onItemLongClick(int position, View v);
    }
};
