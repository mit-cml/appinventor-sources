// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
//========================================================================================================================================================
//Code Modified by Gareth Haylings 26/06/2013 to add the following functions

//(Block property GET/SET added) Pan_left_right
//	This will shift the speaker volume from left to right 
//		(-100 = left speaker max volume & right speaker mute volume)
//		(  0  = left speaker max volume & right speaker max volume)
//		( 100 = left speaker mute volume & right speaker max volume)

//(Block property GET/SET added) Volume
//	This will set the maximum level of the speakers
//		(  0 = 0% Mute sound)
//		(100 = 100% maximum volume)

//(Block property GET added) Volume_Left
//	get the volume of the left speaker

//(Block property GET added) Volume_Right
//	get the volume of the right speaker

//(Block property GET/SET added) Loop 
//	make the sound repeat

//(Block property GET added) IsPlaying
//	Reports whether the media is playing.


//-------------------------------------------------------------------------------------------------------------------------------------------------------


// TODO: Gareth Haylings
// Add Playing events (PlayPaused, PlayResume, PlayStopped, BeforePlayStarts, AfterPlayStarts, Complete, WhilePlaying, BetweenLoops)
//	This will allow user to add block code for process while the sound is playing. Need to investigate adding a timer handler 

// Add events (OnLoopChanged, OnVolumeChanged, OnPanChanged, OnSpeedChanged, OnSourceChanged)
//	When a setting is changed these events will take effect

//========================================================================================================================================================
package com.google.appinventor.components.runtime;

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
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Multimedia component that plays sounds and optionally vibrates.  A
 * sound is specified via filename.  See also
 * {@link android.media.SoundPool}.
 *
 * @author sharon@google.com (Sharon Perl)
 */
@DesignerComponent(
	version = YaVersion.SOUND_COMPONENT_VERSION,
	description = "<p>Multimedia component for playing short sounds</p>" +
		"<b><u>Property Settings</u></b><br>" +
		"<b>Source</b> (Name of the source sound file for the project assets<br>" +
		"For legal sound and video formats, see " +
		"<a href=\"http://developer.android.com/guide/appendix/media-formats.html\"" +
		" target=\"_blank\">Android Supported Media Formats</a><br>" +
		"<br>" +
		"<b>Minimuminteral</b> (minumum time before play next sound on same component)<br>" +
		"    Setting is in thousandths of a second (e.g. 500 = half second)<br>" +
		"<br>" +
		"<b>Volume</b> (set the volume)<br>" +
		"    Setting is in % between 0 to 100<br>" +
		"    0   = Mute sound<br>" +
		"    100 = Maximum volume<br>" +
		"<br>" +
		"<b>Play_speed</b> (Speed of sound when played)<br>" +
		"    Setting is % <i>(integer greater than 1)</i><br>" +
		"    example  50 = Half or 50% play speed    (effect of slow and low pitch sound)<br>" +
		"    example 100 = Normal or 100% play speed (Play sound at orinial source)<br>" +
		"    example 200 = Double or 200% play speed (effect of faste and high pitch sound)<br>" +
		"<br>" +
		"<b>Pan_left_right</b> (Shift sound from left to right speaker)<br>" +
		"<i>(Note: This setting will on work on devices with stero speakers)</i><br>" +
		"    Setting is % <i>(int -100 to 100</i><br>" +
		"    -100 = Left speaker full on    Right speaker muted<br>" +
		"    -100 = Left speaker full on    Right speaker full on<br>" +
		"    -100 = Light muted             Right speaker full on<br>" +
		"<br>" +
		"<b>Loop_ON</b> (Set the sound to auto repeat)<br>" +
		"    Setting is boolean<br>" +
		"<p>This component is best for short sound files, such as sound " +
		"effects, while the <code>Player</code> component is more efficient for " +
		"longer sounds, such as songs.</p>",
	category = ComponentCategory.MEDIA,
	nonVisible = true,
	iconName = "images/soundEffect.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.VIBRATE, android.permission.INTERNET")
