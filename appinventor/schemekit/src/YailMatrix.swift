//
//  YailMatrix.swift
//  SchemeKit
//
//  Created by Grant Hu on 4/1/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
//
@objc open class YailMatrix: NSObject, SCMObject {
    
    public var interpreter: SCMInterpreter {
        SCMInterpreter.shared
    }
    
    public func isPicEqual(_ other: pic_value) -> Bool {
        return false
    }
    
    public func mark() {
        return
    }
    
    public let isBool: Bool = false
    public let isNumber: Bool = false
    public let isString: Bool = false
    public let isList: Bool = false
    public let isDictionary: Bool = false
    public let isComponent: Bool = false
    public let isNil: Bool = false
    public let isSymbol: Bool = false
    public let isCons: Bool = false
    public let isExact: Bool = false
    public let value: pic_value = 0
    
    public func copy(with zone: NSZone? = nil) -> Any {
        return ""
    }
    
    enum MatrixError: Error {
        case dimensionMismatch(String)
        case matrixInversionError(String)
        case matrixAccessError(String)
    }
    
    private let rows: Int
    private let cols: Int
    private var data: [[Double]]
    
    public init(rows: Int, cols: Int, data: [[Double]]) throws {
        if data.count != rows {
            throw MatrixError.dimensionMismatch("Matrix dimensions mismatch: Expected \(rows) rows but got \(data.count)")
        }
        
        for i in 0..<data.count {
            if data[i].count != cols {
                throw MatrixError.dimensionMismatch("Matrix dimensions mismatch: Row \(i+1) has \(data[i].count) columns, expected \(cols)")
            }
        }
        
        self.rows = rows
        self.cols = cols
        self.data = data
        super.init()
    }
    
    @objc public static func makeMatrix(_ dataValues: [AnyObject]) throws -> YailMatrix {
        guard let firstValue = dataValues.first as? [Any] else {
            throw MatrixError.dimensionMismatch("Invalid structure")
        }
        
        guard let rows = firstValue[0] as? Int, let cols = firstValue[1] as? Int else {
            throw MatrixError.dimensionMismatch("First two elements must be rows and cols integers")
        }
        
        if dataValues.count != 2 + rows * cols {
            throw MatrixError.dimensionMismatch("Matrix data size invalid: expected \(rows * cols) values but got \(dataValues.count - 2)")
        }
        
        var matrixData: [[Double]] = []
        for i in 0..<rows {
            var row: [Double] = []
            for j in 0..<cols {
                let value = dataValues[2 + i * cols + j]
                if let number = value as? Double {
                    row.append(number)
                } else {
                    throw MatrixError.dimensionMismatch("Expected number but found \(type(of: value))")
                }
            }
            matrixData.append(row)
        }
        
        return try YailMatrix(rows: rows, cols: cols, data: matrixData)
    }
    
    func getCell(row: Int, col: Int) throws -> Double {
        try validateIndices(row: row, col: col)
        return data[row - 1][col - 1]
    }
    
    func setCell(row: Int, col: Int, value: Double) throws {
        try validateIndices(row: row, col: col)
        data[row - 1][col - 1] = value
    }
    
    func getRow(row: Int) throws -> [Double] {
        try validateRowIndex(row)
        return data[row - 1]
    }
    
    func getColumn(col: Int) throws -> [Double] {
        try validateColumnIndex(col)
        return data.map { $0[col - 1] }
    }
    
    private func validateIndices(row: Int, col: Int) throws {
        try validateRowIndex(row)
        try validateColumnIndex(col)
    }
    
    private func validateRowIndex(_ row: Int) throws {
        if row < 1 || row > rows {
            throw MatrixError.matrixAccessError("Row index out of bounds: \(row)")
        }
    }
    
    private func validateColumnIndex(_ col: Int) throws {
        if col < 1 || col > cols {
            throw MatrixError.matrixAccessError("Column index out of bounds: \(col)")
        }
    }
    
    static func transpose(matrix: YailMatrix) -> YailMatrix {
        var transposedData: [[Double]] = Array(repeating: Array(repeating: 0.0, count: matrix.rows), count: matrix.cols)
        for i in 0..<matrix.rows {
            for j in 0..<matrix.cols {
                transposedData[j][i] = matrix.data[i][j]
            }
        }
        return try! YailMatrix(rows: matrix.cols, cols: matrix.rows, data: transposedData)
    }
    
