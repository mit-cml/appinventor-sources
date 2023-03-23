// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/**
 * A LinkedList protocol.  A protocol for generic LinkedLists.
 */
public protocol LinkedList: Sequence {
  associatedtype NodeValueType: Equatable
  typealias Node = LinkedListNode<NodeValueType>
  
  var head: LinkedListNode<NodeValueType>? { get }
  var maxSize: Int? { get }
  var isEmpty: Bool { get }
  var count: Int { get }
  
  func node(at index: Int) -> Node?
  mutating func push(_ node: Node)
  mutating func push(_ value: NodeValueType)
  mutating func insertAfterNode(_ prevNode: Node, _ newNode: Node)
  mutating func insertAfterNode(_ prevNode: Node, _ value: NodeValueType)
  mutating func append(_ node: Node)
  mutating func append(_ value: NodeValueType)
  mutating func removeAll()
}

/**
 * A SinglyLinkedList struct.  A singly-linked list of generic, equatable type.
 */
public struct SinglyLinkedList<T: Equatable>: LinkedList {
  public typealias Element = LinkedListNode<T>
  public typealias Node = LinkedListNode<T>
  public typealias NodeValueType = T
  
  fileprivate var _head: Node?
  fileprivate var _maxSize: Int?
  
  init(_ head: Node?, _ maxSize: Int?) {
    _head = head
    _maxSize = maxSize
  }
  
  init() {
    self.init(nil, nil)
  }
  
  public var head: Node? {
    return _head
  }
  
  public var maxSize: Int? {
    return _maxSize
  }
  
  public var isEmpty: Bool {
    return _head == nil
  }
  
  public var count: Int {
    guard var node = _head else {
      return 0
    }
    
    var count = 1
    while let next = node.next {
      node = next
      count += 1
    }
    return count
  }
  
  public func node(at index: Int) -> Node? {
    if index >= count || index < 0 || _head == nil {
      return nil
    }
    
    var node = head!.next
    for _ in 1..<index {
      guard let nextNode = node?.next else {
        return nil
      }
      node = nextNode
    }
    return node
  }
  
  public mutating func push(_ node: Node) {
    node.next = _head
    _head = node
    trimToMaxSize()
  }
  
  public mutating func push(_ value: T) {
    let node = Node(value: value)
    push(node)
  }
  
  public func insertAfterNode(_ prevNode: Node, _ newNode: Node) {
    newNode.next = prevNode.next
    prevNode.next = newNode
    trimToMaxSize()
  }
  
  public func insertAfterNode(_ prevNode: Node, _ value: T) {
    let node = Node(value: value)
    insertAfterNode(prevNode, node)
  }
  
  public mutating func append(_ node: Node) {
    guard var last = _head else {
      _head = node
      return
    }
    
    if let _maxSize = _maxSize, count == _maxSize {
      return
    }
    
    while last.next != nil {
      last = last.next!
    }
    last.next = node
    
    trimToMaxSize()
  }
  
  public mutating func append(_ value: T) {
    let node = Node(value: value)
    append(node)
  }
  
  public mutating func removeAll() {
    _head = nil
  }
  
  private func trimToMaxSize() {
    guard let _maxSize = _maxSize, count > _maxSize else {
      return
    }
    let tail = node(at: _maxSize - 1)
    tail?.next = nil
  }
}

extension SinglyLinkedList: Sequence {
  public func makeIterator() -> LinkedListIterator<T> {
    return LinkedListIterator(head: self.head)
  }
}

/**
 * A LinkedListIterator.  An iterator for linked lists to comply with
 * the IteratorProtocol.
 */
public struct LinkedListIterator<NodeValueType: Equatable>: IteratorProtocol {
  public typealias Node = LinkedListNode<NodeValueType>
  public typealias Element = Node
  
  private(set) var head: Node?
  
  public mutating func next() -> Node? {
    let result = self.head
    self.head = result?.next
    return result
  }
}

/**
 * A LinkedListNode class.  A node representation for linked list.
 */
public class LinkedListNode<T: Equatable> {
  var value: T
  var next: LinkedListNode<T>?
  
  init(value: T) {
    self.value = value
  }
}
