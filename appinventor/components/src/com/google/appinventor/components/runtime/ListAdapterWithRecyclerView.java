


package com.google.appinventor.components.runtime;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.ViewCompat;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
//@UsesLibraries(libraries ="classes.jar," + "RecyclerView.jar")
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
    "android.permission.READ_EXTERNAL_STORAGE")
public class ListAdapterWithRecyclerView extends RecyclerView.Adapter<ListAdapterWithRecyclerView.RvViewHolder> {

    private static final String TAG = "ListAdapterWithRecyclerView";

    private String[] firstItem;
    private String[] secondItem;
    private ArrayList<Drawable> images;
    private Context context;
    private int textColor;
    private int textSize; 

    private int idFirst,idSecond,idImages;

    public ListAdapterWithRecyclerView(Context context,String[] first,String[] second,ArrayList<Drawable> images,int textColor,int textSize){
        this.firstItem = first;
        this.secondItem = second;   
        this.images=images;
        this.context=context;
        this.textSize=textSize;
        this.textColor=textColor;
    }

    @Override
    public RvViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        RelativeLayout relativeLayout=new RelativeLayout(context);
        relativeLayout.setBackgroundColor(Color.parseColor("#E9E9E9"));

        ViewCompat.setElevation(relativeLayout, 20);
       
       //CardView cardView = new CardView(context);
       CardView cardView=new CardView(context);
        cardView.setUseCompatPadding(true);
        cardView.setContentPadding(30, 30, 30, 0);
        cardView.setPreventCornerOverlap(true);
        //cardView.setCardBackgroundColor(Color.WHITE);
        cardView.setCardElevation(2.1f);
        cardView.setRadius(0);
        cardView.setMaxCardElevation(3f);


        TextView textViewFirst=new TextView(context);
        TextView textViewSecond=new TextView(context);
        ImageView imageView = new ImageView(context);

        //generateViewId() applicable only for Android 17 and above.ViewCompat.
        //generateViewId() for less than 17 should be used.

        idFirst=View.generateViewId();
        idSecond=View.generateViewId();
        idImages=View.generateViewId();
        
        textViewFirst.setId(idFirst);
        textViewSecond.setId(idSecond);
        imageView.setId(idImages);
       
        LinearLayout linearLayout1= new LinearLayout(context);
        LinearLayout.LayoutParams layoutParamslinear1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout1.setLayoutParams(layoutParamslinear1);
        linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout linearLayout2= new LinearLayout(context);
        LinearLayout.LayoutParams layoutParamslinear2 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2);
        linearLayout2.setLayoutParams(layoutParamslinear2);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);

        linearLayout2.addView(textViewFirst);
        linearLayout2.addView(textViewSecond);

       LinearLayout.LayoutParams layoutParams1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
       layoutParams1.topMargin = 10;

        LinearLayout.LayoutParams layoutParams2=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.topMargin = 10;

        LinearLayout.LayoutParams layoutParamsImage = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        layoutParamsImage.setMargins(5,5,5,5);
        //imageView.getLayoutParams().height = 20;
        //imageView.getLayoutParams().width = 20;
        imageView.setLayoutParams(layoutParamsImage);

        RelativeLayout.LayoutParams params1=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(30 ,30,30,30);

        textViewFirst.setLayoutParams(layoutParams1);
        textViewSecond.setLayoutParams(layoutParams2);

        textViewFirst.setTextSize(textSize);
        textViewSecond.setTextSize(textSize);


        textViewFirst.setTextColor(textColor);
        textViewSecond.setTextColor(textColor);

        linearLayout1.addView(imageView);
        linearLayout1.addView(linearLayout2);
       // relativeLayout.addView(linearLayout1);
       // relativeLayout.setLayoutParams(params1);
        //cardView.addView(imageView);
        //cardView.addView(linearLayout2);
        cardView.addView(linearLayout1);
        cardView.setLayoutParams(params1);
        

        return new RvViewHolder(cardView);

 }

    @Override
    public void onBindViewHolder(final RvViewHolder holder, int position) {
            String first =firstItem[position];
            String second=secondItem[position];
        //    String path = images[position];
            Drawable drawable = images.get(position);   
            ViewUtil.setImage(holder.imageVieww, drawable);

            holder.textViewFirst.setText(first);
            holder.textViewSecond.setText(second);
        //    holder.imageVieww.setImageResource(img);
        }


    @Override
    public int getItemCount() {
        return (firstItem.length);
    }

    class RvViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewFirst;
        public TextView textViewSecond;
        public ImageView imageVieww;

        public RvViewHolder(View view){
            super(view);
            textViewFirst = (TextView)view.findViewById(idFirst);
            textViewSecond=(TextView)view.findViewById(idSecond);
            imageVieww = (ImageView)view.findViewById(idImages);
        }
    }
};




