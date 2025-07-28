// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import ARKit
import RealityKit

// MARK: - Core Protocols

@available(iOS 14.0, *)
@objc public protocol ARNode: FollowsMarker, Component {
  var Name: String { get set }
  var NodeType: String { get }
  var Model: String { get set }
  var Visible: Bool { get set }
  var ShowShadow: Bool { get set }
  var Opacity: Int32 { get set }
  var FillColor: Int32 { get set }
  var FillColorOpacity: Int32 { get set }
  var Texture: String { get set }
  var TextureOpacity: Int32 { get set }
  var EnablePhysics: Bool { get set }
  var PinchToScale: Bool { get set }
  var PanToMove: Bool { get set }
  var RotateWithGesture: Bool { get set }
  var PoseFromPropertyPosition: String { get set }

  var XPosition: Float { get set }
  var YPosition: Float { get set }
  var ZPosition: Float { get set }
  var XRotation: Float { get set }
  var YRotation: Float { get set }
  var ZRotation: Float { get set }
  var Scale: Float { get set }
  // NOTE: uncomment if we want to allow nonuniform scaling
//  var XScale: Float { get }
//  var YScale: Float { get }
//  var ZScale: Float { get }

  /*
   * "By" methods act relative to the node's current properties (rotation, scale, etc)
   */
  func EnablePhysics(_ isDynamic: Bool)
  func ARNodeToYail() -> YailDictionary
  // RotateAboutBlankAxis https://gamedev.stackexchange.com/questions/116676/why-is-scnnode-rotation-property-a-four-dimensional-vector
  func RotateXBy(_ degrees: Float)
  func RotateYBy(_ degrees: Float)
  func RotateZBy(_ degrees: Float)
  func ScaleBy(_ scalar: Float)
  func MoveBy(_ x: Float, _ y: Float, _ z: Float)
  func MoveTo(_ x: Float, _ y: Float, _ z: Float)

  func Click()
  func LongClick()

  func getPosition() -> SIMD3<Float>
  func setPosition(x: Float, y: Float, z: Float)
  func scaleByPinch(scalar: Float)
  func moveByPan(x: Float, y: Float)
  func rotateByGesture(radians: Float)

  func DistanceToNode(_ node: ARNode) -> Float
  func DistanceToSpotlight(_ light: ARSpotlight) -> Float
  func DistanceToPointLight(_ light: ARPointLight) -> Float
  func DistanceToDetectedPlane(_ detectedPlane: ARDetectedPlane) -> Float
}
@available(iOS 14.0, *)
@objc public protocol FollowsMarker {
  var _followingMarker: ARImageMarker? { get }
  var IsFollowingImageMarker: Bool { get }

  func Follow(_ imageMarker: ARImageMarker)
  func FollowWithOffset(_ imageMarker: ARImageMarker, _ x: Float, _ y: Float, _ z: Float)
  func StopFollowingImageMarker()

  // MARK: Events
  func StoppedFollowingMarker()
}

// MARK: - Light Protocols

@objc public protocol ARLight: Component {
  var `Type`: String { get }
  var Color: Int32 { get set }
  var Temperature: Float { get set }
  var Intensity: Float { get set }
}

@available(iOS 14.0, *)
@objc public protocol HasPositionEffects {
  var XPosition: Float { get set }
  var YPosition: Float { get set }
  var ZPosition: Float { get set }
  
  func MoveBy(_ x: Float, _ y: Float, _ z: Float)
  func MoveTo(_ x: Float, _ y: Float, _ z: Float)
  func DistanceToNode(_ node: ARNode) -> Float
  func DistanceToSpotlight(_ light: ARSpotlight) -> Float
  func DistanceToPointLight(_ light: ARPointLight) -> Float
  func DistanceToDetectedPlane(_ detectedPlane: ARDetectedPlane) -> Float
  
  func getPosition() -> SIMD3<Float>
}

@objc public protocol HasDirectionEffects {
  var XRotation: Float { get set }
  var YRotation: Float { get set }
  var ZRotation: Float { get set }
  
  func RotateXBy(_ degrees: Float)
  func RotateYBy(_ degrees: Float)
  func RotateZBy(_ degrees: Float)
}

// Spotlights and Omnidirectional lights
@objc public protocol HasFalloff {
  var FalloffStartDistance: Float { get set }
  var FalloffEndDistance: Float { get set }
  var FalloffType: Int32 { get set }
}

// Spotlights and directional lights
@objc public protocol CastsShadows {
  var CastsShadows: Bool { get set }
  var ShadowColor: Int32 { get set }
  var ShadowOpacity: Int32 { get set }
}

public protocol ARAmbientLight: ARLight {

}

public protocol ARDirectionalLight: ARLight, HasDirectionEffects, CastsShadows {

}

