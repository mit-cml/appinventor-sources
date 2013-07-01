// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

//========================================================================================================================================================
//Code Modified by Gareth Haylings 23/06/2013 to add the following functions

//(ADD - Block event) Between_loops 
//	removed mp.setLoop and made this manual to enable event to detect when media finished playing so user can add their own block code

//(ADD - Block property GET added) Sound_length
//	this give the user useful information about duration on media file

//(ADD - Block property GET/SET added) Current_position
//	this give the user useful information about current play position of the media file and allow them to change on the fly

//(Add - Block/Designer property GET/SET) Start_playing_from
//	This gives the use the ability to start the media file from a set time offset. This is implemented into the Start evented when playstate=1

//(Block property GET/SET added) Pan_left_right
//	This will shift the speaker volume from left to right 
//		(-100 = left speaker max volume & right speaker mute volume)
//		(  0  = left speaker max volume & right speaker max volume)
//		( 100 = left speaker mute volume & right speaker max volume)

//(Block property GET added) Volume_Left
//	get the volume of the left speaker

//(Block property GET added) Volume_Right
//	get the volume of the right speaker

//-------------------------------------------------------------------------------------------------------------------------------------------------------



// TODO: Gareth Haylings
// Add Playing events (PlayPaused, PlayResume, PlayStopped, BeforePlayStarts, AfterPlayStarts, Complete, WhilePlaying, BetweenLoops)
//	This will allow user to add block code for process while the sound is playing. Need to investigate adding a timer handler 

// Add events (OnLoopChanged, OnVolumeChanged, OnPanChanged, OnSourceChanged)
//	When a setting is changed these events will take effect


//(Add - Block property GET/SET) End_playing_at
//	This will play the media to a set position. Managed to do this with a while statement but prevents rest of app processing. Need to use a timer interal
//	such as the clock component is handled. Also look ar Onlisten properties


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
import com.google.appinventor.components.runtime.errors.IllegalArgumentError;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;

// TODO: This implementation does nothing about releasing the Media
// Player resources when the application stops.  This needs to be handled
// at the application level, not just at the component level.
// We do release a previously used MediaPlayer before creating a new one.
//
// TODO: This implementation fails when there are multiple media
// players in an application.  This appears to be a bug in the
// Android SDK, or possibly in ODE, but we need to investigate more
// fully.
//
// TODO: Do more extensive testing of how state is handled here to see
// if the state restrictions are adequate given the API, and prove that
// there can't be deadlock or starvation.
/**
 * Multimedia component that plays audio or video and optionally
 * vibrates.  It is built on top of {@link android.media.MediaPlayer}.
 *
 * @author halabelson@google.com (Hal Abelson)
 */
//TODO: Gareth Haylings - update description below
@DesignerComponent(version = YaVersion.PLAYER_COMPONENT_VERSION,
	description = "<p>Multimedia component that plays audio or video and " +
	"controls phone vibration.  The name of a multimedia field is " +
	"specified in the <code>Source</code> property, which can be set in " +
	"the Designer or in the Blocks Editor.  The length of time for a " +
	"vibration is specified in the Blocks Editor in milliseconds " +
	"(thousandths of a second).</p>" +
	"<p>For legal sound and video formats, see " +
	"<a href=\"http://developer.android.com/guide/appendix/media-formats.html\"" +
	" target=\"_blank\">Android Supported Media Formats</a>.</p>" +
	"<p>If you will only be playing sound files and vibrating, not using " +
	"video, this component is best for long sound files, such as songs, " +
	"while the <code>Sound</code> component is more efficient for short " +
	"files, such as sound effects.</p>",
	category = ComponentCategory.MEDIA,
	nonVisible = true,
	iconName = "images/player.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.VIBRATE, android.permission.INTERNET")
