package com.rit.appinventor.components.runtime;

import android.content.Context;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import android.Manifest;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;

@DesignerComponent(version = YaVersion.SOUNDPRESSURELEVEL_COMPONENT_VERION,
        description = "Non-visible component that can collect sound pressure level data",
        category = ComponentCategory.SENSORS,
        nonVisible = true,
        iconName = "images/extension.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.RECORD_AUDIO");
public class SoundPressureLevel extends AndroidNonvisibleComponent
        implements OnStopListener, OnResumeListener, Deleteable {

    private final static String LOG_TAG = "SoundPressureLevel";
    private boolean isEnabled;
    private short audioData [] = new short[minBufferSize];
    private static final int audioSource = MIC;
    private static final int sampleRateInHz = 44100;
    private static final int channelConfig = CHANNEL_IN_MONO;
    private static final int audioFormat = ENCODING_PCM_16BIT;
    private static final int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig,audioFormat);
    private double currentSoundPressureLevel = 0;

    public SoundPressureLevelRecorder(ComponentContainer container) {
        super(container.$form());

        recorder = new AudioRecord(MIC, sampleRateInHz, channelConfig, audioFormat, minBufferSize);
        form.registerForOnResume(this);
        form.registerForOnStop(this);
        Enabled(true);
        Log.d(LOG_TAG, "spl created");
    }

    @Override
    public void onDelete() {
        if (isEnabled) {
            stopListening();
        }
    }

    @Override
    public void onResume() {
        if (isEnabled) {
            startListening();
        }
    }

    @Override
    public void onStop() {
        if (isEnabled) {
            stopListening();
        }
    }

    @Override
    public void onSoundPressureLevelChanged(double audioData) {
        if (isEnabled) {
            SoundPressureLevelChanged(audioData);
        }
    }

    public void analyzeSoundData(AudioRecord recorder) {
        double spldata = 0;
        recorder.read(audioData, 0, minBufferSize);
        this.onSoundPressureLevelChanged(spldata);
    }

    /**
     * Assumes that audioRecord has been initialized, which happens in constructor
     */
    private void startListening() {
        recorder.startRecording();
    }

    /**
     * Assumes that audioRecord has been initialized, which happens in constructor
     */
    private void stopListening() {
        recorder.stop();
    }

    /**
     * Specifies whether the recorder should start recording audio.  If true,
     * the recorder will record audio.  Otherwise, no data is
     * recorded even if the device microphone is active.
     *
     * @param enabled {@code true} enables audio recording,
     *                {@code false} disables it
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "True")
    @SimpleProperty
    public void Enabled(boolean enabled) {
        if (this.isEnabled != enabled) {
            this.isEnabled = enabled;
            if (enabled) {
                startListening();
            } else {
                stopListening();
            }
        }
    }

    /**
     * Available property getter method (read-only property).
     *
     * @return {@code true} indicates that the device has a microphone,
     * {@code false} that it isn't
     */
    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR)
    public boolean Available() {
        return (AudioRecord.getActiveMicrophones.size() > 0);
    }

    /**
     * If true, the sensor will generate events.  Otherwise, no events
     * are generated
     *
     * @return {@code true} indicates that the sensor generates events,
     * {@code false} that it doesn't
     */
    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR)
    public boolean Enabled() {
        return isEnabled;
    }

    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR)
    public double SoundPressureLevel() {
        return currentSoundPressureLevel;
    }

    /**
     * Indicates the sound pressure level has changed
     */
    @SimpleEvent
    public void SoundPressureLevelChanged(double decibels) {
        this.currentSoundPressureLevel = decibels;
        EventDispatcher.dispatchEvent(this, "SoundPressureLevelChanged", this.currentSoundPressureLevel);
    }
}