# ImageSprite

A 'sprite' that can be placed on a `Canvas`, where it can react to touches and drags, interact with other sprites (`Ball`s and other `ImageSprite`s) and the edge of the Canvas, and move according to its property values. Its appearance is that of the image specified in its `Picture` property (unless its `Visible` property is `False`).

To have an `ImageSprite` move 10 pixels to the left every 1000 milliseconds (one second), for example, you would set the `Speed` property to 10 \[pixels\], the `Interval` property to 1000 \[milliseconds\], the `Heading` property to 180 \[degrees\], and the `Enabled` property to `True`. A sprite whose `Rotates` property is `True` will rotate its image as the sprite's `Heading` changes. Checking for collisions with a rotated sprite currently checks the sprite's unrotated position so that collision checking will be inaccurate for tall narrow or short wide sprites that are rotated. Any of the sprite properties can be changed at any time under program control.

---

## Designer Properties

---

### Enabled

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Enabled    |   boolean   |      True     |

### Heading

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Heading    |    float    |       0       |

### Interval

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|    Interval   | non_negative_integer |      100      |

### Picture

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Picture    |    asset    |               |

### Rotates

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Rotates    |   boolean   |      True     |

### Speed

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Speed     |    float    |      0.0      |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |   boolean   |      True     |

### X

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|       X       |    float    |      0.0      |

### Y

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|       Y       |    float    |      0.0      |

### Z

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|       Z       |    float    |      1.0      |

## Events

---

### CollidedWith

<div block-type = "component_event" component-selector = "ImageSprite" event-selector = "CollidedWith" event-params = "other" id = "imagesprite-collidedwith"></div>

