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

### Properties  {#Ball-Properties}

{:.properties}

{:id="Ball.Enabled" .boolean} *Enabled*
: Controls whether the Ball moves and can be interacted with through collisions, dragging, touching, and flinging.

{:id="Ball.Heading" .number} *Heading*
: Sets heading in which sprite should move.  In addition to changing the
 local variables

{:id="Ball.Interval" .number} *Interval*
: The interval in milliseconds at which the Ball's position is updated.  For example, if the interval is 50 and the speed is 10, then every 50 milliseconds the sprite will move 10 pixels in the heading direction.

{:id="Ball.OriginAtCenter" .boolean .wo .do} *OriginAtCenter*
: Whether the x- and y-coordinates should represent the center of the Ball (<code>true</code>) or its left and top edges (<code>false</code>).

{:id="Ball.PaintColor" .color} *PaintColor*
: The color of the Ball.

{:id="Ball.Radius" .number} *Radius*
: The distance from the edge of the Ball to its center.

{:id="Ball.Speed" .number} *Speed*
: The number of pixels that the Ball should move every interval, if enabled.

{:id="Ball.Visible" .boolean} *Visible*
: Whether the Ball is visible.

{:id="Ball.X" .number} *X*
: The horizontal coordinate of the Ball, increasing as the Ball moves right. If the property OriginAtCenter is true, the coodinate is for the center of the Ball; otherwise, it is for the leftmost point of the Ball.

{:id="Ball.Y" .number} *Y*
: The vertical coordinate of the Ball, increasing as the Ball moves down. If the property OriginAtCenter is true, the coodinate is for the center of the Ball; otherwise, it is for the uppermost point of the Ball.

{:id="Ball.Z" .number} *Z*
: Sets the layer of the sprite, indicating whether it will appear in
 front of or behind other sprites.

### Events  {#Ball-Events}

{:.events}

{:id="Ball.CollidedWith"} CollidedWith(*other*{:.component})
: Event handler called when two enabled sprites (Balls or ImageSprites)
 collide. Note that checking for collisions with a rotated ImageSprite currently
 checks against its unrotated position. Therefore, collision
 checking will be inaccurate for tall narrow or short wide sprites that are
 rotated.

{:id="Ball.Dragged"} Dragged(*startX*{:.number},*startY*{:.number},*prevX*{:.number},*prevY*{:.number},*currentX*{:.number},*currentY*{:.number})
: Event handler called when a Ball is dragged. On all calls, the starting coordinates are where the screen was first touched, and the "current" coordinates describe the endpoint of the current line segment. On the first call within a given drag, the "previous" coordinates are the same as the starting coordinates; subsequently, they are the "current" coordinates from the prior call. Note that the Ball won't actually move anywhere in response to the Dragged event unless MoveTo is explicitly called. For smooth movement, each of its coordinates should be set to the sum of its initial value and the difference between its current and previous values.

{:id="Ball.EdgeReached"} EdgeReached(*edge*{:.number})
: Event handler called when the Ball reaches an edge of the screen. If Bounce is then called with that edge, the Ball will appear to bounce off of the edge it reached. Edge here is represented as an integer that indicates one of eight directions north (1), northeast (2), east (3), southeast (4), south (-1), southwest (-2), west (-3), and northwest (-4).

{:id="Ball.Flung"} Flung(*x*{:.number},*y*{:.number},*speed*{:.number},*heading*{:.number},*xvel*{:.number},*yvel*{:.number})
: Event handler called when a fling gesture (quick swipe) is made on an enabled Ball. This provides the x and y coordinates of the start of the fling (relative to the upper left of the canvas), the speed (pixels per millisecond), the heading (0-360 degrees), and the x and y velocity components of the fling's vector.

{:id="Ball.NoLongerCollidingWith"} NoLongerCollidingWith(*other*{:.component})
: Event handler called when a pair of sprites (Balls and ImageSprites) are no longer colliding.

{:id="Ball.TouchDown"} TouchDown(*x*{:.number},*y*{:.number})
: Event handler called when the user begins touching an enabled Ball (placing their finger on a Ball and leaving it there). This provides the x and y coordinates of the touch, relative to the upper left of the canvas.

{:id="Ball.TouchUp"} TouchUp(*x*{:.number},*y*{:.number})
: Event handler called when the user stops touching an enabled Ball (lifting their finger after a TouchDown event). This provides the x and y coordinates of the touch, relative to the upper left of the canvas.

