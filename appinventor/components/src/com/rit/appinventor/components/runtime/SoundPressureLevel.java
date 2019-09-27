package com.rit.appinventor.components.runtime;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import android.Manifest;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.content.pm.PackageManager;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.MediaRecorder.AudioSource.MIC;

@DesignerComponent(version = YaVersion.SOUNDPRESSURELEVEL_COMPONENT_VERSION,
        description = "Non-visible component that can collect sound pressure level data",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "images/extension.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.RECORD_AUDIO")
public class SoundPressureLevel extends AndroidNonvisibleComponent
        implements OnStopListener, OnResumeListener, Deleteable {

    private final static String LOG_TAG = "SoundPressureLevel";
    private boolean isEnabled;
    private static final int audioSource = MIC;
    private static final int sampleRateInHz = 44100;
    private static final int channelConfig = CHANNEL_IN_MONO;
    private static final int audioFormat = ENCODING_PCM_16BIT;
    private AudioRecord recorder;
    Handler splHandler;
    private static final int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig,audioFormat);
    private double currentSoundPressureLevel = 0;
    private boolean isListening;
    Thread soundChecker;
    private boolean threadSuspended;
    private boolean isRecording;
    private boolean threadRunning = true;
    private int listenIntervalMilliSeconds = 200;

    public SoundPressureLevel(ComponentContainer container) {
        super(container.$form());

        recorder = new AudioRecord(MIC, sampleRateInHz, channelConfig, audioFormat, minBufferSize);
        form.registerForOnResume(this);
        form.registerForOnStop(this);
        Enabled(true);
        splHandler = new Handler();
        soundChecker = new Thread(new Runnable(){
            @Override
            public void run() {
                while(threadRunning){
                Log.d(LOG_TAG, "spl thread loop");
                    if (isRecording) {
                        Log.d(LOG_TAG, "spl thread isRecording");
                        final Pair<short[], Integer> tuple = analyzeSoundData();
                        form.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onSoundPressureLevelChanged(tuple);
                            }
                        });
                    }
                    try {
                        Thread.sleep( (long) listenIntervalMilliSeconds);
                    } catch (InterruptedException e) {
                        Log.d(LOG_TAG, "spl thread sleep error");
                    }
                }
                Log.d(LOG_TAG, "spl thread end");
            }
        });
        if (isListening == false) {
            startListening();
        }
        soundChecker.start();
        Log.d(LOG_TAG, "spl created");
    }

    @Override
    public void onDelete() {
        if (isEnabled) {
            try {
                Log.d(LOG_TAG, "spl joining thread");
                threadRunning = false;
                soundChecker.join();
            }
            catch (InterruptedException e) {
                Log.d(LOG_TAG,"spl error joining thread");
            }
            stopListening();
        }
    }

    @Override
    public void onResume() {
        if (isEnabled) {
            startListening();
            if (threadSuspended) {
                Log.d(LOG_TAG, "spl restarting thread");
                soundChecker.start();
                threadSuspended = false;
            }
        }
    }

    @Override
    public void onStop() {
        if (isEnabled) {
            stopListening();
            if (!threadSuspended) {
                Log.d(LOG_TAG, "spl suspend thrad");
                threadSuspended = true;
                soundChecker.suspend();
            }
        }
    }

    public void onSoundPressureLevelChanged(Pair<short[], Integer> tuple) {
        if (isEnabled) {
            Log.d(LOG_TAG, "spl onSoundPressueLevelChange");
            short[] soundData = tuple.first;
            Integer length = tuple.second;
            double data = 0;

            //Convert data from mic to pressure in pascals.
            double[] soundSamplePressure = convertMicVoltageToPressure(soundData);

            //Find root mean square of sound.
            double rms = calcRootMeanSquare(soundSamplePressure,length);
            Log.d(LOG_TAG,String.format("spl RMS %f",rms));

            //Find SPL of sound.
            double dBs = calcDeciBels(rms);
            Log.d(LOG_TAG,String.format("spl %f dBs",dBs));

            //Round to the tenths decimal place.
            dBs = Math.round(dBs*10)/10;

            SoundPressureLevelChanged(dBs);
        }
    }

    /**
     * Converts Mic Voltage represented by a short to the pressure experienced by the mic.
     *
     * Max short value is 32,767, most smartphone microphones are accurate until about
     * 90dB or 0.6325 pascals. 32,767/0.6325 = 51,805.5336, which will be the value used
     * to convert between microphone data and pascals.
     *
     * @param soundData
     * @return
     */
    private double[] convertMicVoltageToPressure(short[] soundData) {
        double[] soundPressures = new double[soundData.length];
        for (int i = 0; i < soundData.length; i++) {
            soundPressures[i] = soundData[i]/51805.5336;
        }
        return soundPressures;
    }

    /**
     * Calculates the root mean square of sound data.
     * Follows the formula rms=sqrt((p^2)_average)
     * @param soundData
     * @param numSamples
     * @return
     */
    private double calcRootMeanSquare(double[] soundData, int numSamples) {
        //Find Root Square Mean of sound clip.
        double rms;
        double data = 0;
        for (int i = 0; i < numSamples; i++) {
            data+=Math.pow(soundData[i],2);
        }
        data = (data/numSamples);
        rms = Math.sqrt(data);
        return rms;
    }

    /**
     * Calculates the Sound Pressure Level in dBs of the sound pressue in pascals.
     * Follows the formula spl = 20*log10(p/pRef) where p is the current pressue in pascals,
     * pRef is smallest sound humans can hear at 2*10^-5 pascals.
     * @param p
     * @return
     */
    private double calcDeciBels(double p) {
        double pRef = 2*Math.pow(10,-5);
        double dBs = 20*Math.log10(p/pRef);
        return dBs;
    }

    public Pair<short[], Integer> analyzeSoundData() {
        Log.d(LOG_TAG, "spl analyzeSoundData");
        short spldata = 0;
        short recAudioData [] = new short[minBufferSize];
        int length = recorder.read(recAudioData, 0, minBufferSize);
        Pair<short[], Integer> tuple = new Pair<short[],Integer>(recAudioData,length);
        return tuple;
    }

    /**
     * Assumes that audioRecord has been initialized, which happens in constructor
     */
    private void startListening() {
        if(recorder != null) {
            recorder.startRecording();
            isRecording = true;
        }
    }

    /**
     * Assumes that audioRecord has been initialized, which happens in constructor
     */
    private void stopListening() {
        if (recorder != null) {
            recorder.stop();
            isRecording = false;
        }
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

        Log.d(LOG_TAG, "spl Available call");
        AudioRecord testRecorder = new AudioRecord(MIC, sampleRateInHz, channelConfig, audioFormat, minBufferSize);
        testRecorder.startRecording();
        boolean isAvailable = testRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING; //Would be RECORDSTATE_STOPPED if no mic is available
        testRecorder.stop();
        testRecorder.release();
        Log.d(LOG_TAG,"spl Availability: " + String.valueOf(isAvailable));
        return isAvailable;
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



    /**
     * Set the current wait time for the thread that reads the mic data.
     * The wait time will be in milliseconds (ms).
     * @param milliSeconds
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
            defaultValue = "200")
    @SimpleProperty (
            description = "Set the interval of time to listen in milliseconds.",
            category = PropertyCategory.BEHAVIOR)
    public void ListenIntervalMilliseconds(int milliSeconds) {
        if (milliSeconds > 0 && milliSeconds < Integer.MAX_VALUE) {
            this.listenIntervalMilliSeconds = milliSeconds;
        }
    }

    /**
     * Get the current wait time for the thread that reads the mic data.
     * The current wait time will be in milliseconds (ms).
     */
    @SimpleProperty (
            description = "Get the current interval of time spent listening in milliseconds.",
            category = PropertyCategory.BEHAVIOR)
    public int ListenIntervalMilliseconds() {
        return listenIntervalMilliSeconds;
    }
}
