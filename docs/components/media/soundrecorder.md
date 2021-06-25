# SoundRecorder

Multimedia component that records audio.

---

## Designer Properties

---

### SavedRecording

|  Property Name | Editor Type | Default Value |
| :------------: | :---------: | :-----------: |
| SavedRecording |    string   |               |

## Events

---

### AfterSoundRecorded

<div block-type = "component_event" component-selector = "SoundRecorder" event-selector = "AfterSoundRecorded" event-params = "sound" id = "soundrecorder-aftersoundrecorded"></div>

Provides the location of the newly created sound.

| Param Name | IO Type |
| :--------: | :-----: |
|    sound   |   text  |

### StartedRecording

<div block-type = "component_event" component-selector = "SoundRecorder" event-selector = "StartedRecording" event-params = "" id = "soundrecorder-startedrecording"></div>

Indicates that the recorder has started, and can be stopped.

### StoppedRecording

<div block-type = "component_event" component-selector = "SoundRecorder" event-selector = "StoppedRecording" event-params = "" id = "soundrecorder-stoppedrecording"></div>

Indicates that the recorder has stopped, and can be started again.

## Methods

---

### Start

<div block-type = "component_method" component-selector = "SoundRecorder" method-selector = "Start" method-params = "" return-type = "undefined" id = "soundrecorder-start"></div>

Return Type : No Return Value

Starts recording.

### Stop

<div block-type = "component_method" component-selector = "SoundRecorder" method-selector = "Stop" method-params = "" return-type = "undefined" id = "soundrecorder-stop"></div>

Return Type : No Return Value

Stops recording.

## Block Properties

---

### SavedRecording

<div block-type = "component_set_get" component-selector = "SoundRecorder" property-selector = "SavedRecording" property-type = "get" id = "get-soundrecorder-savedrecording"></div>

<div block-type = "component_set_get" component-selector = "SoundRecorder" property-selector = "SavedRecording" property-type = "set" id = "set-soundrecorder-savedrecording"></div>

Specifies the path to the file where the recording should be stored. If this property is the empty string, then starting a recording will create a file in an appropriate location. If the property is not the empty string, it should specify a complete path to a file in an existing directory, including a file name with the extension .3gp.

|   Param Name   | IO Type |
| :------------: | :-----: |
| SavedRecording |   text  |

## Component

---

### SoundRecorder

<div block-type = "component_component_block" component-selector = "SoundRecorder" id = "component-soundrecorder"></div>

Return Type : component

Component SoundRecorder