@available(iOS 14.0, *)
@objc public protocol ARPointLight: ARLight, HasFalloff, HasPositionEffects {
  
}
@available(iOS 14.0, *)
@objc public protocol ARSpotlight: ARLight, HasFalloff, HasDirectionEffects, HasPositionEffects, CastsShadows {
  var SpotInnerAngle: Float { get set }
  var SpotOuterAngle: Float { get set }

  var MinimumDistanceForShadows: Float { get set }
  var MaximumDistanceForShadows: Float { get set }
}

// MARK: - Node Shape Protocols

public protocol HasCornerRadius {
  var CornerRadius: Float { get set }
}

@objc public protocol HasWidthInCentimeters {
  var WidthInCentimeters: Float { get set }
}

@objc public protocol HasHeightInCentimeters {
  var HeightInCentimeters: Float { get set }
}

@available(iOS 14.0, *)
public protocol ARBox: ARNode, HasCornerRadius, HasWidthInCentimeters, HasHeightInCentimeters {
  var LengthInCentimeters: Float { get set }
}

@available(iOS 14.0, *)
public protocol ARSphere: ARNode {
  var RadiusInCentimeters: Float { get set }
}

@available(iOS 14.0, *)
public protocol ARPlane: ARNode, HasCornerRadius, HasWidthInCentimeters, HasHeightInCentimeters {}

@available(iOS 14.0, *)
public protocol ARCylinder: ARNode, HasHeightInCentimeters {
  var RadiusInCentimeters: Float { get set }
}

@available(iOS 14.0, *)
public protocol ARCone: ARNode, HasHeightInCentimeters {
  var TopRadiusInCentimeters: Float { get set }
  var BottomRadiusInCentimeters: Float { get set }
}

@available(iOS 14.0, *)
public protocol ARCapsule: ARNode, HasHeightInCentimeters {
  var CapRadiusInCentimeters: Float { get set }
}

@available(iOS 14.0, *)
public protocol ARTube: ARNode, HasHeightInCentimeters {
  var OuterRadiusInCentimeters: Float { get set }
  var InnerRadiusInCentimeters: Float { get set }
}

@available(iOS 14.0, *)
public protocol ARTorus: ARNode {
  var RingRadiusInCentimeters: Float { get set }
  var PipeRadiusInCentimeters: Float { get set }
}

@available(iOS 14.0, *)
public protocol ARPyramid: ARNode, HasWidthInCentimeters, HasHeightInCentimeters {
  var LengthInCentimeters: Float { get set }
}

@available(iOS 14.0, *)
public protocol ARText: ARNode {
  var Text: String { get set }
  var FontSizeInCentimeters: Float { get set }
  var DepthInCentimeters: Float { get set }
}

@available(iOS 14.0, *)
public protocol ARVideo: ARNode, HasWidthInCentimeters, HasHeightInCentimeters {
  var Source: String { get set }
  var IsPlaying: Bool { get }
  var Volume: Int32 { get set }

  func Play()
  func Pause()
  func GetDuration() -> Int32
  func SeekTo(_ ms: Int32)
  func Completed()
}

@available(iOS 14.0, *)
public protocol ARWebView: ARNode, HasWidthInCentimeters, HasHeightInCentimeters {
  var HomeUrl: String { get set }
  var isUserInteractionEnabled: Bool { get set }

  func CanGoBack() -> Bool
  func CanGoForward() -> Bool
  func GoBack()
  func GoForward()
  func Reload()
  func GoToUrl(_ url: String)
  func GoHome()
}

@available(iOS 14.0, *)
public protocol ARModel: ARNode {
  var RootNodeName: String { get set }
  var BoundingBox: [[Float]] { get }
  var NamesOfNodes: [String] { get }

  func SetFillColorForNode(_ name: String, _ color: Int32, _ opacity: Int32, _ shouldColorChildNodes: Bool)
  func SetFillColorForAllNodes(_ color: Int32, _ opacity: Int32)
  func SetTextureForNode(_ name: String, _ texture: String, _ opacity: Int32, _ shouldTexturizeChildNodes: Bool)
  func SetTextureForAllNodes(_ texture: String, _ opacity: Int32)
  func SetShowShadowForNode(_ name: String, _ showShadow: Bool, _ shouldShadowChildNodes: Bool)
  func SetShowShadowForAllNodes(_ showShadow: Bool)
  func PlayAnimationsForNode(_ name: String, _ shouldPlayChildNodes: Bool)
  func PlayAnimationsForAllNodes()
  func StopAnimationsForNode(_ name: String, _ shouldStopChildNodes: Bool)
  func StopAnimationsForAllNodes()
  func RenameNode(_ oldName: String, _ newName: String)
  
  func NodeNotFound(_ name: String)
}

// MARK: - Detected Objects

