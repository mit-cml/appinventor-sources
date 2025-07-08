package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.Asset;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;

@DesignerComponent(version = YaVersion.PLAYER_COMPONENT_VERSION,
    description = "Web-based player using HTML5 audio.",
    category = ComponentCategory.MEDIA,
    nonVisible = true,
    iconName = "images/player.png")
@SimpleObject
public class Player extends AndroidNonvisibleComponent implements Component {
    private String source;
    private boolean loop;
    private int volume = 100;

    public Player(ComponentContainer container) {
        super(container.$form());
        initAudioElement();
    }

    @JsMethod
    private native void initAudioElement() /*-{
        if (!$wnd.playerAudio) {
            $wnd.playerAudio = new Audio();
        }
    }-*/;

    @SimpleProperty
    @JsProperty(name = "Source")
    public String Source() {
        return source;
    }

    @SimpleProperty
    @JsProperty(name = "Loop")
    public boolean Loop() {
        return loop;
    }

    @SimpleProperty
    @JsProperty(name = "Volume")
    public int Volume() {
        return volume;
    }

    @SimpleProperty
    @JsProperty(name = "IsPlaying")
    public boolean IsPlaying() {
        return isAudioPlaying();
    }

    @SimpleProperty
    @JsProperty(name = "PlayOnlyInForeground")
    public boolean PlayOnlyInForeground() {
        return false; //TODO(lroman10): Real implementation
    }

    @SimpleProperty
    @JsProperty(name = "PlayOnlyInForeground")
    public void PlayOnlyInForeground(boolean value) {
        //TODO(lroman10): Real implementation
    }

    @SimpleProperty
    @JsProperty(name = "Source")
    public void Source(String src){
        source = src;
        setAudioSource(src);
    }

    @SimpleProperty
    @JsProperty(name = "Loop")
    public void Loop(boolean loopFlag) {
        loop = loopFlag;
        setAudioLoop(loop);
    }

    @SimpleProperty
    @JsProperty(name = "Volume")
    public void Volume(int vol) {
        volume = Math.max(0, Math.min(100, vol));
        setAudioVolume(volume / 100.0);
    }

    @SimpleFunction
    @JsMethod(name = "Start")
    public void Start() {
        playAudio();
    }

    @SimpleFunction
    @JsMethod(name = "Pause")
    public void Pause() {
        pauseAudio();
    }

    @SimpleFunction
    @JsMethod(name = "Stop")
    public void Stop() {
        stopAudio();
    }

    @SimpleFunction
    @JsMethod(name = "Vibrate")
    public void Vibrate(int millis) {
        vibrateBrowser(millis);
    }

    private native void setAudioSource(String src) /*-{
        var audio = $wnd.playerAudio;
        if (audio) {
            audio.src = src;
        }
    }-*/;

    private native void setAudioLoop(boolean loop) /*-{
        var audio = $wnd.playerAudio;
        if (audio) {
            audio.loop = loop;
        }
    }-*/;

    private native void setAudioVolume(double volume) /*-{
        var audio = $wnd.playerAudio;
        if (audio) {
            audio.volume = volume;
        }
    }-*/;

    private native void playAudio() /*-{
        var audio = $wnd.playerAudio;
        if (audio) {
            audio.play();
        }
    }-*/;

    private native void stopAudio() /*-{
        var audio = $wnd.playerAudio;
        if (audio) {
            audio.pause();
            audio.currentTime = 0;
        }
    }-*/;

    private native void pauseAudio() /*-{
        var audio = $wnd.playerAudio;
        if (audio) {
            audio.pause();
        }
    }-*/;

    private native boolean isAudioPlaying() /*-{
        var audio = $wnd.playerAudio;
        return audio && !audio.paused;
    }-*/;

    private native void vibrateBrowser(int millis) /*-{
        if (navigator.vibrate) {
            navigator.vibrate(millis);
        }
    }-*/;
    // Not really sure what a browser vibration is meant to look like...
}
