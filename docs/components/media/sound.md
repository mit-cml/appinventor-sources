# Sound

A multimedia component that plays sound files and optionally vibrates for the number of milliseconds (thousandths of a second) specified in the Blocks Editor. The name of the sound file to play can be specified either in the Designer or in the Blocks Editor.

For supported sound file formats, see [Android Supported Media Formats](http://developer.android.com/guide/appendix/media-formats.html).

This `Sound` component is best for short sound files, such as sound effects, while the `Player` component is more efficient for longer sounds, such as songs.

You might get an error if you attempt to play a sound immeditely after setting the source.

---

## Designer Properties

---

### MinimumInterval

|  Property Name  |      Editor Type     | Default Value |
| :-------------: | :------------------: | :-----------: |
| MinimumInterval | non_negative_integer |      500      |

### Source

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Source    |    asset    |               |

## Events

---

### SoundError

<div block-type = "component_event" component-selector = "Sound" event-selector = "SoundError" event-params = "message" id = "sound-sounderror"></div>

The SoundError event is no longer used. Please use the Screen.ErrorOccurred event instead.

| Param Name | IO Type |
| :--------: | :-----: |
|   message  |   text  |

## Methods

---

### Pause

<div block-type = "component_method" component-selector = "Sound" method-selector = "Pause" method-params = "" return-type = "undefined" id = "sound-pause"></div>

Return Type : No Return Value

Pauses playing the sound if it is being played.

### Play

<div block-type = "component_method" component-selector = "Sound" method-selector = "Play" method-params = "" return-type = "undefined" id = "sound-play"></div>

Return Type : No Return Value

Plays the sound specified by the Source property.

### Resume

<div block-type = "component_method" component-selector = "Sound" method-selector = "Resume" method-params = "" return-type = "undefined" id = "sound-resume"></div>

Return Type : No Return Value

Resumes playing the sound after a pause.

### Stop

<div block-type = "component_method" component-selector = "Sound" method-selector = "Stop" method-params = "" return-type = "undefined" id = "sound-stop"></div>

Return Type : No Return Value

Stops playing the sound if it is being played.

### Vibrate

<div block-type = "component_method" component-selector = "Sound" method-selector = "Vibrate" method-params = "millisecs" return-type = "undefined" id = "sound-vibrate"></div>

Return Type : No Return Value

Vibrates for the specified number of milliseconds.

| Param Name | Input Type |
| :--------: | :--------: |
|  millisecs |   number   |

## Block Properties

---

### MinimumInterval

<div block-type = "component_set_get" component-selector = "Sound" property-selector = "MinimumInterval" property-type = "get" id = "get-sound-minimuminterval"></div>

<div block-type = "component_set_get" component-selector = "Sound" property-selector = "MinimumInterval" property-type = "set" id = "set-sound-minimuminterval"></div>

The minimum interval, in milliseconds, between sounds. If you play a sound, all further Play() calls will be ignored until the interval has elapsed.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| MinimumInterval |  number |

### Source

<div block-type = "component_set_get" component-selector = "Sound" property-selector = "Source" property-type = "get" id = "get-sound-source"></div>

<div block-type = "component_set_get" component-selector = "Sound" property-selector = "Source" property-type = "set" id = "set-sound-source"></div>

The name of the sound file. Only certain formats are supported. See http://developer.android.com/guide/appendix/media-formats.html.

| Param Name | IO Type |
| :--------: | :-----: |
|   Source   |   text  |

## Component

---

### Sound

<div block-type = "component_component_block" component-selector = "Sound" id = "component-sound"></div>

Return Type : component

Component Sound

