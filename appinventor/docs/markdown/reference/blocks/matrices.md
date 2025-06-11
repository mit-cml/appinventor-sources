---
title: MIT App Inventor Matrix Blocks
layout: documentation
---

* [create matrix](#creatematrix)
* [create multidimensional matrix](#creatematrixmultidim)
* [get matrix row](#getrow)
* [get matrix column](#getcolumn)
* [get matrix cell](#getcell)
* [set matrix cell](#setcell)
* [matrix +](#matrixadd)
* [matrix -](#matrixsubtract)
* [matrix *](#matrixmultiply)
* [matrix ^](#matrixpower)
* [matrix inverse](#inverse)
* [matrix transpose](#transpose)
* [matrix rotate left](#rotateleft)
* [matrix rotate right](#rotateright)

### create a matrix {#creatematrix}

![](images/matrices/creatematrix.png)

Creates a 2x2 matrix initialized with cell values of 0. Dimensions and cell values can be mutated.

### create multidimensional matrix {#creatematrixmultidim}

![](images/matrices/creatematrixmultidim.png)

Creates a multidimensional matrix initialized with cell values of 0. Dimensions and cell values can be mutated.

### get matrix cell {#getcell}

![](images/matrices/getcell.png)

Returns the cell value at the specified dimensions in the given matrix.

### set matrix cell {#setcell}

![](images/matrices/setcell.png)

Changes the cell value at the specified dimensions in the given matrix. The previous cell value at that position is removed.

### get matrix row {#getrow}

![](images/matrices/getrow.png)

Returns the row of the given row index in the given matrix.

### get matrix column {#getcolumn}

![](images/matrices/getcolumn.png)

Returns the column of the given column index in the given matrix.

### matrix + {#matrixadd}

![](images/matrices/matrixadd.png)

Returns the result of adding two matrices together. The two matrices must have equal dimensions.


### matrix - {#matrixsubtract}

![](images/matrices/matrixsubtract.png)

Returns the result of subtracting one matrix from another matrix. The two matrices must have equal dimensions.

### matrix * {#matrixmultiply}

![](images/matrices/matrixmultiply.png)

Returns the result of multipling either two matrices together or a matrix with a scalar. In the case of two matrices, they must have matching inner dimensions (e.g. the column of the first matrix must equal the row of the second matrix).

### matrix ^ {#matrixpower}

![](images/matrices/matrixpower.png)

Returns the result of raising a matrix to the power of a scalar. The matrix must be a square matrix and the scalar must be nonnegative.

### matrix inverse {#inverse}

![](images/matrices/inverse.png)

Returns the inverse of the given matrix.

### matrix transpose {#transpose}

![](images/matrices/transpose.png)

Returns the transpose of the given matrix.

### matrix rotate left {#rotateleft}

![](images/matrices/rotateleft.png)

Returns the left rotation of the matrix (90 degrees counterclockwise).

### matrix rotate right {#rotateright}

![](images/matrices/rotateright.png)

Returns the right rotation of the matrix (90 degrees clockwise).