    static func inverse(matrix: YailMatrix) throws -> YailMatrix {
        if matrix.rows != matrix.cols {
            throw MatrixError.matrixInversionError("Matrix must be square for inversion")
        }
        
        let n = matrix.rows
        var augmented: [[Double]] = Array(repeating: Array(repeating: 0.0, count: 2 * n), count: n)
        
        // Augmented matrix [A | I]
        for i in 0..<n {
            for j in 0..<n {
                augmented[i][j] = matrix.data[i][j]
            }
            for j in n..<2 * n {
                augmented[i][j] = (i == j - n) ? 1 : 0
            }
        }
        
        // Gaussian elimination
        for i in 0..<n {
            let diag = augmented[i][i]
            if diag == 0 {
                throw MatrixError.matrixInversionError("Matrix is not invertible")
            }
            for j in 0..<2 * n {
                augmented[i][j] /= diag
            }
            
            for k in 0..<n {
                if k == i { continue }
                let factor = augmented[k][i]
                for j in 0..<2 * n {
                    augmented[k][j] -= factor * augmented[i][j]
                }
            }
        }
        
        var inverseData: [[Double]] = Array(repeating: Array(repeating: 0.0, count: n), count: n)
        for i in 0..<n {
            for j in 0..<n {
                inverseData[i][j] = augmented[i][j + n]
            }
        }
        
        return try YailMatrix(rows: n, cols: n, data: inverseData)
    }
    
    static func add(matrix1: YailMatrix, matrix2: YailMatrix) throws -> YailMatrix {
        if matrix1.rows != matrix2.rows || matrix1.cols != matrix2.cols {
            throw MatrixError.dimensionMismatch("Matrix dimensions must match for addition")
        }
        
        var resultData: [[Double]] = Array(repeating: Array(repeating: 0.0, count: matrix1.cols), count: matrix1.rows)
        for i in 0..<matrix1.rows {
            for j in 0..<matrix1.cols {
                resultData[i][j] = matrix1.data[i][j] + matrix2.data[i][j]
            }
        }
        
        return try! YailMatrix(rows: matrix1.rows, cols: matrix1.cols, data: resultData)
    }
    
    static func subtract(matrix1: YailMatrix, matrix2: YailMatrix) throws -> YailMatrix {
        if matrix1.rows != matrix2.rows || matrix1.cols != matrix2.cols {
            throw MatrixError.dimensionMismatch("Matrix dimensions must match for subtraction")
        }
        
        var resultData: [[Double]] = Array(repeating: Array(repeating: 0.0, count: matrix1.cols), count: matrix1.rows)
        for i in 0..<matrix1.rows {
            for j in 0..<matrix1.cols {
                resultData[i][j] = matrix1.data[i][j] - matrix2.data[i][j]
            }
        }
        
        return try! YailMatrix(rows: matrix1.rows, cols: matrix1.cols, data: resultData)
    }
    
    static func multiply(matrix1: YailMatrix, matrix2OrScalar: Any) throws -> YailMatrix {
        if let scalar = matrix2OrScalar as? Double {
            return try scalarMultiply(matrix: matrix1, scalar: scalar)
        } else if let matrix2 = matrix2OrScalar as? YailMatrix {
            if matrix1.cols != matrix2.rows {
                throw MatrixError.dimensionMismatch("Matrix multiplication requires matching inner dimensions")
            }
            
            var resultData: [[Double]] = Array(repeating: Array(repeating: 0.0, count: matrix2.cols), count: matrix1.rows)
            for i in 0..<matrix1.rows {
                for j in 0..<matrix2.cols {
                    for k in 0..<matrix1.cols {
                        resultData[i][j] += matrix1.data[i][k] * matrix2.data[k][j]
                    }
                }
            }
            return try! YailMatrix(rows: matrix1.rows, cols: matrix2.cols, data: resultData)
        } else {
            throw MatrixError.dimensionMismatch("Invalid argument for matrix multiplication")
        }
    }
    
    static func scalarMultiply(matrix: YailMatrix, scalar: Double) -> YailMatrix {
        var resultData: [[Double]] = Array(repeating: Array(repeating: 0.0, count: matrix.cols), count: matrix.rows)
        for i in 0..<matrix.rows {
            for j in 0..<matrix.cols {
                resultData[i][j] = matrix.data[i][j] * scalar
            }
        }
        return try! YailMatrix(rows: matrix.rows, cols: matrix.cols, data: resultData)
    }
    
    static func power(matrix: YailMatrix, exponent: Int) throws -> YailMatrix {
        if matrix.rows != matrix.cols {
            throw MatrixError.dimensionMismatch("Matrix exponentiation requires a square matrix")
        }
        
        if exponent < 0 {
            throw MatrixError.dimensionMismatch("Matrix exponent must be non-negative")
        }
        
        var result = try identityMatrix(size: matrix.rows)
        
        for _ in 0..<exponent {
            result = try multiply(matrix1: result, matrix2OrScalar: matrix)
        }
        
        return result
    }
    
    static func identityMatrix(size: Int) -> YailMatrix {
        var identityData: [[Double]] = Array(repeating: Array(repeating: 0.0, count: size), count: size)
        for i in 0..<size {
            identityData[i][i] = 1.0
        }
        return try! YailMatrix(rows: size, cols: size, data: identityData)
    }
    
    public func toString() -> String {
        var builder = "["
        for i in 0..<rows {
            builder += "["
            for j in 0..<cols {
                builder += "\(data[i][j])"
                if j < cols - 1 {
                    builder += ", "
                }
            }
            builder += "]"
            if i < rows - 1 {
                builder += ", "
            }
        }
        builder += "]"
        return builder
    }
}


