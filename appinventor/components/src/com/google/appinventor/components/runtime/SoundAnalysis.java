// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Sources:
//Steve Rubin, PitchLive, (2014), GitHub repository,
//https://github.com/srubin/cs160-audio-examples/tree/master/PitchLive
//Joren Six, TarsosDSP, (2013), GitHub repository,
//Source: https://github.com/JorenSix/TarsosDSP

  package com.google.appinventor.components.runtime;
  import com.google.appinventor.components.annotations.PropertyCategory;
  import com.google.appinventor.components.annotations.SimpleEvent;
  import com.google.appinventor.components.annotations.SimpleFunction;
  import com.google.appinventor.components.annotations.SimpleObject;
  import com.google.appinventor.components.annotations.SimpleProperty;
  import com.google.appinventor.components.annotations.UsesPermissions;
  import com.google.appinventor.components.common.ComponentCategory;
  import com.google.appinventor.components.common.PropertyTypeConstants;
  import com.google.appinventor.components.common.YaVersion;
  import com.google.appinventor.components.runtime.errors.IllegalArgumentError;
  import com.google.appinventor.components.annotations.UsesLibraries;

  import android.app.Activity;
  import android.content.Context;
  import android.app.Activity;
  import android.media.AudioFormat;
  import android.media.AudioRecord;
  import android.media.MediaRecorder;
  import android.os.Bundle;
  import be.hogent.tarsos.dsp.AudioEvent;
  import be.hogent.tarsos.dsp.AudioFormat.Encoding;
  import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
  import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
  import be.hogent.tarsos.dsp.pitch.PitchProcessor;
  import java.io.IOException;

  /**
  * Multimedia component that analysis the pitch of an audio through the microphone.
  * It uses TarsosDSP, a Real-Time Audio Processing Framework in Java.
  * <a href="https://github.com/JorenSix/TarsosDSP">TarsosDSP</a>
  * It can be used as an input in different situations. For instance to control some components
  * with user whistle (pitch > 500Hz) or clap (pitch > 2000Hz) or.
  */
  @DesignerComponent(version = YaVersion.PLAYER_COMPONENT_VERSION,
  description = "Multimedia component that analysis the pitch of an audio through the microphone. " +
  "It uses TarsosDSP,  a Java library for audio processing. " +
  "It can be used as an input to different situation. For instance to control some components " +
  "with whistle (pitch > 500Hz) or clap(pitch > 2000Hz) ",
  category = ComponentCategory.EXTERNAL,
  nonVisible = true,
  iconName = "images/externalComponent.png")
  @SimpleObject
  @UsesPermissions(permissionNames = "android.permission.RECORD_AUDIO")
  @UsesLibraries(libraries = "TarsosDSP-1.7.jar")

  public final class SoundAnalysis extends AndroidNonvisibleComponent
  implements Component,PitchDetectionHandler {
    private AudioRecord recorder;
    private byte[] buffer;
    private PitchProcessor mPitchProcessor;
    private boolean mIsRecording;
    private be.hogent.tarsos.dsp.AudioFormat tarsosFormat;
    public static final int SAMPLE_RATE = 16000;
    private final Activity activity;

    /**
    * Creates a new SoundAnalysis component.
    *
    * @param container
    */
    public SoundAnalysis(ComponentContainer container) {
      super(container.$form());
      activity = container.$context();
      mIsRecording = false;

      // STEP 1: set up recorder
      int minBufferSize = AudioRecord.getMinBufferSize(
      SAMPLE_RATE,
      AudioFormat.CHANNEL_IN_MONO,
      AudioFormat.ENCODING_PCM_16BIT);
      buffer = new byte[minBufferSize];
      recorder = new AudioRecord(
      MediaRecorder.AudioSource.MIC,
      SAMPLE_RATE,
      AudioFormat.CHANNEL_IN_MONO,
      AudioFormat.ENCODING_PCM_16BIT,
      minBufferSize
      );
      // END STEP 1

      // STEP 2: create pitch detector
      mPitchProcessor = new PitchProcessor(
      PitchProcessor.PitchEstimationAlgorithm.AMDF,
      SAMPLE_RATE,
      minBufferSize,
      this);
      // END STEP 2
    }

    /**
    * Reports whether the component is recording.
    */
    @SimpleProperty(
    category = PropertyCategory.BEHAVIOR)
    public boolean IsRecording() {
      return mIsRecording;
    }

    /**
     * Sets the recording property to true or false.
     *
     * @param recording determines if the component should record.
     */
    @SimpleProperty(description = "")
    public void IsRecording(boolean recording) {
      mIsRecording = recording;
    }

    /**
     * Start the sound Analysis by listening to the microphone input.
     */
    @SimpleFunction
    public void StartListening() {
      mIsRecording = true;
      // STEP 5: start recording and detecting pitch
      listen();
      // END STEP 5
    }

    /**
     * Stop the sound Analysis.
     */
    @SimpleFunction
    public void StopListening() {
      mIsRecording = false;
    }

    @Override
    public void handlePitch(
    PitchDetectionResult pitchDetectionResult,
    AudioEvent audioEvent) {
      String newText;
      String whistleText = "nothing";
      float result=0;
      //Is a pitch detected ?
      if (pitchDetectionResult.isPitched()) {
        result = pitchDetectionResult.getPitch();
      }
      final float finalResult = result;
      activity.runOnUiThread(new Runnable(){
        @Override
        public void run() {
          GotPitch(finalResult);
        }
      });
      // END STEP 3
    }

    // STEP 4: setup recording
    public void listen() {
      recorder.startRecording();
      tarsosFormat = new be.hogent.tarsos.dsp.AudioFormat(
      (float)SAMPLE_RATE, // sample rate
      16, // bit depth
      1, // channels
      true, // signed samples?
      false // big endian?
      );
      Thread listeningThread = new Thread(new Runnable() {
        @Override
        public void run() {
          while (mIsRecording) {
            int bufferReadResult =
            recorder.read(buffer, 0, buffer.length);
            AudioEvent audioEvent =
            new AudioEvent(
            tarsosFormat,
            bufferReadResult);
            audioEvent.setFloatBufferWithByteBuffer(buffer);
            mPitchProcessor.process(audioEvent);
          }
          recorder.stop();
        }

      });

      listeningThread.start();
      // END STEP 4
    }

    /**
    * Event indicating a pitch detection.
    *
    * @param pitchResult the pitch result from the sound analysis.
    */
    @SimpleEvent
    public void GotPitch(float pitchResult ) {
      // invoke the application's "GotPitch" event handler.
      EventDispatcher.dispatchEvent(this, "GotPitch", pitchResult);
    }
  }
