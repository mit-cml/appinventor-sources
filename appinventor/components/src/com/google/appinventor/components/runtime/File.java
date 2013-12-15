package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * An interface for working with files and directories on the phone.
 *
 */
@DesignerComponent(version = YaVersion.FILE_COMPONENT_VERSION,
    description = "Non-visible component for manipulating files on the phone.",
    category = ComponentCategory.STORAGE,
    nonVisible = true,
    iconName = "images/file.png")
@SimpleObject
public class File extends AndroidNonvisibleComponent implements Component {
	private final Activity activity;
  
  /**
   * Creates a new File component.
   *
   * @param container the Form that this component is contained in.
   * @param activity The main activity that is run on the UI.
   */
  public File(ComponentContainer container) {
    super(container.$form());
    final Context context = (Context) container.$context();
    activity = (Activity) container.$context();
  }
  
  /**
   * Stores the text to a specified file on the phone.
   * Calls the Write function to write to the file asynchronously to prevent
   * the UI from hanging when there is a large write.
   *
   * @param text the text to be stored
   * @param fileName the file to which the text will be stored
   */
  @SimpleFunction
  public void SaveFile(String text, String fileName) {
      FileUtil.checkExternalStorageWriteable();
      Write(fileName, text, false);
  }
  
  /**
   * Appends text to a specified file on the phone.
   * Calls the Write function to write to the file asynchronously to prevent
   * the UI from hanging when there is a large write.
   *
   * @param text the text to be stored
   * @param fileName the file to which the text will be stored
   */
  @SimpleFunction
  public void AppendToFile(String text, String fileName) {
  	  FileUtil.checkExternalStorageWriteable();
      Write(fileName, text, true);
  }

  /**
   * Retrieve the text stored in a specified file.
   *
   * @param fileName the file from which the text is read
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if the text cannot be read from the file
   */
  @SimpleFunction
  public void ReadFrom(String fileName) {
      final String filepath = AbsoluteFileName(fileName);
      AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      	public void run() {
      		AsyncRead(filepath);
        }
    });
  }
  
  /**
   * Delete the specified file.
   * 
   * @param fileName the file to be deleted
   */
  @SimpleFunction
  public void Delete(String fileName) {
	  String filepath = AbsoluteFileName(fileName);
	  java.io.File file = new java.io.File(filepath);
	  file.delete();
  }
  
  /**
   * Writes to the specified file.
   * 
   * @param fileName the file to write 
   * @param text to write to the file
   * @param append determines whether text should be appended to the file, 
   * 	or overwrite the file
   */
  private void Write(final String filename, final String text, final boolean append) {
      AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      	public void run() {
      		final String filepath = AbsoluteFileName(filename);
  	  		final java.io.File file = new java.io.File(filepath);
			if(!file.exists()){
      			try {
      				file.createNewFile();
        		} catch (IOException e) {
                  	if (append) {
	  					form.dispatchErrorOccurredEvent(File.this, "AppendTo",
	  						ErrorMessages.ERROR_CANNOT_CREATE_FILE, filepath);
	  				} else {
	  					form.dispatchErrorOccurredEvent(File.this, "SaveFile",
	  						ErrorMessages.ERROR_CANNOT_CREATE_FILE, filepath);
	  				}
        		}
      		}
      		try {
	    		FileOutputStream fileWriter = new FileOutputStream(file, append);
				OutputStreamWriter out = new OutputStreamWriter(fileWriter);
			  	out.write(text);
			  	out.flush();
	  	  		out.close();
	  	  		fileWriter.close();
	  		} catch (IOException e) {
	  			if (append) {
	  				form.dispatchErrorOccurredEvent(File.this, "AppendTo",
	  					ErrorMessages.ERROR_CANNOT_WRITE_TO_FILE, filepath);
	  			} else {
	  				form.dispatchErrorOccurredEvent(File.this, "SaveFile",
	  					ErrorMessages.ERROR_CANNOT_WRITE_TO_FILE, filepath);
	  			}			      	
      		}
		}
	}); 
  }
  
  /**
   * Asynchronously reads from the given file. Calls the main event thread
   * when the function has completed reading from the file.
   * @param fileName the file to read
   * @throws FileNotFoundException
   * @throws IOException when the system cannot read the file 
   */
  private void AsyncRead(final String filepath) {
    try {
	 	java.io.File file = new java.io.File(filepath);
  		StringBuilder sb = new StringBuilder();
  		BufferedReader bufferedReader = new BufferedReader(new FileReader(file)); 
  		String line;
  		while ((line = bufferedReader.readLine()) != null) {
  			sb.append(line);
			sb.append(System.getProperty("line.separator"));
		}
		final String text = sb.toString();
		activity.runOnUiThread(new Runnable() {
    	@Override
        	public void run() {
            	  GotText(text);
            }
    	});
    } catch (FileNotFoundException e) {
				form.dispatchErrorOccurredEvent(File.this, "ReadFrom",
                          ErrorMessages.ERROR_CANNOT_FIND_FILE, filepath);
	} catch (IOException e) {
              form.dispatchErrorOccurredEvent(File.this, "ReadFrom",
                      ErrorMessages.ERROR_CANNOT_READ_FILE, filepath);
    }
  }
	
  /**
   * Event indicating that a request has finished.
   *
   * @param the text read from the file
   */
  @SimpleEvent
  public void GotText(String text) {
    // invoke the application's "GotText" event handler.
    EventDispatcher.dispatchEvent(this, "GotText", text);
  }
  
  /**
   * Returns absolute file path. By default, returns file path to the folder AppInventor/assets.
   * 
   * @param fileName the file used to construct the file path 
   */
  private String AbsoluteFileName(String filename) {
  	if (filename.startsWith("//")) return filename;
  	else if (filename.startsWith("/")) return Environment.getExternalStorageDirectory().getPath() + filename;                  
  	else return Environment.getExternalStorageDirectory().getPath() + "/AppInventor/assets/" + filename;
  }
}