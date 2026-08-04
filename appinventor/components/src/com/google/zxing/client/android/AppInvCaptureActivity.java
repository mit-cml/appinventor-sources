// -*- mode: java; c-basic-offset: 2; -*-
/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.result.ResultButtonListener;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.result.ResultHandlerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class AppInvCaptureActivity extends Activity implements SurfaceHolder.Callback {

  private static final String TAG = AppInvCaptureActivity.class.getSimpleName();

  private static final long DEFAULT_INTENT_RESULT_DURATION_MS = 1500L;
  private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

  private static final String RETURN_CODE_PLACEHOLDER = "{CODE}";
  private static final String RETURN_URL_PARAM = "ret";
  private static final String RAW_PARAM = "raw";

  private static final Set<ResultMetadataType> DISPLAYABLE_METADATA_TYPES =
    EnumSet.of(ResultMetadataType.ISSUE_NUMBER,
      ResultMetadataType.SUGGESTED_PRICE,
      ResultMetadataType.ERROR_CORRECTION_LEVEL,
      ResultMetadataType.POSSIBLE_COUNTRY);

  private CameraManager cameraManager;
  private CaptureActivityHandler handler;
  private Result savedResultToShow;
  private ViewfinderView viewfinderView;
  private Result lastResult;
  private boolean hasSurface;
  private boolean copyToClipboard;
  private IntentSource source;
  private String sourceUrl;
  private String returnUrlTemplate;
  private boolean returnRaw;
  private Collection<BarcodeFormat> decodeFormats;
  private String characterSet;
  private BeepManager beepManager;
  private LinearLayout viewLayout;
  private FrameLayout frameLayout;
  private SurfaceView surfaceView;

  // Holds the android.window.OnBackInvokedCallback registered on API 33 and above, so that it can
  // be unregistered in onDestroy. Declared as Object so that this field does not name an API 33
  // class in a class that also runs on older devices.
  private Object backInvokedCallback = null;

  // Set once back has been acted on in a way that ends the activity. finish() does not take effect
  // immediately, so without this a second delivery of back before teardown completes would run the
  // whole sequence again.
  private boolean backHandled = false;

  ViewfinderView getViewfinderView() {
    return viewfinderView;
  }

  public Handler getHandler() {
    return handler;
  }

  CameraManager getCameraManager() {
    return cameraManager;
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    // Starting with Android 13 (API 33) the system dispatches back through
    // OnBackInvokedDispatcher, and KEYCODE_BACK stops arriving in onKeyDown. Apps targeting SDK 36
    // have predictive back enabled by default, so without this the back button would always exit
    // the scanner instead of restarting the preview after a decode. The onKeyDown handler further
    // down still covers API levels below 33, and continues to handle the focus, camera, and volume
    // keys on every API level.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      registerOnBackInvokedCallback();
    }

    Window window = getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    viewLayout = new LinearLayout(this);
    viewLayout.setOrientation(LinearLayout.HORIZONTAL);
    frameLayout = new FrameLayout(this);
    frameLayout.addView(viewLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
      ViewGroup.LayoutParams.FILL_PARENT));
    frameLayout.setBackgroundColor(0xFFFFFFFF); // COLOR_WHITE XXX

    setContentView(frameLayout);
    frameLayout.requestLayout();

    hasSurface = false;
  }

  @Override
  protected void onResume() {
    super.onResume();

    // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
    // want to open the camera driver and measure the screen size if we're going to show the help on
    // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
    // off screen.
    cameraManager = new CameraManager(getApplication());

    // The ViewfinderView is created once and kept, rather than being rebuilt here. onResume runs
    // again every time the activity comes back to the foreground, so constructing and adding a
    // fresh view each time stacked them on top of each other inside frameLayout and leaked the
    // previous ones. It must stay added after viewLayout so that it draws over the preview.
    if (viewfinderView == null) {
      viewfinderView = new ViewfinderView(this, null);
      frameLayout.addView(viewfinderView);
    }
    viewfinderView.setCameraManager(cameraManager);

    handler = null;
    lastResult = null;

    if (surfaceView == null) {
      surfaceView = new SurfaceView(this);
      viewLayout.addView(surfaceView);
    }
    SurfaceHolder surfaceHolder = surfaceView.getHolder();
    if (hasSurface) {
      // The activity was paused but not stopped, so the surface still exists. Therefore
      // surfaceCreated() won't be called, so init the camera here.
      initCamera(surfaceHolder);
    } else {
      // Install the callback and wait for surfaceCreated() to init the camera.
      surfaceHolder.addCallback(this);
      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    surfaceView.setVisibility(View.VISIBLE);

    Intent intent = getIntent();

    copyToClipboard = true;

    source = IntentSource.NONE;
    decodeFormats = null;
    characterSet = null;

    if (intent != null) {

      String action = intent.getAction();
      String dataString = intent.getDataString();

      if (Intents.Scan.ACTION.equals(action)) {

        // Scan the formats the intent requested, and return the result to the calling activity.
        source = IntentSource.NATIVE_APP_INTENT;
        decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);

        if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
          int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
          int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
          if (width > 0 && height > 0) {
            cameraManager.setManualFramingRect(width, height);
          }
        }
      }

      characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

    }
  }

  @Override
  protected void onPause() {
    if (handler != null) {
      handler.quitSynchronously();
      handler = null;
    }
    if (cameraManager != null) {
      cameraManager.closeDriver();
    }
    // This previously constructed a brand new SurfaceView and removed the callback from that
    // object's holder, which had never had a callback registered on it. Worse, it overwrote the
    // surfaceView field with a view that was never added to viewLayout, so the next onResume found
    // a non-null surfaceView, skipped adding it, and left the user with a blank preview.
    if (!hasSurface && surfaceView != null) {
      surfaceView.getHolder().removeCallback(this);
    }
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      unregisterOnBackInvokedCallback();
    }
    super.onDestroy();
  }

  /**
   * The single owner of back handling for this activity, used by both the API 33 and later
   * OnBackInvokedDispatcher callback and, below 33, the KEYCODE_BACK arm of {@link #onKeyDown}.
   *
   * <p>Only one of those two paths is live on any given device, but the actions that end the
   * activity are guarded so that back can never take effect more than once. Unlike onKeyDown there
   * is no "fall through to the default handling" option once a dispatcher callback is registered,
   * so the case that previously broke out of the switch finishes here instead, which is what the
   * platform would have done.
   */
  private void handleBackInvoked() {
    if (backHandled || isFinishing()) {
      return;
    }
    if (source == IntentSource.NATIVE_APP_INTENT) {
      backHandled = true;
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
      // Restarting the preview leaves the activity open, so this arm is repeatable and must not
      // set backHandled.
      restartPreviewAfterDelay(0L);
      return;
    }
    backHandled = true;
    finish();
  }

  // Callers must guard on Build.VERSION.SDK_INT >= TIRAMISU. This would normally carry
  // @RequiresApi, but the Barcode target compiles against only android.jar and the ZXing core, so
  // androidx.annotation is not on its classpath.
  private void registerOnBackInvokedCallback() {
    if (backInvokedCallback != null) {
      return;
    }
    OnBackInvokedCallback callback = new OnBackInvokedCallback() {
      @Override
      public void onBackInvoked() {
        handleBackInvoked();
      }
    };
    getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
      OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback);
    backInvokedCallback = callback;
  }

  // Callers must guard on Build.VERSION.SDK_INT >= TIRAMISU. See registerOnBackInvokedCallback.
  private void unregisterOnBackInvokedCallback() {
    if (backInvokedCallback instanceof OnBackInvokedCallback) {
      getOnBackInvokedDispatcher()
        .unregisterOnBackInvokedCallback((OnBackInvokedCallback) backInvokedCallback);
      backInvokedCallback = null;
    }
  }

  @Override
  @SuppressWarnings("deprecation")  // KEYCODE_BACK only arrives here below API 33; see onCreate.
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_BACK:
        // When a dispatcher callback is registered it is the sole owner of back, so this arm must
        // not act as well. Below API 33 nothing is registered and this is the only path.
        if (backInvokedCallback != null) {
          break;
        }
        handleBackInvoked();
        return true;
      case KeyEvent.KEYCODE_FOCUS:
      case KeyEvent.KEYCODE_CAMERA:
        // Handle these events so they don't launch the Camera app
        return true;
      // Use volume up/down to turn on light
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        cameraManager.setTorch(false);
        return true;
      case KeyEvent.KEYCODE_VOLUME_UP:
        cameraManager.setTorch(true);
        return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
    // Bitmap isn't used yet -- will be used soon
    if (handler == null) {
      savedResultToShow = result;
    } else {
      if (result != null) {
        savedResultToShow = result;
      }
      //      if (savedResultToShow != null) {
      //        Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
      //        handler.sendMessage(message);
      //      }
      savedResultToShow = null;
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (holder == null) {
      Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
    }
    if (!hasSurface) {
      hasSurface = true;
      initCamera(holder);
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    hasSurface = false;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }

  /**
   * A valid barcode has been found, so give an indication of success and show the results.
   *
   * @param rawResult The contents of the barcode.
   * @param barcode   A greyscale bitmap of the camera data which was decoded.
   */
  public void handleDecode(Result rawResult, Bitmap barcode) {
    lastResult = rawResult;
    ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);

    boolean fromLiveScan = barcode != null;
    if (fromLiveScan) {
      drawResultPoints(barcode, rawResult);
    }

    switch (source) {
      case NATIVE_APP_INTENT:
      case PRODUCT_SEARCH_LINK:
        handleDecodeExternally(rawResult, resultHandler, barcode);
        break;
      case NONE:
        if (fromLiveScan) {
          String message = " (bulk scan)";
          Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
          // Wait a moment or else it will scan the same barcode continuously about 3 times
          restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
        } else {
        }
        break;
    }
  }

  /**
   * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
   *
   * @param barcode   A bitmap of the captured image.
   * @param rawResult The decoded results which contains the points to draw.
   */
  private void drawResultPoints(Bitmap barcode, Result rawResult) {
    ResultPoint[] points = rawResult.getResultPoints();
    if (points != null && points.length > 0) {
      Canvas canvas = new Canvas(barcode);
      Paint paint = new Paint();
      paint.setColor(0xc099cc00);
      if (points.length == 2) {
        paint.setStrokeWidth(4.0f);
        drawLine(canvas, paint, points[0], points[1]);
      } else if (points.length == 4 &&
                 (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                  rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
        // Hacky special case -- draw two lines, for the barcode and metadata
        drawLine(canvas, paint, points[0], points[1]);
        drawLine(canvas, paint, points[2], points[3]);
      } else {
        paint.setStrokeWidth(10.0f);
        for (ResultPoint point : points) {
          canvas.drawPoint(point.getX(), point.getY(), paint);
        }
      }
    }
  }

  private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b) {
    canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
  }

  // Put up our own UI for how to handle the decoded contents.
  // private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
  //   statusView.setVisibility(View.GONE);
  //   viewfinderView.setVisibility(View.GONE);
  //   resultView.setVisibility(View.VISIBLE);

  //   ImageView barcodeImageView = (ImageView) findViewById(R.id.barcode_image_view);
  //   if (barcode == null) {
  //     barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),
  //         R.drawable.launcher_icon));
  //   } else {
  //     barcodeImageView.setImageBitmap(barcode);
  //   }

  //   TextView formatTextView = (TextView) findViewById(R.id.format_text_view);
  //   formatTextView.setText(rawResult.getBarcodeFormat().toString());

  //   TextView typeTextView = (TextView) findViewById(R.id.type_text_view);
  //   typeTextView.setText(resultHandler.getType().toString());

  //   DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
  //   String formattedTime = formatter.format(new Date(rawResult.getTimestamp()));
  //   TextView timeTextView = (TextView) findViewById(R.id.time_text_view);
  //   timeTextView.setText(formattedTime);


  //   TextView metaTextView = (TextView) findViewById(R.id.meta_text_view);
  //   View metaTextViewLabel = findViewById(R.id.meta_text_view_label);
  //   metaTextView.setVisibility(View.GONE);
  //   metaTextViewLabel.setVisibility(View.GONE);
  //   Map<ResultMetadataType,Object> metadata = rawResult.getResultMetadata();
  //   if (metadata != null) {
  //     StringBuilder metadataText = new StringBuilder(20);
  //     for (Map.Entry<ResultMetadataType,Object> entry : metadata.entrySet()) {
  //       if (DISPLAYABLE_METADATA_TYPES.contains(entry.getKey())) {
  //         metadataText.append(entry.getValue()).append('\n');
  //       }
  //     }
  //     if (metadataText.length() > 0) {
  //       metadataText.setLength(metadataText.length() - 1);
  //       metaTextView.setText(metadataText);
  //       metaTextView.setVisibility(View.VISIBLE);
  //       metaTextViewLabel.setVisibility(View.VISIBLE);
  //     }
  //   }

  //   TextView contentsTextView = (TextView) findViewById(R.id.contents_text_view);
  //   CharSequence displayContents = resultHandler.getDisplayContents();
  //   contentsTextView.setText(displayContents);
  //   // Crudely scale betweeen 22 and 32 -- bigger font for shorter text
  //   int scaledSize = Math.max(22, 32 - displayContents.length() / 4);
  //   contentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);

  //   int buttonCount = resultHandler.getButtonCount();
  //   ViewGroup buttonView = (ViewGroup) findViewById(R.id.result_button_view);
  //   buttonView.requestFocus();
  //   for (int x = 0; x < ResultHandler.MAX_BUTTON_COUNT; x++) {
  //     TextView button = (TextView) buttonView.getChildAt(x);
  //     if (x < buttonCount) {
  //       button.setVisibility(View.VISIBLE);
  //       button.setText(resultHandler.getButtonText(x));
  //       button.setOnClickListener(new ResultButtonListener(resultHandler, x));
  //     } else {
  //       button.setVisibility(View.GONE);
  //     }
  //   }

  //   if (copyToClipboard && !resultHandler.areContentsSecure()) {
  //     ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
  //     if (displayContents != null) {
  //       clipboard.setText(displayContents);
  //     }
  //   }
  // }

  // Briefly show the contents of the barcode, then handle the result outside Barcode Scanner.
  private void handleDecodeExternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {

    if (barcode != null) {
      viewfinderView.drawResultBitmap(barcode);
    }

    long resultDurationMS;
    if (getIntent() == null) {
      resultDurationMS = DEFAULT_INTENT_RESULT_DURATION_MS;
    } else {
      resultDurationMS = getIntent().getLongExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS,
        DEFAULT_INTENT_RESULT_DURATION_MS);
    }

    if (copyToClipboard && !resultHandler.areContentsSecure()) {
      ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
      CharSequence text = resultHandler.getDisplayContents();
      if (text != null) {
        clipboard.setText(text);
      }
    }

    if (source == IntentSource.NATIVE_APP_INTENT) {

      // Hand back whatever action they requested - this can be changed to Intents.Scan.ACTION when
      // the deprecated intent is retired.
      Intent intent = new Intent(getIntent().getAction());
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
      intent.putExtra(Intents.Scan.RESULT, rawResult.toString());
      intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.getBarcodeFormat().toString());
      byte[] rawBytes = rawResult.getRawBytes();
      if (rawBytes != null && rawBytes.length > 0) {
        intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes);
      }
      Map<ResultMetadataType,?> metadata = rawResult.getResultMetadata();
      if (metadata != null) {
        if (metadata.containsKey(ResultMetadataType.UPC_EAN_EXTENSION)) {
          intent.putExtra(Intents.Scan.RESULT_UPC_EAN_EXTENSION,
            metadata.get(ResultMetadataType.UPC_EAN_EXTENSION).toString());
        }
        Integer orientation = (Integer) metadata.get(ResultMetadataType.ORIENTATION);
        if (orientation != null) {
          intent.putExtra(Intents.Scan.RESULT_ORIENTATION, orientation.intValue());
        }
        String ecLevel = (String) metadata.get(ResultMetadataType.ERROR_CORRECTION_LEVEL);
        if (ecLevel != null) {
          intent.putExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL, ecLevel);
        }
        Iterable<byte[]> byteSegments = (Iterable<byte[]>) metadata.get(ResultMetadataType.BYTE_SEGMENTS);
        if (byteSegments != null) {
          int i = 0;
          for (byte[] byteSegment : byteSegments) {
            intent.putExtra(Intents.Scan.RESULT_BYTE_SEGMENTS_PREFIX + i, byteSegment);
            i++;
          }
        }
      }
      sendReplyMessage(Constants.return_scan_result, intent, resultDurationMS);
    }
  }

  private void sendReplyMessage(int id, Object arg, long delayMS) {
    Message message = Message.obtain(handler, id, arg);
    if (delayMS > 0L) {
      handler.sendMessageDelayed(message, delayMS);
    } else {
      handler.sendMessage(message);
    }
  }

  private void initCamera(SurfaceHolder surfaceHolder) {
    Log.w(TAG, "initCamera() was called");
    if (surfaceHolder == null) {
      throw new IllegalStateException("No SurfaceHolder provided");
    }
    if (cameraManager.isOpen()) {
      Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
      return;
    }
    try {
      cameraManager.openDriver(surfaceHolder);
      // Creating the handler starts the preview, which can also throw a RuntimeException.
      if (handler == null) {
        handler = new CaptureActivityHandler(this, decodeFormats, characterSet, cameraManager);
      }
      decodeOrStoreSavedBitmap(null, null);
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      displayFrameworkBugMessageAndExit();
    } catch (RuntimeException e) {
      // Barcode Scanner has seen crashes in the wild of this variety:
      // java.?lang.?RuntimeException: Fail to connect to camera service
      Log.w(TAG, "Unexpected error initializing camera", e);
      displayFrameworkBugMessageAndExit();
    }
  }

  private void displayFrameworkBugMessageAndExit() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Scanner");
    builder.setMessage("Camera Framework Bug");
    //    builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
    //    builder.setOnCancelListener(new FinishListener(this));
    builder.show();
  }

  public void restartPreviewAfterDelay(long delayMS) {
    if (handler != null) {
      handler.sendEmptyMessageDelayed(Constants.restart_preview, delayMS);
    }
  }

  public void drawViewfinder() {
    viewfinderView.drawViewfinder();
  }
}
