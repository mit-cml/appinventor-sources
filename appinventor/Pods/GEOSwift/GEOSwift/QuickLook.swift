//
//  QuickLook.swift
//
//  Created by Andrea Cremaschi on 21/05/15.
//  Copyright (c) 2015 andreacremaschi. All rights reserved.
//

#if canImport(UIKit)

import Foundation
import MapKit
import UIKit

public extension Geometry {
    @objc public func debugQuickLookObject() -> AnyObject? {
        let defaultReturnValue = WKT as AnyObject
        guard let geom = self as? GEOSwiftQuickLook, let region = geom.region else {
            return defaultReturnValue
        }
        let mapView = MKMapView()
        mapView.mapType = .standard
        mapView.frame = CGRect(x: 0, y: 0, width: 400, height: 400)
        mapView.region = region
        guard let image = mapView.snapshot else {
            return defaultReturnValue
        }
        UIGraphicsBeginImageContextWithOptions(image.size, true, image.scale)
        guard let context = UIGraphicsGetCurrentContext() else {
            return defaultReturnValue
        }
        defer { UIGraphicsEndImageContext() }
        image.draw(at: .zero)
        geom.draw(in: context, imageSize: image.size, mapRect: mapView.visibleMapRect)
        return UIGraphicsGetImageFromCurrentImageContext() ?? defaultReturnValue
    }
}

// MARK: GEOSwiftQuickLook

protocol GEOSwiftQuickLook {
    var region: MKCoordinateRegion? { get }
    func draw(in context: CGContext, imageSize: CGSize, mapRect: MKMapRect)
    func envelope() -> Envelope?
}

extension GEOSwiftQuickLook {
    var region: MKCoordinateRegion? {
        guard let envelope = envelope(), let centroid = envelope.centroid() else {
            return nil
        }
        let center = CLLocationCoordinate2D(centroid.coordinate)
        let exteriorRing = envelope.exteriorRing
        let upperLeft = exteriorRing.points[2]
        let lowerRight = exteriorRing.points[0]
        let span = MKCoordinateSpan(latitudeDelta: upperLeft.y - lowerRight.y,
                                    longitudeDelta: upperLeft.x - lowerRight.x)
        return MKCoordinateRegion(center: center, span: span)
    }

    fileprivate func draw(in context: CGContext, imageSize: CGSize, mapRect: MKMapRect, renderer: MKOverlayRenderer) {
        context.saveGState()

        // scale the content to fit inside the image
        let scaleX = imageSize.width / CGFloat(mapRect.size.width)
        let scaleY = imageSize.height / CGFloat(mapRect.size.height)
        context.scaleBy(x: scaleX, y: scaleY)

        // the renderer will draw the geometry at (0,0), so offset CoreGraphics by the right measure
        let upperCorner = renderer.mapPoint(for: .zero)
        context.translateBy(x: CGFloat(upperCorner.x - mapRect.origin.x),
                            y: CGFloat(upperCorner.y - mapRect.origin.y))

        renderer.draw(mapRect, zoomScale: imageSize.width / CGFloat(mapRect.size.width), in: context)

        context.restoreGState()
    }
}

extension Waypoint: GEOSwiftQuickLook {
    var region: MKCoordinateRegion? {
        let center = CLLocationCoordinate2D(coordinate)
        let span = MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
        return MKCoordinateRegion(center: center, span: span)
    }

    func draw(in context: CGContext, imageSize: CGSize, mapRect: MKMapRect) {
        let pin = MKPinAnnotationView(annotation: nil, reuseIdentifier: "")
        if let pinImage = pin.image {
            let coord = CLLocationCoordinate2D(coordinate)
            let mapPoint = MKMapPoint(coord)
            let point = CGPoint(
                x: round(CGFloat((mapPoint.x - mapRect.origin.x) / mapRect.size.width) * imageSize.width),
                y: round(CGFloat((mapPoint.y - mapRect.origin.y) / mapRect.size.height) * imageSize.height))
            var pinImageRect = CGRect(x: 0, y: 0, width: pinImage.size.width, height: pinImage.size.height)
            pinImageRect = pinImageRect.offsetBy(dx: point.x - pinImageRect.width / 2,
                                                 dy: point.y - pinImageRect.height)
            pinImage.draw(in: pinImageRect)
        }
    }
}

extension LineString: GEOSwiftQuickLook {
    func draw(in context: CGContext, imageSize: CGSize, mapRect: MKMapRect) {
        if let overlay = mapShape() as? MKOverlay {
            let renderer = MKPolylineRenderer(overlay: overlay)
            renderer.lineWidth = 2
            renderer.strokeColor = UIColor.blue.withAlphaComponent(0.7)
            draw(in: context, imageSize: imageSize, mapRect: mapRect, renderer: renderer)
        }
    }
}

extension Polygon: GEOSwiftQuickLook {
    func draw(in context: CGContext, imageSize: CGSize, mapRect: MKMapRect) {
        if let overlay = mapShape() as? MKOverlay {
            let renderer = MKPolygonRenderer(overlay: overlay)
            renderer.lineWidth = 2
            renderer.strokeColor = UIColor.blue.withAlphaComponent(0.7)
            renderer.fillColor = UIColor.cyan.withAlphaComponent(0.2)
            draw(in: context, imageSize: imageSize, mapRect: mapRect, renderer: renderer)
        }
    }
}

extension GeometryCollection: GEOSwiftQuickLook {
    func draw(in context: CGContext, imageSize: CGSize, mapRect: MKMapRect) {
        for geometry in geometries {
            if let geom = geometry as? GEOSwiftQuickLook {
                geom.draw(in: context, imageSize: imageSize, mapRect: mapRect)
            }
        }
    }
}

// MARK: - MKMapView Snapshotting

private extension MKMapView {
    /**
     Take a snapshot of the map with MKMapSnapshot, which is designed to work in the background,
     so we block the calling thread with a semaphore.
     */
    var snapshot: UIImage? {
        let options = MKMapSnapshotter.Options()
        options.region = region
        options.size = frame.size

        var snapshotImage: UIImage?
        let backgroundQueue = DispatchQueue.global(qos: .background)
        let snapshotter = MKMapSnapshotter(options: options)
        let semaphore = DispatchSemaphore(value: 0)
        snapshotter.start(with: backgroundQueue) { snapshot, _ in
            snapshotImage = snapshot?.image
            semaphore.signal()
        }
        _ = semaphore.wait(timeout: .now() + 3)

        return snapshotImage
    }
}

#endif // canImport(UIKit)
