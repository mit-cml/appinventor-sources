// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;

import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.errors.StopBlocksExecution;

import com.google.appinventor.components.runtime.imagebot.ImageBotToken;

import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.Base58Util;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.IOUtils;
import com.google.appinventor.components.runtime.util.MediaUtil;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.IOException;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * The ImageBot is a non-visible component that uses DALL-E 2 to create and edit images. You must
 * supply your own OpenAI API key for this component by setting its ApiKey property in the blocks.
 */
@SimpleObject
@DesignerComponent(
    version = YaVersion.IMAGEBOT_COMPONENT_VERSION,
    iconName = "images/paintpalette.png",
    nonVisible = true,
    category = ComponentCategory.EXPERIMENTAL,
    androidMinSdk = 9
)
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "protobuf-java-3.0.0.jar")
public class ImageBot extends AndroidNonvisibleComponent {
  public static final String LOG_TAG = ImageBot.class.getSimpleName();
  private String apiKey = "";
  private boolean invert = true;
  private int size = 256;
  private String token;         // MIT Generated access token

  private static final String IMAGEBOT_SERVICE_URL =
    "https://chatbot.appinventor.mit.edu/image/v1";

  public ImageBot(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * The MIT Access token to use. MIT App Inventor will automatically fill this
   * value in. You should not need to change it.
   *
   */

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(description = "The MIT Access token to use. MIT App Inventor will automatically fill this " +
    "value in. You should not need to change it.",
    userVisible = true)
  public void Token(String token) {
    this.token = token;
  }

  /**
   * Specifies the ApiKey used to authenticate with the ImageBot.
   *
   * @param apiKey the API key to use for requests
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void ApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  /**
   * Specifies whether the mask used for editing should have its alpha channel inverted.
   *
   * @param invert true if the alpha channel should be inverted, otherwise false
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void InvertMask(boolean invert) {
    this.invert = invert;
  }

  @SimpleProperty
  public boolean InvertMask() {
    return invert;
  }

  /**
   * Specifies the size of the generated image. Can be one of 256, 512, or 1024.
   *
   * @param size the desired image size
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "256")
  @SimpleProperty
  public void Size(int size) {
    this.size = size;
  }

  @SimpleProperty
  public int Size() {
    return size;
  }

  // region Methods

  /**
   * Create an image using the given description.
   *
   * @param description a description of the image to create
   */
  @SimpleFunction
  public void CreateImage(final String description) {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        doCreateImage(description);
      }
    });
  }

  /**
   * Edit the source image using the given description. Editable areas of the image should have
   * a transparent alpha. The source can be a Canvas component, an Image component, or a string
   * representing the path to a file.
   *
   * @param source the source image
   * @param description the description of how to edit the image
   */
  @SimpleFunction
  public void EditImage(Object source, final String description) {
    try {
      // Load the image on the main thread. This isn't ideal but prevents the image from being
      // edited after this method is called but before the image is copied.
      final Bitmap bitmap = loadImage(source);
      if (bitmap != null) {
        AsynchUtil.runAsynchronously(new Runnable() {
          @Override
          public void run() {
            doEditImage(bitmap, null, description);
          }
        });
      } else {
        form.androidUIHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            ErrorOccurred(-1, "Invalid input to EditImage");
          }
        }, 0);
        throw new StopBlocksExecution();
      }
    } catch (IOException e) {
      Log.e(LOG_TAG, "Unable to read source image", e);
    }
  }

  /**
   * Edit the imageSource using the given description. The editable area of the image should be
   * indicated by the maskSource. The sources can be a Canvas, an Image, or a string
   * representing the path to a file.
   *
   * @param imageSource the source image
   * @param maskSource the edit mask for the image
   * @param prompt the description of how to edit the image
   */
  @SimpleFunction
  public void EditImageWithMask(Object imageSource, Object maskSource, final String prompt) {
    try {
      final Bitmap bitmap = loadImage(imageSource);
      final Bitmap mask = loadMask(maskSource);
      if (bitmap == null) {
        return;
      }
      if (mask == null) {
        return;
      }
      AsynchUtil.runAsynchronously(new Runnable() {
        @Override
        public void run() {
          doEditImage(bitmap, mask, prompt);
        }
      });
    } catch (IOException e) {
      Log.e(LOG_TAG, "Unable to read source image", e);
    }
  }

  // endregion

  // region Events

  /**
   * The ImageCreated event will be run when the ImageBot successfully creates an image.
   *
   * @param fileName the location of the created PNG file
   */
  @SimpleEvent
  public void ImageCreated(String fileName) {
    EventDispatcher.dispatchEvent(this, "ImageCreated", fileName);
  }

  /**
   * The ImageCreated event will be run when the ImageBot successfully edits an image.
   *
   * @param fileName the location of the edited PNG file.
   */
  @SimpleEvent
  public void ImageEdited(String fileName) {
    EventDispatcher.dispatchEvent(this, "ImageEdited", fileName);
  }

  /**
   * The ErrorOccurred event will be run when an error occurs during processing, such as if you
   * forget to provide an API key or the server is overloaded.
   *
   * @param responseCode the HTTP status code returned by the server
   * @param responseText a description of the error
   */
  @SimpleEvent
  public void ErrorOccurred(int responseCode, String responseText) {
    if (!EventDispatcher.dispatchEvent(this, "ErrorOccurred", responseCode, responseText)) {
      form.dispatchErrorOccurredEvent(ImageBot.this, "ErrorOccurred",
              ErrorMessages.ERROR_IMAGEBOT_ERROR, responseCode, responseText);
    }
  }

  private class ImageException extends IOException {
    private int code;
    private String description;

    private ImageException(int code, String description) {
      this.code = code;
      this.description = description;
    }

    private String getResponseMessage() {
      return description;
    }

    private int getResponseCode() {
      return code;
    }
  }

  // endregion

  private void doCreateImage(String prompt) {
    try {
      String iToken;
      if (token != null && !token.equals("") && token.substring(0, 1).equals("%")) {
        iToken = token.substring(1);
      } else {
        iToken = token;
      }
      byte [] decodedToken = Base58Util.decode(iToken);
      ImageBotToken.token token = ImageBotToken.token.parseFrom(decodedToken);
      ImageBotToken.request.Builder builder = ImageBotToken.request.newBuilder()
        .setToken(token)
        .setSize("" + size)
        .setOperation(ImageBotToken.request.OperationType.CREATE)
        .setPrompt(prompt);
      if (apiKey != null && !apiKey.equals("")) {
        builder = builder.setApikey(apiKey);
      }
      ImageBotToken.request request = builder.build();
      try {
        final String response = sendRequest(request);
        form.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              ImageCreated(response);
            }
          });
      }
      catch (final ImageException e) {
        form.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              ErrorOccurred(e.getResponseCode(), e.getResponseMessage());
            }
          });
      }
    } catch (final Exception e) {
      form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            ErrorOccurred(404, e.toString());
          }
        });
    }
  };

  private void doEditImage(Bitmap source, Bitmap mask, String description) {
    ByteArrayOutputStream sourceBuffer = new ByteArrayOutputStream();
    source.compress(Bitmap.CompressFormat.PNG, 100, sourceBuffer);
    ByteString sourceString = ByteString.copyFrom(sourceBuffer.toByteArray());

    ByteArrayOutputStream maskBuffer = new ByteArrayOutputStream();
    mask.compress(Bitmap.CompressFormat.PNG, 100, maskBuffer);
    ByteString maskString = ByteString.copyFrom(maskBuffer.toByteArray());

    String iToken;
    if (token != null && !token.equals("") && token.substring(0, 1).equals("%")) {
      iToken = token.substring(1);
    } else {
      iToken = token;
    }
    ImageBotToken.token token;
    try {
      byte [] decodedToken = Base58Util.decode(iToken);
      token = ImageBotToken.token.parseFrom(decodedToken);
    } catch (IOException e) {
      form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            ErrorOccurred(403, "Invalid Token");
          }
        });
      return;
    }
    ImageBotToken.request.Builder builder = ImageBotToken.request.newBuilder()
      .setToken(token)
      .setSource(sourceString)
      .setMask(maskString)
      .setOperation(ImageBotToken.request.OperationType.EDIT)
      .setSize("" + size)
      .setPrompt(description);
    if (apiKey != null && !apiKey.equals("")) {
      builder = builder.setApikey(apiKey);
    }
    ImageBotToken.request request = builder.build();
    try {
      final String response = sendRequest(request);
      form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            ImageEdited(response);
          }
        });
    }
    catch (final ImageException e) {
      form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            ErrorOccurred(e.getResponseCode(), e.getResponseMessage());
          }
        });
    }
  };

  private String sendRequest(ImageBotToken.request request) throws ImageException {
    HttpsURLConnection connection = null;
    try {
      URL url = new URL(IMAGEBOT_SERVICE_URL);
      connection = (HttpsURLConnection) url.openConnection();
      if (connection != null) {
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        request.writeTo(connection.getOutputStream());
        final int responseCode = connection.getResponseCode();
        ImageBotToken.response response = ImageBotToken.response.parseFrom(connection.getInputStream());
        String returnText;
        if (responseCode == 200) {
          byte [] imageData = response.getImage().toByteArray();
          File outFile = getOutputFile();
          FileOutputStream out = new FileOutputStream(outFile);
          out.write(imageData);
          out.flush();
          out.close();
          String result = Uri.fromFile(outFile).toString();
          return result;
        }
        String errorMessage = IOUtils.readStreamAsString(connection.getErrorStream());
        throw new ImageException(responseCode, errorMessage);
      }
    } catch (IOException e) {
      throw new ImageException(404, e.toString());
    } finally {
      connection.disconnect();
    }
    throw new ImageException(404, "Could not connect to proxy server");
  }

  private Bitmap loadImage(Object source) throws IOException {
    Bitmap bitmap = null;
    Log.d(LOG_TAG, "loadImage source = " + source);
    if (source instanceof Canvas) {
      bitmap = ((Canvas) source).getBitmap();
    } else if (source instanceof Image) {
      bitmap = ((BitmapDrawable) ((Image) source).getView().getBackground()).getBitmap();
    } else {
      String sourceStr = source.toString();
      bitmap = MediaUtil.getBitmapDrawable(form, sourceStr).getBitmap();
    }
    if (bitmap != null) {
      if (bitmap.getWidth() == size && bitmap.getHeight() == size) {
        return bitmap;
      } else {
        bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
      }
    }
    return bitmap;
  }

  private Bitmap loadMask(Object mask) throws IOException {
    Bitmap bitmap = loadImage(mask);
    if (invert) {
      // Invert the alpha channel
      ColorMatrix transform = new ColorMatrix(new float[] {
          0, 0, 0, 0, 0,
          0, 0, 0, 0, 0,
          0, 0, 0, 0, 0,
          0, 0, 0, -1, 255
      });
      ColorMatrixColorFilter filter = new ColorMatrixColorFilter(transform);
      Paint paint = new Paint();
      paint.setColorFilter(filter);
      Bitmap newBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
      android.graphics.Canvas canvas = new android.graphics.Canvas(newBitmap);
      canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
      bitmap = newBitmap;
    }
    return bitmap;
  }

  private File getOutputFile() throws IOException {
    String tempdir = FileUtil.resolveFileName(form, "", form.DefaultFileScope());
    if (tempdir.startsWith("file://")) {
      tempdir = tempdir.substring(7);
    } else if (tempdir.startsWith("file:")) {
      tempdir = tempdir.substring(5);
    }
    Log.d(LOG_TAG, "tempdir = " + tempdir);
    File outFile = File.createTempFile("ImageBot", ".png", new File(tempdir));
    Log.d(LOG_TAG, "outfile = " + outFile);
    return outFile;
  }
}
