# VideoPlayer

A multimedia component capable of playing videos. When the application is run, the VideoPlayer will be displayed as a rectangle on-screen. If the user touches the rectangle, controls will appear to play/pause, skip ahead, and skip backward within the video. The application can also control behavior by calling the `Start`, `Pause`, and `SeekTo` methods.

Video files should be in 3GPP (.3gp) or MPEG-4 (.mp4) formats. For more details about legal formats, see [Android Supported Media Formats](http://developer.android.com/guide/appendix/media-formats.html).

App Inventor for Android only permits video files under 1 MB and limits the total size of an application to 5 MB, not all of which is available for media (video, audio, and sound) files. If your media files are too large, you may get errors when packaging or installing your application, in which case you should reduce the number of media files or their sizes. Most video editing software, such as Windows Movie Maker and Apple iMovie, can help you decrease the size of videos by shortening them or re-encoding the video into a more compact format.

You can also set the media source to a URL that points to a streaming video, but the URL must point to the video file itself, not to a program that plays the video.

---

## Designer Properties

---

### Source

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Source    |    asset    |               |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

### Volume

| Property Name |     Editor Type    | Default Value |
| :-----------: | :----------------: | :-----------: |
|     Volume    | non_negative_float |       50      |

## Events

---

### Completed

<div block-type = "component_event" component-selector = "VideoPlayer" event-selector = "Completed" event-params = "" id = "videoplayer-completed"></div>

Indicates that the video has reached the end

### VideoPlayerError

<div block-type = "component_event" component-selector = "VideoPlayer" event-selector = "VideoPlayerError" event-params = "message" id = "videoplayer-videoplayererror"></div>

The VideoPlayerError event is no longer used. Please use the Screen.ErrorOccurred event instead.

| Param Name | IO Type |
| :--------: | :-----: |
|   message  |   text  |

## Methods

---

### GetDuration

<div block-type = "component_method" component-selector = "VideoPlayer" method-selector = "GetDuration" method-params = "" return-type = "number" id = "videoplayer-getduration"></div>

Return Type : number

Returns duration of the video in milliseconds.

### Pause

<div block-type = "component_method" component-selector = "VideoPlayer" method-selector = "Pause" method-params = "" return-type = "undefined" id = "videoplayer-pause"></div>

Return Type : No Return Value

Pauses playback of the video. Playback can be resumed at the same location by calling the `Start` method.

### SeekTo

<div block-type = "component_method" component-selector = "VideoPlayer" method-selector = "SeekTo" method-params = "ms" return-type = "undefined" id = "videoplayer-seekto"></div>

Return Type : No Return Value

Seeks to the requested time (specified in milliseconds) in the video. If the video is paused, the frame shown will not be updated by the seek. The player can jump only to key frames in the video, so seeking to times that differ by short intervals may not actually move to different frames.

| Param Name | Input Type |
| :--------: | :--------: |
|     ms     |   number   |

### Start

<div block-type = "component_method" component-selector = "VideoPlayer" method-selector = "Start" method-params = "" return-type = "undefined" id = "videoplayer-start"></div>

Return Type : No Return Value

Starts playback of the video.

### Stop

<div block-type = "component_method" component-selector = "VideoPlayer" method-selector = "Stop" method-params = "" return-type = "undefined" id = "videoplayer-stop"></div>

Return Type : No Return Value

Resets to start of video and pauses it if video was playing.

## Block Properties

---

### FullScreen

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "FullScreen" property-type = "get" id = "get-videoplayer-fullscreen"></div>

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "FullScreen" property-type = "set" id = "set-videoplayer-fullscreen"></div>

Returns whether the VideoPlayer's video is currently being shown in fullscreen mode or not.

| Param Name | IO Type |
| :--------: | :-----: |
| FullScreen | boolean |

### Height

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "Height" property-type = "get" id = "get-videoplayer-height"></div>

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "Height" property-type = "set" id = "set-videoplayer-height"></div>

Specifies the component's vertical height, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "HeightPercent" property-type = "set" id = "set-videoplayer-heightpercent"></div>

Specifies the vertical height of the VideoPlayer as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Source

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "Source" property-type = "set" id = "set-videoplayer-source"></div>

The "path" to the video. Usually, this will be the name of the video file, which should be added in the Designer.

| Param Name | IO Type |
| :--------: | :-----: |
|   Source   |   text  |

### Visible

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "Visible" property-type = "get" id = "get-videoplayer-visible"></div>

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "Visible" property-type = "set" id = "set-videoplayer-visible"></div>

Specifies whether the VideoPlayer should be visible on the screen. Value is true if the VideoPlayer is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Volume

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "Volume" property-type = "set" id = "set-videoplayer-volume"></div>

Sets the volume to a number between 0 and 100. Values less than 0 will be treated as 0, and values greater than 100 will be treated as 100.

| Param Name | IO Type |
| :--------: | :-----: |
|   Volume   |  number |

### Width

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "Width" property-type = "get" id = "get-videoplayer-width"></div>

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "Width" property-type = "set" id = "set-videoplayer-width"></div>

Specifies the component's horizontal width, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "VideoPlayer" property-selector = "WidthPercent" property-type = "set" id = "set-videoplayer-widthpercent"></div>

Specifies the horizontal width of the VideoPlayer as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### VideoPlayer

<div block-type = "component_component_block" component-selector = "VideoPlayer" id = "component-videoplayer"></div>

Return Type : component

Component VideoPlayer