Event handler called when two enabled sprites (

| Param Name |  IO Type  |
| :--------: | :-------: |
|    other   | component |

### Dragged

<div block-type = "component_event" component-selector = "ImageSprite" event-selector = "Dragged" event-params = "startX-startY-prevX-prevY-currentX-currentY" id = "imagesprite-dragged"></div>

Event handler called when a ImageSprite is dragged. On all calls, the starting coordinates are where the screen was first touched, and the "current" coordinates describe the endpoint of the current line segment. On the first call within a given drag, the "previous" coordinates are the same as the starting coordinates; subsequently, they are the "current" coordinates from the prior call. Note that the ImageSprite won't actually move anywhere in response to the Dragged event unless MoveTo is explicitly called. For smooth movement, each of its coordinates should be set to the sum of its initial value and the difference between its current and previous values.

| Param Name | IO Type |
| :--------: | :-----: |
|   startX   |  number |
|   startY   |  number |
|    prevX   |  number |
|    prevY   |  number |
|  currentX  |  number |
|  currentY  |  number |

### EdgeReached

<div block-type = "component_event" component-selector = "ImageSprite" event-selector = "EdgeReached" event-params = "edge" id = "imagesprite-edgereached"></div>

Event handler called when the ImageSprite reaches an edge of the screen. If Bounce is then called with that edge, the ImageSprite will appear to bounce off of the edge it reached. Edge here is represented as an integer that indicates one of eight directions north (1), northeast (2), east (3), southeast (4), south (-1), southwest (-2), west (-3), and northwest (-4).

| Param Name | IO Type |
| :--------: | :-----: |
|    edge    |  number |

### Flung

<div block-type = "component_event" component-selector = "ImageSprite" event-selector = "Flung" event-params = "x-y-speed-heading-xvel-yvel" id = "imagesprite-flung"></div>

Event handler called when a fling gesture (quick swipe) is made on an enabled ImageSprite. This provides the x and y coordinates of the start of the fling (relative to the upper left of the canvas), the speed (pixels per millisecond), the heading (0-360 degrees), and the x and y velocity components of the fling's vector.

| Param Name | IO Type |
| :--------: | :-----: |
|      x     |  number |
|      y     |  number |
|    speed   |  number |
|   heading  |  number |
|    xvel    |  number |
|    yvel    |  number |

### NoLongerCollidingWith

<div block-type = "component_event" component-selector = "ImageSprite" event-selector = "NoLongerCollidingWith" event-params = "other" id = "imagesprite-nolongercollidingwith"></div>

Event handler called when a pair of sprites (Balls and ImageSprites) are no longer colliding.

| Param Name |  IO Type  |
| :--------: | :-------: |
|    other   | component |

### TouchDown

<div block-type = "component_event" component-selector = "ImageSprite" event-selector = "TouchDown" event-params = "x-y" id = "imagesprite-touchdown"></div>

Event handler called when the user begins touching an enabled ImageSprite (placing their finger on a ImageSprite and leaving it there). This provides the x and y coordinates of the touch, relative to the upper left of the canvas.

| Param Name | IO Type |
| :--------: | :-----: |
|      x     |  number |
|      y     |  number |

### TouchUp

<div block-type = "component_event" component-selector = "ImageSprite" event-selector = "TouchUp" event-params = "x-y" id = "imagesprite-touchup"></div>

Event handler called when the user stops touching an enabled ImageSprite (lifting their finger after a TouchDown event). This provides the x and y coordinates of the touch, relative to the upper left of the canvas.

| Param Name | IO Type |
| :--------: | :-----: |
|      x     |  number |
|      y     |  number |

### Touched

<div block-type = "component_event" component-selector = "ImageSprite" event-selector = "Touched" event-params = "x-y" id = "imagesprite-touched"></div>

Event handler called when the user touches an enabled ImageSprite and then immediately lifts their finger. The provided x and y coordinates are relative to the upper left of the canvas.

| Param Name | IO Type |
| :--------: | :-----: |
|      x     |  number |
|      y     |  number |

## Methods

---

### Bounce

<div block-type = "component_method" component-selector = "ImageSprite" method-selector = "Bounce" method-params = "edge" return-type = "undefined" id = "imagesprite-bounce"></div>

Return Type : No Return Value

Makes the ImageSprite bounce, as if off a wall. For normal bouncing, the edge argument should be the one returned by EdgeReached.

| Param Name | Input Type |
| :--------: | :--------: |
|    edge    |   number   |

### CollidingWith

<div block-type = "component_method" component-selector = "ImageSprite" method-selector = "CollidingWith" method-params = "other" return-type = "boolean" id = "imagesprite-collidingwith"></div>

Return Type : boolean

Indicates whether a collision has been registered between this ImageSprite and the passed sprite (Ball or ImageSprite).

| Param Name | Input Type |
| :--------: | :--------: |
|    other   |  component |

### MoveIntoBounds

<div block-type = "component_method" component-selector = "ImageSprite" method-selector = "MoveIntoBounds" method-params = "" return-type = "undefined" id = "imagesprite-moveintobounds"></div>

Return Type : No Return Value

Moves the ImageSprite back in bounds if part of it extends out of bounds, having no effect otherwise. If the ImageSprite is too wide to fit on the canvas, this aligns the left side of the ImageSprite with the left side of the canvas. If the ImageSprite is too tall to fit on the canvas, this aligns the top side of the ImageSprite with the top side of the canvas.

### MoveTo

<div block-type = "component_method" component-selector = "ImageSprite" method-selector = "MoveTo" method-params = "x-y" return-type = "undefined" id = "imagesprite-moveto"></div>

Return Type : No Return Value

Moves the ImageSprite so that its left top corner is at the specified x and y coordinates.

| Param Name | Input Type |
| :--------: | :--------: |
|      x     |   number   |
|      y     |   number   |

### MoveToPoint

<div block-type = "component_method" component-selector = "ImageSprite" method-selector = "MoveToPoint" method-params = "coordinates" return-type = "undefined" id = "imagesprite-movetopoint"></div>

Return Type : No Return Value

Moves the origin of ImageSprite to the position of the cooordinates given by the list formatted as \[x-coordinate, y-coordinate\].

|  Param Name | Input Type |
| :---------: | :--------: |
| coordinates |    list    |

### PointInDirection

<div block-type = "component_method" component-selector = "ImageSprite" method-selector = "PointInDirection" method-params = "x-y" return-type = "undefined" id = "imagesprite-pointindirection"></div>

Return Type : No Return Value

Sets the heading of the ImageSprite toward the point with the coordinates (x, y).

| Param Name | Input Type |
| :--------: | :--------: |
|      x     |   number   |
|      y     |   number   |

### PointTowards

<div block-type = "component_method" component-selector = "ImageSprite" method-selector = "PointTowards" method-params = "target" return-type = "undefined" id = "imagesprite-pointtowards"></div>

Return Type : No Return Value

Turns the ImageSprite to point towards a designated target sprite (Ball or ImageSprite). The new heading will be parallel to the line joining the centerpoints of the two sprites.

| Param Name | Input Type |
| :--------: | :--------: |
|   target   |  component |

## Block Properties

---

### Enabled

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Enabled" property-type = "get" id = "get-imagesprite-enabled"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Enabled" property-type = "set" id = "set-imagesprite-enabled"></div>

Controls whether the ImageSprite moves and can be interacted with through collisions, dragging, touching, and flinging.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### Heading

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Heading" property-type = "get" id = "get-imagesprite-heading"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Heading" property-type = "set" id = "set-imagesprite-heading"></div>

Returns the ImageSprite's heading in degrees above the positive x-axis. Zero degrees is toward the right of the screen; 90 degrees is toward the top of the screen.

| Param Name | IO Type |
| :--------: | :-----: |
|   Heading  |  number |

### Height

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Height" property-type = "get" id = "get-imagesprite-height"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Height" property-type = "set" id = "set-imagesprite-height"></div>

The height of the ImageSprite in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### Interval

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Interval" property-type = "get" id = "get-imagesprite-interval"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Interval" property-type = "set" id = "set-imagesprite-interval"></div>

The interval in milliseconds at which the ImageSprite's position is updated. For example, if the interval is 50 and the speed is 10, then every 50 milliseconds the sprite will move 10 pixels in the heading direction.

| Param Name | IO Type |
| :--------: | :-----: |
|  Interval  |  number |

### Picture

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Picture" property-type = "get" id = "get-imagesprite-picture"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Picture" property-type = "set" id = "set-imagesprite-picture"></div>

The picture that determines the ImageSprite's appearance.

| Param Name | IO Type |
| :--------: | :-----: |
|   Picture  |   text  |

### Rotates

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Rotates" property-type = "get" id = "get-imagesprite-rotates"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Rotates" property-type = "set" id = "set-imagesprite-rotates"></div>

Whether the image should rotate to match the ImageSprite's heading. The sprite rotates around its centerpoint.

| Param Name | IO Type |
| :--------: | :-----: |
|   Rotates  | boolean |

### Speed

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Speed" property-type = "get" id = "get-imagesprite-speed"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Speed" property-type = "set" id = "set-imagesprite-speed"></div>

The number of pixels that the ImageSprite should move every interval, if enabled.

| Param Name | IO Type |
| :--------: | :-----: |
|    Speed   |  number |

### Visible

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Visible" property-type = "get" id = "get-imagesprite-visible"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Visible" property-type = "set" id = "set-imagesprite-visible"></div>

Whether the ImageSprite is visible.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Width" property-type = "get" id = "get-imagesprite-width"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Width" property-type = "set" id = "set-imagesprite-width"></div>

The width of the ImageSprite in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### X

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "X" property-type = "get" id = "get-imagesprite-x"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "X" property-type = "set" id = "set-imagesprite-x"></div>

The horizontal coordinate of the left edge of the ImageSprite, increasing as the ImageSprite moves right.

| Param Name | IO Type |
| :--------: | :-----: |
|      X     |  number |

### Y

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Y" property-type = "get" id = "get-imagesprite-y"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Y" property-type = "set" id = "set-imagesprite-y"></div>

The vertical coordinate of the top edge of the ImageSprite, increasing as the ImageSprite moves down.

| Param Name | IO Type |
| :--------: | :-----: |
|      Y     |  number |

### Z

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Z" property-type = "get" id = "get-imagesprite-z"></div>

<div block-type = "component_set_get" component-selector = "ImageSprite" property-selector = "Z" property-type = "set" id = "set-imagesprite-z"></div>

How the \`ImageSprite\` should be layered relative to other

| Param Name | IO Type |
| :--------: | :-----: |
|      Z     |  number |

## Component

---

### ImageSprite

<div block-type = "component_component_block" component-selector = "ImageSprite" id = "component-imagesprite"></div>

Return Type : component

Component ImageSprite

