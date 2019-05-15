// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import ARKit
import SceneKit

@available(iOS 11.3, *)

/**
 * - in order for the node to load, dae files need to be converted (and potentially others)
 * - this script should be added to the server
 */
open class ModelNode: ARNodeBase, ARModel {
  private var _referenceNode: SCNReferenceNode?
  private var _addedNode: SCNNode? {
    didSet {
      if let node = _addedNode {
        DispatchQueue.main.async {
          self._container?.addNode(self)
        }
      }
    }
  }
  private var _nodeNames: [String] = []
  private var _modelString: String = ""
  private var _rootNodeName: String = ""
  private var _numberToUseForNode: String = "1"
  
  @objc init(_ container: ARNodeContainer) {
    super.init(modelNodeContainer: container)
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  @objc public var Model: String {
    get {
      return _modelString
    }
    set(model) {
      let path = AssetManager.shared.pathForExistingFileAsset(model)
      _modelString = model
      
      guard !path.isEmpty else { return }
      
      let url = URL(fileURLWithPath: path)
      
      do {
        _ = try SCNScene(url: url)
        _referenceNode = SCNReferenceNode(url: url)
        setupNode()
      } catch {
        /// Note: I have been able to load obj, scn, and dae models, thus far.
        /// These are also the only model types that I have tried.  We may want to
        /// create an error for incorrect model type loaded, once we have a list of
        /// all working/possible model types.
        _container?.form?.dispatchErrorOccurredEvent(self, "Model", ErrorMessage.ERROR_MODELNODE_COULD_NOT_LOAD.code)
        return
      }
    }
  }
  
  @objc public var BoundingBox: [[Float]] {
    get {
      guard let node = _addedNode else { return [[], []] }
      let min = [UnitHelper.metersToCentimeters(node.boundingBox.min.x), UnitHelper.metersToCentimeters(node.boundingBox.min.y), UnitHelper.metersToCentimeters(node.boundingBox.min.z)]
      let max = [UnitHelper.metersToCentimeters(node.boundingBox.max.x), UnitHelper.metersToCentimeters(node.boundingBox.max.y), UnitHelper.metersToCentimeters(node.boundingBox.max.z)]
      return [min, max]
    }
  }
  
  @objc public var RootNodeName: String {
    get {
      return _rootNodeName
    }
    set(name) {
      _rootNodeName = name
      
      setupNode()
    }
  }
  
  @objc open var NamesOfNodes: [String] {
    get {
      if _nodeNames.isEmpty {
        _nodeNames = NodeNames()
      }
      return _nodeNames
    }
  }
  
  // FillColor is not user accessible
  @objc open override var FillColor: Int32 {
    get {
      return 0
    }
    set(color) {}
  }
  
  // FillColorOpacity is not user accessible
  @objc open override var FillColorOpacity: Int32 {
    get {
      return 1
    }
    set(color) {}
  }
  
  // Texture is not user accessible
  @objc open override var Texture: String {
    get {
      return ""
    }
    set(path) {}
  }
  
  // TextureOpacity is not user accessible
  @objc open override var TextureOpacity: Int32 {
    get {
      return 1
    }
    set(opacity) {}
  }
  
  // ShowShadow is not user accessible
  @objc open override var ShowShadow: Bool {
    get {
      return false
    }
    set(showShadow) {}
  }
  
  private func setupNode() {
    if _addedNode != nil {
      DispatchQueue.main.async {
        self._container?.removeNode(self)
      }
    }
    
    if let node = _referenceNode {
      node.name = node.name ?? Name
      node.load()
      
      if !_rootNodeName.isEmpty, let childNode = node.childNode(withName: _rootNodeName, recursively: true) {
        _addedNode = childNode
      } else {
        _addedNode = node
      }
      
      if let node = _addedNode {
        self._node = node
        node.name = node.name ?? Name
      }
    }
  }
  
  open func NodeNotFound(_ name: String) {
    EventDispatcher.dispatchEvent(of: self, called: "NodeNotFound", arguments: name as NSString)
  }
  
  private func NodeNames() -> [String] {
    var names: [String] = []
    if let node = _addedNode {
      getNames(node: node, list: &names)
    }

    return names
  }
  
  private func getNames(node: SCNNode, list: inout [String]) {
    if let name = node.name {
      list.append(name)
    } else {
      let name = "\(Name)-\(_numberToUseForNode)"
      node.name = name
      list.append(name)
    }
    
    for child in node.childNodes {
      getNames(node: child, list: &list)
    }
  }
}


// MARK: FillColor Functions
@available(iOS 11.3, *)
extension ModelNode {
  @objc open func SetFillColorForNode(_ name: String, _ color: Int32, _ opacity: Int32, _ shouldColorChildNodes: Bool) {
    guard let node = _addedNode else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetFillColorForNode", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let colorNode = (node.name == name) ? node : node.childNode(withName: name, recursively: true) else {
      NodeNotFound(name)
      return
    }
    
