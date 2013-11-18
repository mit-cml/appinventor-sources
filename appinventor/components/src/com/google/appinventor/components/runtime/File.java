// Copyright 2012 Google Inc. All Rights Reserved.
package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;

import android.content.Context;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An interface for working with files and directories on the phone.
 *
 */
@DesignerComponent(version = YaVersion.FILE_COMPONENT_VERSION,
    description = "Non-visible component for manipulating files on the pnohe.",
    category = ComponentCategory.STORAGE,
    nonVisible = true,
    iconName = "images/tinyDB.png")
@SimpleObject
public class File extends AndroidNonvisibleComponent implements Component {
		// the filename set by the user
        private String filename;
        /**
   * Creates a new File component.
   *
   * @param container the Form that this component is contained in.
   */
  public File(ComponentContainer container) {
    super(container.$form());
    final Context context = (Context) container.$context();
  }
  /**
   * Store text to a specified file on the phone.
   *
   * @param text the text to be stored
   * @param fileName the file to which the text will be stored
   * @return the absolute path of the modified file
   * @throws IOException if the text cannot be written to the file
   */
  @SimpleFunction
  public void OverwriteTo(String text) {
      byte[] array = text.getBytes();
      InputStream in = new ByteArrayInputStream(array);
      String absFilename = AbsoluteFileName();
      try {       
          FileUtil.writeToFileHelper(in, absFilename);
          in.close();
          } catch (IOException e) {
                  form.dispatchErrorOccurredEvent(this, "WriteTo",
                                  ErrorMessages.ERROR_UNABLE_TO_WRITE_TO_FILE, absFilename);
          }
  }
  
  /**
   * Store text to a specified file on the phone.
   *
   * @param text the text to be stored
   * @param fileName the file to which the text will be stored
   * @return the absolute path of the modified file
   * @throws IOException if the text cannot be written to the file
   */
  @SimpleFunction
  public void AppendTo(String text) {
      byte[] array = text.getBytes();
      InputStream in = new ByteArrayInputStream(array);
      String absFilename = AbsoluteFileName();
      try {       
          FileUtil.writeToFileHelper(in, absFilename);
          in.close();
          } catch (IOException e) {
                  form.dispatchErrorOccurredEvent(this, "WriteTo",
                                  ErrorMessages.ERROR_UNABLE_TO_WRITE_TO_FILE, absFilename);
          }
  }

  /**
   * Retrieve the text stored in a specified file.
   *
   * @param fileName the file from which the text is read
   * @return the text stored in the specified file
   * @throws IOException if the text cannot be read from the file
   */
  @SimpleFunction
  public String ReadFrom() {
          String text = "";
          String absFilename = AbsoluteFileName();
          try {
                  byte[] viewArray = FileUtil.readFile(absFilename);
              text = new String(viewArray);
          } catch (IOException e) {
                  form.dispatchErrorOccurredEvent(this, "ReadFrom",
                                  ErrorMessages.ERROR_UNABLE_TO_READ_FROM_FILE, absFilename);
          }
      return text;
  }
  
  /**
   * Delete the specified file.
   * 
   * @param fileName the file to be deleted
   */
  @SimpleFunction
  public void Delete() {
          java.io.File file = new java.io.File(AbsoluteFileName());
          boolean success = file.delete();
          if (! success) {
                  form.dispatchErrorOccurredEvent(this, "Delete",
                                  ErrorMessages.ERROR_UNABLE_TO_DELETE_FILE, AbsoluteFileName());
          }
  }
}