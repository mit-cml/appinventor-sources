package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@DesignerComponent(version =  YaVersion.SAF_COMPONENT_VERSION,
        description = "A non-visible component to access files using Storage Access Framework",
        category = ComponentCategory.STORAGE,
        nonVisible = true,
        androidMinSdk = 21,
        iconName = "images/saf.png")
@SimpleObject
public class SAF extends AndroidNonvisibleComponent implements ActivityResultListener {
    private final Activity activity;
    private int intentReqCode = 0;
    private final ContentResolver contentResolver;

    public SAF(ComponentContainer container) {
        super(container.$form());
        activity = container.$context();
        contentResolver = activity.getContentResolver();
    }

    @Override
    public void resultReturned(int requestCode, int resultCode, Intent intent) {
        if (intentReqCode == requestCode) {
            GotUri(intent.getData(), intent.getData().toString());
        }
    }

    private int getIntentReqCode(){
        if (intentReqCode == 0){
            this.intentReqCode = form.registerForActivityResult(this);
        }
        return intentReqCode;
    }

    private void postError(final String method,final String message){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ErrorOccurred(method,message);
            }
        });
    }

    @SimpleEvent(description = "Event indicating error/exception has occurred and returns origin method and error message.")
    public void ErrorOccurred(String methodName,String errorMessage){
        EventDispatcher.dispatchEvent(this,"ErrorOccurred",methodName,errorMessage);
    }
    @SimpleProperty(description = "Returns mime type of document dir.")
    public String DocumentDirMimeType() {
        return DocumentsContract.Document.MIME_TYPE_DIR;
    }

    @SimpleProperty(description = "Flag to get write permission.")
    public int FlagGrantReadPermission() {
        return Intent.FLAG_GRANT_READ_URI_PERMISSION;
    }

    @SimpleProperty(description = "Flag to get read permission.")
    public int FlagGrantWritePermission() {
        return Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
    }

    @SimpleFunction(description = "Combines two flags and returns resulting flag.")
    public int CreateFlags(int f1, int f2) {
        return f1 | f2;
    }

    @SimpleFunction(description = "Convert uri to string.")
    public String UriToString(Object uri) {
        return uri.toString();
    }

    @SimpleFunction(description = "Creates Uri from string.")
    public Object StringToUri(String uriString) {
        return Uri.parse(uriString);
    }

    @SimpleFunction(description = "Prompts user to select a document tree.")
    public void OpenDocumentTree(String title, String initialDir) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (!initialDir.isEmpty()) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(initialDir));
        }
        activity.startActivityForResult(Intent.createChooser(intent, title), getIntentReqCode());
    }

    @SimpleFunction(description = "Prompts user to select a single document.")
    public void OpenSingleDocument(String title,String type, YailList extraMimeTypes) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (!type.isEmpty()) {
            intent.setType(type);
        }
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (!extraMimeTypes.isEmpty()) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes.toStringArray());
        }
        activity.startActivityForResult(Intent.createChooser(intent, title), getIntentReqCode());
    }

    @SimpleFunction(description = "Take a persistable URI permission grant that has been offered. Once taken, the permission grant will be remembered across device reboots.")
    public void TakePersistableUriPermission(Object uri, int flags) {
        activity.getContentResolver().takePersistableUriPermission((Uri) uri, flags);
    }

    @SimpleFunction(description = "Returns whether given uri is a tree uri.")
    public boolean IsTreeUri(String uriString) {
        return DocumentsContract.isTreeUri(Uri.parse(uriString));
    }

    @SimpleFunction(description = "Returns whether given uri is a document uri.")
    public boolean IsDocumentUri(String uriString) {
        return DocumentsContract.isDocumentUri(activity, Uri.parse(uriString));
    }

    @SimpleFunction(description = "Returns whether second uri is child of first uri.")
    public boolean IsChildDocumentUri(String parentUri, String childUri) {
        try {
            return DocumentsContract.isChildDocument(activity.getContentResolver(),
                    Uri.parse(parentUri),
                    Uri.parse(childUri));
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            throw new YailRuntimeError(e.getMessage(), "SAF");
        }
    }

    @SimpleFunction(description = "Returns document id of tree uri.")
    public String GetTreeDocumentId(String uriString) {
        return DocumentsContract.getTreeDocumentId(Uri.parse(uriString));
    }

    @SimpleFunction(description = "Returns document id of an uri.")
    public String GetDocumentId(String uriString) {
        return DocumentsContract.getDocumentId(Uri.parse(uriString));
    }

    @SimpleFunction(description = "Builds document uri using tree uri and document id.")
    public String BuildDocumentUriUsingTree(String treeUri, String documentId) {
        return DocumentsContract.buildDocumentUriUsingTree(Uri.parse(treeUri), documentId).toString();
    }

    @SimpleFunction(description = "Builds child documents uri using tree (parent document) uri and its parent document's id.")
    public String BuildChildDocumentsUriUsingTree(String treeUri, String parentDocumentId) {
        return DocumentsContract.buildChildDocumentsUriUsingTree(Uri.parse(treeUri), parentDocumentId).toString();
    }

    @SimpleFunction(description = "Returns mime type of given document uri.")
    public String MimeType(final String documentUri) {
        try (Cursor cursor = activity.getContentResolver().query(Uri.parse(documentUri),
                new String[]{DocumentsContract.Document.COLUMN_MIME_TYPE},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            postError("MimeType",e.getMessage());
        }
        return "";
    }

    @SimpleFunction(description = "Returns whether document can be copied or not.")
    public boolean IsCopySupported(final String documentUri) {
        return isFlagTrue("IsCopySupported",
                Uri.parse(documentUri),
                DocumentsContract.Document.FLAG_SUPPORTS_COPY);
    }

    @SimpleFunction(description = "Returns whether document is movable or not.")
    public boolean IsMoveSupported(final String documentUri) {
        return isFlagTrue("IsMoveSupported",
                Uri.parse(documentUri),
                DocumentsContract.Document.FLAG_SUPPORTS_MOVE);
    }

    @SimpleFunction(description = "Returns whether document is deletable or not.")
    public boolean IsDeleteSupported(final String documentUri) {
        return isFlagTrue("IsDeleteSupported",
                Uri.parse(documentUri),
                DocumentsContract.Document.FLAG_SUPPORTS_DELETE);
    }

    @SimpleFunction(description = "Returns whether document supports renaming.")
    public boolean IsRenameSupported(final String documentUri) {
        return isFlagTrue("IsRenameSupported",
                Uri.parse(documentUri),
                DocumentsContract.Document.FLAG_SUPPORTS_RENAME);
    }

    private boolean isFlagTrue(String method,Uri uri, int flag) {
        try (Cursor cursor = contentResolver.query(uri,
                new String[]{DocumentsContract.Document.COLUMN_FLAGS},
                null,
                null,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0).contains(String.valueOf(flag));
            }
        } catch (Exception e) {
            postError(method,e.getMessage());
        }
        return false;
    }

    @SimpleFunction(description = "Returns display name of given document uri.")
    public String DisplayName(final String documentUri) {
        try (Cursor cursor = contentResolver.query(Uri.parse(documentUri),
                new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            postError("DisplayName",e.getMessage());
        }
        return "";
    }

    @SimpleFunction(description = "Returns size (in bytes) of given document uri.")
    public String Size(final String documentUri) {
        try (Cursor cursor = contentResolver.query(Uri.parse(documentUri),
                new String[]{DocumentsContract.Document.COLUMN_SIZE},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            postError("Size",e.getMessage());
        }
        return "";
    }

    @SimpleFunction(description = "Returns last modified time/epoch timestamp of given document uri.")
    public String LastModifiedTime(final String documentUri) {
        try (Cursor cursor = contentResolver.query(Uri.parse(documentUri),
                new String[]{DocumentsContract.Document.COLUMN_LAST_MODIFIED},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            postError("LastModifiedTime",e.getMessage());
        }
        return "";
    }

    @SimpleFunction(description = "Creates a new and empty document.If document already exists then an incremental value will be automatically suffixed.")
    public void CreateDocument(final String parentDocumentUri, final String fileName, final String mimeType) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    final String uri = DocumentsContract.createDocument(contentResolver,
                            Uri.parse(parentDocumentUri),
                            mimeType,
                            fileName).toString();
                    postCreateResult(uri);
                } catch (Exception e) {
                    postCreateResult(e.getMessage());
                }
            }
        });
    }

    private void postCreateResult(final String uriString) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DocumentCreated(uriString);
            }
        });
    }

    @SimpleEvent(description = "Event invoked after creating document.Returns document's uri if operation was successful else returns error message.")
    public void DocumentCreated(String uriString) {
        EventDispatcher.dispatchEvent(this, "DocumentCreated", uriString);
    }

    @SimpleFunction(description = "Writes text to given document")
    public void WriteText(final String documentUri, final String text) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                if (!MimeType(documentUri).equals(DocumentDirMimeType())) {
                    String res;
                    try {
                        OutputStream fileOutputStream = contentResolver.openOutputStream(Uri.parse(documentUri));
                        res = writeToOutputStream(fileOutputStream, text);
                        res = res.isEmpty() ? documentUri : res;
                    } catch (Exception e) {
                        res = e.getMessage();
                    }
                    postWriteResult(res);
                }else {
                    postError("WriteText","Can't write text to dir");
                }
            }
        });
    }

    private String writeToOutputStream(OutputStream fileOutputStream, String content) {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(fileOutputStream);
            writer.write(content);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    @SimpleFunction(description = "Writes bytes to given document")
    public void WriteBytes(final String documentUri, final Object bytes) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                if (!MimeType(documentUri).equals(DocumentDirMimeType())) {
                    try {
                        OutputStream outputStream = contentResolver.openOutputStream(Uri.parse(documentUri));
                        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                        arrayOutputStream.write((byte[]) bytes);
                        arrayOutputStream.writeTo(outputStream);
                        postWriteResult(documentUri);
                    } catch (Exception e) {
                        postWriteResult(e.getMessage());
                    }
                }else {
                    postError("WriteBytes","Can't write bytes to dir");
                }
            }
        });
    }

    private void postWriteResult(final String response) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GotWriteResult(response);
            }
        });
    }

    @SimpleEvent(description = "Event invoked after writing to document.Returns document's uri if operation was successful else returns error message")
    public void GotWriteResult(String response) {
        EventDispatcher.dispatchEvent(this, "GotWriteResult", response);
    }
