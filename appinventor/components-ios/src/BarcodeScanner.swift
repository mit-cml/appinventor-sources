// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public protocol BarcodeScannerDelegate {
  func receivedResult(_ result: String)
  func canceled()
}

fileprivate let kLeftMargin = CGFloat(3.0/32.0)
fileprivate let kTopMargin = CGFloat(11.0/48.0)
fileprivate let kWidth = CGFloat(13.0/16.0)
fileprivate let kHeight = CGFloat(13.0/24.0)

class BarcodeScannerViewController: UIViewController, ZXCaptureDelegate {
  fileprivate var _capture: ZXCapture!
  fileprivate var _barcodeDelegate: BarcodeScannerDelegate!
  fileprivate var _captureSizeTransform: CGAffineTransform
  fileprivate var _scanViewRect: UIView

  init() {
    self._scanViewRect = UIView(frame: CGRect(x: 30, y: 105, width: 260, height: 260))
    self._scanViewRect.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.3)
    self._captureSizeTransform = CGAffineTransform()
    super.init(nibName: nil, bundle: nil)
    let _cancel = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.cancel,
                                  target: self,
                                  action: #selector(BarcodeScannerViewController.cancel))
    _cancel.title = "Cancel"
    self.navigationItem.title = "Scan Barcode"
    self.navigationItem.leftBarButtonItem = _cancel
  }

  override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
    self._scanViewRect = UIView(frame: CGRect(x: 30, y: 105, width: 260, height: 260))
    self._captureSizeTransform = CGAffineTransform()
    super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
  }

  required init?(coder aDecoder: NSCoder) {
    self._scanViewRect = UIView(frame: CGRect(x: 30, y: 105, width: 260, height: 260))
    self._captureSizeTransform = CGAffineTransform()
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

  open override func viewDidLoad() {
    super.viewDidLoad()
    _capture = ZXCapture()
    _capture.camera = self._capture.back()
    _capture.focusMode = AVCaptureDevice.FocusMode.continuousAutoFocus
    _capture.delegate = self
  }

  open override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    _scanViewRect.frame = CGRect(x: kLeftMargin * self.view.frame.width,
                                 y: kTopMargin * self.view.frame.height,
                                 width: kWidth * self.view.frame.width,
                                 height: kHeight * self.view.frame.height)
    _scanViewRect.autoresizingMask = UIView.AutoresizingMask.flexibleHeight.union(.flexibleWidth).union(.flexibleTopMargin).union(.flexibleBottomMargin)
    view.layer.addSublayer(self._capture.layer)
    self.view.addSubview(_scanViewRect)
    applyOrientation()
  }

  open override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    if !self._capture.running {
      _capture.delegate = self
      _capture.start()
    }
  }

  open override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    if self._capture.running {
      _capture.stop()
      _capture.delegate = nil
      _capture.layer.removeFromSuperlayer()
      _scanViewRect.removeFromSuperview()
    }
  }

  open func captureResult(_ capture: ZXCapture!, result: ZXResult!) {
    if self._capture.running {
      _capture.stop()
      _capture.delegate = nil
    }
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
    self.dismiss(animated: true) {
      self._barcodeDelegate.receivedResult(result.text)
    }
  }

  @objc func cancel() {
    if self._capture.running {
      _capture.stop()
      _capture.delegate = nil
    }
    self.dismiss(animated: true) {
      self._barcodeDelegate.canceled()
    }
  }
  
  open override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
    super.viewWillTransition(to: size, with: coordinator)
    coordinator.animate(alongsideTransition: { (context: UIViewControllerTransitionCoordinatorContext) in
      return
    }, completion: { (context: UIViewControllerTransitionCoordinatorContext) in
      self.applyOrientation()
      return
    })
  }
  
  fileprivate func applyOrientation() {
    let orientation = UIApplication.shared.statusBarOrientation
    var scanRectRotation: Float32
    var captureRotation: Float32
    
    switch (orientation) {
      case UIInterfaceOrientation.portrait:
        captureRotation = 0
        scanRectRotation = 90
      case UIInterfaceOrientation.landscapeLeft:
        captureRotation = 90
        scanRectRotation = 180
      case UIInterfaceOrientation.landscapeRight:
        captureRotation = 270
        scanRectRotation = 0
        break
      case UIInterfaceOrientation.portraitUpsideDown:
        captureRotation = 180
        scanRectRotation = 270
      default:
        captureRotation = 0
        scanRectRotation = 90
    }
    applyRectOfInterest(orientation)
    let transform = CGAffineTransform(rotationAngle: CGFloat(Double(captureRotation / 180) * .pi))
    self._capture.transform = transform
    self._capture.rotation = CGFloat(scanRectRotation)
    self._capture.layer.frame = self.view.frame
  }
  
  @objc func applyRectOfInterest(_ orientation: UIInterfaceOrientation) {
    var videoSizeWidth: CGFloat, videoSizeHeight: CGFloat
    if(self._capture.sessionPreset == convertFromAVCaptureSessionPreset(AVCaptureSession.Preset.hd1920x1080)) {
      videoSizeWidth = 1920;
      videoSizeHeight = 1080;
    } else {
      videoSizeWidth = 1280;
      videoSizeHeight = 720;
    }
    if(orientation.isPortrait) {
      // the video is landscape, so we need to swap the meaning of left/top and width/height
      self._capture.scanRect = CGRect(x: kTopMargin * videoSizeWidth,
                                      y: kLeftMargin * videoSizeHeight,
                                      width: kHeight * videoSizeWidth,
                                      height: kWidth * videoSizeHeight)
    } else {
      self._capture.scanRect = CGRect(x: kLeftMargin * videoSizeWidth,
                                      y: kTopMargin * videoSizeHeight,
                                      width: kWidth * videoSizeWidth,
                                      height: kHeight * videoSizeHeight)
    }
    NSLog("scanRect = \(_capture.scanRect)")
  }
}

