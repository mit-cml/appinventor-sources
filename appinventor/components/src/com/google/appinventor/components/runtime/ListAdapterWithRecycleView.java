


package com.google.appinventor.components.runtime;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;

import java.util.List;


@SimpleObject
@UsesLibraries(libraries ="RecyclerView.jar" + "CardView.jar")
public final class ListAdapterWithRecycleView extends RecyclerView.Adapter<ListAdapterWithRecycleView.PersonViewHolder> {

    private static final String TAG = "ListAdapterWithRecycleView";

    private String[] firstItem;
    private String[] secondItem;
    private Context context;

    private int idFirst,idSecond;

    public ListAdapterWithRecycleView(Context context,String[] first,String[] second){
        this.firstItem = first;
        this.secondItem = second;
        this.context=context;
    }

    @Override
    public PersonViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        CardView cardView=new CardView(context);
        TextView textViewFirst=new TextView(context);
        TextView textViewSecond=new TextView(context);

        //generateViewId() applicable only for Android 17 and above.ViewCompat.
        //generateViewId() for less than 17 should be used.

        idFirst=View.generateViewId();
        idSecond=View.generateViewId();

        textViewFirst.setId(idFirst);
        textViewSecond.setId(idSecond);
        
        RelativeLayout.LayoutParams layoutParams1=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams1.leftMargin = 25;
        layoutParams1.topMargin = 25;

        RelativeLayout.LayoutParams layoutParams2=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.leftMargin = 500;
        layoutParams2.topMargin = 25;

        RelativeLayout.LayoutParams params1=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(30,30,30,30);

        textViewFirst.setLayoutParams(layoutParams1);
        textViewSecond.setLayoutParams(layoutParams2);

        textViewFirst.setTextSize(20);
        textViewSecond.setTextSize(20);

        cardView.addView(textViewFirst);
        cardView.addView(textViewSecond);

        cardView.setLayoutParams(params1);

        return new PersonViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final PersonViewHolder holder, int position) {

            final String first = firstItem[position];
            final String second=secondItem[position];

            holder.textViewFirst.setText(first);
            holder.textViewSecond.setText(second);
        }


    @Override
    public int getItemCount() {
        return (firstItem.length);
    }

    class PersonViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewFirst;
        public TextView textViewSecond;

        public PersonViewHolder(View view){
            super(view);
            textViewFirst = (TextView)view.findViewById(idFirst);
            textViewSecond=(TextView)view.findViewById(idSecond);
        }
    }
}


