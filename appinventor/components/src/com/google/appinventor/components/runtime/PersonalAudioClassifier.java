// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesPermissions;
import android.util.Base64;
import android.util.Log;
import java.io.File;


@DesignerComponent(version = 20200904,
    category = ComponentCategory.EXPERIMENTAL,
    description = "Component that classifies audio clips using a user trained model from the personal audio classifier",
    iconName = "images/personalAudioClassifier.png",
    nonVisible = true)
@SimpleObject
@UsesAssets(fileNames = "tfjs-1.5.2.js, recorder1.js, chroma.js, spectrogram.js, personal_audio_classifier.html, personal_audio_classifier1.js, mobilenet_group1-shard1of1, mobilenet_group10-shard1of1, mobilenet_group11-shard1of1, mobilenet_group12-shard1of1, mobilenet_group13-shard1of1, mobilenet_group14-shard1of1, mobilenet_group15-shard1of1, mobilenet_group16-shard1of1, mobilenet_group17-shard1of1, mobilenet_group18-shard1of1, mobilenet_group19-shard1of1, mobilenet_group2-shard1of1, mobilenet_group20-shard1of1, mobilenet_group21-shard1of1, mobilenet_group22-shard1of1, mobilenet_group23-shard1of1, mobilenet_group24-shard1of1, mobilenet_group25-shard1of1, mobilenet_group26-shard1of1, mobilenet_group27-shard1of1, mobilenet_group28-shard1of1, mobilenet_group29-shard1of1, mobilenet_group3-shard1of1, mobilenet_group30-shard1of1, mobilenet_group31-shard1of1, mobilenet_group32-shard1of1, mobilenet_group33-shard1of1, mobilenet_group34-shard1of1, mobilenet_group35-shard1of1, mobilenet_group36-shard1of1, mobilenet_group37-shard1of1, mobilenet_group38-shard1of1, mobilenet_group39-shard1of1, mobilenet_group4-shard1of1, mobilenet_group40-shard1of1, mobilenet_group41-shard1of1, mobilenet_group42-shard1of1, mobilenet_group43-shard1of1, mobilenet_group44-shard1of1, mobilenet_group45-shard1of1, mobilenet_group46-shard1of1, mobilenet_group47-shard1of1, mobilenet_group48-shard1of1, mobilenet_group49-shard1of1, mobilenet_group5-shard1of1, mobilenet_group50-shard1of1, mobilenet_group51-shard1of1, mobilenet_group52-shard1of1, mobilenet_group53-shard1of1, mobilenet_group54-shard1of1, mobilenet_group55-shard1of1, mobilenet_group6-shard1of1, mobilenet_group7-shard1of1, mobilenet_group8-shard1of1, mobilenet_group9-shard1of1, mobilenet_model.json")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.CAMERA, android.permission.RECORD_AUDIO, android.permission.MODIFY_AUDIO_SETTINGS")
public class PersonalAudioClassifier extends BaseAiComponent implements Component{
    private static final String LOG_TAG = PersonalAudioClassifier.class.getSimpleName();

    public PersonalAudioClassifier(Form form) {
        super(form);
    }

    private static String encodeFileToBase64(File file) {
        String base64File = "";
        try (FileInputStream imageInFile = new FileInputStream(file)) {
        // Reading a file from file system
        byte fileData[] = new byte[(int) file.length()];
        imageInFile.read(fileData);
        base64File = Base64.encodeToString(fileData, 0).replace("\n", "");
        } catch (FileNotFoundException e) {
        System.out.println("File not found" + e);
        } catch (IOException ioe) {
        System.out.println("Exception while reading the file " + ioe);
        }
        return base64File;
    }

    @SimpleFunction(description = "Performs classification on the image at the given path and triggers the GotClassification event when classification is finished successfully.")
    public void ClassifySoundData(final String sound) {
        assertWebView("ClassifySoundData");
        Log.d(LOG_TAG, "Entered Classify Sound Data");

        String soundPath = (sound == null) ? "" : sound;
        Log.d(LOG_TAG, "soundPath: " + soundPath);

        File soundFile = new File(soundPath);
        Log.d(LOG_TAG, "soundFile: " + soundFile);

        String encodedSound = encodeFileToBase64(soundFile);
        Log.d(LOG_TAG, "encodedSound: " + encodedSound);

        webview.evaluateJavascript("getSpectrogram(\"" + encodedSound + "\");", null);

        Log.d(LOG_TAG, "encodedSound sent to Javascript!");
    }

    @Override
    @SimpleEvent(description = "Event indicating that the classifier is ready.")
    public void ClassifierReady() {
        EventDispatcher.dispatchEvent(this, "ClassifierReady");
    }

    @Override
    @SimpleEvent(description = "Event indicating that classification has finished successfully. Result is of the form [[class1, confidence1], [class2, confidence2], ..., [class10, confidence10]].")
    public void GotClassification(YailDictionary result) {
        Log.d(LOG_TAG, "GOT CLASSIFICATION: " + result);
        EventDispatcher.dispatchEvent(this, "GotClassification", YailDictionary.dictToAlist(result));
    }

    @Override
    @SimpleEvent(description = "Event indicating that an error has occurred.")
    public void Error(final int errorCode) {
      EventDispatcher.dispatchEvent(this, "Error", errorCode);
    }

}