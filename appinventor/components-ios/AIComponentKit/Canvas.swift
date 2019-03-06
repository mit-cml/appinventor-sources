// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import UIKit
import UIKit.UIGestureRecognizerSubclass

fileprivate let kCanvasDefaultBackgroundColor = Color.white.rawValue
fileprivate let kCanvasDefaultPaintColor = Color.black.rawValue
fileprivate let kCanvasDefaultLineWidth: CGFloat = 2.0
fileprivate let kCanvasDefaultFontSize: Float = 14.0
fileprivate let FLING_INTERVAL: CGFloat = 1000
fileprivate let TAP_THRESHOLD = Float(15) // corresponds to 15 pixels
fileprivate let UNSET = CGFloat(-1)
fileprivate let FINGER_WIDTH = CGFloat(24) // corresponds to 24 pixels
fileprivate let FINGER_HEIGHT = CGFloat(24)
fileprivate let HALF_FINGER_WIDTH: CGFloat = FINGER_WIDTH / 2;
fileprivate let HALF_FINGER_HEIGHT: CGFloat = FINGER_HEIGHT / 2;

// MARK: Canvas class
public class Canvas: ViewComponent, AbstractMethodsForViewComponent, ComponentContainer, UIGestureRecognizerDelegate, LifecycleDelegate {
  fileprivate var _view: CanvasView
  fileprivate var _backgroundColor: Int32 = 0
  fileprivate var _backgroundColorInitialized = false
  fileprivate var _backgroundImage = ""
  fileprivate var _paintColor = Int32(bitPattern: kCanvasDefaultPaintColor)
  fileprivate var _lineWidth = kCanvasDefaultLineWidth
  fileprivate var _fontSize = kCanvasDefaultFontSize
  fileprivate var _textAlignment = convertFromCATextLayerAlignmentMode(CATextLayerAlignmentMode.center)
  fileprivate var _frame = CGRect(x: 0, y: 0, width: kCanvasPreferredWidth, height: kCanvasPreferredHeight)
  
  fileprivate var _flingStartX = CGFloat(0)
  fileprivate var _flingStartY = CGFloat(0)
  fileprivate var _dragStartX = CGFloat(0)
  fileprivate var _dragStartY = CGFloat(0)
  
  // Layers are split into four categories. There may be multiple layers in shapeLayers and textLayers.
  // There is always just one background image layer and one background color layer.
  fileprivate var _shapeLayers = [CALayer]()
  fileprivate var _textLayers = [CALayer]()
  fileprivate var _backgroundImageView = UIImageView(image: nil)

  // Old values are used to scale shapes when canvas size changes.
  fileprivate var _oldHeight = CGFloat(0)
  fileprivate var _oldWidth = CGFloat(0)
  
  fileprivate var _sprites = [Sprite]()

  fileprivate var _imageSize: CGSize? = nil
  fileprivate var _initialized = false

