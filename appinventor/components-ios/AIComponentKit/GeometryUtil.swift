// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import MapKit

func degToRad(_ degrees: Double) -> Double {
  return degrees * .pi / 180.0
}

func radToDeg(_ radians: Double) -> Double {
  return radians * 180.0 / .pi
}

let kEarthRadius = 6378137.0

func bearing(from initialPoint: CLLocationCoordinate2D, to finalPoint: CLLocationCoordinate2D) -> Double {
  let lat1 = degToRad(initialPoint.latitude)
  let lat2 = degToRad(finalPoint.latitude)
  let lon1 = degToRad(initialPoint.longitude)
  let lon2 = degToRad(finalPoint.longitude)

  let y = sin(lon2 - lon1) * cos(lat2)
  let x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1)
  return radToDeg(atan2(y, x))
}

func distance(from initialPoint: CLLocationCoordinate2D, to finalPoint: CLLocationCoordinate2D) -> Double {
  let lat1 = degToRad(initialPoint.latitude)
  let lat2 = degToRad(finalPoint.latitude)
  let lon1 = degToRad(initialPoint.longitude)
  let lon2 = degToRad(finalPoint.longitude)

  return 2 * kEarthRadius * asin(sqrt(haversine(lat2 - lat1) + cos(lat1) * cos(lat2) * haversine(lon2 - lon1)))
}

func haversine(_ theta: Double) -> Double {
  return 0.5 - cos(theta) / 2
}
