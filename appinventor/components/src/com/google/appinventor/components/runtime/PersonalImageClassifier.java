// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.YailDictionary;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.Log;

@DesignerComponent(version = YaVersion.PERSONAL_IMAGE_CLASSIFIER_COMPONENT_VERSION,
        category = ComponentCategory.EXPERIMENTAL,
        description = "Component that classifies images using a user trained model from the image " +
            "classification explorer. You must provide a WebViewer component in the PersonalImageClassifier " +
            "component's WebViewer property in order for classification to work.",
        iconName = "images/personalImageClassifier.png",
        nonVisible = true)
@SimpleObject
@UsesAssets(fileNames = "personal_image_classifier.html, personal_image_classifier.js, mobilenet_group1-shard1of1, mobilenet_group10-shard1of1, mobilenet_group11-shard1of1, mobilenet_group12-shard1of1, mobilenet_group13-shard1of1, mobilenet_group14-shard1of1, mobilenet_group15-shard1of1, mobilenet_group16-shard1of1, mobilenet_group17-shard1of1, mobilenet_group18-shard1of1, mobilenet_group19-shard1of1, mobilenet_group2-shard1of1, mobilenet_group20-shard1of1, mobilenet_group21-shard1of1, mobilenet_group22-shard1of1, mobilenet_group23-shard1of1, mobilenet_group24-shard1of1, mobilenet_group25-shard1of1, mobilenet_group26-shard1of1, mobilenet_group27-shard1of1, mobilenet_group28-shard1of1, mobilenet_group29-shard1of1, mobilenet_group3-shard1of1, mobilenet_group30-shard1of1, mobilenet_group31-shard1of1, mobilenet_group32-shard1of1, mobilenet_group33-shard1of1, mobilenet_group34-shard1of1, mobilenet_group35-shard1of1, mobilenet_group36-shard1of1, mobilenet_group37-shard1of1, mobilenet_group38-shard1of1, mobilenet_group39-shard1of1, mobilenet_group4-shard1of1, mobilenet_group40-shard1of1, mobilenet_group41-shard1of1, mobilenet_group42-shard1of1, mobilenet_group43-shard1of1, mobilenet_group44-shard1of1, mobilenet_group45-shard1of1, mobilenet_group46-shard1of1, mobilenet_group47-shard1of1, mobilenet_group48-shard1of1, mobilenet_group49-shard1of1, mobilenet_group5-shard1of1, mobilenet_group50-shard1of1, mobilenet_group51-shard1of1, mobilenet_group52-shard1of1, mobilenet_group53-shard1of1, mobilenet_group54-shard1of1, mobilenet_group55-shard1of1, mobilenet_group6-shard1of1, mobilenet_group7-shard1of1, mobilenet_group8-shard1of1, mobilenet_group9-shard1of1, mobilenet_model.json, squeezenet_group1-shard1of1, squeezenet_model.json, tfjs-0.13.2.js")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.CAMERA")
public class PersonalImageClassifier extends BaseAiComponent implements
 Component, OnPauseListener, OnResumeListener, OnClearListener{

    private static final int IMAGE_WIDTH = 500;
    private static final int IMAGE_QUALITY = 100;
    private static final String MODE_VIDEO = "Video";
    private static final String MODE_IMAGE = "Image";
    
    private static final int ERROR_INVALID_INPUT_MODE = -6;

    private String inputMode = MODE_VIDEO;
    private boolean running = false;
    private int minClassTime = 0;

    public PersonalImageClassifier(Form form) {
        super(form);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHOICES,
     editorArgs = {MODE_VIDEO, MODE_IMAGE})
    @SimpleProperty
    public void InputMode(String mode) {
        if (webview == null) {
        inputMode = mode;
        return;
        }
        if (MODE_VIDEO.equalsIgnoreCase(mode)) {
        webview.evaluateJavascript("setInputMode(\"video\");", null);
        inputMode = MODE_VIDEO;
        } else if (MODE_IMAGE.equalsIgnoreCase(mode)) {
        webview.evaluateJavascript("setInputMode(\"image\");", null);
        inputMode = MODE_IMAGE;
        } else {
        form.dispatchErrorOccurredEvent(this, "InputMode", ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INVALID_INPUT_MODE, LOG_TAG, "Invalid input mode " + mode);
        }
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets or sets the input mode for classification. Valid values are \"Video\" " +
          "(the default) and \"Image\".")
    public String InputMode() {
        return inputMode;
    }

    @SimpleProperty(description = "Gets all of the labels from this model. Only valid after ClassifierReady is signaled.")
    public List<String> ModelLabels() {
        return labels;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean Running() {
        return running;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
        defaultValue = "0")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public void MinimumInterval(int interval) {
        minClassTime = interval;
        if (webview != null) {
        webview.evaluateJavascript("minClassTime = " + interval + ";", null);
        }
    }

    @SimpleProperty
    public int MinimumInterval() {
        return minClassTime;
    }
 
    @SimpleFunction(description = "Performs classification on the image at the given path and triggers the GotClassification event when classification is finished successfully.")
    public void ClassifyImageData(final String image) {
        assertWebView("ClassifyImageData");
        Log.d(LOG_TAG, "Entered Classify");
        Log.d(LOG_TAG, image);

        String imagePath = (image == null) ? "" : image;
        BitmapDrawable imageDrawable;
        Bitmap scaledImageBitmap = null;

        try {
        imageDrawable = MediaUtil.getBitmapDrawable(form.$form(), imagePath);
        scaledImageBitmap = Bitmap.createScaledBitmap(imageDrawable.getBitmap(), IMAGE_WIDTH, (int) (imageDrawable.getBitmap().getHeight() * ((float) IMAGE_WIDTH) / imageDrawable.getBitmap().getWidth()), false);
        } catch (IOException ioe) {
        Log.e(LOG_TAG, "Unable to load " + imagePath);
        }

        // compression format of PNG -> not lossy
        Bitmap immagex = scaledImageBitmap;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, baos);
        byte[] b = baos.toByteArray();

        String imageEncodedbase64String = Base64.encodeToString(b, 0).replace("\n", "");
        Log.d(LOG_TAG, "imageEncodedbase64String: " + imageEncodedbase64String);

        webview.evaluateJavascript("classifyImageData(\"" + imageEncodedbase64String + "\");", null);
    }

    @SimpleFunction(description = "Toggles between user-facing and environment-facing camera.")
    public void ToggleCameraFacingMode() {
        assertWebView("ToggleCameraFacingMode");
        webview.evaluateJavascript("toggleCameraFacingMode();", null);
    }

    @SimpleFunction(description = "Performs classification on current video frame and triggers the GotClassification event when classification is finished successfully.")
    public void ClassifyVideoData() {
        assertWebView("ClassifyVideoData");
        webview.evaluateJavascript("classifyVideoData();", null);
    }

    @SimpleFunction(description = "Starts continuous video classification if the input mode is set to video and the classification is not already running.")
    public void StartContinuousClassification() {
        if (MODE_VIDEO.equals(inputMode) && !running) {
        assertWebView("StartVideoClassification");
        webview.evaluateJavascript("startVideoClassification();", null);
        running = true;
        }
    }

    @SimpleFunction(description = "Stop continuous video classification if the input mode is set to video and the classification is running.")
    public void StopContinuousClassification() {
        if (MODE_VIDEO.equals(inputMode) && running) {
        assertWebView("StopVideoClassification");
        webview.evaluateJavascript("stopVideoClassification();", null);
        running = false;
        }
    }

    @Override
    @SimpleEvent(description = "Event indicating that the classifier is ready.")
    public void ClassifierReady() {
        InputMode(inputMode);
        MinimumInterval(minClassTime);
        EventDispatcher.dispatchEvent(this, "ClassifierReady");
    }

    @Override
    @SimpleEvent(description = "Event indicating that classification has finished successfully. Result is of the form [[class1, confidence1], [class2, confidence2], ..., [class10, confidence10]].")
    public void GotClassification(YailDictionary result) {
      EventDispatcher.dispatchEvent(this, "GotClassification", result);
    }

    @Override
    @SimpleEvent(description = "Event indicating that an error has occurred.")
    public void Error(final int errorCode) {
      EventDispatcher.dispatchEvent(this, "Error", errorCode);
    }

    ///REGION: Lifecycle handling

    @Override
    public void onPause() {
        if (MODE_VIDEO.equals(inputMode)) {
        webview.evaluateJavascript("stopVideo();", null);
        }
    }

    @Override
    public void onResume() {
        if (MODE_VIDEO.equals(inputMode)) {
        webview.evaluateJavascript("startVideo();", null);
        }
    }

    @Override
    public void onClear() {
        webview.evaluateJavascript("stopVideo();", null);
    }
    ///ENDREGION

}