    let floatOpacity = CGFloat(min(max(0, opacity), 100)) / 100.0
    let fillColor = argbToColor(color).withAlphaComponent(floatOpacity)
    
    if let geometry = colorNode.geometry {
      geometry.materials.first?.diffuse.contents = fillColor
    } else if !shouldColorChildNodes {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetFillColorForNode", ErrorMessage.ERROR_MODELNODE_CANNOT_COLOR_NODE.code, name)
      return
    }
    
    if shouldColorChildNodes {
      for childNode in node.childNodes {
        updateAllColors(node: childNode, color: fillColor)
      }
    }
  }
  
  @objc open func SetFillColorForAllNodes(_ color: Int32, _ opacity: Int32) {
    let floatOpacity = CGFloat(min(max(0, opacity), 100)) / 100.0
    let uiColor = argbToColor(color).withAlphaComponent(floatOpacity)
    guard let node = _addedNode else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetFillColorForAllNodes", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    updateAllColors(node: node, color: uiColor)
  }
  
  @objc open func RenameNode(_ oldName: String, _ newName: String) {
    guard let node = (_addedNode?.name == oldName) ? _addedNode : _addedNode?.childNode(withName: oldName, recursively: true) else {
      NodeNotFound(oldName)
      return
    }
    
    node.name = newName
    _nodeNames = NodeNames()
  }
  
  private func updateAllColors(node: SCNNode, color: UIColor) {
    if let geometry = node.geometry {
      geometry.materials.first?.diffuse.contents = color
      geometry.materials.first?.isDoubleSided = true
    }
    for childNode in node.childNodes {
      updateAllColors(node: childNode, color: color)
    }
  }
}


// MARK: Texture Functions
@available(iOS 11.3, *)
extension ModelNode {
  @objc open func SetTextureForNode(_ name: String, _ texture: String, _ opacity: Int32, _ shouldTexturizeChildNodes: Bool) {
    guard let node = _addedNode else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTextureForNode", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let textureImage = AssetManager.shared.imageFromPath(path: texture) else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTextureForNode", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code)
      return
    }
    
    guard let colorNode = (node.name == name) ? node : node.childNode(withName: name, recursively: true) else {
      NodeNotFound(name)
      return
    }
    
    let floatOpacity = CGFloat(min(max(0, opacity), 100)) / 100.0
    
    if let geometry = colorNode.geometry {
      geometry.materials.first?.diffuse.contents = textureImage
      geometry.materials.first?.transparency = floatOpacity
    } else if !shouldTexturizeChildNodes {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTextureForNode", ErrorMessage.ERROR_MODELNODE_CANNOT_TEXTURIZE_NODE.code, name)
      return
    }
    
    if shouldTexturizeChildNodes {
      for childNode in node.childNodes {
        updateAllTextures(node: childNode, texture: textureImage, opacity: floatOpacity)
      }
    }
  }
  
  @objc open func SetTextureForAllNodes(_ texture: String, _ opacity: Int32) {
    guard let node = _addedNode else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTextureForAllNodes", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    guard let textureImage = AssetManager.shared.imageFromPath(path: texture) else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTextureForAllNodes", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code)
      return
    }
    
    let floatOpacity = CGFloat(min(max(0, opacity), 100)) / 100.0
    updateAllTextures(node: node, texture: textureImage, opacity: floatOpacity)
  }
  
  private func updateAllTextures(node: SCNNode, texture: UIImage, opacity: CGFloat) {
    if let geometry = node.geometry {
      geometry.materials.first?.diffuse.contents = texture
      geometry.materials.first?.transparency = opacity
      geometry.materials.first?.isDoubleSided = true
    }
    for childNode in node.childNodes {
      updateAllTextures(node: childNode, texture: texture, opacity: opacity)
    }
  }
}

