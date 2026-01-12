// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import Accelerate
import UIKit
import DGCharts

protocol HasTrendline {
  func getPoints(xMin: CGFloat, xMax: CGFloat, viewWidth: Int) -> [CGPoint]
  func getColor() -> UIColor
  func getDashPattern() -> [CGFloat]?
  func getLineWidth() -> CGFloat
  func isVisible() -> Bool
}

protocol TrendlineCalculator {
  /**
   Compute the trendline for the given x and y values. The x and y arrays must be the same length.

   - Parameters:
   - x: The array of x values.
   - y: The array of y values.
   - Returns: A dictionary containing the results of the computation. The specific keys will vary depending on the underlying model.
   */
  func compute(x: [Double], y: [Double]) -> [String: Any]

  /**
   Compute a set of points that represents the trendline to be drawn on a Chart. The result is an array of
   CGPoint representing the points of the trendline.

   - Parameters:
   - results: The results from a previous call to `compute`.
   - xMin: The x value of the left edge of the chart.
   - xMax: The x value of the right edge of the chart.
   - viewWidth: The width of the chart view, in points.
   - steps: The number of segments to compute.
   - Returns: An array of CGPoint containing the points of the trendline.
   */
  func computePoints(results: [String: Double], xMin: Double, xMax: Double, viewWidth: Int, steps: Int) -> [CGPoint]
}

class LinearRegression: TrendlineCalculator {
  func compute(x: [Double], y: [Double]) -> [String: Any] {
    guard !x.isEmpty, !y.isEmpty, x.count == y.count else {
      fatalError("Lists must not be empty and must have equal numbers of elements")
    }

    let n = Double(x.count)
    let sumx = x.reduce(0, +)
    let sumy = y.reduce(0, +)
    let sumxy = zip(x, y).reduce(0) { $0 + $1.0 * $1.1 }
    let sumxSquared = x.reduce(0) { $0 + $1 * $1 }
    let sumySquared = y.reduce(0) { $0 + $1 * $1 }

    let xmean = sumx / n
    let ymean = sumy / n

    let xxmean = x.reduce(0) { $0 + ($1 - xmean) * ($1 - xmean) }
    let xymean = zip(x, y).reduce(0) { $0 + ($1.0 - xmean) * ($1.1 - ymean) }

    let slope = xymean / xxmean
    let intercept = ymean - slope * xmean

    let predictions = x.map { slope * $0 + intercept }

    let corrNumerator = n * sumxy - sumx * sumy
    let corrDenominator = sqrt((n * sumxSquared - sumx * sumx) * (n * sumySquared - sumy * sumy))
    let corr = corrNumerator / corrDenominator

    let results: [String: Any] = [
      "slope": slope,
      "Yintercept": intercept,
      "correlation coefficient": corr,
      "predictions": predictions,
      "r^2": corr * corr,
      "Xintercepts": slope == 0 ? Double.nan : -intercept / slope
    ]

    return results
  }

  func computePoints(results: [String: Double], xMin: Double, xMax: Double, viewWidth: Int, steps: Int) -> [CGPoint] {
    guard let slope = results["slope"],
          let intercept = results["Yintercept"] else {
      return []
    }

    let yMin = slope * xMin + intercept
    let yMax = slope * xMax + intercept

    return [CGPoint(x: xMin, y: yMin), CGPoint(x: xMax, y: yMax)]
  }
}

class QuadraticRegression: TrendlineCalculator {
  
  func xVector(_ x: Double) -> [Double] {
    return [1, x, x * x]
  }

  func logY() -> Bool {
    return false
  }

  func size() -> Int {
    return 3
  }

