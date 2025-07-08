// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// MIT License
// Test harness for node classes to verify variables and PoseFromPropertyPosition methods

import UIKit

// Protocol defining required node interface
protocol Node {
  var fromPropertyPosition: [Float] { get set }
  var anchor: AnyObject? { get set }
  var trackable: AnyObject? { get set }
  var objectModel: String { get set }
  var texture: String { get set }
  var scale: Float { get set }

  func PoseFromPropertyPosition() -> [Float]
  func PoseFromPropertyPosition(_ positionFromProperty: String)
}

// Base class implementing default behavior
class BaseNode: Node {
  var fromPropertyPosition: [Float] = [0.0, 0.0, 0.0]
  var anchor: AnyObject? = nil
  var trackable: AnyObject? = nil
  var objectModel: String = "Form.ASSETS_PREFIX + default_model"
  var texture: String = "Form.ASSETS_PREFIX + default_texture"
  var scale: Float = 1.0

  func PoseFromPropertyPosition() -> [Float] {
    return fromPropertyPosition
  }

  func PoseFromPropertyPosition(_ positionFromProperty: String) {
    let positionArray = positionFromProperty.split(separator: ",").map { Float($0.trimmingCharacters(in: .whitespaces)) ?? 0.0 }
    var position = [Float](repeating: 0.0, count: 3)
    for i in 0..<min(positionArray.count, 3) {
      position[i] = positionArray[i]
    }
    self.fromPropertyPosition = position
    // Stub: no rotation or anchor creation
    print("PoseFromPropertyPosition set to \(positionFromProperty)")
  }
}

// Stub node classes
class CapsuleNode: BaseNode {}
class CubeNode: BaseNode {}
class SphereNode: BaseNode {}
class ModelNode: BaseNode {}
class AnimatedNode: BaseNode {}
class PlaneNode: BaseNode {}
class PyramidNode: BaseNode {}
class WebViewerNode: BaseNode {}
class VideoNode: BaseNode {}

// Test harness view controller
class NodeTestHarnessViewController: UIViewController {

  override func viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = .white

    let nodeTypes: [(String, Node.Type)] = [
      ("CapsuleNode", CapsuleNode.self),
      ("CubeNode", CubeNode.self),
      ("SphereNode", SphereNode.self),
      ("ModelNode", ModelNode.self),
      ("AnimatedNode", AnimatedNode.self),
      ("PlaneNode", PlaneNode.self),
      ("PyramidNode", PyramidNode.self),
      ("WebViewerNode", WebViewerNode.self),
      ("VideoNode", VideoNode.self)
    ]

    let buttonHeight: CGFloat = 40
    let buttonSpacing: CGFloat = 10
    let startY: CGFloat = 100

    for (index, (name, nodeType)) in nodeTypes.enumerated() {
      let button = UIButton(type: .system)
      button.frame = CGRect(x: 20, y: startY + CGFloat(index) * (buttonHeight + buttonSpacing), width: view.frame.width - 40, height: buttonHeight)
      button.setTitle("Add \(name)", for: .normal)
      button.tag = index
      button.addTarget(self, action: #selector(addNodeButtonTapped(_:)), for: .touchUpInside)
      view.addSubview(button)
    }
  }

  @objc func addNodeButtonTapped(_ sender: UIButton) {
    let nodeTypes: [Node] = [
      CapsuleNode(),
      CubeNode(),
      SphereNode(),
      ModelNode(),
      AnimatedNode(),
      PlaneNode(),
      PyramidNode(),
      WebViewerNode(),
      VideoNode()
    ]

    let node = nodeTypes[sender.tag]
    print("Adding node of type: \(type(of: node))")

    // Test setting PoseFromPropertyPosition string
    node.PoseFromPropertyPosition("1.0, 2.0, 3.0")
    print("PoseFromPropertyPosition() returns: \(node.PoseFromPropertyPosition())")

    // Log other properties
    print("objectModel: \(node.objectModel)")
    print("texture: \(node.texture)")
    print("scale: \(node.scale)")
  }
}