{:id="Ball.Touched"} Touched(*x*{:.number},*y*{:.number})
: Event handler called when the user touches an enabled Ball and then immediately lifts their finger. The provided x and y coordinates are relative to the upper left of the canvas.

### Methods  {#Ball-Methods}

{:.methods}

{:id="Ball.Bounce" class="method"} <i/> Bounce(*edge*{:.number})
: Makes the Ball bounce, as if off a wall. For normal bouncing, the edge argument should be the one returned by EdgeReached.

{:id="Ball.CollidingWith" class="method returns boolean"} <i/> CollidingWith(*other*{:.component})
: Indicates whether a collision has been registered between this Ball and the passed sprite (Ball or ImageSprite).

{:id="Ball.MoveIntoBounds" class="method"} <i/> MoveIntoBounds()
: Moves the Ball back in bounds if part of it extends out of bounds, having no effect otherwise. If the Ball is too wide to fit on the canvas, this aligns the left side of the Ball with the left side of the canvas. If the Ball is too tall to fit on the canvas, this aligns the top side of the Ball with the top side of the canvas.

{:id="Ball.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number})
: Sets the x and y coordinates of the Ball. If CenterAtOrigin is true, the center of the Ball will be placed here. Otherwise, the top left edge of the Ball will be placed at the specified coordinates.

{:id="Ball.PointInDirection" class="method"} <i/> PointInDirection(*x*{:.number},*y*{:.number})
: Sets the heading of the Ball toward the point with the coordinates (x, y).

{:id="Ball.PointTowards" class="method"} <i/> PointTowards(*target*{:.component})
: Turns the Ball to point towards a designated target sprite (Ball or ImageSprite). The new heading will be parallel to the line joining the centerpoints of the two sprites.

## Canvas  {#Canvas}

### Properties  {#Canvas-Properties}

{:.properties}

{:id="Canvas.BackgroundColor" .color} *BackgroundColor*
: The color of the canvas background.

{:id="Canvas.BackgroundImage" .text} *BackgroundImage*
: The name of a file containing the background image for the canvas

{:id="Canvas.ExtendMovesOutsideCanvas" .boolean} *ExtendMovesOutsideCanvas*
: Determines whether moves can extend beyond the canvas borders.   Default is false. This should normally be false, and the property is provided for backwards compatibility.

{:id="Canvas.FontSize" .number} *FontSize*
: The font size of text drawn on the canvas.

{:id="Canvas.Height" .number .bo} *Height*
: Set the canvas height

 The height can only be set to >0 or -1 (automatic) or -2 (fill parent) or
 to a value less then or equal to LENGTH_PERCENT_TAG (which is later
 converted to pixels.

{:id="Canvas.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the Canvas as a percentage of the height of the Screen.

{:id="Canvas.LineWidth" .number} *LineWidth*
: The width of lines drawn on the canvas.

{:id="Canvas.PaintColor" .color} *PaintColor*
: The color in which lines are drawn

{:id="Canvas.TextAlignment" .number} *TextAlignment*
: Determines the alignment of the text drawn by DrawText() or DrawAngle() with respect to the point specified by that command: point at the left of the text, point at the center of the text, or point at the right of the text.

{:id="Canvas.Visible" .boolean} *Visible*
: Returns true iff the Canvas is visible.

{:id="Canvas.Width" .number .bo} *Width*
: Set the canvas width

 The width can only be set to >0 or -1 (automatic) or -2 (fill parent)
 or to a value less then or equal to LENGTH_PERCENT_TAG (which is later
 converted to pixels.

{:id="Canvas.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the Canvas as a percentage of the width of the Screen.

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
 (0-360 degrees) of the fling, as well as the x velocity and y velocity
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
: Clears anything drawn on this Canvas but not any background color or image.

{:id="Canvas.DrawArc" class="method"} <i/> DrawArc(*left*{:.number},*top*{:.number},*right*{:.number},*bottom*{:.number},*startAngle*{:.number},*sweepAngle*{:.number},*useCenter*{:.boolean},*fill*{:.boolean})
: Draw an arc on Canvas, by drawing an arc from a specified oval (specified by left, top, right & bottom). Start angle is 0 when heading to the right, and increase when rotate clockwise. When useCenter is true, a sector will be drawed instead of an arc. When fill is true, a filled arc (or sector) will be drawed instead of just an outline.

{:id="Canvas.DrawCircle" class="method"} <i/> DrawCircle(*centerX*{:.number},*centerY*{:.number},*radius*{:.number},*fill*{:.boolean})
: Draws a circle (filled in) with the given radius centered at the given coordinates on the canvas

{:id="Canvas.DrawLine" class="method"} <i/> DrawLine(*x1*{:.number},*y1*{:.number},*x2*{:.number},*y2*{:.number})
: Draws a line between the given coordinates on the canvas.

{:id="Canvas.DrawPoint" class="method"} <i/> DrawPoint(*x*{:.number},*y*{:.number})
: Draws a point at the given coordinates on the canvas.

{:id="Canvas.DrawShape" class="method"} <i/> DrawShape(*pointList*{:.list},*fill*{:.boolean})
: Draws a shape on the canvas. pointList should be a list contains sub-lists with two number which represents a coordinate. The first point and last point does not need to be the same. e.g. ((x1 y1) (x2 y2) (x3 y3)) When fill is true, the shape will be filled.

{:id="Canvas.DrawText" class="method"} <i/> DrawText(*text*{:.text},*x*{:.number},*y*{:.number})
: Draws the specified text relative to the specified coordinates using the values of the FontSize and TextAlignment properties.

{:id="Canvas.DrawTextAtAngle" class="method"} <i/> DrawTextAtAngle(*text*{:.text},*x*{:.number},*y*{:.number},*angle*{:.number})
: Draws the specified text starting at the specified coordinates at the specified angle using the values of the FontSize and TextAlignment properties.

{:id="Canvas.GetBackgroundPixelColor" class="method returns color"} <i/> GetBackgroundPixelColor(*x*{:.number},*y*{:.number})
: Gets the color of the specified point. This includes the background and any drawn points, lines, or circles but not sprites.

{:id="Canvas.GetPixelColor" class="method returns color"} <i/> GetPixelColor(*x*{:.number},*y*{:.number})
: Gets the color of the specified point.

{:id="Canvas.Save" class="method returns text"} <i/> Save()
: Saves a picture of this Canvas to the device's external storage. If an error occurs, the Screen's ErrorOccurred event will be called.

{:id="Canvas.SaveAs" class="method returns text"} <i/> SaveAs(*fileName*{:.text})
: Saves a picture of this Canvas to the device's external storage in the file named fileName. fileName must end with one of .jpg, .jpeg, or .png, which determines the file type.

{:id="Canvas.SetBackgroundPixelColor" class="method"} <i/> SetBackgroundPixelColor(*x*{:.number},*y*{:.number},*color*{:.color})
: Sets the color of the specified point. This differs from DrawPoint by having an argument for color.

## ImageSprite  {#ImageSprite}

### Properties  {#ImageSprite-Properties}

{:.properties}

{:id="ImageSprite.Enabled" .boolean} *Enabled*
: Controls whether the ImageSprite moves and can be interacted with through collisions, dragging, touching, and flinging.

{:id="ImageSprite.Heading" .number} *Heading*
: Sets heading in which sprite should move.  In addition to changing the
 local variables

{:id="ImageSprite.Height" .number .bo} *Height*
: The height of the ImageSprite in pixels.

{:id="ImageSprite.Interval" .number} *Interval*
: The interval in milliseconds at which the ImageSprite's position is updated.  For example, if the interval is 50 and the speed is 10, then every 50 milliseconds the sprite will move 10 pixels in the heading direction.

{:id="ImageSprite.Picture" .text} *Picture*
: The picture that determines the ImageSprite's appearance.

{:id="ImageSprite.Rotates" .boolean} *Rotates*
: Whether the image should rotate to match the ImageSprite's heading. The sprite rotates around its centerpoint.

{:id="ImageSprite.Speed" .number} *Speed*
: The number of pixels that the ImageSprite should move every interval, if enabled.

{:id="ImageSprite.Visible" .boolean} *Visible*
: Whether the ImageSprite is visible.

{:id="ImageSprite.Width" .number .bo} *Width*
: The width of the ImageSprite in pixels.

{:id="ImageSprite.X" .number} *X*
: The horizontal coordinate of the left edge of the ImageSprite, increasing as the ImageSprite moves right.

{:id="ImageSprite.Y" .number} *Y*
: The vertical coordinate of the top edge of the ImageSprite, increasing as the ImageSprite moves down.

{:id="ImageSprite.Z" .number} *Z*
: Sets the layer of the sprite, indicating whether it will appear in
 front of or behind other sprites.

### Events  {#ImageSprite-Events}

{:.events}

{:id="ImageSprite.CollidedWith"} CollidedWith(*other*{:.component})
: Event handler called when two enabled sprites (Balls or ImageSprites)
 collide. Note that checking for collisions with a rotated ImageSprite currently
 checks against its unrotated position. Therefore, collision
 checking will be inaccurate for tall narrow or short wide sprites that are
 rotated.

{:id="ImageSprite.Dragged"} Dragged(*startX*{:.number},*startY*{:.number},*prevX*{:.number},*prevY*{:.number},*currentX*{:.number},*currentY*{:.number})
: Event handler called when a ImageSprite is dragged. On all calls, the starting coordinates are where the screen was first touched, and the "current" coordinates describe the endpoint of the current line segment. On the first call within a given drag, the "previous" coordinates are the same as the starting coordinates; subsequently, they are the "current" coordinates from the prior call. Note that the ImageSprite won't actually move anywhere in response to the Dragged event unless MoveTo is explicitly called. For smooth movement, each of its coordinates should be set to the sum of its initial value and the difference between its current and previous values.

{:id="ImageSprite.EdgeReached"} EdgeReached(*edge*{:.number})
: Event handler called when the ImageSprite reaches an edge of the screen. If Bounce is then called with that edge, the ImageSprite will appear to bounce off of the edge it reached. Edge here is represented as an integer that indicates one of eight directions north (1), northeast (2), east (3), southeast (4), south (-1), southwest (-2), west (-3), and northwest (-4).

{:id="ImageSprite.Flung"} Flung(*x*{:.number},*y*{:.number},*speed*{:.number},*heading*{:.number},*xvel*{:.number},*yvel*{:.number})
: Event handler called when a fling gesture (quick swipe) is made on an enabled ImageSprite. This provides the x and y coordinates of the start of the fling (relative to the upper left of the canvas), the speed (pixels per millisecond), the heading (0-360 degrees), and the x and y velocity components of the fling's vector.

{:id="ImageSprite.NoLongerCollidingWith"} NoLongerCollidingWith(*other*{:.component})
: Event handler called when a pair of sprites (Balls and ImageSprites) are no longer colliding.

{:id="ImageSprite.TouchDown"} TouchDown(*x*{:.number},*y*{:.number})
: Event handler called when the user begins touching an enabled ImageSprite (placing their finger on a ImageSprite and leaving it there). This provides the x and y coordinates of the touch, relative to the upper left of the canvas.

{:id="ImageSprite.TouchUp"} TouchUp(*x*{:.number},*y*{:.number})
: Event handler called when the user stops touching an enabled ImageSprite (lifting their finger after a TouchDown event). This provides the x and y coordinates of the touch, relative to the upper left of the canvas.

{:id="ImageSprite.Touched"} Touched(*x*{:.number},*y*{:.number})
: Event handler called when the user touches an enabled ImageSprite and then immediately lifts their finger. The provided x and y coordinates are relative to the upper left of the canvas.

### Methods  {#ImageSprite-Methods}

{:.methods}

{:id="ImageSprite.Bounce" class="method"} <i/> Bounce(*edge*{:.number})
: Makes the ImageSprite bounce, as if off a wall. For normal bouncing, the edge argument should be the one returned by EdgeReached.

{:id="ImageSprite.CollidingWith" class="method returns boolean"} <i/> CollidingWith(*other*{:.component})
: Indicates whether a collision has been registered between this ImageSprite and the passed sprite (Ball or ImageSprite).

{:id="ImageSprite.MoveIntoBounds" class="method"} <i/> MoveIntoBounds()
: Moves the ImageSprite back in bounds if part of it extends out of bounds, having no effect otherwise. If the ImageSprite is too wide to fit on the canvas, this aligns the left side of the ImageSprite with the left side of the canvas. If the ImageSprite is too tall to fit on the canvas, this aligns the top side of the ImageSprite with the top side of the canvas.

{:id="ImageSprite.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number})
: Moves the ImageSprite so that its left top corner is at the specfied x and y coordinates.

{:id="ImageSprite.PointInDirection" class="method"} <i/> PointInDirection(*x*{:.number},*y*{:.number})
: Sets the heading of the ImageSprite toward the point with the coordinates (x, y).

{:id="ImageSprite.PointTowards" class="method"} <i/> PointTowards(*target*{:.component})
: Turns the ImageSprite to point towards a designated target sprite (Ball or ImageSprite). The new heading will be parallel to the line joining the centerpoints of the two sprites.