  func compute(x: [Double], y: [Double]) -> [String: Any] {
    guard !x.isEmpty, !y.isEmpty, x.count == y.count else {
      fatalError("Lists must not be empty and must have equal numbers of elements")
    }

    let n = Double(x.count)
    let sumX = x.reduce(0, +)
    let sumY = y.reduce(0, +)
    let sumX2 = x.reduce(0) { $0 + $1 * $1 }
    let sumX3 = x.reduce(0) { $0 + $1 * $1 * $1 }
    let sumX4 = x.reduce(0) { $0 + $1 * $1 * $1 * $1 }
    let sumXY = zip(x, y).reduce(0) { $0 + $1.0 * $1.1 }
    let sumX2Y = zip(x, y).reduce(0) { $0 + $1.0 * $1.0 * $1.1 }

    // Calculate adjusted sums
    let xx = sumX2 - (sumX * sumX) / n
    let xy = sumXY - (sumX * sumY) / n
    let xx2 = sumX3 - (sumX2 * sumX) / n
    let x2y = sumX2Y - (sumX2 * sumY) / n
    let x2x2 = sumX4 - (sumX2 * sumX2) / n

    // Calculate coefficients
    let denominator = (xx * x2x2) - (xx2 * xx2)
    guard denominator != 0 else {
      return ["error": "Invalid input data, cannot compute coefficients"]
    }

    let a = ((x2y * xx) - (xy * xx2)) / denominator
    let b = ((xy * x2x2) - (x2y * xx2)) / denominator
    let c = (sumY / n) - b * (sumX / n) - a * (sumX2 / n)

    // Calculate discriminant for roots
    let discriminant = b * b - 4 * a * c
    var intercepts = [Double]()
    if discriminant > 0 {
      let sqrtDiscriminant = sqrt(discriminant)
      intercepts.append((-b + sqrtDiscriminant) / (2 * a))
      intercepts.append((-b - sqrtDiscriminant) / (2 * a))
    } else if discriminant == 0 {
      intercepts.append(-b / (2 * a))
    }

    // Correlation coefficient and R²
    let meanY = sumY / n
    var ssTotal = 0.0, ssResidual = 0.0
    for i in 0..<x.count {
      let predictedY = (a * x[i] + b) * x[i] + c
      ssTotal += (y[i] - meanY) * (y[i] - meanY)
      ssResidual += (y[i] - predictedY) * (y[i] - predictedY)
    }

    let rSquared = 1 - ssResidual / ssTotal
    let correlationCoefficient = sqrt(rSquared)

    return [
      "x^2": a,
      "slope": b,
      "Yintercept": c,
      "correlation coefficient": correlationCoefficient,
      "r^2": rSquared,
      "Xintercepts": intercepts.isEmpty ? [Double.nan] : intercepts
    ]
  }

  func computePoints(results: [String: Double], xMin: Double, xMax: Double, viewWidth: Int, steps: Int) -> [CGPoint] {
    guard let a = results["x^2"], let b = results["slope"], let c = results["Yintercept"] else {
      return []
    }

    var points = [CGPoint]()
    let stepSize = (xMax - xMin) / Double(steps)
    for i in 0..<steps {
      let x = xMin + Double(i) * stepSize
      let y = (a * x + b) * x + c
      points.append(CGPoint(x: x, y: y))
    }

    return points
  }
}

class ExponentialRegression: TrendlineCalculator {
  func xVector(_ x: Double) -> [Double] {
    return [1, x]
  }

  func logY() -> Bool {
    return true
  }

  func size() -> Int {
    return 2
  }

  func compute(x: [Double], y: [Double]) -> [String: Any] {
    guard !x.isEmpty, !y.isEmpty, x.count == y.count else {
      fatalError("Lists must not be empty and must have equal numbers of elements")
    }

    let n = Double(x.count)
    let logYValues = y.map { log($0) }
    let sumx = x.reduce(0, +)
    let sumy = logYValues.reduce(0, +)
    let sumxy = zip(x, logYValues).reduce(0) { $0 + $1.0 * $1.1 }
    let sumxSquared = x.reduce(0) { $0 + $1 * $1 }
    let sumySquared = logYValues.reduce(0) { $0 + $1 * $1 }

    let xmean = sumx / n
    let ymean = sumy / n

    let xxmean = x.reduce(0) { $0 + ($1 - xmean) * ($1 - xmean) }
    let xymean = zip(x, logYValues).reduce(0) { $0 + ($1.0 - xmean) * ($1.1 - ymean) }

    let slope = xymean / xxmean
    let intercept = ymean - slope * xmean

    let corrNumerator = n * sumxy - sumx * sumy
    let corrDenominator = sqrt((n * sumxSquared - sumx * sumx) * (n * sumySquared - sumy * sumy))
    let corr = corrNumerator / corrDenominator

    let a = exp(intercept)
    let b = exp(slope)

    let results: [String: Any] = [
      "a": a,
      "b": b,
      "Yintercept": a, // Since 'a' is the exponential model's Y-intercept
      "correlation coefficient": corr,
      "r^2": corr * corr
    ]

    return results
  }