  public override init(_ parent: ComponentContainer) {
    _view = CanvasView()
    super.init(parent)
    super.setDelegate(self)

    _view.Canvas = self
  
    // set up gesture recognizers
    let tapGesture = UITapGestureRecognizer(target: self, action: #selector(onTap))
    let longTouchGesture = UILongPressGestureRecognizer(target: self, action: #selector(onLongTouch))
    let flingGesture = UIPanGestureRecognizer(target: self, action: #selector(onFling))
    let dragGesture = DragGestureRecognizer(target: self, action: #selector(onDrag))
    _view.addGestureRecognizer(tapGesture)
    _view.addGestureRecognizer(longTouchGesture)
    _view.addGestureRecognizer(flingGesture)
    _view.addGestureRecognizer(dragGesture)
    dragGesture.delegate = self
    
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.clipsToBounds = true
    parent.add(self)

    /**
     * constraints to force automatically sized width/height to match parent
     * These constraints will break when setting pixel-based width or height greater than view's frame
     */
    if let superView = _view.superview {
      _view.heightAnchor.constraint(lessThanOrEqualTo: superView.heightAnchor).isActive = true
      _view.widthAnchor.constraint(lessThanOrEqualTo: superView.widthAnchor).isActive = true
    }

    _view.addSubview(_backgroundImageView)
    _backgroundImageView.translatesAutoresizingMaskIntoConstraints = false

    _backgroundImageView.topAnchor.constraint(equalTo: _view.topAnchor).isActive = true
    _backgroundImageView.bottomAnchor.constraint(equalTo: _view.bottomAnchor).isActive = true
    _backgroundImageView.leftAnchor.constraint(equalTo: _view.leftAnchor).isActive = true
    _backgroundImageView.rightAnchor.constraint(equalTo: _view.rightAnchor).isActive = true

    BackgroundColor = Int32(bitPattern: kCanvasDefaultBackgroundColor)
    updateSize()
  }

  @objc open func Initialize() {
    _initialized = true
    updateSize()
    
    Height = Int32(kCanvasPreferredHeight)
    Width = Int32(kCanvasPreferredWidth)
    BackgroundColor = _backgroundColor
  }
  
  // Returns a UIImage filled with the current background color
  @objc func backgroundColorImage() -> UIImage? {
    let color = argbToColor(_backgroundColor)
    var width = Int(_view.bounds.width)
    var height = Int(_view.bounds.height)
    if width == 0 {
      width = kCanvasPreferredWidth
    }
    if height == 0 {
      height = kCanvasPreferredHeight
    }
    let centerX = CGFloat(width) / 2
    let centerY = CGFloat(height) / 2
    let rect = CGRect(x: 0, y: 0, width: centerX * 2, height: centerY * 2)
    UIGraphicsBeginImageContext(rect.size)
    color.setFill()
    UIRectFill(rect)
    let image = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return image
  }

  // MARK: Properties
  @objc open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(backgroundColor) {
      // Canvas is cleared whenever BackgroundColor is set.
      Clear()
      
      // The color 'none' is rendered as white
      let newColor = backgroundColor != Int32(bitPattern: Color.none.rawValue) ? backgroundColor : Int32(bitPattern: Color.white.rawValue)
      if newColor != _backgroundColor {
        _backgroundImageView.backgroundColor = argbToColor(newColor)
      }
    }
  }

  @objc open var BackgroundImage: String {
    get {
      return _backgroundImage
    }
    set(path) {
      // Canvas is cleared whenever BackgroundImage is set.
      Clear()
      
      if path != _backgroundImage {
        // There are two possibilities when the backgroud image is changed:
        // 1) the provided path is valid, so the background image is updated or
        // 2) the provided path is invalid, so the background color is shown
        if let image = AssetManager.shared.imageFromPath(path: path) {
          _backgroundImage = path
          _imageSize = image.size
          _backgroundImageView.image = image
        } else {
          _imageSize = nil
          _backgroundImage = ""
          _backgroundImageView.image = nil
        }
        updateSize()
      }
    }
  }

  fileprivate func updateSize() {
//    if _initialized {
      updateWidth()
      updateHeight()
//    }
  }

  fileprivate func updateWidth() {
    if _lastSetWidth == kLengthPreferred {
      _view.constrain(width: Int32(_imageSize?.width ?? CGFloat(kCanvasPreferredWidth)))
    }
  }

  fileprivate func updateHeight() {
    if _lastSetHeight == kLengthPreferred {
      _view.constrain(height: Int32(_imageSize?.height ?? CGFloat(kCanvasPreferredHeight)))
    }
  }

  override open var Width: Int32 {
    get {
      if super.Width < 0 && _view.Drawn {
        return Int32(_view.bounds.width)
      }
      return super.Width
    }
    set(width) {
      _oldWidth = _view.bounds.width
      if width == kLengthPreferred {
        _view.constrain(width: Int32(_imageSize?.width ?? CGFloat(kCanvasPreferredWidth)))
        _lastSetWidth = kLengthPreferred
      } else {
        setNestedViewWidth(nestedView: _view, width: width, shouldAddConstraints: true)
      }
    }
  }
  
  @objc func updateLayerWidth() {
    let newWidth = _view.bounds.width
    if newWidth >= 0 {
      _view.frame.size.width = CGFloat(newWidth)
    }

    // Adjust all the shapeLayers transforms on the x-axis
    let xScaleFactor = CGFloat(newWidth) / _oldWidth
    for s in _shapeLayers {
      transformLayerWidth(s, xScaleFactor)
    }
    
    // Adjust all the textLayers transforms and positions on the x-axis
    for s in _textLayers {
      s.position.x *= xScaleFactor
      transformLayerWidth(s, xScaleFactor)
    }
  }
    
  override open var Height: Int32 {
    get {
      if _lastSetHeight < 0 && _view.Drawn {
        return Int32(_view.bounds.height)
      }
      return super.Height
    }
    set(height) {
      _oldHeight = _view.bounds.height
      if height == kLengthPreferred {
        _view.constrain(height: Int32(_imageSize?.height ?? CGFloat(kCanvasPreferredHeight)))
        _lastSetHeight = kLengthPreferred
      } else {
        setNestedViewHeight(nestedView: _view, height: height, shouldAddConstraints: true)
      }
    }
  }

  @objc func updateLayerHeight() {
    let newHeight = _view.bounds.height
    if newHeight >= 0 {
      _view.frame.size.height = CGFloat(newHeight)
    }

    // Adjust all the shapeLayers transforms on the y-axis
    let yScaleFactor = CGFloat(newHeight) / _oldHeight
    for s in _shapeLayers {
      transformLayerHeight(s, yScaleFactor)
    }
    
    // Adjust all the textLayers transforms and positions on the y-axis
    for s in _textLayers {
      s.position.y *= yScaleFactor
      transformLayerHeight(s, yScaleFactor)
    }
  }
  
  @objc open var PaintColor: Int32 {
    get {
      return _paintColor
    }
    set(color) {
      _paintColor = color
    }
  }

  @objc open var FontSize: Float {
    get {
      return _fontSize
    }
    set(font) {
      _fontSize = font
    }
  }

  @objc open var LineWidth: Float {
    get {
      return Float(_lineWidth)
    }
    set(width) {
      _lineWidth = CGFloat(width)
    }
  }

  @objc open var TextAlignment: Int32 {
    get {
      switch _textAlignment {
        case convertFromCATextLayerAlignmentMode(CATextLayerAlignmentMode.right): // ending at the specified point
          return Alignment.opposite.rawValue
        case convertFromCATextLayerAlignmentMode(CATextLayerAlignmentMode.left): // starting at the specified point
          return Alignment.normal.rawValue
        default:
          return Alignment.center.rawValue
      }
    }
    set(alignment) {
      switch alignment {
        case Alignment.normal.rawValue:
          _textAlignment = convertFromCATextLayerAlignmentMode(CATextLayerAlignmentMode.left)
        case Alignment.opposite.rawValue:
          _textAlignment = convertFromCATextLayerAlignmentMode(CATextLayerAlignmentMode.right)
        default:
          _textAlignment = convertFromCATextLayerAlignmentMode(CATextLayerAlignmentMode.center)
      }
    }
  }

  override open var view: UIView {
    get {
      return _view
    }
  }
  
  @objc open var canvasView: CanvasView {
    get {
      return _view
    }
  }

  fileprivate func transformLayerWidth(_ s: CALayer, _ xScaleFactor: CGFloat) {
    s.transform.m11 *= xScaleFactor
    s.transform.m12 *= xScaleFactor
    s.transform.m13 *= xScaleFactor
    s.transform.m14 *= xScaleFactor
  }
  
  fileprivate func transformLayerHeight(_ s: CALayer, _ yScaleFactor: CGFloat) {
    s.transform.m21 *= yScaleFactor
    s.transform.m22 *= yScaleFactor
    s.transform.m23 *= yScaleFactor
    s.transform.m24 *= yScaleFactor
  }

  // MARK: Events
  // Returns bounding box within canvas borders centered at the start of the gesture.
  @objc func getGestureBoundingBox(_ x: CGFloat, _ y: CGFloat) -> CGRect {
    let startX = max(0, x - HALF_FINGER_WIDTH)
    let startY = max(0, y - HALF_FINGER_HEIGHT)
    let origin = CGPoint(x: startX, y: startY)
    let width = min(CGFloat(Width), startX + FINGER_WIDTH) - startX
    let height = min(CGFloat(Height), startY + FINGER_HEIGHT) - startY
    let size = CGSize(width: width, height: height)
    let rect = CGRect(origin: origin, size: size)
    return rect
  }
  
  // Allow drag and fling gestures to be recognized simultaneously
  public func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
    return (gestureRecognizer is DragGestureRecognizer && otherGestureRecognizer is UIPanGestureRecognizer) || (gestureRecognizer is UIPanGestureRecognizer && otherGestureRecognizer is DragGestureRecognizer)
  }
  
  @objc func onTap(gesture: UITapGestureRecognizer) {
    let x = gesture.location(in: _view).x
    let y = gesture.location(in: _view).y
    let rect = getGestureBoundingBox(x, y)
    var touchedAnySprite = false
    for sprite in _sprites {
      if sprite.Enabled && sprite.Visible && sprite.intersectsWith(rect) {
        sprite.Touched(Float(x), Float(y))
        touchedAnySprite = true
      }
    }
    Touched(Float(x), Float(y), touchedAnySprite)
  }

  @objc func onLongTouch(gesture: UILongPressGestureRecognizer) {
    let x = gesture.location(in: _view).x
    let y = gesture.location(in: _view).y
    let rect = getGestureBoundingBox(x, y)
    if gesture.state == UIGestureRecognizer.State.began {
      for sprite in _sprites {
        if sprite.Enabled && sprite.Visible && sprite.intersectsWith(rect) {
          sprite.TouchDown(Float(x), Float(y))
        }
      }
      TouchDown(Float(x), Float(y))
    } else if gesture.state == UIGestureRecognizer.State.ended {
      for sprite in _sprites {
        if sprite.Enabled && sprite.Visible && sprite.intersectsWith(rect) {
          sprite.TouchUp(Float(x), Float(y))
        }
      }
      TouchUp(Float(x), Float(y))
    }
  }
  
  @objc open func Touched(_ x: Float, _ y: Float, _ touchedAnySprite: Bool) {
    EventDispatcher.dispatchEvent(of: self, called: "Touched", arguments: x as NSNumber, y as NSNumber, touchedAnySprite as AnyObject)
  }

  @objc open func TouchDown(_ x: Float, _ y: Float) {
    EventDispatcher.dispatchEvent(of: self, called: "TouchDown", arguments: x as NSNumber, y as NSNumber)
  }

  @objc open func TouchUp(_ x: Float, _ y: Float) {
    EventDispatcher.dispatchEvent(of: self, called: "TouchUp", arguments: x as NSNumber, y as NSNumber)
  }

  // Fling and Gesture are simultaneously recognized.
  @objc open func onFling(gesture: UIPanGestureRecognizer) {
    var velocity = gesture.velocity(in: _view)
    velocity.x = velocity.x / FLING_INTERVAL
    velocity.y = velocity.y / FLING_INTERVAL
    switch gesture.state {
    case .began:
      // save starting position
      _flingStartX = gesture.location(in: _view).x
      _flingStartY = gesture.location(in: _view).y
    case .ended:
      let speed = pow(pow(velocity.x, 2) + pow(velocity.y, 2), 0.5)
      let heading = -atan2(velocity.y, velocity.x) / (2 * CGFloat.pi) * 360
      var spriteHandledFling = false
      let rect = getGestureBoundingBox(_flingStartX, _flingStartY)
      for sprite in _sprites {
        if sprite.Enabled && sprite.Visible && sprite.intersectsWith(rect) {
          sprite.Flung(Float(_flingStartX), Float(_flingStartY), Float(speed), Float(heading),
                       Float(velocity.x), Float(velocity.y))
          spriteHandledFling = true
        }
      }
      Flung(Float(_flingStartX), Float(_flingStartY), Float(speed), Float(heading),
            Float(velocity.x), Float(velocity.y), spriteHandledFling)
    default:
      break
    }
  }

  @objc open func Flung(_ flingStartX: Float, _ flingStartY: Float, _ speed: Float, _ heading: Float,
                  _ velocityX: Float, _ velocityY: Float, _ flungSprite: Bool) {
    EventDispatcher.dispatchEvent(of: self, called: "Flung", arguments: flingStartX as NSNumber,
                                  flingStartY as NSNumber, speed as NSNumber, heading as NSNumber,
                                  velocityX as NSNumber, velocityY as NSNumber,
                                  flungSprite as NSNumber)
  }

  @objc open func onDrag(gesture: DragGestureRecognizer) {
    var draggedAnySprite = false
    if gesture.state == .began || gesture.state == .changed {
      let viewWidth = _view.bounds.width
      let viewHeight = _view.bounds.height
      let gestureX = gesture.state == .began ? gesture.startX : gesture.currentX
      let gestureY = gesture.state == .began ? gesture.startY : gesture.currentY
      if gestureX <= viewWidth && gestureY <= viewHeight {
        let rect = getGestureBoundingBox(gestureX, gestureY)
        for sprite in _sprites {
          if sprite.Enabled && sprite.Visible && sprite.intersectsWith(rect) {
            draggedAnySprite = true
            OperationQueue.main.addOperation {
              sprite.Dragged(Float(gesture.startX), Float(gesture.startY), Float(max(0, gesture.prevX)), Float(max(0, gesture.prevY)), Float(max(0, gesture.currentX)), Float(max(0, gesture.currentY)))
            }
          }
        }
        Dragged(Float(gesture.startX), Float(gesture.startY), Float(max(0, gesture.prevX)), Float(max(0, gesture.prevY)), Float(max(0, gesture.currentX)), Float(max(0, gesture.currentY)), draggedAnySprite)
      }
    }
  }

  @objc open func Dragged(_ startX: Float, _ startY: Float, _ prevX: Float, _ prevY: Float,
                    _ currentX: Float, _ currentY: Float, _ draggedAnySprite: Bool) {
    EventDispatcher.dispatchEvent(of: self, called: "Dragged", arguments: startX as NSNumber,
                                  startY as NSNumber, prevX as NSNumber, prevY as NSNumber,
                                  currentX as NSNumber, currentY as NSNumber,
                                  draggedAnySprite as NSNumber)
  }
  
  //MARK: Container methods
  public var form: Form {
    get {
      return _container.form
    }
  }
  
  public func add(_ component: ViewComponent) {
    // unsupported
  }
  
  public func setChildWidth(of component: ViewComponent, width: Int32) {
    // unsupported
  }
  
  public func setChildHeight(of component: ViewComponent, height: Int32) {
    // unsupported
  }
  
  //MARK: LifeCycleDelegate methods
  @objc public func onResume() {
    for s in _sprites {
        if s.Enabled {
            s.restartTimer()
        }
    }
  }
  
  @objc public func onPause() {
    for s in _sprites {
        s.removeTimer()
    }
  }
  
  @objc public func onDelete() {
    for s in _sprites {
      s.removeTimer()
    }
  }
  
  @objc public func onDestroy() {
    for s in _sprites {
      s.removeTimer()
    }
  }
  
  @objc func addSprite(_ sprite: Sprite) {
    _sprites.append(sprite)
    _view.layer.addSublayer(sprite.DisplayLayer)
  }
  
  // Re-render the sprite imageLayer when change has been registered.
  @objc func registerChange(_ sprite: Sprite) {
    sprite.updateDisplayLayer()
    findSpriteCollisions(sprite)
  }
  
  fileprivate func findSpriteCollisions(_ movedSprite: Sprite) {
    for sprite in _sprites {
      if sprite != movedSprite {
        let bothSpritesActive = movedSprite.Visible && movedSprite.Enabled && sprite.Visible && sprite.Enabled
        // Check whether we already raised an event for their collision.
        if movedSprite.CollidingWith(sprite) {
          // If they no longer conflict, make modifications.
          if !bothSpritesActive || !Sprite.colliding(movedSprite, sprite) {
            movedSprite.NoLongerCollidingWith(sprite)
            sprite.NoLongerCollidingWith(movedSprite)
          }
        } else {
          // Check if they now conflict.
          if bothSpritesActive && Sprite.colliding(movedSprite, sprite) {
            movedSprite.CollidedWith(sprite)
            sprite.CollidedWith(movedSprite)
          }
        }
      }
    }
  }

  //MARK: Drawing methods
  fileprivate func isInCanvasBoundaries(_ x: CGFloat, _ y: CGFloat) -> Bool {
    return x >= 0 && x <= _view.frame.size.width && y >= 0 && y <= _view.frame.size.height
  }
  
  @objc open func Clear() {
    // background image and background color are not cleared
    for l in _shapeLayers {
      l.removeFromSuperlayer()
    }
    for l in _textLayers {
      l.removeFromSuperlayer()
    }
  }

  @objc open func DrawCircle(_ centerX: Float, _ centerY: Float, _ radius: Float, _ fill: Bool) {
    let finalX = CGFloat(centerX)
    let finalY = CGFloat(centerY)
    if !isInCanvasBoundaries(finalX, finalY) {
      return
    }
    let shapeLayer = CAShapeLayer()
    let point = UIBezierPath(arcCenter: CGPoint(x: finalX, y: finalY), radius: CGFloat(radius), startAngle: 0, endAngle:CGFloat(Double.pi * 2), clockwise: true)
    if fill {
      shapeLayer.fillColor = argbToColor(_paintColor).cgColor
    } else {
      let clearColor = Int32(bitPattern: kCanvasDefaultBackgroundColor)
      shapeLayer.fillColor = argbToColor(clearColor).cgColor
    }
    shapeLayer.lineWidth = _lineWidth
    shapeLayer.strokeColor = argbToColor(_paintColor).cgColor
    shapeLayer.path = point.cgPath
    _view.layer.addSublayer(shapeLayer)
    _shapeLayers.append(shapeLayer)
  }

  @objc open func DrawLine(_ x1: Float, _ y1: Float, _ x2: Float, _ y2: Float) {
    let finalX1 = CGFloat(x1); let finalY1 = CGFloat(y1)
    var finalX2 = CGFloat(x2); var finalY2 = CGFloat(y2)
    if !isInCanvasBoundaries(finalX1, finalY1) {
      return
    }
    if !isInCanvasBoundaries(finalX2, finalY2) {
      // Setting finalX2 and finalY2 to be within the canvas bounds (between 0 and the view's width/height).
      finalX2 = max(0, min(finalX2, _view.frame.size.width))
      finalY2 = max(0, min(finalY2, _view.frame.size.height))
    }
    let shapeLayer = CAShapeLayer()
    let line = UIBezierPath()
    line.move(to: CGPoint(x: finalX1, y: finalY1))
    line.addLine(to: CGPoint(x: finalX2, y: finalY2))
    line.close()
    shapeLayer.path = line.cgPath
    shapeLayer.strokeColor = argbToColor(_paintColor).cgColor
    shapeLayer.lineWidth = _lineWidth
    _view.layer.addSublayer(shapeLayer)
    _shapeLayers.append(shapeLayer)
  }
  
  @objc open func DrawPoint(_ x: Float, _ y: Float) {
    let finalX = CGFloat(x)
    let finalY = CGFloat(y)
    if !isInCanvasBoundaries(finalX, finalY) {
      return
    }
    let shapeLayer = CAShapeLayer()
    let point = UIBezierPath(arcCenter: CGPoint(x: finalX, y:finalY), radius: 1.0, startAngle: 0, endAngle:CGFloat(Float.pi * 2), clockwise: true)
    shapeLayer.fillColor = argbToColor(_paintColor).cgColor
    shapeLayer.lineWidth = _lineWidth
    shapeLayer.path = point.cgPath
    _view.layer.addSublayer(shapeLayer)
    _shapeLayers.append(shapeLayer)
  }

  fileprivate func makeTextLayer(text: String, x: Float, y: Float) -> CATextLayer {
    let textLayer = CATextLayer()
    textLayer.frame = _view.bounds
    textLayer.string = text
    textLayer.fontSize = CGFloat(_fontSize)
    textLayer.anchorPoint = CGPoint(x: 0, y: 0)
    switch _textAlignment {
    case convertFromCATextLayerAlignmentMode(CATextLayerAlignmentMode.right): // text layer ends at x,y
      textLayer.anchorPoint = CGPoint(x: 1, y:0)
    case convertFromCATextLayerAlignmentMode(CATextLayerAlignmentMode.left): // text layer starts at x,y
      textLayer.anchorPoint = CGPoint(x: 0, y:0)
    default: // text layer is centered at x,y
      textLayer.anchorPoint = CGPoint(x: 0.5, y: 0)
    }
    textLayer.position = CGPoint(x: CGFloat(x), y: CGFloat(y))
    textLayer.foregroundColor = argbToColor(_paintColor).cgColor
    textLayer.alignmentMode = convertToCATextLayerAlignmentMode(_textAlignment)
    return textLayer
  }
  
  @objc open func DrawText(_ text: String, _ x: Float, _ y: Float) {
    if isInCanvasBoundaries(CGFloat(x), CGFloat(y)) {
      let textLayer = makeTextLayer(text: text, x: x, y: y)
      _view.layer.addSublayer(textLayer)
      _textLayers.append(textLayer)
    }
  }

  @objc open func DrawTextAtAngle(_ text: String, _ x: Float, _ y: Float, _ angle: Float) {
    if isInCanvasBoundaries(CGFloat(x), CGFloat(y)) {
      let textLayer = makeTextLayer(text: text, x: x, y: y)

      // this is counterclockwise, same as Android.
      let radians = CGFloat(angle * Float.pi / 180)
      textLayer.transform = CATransform3DMakeRotation(-radians, 0, 0, 1.0)
      _view.layer.addSublayer(textLayer)
      _textLayers.append(textLayer)
    }
  }

  /**
   * Gets the color of the specified point. This includes the background
   * and any drawn points, lines, or circles but not sprites.
   */
  @objc open func GetBackgroundPixelColor(_ x: Int32, _ y: Int32) -> Int32 {
    var pixel: [CUnsignedChar] = [0, 0, 0, 0]

    let colorSpace = CGColorSpaceCreateDeviceRGB()
    let bitmapInfo = CGBitmapInfo(rawValue: CGImageAlphaInfo.premultipliedLast.rawValue)

    if let context = CGContext(data: &pixel, width: 1, height: 1, bitsPerComponent: 8, bytesPerRow: 4, space: colorSpace, bitmapInfo: bitmapInfo.rawValue) {
      context.translateBy(x: -CGFloat(x), y: -CGFloat(y))
      _view.layer.render(in: context)
    } else {
      return Int32(Color.none.rawValue)
    }

    let red: CGFloat   = CGFloat(pixel[0]) / 255.0
    let green: CGFloat = CGFloat(pixel[1]) / 255.0
    let blue: CGFloat  = CGFloat(pixel[2]) / 255.0
    let alpha: CGFloat = CGFloat(pixel[3]) / 255.0
    let color = UIColor(red:red, green: green, blue:blue, alpha:alpha)
    return colorToArgb(color)
  }

  /**
   * Gets the color of the specified point. This includes sprites.
   */
  @objc open func GetPixelColor(_ x: Int32, _ y: Int32) -> Int32 {
    if !isInCanvasBoundaries(CGFloat(x), CGFloat(y)) {
      return Int32(Color.none.rawValue)
    }
    for sprite in _sprites {
      if sprite.contains(CGPoint(x: CGFloat(x), y: CGFloat(y))) {
        if let imgSprite = sprite as? ImageSprite {
          let img = imgSprite.Image
          if let cgImg = img.cgImage {
            if let provider = cgImg.dataProvider {
              let pixelData = provider.data
              let data: UnsafePointer<UInt8> = CFDataGetBytePtr(pixelData)
              
              let xRatio = Double(img.size.width) / Double(sprite.Width)
              let yRatio = Double(img.size.height) / Double(sprite.Height)
              let newX = (Double(x) - imgSprite.X) * xRatio
              let newY = (Double(y) - imgSprite.Y) * yRatio
              
              let pixelInfo: Int = ((Int(img.size.width) * Int(newY)) + Int(newX)) * 4
              
              let r = CGFloat(data[pixelInfo]) / CGFloat(255.0)
              let g = CGFloat(data[pixelInfo+1]) / CGFloat(255.0)
              let b = CGFloat(data[pixelInfo+2]) / CGFloat(255.0)
              let a = CGFloat(data[pixelInfo+3]) / CGFloat(255.0)
              
              return colorToArgb(UIColor(red: r, green: g, blue: b, alpha: a))
            }
          }
        }
      }
    }
    return GetBackgroundPixelColor(x, y)
  }

  @objc open func SetBackgroundPixelColor(_ x: Float, _ y: Float, _ color: Int32) {
    let finalX = CGFloat(x); let finalY = CGFloat(y)
    if !isInCanvasBoundaries(finalX, finalY) {
      return
    }
    let shapeLayer = CAShapeLayer()
    let point = UIBezierPath(arcCenter: CGPoint(x: finalX,y: finalY), radius: 0.5, startAngle: 0, endAngle:CGFloat(Float.pi * 2), clockwise: true)
    shapeLayer.fillColor = argbToColor(color).cgColor
    shapeLayer.lineWidth = _lineWidth
    shapeLayer.path = point.cgPath
    _view.layer.addSublayer(shapeLayer)
    _shapeLayers.append(shapeLayer)
  }

  @objc open func Save() -> String {
    // get image data
    UIGraphicsBeginImageContextWithOptions(_view.bounds.size, true, 1)
    _view.drawHierarchy(in: _view.bounds, afterScreenUpdates: true)
    guard let image = UIGraphicsGetImageFromCurrentImageContext() else {
      _container.form.dispatchErrorOccurredEvent(self, "SaveAs", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code, ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.message)
      return ""
    }
    let data = image.pngData()

    // save data to fileURL
    do {
      let filePath = try FileUtil.getPictureFile("png")
      let fileURL = URL(fileURLWithPath: filePath)
      try data?.write(to: fileURL)
      UIGraphicsEndImageContext()
      return fileURL.absoluteString
    } catch {
      _container.form.dispatchErrorOccurredEvent(self, "SaveAs", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code, ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.message)
      return ""
    }
  }
    
  @objc open func SaveAs(_ fileName: String) -> String {
    UIGraphicsBeginImageContextWithOptions(_view.bounds.size, true, 0)
    _view.drawHierarchy(in: _view.bounds, afterScreenUpdates: true)
    var finalFileName = ""
    guard let image = UIGraphicsGetImageFromCurrentImageContext() else {
      _container.form.dispatchErrorOccurredEvent(self, "SaveAs", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code, ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.message)
      return ""
    }
    
    // Get image data in the correct format
    var data: Data?
    let lowercaseFileName = fileName.lowercased()
    if lowercaseFileName.hasSuffix(".jpg") || lowercaseFileName.hasSuffix(".jpeg") {
      data = image.jpegData(compressionQuality: 1.0)
      finalFileName = fileName
    } else if lowercaseFileName.hasSuffix(".png") {
      data = image.pngData()
      finalFileName = fileName
    } else if !lowercaseFileName.contains(".") {
      data = image.pngData()
      finalFileName = fileName + ".png"
    } else {
      _container.form.dispatchErrorOccurredEvent(self, "SaveAs", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code, ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.message)
      return ""
    }
    
    // Get finalFileName
    finalFileName = AssetManager.shared.pathForPublicAsset(finalFileName)
    if let encoded = finalFileName.addingPercentEncoding(withAllowedCharacters:NSCharacterSet.urlPathAllowed) {
      finalFileName = "file://" + encoded
    }
    
    // Save image data to finalImageURL
    do {
      if let finalImageURL = URL(string: finalFileName) {
        try data?.write(to: finalImageURL)
      }
    } catch {
      _container.form.dispatchErrorOccurredEvent(self, "SaveAs", ErrorMessage.ERROR_MEDIA_FILE_ERROR.code, ErrorMessage.ERROR_MEDIA_FILE_ERROR.message)
      return ""
    }
    UIGraphicsEndImageContext()
    
    return finalFileName
  }
}

// MARK: Custom Drag Gesture Recognizer

struct LocationSample {
  let location: CGPoint
  
  init(location: CGPoint) {
    self.location = location
  }
}

open class DragGestureRecognizer: UIGestureRecognizer {
  fileprivate var _startX = UNSET
  fileprivate var _startY = UNSET
  fileprivate var _prevX = UNSET
  fileprivate var _prevY = UNSET
  fileprivate var _currentX = UNSET
  fileprivate var _currentY = UNSET
  fileprivate var _touchedAnySprite = false
  fileprivate var _isDrag = false
  
  var samples = [LocationSample]()

  public override init(target:Any?, action:Selector?) {
    super.init(target: target, action: action)
  }

  @objc open var prevX: CGFloat {
    get {
      return _prevX
    }
  }

  @objc open var prevY: CGFloat {
    get {
      return _prevY
    }
  }

  @objc open var startX: CGFloat {
    get {
      return _startX
    }
  }

  @objc open var startY: CGFloat {
    get {
      return _startY
    }
  }
  
  @objc open var currentX: CGFloat {
    get {
      return _currentX
    }
  }
  
  @objc open var currentY: CGFloat {
    get {
      return _currentY
    }
  }

  override open func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent) {
    super.touchesBegan(touches, with: event)
    state = .possible
    if let touch = touches.first as UITouch? {
      addSample(for: touch)
      _startX = touch.location(in: self.view).x
      _startY = touch.location(in: self.view).y
      _prevX = _startX
      _prevY = _startY
      _currentX = _startX
      _currentY = _startY
      _isDrag = false
    }
  }

  override open func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
    if let touch = touches.first {
      self.addSample(for: touch)
      let x = touch.location(in: self.view).x
      let y = touch.location(in: self.view).y
      if !_isDrag && Float(abs(x - _startX)) < TAP_THRESHOLD && Float(abs(y - _startY)) < TAP_THRESHOLD {
        state = .possible
      } else {
        _isDrag = true
        if state == .began {
          state = .changed
        } else {
          state = .began
        }
        _prevX = _currentX
        _currentX = x
        _prevY = _currentY
        _currentY = y
      }
    }
  }

