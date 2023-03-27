// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit
import UIKit.UIGestureRecognizerSubclass

fileprivate let DEFAULT_ENABLED: Bool = true // Enable timer for movement
fileprivate let DEFAULT_HEADING: CGFloat = 0.0 // degrees
fileprivate let DEFAULT_INTERVAL: Int32 = 100 // ms
fileprivate let DEFAULT_SPEED: CGFloat = 0.0 // pixels per interval
fileprivate let DEFAULT_VISIBLE: Bool = true
fileprivate let DEFAULT_Z: Double = 1.0
fileprivate let DEFAULT_ORIGIN_AT_CENTER: Bool = false

open class Sprite: ViewComponent, UIGestureRecognizerDelegate {
  fileprivate let _view = UIView()
  fileprivate var _visible = DEFAULT_VISIBLE
  fileprivate var _interval = DEFAULT_INTERVAL
  fileprivate var _enabled = DEFAULT_ENABLED
  fileprivate var _heading = DEFAULT_HEADING
  fileprivate var _headingRadians = DEFAULT_HEADING
  fileprivate var _xLeft: CGFloat = 0
  fileprivate var _yTop: CGFloat = 0
  fileprivate var _z = DEFAULT_Z
  fileprivate var _xCenter: CGFloat = 0
  fileprivate var _yCenter: CGFloat = 0
  fileprivate var _speed = DEFAULT_SPEED
  fileprivate var _timer: Timer?
  fileprivate var _initialized = false
  fileprivate var _registeredCollisions = Set<Sprite>()
  fileprivate var _originAtCenter: Bool = DEFAULT_ORIGIN_AT_CENTER
  @objc var _canvas: Canvas

  // Layer displays either ball or image set by user.
  fileprivate var _displayLayer: CAShapeLayer

  public override init(_ parent: ComponentContainer) {
    if !(parent is Canvas) {
      fatalError("Canvas must be parent of ImageSprite")
    }
    _view.isUserInteractionEnabled = true
    _view.translatesAutoresizingMaskIntoConstraints = false
    _canvas = parent as! Canvas
    _displayLayer = CAShapeLayer()
    _displayLayer.zPosition = CGFloat(_z)
    
    super.init(parent)
    parent.add(self)
    _canvas.addSprite(self)
  }
  
  @objc open func Initialize() {
    guard !_initialized else {
      print("Sprite \(self) is already initialized")
      return
    }
    _initialized = true
    _canvas.registerChange(self)
    if Enabled {
      restartTimer()
    }
  }
  
  // MARK: Properties
  /**
   * Controls whether the sprite moves when its speed is non-zero.
   * Overridden by subclasses ImageSprite and Ball to enable animations.
   */
  @objc open var Enabled: Bool {
    get {
      return _enabled
    }
    set(enabled) {
      if enabled != _enabled {
        _enabled = enabled
        if enabled {
          restartTimer()
        } else {
          removeTimer()
        }
      }
    }
  }
  
  /**
   * The direction in which sprite should move.
   */
  @objc open var Heading: CGFloat {
    get {
      return -_heading
    }
    set(heading) {
      // Flip, because y increases in the downward direction
      _heading = -heading
      _headingRadians = _heading * CGFloat.pi / 180
    }
  }
  
  @objc public var HeadingRadians: CGFloat {
    get {
      return _headingRadians
    }
  }
  
  /**
   * The interval in milliseconds at which the sprite's
   * position is updated. For example, if the interval is
   * 50 and the speed is 10, the sprite will move 10 pixels
   * every 50 milliseconds.
   */
  @objc open var Interval: Int32 {
    get {
      return _interval
    }
    set(interval) {
      if interval != _interval {
        _interval = interval
        restartTimer()
      }
    }
  }
  
  /**
   * The speed is the magnitude in pixels moved every
   * interval.
   */
  @objc open var Speed: Float {
    get {
      return Float(_speed)
    }
    set(speed) {
      if _speed != CGFloat(speed) {
        _speed = CGFloat(speed)
      }
    }
  }
  
  /**
   * Whether the sprite is visible.
   */
  override open var Visible: Bool {
    get {
      return _visible
    }
    set(visible) {
      if visible != _visible {
        _visible = visible
        DisplayLayer.isHidden = !_visible
      }
    }
  }
  
  open var OriginAtCenter: Bool {
    get {
      return _originAtCenter
    }
    set(originAtCenter) {
      if originAtCenter != _originAtCenter {
        _originAtCenter = originAtCenter
      }
    }
  }
  
  open var XCenter: Double {
    return Double(_xCenter)
  }
  
  open var YCenter: Double {
    return Double(_yCenter)
  }
  
