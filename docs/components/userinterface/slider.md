# Slider

A Slider is a progress bar that adds a draggable thumb. You can touch the thumb and drag left or right to set the slider thumb position. As the Slider thumb is dragged, it will trigger the PositionChanged event, reporting the position of the Slider thumb. The reported position of the Slider thumb can be used to dynamically update another component attribute, such as the font size of a TextBox or the radius of a Ball.

---

## Designer Properties

---

### ColorLeft

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   ColorLeft   |    color    |   &HFFFFC800  |

### ColorRight

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   ColorRight  |    color    |   &HFF888888  |

### MaxValue

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    MaxValue   |    float    |      50.0     |

### MinValue

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    MinValue   |    float    |      10.0     |

### ThumbEnabled

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  ThumbEnabled |   boolean   |      True     |

### ThumbPosition

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| ThumbPosition |    float    |      30.0     |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Events

---

### PositionChanged

<div block-type = "component_event" component-selector = "Slider" event-selector = "PositionChanged" event-params = "thumbPosition" id = "slider-positionchanged"></div>

Indicates that position of the slider thumb has changed.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| thumbPosition |  number |

## Block Properties

---

### ColorLeft

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "ColorLeft" property-type = "get" id = "get-slider-colorleft"></div>

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "ColorLeft" property-type = "set" id = "set-slider-colorleft"></div>

The color of slider to the left of the thumb.

| Param Name | IO Type |
| :--------: | :-----: |
|  ColorLeft |  number |

### ColorRight

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "ColorRight" property-type = "get" id = "get-slider-colorright"></div>

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "ColorRight" property-type = "set" id = "set-slider-colorright"></div>

The color of slider to the right of the thumb.

| Param Name | IO Type |
| :--------: | :-----: |
| ColorRight |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "HeightPercent" property-type = "set" id = "set-slider-heightpercent"></div>

Specifies the vertical height of the Slider as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### MaxValue

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "MaxValue" property-type = "get" id = "get-slider-maxvalue"></div>

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "MaxValue" property-type = "set" id = "set-slider-maxvalue"></div>

Sets the maximum value of slider. Changing the maximum value also resets Thumbposition to be halfway between the minimum and the (new) maximum. If the new maximum is less than the current minimum, then minimum and maximum will both be set to this value. Setting MaxValue resets the thumb position to halfway between MinValue and MaxValue and signals the PositionChanged event.

| Param Name | IO Type |
| :--------: | :-----: |
|  MaxValue  |  number |

### MinValue

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "MinValue" property-type = "get" id = "get-slider-minvalue"></div>

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "MinValue" property-type = "set" id = "set-slider-minvalue"></div>

Sets the minimum value of slider. Changing the minimum value also resets Thumbposition to be halfway between the (new) minimum and the maximum. If the new minimum is greater than the current maximum, then minimum and maximum will both be set to this value. Setting MinValue resets the thumb position to halfway between MinValue and MaxValue and signals the PositionChanged event.

| Param Name | IO Type |
| :--------: | :-----: |
|  MinValue  |  number |

### ThumbEnabled

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "ThumbEnabled" property-type = "get" id = "get-slider-thumbenabled"></div>

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "ThumbEnabled" property-type = "set" id = "set-slider-thumbenabled"></div>

Sets whether or not to display the slider thumb.

|  Param Name  | IO Type |
| :----------: | :-----: |
| ThumbEnabled | boolean |

### ThumbPosition

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "ThumbPosition" property-type = "get" id = "get-slider-thumbposition"></div>

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "ThumbPosition" property-type = "set" id = "set-slider-thumbposition"></div>

Sets the position of the slider thumb. If this value is greater than MaxValue, then it will be set to same value as MaxValue. If this value is less than MinValue, then it will be set to same value as MinValue.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| ThumbPosition |  number |

### Visible

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "Visible" property-type = "get" id = "get-slider-visible"></div>

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "Visible" property-type = "set" id = "set-slider-visible"></div>

Specifies whether the Slider should be visible on the screen. Value is true if the Slider is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "Width" property-type = "get" id = "get-slider-width"></div>

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "Width" property-type = "set" id = "set-slider-width"></div>

Specifies the horizontal width of the Slider, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "Slider" property-selector = "WidthPercent" property-type = "set" id = "set-slider-widthpercent"></div>

Specifies the horizontal width of the Slider as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### Slider

<div block-type = "component_component_block" component-selector = "Slider" id = "component-slider"></div>

Return Type : component

Component Slider