// MARK: ShowShadow Functions
@available(iOS 11.3, *)
extension ModelNode {
  @objc open func SetShowShadowForNode(_ name: String, _ showShadow: Bool, _ shouldShadowChildNodes: Bool) {
    guard let node = _addedNode else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetShowShadowForNode", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let shadowNode = (node.name == name) ? node : node.childNode(withName: name, recursively: true) else {
      NodeNotFound(name)
      return
    }
    
    shadowNode.castsShadow = showShadow
    
    if shouldShadowChildNodes {
      for childNode in node.childNodes {
        updateAllShadows(node: childNode, showShadow: showShadow)
      }
    }
  }
  
  @objc open func SetShowShadowForAllNodes(_ showShadow: Bool) {
    guard let node = _addedNode else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetShowShadowForAllNodes", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    updateAllShadows(node: node, showShadow: showShadow)
  }
  
  private func updateAllShadows(node: SCNNode, showShadow: Bool) {
    node.castsShadow = showShadow
    for childNode in node.childNodes {
      updateAllShadows(node: childNode, showShadow: showShadow)
    }
  }
}

// MARK: Animation Functions
@available(iOS 11.3, *)
extension ModelNode {
  @objc open func PlayAnimationsForNode(_ name: String, _ shouldPlayChildNodes: Bool) {
    guard let node = _addedNode else {
      _container?.form?.dispatchErrorOccurredEvent(self, "PlayAnimationsForNode", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let animationNode = (node.name == name) ? node : node.childNode(withName: name, recursively: true) else {
      NodeNotFound(name)
      return
    }
    
    toggleAnimation(node: animationNode, toggleStop: false)
    
    if shouldPlayChildNodes {
      toggleAnimationsForAllChildren(node: animationNode, toggleStop: false)
    }
  }
  
  @objc open func PlayAnimationsForAllNodes() {
    guard let node = _addedNode else {
      _container?.form?.dispatchErrorOccurredEvent(self, "PlayAnimationsForAllNodes", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    toggleAnimation(node: node, toggleStop: false)
    
    for childNode in node.childNodes {
      toggleAnimationsForAllChildren(node: childNode, toggleStop: false)
    }
  }
  
  @objc open func StopAnimationsForNode(_ name: String, _ shouldStopChildNodes: Bool) {
    guard let node = _addedNode else {
      _container?.form?.dispatchErrorOccurredEvent(self, "StopAnimationsForNode", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let animationNode = (node.name == name) ? node : node.childNode(withName: name, recursively: true) else {
      NodeNotFound(name)
      return
    }
    
    toggleAnimation(node: animationNode)
    
    if shouldStopChildNodes {
      toggleAnimationsForAllChildren(node: animationNode)
    }
  }
  
  @objc open func StopAnimationsForAllNodes() {
    guard let node = _addedNode else {
      _container?.form?.dispatchErrorOccurredEvent(self, "StopAnimationsForAllNodes", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    toggleAnimation(node: node)
    
    toggleAnimationsForAllChildren(node: node)
  }
  
  private func toggleAnimationsForAllChildren(node: SCNNode, toggleStop: Bool = true) {
    for childNode in node.childNodes {
      toggleAnimation(node: childNode, toggleStop: toggleStop)
      toggleAnimationsForAllChildren(node: childNode, toggleStop: toggleStop)
    }
  }
  
  private func toggleAnimation(node: SCNNode, toggleStop: Bool = true) {
    for key in node.animationKeys where node.animationPlayer(forKey: key) != nil {
      let player = node.animationPlayer(forKey: key)
      toggleStop ? player?.stop() : player?.play()
    }
  }
}
