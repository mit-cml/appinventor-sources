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

class BarcodeScannerViewController: UIViewController, ZXCaptureDelegate {
  private var _capture: ZXCapture!
  private var _barcodeDelegate: BarcodeScannerDelegate!
  private var _started = false
  private var _captureSizeTransform: CGAffineTransform
  private var _scanViewRect: UIView

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

  public override func viewDidLoad() {
    super.viewDidLoad()
    NSLog("View did load")
    _capture = ZXCapture()
    _capture.camera = self._capture.back()
    _capture.focusMode = AVCaptureFocusMode.continuousAutoFocus
    NSLog("sessionPreset = \(_capture.sessionPreset)")
    _scanViewRect.frame = CGRect(x: (3.0/32.0)*self.view.frame.width,
                                 y: (11.0/48.0)*self.view.frame.height,
                                 width: (13.0/16.0)*self.view.frame.width,
                                 height: (13.0/24.0)*self.view.frame.height)
    _scanViewRect.autoresizingMask = UIViewAutoresizing.flexibleHeight.union(.flexibleWidth).union(.flexibleTopMargin).union(.flexibleBottomMargin)
    view.layer.addSublayer(self._capture.layer)
    self.view.addSubview(_scanViewRect)
  }

  public override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    NSLog("View will appear")
    _capture.delegate = self
    applyOrientation()
    _started = true
  }

  public override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    NSLog("View did appear")
    if (!_started) {
      self._capture.start()
    }
  }

  public override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    self._capture.stop()
    _started = false
  }

  public func captureResult(_ capture: ZXCapture!, result: ZXResult!) {
    NSLog("Captured result: \(result)")
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
    self.dismiss(animated: true, completion: {})
    _barcodeDelegate.receivedResult(result: result.text)
  }

  func cancel() {
    self.dismiss(animated: true, completion: {})
    _barcodeDelegate.canceled()
  }
  
  public override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
    super.viewWillTransition(to: size, with: coordinator)
    coordinator.animate(alongsideTransition: { (context: UIViewControllerTransitionCoordinatorContext) in
      return
    }, completion: { (context: UIViewControllerTransitionCoordinatorContext) in
      self.applyOrientation()
      return
    })
  }
  
  private func applyOrientation() {
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
    NSLog("sessionPreset = \(self._capture.sessionPreset)")
    if(self._capture.sessionPreset == AVCaptureSessionPreset1920x1080) {
      videoSizeX = 1080;
      videoSizeY = 1920;
    } else {
      videoSizeX = 720;
      videoSizeY = 1280;
    }
    NSLog("videoSizeX = \(videoSizeX)   videoSizeY = \(videoSizeY)")
    NSLog("self.view.frame = \(self.view.frame)")
    NSLog("self._scanView.frame = \(self._scanViewRect.frame)")
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
    NSLog("_captureSizeTransform = \(_captureSizeTransform)")
    self._capture.scanRect = transformedVideoRect.applying(_captureSizeTransform);
    NSLog("Capture rect: \(_capture.scanRect)")
  }
}

public class BarcodeScanner: NonvisibleComponent, BarcodeScannerDelegate {
  private let _container: ComponentContainer
  private var _result = ""
  private var _viewController: BarcodeScannerViewController?
  private var _navController: UINavigationController?

  public override init(_ container: ComponentContainer) {
    self._container = container
    super.init(container)
  }

  public var Result: String {
    get {
      return _result
    }
  }

  public func DoScan() {
    _viewController = BarcodeScannerViewController()
    _navController = UINavigationController(rootViewController: _viewController!)
    _navController?.modalPresentationStyle = UIModalPresentationStyle.fullScreen
    _navController?.modalTransitionStyle = UIModalTransitionStyle.coverVertical
    _viewController?.barcodeDelegate = self
    UIApplication.shared.keyWindow?.rootViewController?.present(_navController!, animated: true, completion: {})
  }

  public func AfterScan(result: String) {
    _navController?.dismiss(animated: true, completion: nil)
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
    NSLog("received result \(result)")
    _result = result
    self.performSelector(onMainThread: #selector(BarcodeScanner.AfterScan(result:)), with: _result, waitUntilDone: false)
  }

  public func canceled() {
    _result = ""
    self.performSelector(onMainThread: #selector(BarcodeScanner.AfterScan(result:)), with: _result, waitUntilDone: false)
  }
}
