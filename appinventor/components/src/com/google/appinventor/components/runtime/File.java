package com.google.appinventor.components.runtime;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.QUtil;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.PermissionResultHandler;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Non-visible component for storing and retrieving files. Use this component to write or read files
 * on the device. The default behavior is to write files to the private data directory associated
 * with the app. The Companion writes files to `/sdcard/AppInventor/data` for easy debugging. If
 * the file path starts with a slash (`/`), then the file is created relative to `/sdcard`.
 * For example, writing a file to `/myFile.txt` will write the file in `/sdcard/myFile.txt`.
 */
@DesignerComponent(version = YaVersion.FILE_COMPONENT_VERSION,
        description = "Non-visible component for storing and retrieving files. Use this component to " +
                "write or read files on your device. The default behaviour is to write files to the " +
                "private data directory associated with your App. The Companion is special cased to write " +
                "files to /sdcard/AppInventor/data to facilitate debugging. " +
                "If the file path starts with a slash (/), then the file is created relative to /sdcard. " +
                "For example writing a file to /myFile.txt will write the file in /sdcard/myFile.txt.",
        category = ComponentCategory.STORAGE,
        nonVisible = true,
        iconName = "images/file.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class File extends AndroidNonvisibleComponent implements Component {
  public Activity activity;
  public boolean isRepl = false;
  public final int BUFFER_LENGTH = 4096;
  public boolean hasWriteAccess = false;
  public boolean hasReadAccess = false;
  public Context context;
  private static final String LOG_TAG = "FileComponent";

  /**
   * Creates a new File component.
   * @param container the Form that this component is contained in.
   */
  public File(ComponentContainer container) {
    super(container.$form());
    if (form instanceof com.google.appinventor.components.runtime.ReplForm) {
      isRepl = true;
    }
    activity = (Activity) container.$context();
    context = (Context)container.$context();
    hasWriteAccess = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0;
    hasReadAccess = context.checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0;
  }

  /**
   * Saves text to a file. If the `fileName`{:.text.block} begins with a slash (`/`) the file is
   * written to the sdcard (for example, writing to `/myFile.txt` will write the file to
   * `/sdcard/myFile.txt`). If the `fileName`{:.text.block} does not start with a slash, it will be
   * written in the program's private data directory where it will not be accessible to other
   * programs on the phone. There is a special exception for the AI Companion where these files are
   * written to `/sdcard/AppInventor/data` to facilitate debugging.
   *
   *   Note that this block will overwrite a file if it already exists. If you want to add content
   * to an existing file use the {@link #AppendToFile(String, String)} method.
   *
   * @internaldoc
   * Calls the Write function to write to the file asynchronously to prevent
   * the UI from hanging when there is a large write.
   *
   * @param text the text to be stored
   * @param fileName the file to which the text will be stored
   */
  @SimpleFunction(description = "Saves text to a file. If the filename " +
          "begins with a slash (/) the file is written to the sdcard. For example writing to " +
          "/myFile.txt will write the file to /sdcard/myFile.txt. If the filename does not start " +
          "with a slash, it will be written in the programs private data directory where it will " +
          "not be accessible to other programs on the phone. There is a special exception for the " +
          "AI Companion where these files are written to /sdcard/AppInventor/data to facilitate " +
          "debugging. Note that this block will overwrite a file if it already exists." +
          "\n\nIf you want to add content to a file use the append block.")
  public void SaveFile(String text, String fileName){
    Write(fileName,text,false);
  }
  /**
   * Appends text to the end of a file. Creates the file if it does not already exist. See the help
   * text under {@link #SaveFile(String, String)} for information about where files are written.
   * On success, the {@link #AfterFileSaved(String)} event will run.
   *
   * @internaldoc
   * Calls the Write function to write to the file asynchronously to prevent
   * the UI from hanging when there is a large write.
   *
   * @param text the text to be stored
   * @param fileName the file to which the text will be stored
   */
  @SimpleFunction(description = "Appends text to the end of a file storage, creating the file if it does not exist. " +
          "See the help text under SaveFile for information about where files are written.")
  public void AppendToFile(String text, String fileName) {
    Write(fileName, text, true);
  }

  /**
   * Reads text from a file in storage. Prefix the `fileName`{:.text.block} with `/` to read from a
   * specific file on the SD card (for example, `/myFile.txt` will read the file
   * `/sdcard/myFile.txt`). To read assets packaged with an application (also works for the
   * Companion) start the `fileName`{:.text.block} with `//` (two slashes). If a
   * `fileName`{:.text.block} does not start with a slash, it will be read from the application's
   * private storage (for packaged apps) and from `/sdcard/AppInventor/data` for the Companion.
   *
   * @param fileName the file from which the text is read
   */
  @SimpleFunction(description = "Reads text from a file in storage. " +
          "Prefix the filename with / to read from a specific file on the SD card. " +
          "for instance /myFile.txt will read the file /sdcard/myFile.txt. To read " +
          "assets packaged with an application (also works for the Companion) start " +
          "the filename with // (two slashes). If a filename does not start with a " +
          "slash, it will be read from the applications private storage (for packaged " +
          "apps) and from /sdcard/AppInventor/data for the Companion.")
  public void ReadFrom(final String fileName){
    try{
      InputStream inputStream;
      if(fileName.startsWith("//")){
        inputStream = form.openAsset(fileName.substring(2));
        final InputStream asyncInputStream = inputStream;
        AsynchUtil.runAsynchronously(new Runnable() {
          @Override
          public void run() {
            AsyncRead(asyncInputStream,fileName);
          }
        });
        return;
      }else if(fileName.startsWith("/")){
        if (fileName.contains(context.getApplicationContext().getPackageName())) {
          final String filename = completeFileName(fileName);
          inputStream = new FileInputStream(filename);
          final InputStream asyncInputStream = inputStream;
          AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
              AsyncRead(asyncInputStream,filename);
            }
          });
        }else{
          if (!hasReadAccess) {
            new Handler().post(new Runnable() {
              @Override
              public void run() {
                form.askPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                        new PermissionResultHandler() {
                          @Override
                          public void HandlePermissionResponse(String permission, boolean granted) {
                            hasReadAccess = granted;
                          }
                        });
              }
            });
          }
          if (hasReadAccess) {
            inputStream = FileUtil.openFile(completeFileName(fileName));
            final InputStream asyncInputStream = inputStream;
            AsynchUtil.runAsynchronously(new Runnable() {
              @Override
              public void run() {
                AsyncRead(asyncInputStream, fileName);
              }
            });
          }
        }
      }else{
        final String filename = context.getExternalFilesDir(null).getPath() + "/" + fileName;
        inputStream = new FileInputStream(filename);
        final InputStream asyncInputStream = inputStream;
        AsynchUtil.runAsynchronously(new Runnable() {
          @Override
          public void run() {
            AsyncRead(asyncInputStream,filename);
          }
        });
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * Deletes a file from storage. Prefix the `fileName`{:.text.block} with `/` to delete a specific
   * file in the SD card (for example, `/myFile.txt` will delete the file `/sdcard/myFile.txt`).
   * If the `fileName`{:.text.block} does not begin with a `/`, then the file located in the
   * program's private storage will be deleted. Starting the `fileName`{:.text.block} with `//` is
   * an error because asset files cannot be deleted.
   *
   * @param fileName the file to be deleted
   */
  @SimpleFunction(description = "Deletes a file from storage. " +
          "Prefix the filename with / to delete a specific file in the SD card, for instance /myFile.txt. " +
          "will delete the file /sdcard/myFile.txt. If the file does not begin with a /, then the file " +
          "located in the programs private storage will be deleted. Starting the file with // is an error " +
          "because assets files cannot be deleted.")
  public void Delete(final String fileName) {
    if(fileName.startsWith("/")) {
      if (fileName.contains(context.getApplicationContext().getPackageName())) {
        final String file = completeFileName(fileName);
        AsynchUtil.runAsynchronously(new Runnable() {
          public void run() {
            delete(file);
          }
        });
      }else{
        if(!hasWriteAccess){
          new Handler().post(new Runnable() {
            @Override
            public void run() {
              form.askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                      new PermissionResultHandler() {
                        @Override
                        public void HandlePermissionResponse(String permission, boolean granted) {
                          hasWriteAccess = granted;
                        }
                      });
            }
          });
        }
        if (hasWriteAccess) {
          AsynchUtil.runAsynchronously(new Runnable() {
            public void run() {
              delete(completeFileName(fileName));
            }
          });
        }
      }
    }else{
      final String file = context.getExternalFilesDir(null).getPath() + "/" + fileName;
      AsynchUtil.runAsynchronously(new Runnable() {
        public void run() {
          delete(file);
        }
      });
    }
  }
  public void delete(String fileName){
    final boolean success = new java.io.File(fileName).delete();
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        AfterFileDeleted(success);
      }
    });
  }

  /**
   * Writes to the specified file.
   * @param filename the file to write
   * @param text to write to the file
   * @param append determines whether text should be appended to the file,
   * or overwrite the file
   */
  public void Write(final String filename, final String text, final boolean append){
    if(!context.getExternalFilesDir(null).exists()){
      context.getExternalFilesDir(null).mkdirs();
    }
    if(filename.startsWith("/")) {
      if (filename.contains(context.getApplicationContext().getPackageName())) {
        final String file = completeFileName(filename);
        AsynchUtil.runAsynchronously(new Runnable() {
          public void run() {
            save(file,text,append);
          }
        });
      }else{
        if(!hasWriteAccess){
          new Handler().post(new Runnable() {
            @Override
            public void run() {
              form.askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                      new PermissionResultHandler() {
                        @Override
                        public void HandlePermissionResponse(String permission, boolean granted) {
                          hasWriteAccess = granted;
                        }
                      });
            }
          });
        }
        if (hasWriteAccess) {
          AsynchUtil.runAsynchronously(new Runnable() {
            public void run() {
              save(completeFileName(filename),text,append);
            }
          });
        }
      }
    }else{
      final String file = context.getExternalFilesDir(null).getPath() + "/" + filename;
      AsynchUtil.runAsynchronously(new Runnable() {
        public void run() {
          save(file,text,append);
        }
      });
    }
  }
  public void save(final String filename,final String text,final boolean append){
    final java.io.File file = new java.io.File(filename);
    if(!file.exists()){
      try{
        file.createNewFile();
      }catch(Exception e){
        e.printStackTrace();
      }
    }
    try {
      FileOutputStream fileWriter = new FileOutputStream(file,append);
      OutputStreamWriter out = new OutputStreamWriter(fileWriter);
      out.write(text);
      out.flush();
      out.close();
      fileWriter.close();
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          AfterFileSaved(filename);
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Replace Windows-style CRLF with Unix LF as String. This allows
   * end-user to treat Windows text files same as Unix or Mac. In
   * future, allowing user to choose to normalize new lines might also
   * be nice - in case someone really wants to detect Windows-style
   * line separators, or save a file which was read (and expect no
   * changes in size or checksum).
   * @param s to convert
   */

  private String normalizeNewLines(String s) {
    return s.replaceAll("\r\n", "\n");
  }


  /**
   * Asynchronously reads from the given file. Calls the main event thread
   * when the function has completed reading from the file.
   * @param fileInput the stream to read from
   * @param fileName the file to read
   * @throws FileNotFoundException
   * @throws IOException when the system cannot read the file
   */
  private void AsyncRead(InputStream fileInput, final String fileName) {
    InputStreamReader input = null;
    try {
      input = new InputStreamReader(fileInput);
      StringWriter output = new StringWriter();
      char [] buffer = new char[BUFFER_LENGTH];
      int offset = 0;
      int length = 0;
      while ((length = input.read(buffer, offset, BUFFER_LENGTH)) > 0) {
        output.write(buffer, 0, length);
      }
      final String text = normalizeNewLines(output.toString());
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          GotText(text);
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Event indicating that the contents from the file have been read.
   *
   * @param text read from the file
   */
  @SimpleEvent (description = "Event indicating that the contents from the file have been read.")
  public void GotText(String text) {
    // invoke the application's "GotText" event handler.
    EventDispatcher.dispatchEvent(this, "GotText", text);
  }

  /**
   * Event indicating that the contents of the file have been written.
   *
   * @param fileName the name of the written file
   */
  @SimpleEvent (description = "Event indicating that the contents of the file have been written.")
  public void AfterFileSaved(String fileName) {
    // invoke the application's "AfterFileSaved" event handler.
    EventDispatcher.dispatchEvent(this, "AfterFileSaved", fileName);
  }
  @SimpleEvent(description="")
  public void AfterFileDeleted(boolean successful){
    EventDispatcher.dispatchEvent(this, "AfterFileDeleted", successful);
  }

  /**
   * Returns file path.
   *
   * @param filename the file used to construct the file path
   */
  private String AbsoluteFileName(String filename) {
    if (filename.startsWith("/")) {
      return QUtil.getExternalStoragePath(form) + filename;
    } else {
      java.io.File dirPath;
      if (form.isRepl()) {
        dirPath = new java.io.File(QUtil.getReplDataPath(form, false));
      } else {
        dirPath = form.getFilesDir();
      }
      if (!dirPath.exists()) {
        dirPath.mkdirs();           // Make sure it exists
      }
      return dirPath.getPath() + "/" + filename;
    }
  }
  public String completeFileName(String fileName) {
    if(fileName.isEmpty()){
      return fileName;
    }else{
      java.io.File sd = new java.io.File(QUtil.getExternalStoragePath(form));
      String completeFileName = fileName;
      if (fileName.startsWith("file:///")) {
        completeFileName = fileName.substring(7);
      } else if (fileName.startsWith("//")) {
        fileName = fileName.substring(2);
        if (isRepl) {
          completeFileName = new java.io.File(QUtil.getReplDataPath(form, false)) + fileName;
        }
      } else if (fileName.startsWith("/")) {
        if (!fileName.startsWith(sd.toString())){
          completeFileName = sd.getPath() + fileName;
        }
      } else {
        completeFileName = sd.getPath() + java.io.File.separator + fileName;
      }
      if (!sd.exists()){
        sd.mkdirs();
      }
      return completeFileName;
    }
  }

}
