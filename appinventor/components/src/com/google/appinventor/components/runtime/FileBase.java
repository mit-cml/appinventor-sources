package com.google.appinventor.components.runtime;

import android.Manifest;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;

import java.io.*;

@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public abstract class FileBase extends AndroidNonvisibleComponent implements Component {
    public static final String NO_ASSETS = "No_Assets";
    protected static final String LOG_TAG = "FileComponent";
    private static final int BUFFER_LENGTH = 4096;

    protected final Activity activity;
    protected boolean isRepl = false;

    /**
     * Creates a new FileBase component.
     * @param container the Form that this component is contained in.
     */
    protected FileBase(ComponentContainer container) {
        super(container.$form());
        if (form instanceof ReplForm) { // Note: form is defined in our superclass
            isRepl = true;
        }
        activity = (Activity) container.$context();
    }

    public void ReadFromFile(final String fileName) {
        form.askPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new PermissionResultHandler() {
            @Override
            public void HandlePermissionResponse(String permission, boolean granted) {
                if (granted) {
                    try {
                        InputStream inputStream;
                        if (fileName.startsWith("//")) {
                            inputStream = form.openAsset(fileName.substring(2));
                        } else {
                            String filepath = AbsoluteFileName(fileName);
                            Log.d(LOG_TAG, "filepath = " + filepath);
                            inputStream = FileUtil.openFile(filepath);
                        }

                        final InputStream asyncInputStream = inputStream;

                        AsyncRead(inputStream, fileName);
                    } catch (PermissionException e) {
                        form.dispatchPermissionDeniedEvent(FileBase.this, "ReadFrom", e);
                    } catch (FileNotFoundException e) {
                        Log.e(LOG_TAG, "FileNotFoundException", e);
                        form.dispatchErrorOccurredEvent(FileBase.this, "ReadFrom",
                                ErrorMessages.ERROR_CANNOT_FIND_FILE, fileName);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "IOException", e);
                        form.dispatchErrorOccurredEvent(FileBase.this, "ReadFrom",
                                ErrorMessages.ERROR_CANNOT_FIND_FILE, fileName);
                    }
                } else {
                    form.dispatchPermissionDeniedEvent(FileBase.this, "ReadFrom", permission);
                }
            }
        });
    }

    /**
     * Returns absolute file path.
     *
     * @param filename the file used to construct the file path
     */
    protected String AbsoluteFileName(String filename) {
        if (filename.startsWith("/")) {
            return Environment.getExternalStorageDirectory().getPath() + filename;
        } else {
            java.io.File dirPath = activity.getFilesDir();
            if (isRepl) {
                String path = Environment.getExternalStorageDirectory().getPath() + "/AppInventor/data/";
                dirPath = new java.io.File(path);
                if (!dirPath.exists()) {
                    dirPath.mkdirs();           // Make sure it exists
                }
            }
            return dirPath.getPath() + "/" + filename;
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
     * Reads from the specified InputStream and returns the contents as a String.
     * @param fileInput  the stream to read from
     * @return  Contents of the file (as a String)
     * @throws IOException  when the system cannot read the file
     */
    public String readFromInputString(InputStream fileInput) throws IOException {
        InputStreamReader input = new InputStreamReader(fileInput);
        StringWriter output = new StringWriter();
        char [] buffer = new char[BUFFER_LENGTH];
        int offset = 0;
        int length = 0;
        while ((length = input.read(buffer, offset, BUFFER_LENGTH)) > 0) {
            output.write(buffer, 0, length);
        }

        // Now that we have the file as a String,
        // normalize any line separators to avoid compatibility between Windows and Mac
        // text files. Users can expect \n to mean a line separator regardless of how
        // file was created. Currently only doing this for files opened locally - not files we pull
        // from other places like URLs.

        final String text = normalizeNewLines(output.toString());

        try {
            input.close();
        } catch (IOException e) {
            // do nothing...
        }

        return text;
    }

    protected abstract void AsyncRead(InputStream inputStream, String fileName);
}
