//
//  BarcodeScanner.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public protocol BarcodeScannerDelegate {
  func receivedResult(_ result: String)
  func canceled()
}

class BarcodeScannerViewController: UIViewController, ZXCaptureDelegate {
  fileprivate var _capture: ZXCapture!
  fileprivate var _barcodeDelegate: BarcodeScannerDelegate!
  fileprivate var _captureSizeTransform: CGAffineTransform
  fileprivate var _scanViewRect: UIView

  init() {
    self._scanViewRect = UIView(frame: CGRect(x: 30, y: 105, width: 260, height: 260))
    self._scanViewRect.backgroundColor = UIColor(colorLiteralRed: 1.0, green: 1.0, blue: 1.0, alpha: 0.3)
    self._captureSizeTransform = CGAffineTransform()
    super.init(nibName: nil, bundle: nil)
    let _cancel = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.cancel,
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
    _capture.focusMode = AVCaptureFocusMode.continuousAutoFocus
    _capture.delegate = self
  }

  open override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    _scanViewRect.frame = CGRect(x: (3.0/32.0)*self.view.frame.width,
                                 y: (11.0/48.0)*self.view.frame.height,
                                 width: (13.0/16.0)*self.view.frame.width,
                                 height: (13.0/24.0)*self.view.frame.height)
    _scanViewRect.autoresizingMask = UIViewAutoresizing.flexibleHeight.union(.flexibleWidth).union(.flexibleTopMargin).union(.flexibleBottomMargin)
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
    self.dismiss(animated: true, completion: {})
    _barcodeDelegate.receivedResult(result.text)
  }

  func cancel() {
    if self._capture.running {
      _capture.stop()
      _capture.delegate = nil
    }
    self.dismiss(animated: true, completion: {})
    _barcodeDelegate.canceled()
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
    var scanRectRotation: Float32;
    var captureRotation: Float32;
    
    switch (orientation) {
      case UIInterfaceOrientation.portrait:
        captureRotation = 0
        scanRectRotation = 90
        break
      case UIInterfaceOrientation.landscapeLeft:
        captureRotation = 90;
        scanRectRotation = 180;
        break;
      case UIInterfaceOrientation.landscapeRight:
        captureRotation = 270;
        scanRectRotation = 0;
        break;
      case UIInterfaceOrientation.portraitUpsideDown:
        captureRotation = 180;
        scanRectRotation = 270;
        break;
      default:
        captureRotation = 0;
        scanRectRotation = 90;
        break;
    }
    applyRectOfInterest(orientation)
    let transform = CGAffineTransform(rotationAngle: CGFloat(Double(captureRotation / 180) * M_PI))
    self._capture.transform = transform
    self._capture.rotation = CGFloat(scanRectRotation)
    self._capture.layer.frame = self.view.frame;
  }
  
  func applyRectOfInterest(_ orientation: UIInterfaceOrientation) {
    var scaleVideo: CGFloat, scaleVideoX: CGFloat, scaleVideoY: CGFloat
    var videoSizeX: CGFloat, videoSizeY: CGFloat
    var transformedVideoRect = self._scanViewRect.frame;
    if(self._capture.sessionPreset == AVCaptureSessionPreset1920x1080) {
      videoSizeX = 1080;
      videoSizeY = 1920;
    } else {
      videoSizeX = 720;
      videoSizeY = 1280;
    }
    if(UIInterfaceOrientationIsPortrait(orientation)) {
      scaleVideoX = self.view.frame.size.width / videoSizeX;
      scaleVideoY = self.view.frame.size.height / videoSizeY;
      scaleVideo = max(scaleVideoX, scaleVideoY);
      if(scaleVideoX > scaleVideoY) {
        transformedVideoRect.origin.y += (scaleVideo * videoSizeY - self.view.frame.size.height) / 2;
      } else {
        transformedVideoRect.origin.x += (scaleVideo * videoSizeX - self.view.frame.size.width) / 2;
      }
    } else {
      scaleVideoX = self.view.frame.size.width / videoSizeY;
      scaleVideoY = self.view.frame.size.height / videoSizeX;
      scaleVideo = max(scaleVideoX, scaleVideoY);
      if(scaleVideoX > scaleVideoY) {
        transformedVideoRect.origin.y += (scaleVideo * videoSizeX - self.view.frame.size.height) / 2;
      } else {
        transformedVideoRect.origin.x += (scaleVideo * videoSizeY - self.view.frame.size.width) / 2;
      }
    }
    _captureSizeTransform = CGAffineTransform(scaleX: 1/scaleVideo, y: 1/scaleVideo);
    self._capture.scanRect = transformedVideoRect.applying(_captureSizeTransform);
  }
}

open class BarcodeScanner: NonvisibleComponent, BarcodeScannerDelegate {
  fileprivate let _container: ComponentContainer
  fileprivate var _result = ""
  fileprivate var _viewController: BarcodeScannerViewController?
  fileprivate var _navController: UINavigationController?

  public override init(_ container: ComponentContainer) {
    self._container = container
    super.init(container)
  }

  open var Result: String {
    get {
      return _result
    }
  }

  open func DoScan() {
    _viewController = BarcodeScannerViewController()
    _navController = UINavigationController(rootViewController: _viewController!)
    _navController?.modalPresentationStyle = UIModalPresentationStyle.fullScreen
    _navController?.modalTransitionStyle = UIModalTransitionStyle.coverVertical
    _viewController?.barcodeDelegate = self
    UIApplication.shared.keyWindow?.rootViewController?.present(_navController!, animated: true, completion: {})
  }

  open func AfterScan(_ result: String) {
    _navController?.dismiss(animated: true, completion: nil)
    EventDispatcher.dispatchEvent(of: self, called: "AfterScan", arguments: result as AnyObject)
  }

  open var UseExternalScanner: Bool {
    get {
      return false
    }
    set {
      NSLog("Suppressing set of UseExternalScanner, which is not required on iOS")
    }
  }

  open func receivedResult(_ result: String) {
    _result = result
    self.performSelector(onMainThread: #selector(BarcodeScanner.AfterScan(_:)), with: _result, waitUntilDone: false)
  }

  open func canceled() {
    _result = ""
    self.performSelector(onMainThread: #selector(BarcodeScanner.AfterScan(_:)), with: _result, waitUntilDone: false)
  }
}
