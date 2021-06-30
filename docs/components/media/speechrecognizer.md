<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# SpeechRecognizer

Component for using Voice Recognition to convert from speech to text

---

## Designer Properties

---

### UseLegacy

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   UseLegacy   |   boolean   |      True     |

## Events

---

### AfterGettingText

<div block-type = "component_event" component-selector = "SpeechRecognizer" event-selector = "AfterGettingText" id = "speechrecognizer-aftergettingtext"></div>

Simple event to raise after the SpeechRecognizer has recognized speech. If

| Param Name | IO Type |
| :--------: | :-----: |
|   result   |   text  |
|   partial  | boolean |

### BeforeGettingText

<div block-type = "component_event" component-selector = "SpeechRecognizer" event-selector = "BeforeGettingText" id = "speechrecognizer-beforegettingtext"></div>

Simple event to raise when the \`SpeechRecognizer\` is invoked but before its activity is started.

## Methods

---

### GetText

<div block-type = "component_method" component-selector = "SpeechRecognizer" method-selector = "GetText" id = "speechrecognizer-gettext"></div>

Return Type : No Return Value

Asks the user to speak, and converts the speech to text. Signals the

### Stop

<div block-type = "component_method" component-selector = "SpeechRecognizer" method-selector = "Stop" id = "speechrecognizer-stop"></div>

Return Type : No Return Value

Function used to forcefully stop listening speech in cases where SpeechRecognizer cannot stop automatically. This function works only when the

## Block Properties

---

### Result

<div block-type = "component_set_get" component-selector = "SpeechRecognizer" property-selector = "Result" property-type = "get" id = "get-speechrecognizer-result"></div>

Returns the last text produced by the recognizer.

| Param Name | IO Type |
| :--------: | :-----: |
|   Result   |   text  |

### UseLegacy

<div block-type = "component_set_get" component-selector = "SpeechRecognizer" property-selector = "UseLegacy" property-type = "get" id = "get-speechrecognizer-uselegacy"></div>

<div block-type = "component_set_get" component-selector = "SpeechRecognizer" property-selector = "UseLegacy" property-type = "set" id = "set-speechrecognizer-uselegacy"></div>

If true, an app can retain their older behaviour.

| Param Name | IO Type |
| :--------: | :-----: |
|  UseLegacy | boolean |

## Component

---

### SpeechRecognizer

<div block-type = "component_component_block" component-selector = "SpeechRecognizer" id = "component-speechrecognizer"></div>

Return Type : component

Component SpeechRecognizer

