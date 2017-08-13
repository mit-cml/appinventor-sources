//
//  TinyDB.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/20/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import CoreData
import SQLite

open class TinyDB: NonvisibleComponent {

  fileprivate var _database: Connection!
  fileprivate let _table = Table("tinydb")
  fileprivate let _key = Expression<String>("_key")
  fileprivate let _value = Expression<String>("_value")

  public override init(_ parent: ComponentContainer) {
    let assetmgr = parent.form.application?.assetManager
    let sqlitedb = (assetmgr?.pathForPrivateAsset("TinyDb1.sqlite"))!
    do {
      _database = try Connection(sqlitedb)
    } catch {
      _database = nil
    }
    super.init(parent)
    do {
      try _database.run(_table.create(ifNotExists: true) { t in
        t.column(_key, primaryKey: true)
        t.column(_value)
      })
    } catch {
      NSLog("Unexpected error creating TinyDB")
    }
  }

  open func StoreValue(_ tag: String, _ valueToStore: AnyObject) {
    do {
      let valueAsString = try getJsonRepresentation(valueToStore)
      _ = try _database.run(_table.insert(or: .replace, _key <- tag, _value <- valueAsString))
    } catch {
      NSLog("Unable to write to TinyDB")
    }
  }

  open func GetValue(_ tag: String, _ valueIfTagNotThere: AnyObject) -> AnyObject {
    do {
      if let value = try _database.pluck(_table.select(_value).filter(_key == tag)) {
        if let result = try getObjectFromJson(value[_value]) {
          return result
        }
      }
    } catch {
      NSLog("Unable to read value from TinyDB")
    }
    return valueIfTagNotThere
  }

  open func GetTags() -> [String] {
    var result: [String] = []
    do {
      for tag in try _database.prepare(_table.select(_key)) {
        result.append(tag[_key])
      }
    } catch {
      NSLog("Unable to read tags from TinyDB")
    }
    return result
  }

  open func ClearAll() {
    do {
      _ = try _database.run(_table.delete())
    } catch {
      NSLog("Unable to clear all tags")
    }
  }

  open func ClearTag(_ tag: String) {
    do {
      _ = try _database.run(_table.filter(_key == tag).delete())
    } catch {
      NSLog("Unable to clear tag from TinyDB")
    }
  }
}
