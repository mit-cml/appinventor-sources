//
//  BarcodeScanner.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public protocol BarcodeScannerDelegate {
  func receivedResult(result: String)
  func canceled()
}

class BarcodeScannerViewController: UINavigationController, ZXCaptureDelegate {
  private var _capture: ZXCapture!
  private var _barcodeDelegate: BarcodeScannerDelegate!

  init() {
    self._capture = ZXCapture()
    super.init(navigationBarClass: nil, toolbarClass: nil)
    self.view.layer.addSublayer(self._capture.layer)
    let _cancel = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.cancel,
                                  target: self,
                                  action: #selector(BarcodeScannerViewController.cancel))
    _cancel.title = "Cancel"
    self.navigationItem.leftBarButtonItem = _cancel
    self.modalPresentationStyle = UIModalPresentationStyle.fullScreen
    self.modalTransitionStyle = UIModalTransitionStyle.coverVertical
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }

  var barcodeDelegate: BarcodeScannerDelegate {
    get {
      return _barcodeDelegate
    }
    set(delegate) {
      _barcodeDelegate = delegate
    }
  }

  public override func viewDidAppear(_ animated: Bool) {
    self._capture.focusMode = AVCaptureFocusMode.continuousAutoFocus
    self._capture.reader = ZXQRCodeReader()
    self._capture.camera = self._capture.back()
    self._capture.start()
  }

  public override func viewWillDisappear(_ animated: Bool) {
    self._capture.stop()
  }

  public func captureResult(_ capture: ZXCapture!, result: ZXResult!) {
    _barcodeDelegate.receivedResult(result: result.text)
  }

  func cancel() {
    self.dismiss(animated: true, completion: {})
    _barcodeDelegate.canceled()
  }
}

public class BarcodeScanner: NonvisibleComponent, BarcodeScannerDelegate {
  private let _container: ComponentContainer
  private var _result = ""
  private var _viewController: BarcodeScannerViewController

  public init(parent container: ComponentContainer) {
    self._container = container
    self._viewController = BarcodeScannerViewController()
    super.init(dispatcher: container)
    self._viewController.barcodeDelegate = self
  }

  public var Result: String {
    get {
      return _result
    }
  }

  public func DoScan() {
    UIApplication.shared.keyWindow?.rootViewController?.present(_viewController, animated: true, completion: {})
  }

  public func AfterScan(result: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterScan", arguments: result as AnyObject)
  }

  public var UseExternalScanner: Bool {
    get {
      return false
    }
    set {
      NSLog("Suppressing set of UseExternalScanner, which is not required on iOS")
    }
  }

  public func receivedResult(result: String) {
    _result = result
    self.performSelector(onMainThread: #selector(BarcodeScanner.AfterScan(result:)), with: _result, waitUntilDone: false)
  }

  public func canceled() {
    _result = ""
    self.performSelector(onMainThread: #selector(BarcodeScanner.AfterScan(result:)), with: _result, waitUntilDone: false)
  }
}
