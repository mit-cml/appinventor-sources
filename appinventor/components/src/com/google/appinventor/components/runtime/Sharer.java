package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import android.os.Environment;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.util.Log;
import android.widget.Toast;

import java.io.File;


@DesignerComponent(version = YaVersion.SHARER_COMPONENT_VERSION,
	description ="<p>Simple Sharer</p>",
	category = ComponentCategory.SOCIAL,
	nonVisible = true)
public class Sharer extends AndroidNonvisibleComponent {

	public Sharer(ComponentContainer container) {
		super(container.$form());
	}

	@SimpleFunction
	public void ShareText(String text) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, text);
		shareIntent.setType("text/plain");
		
		Context cont = this.form.$context();
		cont.startActivity(Intent.createChooser(shareIntent,  "Send using..."));
	}

	@SimpleFunction
	public void ShareFile(String path, boolean alert) {
		ShareFileWithText("", path, alert);
	}

	@SimpleFunction
	public void ShareFileWithText(String text, String path, boolean alert) {
		Activity act = (Activity) this.form.$context();

		File file =  Environment.getExternalStoragePublicDirectory(path); 
		if (file.exists()) {
			Uri uri = Uri.fromFile(file);

	    	String ext = path.substring(path.lastIndexOf(".")+1).toLowerCase();
			ContentResolver cR = act.getContentResolver();
			MimeTypeMap mime = MimeTypeMap.getSingleton();
	    	String type = mime.getMimeTypeFromExtension(ext);
		
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
			shareIntent.setType(type);

			if (text.length() > 0) {
				shareIntent.putExtra(Intent.EXTRA_TEXT, text);
			}
			
			act.startActivity(Intent.createChooser(shareIntent, "Send using..."));
		}
		else if (alert) {
			Toast.makeText(act, path + " not found", Toast.LENGTH_SHORT).show();
		}
	}
}