package com.google.appinventor.components.runtime;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.appinventor.components.runtime.util.MediaUtil;

import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import twitter4j.media.MediaProvider;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.Contacts;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import 	android.graphics.Color;

/********* Adapter class extends with BaseAdapter and implements with OnClickListener ************/
public class CustomAdapter extends BaseAdapter {

	/*********** Declare Used Variables *********/
	Activity activity;
	private String items[];
	private String subitems[];
	private String images[];

	/************* CustomAdapter Constructor *****************/
	public CustomAdapter(Activity a, String[] its, String[] subits, String[] ims) {

		/********** Take passed values **********/
		activity = a;
		items = its;
		subitems = subits;
		images=ims;
	}

	/******** What is the size of Passed Arraylist Size ************/
	public int getCount() {
		return max(items.length, subitems.length, images.length);
	}

	public int max(int a, int b, int c){
		int m=a;
		if(b>m)
			m=b;
		if(c>m)
			m=c;
		return m;
	}

	public Object getItem(int position) {
		if(items.length!=0)
			return items[position];
		else
			return images[position];
	}

	public long getItemId(int position) {
		return position;
	}


	/****** Depends upon data size called for each row , Create each ListView row *****/
	public View getView(int position, View convertView, ViewGroup parent) {

		LinearLayout ll = new LinearLayout(activity);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setPadding(3, 10, 2, 10);

		TextView t1 = new TextView(activity);
		TextView t2 = new TextView(activity);
		ImageView im1 = new ImageView(activity);
		
		if(items.length!=0){
			if(items[position].startsWith("http")){
				im1.setImageBitmap(getBitmapFromURL(items[position]));
				ll.addView(im1);
				return ll;
			}
			else{
			t1.setText(items[position]);
			t1.setTextSize(25);
			t1.setTextColor(Color.parseColor("#FFFFFF"));
			ll.addView(t1);
			}
		}

		if(subitems.length!=0){
			if(subitems[position].startsWith("http")){
				im1.setImageBitmap(getBitmapFromURL(subitems[position]));
				ll.addView(im1);
				return ll;
			}
			else{
			t2.setText(subitems[position]);
			t2.setTextColor(Color.parseColor("#FFFFFF"));
			ll.addView(t2);
			}
		}

		if(images.length!=0){
			if(images[position].startsWith("http"))
				im1.setImageBitmap(getBitmapFromURL(images[position]));
			else
				im1.setImageDrawable(Image.ourImage().get(Integer.parseInt(images[position]))); 

			ll.addView(im1);
		}else{
			im1.setImageBitmap(null);
			ll.addView(im1);
		}
		

		return ll;
	}
	
	public static Bitmap getBitmapFromURL(String src) {
    try {
        URL url = new URL(src);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        Bitmap myBitmap = BitmapFactory.decodeStream(input);
        return myBitmap;
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
} // Author: silentnuke
	
}