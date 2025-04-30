---
layout: documentation
title: Drawing and Animation
---

[&laquo; Back to index](index.html)
# Drawing and Animation

Table of Contents:

* [Ball](#Ball)
* [Canvas](#Canvas)
* [ImageSprite](#ImageSprite)

## Ball  {#Ball}

A round 'sprite' that can be placed on a [`Canvas`](#Canvas), where it can react to touches and drags,
 interact with other sprites ([`ImageSprite`](#ImageSprite)s and other `Ball`s) and the edge of the
 `Canvas`, and move according to its property values.

 For example, to have a `Ball` move 4 pixels toward the top of a `Canvas` every 500 milliseconds
 (half second), you would set the [`Speed`](#Ball.Speed) property to 4 [pixels], the
 [`Interval`](#Ball.Interval) property to 500 [milliseconds], the [`Heading`](#Ball.Heading) property to
 90 [degrees], and the [`Enabled`](#Ball.Enabled) property to `true`{:.logic.block}. These and its
 other properties can be changed at any time.

 The difference between a `Ball` and an `ImageSprite` is that the latter can get its appearance
 from an image file, while a `Ball`'s appearance can only be changed by varying its
 [`PaintColor`](#Ball.PaintColor) and [`Radius`](#Ball.Radius) properties.



### Properties  {#Ball-Properties}

{:.properties}

{:id="Ball.Enabled" .boolean} *Enabled*
: Controls whether the `Ball` moves when its speed is non-zero.

{:id="Ball.Heading" .number} *Heading*
: The `Ball`'s heading in degrees above the positive x-axis. Zero degrees is toward the right
 of the screen; 90 degrees is toward the top of the screen.

{:id="Ball.Interval" .number} *Interval*
: The interval in milliseconds at which the `Ball`'s position is updated. For example, if the
 `Interval` is 50 and the [`Speed`](#Ball.Speed) is 10, then the `Ball` will move 10 pixels
 every 50 milliseconds.

{:id="Ball.OriginAtCenter" .boolean .wo .do} *OriginAtCenter*
: Whether the x- and y-coordinates should represent the center of the `Ball`
 (`true`{:.logic.block}) or its left and top edges (`false`{:.logic.block}).

{:id="Ball.PaintColor" .color} *PaintColor*
: The color of the `Ball`.

{:id="Ball.Radius" .number} *Radius*
: The distance from the center of the `Ball` to its edge.

{:id="Ball.Speed" .number} *Speed*
: The speed at which the `Ball` moves. The `Ball` moves this many pixels every
 [`Interval`](#Ball.Interval) milliseconds if [`Enabled`](#Ball.Enabled) is `true`{:.logic.block}.

{:id="Ball.Visible" .boolean} *Visible*
: Sets whether sprite should be visible.

{:id="Ball.X" .number} *X*
: The horizontal coordinate of the `Ball`, increasing as the `Ball` moves right. If the property
 [`OriginAtCenter`](#Ball.OriginAtCenter) is true, the coordinate is for the center of the `Ball`;
 otherwise, it is for the leftmost point of the `Ball`.

{:id="Ball.Y" .number} *Y*
: The vertical coordinate of the `Ball`, increasing as the `Ball` moves down. If the property
 [`OriginAtCenter`](#Ball.OriginAtCenter) is true, the coordinate is for the center of the `Ball`
 otherwise, it is for the uppermost point of the `Ball`.

{:id="Ball.Z" .number} *Z*
: How the Ball should be layered relative to other Balls and ImageSprites, with higher-numbered layers in front of lower-numbered layers.

### Events  {#Ball-Events}

{:.events}

{:id="Ball.CollidedWith"} CollidedWith(*other*{:.component})
: Event handler called when two enabled sprites ([`Ball`](#Ball)s or [`ImageSprite`](#ImageSprite)s)
 collide. Note that checking for collisions with a rotated `ImageSprite` currently
 checks against its unrotated position. Therefore, collision
 checking will be inaccurate for tall narrow or short wide sprites that are
 rotated.

{:id="Ball.Dragged"} Dragged(*startX*{:.number},*startY*{:.number},*prevX*{:.number},*prevY*{:.number},*currentX*{:.number},*currentY*{:.number})
: Event handler for Dragged events.  On all calls, the starting coordinates
 are where the screen was first touched, and the "current" coordinates
 describe the endpoint of the current line segment.  On the first call
 within a given drag, the "previous" coordinates are the same as the
 starting coordinates; subsequently, they are the "current" coordinates
 from the prior call. Note that the `Ball` won't actually move
 anywhere in response to the Dragged event unless
 [`MoveTo`](#Ball.MoveTo) is specifically called.

{:id="Ball.EdgeReached"} EdgeReached(*edge*{:.number})
: Event handler called when the `Ball` reaches an `edge`{:.variable.block} of the screen.
 If [`Bounce`](#Ball.Bounce) is then called with that edge, the sprite will appear to bounce off
 of the edge it reached. Edge here is represented as an integer that indicates one of eight
 directions north(1), northeast(2), east(3), southeast(4), south (-1), southwest(-2), west(-3),
 and northwest(-4).

{:id="Ball.Flung"} Flung(*x*{:.number},*y*{:.number},*speed*{:.number},*heading*{:.number},*xvel*{:.number},*yvel*{:.number})
: When a fling gesture (quick swipe) is made on the sprite: provides
 the (x,y) position of the start of the fling, relative to the upper
 left of the canvas. Also provides the speed (pixels per millisecond) and heading
 (-180 to 180 degrees) of the fling, as well as the x velocity and y velocity
 components of the fling's vector.

{:id="Ball.NoLongerCollidingWith"} NoLongerCollidingWith(*other*{:.component})
: Event indicating that a pair of sprites are no longer colliding.

{:id="Ball.TouchDown"} TouchDown(*x*{:.number},*y*{:.number})
: When the user begins touching the sprite (places finger on sprite and
 leaves it there): provides the (x,y) position of the touch, relative
 to the upper left of the canvas

{:id="Ball.TouchUp"} TouchUp(*x*{:.number},*y*{:.number})
: When the user stops touching the sprite (lifts finger after a
 TouchDown event): provides the (x,y) position of the touch, relative
 to the upper left of the canvas.

{:id="Ball.Touched"} Touched(*x*{:.number},*y*{:.number})
: When the user touches the sprite and then immediately lifts finger: provides
 the (x,y) position of the touch, relative to the upper left of the canvas.

### Methods  {#Ball-Methods}

{:.methods}

{:id="Ball.Bounce" class="method"} <i/> Bounce(*edge*{:.number})
: Makes this `Ball` bounce, as if off a wall. For normal bouncing, the `edge` argument should
 be the one returned by [`EdgeReached`](#Ball.EdgeReached).

{:id="Ball.CollidingWith" class="method returns boolean"} <i/> CollidingWith(*other*{:.component})
: Indicates whether a collision has been registered between this `Ball`
 and the passed `other` sprite.

{:id="Ball.MoveIntoBounds" class="method"} <i/> MoveIntoBounds()
: Moves the sprite back in bounds if part of it extends out of bounds,
 having no effect otherwise. If the sprite is too wide to fit on the
 canvas, this aligns the left side of the sprite with the left side of the
 canvas. If the sprite is too tall to fit on the canvas, this aligns the
 top side of the sprite with the top side of the canvas.

{:id="Ball.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number})
: Sets the `x` and `y` coordinates of the `Ball`. If [`OriginAtCenter`](#Ball.OriginAtCenter) is true,
 the center of the `Ball` will be placed here. Otherwise, the top left edge of the `Ball` will
 be placed at the specified coordinates.

{:id="Ball.MoveToPoint" class="method"} <i/> MoveToPoint(*coordinates*{:.list})
: Moves the Ball so that its origin is at the specified x and y coordinates.

{:id="Ball.PointInDirection" class="method"} <i/> PointInDirection(*x*{:.number},*y*{:.number})
: Turns this `Ball` to point toward the point with the coordinates `(x, y)`.

{:id="Ball.PointTowards" class="method"} <i/> PointTowards(*target*{:.component})
: Turns this `Ball` to point towards a given `target` sprite. The new heading will be parallel
 to the line joining the origins of the two sprites.

## Canvas  {#Canvas}

A two-dimensional touch-sensitive rectangular panel on which drawing can
 be done and sprites can be moved.

 The [`BackgroundColor`](#Canvas.BackgroundColor), [`PaintColor`](#Canvas.PaintColor), [`BackgroundImage`](#Canvas.BackgroundImage),
 [`Width`](#Canvas.Width), and [`Height`](#Canvas.Height) of the `Canvas` can be set in either the Designer or in
 the Blocks Editor. The `Width` and `Height` are measured in pixels and must be positive.

 Any location on the `Canvas` can be specified as a pair of `(X, Y)` values, where

 * X is the number of pixels away from the left edge of the `Canvas`
 * Y is the number of pixels away from the top edge of the `Canvas`

 There are events to tell when and where a `Canvas` has been touched or a Sprite
 ([`ImageSprite`](#ImageSprite) or [`Ball`](#Ball)) has been dragged. There are also methods for drawing
 points, lines, circles, shapes, arcs, and text.



### Properties  {#Canvas-Properties}

{:.properties}

{:id="Canvas.BackgroundColor" .color} *BackgroundColor*
: Specifies the Canvas's background color as an alpha-red-green-blue
 integer, i.e., `0xAARRGGBB`.  An alpha of `00`
 indicates fully transparent and `FF` means opaque.
 The background color only shows if there is no background image.

{:id="Canvas.BackgroundImage" .text} *BackgroundImage*
: Specifies the name of a file containing the background image for the `Canvas`.

{:id="Canvas.BackgroundImageinBase64" .text .wo .bo} *BackgroundImageinBase64*
: Set the background image in Base64 format. This requires API level >= 8. For devices with API level less than 8, setting this will end up with an empty background.

{:id="Canvas.ExtendMovesOutsideCanvas" .boolean} *ExtendMovesOutsideCanvas*
: Determines whether moves can extend beyond the canvas borders.   Default is false. This should normally be false, and the property is provided for backwards compatibility.

{:id="Canvas.FontSize" .number} *FontSize*
: Specifies the font size of text drawn on the Canvas.

{:id="Canvas.Height" .number .bo} *Height*
: Specifies the `Canvas`'s vertical height, measured in pixels.

{:id="Canvas.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `Canvas`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="Canvas.Left" .number} *Left*
: Specifies the position of the Left edge of the component relative to an
 AbsoluteArrangement.

{:id="Canvas.LineWidth" .number} *LineWidth*
: Specifies the width of lines drawn on the Canvas.

{:id="Canvas.PaintColor" .color} *PaintColor*
: Specifies the paint color as an alpha-red-green-blue integer,
 i.e., `0xAARRGGBB`.  An alpha of `00` indicates fully
 transparent and `FF` means opaque.

{:id="Canvas.TapThreshold" .number} *TapThreshold*
: Specifies the movement threshold to differentiate a drag from a tap.

{:id="Canvas.TextAlignment" .number} *TextAlignment*
: Specifies the alignment of the canvas's text: center, normal
 (starting at the specified point in [`DrawText`](#Canvas.DrawText) or
 [`DrawTextAtAngle`](#Canvas.DrawTextAtAngle)),
 or opposite (ending at the specified point in
 [`DrawText`](#Canvas.DrawText) or
 [`DrawTextAtAngle`](#Canvas.DrawTextAtAngle)).

{:id="Canvas.Top" .number} *Top*
: Specifies the position of the Top edge of the component relative to an
 AbsoluteArrangement.

{:id="Canvas.Visible" .boolean} *Visible*
: Specifies whether the `Canvas` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `Canvas` is showing and `false`{:.logic.block} if hidden.

{:id="Canvas.Width" .number .bo} *Width*
: Specifies the horizontal width of the `Canvas`, measured in pixels.

{:id="Canvas.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `Canvas` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#Canvas-Events}

{:.events}

{:id="Canvas.Dragged"} Dragged(*startX*{:.number},*startY*{:.number},*prevX*{:.number},*prevY*{:.number},*currentX*{:.number},*currentY*{:.number},*draggedAnySprite*{:.boolean})
: When the user does a drag from one point (prevX, prevY) to
 another (x, y).  The pair (startX, startY) indicates where the
 user first touched the screen, and "draggedAnySprite" indicates whether a
 sprite is being dragged.

{:id="Canvas.Flung"} Flung(*x*{:.number},*y*{:.number},*speed*{:.number},*heading*{:.number},*xvel*{:.number},*yvel*{:.number},*flungSprite*{:.boolean})
: When a fling gesture (quick swipe) is made on the canvas: provides
 the (x,y) position of the start of the fling, relative to the upper
 left of the canvas. Also provides the speed (pixels per millisecond) and heading
 (-180 to 180 degrees) of the fling, as well as the x velocity and y velocity
 components of the fling's vector. The value "flungSprite" is true if a sprite
 was located near the the starting point of the fling gesture.

{:id="Canvas.TouchDown"} TouchDown(*x*{:.number},*y*{:.number})
: When the user begins touching the canvas (places finger on canvas and
 leaves it there): provides the (x,y) position of the touch, relative
 to the upper left of the canvas

{:id="Canvas.TouchUp"} TouchUp(*x*{:.number},*y*{:.number})
: When the user stops touching the canvas (lifts finger after a
 TouchDown event): provides the (x,y) position of the touch, relative
 to the upper left of the canvas

{:id="Canvas.Touched"} Touched(*x*{:.number},*y*{:.number},*touchedAnySprite*{:.boolean})
: When the user touches the canvas and then immediately lifts finger: provides
 the (x,y) position of the touch, relative to the upper left of the canvas.  TouchedAnySprite
 is true if the same touch also touched a sprite, and false otherwise.

### Methods  {#Canvas-Methods}

{:.methods}

{:id="Canvas.Clear" class="method"} <i/> Clear()
: Clears the canvas, without removing the background image, if one
 was provided.

{:id="Canvas.DrawArc" class="method"} <i/> DrawArc(*left*{:.number},*top*{:.number},*right*{:.number},*bottom*{:.number},*startAngle*{:.number},*sweepAngle*{:.number},*useCenter*{:.boolean},*fill*{:.boolean})
: Draw an arc on Canvas, by drawing an arc from a specified oval (specified by left, top, right & bottom).
 Start angle is 0 when heading to the right, and increase when rotate clockwise.
 When useCenter is true, a sector will be drawed instead of an arc.
 When fill is true, a filled arc (or sector) will be drawed instead of just an outline.

{:id="Canvas.DrawCircle" class="method"} <i/> DrawCircle(*centerX*{:.number},*centerY*{:.number},*radius*{:.number},*fill*{:.boolean})
: Draws a circle (filled in) with the given radius centered at the given coordinates on the
 Canvas.

{:id="Canvas.DrawLine" class="method"} <i/> DrawLine(*x1*{:.number},*y1*{:.number},*x2*{:.number},*y2*{:.number})
: Draws a line between the given coordinates on the canvas.

{:id="Canvas.DrawPoint" class="method"} <i/> DrawPoint(*x*{:.number},*y*{:.number})
: Draws a point at the given coordinates on the canvas.

{:id="Canvas.DrawShape" class="method"} <i/> DrawShape(*pointList*{:.list},*fill*{:.boolean})
: Draws a shape on the canvas.
 pointList should be a list contains sub-lists with two number which represents a coordinate.
 The first point and last point does not need to be the same. e.g. ((x1 y1) (x2 y2) (x3 y3))
 When fill is true, the shape will be filled.

{:id="Canvas.DrawText" class="method"} <i/> DrawText(*text*{:.text},*x*{:.number},*y*{:.number})
: Draws the specified text relative to the specified coordinates
 using the values of the [`FontSize`](#Canvas.FontSize) and
 [`TextAlignment`](#Canvas.TextAlignment) properties.

{:id="Canvas.DrawTextAtAngle" class="method"} <i/> DrawTextAtAngle(*text*{:.text},*x*{:.number},*y*{:.number},*angle*{:.number})
: Draws the specified text starting at the specified coordinates
 at the specified angle using the values of the [`FontSize`](#Canvas.FontSize) and
 [`TextAlignment`](#Canvas.TextAlignment) properties.

{:id="Canvas.GetBackgroundPixelColor" class="method returns color"} <i/> GetBackgroundPixelColor(*x*{:.number},*y*{:.number})
: Gets the color of the given pixel, ignoring sprites.

{:id="Canvas.GetPixelColor" class="method returns color"} <i/> GetPixelColor(*x*{:.number},*y*{:.number})
: Gets the color of the given pixel, including sprites.

{:id="Canvas.Save" class="method returns text"} <i/> Save()
: Saves a picture of this Canvas to the device's external storage.
 If an error occurs, the Screen's ErrorOccurred event will be called.

{:id="Canvas.SaveAs" class="method returns text"} <i/> SaveAs(*fileName*{:.text})
: Saves a picture of this Canvas to the device's external storage in the file
 named fileName. fileName must end with one of ".jpg", ".jpeg", or ".png"
 (which determines the file type: JPEG, or PNG).

{:id="Canvas.SetBackgroundPixelColor" class="method"} <i/> SetBackgroundPixelColor(*x*{:.number},*y*{:.number},*color*{:.color})
: Sets the color of the given pixel.  This has no effect if the
 coordinates are out of bounds.

## ImageSprite  {#ImageSprite}

A 'sprite' that can be placed on a [`Canvas`](#Canvas), where it can react to touches and drags,
 interact with other sprites ([`Ball`](#Ball)s and other `ImageSprite`s) and the edge of the
 `Canvas`, and move according to its property values. Its appearance is that of the image
 specified in its [`Picture`](#ImageSprite.Picture) property (unless its [`Visible`](#ImageSprite.Visible) property is
 `false`{:.logic.block}.

 To have an `ImageSprite` move 10 pixels to the left every 1000 milliseconds (one second), for
 example, you would set the [`Speed`](#ImageSprite.Speed) property to 10 [pixels], the [`Interval`](#ImageSprite.Interval)
 property to 1000 [milliseconds], the [`Heading`](#ImageSprite.Heading) property to 180 [degrees], and the
 [`Enabled`](#ImageSprite.Enabled) property to `true`{:.logic.block}. A sprite whose [`Rotates`](#ImageSprite.Rotates)
 property is `true`{:.logic.block} will rotate its image as the sprite's heading changes.
 *Checking for collisions with a rotated sprite currently checks the sprite's unrotated position
 so that collision checking will be inaccurate for tall narrow or short wide sprites that are
 rotated.* Any of the sprite properties can be changed at any time under program control.



### Properties  {#ImageSprite-Properties}

{:.properties}

{:id="ImageSprite.Enabled" .boolean} *Enabled*
: Controls whether the `ImageSprite` moves when its speed is non-zero.

{:id="ImageSprite.Heading" .number} *Heading*
: The `ImageSprite`'s heading in degrees above the positive x-axis. Zero degrees is toward the right
 of the screen; 90 degrees is toward the top of the screen.

{:id="ImageSprite.Height" .number .bo} *Height*
: The height of the ImageSprite in pixels.

{:id="ImageSprite.Interval" .number} *Interval*
: The interval in milliseconds at which the `ImageSprite`'s position is updated. For example, if the
 `Interval` is 50 and the [`Speed`](#ImageSprite.Speed) is 10, then the `ImageSprite` will move 10 pixels
 every 50 milliseconds.

{:id="ImageSprite.MarkOrigin" .text .wo} *MarkOrigin*
: Mark the origin of ImageSprite using a draggable marker.

{:id="ImageSprite.OriginX" .number} *OriginX*
: Horizontal unit coordinate of the origin with respect to left edge. Permitted values in [0, 1].
 A value of 0.0 means the origin lies on the left edge, 0.5 means the origin lies in the middle
 and 1.0 means the origin is on the right edge.

{:id="ImageSprite.OriginY" .number} *OriginY*
: Vertical unit coordinate of the origin with respect to top edge. Permitted values in [0, 1].
 A value of 0.0 means the origin lies on the top edge, 0.5 means the origin lies in the middle
 and 1.0 means the origin is on the bottom edge.

{:id="ImageSprite.Picture" .text} *Picture*
: Specifies the path of the sprite's picture.

{:id="ImageSprite.Rotates" .boolean} *Rotates*
: If true, the sprite image rotates to match the sprite's heading. If false, the sprite image
 does not rotate when the sprite changes heading. The sprite rotates around its origin.

{:id="ImageSprite.Speed" .number} *Speed*
: The speed at which the `ImageSprite` moves. The `ImageSprite` moves this many pixels every
 [`Interval`](#ImageSprite.Interval) milliseconds if [`Enabled`](#ImageSprite.Enabled) is `true`{:.logic.block}.

{:id="ImageSprite.Visible" .boolean} *Visible*
: Sets whether sprite should be visible.

{:id="ImageSprite.Width" .number .bo} *Width*
: The width of the ImageSprite in pixels.

{:id="ImageSprite.X" .number} *X*
: The horizontal coordinate of the origin of the ImageSprite, increasing as the ImageSprite moves right.

{:id="ImageSprite.Y" .number} *Y*
: The vertical coordinate of the origin of the ImageSprite, increasing as the ImageSprite moves down.

{:id="ImageSprite.Z" .number} *Z*
: How the ImageSprite should be layered relative to other Balls and ImageSprites, with higher-numbered layers in front of lower-numbered layers.

### Events  {#ImageSprite-Events}

{:.events}

{:id="ImageSprite.CollidedWith"} CollidedWith(*other*{:.component})
: Event handler called when two enabled sprites ([`Ball`](#Ball)s or [`ImageSprite`](#ImageSprite)s)
 collide. Note that checking for collisions with a rotated `ImageSprite` currently
 checks against its unrotated position. Therefore, collision
 checking will be inaccurate for tall narrow or short wide sprites that are
 rotated.

{:id="ImageSprite.Dragged"} Dragged(*startX*{:.number},*startY*{:.number},*prevX*{:.number},*prevY*{:.number},*currentX*{:.number},*currentY*{:.number})
: Event handler for Dragged events.  On all calls, the starting coordinates
 are where the screen was first touched, and the "current" coordinates
 describe the endpoint of the current line segment.  On the first call
 within a given drag, the "previous" coordinates are the same as the
 starting coordinates; subsequently, they are the "current" coordinates
 from the prior call. Note that the `ImageSprite` won't actually move
 anywhere in response to the Dragged event unless
 [`MoveTo`](#ImageSprite.MoveTo) is specifically called.

{:id="ImageSprite.EdgeReached"} EdgeReached(*edge*{:.number})
: Event handler called when the `ImageSprite` reaches an `edge`{:.variable.block} of the screen.
 If [`Bounce`](#ImageSprite.Bounce) is then called with that edge, the sprite will appear to bounce off
 of the edge it reached. Edge here is represented as an integer that indicates one of eight
 directions north(1), northeast(2), east(3), southeast(4), south (-1), southwest(-2), west(-3),
 and northwest(-4).

{:id="ImageSprite.Flung"} Flung(*x*{:.number},*y*{:.number},*speed*{:.number},*heading*{:.number},*xvel*{:.number},*yvel*{:.number})
: When a fling gesture (quick swipe) is made on the sprite: provides
 the (x,y) position of the start of the fling, relative to the upper
 left of the canvas. Also provides the speed (pixels per millisecond) and heading
 (-180 to 180 degrees) of the fling, as well as the x velocity and y velocity
 components of the fling's vector.

{:id="ImageSprite.NoLongerCollidingWith"} NoLongerCollidingWith(*other*{:.component})
: Event indicating that a pair of sprites are no longer colliding.

{:id="ImageSprite.TouchDown"} TouchDown(*x*{:.number},*y*{:.number})
: When the user begins touching the sprite (places finger on sprite and
 leaves it there): provides the (x,y) position of the touch, relative
 to the upper left of the canvas

{:id="ImageSprite.TouchUp"} TouchUp(*x*{:.number},*y*{:.number})
: When the user stops touching the sprite (lifts finger after a
 TouchDown event): provides the (x,y) position of the touch, relative
 to the upper left of the canvas.

{:id="ImageSprite.Touched"} Touched(*x*{:.number},*y*{:.number})
: When the user touches the sprite and then immediately lifts finger: provides
 the (x,y) position of the touch, relative to the upper left of the canvas.

### Methods  {#ImageSprite-Methods}

{:.methods}

{:id="ImageSprite.Bounce" class="method"} <i/> Bounce(*edge*{:.number})
: Makes this `ImageSprite` bounce, as if off a wall. For normal bouncing, the `edge` argument should
 be the one returned by [`EdgeReached`](#ImageSprite.EdgeReached).

{:id="ImageSprite.CollidingWith" class="method returns boolean"} <i/> CollidingWith(*other*{:.component})
: Indicates whether a collision has been registered between this `ImageSprite`
 and the passed `other` sprite.

{:id="ImageSprite.MoveIntoBounds" class="method"} <i/> MoveIntoBounds()
: Moves the sprite back in bounds if part of it extends out of bounds,
 having no effect otherwise. If the sprite is too wide to fit on the
 canvas, this aligns the left side of the sprite with the left side of the
 canvas. If the sprite is too tall to fit on the canvas, this aligns the
 top side of the sprite with the top side of the canvas.

{:id="ImageSprite.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number})
: Moves the ImageSprite so that its origin is at the specified `x` and `y` coordinates.

{:id="ImageSprite.MoveToPoint" class="method"} <i/> MoveToPoint(*coordinates*{:.list})
: Moves the ImageSprite so that its origin is at the specified x and y coordinates.

{:id="ImageSprite.PointInDirection" class="method"} <i/> PointInDirection(*x*{:.number},*y*{:.number})
: Turns this `ImageSprite` to point toward the point with the coordinates `(x, y)`.

{:id="ImageSprite.PointTowards" class="method"} <i/> PointTowards(*target*{:.component})
: Turns this `ImageSprite` to point towards a given `target` sprite. The new heading will be parallel
 to the line joining the origins of the two sprites.