open class BarcodeScanner: NonvisibleComponent, BarcodeScannerDelegate {
  fileprivate var _result = ""
  fileprivate var _viewController: BarcodeScannerViewController?
  fileprivate var _navController: UINavigationController?
  fileprivate static var errorSeen = false
  fileprivate static let UILock = DispatchSemaphore(value: 1) //in the event that multiple BarcodeScanner items exist, to prevent multiple alerts

  public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  @objc open var Result: String {
    get {
      return _result
    }
  }

  @objc open func HasPermission() -> Bool {
    if let result = PermissionHandler.HasPermission(for: .camera) {
      return result
    } else {
      return false
    }
  }

  @objc open func RequestPermission() {
    doRequestPermission()
  }

  fileprivate func doRequestPermission(completionHandler: ((Bool, Bool) -> ())? = nil) {
    PermissionHandler.RequestPermission(for: .camera, with: completionHandler)
  }

  @objc open func PermissionChange(_ allowed: Bool){
    EventDispatcher.dispatchEvent(of: self, called: "PermissionChange", arguments: allowed as AnyObject)
  }

  @objc open func DoScan() {
    doRequestPermission() { allowed, changed in
      if changed {
        self.PermissionChange(allowed)
      }
      DispatchQueue.main.async {
        if allowed {
          BarcodeScanner.UILock.wait()
          self._viewController = BarcodeScannerViewController()
          self._navController = UINavigationController(rootViewController: self._viewController!)
          self._navController?.modalPresentationStyle = UIModalPresentationStyle.fullScreen
          self._navController?.modalTransitionStyle = UIModalTransitionStyle.coverVertical
          self._viewController?.barcodeDelegate = self
          UIApplication.shared.keyWindow?.rootViewController?.present(self._navController!, animated: true, completion: {})
          BarcodeScanner.errorSeen = false
          BarcodeScanner.UILock.signal()
        } else {
          BarcodeScanner.UILock.wait()
          if !BarcodeScanner.errorSeen {
            let alert = UIAlertController(title: "Camera Access Denied", message: "Because AppInventor does not have permission to access the camera, Barcode Scanner components will not open.", preferredStyle: .alert)
            let close = UIAlertAction(title: "OK", style: .default, handler: nil)
            alert.addAction(close)
            UIApplication.shared.keyWindow?.rootViewController?.present(alert, animated: true, completion: {})
          }

          BarcodeScanner.errorSeen = true
          BarcodeScanner.UILock.signal()
        }
      }
    }
  }

  @objc open func AfterScan(_ result: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterScan", arguments: result as AnyObject)
  }

  @objc open var UseExternalScanner: Bool {
    get {
      return false
    }
    set {
      NSLog("Suppressing set of UseExternalScanner, which is not required on iOS")
    }
  }

  @objc open func receivedResult(_ result: String) {
    _result = result
    self.performSelector(onMainThread: #selector(BarcodeScanner.AfterScan(_:)), with: _result, waitUntilDone: false)
  }

  @objc open func canceled() {
    _result = ""
    self.performSelector(onMainThread: #selector(BarcodeScanner.AfterScan(_:)), with: _result, waitUntilDone: false)
  }
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertFromAVCaptureSessionPreset(_ input: AVCaptureSession.Preset) -> String {
	return input.rawValue
}