  override open func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
    if let firstTouch = touches.first {
      self.addSample(for: firstTouch)
      let x = firstTouch.location(in: self.view).x
      let y = firstTouch.location(in: self.view).y
      if !_isDrag && Float(abs(x - _startX)) < TAP_THRESHOLD && Float(abs(y - _startY)) < TAP_THRESHOLD {
        // this is a touch event
        state = .failed
      } else {
        _prevX = _currentX
        _currentX = x
        _prevY = _currentY
        _currentY = y
        state = .ended
      }
    }
  }

  override open func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
    self.samples.removeAll()
    state = .cancelled
  }
  
  override open func reset() {
    self.samples.removeAll()
  }
  
  @objc func addSample(for touch: UITouch) {
    let newSample = LocationSample(location: touch.location(in: self.view))
    self.samples.append(newSample)
  }
}

open class CanvasView: UIView {
  private var _drawn = false
  private weak var _canvas: Canvas?
  private var _oldSize: CGSize = .zero
  private var _heightConstraint: NSLayoutConstraint? = nil
  private var _widthConstraint: NSLayoutConstraint? = nil


  required public init?(coder aDecoder: NSCoder) {
    fatalError("This class does not support NSCoding")
  }
  
  override init(frame: CGRect) {
    super.init(frame: frame)
  }
  