@objc public protocol ARDetectedPlane: Component, HasWidthInCentimeters, HasHeightInCentimeters {
  var FillColor: Int32 { get set }
  var FillColorOpacity: Int32 { get set }
  var Texture: String { get set }
  var TextureOpacity: Int32 { get set }
  var Opacity: Int32 { get set }
  var IsHorizontal: Bool { get }

  func getPosition() -> SIMD3<Float>
  func updateFor(anchor: ARPlaneAnchor)
  func removed()
}

@available(iOS 14.0, *)
@objc public protocol ARImageMarker: Component {
  var Name: String { get }
  var Image: String { get set }
  var PhysicalWidthInCentimeters: Float { get set }
  var PhysicalHeightInCentimeters: Float { get }
  var _attachedNodes: [ARNodeBase] { get }
  var _referenceImage: ARReferenceImage? { get }
  var _isTracking: Bool { get set }

  func attach(_ node: ARNodeBase)
  func removeNode(_ node: ARNodeBase)
  func removeAllNodes()
  func pushUpdate(_ position: SIMD3<Float>, _ angles: SIMD3<Float>)

  // MARK: Events
  func FirstDetected(_ anchor: AnyObject)
  func PositionChanged(_ x: Float, _ y: Float, _ z: Float)
  func RotationChanged(_ x: Float, _ y: Float, _ z: Float)
  func NoLongerInView()
  func AppearedInView()
  func Reset()
}

// MARK: - Container Protocols

@available(iOS 14.0, *)
@objc public protocol ARNodeContainer: ComponentContainer {
  var Nodes: [ARNode] { get }
  var ShowWireframes: Bool { get set }
  var ShowBoundingBoxes: Bool { get set }

  func getARView() -> ARView3D
  func addNode(_ node: ARNodeBase)
  func removeNode(_ node: ARNodeBase)

  // Events
  func NodeClick(_ node: ARNode)
  func TapAtPoint(_ x: Float, _ y: Float, _ z: Float, _ isANodeAtPoint: Bool)
  func LongPressAtPoint(_ x: Float, _ y: Float, _ z: Float, _ isANodeAtPoint: Bool)
  func NodeLongClick(_ node: ARNode)
}

@objc public protocol ARDetectedPlaneContainer: ComponentContainer {
  var DetectedPlanes: [ARDetectedPlane] { get }

  func ClickOnDetectedPlaneAt(_ detectedPlane: ARDetectedPlane, _ x: Float, _ y: Float, _ z: Float, _ isANodeAtPoint: Bool)
  func LongClickOnDetectedPlaneAt(_ detectedPlane: ARDetectedPlane, _ x: Float, _ y: Float, _ z: Float, _ isANodeAtPoint:Bool)
  func PlaneDetected(_ detectedPlane: ARDetectedPlane)
  func DetectedPlaneUpdated(_ detectedPlane: ARDetectedPlane)
  func DetectedPlaneRemoved(_ detectedPlane: ARDetectedPlane)
}

@available(iOS 14.0, *)
@objc public protocol ARImageMarkerContainer: ComponentContainer {
  var ImageMarkers: [ARImageMarker] { get }

  func addMarker(_ marker: ImageMarker)
  func removeMarker(_ marker: ImageMarker)
  func updateMarker(_ marker: ImageMarker, for oldName: String, with newName: String) -> Bool
  func markerNameIsAvailable(_ name: String) -> Bool
}

@available(iOS 14.0, *)
@objc public protocol ARLightContainer: ComponentContainer {
  var Lights: [ARLight] { get }
  var LightingEstimation: Bool { get set }
  var ShowLightLocations: Bool { get set }
  var ShowLightAreas: Bool { get set }

  func HideAllLights()

  func addLight(_ light: ARLightBase)
  func removeLight(_ light: ARLightBase)
  
  func LightingEstimateUpdated(_ ambientIntensity: Float, _ ambientTemperature: Float)
}

// MARK: - Additional Protocols
@available(iOS 14.0, *)
@objc public protocol CanLook {
  func LookAtNode(_ node: ARNode)
  func LookAtDetectedPlane(_ detectedPlane: ARDetectedPlane)
  func LookAtSpotlight(_ light: ARSpotlight)
  func LookAtPointLight(_ light: ARPointLight)
  func LookAtPosition(_ x: Float, _ y: Float, _ z: Float)
}
// MARK: - Utility Classes

public class UnitHelper {
  public static func metersToCentimeters(_ meters: Float) -> Float {
    return meters * 100
  }
  
  @objc public static func centimetersToMeters(_ centimeters: Float) -> Float {
    return centimeters / 100
  }
  
  public static func metersToCentimeters(_ meters: CGFloat) -> Float {
    return metersToCentimeters(Float(meters))
  }
  
  public static func centimetersToMeters(_ centimeters: Float) -> CGFloat {
    return CGFloat(centimeters / 100)
  }
}