/*
    @SimpleFunction()
    public void SaveImageToDocumentUri(final Image image,final String documentUri,final String format,final int quality) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = ((BitmapDrawable) ((ImageView) image.getView()).getDrawable()).getBitmap();
                    bitmap.compress(Bitmap.CompressFormat.valueOf(format.toUpperCase()),
                            quality,
                            contentResolver.openOutputStream(Uri.parse(documentUri)));
                    postSaveImgResult(documentUri);
                } catch (Exception e) {
                    //e.printStackTrace();
                    postSaveImgResult(e.getMessage());
                }
            }
        });
    }

    private void postSaveImgResult(final String res) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GotSaveImageResult(res);
            }
        });
    }

    @SimpleEvent()
    public void GotSaveImageResult(String response) {
        EventDispatcher.dispatchEvent(this, "GotSaveImageResult", response);
    }
 */
    @SimpleFunction(description = "Tries to delete document and returns result.")
    public boolean DeleteDocument(String documentUri) {
        try {
            return DocumentsContract.deleteDocument(contentResolver,
                    Uri.parse(documentUri));
        } catch (Exception e) {
            postError("DeleteDocument",e.getMessage());
        }
        return false;
    }

    @SimpleFunction(description = "Reads from given document as text")
    public void ReadText(final String documentUri) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                if (!MimeType(documentUri).equals(DocumentDirMimeType())) {
                    String res;
                    try {
                        res = readFromInputStream(contentResolver.openInputStream(Uri.parse(documentUri)));
                    } catch (FileNotFoundException e) {
                        res = e.getMessage();
                    }
                    postReadResult(res);
                }else {
                    postError("ReadText","Can't read text from dir");
                }
            }
        });
    }

    private String readFromInputStream(InputStream fileInputStream) {
        InputStreamReader input = new InputStreamReader(fileInputStream);
        try {
            StringWriter output = new StringWriter();
            int BUFFER_LENGTH = 4096;
            char[] buffer = new char[BUFFER_LENGTH];
            int offset = 0;
            int length;
            while ((length = input.read(buffer, offset, BUFFER_LENGTH)) > 0) {
                output.write(buffer, 0, length);
            }
            return normalizeNewLines(output.toString());
        } catch (Exception e) {
            //e.printStackTrace();
            return e.getMessage();
        } finally {
            try {
                input.close();
                /*
                fileInputStream.close();
                 */
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @SimpleFunction(description = "Reads content of document as bytes")
    public void ReadBytes(final String documentUri) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                if (!MimeType(documentUri).equals(DocumentDirMimeType())) {
                    try {
                        InputStream inputStream = contentResolver.openInputStream(Uri.parse(documentUri));
                        byte[] byteArray = new byte[Integer.parseInt(Size(documentUri))];
                        inputStream.read(byteArray);
                        inputStream.close();
                        postReadResult(byteArray);
                    } catch (Exception e) {
                        postReadResult(e.getMessage());
                    }
                }else {
                    postError("ReadBytes","Can't read bytes from dir");
                }
            }
        });
    }

    @SimpleEvent(description = "Event invoked after reading from document.Returns content if operation was successful else returns error message.")
    public void GotReadResult(Object result) {
        EventDispatcher.dispatchEvent(this, "GotReadResult", result);
    }

    private void postReadResult(final Object r) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GotReadResult(r);
            }
        });
    }

    private String normalizeNewLines(String s) {
        return s.replaceAll("\r\n", "\n");
    }

    @SimpleFunction(description = "Returns whether read is granted for given uri.")
    public boolean IsReadGranted(String uri) {
        for (UriPermission uri1 : contentResolver.getPersistedUriPermissions()) {
            String str = uri1.getUri().toString();
            if (uri.equalsIgnoreCase(str)) {
                return uri1.isReadPermission();
            }
        }
        return false;
    }

    @SimpleFunction(description = "Relinquish a persisted URI permission grant.")
    public void ReleasePermission(String uri, int flags) {
        contentResolver.releasePersistableUriPermission(Uri.parse(uri), flags);
    }

    @SimpleFunction(description = "Returns whether write is granted for given uri.")
    public boolean IsWriteGranted(String uri) {
        for (UriPermission uri1 : contentResolver.getPersistedUriPermissions()) {
            String str = uri1.getUri().toString();
            if (uri.equalsIgnoreCase(str)) {
                return uri1.isWritePermission();
            }
        }
        return false;
    }

    @SimpleEvent(description = "Event invoked when user selects a document or tree from SAF file picker.")
    public void GotUri(Object uri, String uriString) {
        EventDispatcher.dispatchEvent(this, "GotUri", uri, uriString);
    }

    @SimpleFunction(description = "Tries to list files from given document dir.")
    public void ListFiles(final String dirUri, final String dirDocumentId) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                final List<String> list = listFiles(Uri.parse(dirUri), dirDocumentId);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GotFilesList(list);
                    }
                });
            }
        });
    }

    // taken from https://stackoverflow.com/questions/41096332/issues-traversing-through-directory-hierarchy-with-android-storage-access-framew
    private List<String> listFiles(Uri treeUri, String documentId) {
        List<String> uriList = new ArrayList<>();
        Uri uriFolder = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId);
        try (Cursor cursor = contentResolver.query(uriFolder,
                new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                Uri uriFile = DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(0));
                uriList.add(uriFile.toString());
                while (cursor.moveToNext()) {
                    uriFile = DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(0));
                    uriList.add(uriFile.toString());
                }
            }
        } catch (Exception e) {
            postError("ListFiles",e.getMessage());
        }
        return uriList;
    }

    @SimpleEvent(description = "Event invoked after getting files list.")
    public void GotFilesList(List<String> filesList) {
        EventDispatcher.dispatchEvent(this, "GotFilesList", filesList);
    }

    @SimpleFunction(description = "Tries to copy document from source uri to target dir.")
    public void CopyDocument(final String sourceUri, final String targetParentUri) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                boolean successful = true;
                String response = "";
                try {
                    response = DocumentsContract.copyDocument(contentResolver,
                            Uri.parse(sourceUri),
                            Uri.parse(targetParentUri)).toString();
                } catch (Exception e) {
                    successful = false;
                    response = e.getMessage();
                }
                postCopyResult(successful, response);
            }
        });
    }

    private void postCopyResult(final boolean successful, final String response) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GotCopyResult(successful, response);
            }
        });
    }

    @SimpleEvent(description = "Event invoked after getting copy document result.Response will be target document's uri if operation was successful else returns error message.")
    public void GotCopyResult(boolean successful, String response) {
        EventDispatcher.dispatchEvent(this, "GotCopyResult", successful, response);
    }

    @SimpleFunction(description = "Tries to move document from source uri to target dir.")
    public void MoveDocument(final String sourceUri, final String sourceParentUri, final String targetParentUri) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                boolean successful = true;
                String response = "";
                try {
                    response = DocumentsContract.moveDocument(contentResolver,
                            Uri.parse(sourceUri),
                            Uri.parse(sourceParentUri),
                            Uri.parse(targetParentUri)).toString();
                } catch (Exception e) {
                    successful = false;
                    response = e.getMessage();
                }
                postMoveResult(successful, response);
            }
        });
    }

    private void postMoveResult(final boolean successful, final String response) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GotMoveResult(successful, response);
            }
        });
    }

    @SimpleEvent(description = "Event invoked after getting move document result.Response will be target document's uri if operation was successful else returns error message.")
    public void GotMoveResult(boolean successful, String response) {
        EventDispatcher.dispatchEvent(this, "GotMoveResult", successful, response);
    }

    @SimpleFunction(description = "Tries to rename a document and returns updated uri.")
    public String RenameDocument(final String documentUri, final String displayName) {
        try {
            return DocumentsContract.renameDocument(contentResolver,
                    Uri.parse(documentUri),
                    displayName).toString();
        } catch (FileNotFoundException e) {
            postError("RenameDocument",e.getMessage());
            return "";
        }
    }
}
