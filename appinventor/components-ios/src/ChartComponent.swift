//
//  ChartComponent.swift
//  AIComponentKit
//
//  Created by David Kim on 3/19/24.
//  Copyright © 2024 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation

// Define a protocol to represent the common functionalities for chart components
protocol ChartComponent: Component {
    // Method to initialize the chart data object
    func initChartData()
}
