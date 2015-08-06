// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.errors.AssertionFailure;
import com.google.appinventor.components.runtime.errors.IllegalArgumentError;
import com.google.appinventor.components.runtime.util.BoundingBox;
import com.google.appinventor.components.runtime.util.TimerInternal;

import android.os.Handler;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Superclass of sprites able to move and interact with other sprites.
 *
 * While the Simple programmer sees the x- and y-coordinates as integers,
 * they are maintained internally as doubles so fractional changes (caused
 * by multiplying the speed by a cosine or sine value) have the chance to
 * add up.
 *
 * @author spertus.google.com (Ellen Spertus)
 */
@SimpleObject
public abstract class Sprite extends VisibleComponent
    implements AlarmHandler, OnDestroyListener, Deleteable {
  private static final String LOG_TAG = "Sprite";
  private static final boolean DEFAULT_ENABLED = true;  // Enable timer for movement
  private static final int DEFAULT_HEADING = 0;      // degrees
  private static final int DEFAULT_INTERVAL = 100;  // ms
  private static final float DEFAULT_SPEED = 0.0f;   // pixels per interval
  private static final boolean DEFAULT_VISIBLE = true;
  private static final double DEFAULT_Z = 1.0;

  protected final Canvas canvas;              // enclosing Canvas
  private final TimerInternal timerInternal;  // timer to control movement
  private final Handler androidUIHandler;     // for posting actions

  // Keeps track of which other sprites are currently colliding with this one.
  // That way, we don't raise CollidedWith() more than once for each collision.
  // Events are only raised when sprites are added to this collision set.  They
  // are removed when they no longer collide.
  private final Set<Sprite> registeredCollisions;

  // This variable prevents events from being raised before construction of
  // all components has taken place.  This was added to fix bug 2262218.
  protected boolean initialized = false;

  // Properties: These are protected, instead of private, both so they
  // can be used by subclasses and tests.
  protected int interval;      // number of milliseconds until next move
  protected boolean visible = true;
  // TODO(user): Convert to have co-ordinates be center, not upper left.
  // Note that this would simplify pointTowards to remove the adjustment
  // to the center points
  protected double xLeft;      // leftmost x-coordinate
  protected double yTop;       // uppermost y-coordinate
  protected double zLayer;     // z-coordinate, higher values go in front
  protected float speed;       // magnitude in pixels

  protected Form form;

  /**
   * The angle, in degrees above the positive x-axis, specified by the user.
   * This is private in order to enforce that changing it also changes
   * {@link #heading}, {@link #headingRadians}, {@link #headingCos}, and
   * {@link #headingSin}.
   */
  protected double userHeading;

  /**
   * The angle, in degrees <em>below</em> the positive x-axis, specified by the
   * user.  We use this to compute new coordinates because, on Android, the
   * y-coordinate increases "below" the x-axis.
   */
  protected double heading;
  protected double headingRadians;  // heading in radians
  protected double headingCos;      // cosine(heading)
  protected double headingSin;      // sine(heading)

  /**
   * Creates a new Sprite component.  This version exists to allow injection
   * of a mock handler for testing.
   *
   * @param container where the component will be placed
   * @param handler a scheduler to which runnable events will be posted
   */
  protected Sprite(ComponentContainer container, Handler handler) {
    super();
    androidUIHandler = handler;

    // Add to containing Canvas.
    if (!(container instanceof Canvas)) {
      throw new IllegalArgumentError("Sprite constructor called with container " + container);
    }
    this.canvas = (Canvas) container;
    this.canvas.addSprite(this);

    // Maintain a list of collisions.
    registeredCollisions = new HashSet<Sprite>();

    // Set in motion.
    timerInternal = new TimerInternal(this, DEFAULT_ENABLED, DEFAULT_INTERVAL, handler);

    this.form = container.$form();

    // Set default property values.
    Heading(0);  // Default initial heading
    Enabled(DEFAULT_ENABLED);
    Interval(DEFAULT_INTERVAL);
    Speed(DEFAULT_SPEED);
    Visible(DEFAULT_VISIBLE);
    Z(DEFAULT_Z);

    container.$form().registerForOnDestroy(this);
  }

  /**
   * Creates a new Sprite component.  This is called by the constructors of
   * concrete subclasses, such as {@link Ball} and {@link ImageSprite}.
   *
   * @param container where the component will be placed
   */
  protected Sprite(ComponentContainer container) {
    // Note that although this is creating a new Handler, there is
    // only one UI thread in an Android app and posting to this
    // handler queues up a Runnable for execution on that thread.
    this(container, new Handler());
  }

  public void Initialize() {
    initialized = true;
    canvas.registerChange(this);
  }

  // Properties (Enabled, Heading, Interval, Speed, Visible, X, Y, Z)

  /**
   * Enabled property getter method.
   *
   * @return  {@code true} indicates a running timer, {@code false} a stopped
   *          timer
   */
  @SimpleProperty(
      description = "Controls whether the sprite moves when its speed is non-zero.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return timerInternal.Enabled();
  }

  /**
   * Enabled property setter method: starts or stops the timer.
   *
   * @param enabled  {@code true} starts the timer, {@code false} stops it
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = DEFAULT_ENABLED ? "True" : "False")
  @SimpleProperty
      public void Enabled(boolean enabled) {
    timerInternal.Enabled(enabled);
  }

  /**
   * Sets heading in which sprite should move.  In addition to changing the
   * local variables {@link #userHeading} and {@link #heading}, this
   * sets {@link #headingCos}, {@link #headingSin}, and {@link #headingRadians}.
   *
   * @param userHeading degrees above the positive x-axis
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = DEFAULT_HEADING + "")
  public void Heading(double userHeading) {
    this.userHeading = userHeading;
    // Flip, because y increases in the downward direction on Android canvases
    heading = -userHeading;
    headingRadians = Math.toRadians(heading);
    headingCos = Math.cos(headingRadians);
    headingSin = Math.sin(headingRadians);
    // changing the heading needs to force a redraw for image sprites that rotate
    registerChange();
  }

  /**
   * Returns the heading of the sprite.
   *
   * @return degrees above the positive x-axis
   */
  @SimpleProperty(
    description = "Returns the sprite's heading in degrees above the positive " +
    "x-axis.  Zero degrees is toward the right of the screen; 90 degrees is toward the " +
    "top of the screen.")
  public double Heading() {
    return userHeading;
  }

  /**
   * Interval property getter method.
   *
   * @return  timer interval in ms
   */
  @SimpleProperty(
      description = "The interval in milliseconds at which the sprite's " +
      "position is updated.  For example, if the interval is 50 and the speed is 10, " +
      "then the sprite will move 10 pixels every 50 milliseconds.",
      category = PropertyCategory.BEHAVIOR)
  public int Interval() {
    return timerInternal.Interval();
  }

  /**
   * Interval property setter method: sets the interval between timer events.
   *
   * @param interval  timer interval in ms
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_INTERVAL + "")
  @SimpleProperty
  public void Interval(int interval) {
    timerInternal.Interval(interval);
  }

  /**
   * Sets the speed with which this sprite should move.
   *
   * @param speed the magnitude (in pixels) to move every {@link #interval}
   * milliseconds
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = DEFAULT_SPEED + "")
  public void Speed(float speed) {
    this.speed = speed;
  }

  /**
   * Gets the speed with which this sprite moves.
   *
   * @return the magnitude (in pixels) the sprite moves every {@link #interval}
   *         milliseconds.
   */
  @SimpleProperty(
    description = "he speed at which the sprite moves.  The sprite moves " +
    "this many pixels every interval.")
  public float Speed() {
    return speed;
  }

  /**
   * Gets whether sprite is visible.
   *
   * @return  {@code true} if the sprite is visible, {@code false} otherwise
   */
  @SimpleProperty(
      description = "True if the sprite is visible.",
      category = PropertyCategory.APPEARANCE)
  public boolean Visible() {
    return visible;
  }

  /**
   * Sets whether sprite should be visible.
   *
   * @param visible  {@code true} if the sprite should be visible; {@code false}
   * otherwise.
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = DEFAULT_VISIBLE ? "True" : "False")
  @SimpleProperty
  public void Visible(boolean visible) {
    this.visible = visible;
    registerChange();
  }

  @SimpleProperty(
      description = "The horizontal coordinate of the left edge of the sprite, " +
      "increasing as the sprite moves to the right.")
  public double X() {
    return xLeft;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0.0")
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public void X(double x) {
    xLeft = x;
    registerChange();
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0.0")
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public void Y(double y) {
    yTop = y;
    registerChange();
  }

  @SimpleProperty(
      description = "The vertical coordinate of the top of the sprite, " +
      "increasing as the sprite moves down.")
  public double Y() {
    return yTop;
  }

  /**
   * Sets the layer of the sprite, indicating whether it will appear in
   * front of or behind other sprites.
   *
   * @param layer higher numbers indicate that this sprite should appear
   *        in front of ones with lower numbers; if values are equal for
   *        sprites, either can go in front of the other
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
                    defaultValue = DEFAULT_Z + "")
  public void Z(double layer) {
    this.zLayer = layer;
    canvas.changeSpriteLayer(this);  // Tell canvas about change
  }

  @SimpleProperty(
      description = "How the sprite should be layered relative to other sprits, " +
      "with higher-numbered layers in front of lower-numbered layers.")
  public double Z() {
    return zLayer;
  }

  // Methods for event handling: general purpose method postEvent() and
  // Simple events: CollidedWith, Dragged, EdgeReached, Touched, NoLongeCollidingWith,
  // Flung, TouchUp, and TouchDown.

  /**
   * Posts a dispatch for the specified event.  This guarantees that event
   * handlers run with serial semantics, e.g., appear atomic relative to
   * each other.
   *
   * This method is overridden in tests.
   *
   * @param sprite the instance on which the event takes place
   * @param eventName the name of the event
   * @param args the arguments to the event handler
   */
  protected void postEvent(final Sprite sprite,
                           final String eventName,
                           final Object... args) {
    androidUIHandler.post(new Runnable() {
        public void run() {
          EventDispatcher.dispatchEvent(sprite, eventName, args);
        }});
  }

  // TODO(halabelson): Fix collision detection for rotated sprites.
  /**
   * Handler for CollidedWith events, called when two sprites collide.
   * Note that checking for collisions with a rotated ImageSprite currently
   * checks against the sprite's unrotated position.  Therefore, collision
   * checking will be inaccurate for tall narrow or short wide sprites that are
   * rotated.
   *
   * @param other the other sprite in the collision
   */
  @SimpleEvent
  public void CollidedWith(Sprite other) {
    if (registeredCollisions.contains(other)) {
      Log.e(LOG_TAG, "Collision between sprites " + this + " and "
          + other + " re-registered");
      return;
    }
    registeredCollisions.add(other);
    postEvent(this, "CollidedWith", other);
  }

  /**
   * Handler for Dragged events.  On all calls, the starting coordinates
   * are where the screen was first touched, and the "current" coordinates
   * describe the endpoint of the current line segment.  On the first call
   * within a given drag, the "previous" coordinates are the same as the
   * starting coordinates; subsequently, they are the "current" coordinates
   * from the prior call.  Note that the Sprite won't actually move
   * anywhere in response to the Dragged event unless MoveTo is
   * specifically called.
   *
   * @param startX the starting x-coordinate
   * @param startY the starting y-coordinate
   * @param prevX the previous x-coordinate (possibly equal to startX)
   * @param prevY the previous y-coordinate (possibly equal to startY)
   * @param currentX the current x-coordinate
   * @param currentY the current y-coordinate
   */
  @SimpleEvent
  public void Dragged(float startX, float startY,
                      float prevX, float prevY,
                      float currentX, float currentY) {
    postEvent(this, "Dragged", startX, startY, prevX, prevY, currentX, currentY);
  }

  /**
   * Event handler called when the sprite reaches an edge of the screen.
   * If Bounce is then called with that edge, the sprite will appear to
   * bounce off of the edge it reached.
   */
  @SimpleEvent(
      description = "Event handler called when the sprite reaches an edge of the screen. " +
        "If Bounce is then called with that edge, the sprite will appear to " +
        "bounce off of the edge it reached.  Edge here is represented as an integer that " +
        "indicates one of eight directions north(1), northeast(2), east(3), southeast(4), " +
        "south (-1), southwest(-2), west(-3), and northwest(-4).")
  public void EdgeReached(int edge) {
    if (edge == Component.DIRECTION_NONE
        || edge < Component.DIRECTION_MIN
        || edge > Component.DIRECTION_MAX) {
      throw new IllegalArgumentException("Illegal argument " + edge +
          " to Sprite.EdgeReached()");
    }
    postEvent(this, "EdgeReached", edge);
  }

  /**
   * Handler for NoLongerCollidingWith events, called when a pair of sprites
   * cease colliding.  This also registers the removal of the collision to a
   * private variable {@link #registeredCollisions} so that
   * {@link #CollidedWith(Sprite)} and this event are only raised once per
   * beginning and ending of a collision.
   *
   * @param other the sprite formerly colliding with this sprite
   */
  @SimpleEvent(
      description = "Event indicating that a pair of sprites are no longer " +
      "colliding.")
  public void NoLongerCollidingWith(Sprite other) {
    if (!registeredCollisions.contains(other)) {
      Log.e(LOG_TAG, "Collision between sprites " + this + " and "
          + other + " removed but not present");
    }
    registeredCollisions.remove(other);
    postEvent(this, "NoLongerCollidingWith", other);
  }

  /**
   * When the user touches the sprite and then immediately lifts finger: provides
   * the (x,y) position of the touch, relative to the upper left of the canvas
   *
   * @param x  x-coordinate of touched point
   * @param y  y-coordinate of touched point
   */
  @SimpleEvent
  public void Touched(float x, float y) {
    postEvent(this, "Touched", x, y);
  }

  /**
   * When a fling gesture (quick swipe) is made on the sprite: provides
   * the (x,y) position of the start of the fling, relative to the upper
   * left of the canvas. Also provides the speed (pixels per millisecond) and heading
   * (0-360 degrees) of the fling, as well as the x velocity and y velocity
   * components of the fling's vector.
   *
   * @param x  x-coordinate of touched point
   * @param y  y-coordinate of touched point
   * * @param speed  the speed of the fling sqrt(xspeed^2 + yspeed^2)
   * @param heading  the heading of the fling
   * @param xvel  the speed in x-direction of the fling
   * @param yvel  the speed in y-direction of the fling

   */
  @SimpleEvent
  public void Flung(float x, float y, float speed, float heading, float xvel, float yvel) {
    postEvent(this, "Flung", x, y, speed, heading, xvel, yvel);
  }

  /**
   * When the user stops touching the sprite (lifts finger after a
   * TouchDown event): provides the (x,y) position of the touch, relative
   * to the upper left of the canvas
   *
   * @param x  x-coordinate of touched point
   * @param y  y-coordinate of touched point
   */
  @SimpleEvent
  public void TouchUp(float x, float y) {
    postEvent(this, "TouchUp", x, y);
  }

  /**
   * When the user begins touching the sprite (places finger on sprite and
   * leaves it there): provides the (x,y) position of the touch, relative
   * to the upper left of the canvas
   *
   * @param x  x-coordinate of touched point
   * @param y  y-coordinate of touched point
   */
  @SimpleEvent
  public void TouchDown(float x, float y) {
    postEvent(this, "TouchDown", x, y);
  }

  // Methods providing Simple functions:
  // Bounce, CollidingWith, MoveIntoBounds, MoveTo, PointTowards.

  /**
   * Makes this sprite bounce, as if off of a wall by changing the
   * {@link #heading} (unless the sprite is not traveling toward the specified
   * direction).  This also calls {@link #MoveIntoBounds()} in case the
   * sprite is out of bounds.
   *
   * @param edge the direction of the object (real or imaginary) to bounce off
   *             of; this should be one of
   *    {@link com.google.appinventor.components.runtime.Component#DIRECTION_NORTH},
   *    {@link com.google.appinventor.components.runtime.Component#DIRECTION_NORTHEAST},
   *    {@link com.google.appinventor.components.runtime.Component#DIRECTION_EAST},
   *    {@link com.google.appinventor.components.runtime.Component#DIRECTION_SOUTHEAST},
   *    {@link com.google.appinventor.components.runtime.Component#DIRECTION_SOUTH},
   *    {@link com.google.appinventor.components.runtime.Component#DIRECTION_SOUTHWEST},
   *    {@link com.google.appinventor.components.runtime.Component#DIRECTION_WEST}, or
   *    {@link com.google.appinventor.components.runtime.Component#DIRECTION_NORTHWEST}.
   */
  @SimpleFunction(description = "Makes this sprite bounce, as if off a wall.  " +
      "For normal bouncing, the edge argument should be the one returned by EdgeReached.")
  public void Bounce (int edge) {
    MoveIntoBounds();

    // Normalize heading to [0, 360)
    double normalizedAngle = userHeading % 360;
    // The following step is necessary because Java's modulus operation yields a
    // negative number if the dividend is negative and the divisor is positive.
    if (normalizedAngle < 0) {
      normalizedAngle += 360;
    }

    // Only transform heading if sprite was moving in that direction.
    // This avoids oscillations.
    if ((edge == Component.DIRECTION_EAST
         && (normalizedAngle < 90 || normalizedAngle > 270))
        || (edge == Component.DIRECTION_WEST
            && (normalizedAngle > 90 && normalizedAngle < 270))) {
      Heading(180 - normalizedAngle);
    } else if ((edge == Component.DIRECTION_NORTH
                && normalizedAngle > 0 && normalizedAngle < 180)
               || (edge == Component.DIRECTION_SOUTH && normalizedAngle > 180)) {
      Heading(360 - normalizedAngle);
    } else if ((edge == Component.DIRECTION_NORTHEAST
                && normalizedAngle > 0 && normalizedAngle < 90)
              || (edge == Component.DIRECTION_NORTHWEST
                  && normalizedAngle > 90 && normalizedAngle < 180)
              || (edge == Component.DIRECTION_SOUTHWEST
                  && normalizedAngle > 180 && normalizedAngle < 270)
              || (edge == Component.DIRECTION_SOUTHEAST && normalizedAngle > 270)) {
      Heading(180 + normalizedAngle);
    }
  }

  // This is primarily used to enforce raising only
  // one {@link #CollidedWith(Sprite)} event per collision but is also
  // made available to the Simple programmer.
  /**
   * Indicates whether a collision has been registered between this sprite
   * and the passed sprite.
   *
   * @param other the sprite to check for collision with this sprite
   * @return {@code true} if a collision event has been raised for the pair of
   *         sprites and they still are in collision, {@code false} otherwise.
   */
  @SimpleFunction
  public boolean CollidingWith(Sprite other) {
    return registeredCollisions.contains(other);
  }

  /**
   * Moves the sprite back in bounds if part of it extends out of bounds,
   * having no effect otherwise. If the sprite is too wide to fit on the
   * canvas, this aligns the left side of the sprite with the left side of the
   * canvas. If the sprite is too tall to fit on the canvas, this aligns the
   * top side of the sprite with the top side of the canvas.
   */
  @SimpleFunction
  public void MoveIntoBounds() {
    moveIntoBounds(canvas.Width(), canvas.Height());
  }

  /**
   * Moves sprite directly to specified point.
   *
   * @param x the x-coordinate
   * @param y the y-coordinate
   */
  @SimpleFunction(
    description = "Moves the sprite so that its left top corner is at " +
    "the specfied x and y coordinates.")
  public void MoveTo(double x, double y) {
    xLeft = x;
    yTop = y;
    registerChange();
  }

  /**
   * Turns this sprite to point towards a given other sprite.
   *
   * @param target the other sprite to point towards
   */
  @SimpleFunction(
    description = "Turns the sprite to point towards a designated " +
    "target sprite. The new heading will be parallel to the line joining " +
    "the centerpoints of the two sprites.")
  public void PointTowards(Sprite target) {
    Heading(-Math.toDegrees(Math.atan2(
        // we adjust for the fact that the sprites' X() and Y()
        // are not the center points.
        target.Y() - Y() + (target.Height() - Height()) / 2,
        target.X() - X() + (target.Width() - Width()) / 2)));
  }

  /**
   * Turns this sprite to point towards a given point.
   *
   * @param x parameter of the point to turn to
   * @param y parameter of the point to turn to
   */
  @SimpleFunction(
    description = "Turns the sprite to point towards the point " +
    "with coordinates as (x, y).")
  public void PointInDirection(double x, double y) {
    Heading(-Math.toDegrees(Math.atan2(
        // we adjust for the fact that the sprite's X() and Y()
        // is not the center point.
        y - Y() - Height() / 2,
        x - X() - Width() / 2)));
  }

  // Internal methods supporting move-related functionality

  /**
   * Responds to a move or change of this sprite by redrawing the
   * enclosing Canvas and checking for any consequences that need
   * handling.  Specifically, this (1) notifies the Canvas of a change
   * so it can detect any collisions, etc., and (2) raises the
   * {@link #EdgeReached(int)} event if the Sprite has reached the edge of the
   * Canvas.
   */
  protected void registerChange() {
    // This was added to fix bug 2262218, where Ball.CollidedWith() was called
    // before all components had been constructed.
    if (!initialized) {
      // During REPL, components are not initalized, but we still want to repaint the canvas.
      canvas.getView().invalidate();
      return;
    }
    int edge = hitEdge();
    if (edge != Component.DIRECTION_NONE) {
      EdgeReached(edge);
    }
    canvas.registerChange(this);
  }

  /**
   * Specifies which edge of the canvas has been hit by the Sprite, if
   * any, moving the sprite back in bounds.
   *
   * @return {@link Component#DIRECTION_NONE} if no edge has been hit, or a
   *         direction (e.g., {@link Component#DIRECTION_NORTHEAST}) if that
   *         edge of the canvas has been hit
   */
  protected int hitEdge() {
    if (!canvas.ready()) {
      return Component.DIRECTION_NONE;
    }

    return hitEdge(canvas.Width(), canvas.Height());
  }

  /**
   * Moves the sprite back in bounds if part of it extends out of bounds,
   * having no effect otherwise. If the sprite is too wide to fit on the
   * canvas, this aligns the left side of the sprite with the left side of the
   * canvas. If the sprite is too tall to fit on the canvas, this aligns the
   * top side of the sprite with the top side of the canvas.
   */
  @SimpleFunction
  protected final void moveIntoBounds(int canvasWidth, int canvasHeight) {
    boolean moved = false;

    // We set the xLeft and/or yTop fields directly, instead of calling X(123) and Y(123), to avoid
    // having multiple calls to registerChange.

    // Check if the sprite is too wide to fit on the canvas.
    if (Width() > canvasWidth) {
      // Sprite is too wide to fit. If it isn't already at the left edge, move it there.
      // It is important not to set moved to true if xLeft is already 0. Doing so can cause a stack
      // overflow.
      if (xLeft != 0) {
        xLeft = 0;
        moved = true;
      }
    } else if (overWestEdge()) {
      xLeft = 0;
      moved = true;
    } else if (overEastEdge(canvasWidth)) {
      xLeft = canvasWidth - Width();
      moved = true;
    }

    // Check if the sprite is too tall to fit on the canvas. We don't want to cause a stack
    // overflow by moving the sprite to the top edge and then to the bottom edge, repeatedly.
    if (Height() > canvasHeight) {
      // Sprite is too tall to fit. If it isn't already at the top edge, move it there.
      // It is important not to set moved to true if yTop is already 0. Doing so can cause a stack
      // overflow.
      if (yTop != 0) {
        yTop = 0;
        moved = true;
      }
    } else if (overNorthEdge()) {
      yTop = 0;
      moved = true;
    } else if (overSouthEdge(canvasHeight)) {
      yTop = canvasHeight - Height();
      moved = true;
    }

    // Then, call registerChange (just once!) if necessary.
    if (moved) {
      registerChange();
    }
  }

  /**
   * Updates the x- and y-coordinates based on the heading and speed.  The
   * caller is responsible for calling {@link #registerChange()}.
   */
  protected void updateCoordinates() {
    xLeft += speed * headingCos;
    yTop += speed * headingSin;
  }

  // Methods for determining collisions with other Sprites and the edge
  // of the Canvas.

  private final boolean overWestEdge() {
    return xLeft < 0;
  }

  private final boolean overEastEdge(int canvasWidth) {
    return xLeft + Width() > canvasWidth;
  }

  private final boolean overNorthEdge() {
    return yTop < 0;
  }

  private final boolean overSouthEdge(int canvasHeight) {
    return yTop + Height() > canvasHeight;
  }

  protected int hitEdge(int canvasWidth, int canvasHeight) {
    // Determine in which direction(s) we are out of bounds, if any.
    // Note that more than one boolean value can be true.  For example, if
    // the sprite is past the northwest boundary, north and west will be true.
    boolean west = overWestEdge();
    boolean north = overNorthEdge();
    boolean east = overEastEdge(canvasWidth);
    boolean south = overSouthEdge(canvasHeight);

    // If no edge was hit, return.
    if (!(north || south || east || west)) {
      return Component.DIRECTION_NONE;
    }

    // Move the sprite back into bounds.  Note that we don't just reverse the
    // last move, since that might have been multiple pixels, and we'd only need
    // to undo part of it.
    MoveIntoBounds();

    // Determine the appropriate return value.
    if (west) {
      if (north) {
        return Component.DIRECTION_NORTHWEST;
      } else if (south) {
        return Component.DIRECTION_SOUTHWEST;
      } else {
        return Component.DIRECTION_WEST;
      }
    }

    if (east) {
      if (north) {
        return Component.DIRECTION_NORTHEAST;
      } else if (south) {
        return Component.DIRECTION_SOUTHEAST;
      } else {
        return Component.DIRECTION_EAST;
      }
    }

    if (north) {
      return Component.DIRECTION_NORTH;
    }
    if (south) {
      return Component.DIRECTION_SOUTH;
    }

    throw new AssertionFailure("Unreachable code hit in Sprite.hitEdge()");
  }

  /**
   * Provides the bounding box for this sprite.  Modifying the returned value
   * does not affect the sprite.
   *
   * @param border the number of pixels outside the sprite to include in the
   *        bounding box
   * @return the bounding box for this sprite
   */
  public BoundingBox getBoundingBox(int border) {
    return new BoundingBox(X() - border, Y() - border,
        X() + Width() - 1 + border, Y() + Height() - 1 + border);
  }

  /**
   * Determines whether two sprites are in collision.  Note that we cannot
   * merely see whether the rectangular regions around each intersect, since
   * some types of sprite, such as BallSprite, are not rectangular.
   *
   * @param sprite1 one sprite
   * @param sprite2 another sprite
   * @return {@code true} if they are in collision, {@code false} otherwise
   */
  public static boolean colliding(Sprite sprite1, Sprite sprite2) {
    // If the bounding boxes don't intersect, there can be no collision.
    BoundingBox rect1 = sprite1.getBoundingBox(1);
    BoundingBox rect2 = sprite2.getBoundingBox(1);
    if (!rect1.intersectDestructively(rect2)) {
      return false;
    }

    // If we get here, rect1 has been mutated to hold the intersection of the
    // two bounding boxes.  Now check every point in the intersection to see if
    // both sprites contain that point.
    // TODO(user): Handling abutting sprites properly
    for (double x = rect1.getLeft(); x <= rect1.getRight(); x++) {
      for (double y = rect1.getTop(); y <= rect1.getBottom(); y++) {
        if (sprite1.containsPoint(x, y) && sprite2.containsPoint(x, y)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Determines whether this sprite intersects with the given rectangle.
   *
   * @param rect the rectangle
   * @return {@code true} if they intersect, {@code false} otherwise
   */
  public boolean intersectsWith(BoundingBox rect) {
    // If the bounding boxes don't intersect, there can be no intersection.
    BoundingBox rect1 = getBoundingBox(0);
    if (!rect1.intersectDestructively(rect)) {
      return false;
    }

    // If we get here, rect1 has been mutated to hold the intersection of the
    // two bounding boxes.  Now check every point in the intersection to see if
    // the sprite contains it.
    for (double x = rect1.getLeft(); x < rect1.getRight(); x++) {
      for (double y = rect1.getTop(); y < rect1.getBottom(); y++) {
        if (containsPoint(x, y)) {
            return true;
        }
      }
    }
    return false;
  }

  /**
   * Indicates whether the specified point is contained by this sprite.
   * Subclasses of Sprite that are not rectangular should override this method.
   *
   * @param qx the x-coordinate
   * @param qy the y-coordinate
   * @return whether (qx, qy) falls within this sprite
   */
  public boolean containsPoint(double qx, double qy) {
    return qx >= xLeft && qx < xLeft + Width() &&
        qy >= yTop && qy < yTop + Height();
  }

  // Convenience methods for dealing with hitting the screen edge and collisions

  // AlarmHandler implementation

  /**
   * Moves and redraws sprite, registering changes.
   */
  public void alarm() {
    // This check on initialized is currently redundant, since registerChange()
    // checks it too.
    if (initialized && speed != 0) {
      updateCoordinates();
      registerChange();
    }
  }

  // Component implementation

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return canvas.$form();
  }

  // OnDestroyListener implementation

  @Override
  public void onDestroy() {
    timerInternal.Enabled(false);
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    timerInternal.Enabled(false);
    canvas.removeSprite(this);
  }

  // Abstract methods that must be defined by subclasses

  /**
   * Draws the sprite on the given canvas
   *
   * @param canvas the canvas on which to draw
   */
  protected abstract void onDraw(android.graphics.Canvas canvas);
}
