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
  internal var _xLeft: CGFloat = 0
  internal var _yTop: CGFloat = 0
  fileprivate var _z = DEFAULT_Z
  fileprivate var _xCenter: CGFloat = 0
  fileprivate var _yCenter: CGFloat = 0
  fileprivate var _speed = DEFAULT_SPEED
  fileprivate var _timer: Timer?
  fileprivate var _initialized = false
  fileprivate var _registeredCollisions = Set<Sprite>()
  fileprivate var _originAtCenter: Bool = DEFAULT_ORIGIN_AT_CENTER
  var xOrigin = 0.0
  var yOrigin = 0.0
  var u = 0.0
  var v = 0.0
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
      _originAtCenter = originAtCenter
      if originAtCenter {
        u = 0.5
        v = 0.5
      } else {
        u = 0.0
        v = 0.0
      }
      _xLeft = xOriginToLeft(xOrigin: xOrigin)
      _yTop = yOriginToTop(yOrigin: yOrigin)
    }
  }

  open var U: Double {
    get {
      return u
    }
    set {
      u = newValue
      _xLeft = xOriginToLeft(xOrigin: xOrigin)
      registerChanges()
      updateDisplayLayer()
    }
  }

  open var V: Double {
    get {
      return v
    }
    set {
      v = newValue
      _yTop = yOriginToTop(yOrigin: yOrigin)
      registerChanges()
      updateDisplayLayer()
    }
  }

  open var XCenter: Double {
    return Double(_xCenter)
  }

  open var YCenter: Double {
    return Double(_yCenter)
  }

  public func xLeftToOrigin(xLeft: Double) -> Double {
    return xLeft + Double(Width) * u
  }

  public func xOriginToLeft(xOrigin: Double) -> Double {
    return xOrigin - Double(Width) * u
  }

  private func updateX(x: Double) {
    xOrigin = x
    _xLeft = xOriginToLeft(xOrigin: x)
  }

  /**
   * Distance to the left edge of the canvas.
   */
  @objc open var X: Double {
    get {
      return xOrigin
    }
    set(x) {
      updateX(x: x)
      registerChanges()
    }
  }

  public func yTopToOrigin(yTop: Double) -> Double {
    return yTop + Double(Height) * v
  }

  public func yOriginToTop(yOrigin: Double) -> Double {
    return yOrigin - Double(Height) * v
  }

  private func updateY(y: Double) {
    yOrigin = y
    _yTop = yOriginToTop(yOrigin: yOrigin)
  }

  /**
   * Distance from the top edge of canvas.
   */
  @objc open var Y: Double {
    get {
      return yOrigin
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
        xOrigin = xLeftToOrigin(xLeft: _xLeft)
        moved = true
      }
    } else if (overWestEdge()) {
      _xLeft = 0
      xOrigin = xLeftToOrigin(xLeft: _xLeft)
      moved = true
    } else if (overEastEdge(canvasWidth)) {
      _xLeft = CGFloat(canvasWidth - Width)
      xOrigin = xLeftToOrigin(xLeft: _xLeft)
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
        yOrigin = yTopToOrigin(yTop: _yTop)
        moved = true
      }
    } else if (overNorthEdge()) {
      _yTop = 0
      yOrigin = yTopToOrigin(yTop: _yTop)
      moved = true
    } else if (overSouthEdge(canvasHeight)) {
      _yTop = CGFloat(canvasHeight - Height)
      yOrigin = yTopToOrigin(yTop: _yTop)
      moved = true
    }

    if moved {
      // Originally registerChanges was called directly, but if the sprite is larger than the
      // canvas, this will result in a stack overflow as we keep trying to move the sprite in
      // bounds, which is impossible. We defer the change update so at least the app remains
      // responsive in case the sizes are being updated in an event handler.
      DispatchQueue.main.async {
        self.registerChanges()
      }
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
   * Moves the sprite to given point after checking whether the coordinates are numbers.
   */
  @objc open func MoveToPoint(_ coordinates: YailList<AnyObject>) {
    let xCoord = coerceToDouble(coordinates[1] as AnyObject)
    let yCoord = coerceToDouble(coordinates[2] as AnyObject)
    self.MoveTo(xCoord, yCoord)
  }

  /**
   * Helper function for MoveToPoint
   */
  private func coerceToDouble(_ coordinate: AnyObject ) -> Double {
    //unpack coordinate (check if are numbers and parse if string)
    if let number = coordinate as? NSNumber {
      return number.doubleValue
    } else if let number = Double(coordinate as? NSString as? String ?? "") {
      return number
    } else {
      return Double.nan
    }
  }

  /**
   * Turns the sprite to point towards the point with coordinates as (x, y).
   */
  @objc open func PointInDirection(_ x: Double, _ y: Double) {
    // we adjust for the fact that the sprite's X() and Y()
    // is not the center point.
    let yDiff = y - yOrigin
    let xDiff = x - xOrigin
    let radians = atan2(CGFloat(yDiff), CGFloat(xDiff))
    Heading = -radians / CGFloat.pi * 180
  }

  /**
   * Turns the sprite to point towards a designated target sprite.
   * The new heading will be parallel to the line joining the centerpoints of the two sprites.
   */
  @objc open func PointTowards(_ target: Sprite) {
    // we adjust for the fact that the sprites' X() and Y() are not the center points.
    let yDiff = target.yOrigin - yOrigin
    let xDiff = target.xOrigin - xOrigin
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
    let oldX = xOrigin
    let oldY = yOrigin
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

  var centerVector: Vector2D {
    return Vector2D(x: XCenter, y: YCenter)
  }

  @objc public static func colliding(_ sprite1: Sprite, _ sprite2: Sprite) -> Bool {
    if let ball1 = sprite1 as? Ball {
      if let ball2 = sprite2 as? Ball {
        return collidingBalls(ball1, ball2)
      } else if let imageSprite = sprite2 as? ImageSprite {
        return collidingBallAndImageSprite(ball1, imageSprite)
      }
    } else if let imageSprite1 = sprite1 as? ImageSprite {
      if let imageSprite2 = sprite2 as? ImageSprite {
        return collidingImageSprites(imageSprite1, imageSprite2)
      } else if let ball = sprite2 as? Ball {
        return collidingBallAndImageSprite(ball, imageSprite1)
      }
    }
    return false
  }

  private static func collidingBalls(_ ball1: Ball, _ ball2: Ball) -> Bool {
    let dx = ball1.XCenter - ball2.XCenter
    let dy = ball1.YCenter - ball2.YCenter
    let dist2 = dx * dx + dy * dy
    let maxdist = Double(ball1.Radius) + Double(ball2.Radius)
    return dist2 <= maxdist * maxdist
  }

  private static func collidingImageSprites(_ sprite1: ImageSprite, _ sprite2: ImageSprite) -> Bool {
    var axes = sprite1.normalAxes
    axes.append(contentsOf: sprite2.normalAxes)
    for a in axes {
      let minA = sprite1.getMinProjection(a)
      let maxA = sprite1.getMaxProjection(a)
      let minB = sprite2.getMinProjection(a)
      let maxB = sprite2.getMaxProjection(a)
      if maxA < minB || maxB < minA {
        return false
      }
    }
    return true
  }

  private static func collidingBallAndImageSprite(_ ball: Ball, _ imageSprite: ImageSprite) -> Bool {
    var axes = imageSprite.normalAxes
    let imageCorners = imageSprite.extremityVectors
    let ballCenter = ball.centerVector
    guard let closestCorner = ballCenter.closestVector(in: imageCorners) else {
      return false
    }
    let ballCenterToClosestCorner = closestCorner - ballCenter
    axes.append(ballCenterToClosestCorner)
    for a in axes {
      let minA = imageSprite.getMinProjection(a)
      let maxA = imageSprite.getMaxProjection(a)
      let minB = ball.getMinProjection(a)
      let maxB = ball.getMaxProjection(a)
      if maxA < minB || maxB < minA {
        return false
      }
    }
    return true
  }
}
