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

![Camcorder icon](images/camcorder.png)

 A component to record a video using the device's camcorder. After the video is recorded, the
 name of the file on the phone containing the clip is available as an argument to the
 [`AfterRecording`](#Camcorder.AfterRecording) event. The file name can be used, for example, to set the source
 property of a [`VideoPlayer`](#VideoPlayer) component.



### Properties  {#Camcorder-Properties}

{:.properties}
None


### Events  {#Camcorder-Events}

{:.events}

{:id="Camcorder.AfterRecording"} AfterRecording(*clip*{:.text})
: Indicates that a video was recorded with the camera and provides the path to
 the stored video.

### Methods  {#Camcorder-Methods}

{:.methods}

{:id="Camcorder.RecordVideo" class="method"} <i/> RecordVideo()
: Records a video, then raises the [`AfterRecording`](#Camcorder.AfterRecording) event.

## Camera  {#Camera}

![Camera icon](images/camera.png)

 Use a camera component to take a picture on the phone.

 `Camera` is a non-visible component that takes a picture using the device's camera. After the
 picture is taken, the path to the file on the phone containing the picture is available as an
 argument to the [`AfterPicture`](#Camera.AfterPicture) event. The path can be used, for example, as the
 [`Picture`](userinterface.html#Image.Picture) property of an [`Image`](userinterface.html3Image)
 component.



### Properties  {#Camera-Properties}

{:.properties}
None


### Events  {#Camera-Events}

{:.events}

{:id="Camera.AfterPicture"} AfterPicture(*image*{:.text})
: Called after the picture is taken. The text argument `image` is the path that can be used to
 locate the image on the phone.

### Methods  {#Camera-Methods}

{:.methods}

{:id="Camera.TakePicture" class="method"} <i/> TakePicture()
: Takes a picture, then raises the [`AfterPicture`](#Camera.AfterPicture) event.

## ImagePicker  {#ImagePicker}

A special-purpose button. When the user taps an `ImagePicker`, the device's image gallery
 appears, and the user can choose an image. After an image is picked, it is saved, and the
 [`Selection`](#ImagePicker.Selection) property will be the name of the file where the image is stored. In order
 to not fill up storage, a maximum of 10 images will be stored. Picking more images will delete
 previous images, in order from oldest to newest.



### Properties  {#ImagePicker-Properties}

{:.properties}

{:id="ImagePicker.BackgroundColor" .color} *BackgroundColor*
: Specifies the `ImagePicker`'s background color as an alpha-red-green-blue
 integer.  If an [`Image`](#ImagePicker.Image) has been set, the color
 change will not be visible until the [`Image`](#ImagePicker.Image) is removed.

{:id="ImagePicker.Enabled" .boolean} *Enabled*
: Specifies whether the `ImagePicker` should be active and clickable.

{:id="ImagePicker.FontBold" .boolean} *FontBold*
: Specifies whether the text of the `ImagePicker` should be bold.
 Some fonts do not support bold.

{:id="ImagePicker.FontItalic" .boolean} *FontItalic*
: Specifies whether the text of the `ImagePicker` should be italic.
 Some fonts do not support italic.

{:id="ImagePicker.FontSize" .number} *FontSize*
: Specifies the text font size of the `ImagePicker`, measured in sp(scale-independent pixels).

{:id="ImagePicker.FontTypeface" .number .do} *FontTypeface*
: Specifies the text font face of the `ImagePicker` as default, serif, sans
 serif, or monospace.

{:id="ImagePicker.Height" .number .bo} *Height*
: Specifies the `ImagePicker`'s vertical height, measured in pixels.

{:id="ImagePicker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `ImagePicker`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="ImagePicker.Image" .text} *Image*
: Specifies the path of the `ImagePicker`'s image. If there is both an `Image` and a
 [`BackgroundColor`](#ImagePicker.BackgroundColor) specified, only the `Image` will be visible.

{:id="ImagePicker.Selection" .text .ro .bo} *Selection*
: Path to the file containing the image that was selected.

{:id="ImagePicker.Shape" .number .do} *Shape*
: Specifies the shape of the `ImagePicker`. The valid values for this property are `0` (default),
 `1` (rounded), `2` (rectangle), and `3` (oval). The `Shape` will not be visible if an
 [`Image`](#ImagePicker.Image) is used.

{:id="ImagePicker.ShowFeedback" .boolean} *ShowFeedback*
: Specifies if a visual feedback should be shown when a `ImagePicker` with an assigned
 [`Image`](#ImagePicker.Image) is pressed.

{:id="ImagePicker.Text" .text} *Text*
: Specifies the text displayed by the `ImagePicker`.

{:id="ImagePicker.TextAlignment" .number .do} *TextAlignment*
: Specifies the alignment of the `ImagePicker`'s text. Valid values are:
 `0` (normal; e.g., left-justified if text is written left to right),
 `1` (center), or
 `2` (opposite; e.g., right-justified if text is written left to right).

{:id="ImagePicker.TextColor" .color} *TextColor*
: Specifies the text color of the `ImagePicker` as an alpha-red-green-blue
 integer.

{:id="ImagePicker.Visible" .boolean} *Visible*
: Specifies whether the `ImagePicker` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `ImagePicker` is showing and `false`{:.logic.block} if hidden.

{:id="ImagePicker.Width" .number .bo} *Width*
: Specifies the horizontal width of the `ImagePicker`, measured in pixels.

{:id="ImagePicker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `ImagePicker` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#ImagePicker-Events}

{:.events}

{:id="ImagePicker.AfterPicking"} AfterPicking()
: Event to be raised after the `ImagePicker` activity returns its
 result and the properties have been filled in.

{:id="ImagePicker.BeforePicking"} BeforePicking()
: Event to raise when the `ImagePicker` is clicked or the picker is shown
 using the [`Open`](#ImagePicker.Open) method.  This event occurs before the picker is displayed, and
 can be used to prepare the picker before it is shown.

{:id="ImagePicker.GotFocus"} GotFocus()
: Indicates the cursor moved over the `ImagePicker` so it is now possible
 to click it.

{:id="ImagePicker.LostFocus"} LostFocus()
: Indicates the cursor moved away from the `ImagePicker` so it is now no
 longer possible to click it.

{:id="ImagePicker.TouchDown"} TouchDown()
: Indicates that the `ImagePicker` was pressed down.

{:id="ImagePicker.TouchUp"} TouchUp()
: Indicates that the `ImagePicker` has been released.

### Methods  {#ImagePicker-Methods}

{:.methods}

{:id="ImagePicker.Open" class="method"} <i/> Open()
: Opens the `ImagePicker`, as though the user clicked on it.

## Player  {#Player}

Multimedia component that plays audio and controls phone vibration. The name of a multimedia
 file is specified in the [`Source`](#Player.Source) property, which can be set in the Designer or
 in the Blocks Editor. The length of time for a vibration is specified in the Blocks Editor in
 milliseconds (thousandths of a second).

 For supported audio formats, see
 [Android Supported Media Formats](//developer.android.com/guide/appendix/media-formats.html).

 This component is best for long sound files, such as songs, while the [`Sound`](#Sound) component is
 more efficient for short files, such as sound effects.



### Properties  {#Player-Properties}

{:.properties}

{:id="Player.IsPlaying" .boolean .ro .bo} *IsPlaying*
: Reports whether the media is playing.

{:id="Player.Loop" .boolean} *Loop*
: If true, the `Player` will loop when it plays. Setting `Loop` while the player is playing will
 affect the current playing.

{:id="Player.PlayOnlyInForeground" .boolean} *PlayOnlyInForeground*
: If true, the `Player` will pause playing when leaving the current screen; if false
 (default option), the `Player` continues playing whenever the current screen is displaying or
 not.

{:id="Player.Source" .text} *Source*
: Sets the audio source.

{:id="Player.Volume" .number .wo} *Volume*
: Sets the volume property to a number between 0 and 100.

### Events  {#Player-Events}

{:.events}

{:id="Player.Completed"} Completed()
: Indicates that the media has reached the end

{:id="Player.OtherPlayerStarted"} OtherPlayerStarted()
: This event is signaled when another player has started (and the current player is playing or
 paused, but not stopped).

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

A multimedia component that plays sound files and optionally vibrates for the number of
 milliseconds (thousandths of a second) specified in the Blocks Editor. The name of the sound
 file to play can be specified either in the Designer or in the Blocks Editor.

 For supported sound file formats, see
 [Android Supported Media Formats](//developer.android.com/guide/appendix/media-formats.html).

 This `Sound` component is best for short sound files, such as sound effects, while the
 [`Player`](#Player) component is more efficient for longer sounds, such as songs.



### Properties  {#Sound-Properties}

{:.properties}

{:id="Sound.MinimumInterval" .number} *MinimumInterval*
: Specifies the minimum interval required between calls to [`Play`](#Sound.Play), in
 milliseconds.
 Once the sound starts playing, all further [`Play`](#Sound.Play) calls will be ignored
 until the interval has elapsed.

{:id="Sound.Source" .text} *Source*
: The name of the sound file. Only certain formats are supported.
 See http://developer.android.com/guide/appendix/media-formats.html.

### Events  {#Sound-Events}

{:.events}
None


### Methods  {#Sound-Methods}

{:.methods}

{:id="Sound.Pause" class="method"} <i/> Pause()
: Pauses playing the sound if it is being played.

{:id="Sound.Play" class="method"} <i/> Play()
: Plays the sound.

{:id="Sound.Resume" class="method"} <i/> Resume()
: Resumes playing the sound after a pause.

{:id="Sound.Stop" class="method"} <i/> Stop()
: Stops playing the sound if it is being played.

{:id="Sound.Vibrate" class="method"} <i/> Vibrate(*millisecs*{:.number})
: Vibrates for the specified number of milliseconds.

## SoundRecorder  {#SoundRecorder}

![SoundRecorder icon](images/soundrecorder.png)

 Multimedia component that records audio.



### Properties  {#SoundRecorder-Properties}

{:.properties}

{:id="SoundRecorder.SavedRecording" .text} *SavedRecording*
: Specifies the path to the file where the recording should be stored. If this property is the
 empty string, then starting a recording will create a file in an appropriate location. If the
 property is not the empty string, it should specify a complete path to a file in an existing
 directory, including a file name with the extension .3gp.

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

![SpeechRecognizer icon](images/speechrecognizer.png)

 Use a `SpeechRecognizer` component to listen to the user speaking and convert the spoken sound
 into text using the device's speech recognition feature.



### Properties  {#SpeechRecognizer-Properties}

{:.properties}

{:id="SpeechRecognizer.Result" .text .ro .bo} *Result*
: Returns the last text produced by the recognizer.

{:id="SpeechRecognizer.UseLegacy" .boolean} *UseLegacy*
: If true, a separate dialog is used to recognize speech (the default). If false, speech is
 recognized in the background and updates are received as it recognizes words.
 [`AfterGettingText`](#SpeechRecognizer.AfterGettingText) may get several calls with `partial` set to `true`{:.logic.block}.
 Once sufficient time has elapsed since the last utterance, or `StopListening` is called,
 the last string will be returned with `partial` set to `false`{:.logic.block} to indicate that it is the
 final recognized string and no more data will be provided until recognition is again started. See
 [`AfterGettingText`](#SpeechRecognizer.AfterGettingText) for more details on partial speech recognition.

### Events  {#SpeechRecognizer-Events}

{:.events}

{:id="SpeechRecognizer.AfterGettingText"} AfterGettingText(*result*{:.text},*partial*{:.boolean})
: Simple event to raise after the SpeechRecognizer has recognized speech. If
 [`UseLegacy`](#SpeechRecognizer.UseLegacy) is `true`{:.logic.block}, then this event will only happen once
 at the very end of the recognition. If [`UseLegacy`](#SpeechRecognizer.UseLegacy) is `false`{:.logic.block},
 then this event will run multiple times as the `SpeechRecognizer` incrementally recognizes
 speech. In this case, `partial` will be `true`{:.logic.block} until the recognized speech
 has been finalized (e.g., the user has stopped speaking), in which case `partial` will be
 `false`{:.logic.block}.

{:id="SpeechRecognizer.BeforeGettingText"} BeforeGettingText()
: Simple event to raise when the `SpeechRecognizer` is invoked but before its
 activity is started.

### Methods  {#SpeechRecognizer-Methods}

{:.methods}

{:id="SpeechRecognizer.GetText" class="method"} <i/> GetText()
: Asks the user to speak, and converts the speech to text. Signals the
 [`AfterGettingText`](#SpeechRecognizer.AfterGettingText) event when the result is available.

{:id="SpeechRecognizer.Stop" class="method"} <i/> Stop()
: Function used to forcefully stop listening speech in cases where
 SpeechRecognizer cannot stop automatically.
 This function works only when the [`UseLegacy`](#SpeechRecognizer.UseLegacy) property is
 set to `false`{:.logic.block}.

## TextToSpeech  {#TextToSpeech}

The `TextToSpeech` component speaks a given text aloud. You can set the pitch
 and the rate of speech.

 You can also set a language by supplying a language code. This changes the pronunciation of
 words, not the actual language spoken. For example, setting the [`Language`](#TextToSpeech.Language) to French
 and speaking English text will sound like someone speaking English (en) with a French accent.

 You can also specify a country by supplying a [`Country`](#TextToSpeech.Country) code. This can affect the
 pronunciation. For example, British English (GBR) will sound different from US English (USA).
 Not every country code will affect every language.

 The languages and countries available depend on the particular device, and can be listed with
 the [`AvailableLanguages`](#TextToSpeech.AvailableLanguages) and [`AvailableCountries`](#TextToSpeech.AvailableCountries) properties.



### Properties  {#TextToSpeech-Properties}

{:.properties}

{:id="TextToSpeech.AvailableCountries" .list .ro .bo} *AvailableCountries*
: List of the country codes available on this device for use with TextToSpeech.  Check the Android developer documentation under supported languages to find the meanings of these abbreviations.

{:id="TextToSpeech.AvailableLanguages" .list .ro .bo} *AvailableLanguages*
: List of the languages available on this device for use with TextToSpeech.  Check the Android developer documentation under supported languages to find the meanings of these abbreviations.

{:id="TextToSpeech.Country" .text} *Country*
: Country code to use for speech generation. This can affect the pronunciation. For example,
 British English (GBR) will sound different from US English (USA). Not every country code will
 affect every language.

{:id="TextToSpeech.Language" .text} *Language*
: Sets the language for TextToSpeech. This changes the way that words are pronounced, not the
 actual language that is spoken. For example, setting the language to French and speaking
 English text will sound like someone speaking English with a French accent.

{:id="TextToSpeech.Pitch" .number} *Pitch*
: Sets the speech pitch for the TextToSpeech.

   The values should be between 0 and 2 where lower values lower the tone of synthesized voice
 and greater values raise it.

   The default value is 1.0 for normal pitch.

{:id="TextToSpeech.Result" .boolean .ro .bo} *Result*
: Returns `true`{:.logic.block} if the text was successfully converted to
 speech, otherwise `false`{:.logic.block}.

{:id="TextToSpeech.SpeechRate" .number} *SpeechRate*
: Sets the SpeechRate for TextToSpeech.

   The values should be between 0 and 2 where lower values slow down the pitch and greater
 values accelerate it.

   The default value is 1.0 for normal speech rate.

### Events  {#TextToSpeech-Events}

{:.events}

{:id="TextToSpeech.AfterSpeaking"} AfterSpeaking(*result*{:.boolean})
: Event to raise after the message is spoken. The `result`{:.variable.block} will be
 `true`{:.logic.block} if the message is spoken successfully, otherwise it will be
 `false`{:.logic.block}.

{:id="TextToSpeech.BeforeSpeaking"} BeforeSpeaking()
: Event to raise when Speak is invoked, before the message is spoken.

### Methods  {#TextToSpeech-Methods}

{:.methods}

{:id="TextToSpeech.Speak" class="method"} <i/> Speak(*message*{:.text})
: Speaks the given message.

## VideoPlayer  {#VideoPlayer}

A multimedia component capable of playing videos. When the application is run, the `VideoPlayer`
 will be displayed as a rectangle on-screen. If the user touches the rectangle, controls will
 appear to play/pause, skip ahead, and skip backward within the video. The application can also
 control behavior by calling the [`Start`](#VideoPlayer.Start), [`Pause`](#VideoPlayer.Pause), and [`SeekTo`](#VideoPlayer.SeekTo)
 methods.

 Video files should be in 3GPP (.3gp) or MPEG-4 (.mp4) formats. For more details about legal
 formats, see
 [Android Supported Media Formats](//developer.android.com/guide/appendix/media-formats.html).

 App Inventor only permits video files under 1 MB and limits the total size of an application to
 5 MB, not all of which is available for media (video, audio, and sound) files. If your media
 files are too large, you may get errors when packaging or installing your application, in which
 case you should reduce the number of media files or their sizes. Most video editing software,
 such as Windows Movie Maker and Apple iMovie, can help you decrease the size of videos by
 shortening them or re-encoding the video into a more compact format.

 You can also set the media source to a URL that points to a streaming video, but the URL must
 point to the video file itself, not to a program that plays the video.



### Properties  {#VideoPlayer-Properties}

{:.properties}

{:id="VideoPlayer.FullScreen" .boolean .bo} *FullScreen*
: Sets whether the video should be shown in fullscreen or not.

{:id="VideoPlayer.Height" .number .bo} *Height*
: Specifies the component's vertical height, measured in pixels.

{:id="VideoPlayer.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `VideoPlayer`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="VideoPlayer.Source" .text .wo} *Source*
: Sets the "path" to the video. Usually, this will be the name of the video file, which should be
 added in the Designer.

{:id="VideoPlayer.Visible" .boolean} *Visible*
: Specifies whether the `VideoPlayer` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `VideoPlayer` is showing and `false`{:.logic.block} if hidden.

{:id="VideoPlayer.Volume" .number .wo} *Volume*
: Sets the volume property to a number between 0 and 100. Values less than 0
 will be treated as 0, and values greater than 100 will be treated as 100.

{:id="VideoPlayer.Width" .number .bo} *Width*
: Specifies the component's horizontal width, measured in pixels.

{:id="VideoPlayer.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `VideoPlayer` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#VideoPlayer-Events}

{:.events}

{:id="VideoPlayer.Completed"} Completed()
: Indicates that the video has reached the end

### Methods  {#VideoPlayer-Methods}

{:.methods}

{:id="VideoPlayer.GetDuration" class="method returns number"} <i/> GetDuration()
: Returns duration of the video in milliseconds.

{:id="VideoPlayer.Pause" class="method"} <i/> Pause()
: Pauses playback of the video.  Playback can be resumed at the same location by calling the
 [`Start`](#VideoPlayer.Start) method.

{:id="VideoPlayer.SeekTo" class="method"} <i/> SeekTo(*ms*{:.number})
: Seeks to the requested time (specified in milliseconds) in the video. If the video is paused, the frame shown will not be updated by the seek. The player can jump only to key frames in the video, so seeking to times that differ by short intervals may not actually move to different frames.

{:id="VideoPlayer.Start" class="method"} <i/> Start()
: Plays the media specified by the [`Source`](#VideoPlayer.Source).

{:id="VideoPlayer.Stop" class="method"} <i/> Stop()
: Resets to start of video and pauses it if video was playing.

## YandexTranslate  {#YandexTranslate}

Use this component to translate words and sentences between different languages. This component
 needs Internet access, as it will request translations to the Yandex.Translate service.
 Specify the source and target language in the form source-target using two letter language codes.
 So "en-es" will translate from English to Spanish while "es-ru" will translate from Spanish to
 Russian. If you leave out the source language, the service will attempt to detect the source
 language. So providing just "es" will attempt to detect the source language and translate it
 to Spanish.

 This component is powered by the Yandex translation service. See
 http://api.yandex.com/translate/ for more information, including the list of available languages
 and the meanings of the language codes and status codes.

 **Note:** Translation happens asynchronously in the background. When the translation is complete,
 the [`GotTranslation`](#YandexTranslate.GotTranslation) event is triggered.



### Properties  {#YandexTranslate-Properties}

{:.properties}

{:id="YandexTranslate.ApiKey" .text .wo} *ApiKey*
: The Yandex API Key to use. If set to DEFAULT the platform default key (if any)
 will be used. Otherwise should be set to a valid API key which can be obtained
 from https://tech.yandex.com/translate/. If the platform doesn't have a default
 key and one isn't provided here, an error will be raised.

### Events  {#YandexTranslate-Events}

{:.events}

{:id="YandexTranslate.GotTranslation"} GotTranslation(*responseCode*{:.text},*translation*{:.text})
: Event indicating that a request has finished and has returned data (translation).

### Methods  {#YandexTranslate-Methods}

{:.methods}

{:id="YandexTranslate.RequestTranslation" class="method"} <i/> RequestTranslation(*languageToTranslateTo*{:.text},*textToTranslate*{:.text})
: By providing a target language to translate to (for instance, 'es' for Spanish, 'en' for
 English, or 'ru' for Russian), and a word or sentence to translate, this method will request
 a translation to the Yandex.Translate service. Once the text is translated by the external
 service, the event [`GotTranslation`](#YandexTranslate.GotTranslation) will be executed.

   **Note:** Yandex.Translate will attempt to detect the source language. You can also specify
 prepending it to the language translation, e.g., es-ru will specify Spanish to Russian
 translation.