  @objc open var Drawn: Bool {
    get {
      return _drawn
    }
  }
  
  @objc open var Canvas: Canvas? {
    get {
      return _canvas
    }
    set(c) {
      _canvas = c
    }
  }

  fileprivate func constrain(height: Int32) {
    if let constraint = _heightConstraint {
      self.removeConstraint(constraint)
    }
    _heightConstraint = heightAnchor.constraint(equalToConstant: CGFloat(height))
    _heightConstraint?.priority = UILayoutPriority.defaultHigh
    addConstraint(_heightConstraint!)
  }

  fileprivate func constrain(width: Int32) {
    if let constraint = _widthConstraint {
      self.removeConstraint(constraint)
    }
    _widthConstraint = widthAnchor.constraint(equalToConstant: CGFloat(width))
    _widthConstraint?.priority = UILayoutPriority.defaultHigh
    addConstraint(_widthConstraint!)
  }

  override open func draw(_ rect: CGRect) {
    super.draw(rect)
    _drawn = true
    if let c = _canvas {
      c.updateLayerHeight()
      c.updateLayerWidth()
      for sprite in c._sprites {
        sprite.updateWidth()
        sprite.updateHeight()
      }
    }
  }

  open override func layoutSubviews() {
    super.layoutSubviews()
    if let canvas = _canvas, _oldSize != .zero {
      if _oldSize != frame.size {
        let xScale = frame.width / _oldSize.width
        let yScale = frame.height / _oldSize.height

        for s in canvas._shapeLayers {
          canvas.transformLayerHeight(s, yScale)
          canvas.transformLayerWidth(s, xScale)
        }

        for s in canvas._textLayers {
          s.position.x *= xScale
          s.position.y *= yScale
          canvas.transformLayerHeight(s, yScale)
          canvas.transformLayerWidth(s, xScale)
        }
      }
    }
    if frame.size != .zero {
      _oldSize = frame.size
    }
  }
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertFromCATextLayerAlignmentMode(_ input: CATextLayerAlignmentMode) -> String {
	return input.rawValue
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertToCATextLayerAlignmentMode(_ input: String) -> CATextLayerAlignmentMode {
	return CATextLayerAlignmentMode(rawValue: input)
}
