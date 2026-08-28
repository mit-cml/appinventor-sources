// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import CoreData
import SQLite

open class TinyDB: NonvisibleComponent {

  fileprivate var _database: Connection!
  fileprivate var _table = Table("TinyDB1")
  fileprivate let _key = SQLite.Expression<String>("_key")
  fileprivate let _value = SQLite.Expression<String>("_value")
  private var _namespace = "TinyDB1"

  public override init(_ parent: ComponentContainer) {
    let assetmgr = parent.form?.application?.assetManager
    let sqlitedb = assetmgr?.pathForPrivateAsset("TinyDb1.sqlite") ?? ""
    do {
      _database = try Connection(sqlitedb)
    } catch {
      NSLog("TinyDB: Unable to open database at \(sqlitedb): \(error.localizedDescription)")
    }
    super.init(parent)
    if let db = _database {
      do {
        try db.run("PRAGMA fullfsync = 1")
      } catch {
        NSLog("TinyDB: Unable to enable fullfsync: \(error.localizedDescription)")
      }
      do {
        try db.run(_table.create(ifNotExists: true) { t in
          t.column(_key, primaryKey: true)
          t.column(_value)
        })
      } catch {
        NSLog("TinyDB: Unable to create table \(_namespace): \(error.localizedDescription)")
      }
    }
  }

  /// MARK: TinyDB Properties

  @objc open var Namespace: String {
    get {
      return _namespace
    }
    set (namespace) {
      guard let db = _database else {
        NSLog("TinyDB: Database not initialized; cannot switch to namespace \(namespace)")
        return
      }
      let new_table = Table(namespace)
      do {
        try db.run(new_table.create(ifNotExists: true) { t in
          t.column(_key, primaryKey: true)
          t.column(_value)
        })
        _table = new_table
        _namespace = namespace
      } catch {
        NSLog("TinyDB: Unable to switch to namespace \(namespace): \(error.localizedDescription)")
      }
    }
  }

  /// MARK: TinyDB Methods

  @objc open func StoreValue(_ tag: String, _ valueToStore: AnyObject) {
    guard let db = _database else {
      NSLog("TinyDB: Database not initialized; cannot store value for tag \(tag)")
      return
    }
    do {
      let valueAsString = try getJsonRepresentation(valueToStore)
      _ = try db.run(_table.insert(or: .replace, _key <- tag, _value <- valueAsString))
    } catch {
      NSLog("TinyDB: Unable to write tag \(tag): \(error.localizedDescription)")
    }
  }

  @objc open func GetEntries() -> YailDictionary {
    let result = YailDictionary()
    guard let db = _database else {
      NSLog("TinyDB: Database not initialized; cannot read entries")
      return result
    }
    do {
      let statement = try db.run("SELECT _key, _value FROM \(_namespace)")
      for row in statement {
        guard let key = row[0] as? NSString else {
          continue
        }
        guard let valueAsString = row[1] as? String else {
          continue
        }
        if let value = try getObjectFromJson(valueAsString) {
          result[key] = value
        }
      }
    } catch {
      NSLog("TinyDB: Unable to read entries: \(error.localizedDescription)")
    }
    return result
  }

  @objc open func GetValue(_ tag: String, _ valueIfTagNotThere: AnyObject) -> AnyObject {
    guard let db = _database else {
      NSLog("TinyDB: Database not initialized; returning default for tag \(tag)")
      return valueIfTagNotThere
    }
    do {
      if let value = try db.pluck(_table.select(_value).filter(_key == tag)),
        let result = try getObjectFromJson(value[_value]) {
          return result
      }
    } catch {
      NSLog("TinyDB: Unable to read value for tag \(tag): \(error.localizedDescription)")
    }
    return valueIfTagNotThere
  }

  @objc open func GetTags() -> [String] {
    var result: [String] = []
    guard let db = _database else {
      NSLog("TinyDB: Database not initialized; cannot read tags")
      return result
    }
    do {
      for tag in try db.prepare(_table.select(_key)) {
        result.append(tag[_key])
      }
    } catch {
      NSLog("TinyDB: Unable to read tags: \(error.localizedDescription)")
    }
    return result
  }

  @objc open func ClearAll() {
    guard let db = _database else {
      NSLog("TinyDB: Database not initialized; cannot clear all tags")
      return
    }
    do {
      _ = try db.run(_table.delete())
    } catch {
      NSLog("TinyDB: Unable to clear all tags: \(error.localizedDescription)")
    }
  }

  @objc open func ClearTag(_ tag: String) {
    guard let db = _database else {
      NSLog("TinyDB: Database not initialized; cannot clear tag \(tag)")
      return
    }
    do {
      _ = try db.run(_table.filter(_key == tag).delete())
    } catch {
      NSLog("TinyDB: Unable to clear tag \(tag): \(error.localizedDescription)")
    }
  }
}

extension TinyDB: DataSource {
  func getDataValue(_ key: AnyObject?) -> [Any] {
    if let tag = key as? String {
      let result = GetValue(tag, YailList<AnyObject>())
      if result is YailList<AnyObject> {
        return result as! [Any]
      }
    }
    return []
  }
}
