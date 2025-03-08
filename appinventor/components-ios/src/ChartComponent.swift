// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

// Define a protocol to represent the common functionalities for chart components
protocol ChartComponent: Component {
  // Method to initialize the chart data object
  func initChartData()
}
