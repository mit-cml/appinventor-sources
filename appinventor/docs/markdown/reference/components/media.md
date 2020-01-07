---
layout: documentation
title: Media
---

[&laquo; Back to index](index.html)
# Media

Table of Contents:

* [Camcorder](#Camcorder)
* [Camera](#Camera)
* [ImagePicker](#ImagePicker)
* [Player](#Player)
* [Sound](#Sound)
* [SoundRecorder](#SoundRecorder)
* [SpeechRecognizer](#SpeechRecognizer)
* [TextToSpeech](#TextToSpeech)
* [VideoPlayer](#VideoPlayer)
* [YandexTranslate](#YandexTranslate)

## Camcorder  {#Camcorder}

### Events  {#Camcorder-Events}

{:.events}

{:id="Camcorder.AfterRecording"} AfterRecording(*clip*{:.text})
: Indicates that a video was recorded with the camera and provides the path to
 the stored picture.

### Methods  {#Camcorder-Methods}

{:.methods}

{:id="Camcorder.RecordVideo" class="method"} <i/> RecordVideo()
: Records a video, then raises the AfterRecoding event.

## Camera  {#Camera}

### Properties  {#Camera-Properties}

{:.properties}

{:id="Camera.UseFront" .boolean .bo} *UseFront*
: Returns true if the front-facing camera is to be used (when available)

### Events  {#Camera-Events}

{:.events}

{:id="Camera.AfterPicture"} AfterPicture(*image*{:.text})
: Indicates that a photo was taken with the camera and provides the path to
 the stored picture.

### Methods  {#Camera-Methods}

{:.methods}

{:id="Camera.TakePicture" class="method"} <i/> TakePicture()
: Takes a picture, then raises the AfterPicture event.
 If useFront is true, adds an extra to the intent that requests the front-facing camera.

## ImagePicker  {#ImagePicker}

### Properties  {#ImagePicker-Properties}

{:.properties}

{:id="ImagePicker.BackgroundColor" .color} *BackgroundColor*
: Returns the button's background color

{:id="ImagePicker.Enabled" .boolean} *Enabled*
: If set, user can tap ImagePicker to cause action.

{:id="ImagePicker.FontBold" .boolean} *FontBold*
: If set, ImagePicker text is displayed in bold.

{:id="ImagePicker.FontItalic" .boolean} *FontItalic*
: If set, ImagePicker text is displayed in italics.

{:id="ImagePicker.FontSize" .number} *FontSize*
: Point size for ImagePicker text.

{:id="ImagePicker.FontTypeface" .number .do} *FontTypeface*
: Font family for ImagePicker text.

{:id="ImagePicker.Height" .number .bo} *Height*
: Specifies the vertical height of the ImagePicker, measured in pixels.

{:id="ImagePicker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the ImagePicker as a percentage of the height of the Screen.

{:id="ImagePicker.Image" .text} *Image*
: Image to display on button.

{:id="ImagePicker.Selection" .text .ro .bo} *Selection*
: Path to the file containing the image that was selected.

{:id="ImagePicker.Shape" .number .do} *Shape*
: Returns the style of the button.

{:id="ImagePicker.ShowFeedback" .boolean} *ShowFeedback*
: Specifies if a visual feedback should be shown  for a ImagePicker that as an image as background.

{:id="ImagePicker.Text" .text} *Text*
: Text to display on ImagePicker.

{:id="ImagePicker.TextAlignment" .number .do} *TextAlignment*
: Left, center, or right.

{:id="ImagePicker.TextColor" .color} *TextColor*
: Color for button text.

{:id="ImagePicker.Visible" .boolean} *Visible*
: Returns true iff the ImagePicker is visible.

{:id="ImagePicker.Width" .number .bo} *Width*
: Specifies the horizontal width of the ImagePicker, measured in pixels.

{:id="ImagePicker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the ImagePicker as a percentage of the width of the Screen.

### Events  {#ImagePicker-Events}

{:.events}

{:id="ImagePicker.AfterPicking"} AfterPicking()
: Event to be raised after the picker activity returns its
 result and the properties have been filled in.

{:id="ImagePicker.BeforePicking"} BeforePicking()
: Event to raise when the ImagePicker is clicked or the list is shown
 using the Open block.  This event occurs before the list of items is displayed, and 
 can be used to prepare the list before it is shown.

{:id="ImagePicker.GotFocus"} GotFocus()
: Indicates the cursor moved over the ImagePicker so it is now possible to click it.

{:id="ImagePicker.LostFocus"} LostFocus()
: Indicates the cursor moved away from the ImagePicker so it is now no longer possible to click it.

{:id="ImagePicker.TouchDown"} TouchDown()
: Indicates that the ImagePicker was pressed down.

{:id="ImagePicker.TouchUp"} TouchUp()
: Indicates that the ImagePicker has been released.

### Methods  {#ImagePicker-Methods}

{:.methods}

{:id="ImagePicker.Open" class="method"} <i/> Open()
: Opens the picker, as though the user clicked on it.

## Player  {#Player}

### Properties  {#Player-Properties}

{:.properties}

{:id="Player.IsPlaying" .boolean .ro .bo} *IsPlaying*
: Reports whether the media is playing

{:id="Player.Loop" .boolean} *Loop*
: If true, the player will loop when it plays. Setting Loop while the player is playing will affect the current playing.

{:id="Player.PlayOnlyInForeground" .boolean} *PlayOnlyInForeground*
: If true, the player will pause playing when leaving the current screen; if false (default option), the player continues playing whenever the current screen is displaying or not.

{:id="Player.Source" .text} *Source*
: Returns the path to the audio source

{:id="Player.Volume" .number .wo} *Volume*
: Sets the volume to a number between 0 and 100

### Events  {#Player-Events}

{:.events}

{:id="Player.Completed"} Completed()
: Indicates that the media has reached the end

{:id="Player.OtherPlayerStarted"} OtherPlayerStarted()
: This event is signaled when another player has started (and the current player is playing or paused, but not stopped).

### Methods  {#Player-Methods}

{:.methods}

{:id="Player.Pause" class="method"} <i/> Pause()
: Suspends playing the media if it is playing.

{:id="Player.Start" class="method"} <i/> Start()
: Plays the media.  If it was previously paused, the playing is resumed.
 If it was previously stopped, it starts from the beginning.

{:id="Player.Stop" class="method"} <i/> Stop()
: Stops playing the media and seeks to the beginning of the song.

{:id="Player.Vibrate" class="method"} <i/> Vibrate(*milliseconds*{:.number})
: Vibrates for specified number of milliseconds.

## Sound  {#Sound}

### Properties  {#Sound-Properties}

{:.properties}

{:id="Sound.MinimumInterval" .number} *MinimumInterval*
: The minimum interval, in milliseconds, between sounds.  If you play a sound, all further Play() calls will be ignored until the interval has elapsed.

{:id="Sound.Source" .text} *Source*
: The name of the sound file.  Only certain formats are supported.  See http://developer.android.com/guide/appendix/media-formats.html.

### Events  {#Sound-Events}

{:.events}

### Methods  {#Sound-Methods}

{:.methods}

{:id="Sound.Pause" class="method"} <i/> Pause()
: Pauses playing the sound if it is being played.

{:id="Sound.Play" class="method"} <i/> Play()
: Plays the sound specified by the Source property.

{:id="Sound.Resume" class="method"} <i/> Resume()
: Resumes playing the sound after a pause.

{:id="Sound.Stop" class="method"} <i/> Stop()
: Stops playing the sound if it is being played.

{:id="Sound.Vibrate" class="method"} <i/> Vibrate(*millisecs*{:.number})
: Vibrates for the specified number of milliseconds.

## SoundRecorder  {#SoundRecorder}

### Properties  {#SoundRecorder-Properties}

{:.properties}

{:id="SoundRecorder.SavedRecording" .text} *SavedRecording*
: Specifies the path to the file where the recording should be stored. If this proprety is the empty string, then starting a recording will create a file in an appropriate location.  If the property is not the empty string, it should specify a complete path to a file in an existing directory, including a file name with the extension .3gp.

### Events  {#SoundRecorder-Events}

{:.events}

{:id="SoundRecorder.AfterSoundRecorded"} AfterSoundRecorded(*sound*{:.text})
: Provides the location of the newly created sound.

{:id="SoundRecorder.StartedRecording"} StartedRecording()
: Indicates that the recorder has started, and can be stopped.

{:id="SoundRecorder.StoppedRecording"} StoppedRecording()
: Indicates that the recorder has stopped, and can be started again.

### Methods  {#SoundRecorder-Methods}

{:.methods}

{:id="SoundRecorder.Start" class="method"} <i/> Start()
: Starts recording.

{:id="SoundRecorder.Stop" class="method"} <i/> Stop()
: Stops recording.

## SpeechRecognizer  {#SpeechRecognizer}

### Properties  {#SpeechRecognizer-Properties}

{:.properties}

{:id="SpeechRecognizer.Result" .text .ro .bo} *Result*
: Result property getter method.

{:id="SpeechRecognizer.UseLegacy" .boolean} *UseLegacy*
: If true, an app can retain their older behaviour.

### Events  {#SpeechRecognizer-Events}

{:.events}

{:id="SpeechRecognizer.AfterGettingText"} AfterGettingText(*result*{:.text},*partial*{:.boolean})
: Simple event to raise after the VoiceReco activity has returned.
 Partial is 'false' when SpeechRecognizer stops automatically after listening,
 else it is 'true' if SpeechRecognizer returns partial results.

{:id="SpeechRecognizer.BeforeGettingText"} BeforeGettingText()
: Simple event to raise when VoiceReco is invoked but before the VoiceReco
 activity is started.

### Methods  {#SpeechRecognizer-Methods}

{:.methods}

{:id="SpeechRecognizer.GetText" class="method"} <i/> GetText()
: Solicits speech input from the user.  After the speech is converted to
 text, the AfterGettingText event will be raised.

{:id="SpeechRecognizer.Stop" class="method"} <i/> Stop()
: Function used to forcefully stop listening speech in cases where
 SpeechRecognizer cannot stop automatically.
 This function works only when UseLegacy property is set to 'false'.

## TextToSpeech  {#TextToSpeech}

### Properties  {#TextToSpeech-Properties}

{:.properties}

{:id="TextToSpeech.AvailableCountries" .list .ro .bo} *AvailableCountries*
: List of the country codes available on this device for use with TextToSpeech.  Check the Android developer documentation under supported languages to find the meanings of these abbreviations.

{:id="TextToSpeech.AvailableLanguages" .list .ro .bo} *AvailableLanguages*
: List of the languages available on this device for use with TextToSpeech.  Check the Android developer documentation under supported languages to find the meanings of these abbreviations.

{:id="TextToSpeech.Country" .text} *Country*
: Country code to use for speech generation.  This can affect the pronounciation.  For example, British English (GBR) will sound different from US English (USA).  Not every country code will affect every language.

{:id="TextToSpeech.Language" .text} *Language*
: Sets the language for TextToSpeech. This changes the way that words are pronounced, not the actual language that is spoken.  For example setting the language to and speaking English text with sound like someone speaking English with a Frernch accent.

{:id="TextToSpeech.Pitch" .number} *Pitch*
: Sets the Pitch for TextToSpeech The values should be between 0 and 2 where lower values lower the tone of synthesized voice and greater values raise it.

{:id="TextToSpeech.Result" .boolean .ro .bo} *Result*
: Result property getter method.

{:id="TextToSpeech.SpeechRate" .number} *SpeechRate*
: Sets the SpeechRate for TextToSpeech. The values should be between 0 and 2 where lower values slow down the pitch and greater values accelerate it.

### Events  {#TextToSpeech-Events}

{:.events}

{:id="TextToSpeech.AfterSpeaking"} AfterSpeaking(*result*{:.boolean})
: Event to raise after the message is spoken.

{:id="TextToSpeech.BeforeSpeaking"} BeforeSpeaking()
: Event to raise when Speak is invoked, before the message is spoken.

### Methods  {#TextToSpeech-Methods}

{:.methods}

{:id="TextToSpeech.Speak" class="method"} <i/> Speak(*message*{:.text})
: Speaks the given message.

## VideoPlayer  {#VideoPlayer}

### Properties  {#VideoPlayer-Properties}

{:.properties}

{:id="VideoPlayer.FullScreen" .boolean .bo} *FullScreen*
: Returns whether the VideoPlayer's video is currently being
 shown in fullscreen mode or not.

{:id="VideoPlayer.Height" .number .bo} *Height*
: Specifies the component's vertical height, measured in pixels.

{:id="VideoPlayer.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the VideoPlayer as a percentage of the height of the Screen.

{:id="VideoPlayer.Source" .text .wo} *Source*
: The "path" to the video.  Usually, this will be the name of the video file, which should be added in the Designer.

{:id="VideoPlayer.Visible" .boolean} *Visible*
: Returns true iff the VideoPlayer is visible.

{:id="VideoPlayer.Volume" .number .wo} *Volume*
: Sets the volume to a number between 0 and 100. Values less than 0 will be treated as 0, and values greater than 100 will be treated as 100.

{:id="VideoPlayer.Width" .number .bo} *Width*
: Specifies the component's horizontal width, measured in pixels.

{:id="VideoPlayer.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the VideoPlayer as a percentage of the width of the Screen.

### Events  {#VideoPlayer-Events}

{:.events}

{:id="VideoPlayer.Completed"} Completed()
: Indicates that the video has reached the end

### Methods  {#VideoPlayer-Methods}

{:.methods}

{:id="VideoPlayer.GetDuration" class="method returns number"} <i/> GetDuration()
: Returns duration of the video in milliseconds.

{:id="VideoPlayer.Pause" class="method"} <i/> Pause()
: Pauses playback of the video.  Playback can be resumed at the same location by calling the <code>Start</code> method.

{:id="VideoPlayer.SeekTo" class="method"} <i/> SeekTo(*ms*{:.number})
: Seeks to the requested time (specified in milliseconds) in the video. If the video is paused, the frame shown will not be updated by the seek. The player can jump only to key frames in the video, so seeking to times that differ by short intervals may not actually move to different frames.

{:id="VideoPlayer.Start" class="method"} <i/> Start()
: Starts playback of the video.

{:id="VideoPlayer.Stop" class="method"} <i/> Stop()
: Resets to start of video and pauses it if video was playing.

## YandexTranslate  {#YandexTranslate}

### Events  {#YandexTranslate-Events}

{:.events}

{:id="YandexTranslate.GotTranslation"} GotTranslation(*responseCode*{:.text},*translation*{:.text})
: Event triggered when the Yandex.Translate service returns the translated text. This event also provides a response code for error handling. If the responseCode is not 200, then something went wrong with the call, and the translation will not be available.

### Methods  {#YandexTranslate-Methods}

{:.methods}

{:id="YandexTranslate.RequestTranslation" class="method"} <i/> RequestTranslation(*languageToTranslateTo*{:.text},*textToTranslate*{:.text})
: By providing a target language to translate to (for instance, 'es' for Spanish, 'en' for English, or 'ru' for Russian), and a word or sentence to translate, this method will request a translation to the Yandex.Translate service.
Once the text is translated by the external service, the event GotTranslation will be executed.
Note: Yandex.Translate will attempt to detect the source language. You can also specify prepending it to the language translation. I.e., es-ru will specify Spanish to Russian translation.