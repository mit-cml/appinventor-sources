# Player

Multimedia component that plays audio and controls phone vibration. The name of a multimedia field is specified in the `Source` property, which can be set in the Designer or in the Blocks Editor. The length of time for a vibration is specified in the Blocks Editor in milliseconds (thousandths of a second).

For supported audio formats, see [Android Supported Media Formats](http://developer.android.com/guide/appendix/media-formats.html).

This component is best for long sound files, such as songs, while the `Sound` component is more efficient for short files, such as sound effects.

---

## Designer Properties

---

### Loop

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|      Loop     |   boolean   |     False     |

### PlayOnlyInForeground

|     Property Name    | Editor Type | Default Value |
| :------------------: | :---------: | :-----------: |
| PlayOnlyInForeground |   boolean   |     False     |

### Source

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Source    |    asset    |               |

### Volume

| Property Name |     Editor Type    | Default Value |
| :-----------: | :----------------: | :-----------: |
|     Volume    | non_negative_float |       50      |

## Events

---

### Completed

<div block-type = "component_event" component-selector = "Player" event-selector = "Completed" event-params = "" id = "player-completed"></div>

Indicates that the media has reached the end

### OtherPlayerStarted

<div block-type = "component_event" component-selector = "Player" event-selector = "OtherPlayerStarted" event-params = "" id = "player-otherplayerstarted"></div>

This event is signaled when another player has started (and the current player is playing or paused, but not stopped).

### PlayerError

<div block-type = "component_event" component-selector = "Player" event-selector = "PlayerError" event-params = "message" id = "player-playererror"></div>

The PlayerError event is no longer used. Please use the Screen.ErrorOccurred event instead.

| Param Name | IO Type |
| :--------: | :-----: |
|   message  |   text  |

## Methods

---

### Pause

<div block-type = "component_method" component-selector = "Player" method-selector = "Pause" method-params = "" return-type = "undefined" id = "player-pause"></div>

Return Type : No Return Value

Suspends playing the media if it is playing.

### Start

<div block-type = "component_method" component-selector = "Player" method-selector = "Start" method-params = "" return-type = "undefined" id = "player-start"></div>

Return Type : No Return Value

Plays the media. If it was previously paused, the playing is resumed. If it was previously stopped, it starts from the beginning.

### Stop

<div block-type = "component_method" component-selector = "Player" method-selector = "Stop" method-params = "" return-type = "undefined" id = "player-stop"></div>

Return Type : No Return Value

Stops playing the media and seeks to the beginning of the song.

### Vibrate

<div block-type = "component_method" component-selector = "Player" method-selector = "Vibrate" method-params = "milliseconds" return-type = "undefined" id = "player-vibrate"></div>

Return Type : No Return Value

Vibrates for specified number of milliseconds.

|  Param Name  | Input Type |
| :----------: | :--------: |
| milliseconds |   number   |

## Block Properties

---

### IsPlaying

<div block-type = "component_set_get" component-selector = "Player" property-selector = "IsPlaying" property-type = "get" id = "get-player-isplaying"></div>

Reports whether the media is playing

| Param Name | IO Type |
| :--------: | :-----: |
|  IsPlaying | boolean |

### Loop

<div block-type = "component_set_get" component-selector = "Player" property-selector = "Loop" property-type = "get" id = "get-player-loop"></div>

<div block-type = "component_set_get" component-selector = "Player" property-selector = "Loop" property-type = "set" id = "set-player-loop"></div>

If true, the player will loop when it plays. Setting Loop while the player is playing will affect the current playing.

| Param Name | IO Type |
| :--------: | :-----: |
|    Loop    | boolean |

### PlayOnlyInForeground

<div block-type = "component_set_get" component-selector = "Player" property-selector = "PlayOnlyInForeground" property-type = "get" id = "get-player-playonlyinforeground"></div>

<div block-type = "component_set_get" component-selector = "Player" property-selector = "PlayOnlyInForeground" property-type = "set" id = "set-player-playonlyinforeground"></div>

If true, the player will pause playing when leaving the current screen; if false (default option), the player continues playing whenever the current screen is displaying or not.

|      Param Name      | IO Type |
| :------------------: | :-----: |
| PlayOnlyInForeground | boolean |

### Source

<div block-type = "component_set_get" component-selector = "Player" property-selector = "Source" property-type = "get" id = "get-player-source"></div>

<div block-type = "component_set_get" component-selector = "Player" property-selector = "Source" property-type = "set" id = "set-player-source"></div>

Returns the path to the audio source

| Param Name | IO Type |
| :--------: | :-----: |
|   Source   |   text  |

### Volume

<div block-type = "component_set_get" component-selector = "Player" property-selector = "Volume" property-type = "set" id = "set-player-volume"></div>

Sets the volume to a number between 0 and 100

| Param Name | IO Type |
| :--------: | :-----: |
|   Volume   |  number |

## Component

---

### Player

<div block-type = "component_component_block" component-selector = "Player" id = "component-player"></div>

Return Type : component

Component Player