  func computePoints(results: [String: Double], xMin: Double, xMax: Double, viewWidth: Int, steps: Int) -> [CGPoint] {
    guard let a = results["a"], let b = results["b"] else {
      return []
    }

    var points = [CGPoint]()
    let stepSize = (xMax - xMin) / Double(steps)
    for i in 0..<steps {
      let x = xMin + Double(i) * stepSize
      let y = a * pow(b, x)
      points.append(CGPoint(x: x, y: y))
    }

    return points
  }
}


class LogarithmicRegression: TrendlineCalculator {
  func xVector(_ x: Double) -> [Double] {
    // Ensure that x is greater than zero to apply the logarithm
    guard x > 0 else { return [1, Double.nan] } // Logarithm of non-positive numbers is undefined
    return [1, log(x)]
  }

  func logY() -> Bool {
    return false
  }

  func size() -> Int {
    return 2
  }

  func compute(x: [Double], y: [Double]) -> [String: Any] {
    guard !x.isEmpty, !y.isEmpty, x.count == y.count else {
      fatalError("Lists must not be empty and must have equal numbers of elements")
    }

    let n = Double(x.count)
    let transformedX = x.map { $0 > 0 ? log($0) : Double.nan }
    let sumx = transformedX.reduce(0, +)
    let sumy = y.reduce(0, +)
    let sumxy = zip(transformedX, y).reduce(0) { $0 + $1.0 * $1.1 }
    let sumxSquared = transformedX.reduce(0) { $0 + $1 * $1 }
    let sumySquared = y.reduce(0) { $0 + $1 * $1 }

    let xmean = sumx / n
    let ymean = sumy / n

    let xxmean = transformedX.reduce(0) { $0 + ($1 - xmean) * ($1 - xmean) }
    let xymean = zip(transformedX, y).reduce(0) { $0 + ($1.0 - xmean) * ($1.1 - ymean) }

    let slope = xymean / xxmean
    let intercept = ymean - slope * xmean

    let corrNumerator = n * sumxy - sumx * sumy
    let corrDenominator = sqrt((n * sumxSquared - sumx * sumx) * (n * sumySquared - sumy * sumy))
    let corr = corrNumerator / corrDenominator

    var results: [String: Double] = [
      "slope": round(slope),
      "intercept": round(intercept),
      "r^2": corr * corr
    ]

    if let m = results.removeValue(forKey: "slope"),
       let i = results.removeValue(forKey: "intercept") {
      results["a"] = i
      results["b"] = m
    }

    return results
  }

  func computePoints(results: [String: Double], xMin: Double, xMax: Double, viewWidth: Int, steps: Int) -> [CGPoint] {
    guard let b = results["b"], let a = results["a"], xMin > 0 else {
      return []
    }

    var points = [CGPoint]()
    let stepSize = (xMax - xMin) / Double(steps)
    var lastX = max(xMin, 0.0001)  // Ensure x is positive since log(x) is undefined for x <= 0
    var lastY = a + b * log(lastX)

    for i in 0..<steps {
      let x = xMin + Double(i) * stepSize
      let y = a + b * log(x)
      points.append(CGPoint(x: x, y: y))

      lastX = x
      lastY = y
    }

    return points
  }
}