public final class Player extends AndroidNonvisibleComponent 
implements Component, OnCompletionListener, OnDestroyListener, Deleteable 
{

	private static final boolean DEBUG = false;
	
	private MediaPlayer mp;
	private final Vibrator vibe;
	
	private int playerState;
	private String sourcePath;
	
	// determines if playing should loop
	private boolean loop;
	private int playstarttime; //start time for playing sound
	private int soundlength; //length of current sound

	private int volume_left;                 // volume of left speaker
	private int volume_right;                // volume of right speaker
	private int max_volume;                  // volume max level
	private int pan_leftright;               // Pan speaker left/right balance
	/*
	* playerState encodes a simplified version of the full MediaPlayer state space, that should be
	* adequate, given this API:
	* 0: player initial state
	* 1: player prepared but not started
	* 2: player started or paused
	* The allowable transitions are:
	* Start: must be called in state 1 or 2, results in state 2
	* Pause: must be called in state 2, results in state 2
	* Stop: must be called in state 1 or 2, results in state 1
	* We can simplify this to remove state 0 and use a simple boolean after we're
	* more confident that there are no start-up problems.
	*/
	
	/**
	* Creates a new Player component.
	*
	* @param container
	*/
	public Player(ComponentContainer container)
	{
		super(container.$form());
		sourcePath = "";
		vibe = (Vibrator) form.getSystemService(Context.VIBRATOR_SERVICE);
		form.registerForOnDestroy(this);
		// Make volume buttons control media, not ringer.
		form.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		loop = false;
		soundlength = 0;
		playstarttime = 0;
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//designer view - (Source)
	/**
	* Returns the path to the audio or video source
	*/
	@SimpleProperty(
		category = PropertyCategory.BEHAVIOR)
	public String Source()
	{
		return sourcePath;
	}
	
	/**
	* Sets the audio or video source.
	*
	* <p/>See {@link MediaUtil#determineMediaSource} for information about what
	* a path can be.
	*
	* @param path  the path to the audio or video source
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
		defaultValue = "")
	@SimpleProperty
	public void Source(String path)
	{
		sourcePath = (path == null) ? "" : path;
		// Clear the previous MediaPlayer.
		if (playerState == 1 || playerState == 2) {mp.stop();}
		playerState = 0;
		if (mp != null)
		{
			mp.release();
			mp = null;
		}
		if (sourcePath.length() > 0)
		{
			if (DEBUG) {Log.i("Player", "Source path is " + sourcePath);}
			mp = new MediaPlayer();
			mp.setOnCompletionListener(this);
			try
			{
				MediaUtil.loadMediaPlayer(mp, form, sourcePath);
			} 
			catch (IOException e)
			{
				mp.release();
				mp = null;
				form.dispatchErrorOccurredEvent(this, "Source",ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, sourcePath);
				return;
			}
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			if (DEBUG) {Log.i("Player", "Successfully loaded source path " + sourcePath);}
		
			// The Simple API is set up so that the user never has
			// to call prepare.
			prepare();
			// Player should now be in state 1. (If prepare failed, we are in state 0.)
		}
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//designer view -(Start_playing_from)
	/**
	* Returns the path to the audio or video source
	*/
	@SimpleProperty(
		category = PropertyCategory.BEHAVIOR)
	public int Start_playing_from()
	{
		return playstarttime;
	}
	
	@DesignerProperty(
		editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
		defaultValue = "0")
	@SimpleProperty(
		description = "Start position to play sound from in millseconds")
	public void Start_playing_from(int startpos)
	{
		if (startpos < 0) {playstarttime = 0;}
		else if (startpos > soundlength) {playstarttime = soundlength;}
		else {playstarttime = startpos;}
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//designer view -(Loop)
	/**
	* Reports whether the playing should loop.
	*/
	@SimpleProperty(
		description = "If true, the player will loop when it plays. Setting Loop while the player " +
			"is playing will affect the current playing.",
		category = PropertyCategory.BEHAVIOR)
	public boolean Loop()
	{
		return loop;
	}
	
	/**
	* Sets the looping property to true or false.
	*
	* @param shouldLoop determines if the playing should loop
	*/
	@DesignerProperty(
		editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
		defaultValue = "False")
	@SimpleProperty
	public void Loop(boolean shouldLoop)
	{
		// even if the player is not prepared, it will be set according to
		// Loop the next time it is started
		loop = shouldLoop;
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------
	//designer view - (Volume)
	/**
	* Sets the volume property to a number between 0 and 100.
	*
	* @param vol  the desired volume level
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
	//Designer view - (Pan_left_right)
	/**
	* SER speaker Pan left right
	* pan volume from left to right settings (-100 to 100)
	* -100 - left speaker full & right speaker mute
	* 0    - left speaker full & right speaker full
	* 100  - left speaker mute & right speaker full
	*
	* Once the sound starts playing, all further Play() calls will be ignored
	* until the interval has elapsed.
	* @return  minimum interval in ms
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
		if (playerState == 1 || playerState == 2)
		{
			mp.setVolume(((float) volume_left)/100 , ((float) volume_right)/100);
			if (DEBUG) {Log.i("Player", "Left volume is " + String.valueOf(volume_left) + " Right volume is " + String.valueOf(volume_right));}
		}
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
	
	//================================================================================================================================
	//Block editor property (IsPlaying)
	/**
	* Reports whether the media is playing.
	*/
	@SimpleProperty(
		description = "Reports whether the media is playing",
		category = PropertyCategory.BEHAVIOR)
		public boolean IsPlaying()
		{
			if (playerState == 1 || playerState == 2) {return mp.isPlaying();}
			else {return false;}
		}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//Block editor GET property (Sound_length) 
	@SimpleProperty(
		description = "Length of sound in milliseconds",
		category = PropertyCategory.BEHAVIOR)
	public int Sound_length() 
	{
		if (mp != null) 
		{
			soundlength = mp.getDuration();
			return soundlength;
		}
		else 
		{
			return 0;
		}
	}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//Block editor GET property (Current_position) 
	@SimpleProperty(
		description = "current position of sound in milliseconds",
		category = PropertyCategory.BEHAVIOR)
	public int Current_position()
	{
		if (mp != null) {return mp.getCurrentPosition();}
		else {return 0;}
	}
		
	/**
	* Set the play position for the current.
	*/	
	@SimpleProperty
	public void Current_position(int pos)
	{
		if (mp != null) 
		{
			if (pos > soundlength) {mp.seekTo(soundlength);}
			else {mp.seekTo(pos);}
		}
	}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//Block editor SET property (Vibrate) 
	//  TODO: Reconsider whether vibrate should be here or in a separate component.
	/**
	* Vibrates for specified number of milliseconds.
	*/
	@SimpleFunction
		public void Vibrate(long milliseconds)
		{
			vibe.vibrate(milliseconds);
		}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//Block editor GET property (PlayerError) not visible
	@SimpleEvent(
		description = "The PlayerError event is no longer used. " +
			"Please use the Screen.ErrorOccurred event instead.",
		userVisible = false)
		public void PlayerError(String message) {}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//Block editor EVENT (Start)
	/**
	* Plays the media.  If it was previously paused, the playing is resumed.
	* If it was previously stopped, it starts from the beginning.
	*/
	@SimpleFunction
		public void Start()
		{
			if (DEBUG) {Log.i("Player", "Calling Start -- State=" + playerState);}
			if (playerState == 1 || playerState == 2)
			{
				if (playerState == 1) {mp.seekTo(playstarttime);}
				mp.start();
				playerState = 2;
				// Player should now be in state 2
			}
		}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//Block editor EVENT (Pause)
		
	/**
	* Suspends playing the media if it is playing.
	*/
	@SimpleFunction
		public void Pause()
		{
			if (DEBUG) {Log.i("Player", "Calling Pause -- State=" + playerState);}
			if (playerState == 2)
			{
				mp.pause();
				playerState = 2;
				// Player should now be in state 2.
			}
		}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------
	//Block editor EVENT (Stop)
	/**
	* Stops playing the media and seeks to the beginning of the song.
	*/
	@SimpleFunction
		public void Stop()
		{
			if (DEBUG) {Log.i("Player", "Calling Stop -- State=" + playerState);}
			if (playerState == 1 || playerState == 2)
			{
				mp.stop();
				prepare();
				mp.seekTo(0);
				// Player should now be in state 1. (If prepare failed, we are in state 0.)
			}
		}

	//--------------------------------------------------------------------------------------------------------------------------------
	private void prepare()
	{
		// This should be called only after mp.stop() or directly after
		// initialization
		try
		{
			mp.prepare();
			playerState = 1;
			if (DEBUG) {Log.i("Player", "Successfully prepared");}
		}
		catch (IOException ioe)
		{
			mp.release();
			mp = null;
			playerState = 0;
			form.dispatchErrorOccurredEvent(this, "Source",ErrorMessages.ERROR_UNABLE_TO_PREPARE_MEDIA, sourcePath);
		}
	}
	
	// OnCompletionListener implementation
	@Override
		public void onCompletion(MediaPlayer m)
		{
			//When sound finished playing check if looping is on. If looping on call event to restart
			//player and allow user to add there own block code 
			playerState = 1; //set playstate to show start over
			if (loop == false) {Completed();}
			else {Between_loops();}             
		}
	
	/**
	* Indicates that the media has reached the end
	*/
	@SimpleEvent
		public void Completed()
		{
			if (DEBUG) {Log.i("Player", "Calling Completed -- State=" + playerState);}
			EventDispatcher.dispatchEvent(this, "Completed");
		}
	
	/**
	* Indicates that the media has reached the end but is to play again
	*/
	@SimpleEvent
		public void Between_loops()
		{
			if (DEBUG) {Log.i("Player", "Calling Between_loops -- State=" + playerState);}
			EventDispatcher.dispatchEvent(this, "Between_loops");
			Start();
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
		// TODO(lizlooney) - add descriptively named constants for these magic numbers.
		if (playerState == 1 || playerState == 2) {mp.stop();}
		playerState = 0;
		if (mp != null)
		{
			mp.release();
			mp = null;
		}
		vibe.cancel();
	}
}
