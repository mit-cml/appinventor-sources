// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2022-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

#if DEBUG

private func printView(_ view: UIView, depth: Int = 0) {
  let viewType = type(of: view)
  let frame = view.frame
  let compressH = view.contentCompressionResistancePriority(for: .horizontal).rawValue
  let compressV = view.contentCompressionResistancePriority(for: .vertical).rawValue
  let hugH = view.contentHuggingPriority(for: .horizontal).rawValue
  let hugV = view.contentHuggingPriority(for: .vertical).rawValue
  let indent = String(repeating: "  ", count: depth)
  let description = "\(view.debugDescription)".replacingOccurrences(of: ": 0x[0-9a-fA-F]+;", with: "", options: .regularExpression)
    .replacingOccurrences(of: "_TtC14AIComponentKit[^ .]*", with: "HelperView", options: .regularExpression)
  print("\(indent)\(viewType)\(description)")
  print("\(indent)  f: \(frame) c: (\(compressH), \(compressV)) h: (\(hugH), \(hugV))")
  view.constraints.forEach { (constraint) in
    let description = "\(constraint)".replacingOccurrences(of: ":0x[0-9a-fA-F]+", with: "", options: .regularExpression)
      .replacingOccurrences(of: "_TtC14AIComponentKit[^ .]*", with: "HelperView", options: .regularExpression)
    print("\(indent)  \(description)")
  }
  view.subviews.forEach { (child) in
    printView(child, depth: depth + 1)
  }
}

extension Form {
  public func printViewHierarchy() {
    if let view = self.view {
      printView(view)
    }
  }

  public func printViewHierarchy(for component: ViewComponent) {
    printView(component.view)
  }

  public func getComponent(named name: String) -> Component? {
    let sym = SCMInterpreter.shared.makeSymbol(name)
    return environment[sym] as? Component
  }
}

public func printViewHierarchy() {
  if let form = ReplForm.getActiveForm() {
    form.printViewHierarchy()
  }
}

struct ViewProperty: OptionSet {
  let rawValue: Int

  static let frame = ViewProperty(rawValue: 1 << 0)
  static let intrinsicSize = ViewProperty(rawValue: 1 << 1)
  static let resistancePriorities = ViewProperty(rawValue: 1 << 2)
  static let huggingPriorities = ViewProperty(rawValue: 1 << 3)
  static let recursive = ViewProperty(rawValue: 1 << 30)

  static let all: ViewProperty = [.frame, .intrinsicSize, .resistancePriorities, .huggingPriorities]
}

func printProperties(_ properties: ViewProperty = .all, of view: UIView, depth: Int = 0) {
  let indent = String(repeating: " ", count: 2 * depth)
  var result = [String]()
  if properties.contains(.frame) {
    result.append("\(view.frame)")
  }
  if properties.contains(.intrinsicSize) {
    result.append("\(view.intrinsicContentSize)")
  }
  if properties.contains(.resistancePriorities) {
    let rH = view.contentCompressionResistancePriority(for: .horizontal)
    let rV = view.contentCompressionResistancePriority(for: .vertical)
    result.append("resist: (\(rH), \(rV))")
  }
  if properties.contains(.huggingPriorities) {
    let hH = view.contentHuggingPriority(for: .horizontal)
    let hV = view.contentHuggingPriority(for: .vertical)
    result.append("hugging: (\(hH), \(hV))")
  }
  print(indent + result.joined(separator: " "))
  if properties.contains(.recursive) {
    view.subviews.forEach { (child) in
      printProperties(properties, of: child, depth: depth + 1)
    }
  }
}

private func lookupClass(named name: String) -> AnyClass? {
  let data = name.data(using: .utf8)
  var clazz: AnyClass? = nil
  data?.withUnsafeBytes({ (bytes: UnsafeRawBufferPointer) -> Void in
    if let base = bytes.bindMemory(to: Int8.self).baseAddress {
      clazz = objc_getClass(base) as? AnyClass
    }
  })
  return clazz
}

private func printPathPropertiesInner(_ properties: ViewProperty, node: Any, edges: [String]) {
  guard !edges.isEmpty else {
    if let component = node as? ViewComponent {
      printProperties(properties, of: component.view)
    } else if let view = node as? UIView {
      printProperties(properties, of: view)
    }
    return
  }
  let edge = edges[0]
  if edge == "*" {
    if let container = node as? ComponentContainer {
      for child in container.getChildren() {
        printPathPropertiesInner(properties, node: child, edges: [String](edges[1...]))
      }
    } else if let view = node as? UIView {
      for child in view.subviews {
        printPathPropertiesInner(properties, node: child, edges: [String](edges[1...]))
      }
    }
  } else if let clazz = lookupClass(named: edge) {
    if let container = node as? ComponentContainer {
      for child in container.getChildren() {
        if type(of: child) == clazz {
          printPathPropertiesInner(properties, node: child, edges: [String](edges[1...]))
        }
      }
    } else if let view = node as? UIView {
      for child in view.subviews {
        if type(of: child) == clazz {
          printPathPropertiesInner(properties, node: child, edges: [String](edges[1...]))
        }
      }
    }
  }
}

private func printPathProperties(_ properties: ViewProperty, root: Component, pathParts: [String]) {
  guard !pathParts.isEmpty else {
    return
  }
  let part = pathParts[0]
  let edges = part.split("/")
  printPathPropertiesInner(properties, node: root, edges: edges)
}

func printPathProperties(_ properties: ViewProperty = .all, path: String) {
  let pathParts = path.split("//")
  printPathProperties(properties, root: ReplForm.getActiveForm()!, pathParts: pathParts)
}

#endif