public class Sound extends AndroidNonvisibleComponent
	implements Component, OnResumeListener, OnStopListener, OnDestroyListener, Deleteable
{
	private static final int MAX_STREAMS = 10;
	private SoundPool soundPool;
	// soundMap maps sounds (assets, etc) that are loaded into soundPool to their respective
	// soundIds.
	private final Map<String, Integer> soundMap;
	
	private String sourcePath;              // name of source
	private int soundId;                    // id of sound in the soundPool
	private int streamId;                   // stream id returned from last call to SoundPool.play
	private int minimumInterval;            // minimum interval between Play() calls
	private long timeLastPlayed;            // the system time when Play() was last called
	private final Vibrator vibe;
	
	private int playback_rate;               // speed of playback
	private int volume_left;                 // volume of left speaker
	private int volume_right;                // volume of right speaker
	private int max_volume;                  // volume max level
	private int looping_mode;                // Sound loop on/off
	private int pan_leftright;               // Pan speaker left/right balance
	
	
	//-----------------------------------------------------------------------------------------
	public Sound(ComponentContainer container) 
	{
		super(container.$form());
		soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
		soundMap = new HashMap<String, Integer>();
		vibe = (Vibrator) form.getSystemService(Context.VIBRATOR_SERVICE);
		sourcePath = "";
		form.registerForOnResume(this);
		form.registerForOnStop(this);
		form.registerForOnDestroy(this);
		
		// Make volume buttons control media, not ringer.
		form.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		// Default property values
		MinimumInterval(500);
		Play_speed(100);  
		Pan_left_right(0);
		Volume(50);
		Loop(false);
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//(Source) Property
	/**
	* Returns the sound's filename.
	*/
	@SimpleProperty(
		category = PropertyCategory.BEHAVIOR,
		description = "The name of the sound file.  Only <a href=\"" +
			"http://developer.android.com/guide/appendix/media-formats.html" +
			"\">certain formats</a> are supported.")
	public String Source()
	{
		return sourcePath;
	}
	
	/**
	* Sets the sound source
	*
	* <p/>See {@link MediaUtil#determineMediaSource} for information about what
	* a path can be.
	*
	* @param path  the path to the sound source
	*/
	@DesignerProperty(
		editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
		defaultValue = "")
	@SimpleProperty
	public void Source(String path)
	{
		sourcePath = (path == null) ? "" : path;
		
		// Clear the previous sound.
		if (streamId != 0)
		{
			soundPool.stop(streamId);
			streamId = 0;
		}
		soundId = 0;
		
		if (sourcePath.length() != 0)
		{
			Integer existingSoundId = soundMap.get(sourcePath);
			if (existingSoundId != null)
			{
				soundId = existingSoundId;
			}
			else
			{
				Log.i("Sound", "No existing sound with path " + sourcePath + ".");
				try
				{
					int newSoundId = MediaUtil.loadSoundPool(soundPool, form, sourcePath);
					if (newSoundId != 0)
					{
						soundMap.put(sourcePath, newSoundId);
						Log.i("Sound", "Successfully loaded sound: setting soundId to " + newSoundId + ".");
						soundId = newSoundId;
					}
					else
					{
						form.dispatchErrorOccurredEvent(this, "Source",
						ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, sourcePath);
					}
				}
				catch (IOException e)
				{
					form.dispatchErrorOccurredEvent(this, "Source",
					ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, sourcePath);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//(Loop) Property
	/**
	* Set the looping mode on/off
	* Looping settings True or False
	* True convert to -1 = loop for ever
	* True convert to 0 = loop off
	*
	* Once the sound starts playing, all further Play() calls will be ignored
	* until the interval has elapsed.
	* @return  minimum interval in ms
	*/
	@DesignerProperty(
		editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
		defaultValue = "FALSE")
	@SimpleProperty
	public void Loop(boolean shouldloop) 
	{
		if (shouldloop == true) {looping_mode = -1;}
		else {looping_mode = 0;}
		// if sound playing reset the sound loop state
		if (streamId != 0) {soundPool.setLoop(streamId, looping_mode);}
	}
	
	/**
	* Returns the looping mode on/off
	*/
	@SimpleProperty(
		category = PropertyCategory.BEHAVIOR,
		description = "Looping state (True=ON  False=OFF)")
	public boolean Loop()
	{
		if (looping_mode == -1) {return true;}
		else {return false;}
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//(Volume) Property
   /**
   * Set the max Volume for left & right speaker
   * Volume setting range 0 - 100
   * 0   - Mute
   * 100 - maximum volume 
	*/
	@DesignerProperty(
		editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
		defaultValue = "50")
	@SimpleProperty(
		description = "Sets the volume to a number between 0 and 100")
	public void Volume(int volume)
	{
		//If value outside limit the reset value to upper/lower limit
		if (volume >100) {max_volume = 100;}
		else if (volume <0) {max_volume = 0;}
		else {max_volume = volume;}
		set_volume();
	}
	
	/**
	* Returns the max Volume for left & right speaker
	*/
	@SimpleProperty(
		category = PropertyCategory.BEHAVIOR,
		description = "Volume 0 to 100.")
	public int Volume()
	{
		return max_volume;
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//(Pan_left_right) Property
	/**
	* SET speaker Pan left right
	* pan volume from left to right settings (-100 to 100)
	* -100 - left speaker full & right speaker mute
	* 0    - left speaker full & right speaker full
	* 100  - left speaker mute & right speaker full
	*/
	@DesignerProperty(
		editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
		defaultValue = "0")
	@SimpleProperty
	public void Pan_left_right(int pan)
	{
		//If value outside limit the reset value to upper/lower limit
		if (pan < -100) {pan_leftright = -100;}
		else if (pan > 100) {pan_leftright = 100;}
		else {pan_leftright = pan;}
		set_volume();
	}
	
	
	/**
	* GET speaker Pan left right
	*/
	@SimpleProperty(
		category = PropertyCategory.BEHAVIOR,
		description = "Speaker Left Right balance -100 to 100.")
	public int Pan_left_right()
	{
		return pan_leftright;
	} 
	
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//Set the volume of speak based on the max_volume and pan_leftright
	private void set_volume()
	{
		//Code to reset left & right speakers calculated on the pan value 
		//IF(pan<0,100,100+pan) right pan
		//IF(pan>0,100,100-pan) left pan
		//
		//TODO: Gareth Haylings: Make improvement speaker balance as max_volume reduced and increase
		if (pan_leftright < 0) {volume_left  = 100;}
		else {volume_left = 100 - pan_leftright;}
		if (pan_leftright > 0) {volume_right = 100;}
		else {volume_right = 100 + pan_leftright;}
		
		if (volume_right > max_volume) {volume_right = max_volume;}
		if (volume_left > max_volume) {volume_left = max_volume;}
		
		// if sound playing reset the volume level
		if (streamId != 0) {soundPool.setVolume(streamId, ((float) volume_left)/100 , ((float) volume_right)/100);}
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//Get the left speaker volume
	@SimpleProperty(
		category = PropertyCategory.BEHAVIOR,
		description = "Volume 0 to 100.")
	public int Volume_Left()
	{
		return volume_left;
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//Get the right speaker volume
	@SimpleProperty(
		category = PropertyCategory.BEHAVIOR,
		description = "Volume 0 to 100.")
	public int Volume_Right()
	{
		return volume_right;
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//(Play_speed) Property
	/**
	* Playback Speed setting range 0.5 - 2.0
	* 50  - 50% or half speed (lower pitch)
	* 100 - 100% or normal speed 
	* 300 - 300% Tripple speed (high pitch)
	*/
	@DesignerProperty(
		editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
		defaultValue = "100")
	@SimpleProperty
	public void Play_speed(int rate)
	{
		//If value outside limit the reset value to lowest limit value
		if (rate < 1) {playback_rate = 1;}
		else {playback_rate = rate;}
		// if sound playing reset the sound playback speed
		if (streamId != 0) {soundPool.setRate(streamId, ((float) playback_rate)/100);}
	}
	
	/**
	* Get the Playback speed
	*/
	@SimpleProperty(
		category = PropertyCategory.BEHAVIOR,
		description = "Playing speed 0 and above.")
	public int Play_speed()
	{
		return playback_rate;
	} 
	
	
	//================================================================================================================================
	//(IsPlaying)
	/**
	* Reports whether the media is playing.
	*/
	@SimpleProperty(
		category = PropertyCategory.BEHAVIOR,
		description = "Playing state.")
	public boolean IsPlaying()
	{
		if (streamId != 0) {return true;}
		else {return false;}
	} 
	
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//Set the minimal interval in milliseconds before next sound can be played
	/**
	* Once the sound starts playing, all further Play() calls will be ignored
	* until the interval has elapsed.
	* @param interval  minimum interval in ms
	*/
	@DesignerProperty(
		editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
		defaultValue = "500")
	@SimpleProperty
	public void MinimumInterval(int interval)
	{
		minimumInterval = interval;
	}
	
	/**
	* Get the IinimumInterval between sounds beeing playedd
	*/
	@SimpleProperty(
		category = PropertyCategory.BEHAVIOR,
		description = "Interval in milliseconds before next sound can be played")
	public int MinimumInterval()
	{
		return minimumInterval;
	} 
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//(Vibrate) make the device vibrate for a speciefied nuber of milliseconds.
	//  TODO: Reconsider whether vibrate should be here or in a separate component.
	@SimpleFunction
	public void Vibrate(int millisecs)
	{
		vibe.vibrate(millisecs);
	}
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//Block editor GET property (SoundError) not visible
	@SimpleEvent(
		description = "The SoundError event is no longer used. " +
			"Please use the Screen.ErrorOccurred event instead.",
		userVisible = false)
	public void SoundError(String message) {}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//(Play) Playing control
	/**
	* Plays the sound.
	*/
	@SimpleFunction
	public void Play()
	{
		if (soundId != 0)
		{
			long currentTime = System.currentTimeMillis();
			if (timeLastPlayed == 0 || currentTime >= timeLastPlayed + minimumInterval)
			{
				timeLastPlayed = currentTime;
				streamId = soundPool.play(soundId, ((float) volume_left)/100 , ((float) volume_right)/100, 0, looping_mode,   ((float) playback_rate)/100);
				Log.i("Sound", "SoundPool.play returned stream id " + streamId);
				if (streamId == 0) {form.dispatchErrorOccurredEvent(this, "Play",ErrorMessages.ERROR_UNABLE_TO_PLAY_MEDIA, sourcePath);}
			}
			else
			{
				Log.i("Sound", "Unable to play because MinimumInterval has not elapsed since last play.");
			}
		}
		else
		{
			Log.i("Sound", "Unable to play. Did you remember to set the Source property?");
		}
	}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//(Pause) Playing control
	/**
	* Pauses playing the sound if it is being played.
	*/
	@SimpleFunction
	public void Pause()
	{
		if (streamId != 0) {soundPool.pause(streamId);}
		else {Log.i("Sound", "Unable to pause. Did you remember to call the Play function?");}
	}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//(Resume) Playing control
	/**
	* Resumes playing the sound after a pause.
	*/
	@SimpleFunction
	public void Resume()
	{
		if (streamId != 0) {soundPool.resume(streamId);}
		else {Log.i("Sound", "Unable to resume. Did you remember to call the Play function?");}
	}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//(Stop) Playing control
	/**
	* Stops playing the sound if it is being played.
	*/
	@SimpleFunction
	public void Stop()
	{
		if (streamId != 0)
		{
			soundPool.stop(streamId);
			streamId = 0;
		}
		else
		{
			Log.i("Sound", "Unable to stop. Did you remember to call the Play function?");
		}
	}
	
	
	//================================================================================================================================
	// OnStopListener implementation
	@Override
	public void onStop()
	{
		Log.i("Sound", "Got onStop");
		if (streamId != 0) {soundPool.pause(streamId);}
	}
	
	
	// OnResumeListener implementation
	@Override
	public void onResume()
	{
		Log.i("Sound", "Got onResume");
		if (streamId != 0) {soundPool.resume(streamId);}
	}
	
	
	// OnDestroyListener implementation
	@Override
	public void onDestroy()
	{
		prepareToDie();
	}
	
	
	// Deleteable implementation
	@Override
	public void onDelete()
	{
		prepareToDie();
	}
	
	
	private void prepareToDie()
	{
		if (streamId != 0)
		{
			soundPool.stop(streamId);
			soundPool.unload(streamId);
		}
		soundPool.release();
		vibe.cancel();
		// The documentation for SoundPool suggests setting the reference to null;
		soundPool = null;
	}
}