  private func xLeftToCenter(xLeft: Double) -> Double {
    return xLeft + Double(Width / 2)
  }

  private func xCenterToLeft(xCenter: Double) -> Double {
    return xCenter - Double(Width / 2)
  }
  
  private func updateX(x: Double) {
    if (_originAtCenter) {
      _xCenter = CGFloat(x)
      _xLeft = CGFloat(xCenterToLeft(xCenter: x))
    } else {
      _xLeft = CGFloat(x)
      _xCenter = CGFloat(xLeftToCenter(xLeft: x))
    }
  }
  
  /**
   * Distance to the left edge of the canvas.
   */
  @objc open var X: Double {
    get {
      return _originAtCenter ? Double(_xCenter) : Double(_xLeft)
    }
    set(x) {
      updateX(x: x)
      registerChanges()
    }
  }
  
  private func yTopToCenter(yTop: Double) -> Double {
    return yTop + Double(Height / 2)
  }

  private func yCenterToTop(yCenter: Double) -> Double {
    return yCenter - Double(Height / 2)
  }
  
  private func updateY(y: Double) {
    if (_originAtCenter) {
      _yCenter = CGFloat(y)
      _yTop = CGFloat(yCenterToTop(yCenter: y))
    } else {
      _yTop = CGFloat(y)
      _yCenter = CGFloat(yTopToCenter(yTop: y))
    }
  }
  /**
   * Distance from the top edge of canvas.
   */
  @objc open var Y: Double {
    get {
      return _originAtCenter ? Double(_yCenter) : Double(_yTop)
    }
    set(y) {
      updateY(y: y)
      registerChanges()
    }
  }
  
  /**
   * The layer of the sprite, indicating whether it will
   * appear in front of or behind other sprites.
   *
   * Higher-numbered layers appear in front of lower-numbered
   * ones. Layers of equal value can have either one in front.
   */
  @objc public var Z: Double {
    get {
      return _z
    }
    set(z) {
      if _z != z {
        _z = z
        DisplayLayer.zPosition = CGFloat(_z)
      }
    }
  }
  
  override open var view: UIView {
    get {
      return _view
    }
  }
  
  @objc open var DisplayLayer: CAShapeLayer {
    get {
      return _displayLayer
    }
    set(layer) {
      _displayLayer = layer
    }
  }

  // MARK: Events

