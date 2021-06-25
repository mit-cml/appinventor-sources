# Ball

A round 'sprite' that can be placed on a `Canvas`, where it can react to touches and drags, interact with other sprites (`ImageSprite`s and other `Ball`s) and the edge of the Canvas, and move according to its property values.

For example, to have a `Ball` move 4 pixels toward the top of a `Canvas` every 500 milliseconds (half second), you would set the `Speed` property to 4 \[pixels\], the `Interval` property to 500 \[milliseconds\], the `Heading` property to 90 \[degrees\], and the `Enabled` property to `True`.

The difference between a `Ball` and an `ImageSprite` is that the latter can get its appearance from an image file, while a `Ball`'s appearance can be changed only by varying its `PaintColor` and `Radius` properties.

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

### OriginAtCenter

|  Property Name | Editor Type | Default Value |
| :------------: | :---------: | :-----------: |
| OriginAtCenter |   boolean   |     False     |

### PaintColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   PaintColor  |    color    |   &HFF000000  |

### Radius

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|     Radius    | non_negative_integer |       5       |

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

<div block-type = "component_event" component-selector = "Ball" event-selector = "CollidedWith" event-params = "other" id = "ball-collidedwith"></div>

Event handler called when two enabled sprites (

| Param Name |  IO Type  |
| :--------: | :-------: |
|    other   | component |

### Dragged

<div block-type = "component_event" component-selector = "Ball" event-selector = "Dragged" event-params = "startX-startY-prevX-prevY-currentX-currentY" id = "ball-dragged"></div>

Event handler called when a Ball is dragged. On all calls, the starting coordinates are where the screen was first touched, and the "current" coordinates describe the endpoint of the current line segment. On the first call within a given drag, the "previous" coordinates are the same as the starting coordinates; subsequently, they are the "current" coordinates from the prior call. Note that the Ball won't actually move anywhere in response to the Dragged event unless MoveTo is explicitly called. For smooth movement, each of its coordinates should be set to the sum of its initial value and the difference between its current and previous values.

| Param Name | IO Type |
| :--------: | :-----: |
|   startX   |  number |
|   startY   |  number |
|    prevX   |  number |
|    prevY   |  number |
|  currentX  |  number |
|  currentY  |  number |

### EdgeReached

<div block-type = "component_event" component-selector = "Ball" event-selector = "EdgeReached" event-params = "edge" id = "ball-edgereached"></div>

Event handler called when the Ball reaches an edge of the screen. If Bounce is then called with that edge, the Ball will appear to bounce off of the edge it reached. Edge here is represented as an integer that indicates one of eight directions north (1), northeast (2), east (3), southeast (4), south (-1), southwest (-2), west (-3), and northwest (-4).

| Param Name | IO Type |
| :--------: | :-----: |
|    edge    |  number |

### Flung

<div block-type = "component_event" component-selector = "Ball" event-selector = "Flung" event-params = "x-y-speed-heading-xvel-yvel" id = "ball-flung"></div>

Event handler called when a fling gesture (quick swipe) is made on an enabled Ball. This provides the x and y coordinates of the start of the fling (relative to the upper left of the canvas), the speed (pixels per millisecond), the heading (0-360 degrees), and the x and y velocity components of the fling's vector.

| Param Name | IO Type |
| :--------: | :-----: |
|      x     |  number |
|      y     |  number |
|    speed   |  number |
|   heading  |  number |
|    xvel    |  number |
|    yvel    |  number |

### NoLongerCollidingWith

<div block-type = "component_event" component-selector = "Ball" event-selector = "NoLongerCollidingWith" event-params = "other" id = "ball-nolongercollidingwith"></div>

Event handler called when a pair of sprites (Balls and ImageSprites) are no longer colliding.

| Param Name |  IO Type  |
| :--------: | :-------: |
|    other   | component |

### TouchDown

<div block-type = "component_event" component-selector = "Ball" event-selector = "TouchDown" event-params = "x-y" id = "ball-touchdown"></div>

Event handler called when the user begins touching an enabled Ball (placing their finger on a Ball and leaving it there). This provides the x and y coordinates of the touch, relative to the upper left of the canvas.

| Param Name | IO Type |
| :--------: | :-----: |
|      x     |  number |
|      y     |  number |

### TouchUp

<div block-type = "component_event" component-selector = "Ball" event-selector = "TouchUp" event-params = "x-y" id = "ball-touchup"></div>

Event handler called when the user stops touching an enabled Ball (lifting their finger after a TouchDown event). This provides the x and y coordinates of the touch, relative to the upper left of the canvas.

| Param Name | IO Type |
| :--------: | :-----: |
|      x     |  number |
|      y     |  number |

### Touched

<div block-type = "component_event" component-selector = "Ball" event-selector = "Touched" event-params = "x-y" id = "ball-touched"></div>

Event handler called when the user touches an enabled Ball and then immediately lifts their finger. The provided x and y coordinates are relative to the upper left of the canvas.

| Param Name | IO Type |
| :--------: | :-----: |
|      x     |  number |
|      y     |  number |

## Methods

---

### Bounce

<div block-type = "component_method" component-selector = "Ball" method-selector = "Bounce" method-params = "edge" return-type = "undefined" id = "ball-bounce"></div>

Return Type : No Return Value

Makes the Ball bounce, as if off a wall. For normal bouncing, the edge argument should be the one returned by EdgeReached.

| Param Name | Input Type |
| :--------: | :--------: |
|    edge    |   number   |

### CollidingWith

<div block-type = "component_method" component-selector = "Ball" method-selector = "CollidingWith" method-params = "other" return-type = "boolean" id = "ball-collidingwith"></div>

Return Type : boolean

Indicates whether a collision has been registered between this Ball and the passed sprite (Ball or ImageSprite).

| Param Name | Input Type |
| :--------: | :--------: |
|    other   |  component |

### MoveIntoBounds

<div block-type = "component_method" component-selector = "Ball" method-selector = "MoveIntoBounds" method-params = "" return-type = "undefined" id = "ball-moveintobounds"></div>

Return Type : No Return Value

Moves the Ball back in bounds if part of it extends out of bounds, having no effect otherwise. If the Ball is too wide to fit on the canvas, this aligns the left side of the Ball with the left side of the canvas. If the Ball is too tall to fit on the canvas, this aligns the top side of the Ball with the top side of the canvas.

### MoveTo

<div block-type = "component_method" component-selector = "Ball" method-selector = "MoveTo" method-params = "x-y" return-type = "undefined" id = "ball-moveto"></div>

Return Type : No Return Value

Sets the x and y coordinates of the Ball. If CenterAtOrigin is true, the center of the Ball will be placed here. Otherwise, the top left edge of the Ball will be placed at the specified coordinates.

| Param Name | Input Type |
| :--------: | :--------: |
|      x     |   number   |
|      y     |   number   |

### MoveToPoint

<div block-type = "component_method" component-selector = "Ball" method-selector = "MoveToPoint" method-params = "coordinates" return-type = "undefined" id = "ball-movetopoint"></div>

Return Type : No Return Value

Moves the origin of Ball to the position of the cooordinates given by the list formatted as \[x-coordinate, y-coordinate\].

|  Param Name | Input Type |
| :---------: | :--------: |
| coordinates |    list    |

### PointInDirection

<div block-type = "component_method" component-selector = "Ball" method-selector = "PointInDirection" method-params = "x-y" return-type = "undefined" id = "ball-pointindirection"></div>

Return Type : No Return Value

Sets the heading of the Ball toward the point with the coordinates (x, y).

| Param Name | Input Type |
| :--------: | :--------: |
|      x     |   number   |
|      y     |   number   |

### PointTowards

<div block-type = "component_method" component-selector = "Ball" method-selector = "PointTowards" method-params = "target" return-type = "undefined" id = "ball-pointtowards"></div>

Return Type : No Return Value

Turns the Ball to point towards a designated target sprite (Ball or ImageSprite). The new heading will be parallel to the line joining the centerpoints of the two sprites.

| Param Name | Input Type |
| :--------: | :--------: |
|   target   |  component |

## Block Properties

---

### Enabled

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Enabled" property-type = "get" id = "get-ball-enabled"></div>

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Enabled" property-type = "set" id = "set-ball-enabled"></div>

Controls whether the Ball moves and can be interacted with through collisions, dragging, touching, and flinging.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### Heading

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Heading" property-type = "get" id = "get-ball-heading"></div>

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Heading" property-type = "set" id = "set-ball-heading"></div>

Returns the Ball's heading in degrees above the positive x-axis. Zero degrees is toward the right of the screen; 90 degrees is toward the top of the screen.

| Param Name | IO Type |
| :--------: | :-----: |
|   Heading  |  number |

### Interval

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Interval" property-type = "get" id = "get-ball-interval"></div>

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Interval" property-type = "set" id = "set-ball-interval"></div>

The interval in milliseconds at which the Ball's position is updated. For example, if the interval is 50 and the speed is 10, then every 50 milliseconds the sprite will move 10 pixels in the heading direction.

| Param Name | IO Type |
| :--------: | :-----: |
|  Interval  |  number |

### PaintColor

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "PaintColor" property-type = "get" id = "get-ball-paintcolor"></div>

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "PaintColor" property-type = "set" id = "set-ball-paintcolor"></div>

The color of the Ball.

| Param Name | IO Type |
| :--------: | :-----: |
| PaintColor |  number |

### Radius

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Radius" property-type = "get" id = "get-ball-radius"></div>

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Radius" property-type = "set" id = "set-ball-radius"></div>

The distance from the edge of the Ball to its center.

| Param Name | IO Type |
| :--------: | :-----: |
|   Radius   |  number |

### Speed

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Speed" property-type = "get" id = "get-ball-speed"></div>

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Speed" property-type = "set" id = "set-ball-speed"></div>

The number of pixels that the Ball should move every interval, if enabled.

| Param Name | IO Type |
| :--------: | :-----: |
|    Speed   |  number |

### Visible

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Visible" property-type = "get" id = "get-ball-visible"></div>

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Visible" property-type = "set" id = "set-ball-visible"></div>

Whether the Ball is visible.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### X

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "X" property-type = "get" id = "get-ball-x"></div>

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "X" property-type = "set" id = "set-ball-x"></div>

The horizontal coordinate of the Ball, increasing as the Ball moves right. If the property OriginAtCenter is true, the coordinate is for the center of the Ball; otherwise, it is for the leftmost point of the Ball.

| Param Name | IO Type |
| :--------: | :-----: |
|      X     |  number |

### Y

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Y" property-type = "get" id = "get-ball-y"></div>

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Y" property-type = "set" id = "set-ball-y"></div>

The vertical coordinate of the Ball, increasing as the Ball moves down. If the property OriginAtCenter is true, the coordinate is for the center of the Ball; otherwise, it is for the uppermost point of the Ball.

| Param Name | IO Type |
| :--------: | :-----: |
|      Y     |  number |

### Z

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Z" property-type = "get" id = "get-ball-z"></div>

<div block-type = "component_set_get" component-selector = "Ball" property-selector = "Z" property-type = "set" id = "set-ball-z"></div>

How the \`Ball\` should be layered relative to other

| Param Name | IO Type |
| :--------: | :-----: |
|      Z     |  number |

## Component

---

### Ball

<div block-type = "component_component_block" component-selector = "Ball" id = "component-ball"></div>

Return Type : component

Component Ball