  /** Handler called when two sprites collide. Note that checking for collisions
    * with a rotated ImageSprite currently checks against the sprite's unrotated
    * position.  Therefore, collision checking will be inaccurate for tall narrow
    * or short wide sprites that are rotated.
    */
  @objc open func CollidedWith(_ other: Sprite) {
    if _registeredCollisions.contains(other) {
      return
    }
    _registeredCollisions.insert(other)
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "CollidedWith", arguments: other as AnyObject)
    }
  }
  
  // Handler called when a pair of sprites cease colliding.
  @objc open func NoLongerCollidingWith(_ other: Sprite) {
    _registeredCollisions.remove(other)
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "NoLongerCollidingWith", arguments: other as AnyObject)
    }
  }
  
  @objc open func EdgeReached(_ edge: Direction) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "EdgeReached", arguments: edge.rawValue as NSNumber)
    }
  }
  
  //MARK: Methods
  /**
   * Makes this sprite bounce, as if off a wall.
   * For normal bouncing, the edge argument should be the one returned by EdgeReached.
   */
  @objc open func Bounce(_ edge: Int32) {
    let e = Direction(rawValue: edge)
    MoveIntoBounds()
    
    // Normalize heading to [0, 360)
    var normalizedAngle = -_heading.truncatingRemainder(dividingBy: 360)
    if (normalizedAngle < 0) {
      normalizedAngle += 360
    }

    // Only transform heading if sprite was moving in that direction.
    // This avoids oscillations.
    if ((e == Direction.east
      && (normalizedAngle < 90 || normalizedAngle > 270))
      || (e == Direction.west
        && (normalizedAngle > 90 && normalizedAngle < 270))) {
      Heading = 180 - normalizedAngle
    } else if ((e == Direction.north
      && normalizedAngle > 0 && normalizedAngle < 180)
      || (e == Direction.south && normalizedAngle > 180)) {
      Heading = 360 - normalizedAngle
    } else if ((e == Direction.northeast
      && normalizedAngle > 0 && normalizedAngle < 90)
      || (e == Direction.northwest
        && normalizedAngle > 90 && normalizedAngle < 180)
      || (e == Direction.southwest
        && normalizedAngle > 180 && normalizedAngle < 270)
      || (e == Direction.southeast && normalizedAngle > 270)) {
      Heading = 180 + normalizedAngle
    }
  }
  
  /**
   * Returns whether Sprite is currently colliding with other Sprite
   */
  @objc open func CollidingWith(_ other: Sprite) -> Bool {
    return _registeredCollisions.contains(other)
  }

  /**
   * Moves the sprite back in bounds if part of it extends out of bounds, having no effect otherwise.
   * If the sprite is too wide to fit on the canvas, this aligns the left side of the sprite with the
   * left side of the canvas. If the sprite is too tall to fit on the canvas, this aligns the top side
   * of the sprite with the top side of the canvas.
   */
  @objc open func MoveIntoBounds() {
    var moved = false
    let canvasWidth = Int32(_canvas.canvasView.bounds.width)
    let canvasHeight = Int32(_canvas.canvasView.bounds.height)
    
    // We set the xLeft and/or yTop fields directly, instead of calling X(123) and Y(123), to avoid
    // having multiple calls to registerChange.
    
    // Check if the sprite is too wide to fit on the canvas.
    if (Width > canvasWidth) {
      // Sprite is too wide to fit. If it isn't already at the left edge, move it there.
      // It is important not to set moved to true if xLeft is already 0. Doing so can cause a stack
      // overflow.
      if (_xLeft != 0) {
        _xLeft = 0
        moved = true
      }
    } else if (overWestEdge()) {
      _xLeft = 0
      moved = true
    } else if (overEastEdge(canvasWidth)) {
      _xLeft = CGFloat(canvasWidth - Width)
      moved = true
    }
    
    // Check if the sprite is too tall to fit on the canvas. We don't want to cause a stack
    // overflow by moving the sprite to the top edge and then to the bottom edge, repeatedly.
    if (Height > Int32(canvasHeight)) {
      // Sprite is too tall to fit. If it isn't already at the top edge, move it there.
      // It is important not to set moved to true if yTop is already 0. Doing so can cause a stack
      // overflow.
      if (_yTop != 0) {
        _yTop = 0
        moved = true
      }
    } else if (overNorthEdge()) {
      _yTop = 0
      moved = true
    } else if (overSouthEdge(canvasHeight)) {
      _yTop = CGFloat(canvasHeight - Height)
      moved = true
    }
    
    if moved {
      registerChanges()
    }
  }
  
  /**
   * Moves the sprite so that its left top corner is at the specfied x and y coordinates.
   */
  @objc open func MoveTo(_ x: Double, _ y: Double) {
    updateX(x: x)
    updateY(y: y)
    registerChanges()
  }
  
  /**
   * Turns the sprite to point towards the point with coordinates as (x, y).
   */
  @objc open func PointInDirection(_ x: Double, _ y: Double) {
    // we adjust for the fact that the sprite's X() and Y()
    // is not the center point.
    let yDiff = y - Y - Double(Height) / 2
    let xDiff = x - X - Double(Width) / 2
    let radians = atan2(CGFloat(yDiff), CGFloat(xDiff))
    Heading = -radians / CGFloat.pi * 180
  }
  
  /**
   * Turns the sprite to point towards a designated target sprite.
   * The new heading will be parallel to the line joining the centerpoints of the two sprites.
   */
  @objc open func PointTowards(_ target: Sprite) {
    // we adjust for the fact that the sprites' X() and Y() are not the center points.
    let yDiff = target.Y - Y + Double(target.Height - Height) / 2
    let xDiff = target.X - X - Double(target.Width - Width) / 2
    let radians = atan2(CGFloat(yDiff), CGFloat(xDiff))
    Heading = -radians / CGFloat.pi * 180
  }
  
  @objc open func Touched(_ x: Float, _ y: Float) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "Touched", arguments: x as NSNumber, y as NSNumber)
    }
  }

  @objc open func TouchDown(_ x: Float, _ y: Float) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "TouchDown", arguments: x as NSNumber, y as NSNumber)
    }
  }

  @objc open func TouchUp(_ x: Float, _ y: Float) {
    DispatchQueue.main.async{
      EventDispatcher.dispatchEvent(of: self, called: "TouchUp", arguments: x as NSNumber, y as NSNumber)
    }
  }

  @objc open func Flung(_ flingStartX: Float, _ flingStartY: Float, _ speed: Float, _ heading: Float,
                  _ velocityX: Float, _ velocityY: Float) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "Flung", arguments: flingStartX as NSNumber,
                                    flingStartY as NSNumber, speed as NSNumber, heading as NSNumber,
                                    velocityX as NSNumber, velocityY as NSNumber)
    }
  }
  
  @objc open func Dragged(_ startX: Float, _ startY: Float, _ prevX: Float, _ prevY: Float,
                    _ currentX: Float, _ currentY: Float) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "Dragged", arguments: startX as NSNumber,
                                    startY as NSNumber, prevX as NSNumber, prevY as NSNumber,
                                    currentX as NSNumber, currentY as NSNumber)
    }
  }

  // To be overriden in sub-classes (ImageSprite and Ball)
  @objc func updateDisplayLayer(){}
  
  // Notifies canvas of a sprite change and raises any EdgeReached events.
  @objc func registerChanges() {
    if _initialized {
      let edge = self.hitEdge()
      if edge != Direction.none {
        self.EdgeReached(edge)
      }
      self._canvas.registerChange(self)
    }
  }
  
  // Returns which edge the sprite overlaps with and moves the sprite into bounds if
  // it is out of bounds.
  fileprivate func hitEdge() -> Direction {
    if !_canvas.canvasView.Drawn {
      return Direction.none
    }
    
    let canvasWidth = Int32(_canvas.canvasView.bounds.width)
    let canvasHeight = Int32(_canvas.canvasView.bounds.height)
    
    let west = overWestEdge()
    let north = overNorthEdge()
    let east = overEastEdge(canvasWidth)
    let south = overSouthEdge(canvasHeight)
    
    if !(west || north || east || south) {
      return Direction.none
    }
    
    MoveIntoBounds()
    
    if (west) {
      if (north) {
        return Direction.northwest
      } else if (south) {
        return Direction.southwest
      } else {
        return Direction.west
      }
    }
    
    if (east) {
      if (north) {
        return Direction.northeast
      } else if (south) {
        return Direction.southeast
      } else {
        return Direction.east
      }
    }
    
    if (north) {
      return Direction.north
    }
    
    return Direction.south
  }
  
  fileprivate func overWestEdge() -> Bool {
    return X < 0
  }
  
  fileprivate func overNorthEdge() -> Bool {
    return Y < 0
  }
  
  fileprivate func overEastEdge(_ canvasWidth: Int32) -> Bool {
    return X + Double(Width) > Double(canvasWidth)
  }
  
  fileprivate func overSouthEdge(_ canvasHeight: Int32) -> Bool {
    return Y + Double(Height) > Double(canvasHeight)
  }

  @objc open func intersectsWith(_ rect: CGRect) -> Bool {
    return getBoundingBox(border: 0).intersects(rect)
  }
  
  // Returns bounding box of sprite
  @objc open func getBoundingBox(border: Int) -> CGRect {
    let start_x = CGFloat(X)
    let start_y = CGFloat(Y)
    return CGRect(origin: CGPoint(x: start_x, y: start_y), size: CGSize(width: CGFloat(Width), height: CGFloat(Height)))
  }
  
  @objc static public func colliding(_ sprite1: Sprite, _ sprite2: Sprite) -> Bool {
    let box1 = sprite1.getBoundingBox(border: 1)
    let box2 = sprite2.getBoundingBox(border: 1)
    let intersect = box1.intersection(box2)
    if intersect.isNull {
      return false
    }
    
    // If we get here, intersect has been mutated to hold the intersection of the
    // two bounding boxes. Now check every point in the intersection to see if
    // both sprites contain any of those points.
    for x in stride(from: intersect.minX, through: intersect.maxX, by: 1) {
      for y in stride(from: intersect.minY, through: intersect.maxY, by: 1) {
        if sprite1.contains(CGPoint(x: x, y: y)) && sprite2.contains(CGPoint(x: x, y: y)) {
          return true
        }
      }
    }
    return false
  }
  
  @objc func contains(_ point: CGPoint) -> Bool {
    return CGFloat(X) <= point.x && point.x <= CGFloat(X + Double(Width)) && CGFloat(Y) <= point.y && point.y <= CGFloat(Y + Double(Height))
  }
  
  @objc func restartTimer() {
    removeTimer()
    
    if Enabled {
      let timer = Timer.scheduledTimer(timeInterval: TimeInterval(Double(Interval) / 1000.0), target: self, selector: #selector(animate), userInfo: nil, repeats: true)
      RunLoop.main.add(timer, forMode: RunLoop.Mode.common)
      self._timer = timer
    }
  }
  
  @objc func removeTimer() {
    if let t = _timer {
      t.invalidate()
    }
    _timer = nil
  }
  
  @objc func updateWidth(){}
  @objc func updateHeight(){}
  
  @objc func animate() {
    let oldX = CGFloat(X)
    let oldY = CGFloat(Y)
    let headingCos = cos(_headingRadians)
    let headingSin = sin(_headingRadians)
    let newX = oldX + CGFloat(Speed) * headingCos
    let newY = oldY + CGFloat(Speed) * headingSin

    if oldX == newX && oldY == newY {
      // no changes
      return
    }

    // Update position.
    updateX(x: Double(newX))
    updateY(y: Double(newY))
    registerChanges()
  }
}

