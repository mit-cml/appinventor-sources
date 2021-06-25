const repeat = require('repeat-string')

/**
 * @typedef MarkdownTableOptions
 * @property {string|string[]} [align]
 * @property {boolean} [padding=true]
 * @property {boolean} [delimiterStart=true]
 * @property {boolean} [delimiterStart=true]
 * @property {boolean} [delimiterEnd=true]
 * @property {boolean} [alignDelimiters=true]
 * @property {(value: string) => number} [stringLength]
 */

/**
 * Create a table from a matrix of strings.
 *
 * @param {string[][]} table
 * @param {MarkdownTableOptions} [options]
 * @returns {string}
 */
module.exports =  function markdownTable(table, options) {
  var settings = options || {}
  var align = (settings.align || []).concat()
  var stringLength = settings.stringLength || defaultStringLength
  /** @type {number[]} Character codes as symbols for alignment per column. */
  var alignments = []
  var rowIndex = -1
  /** @type {string[][]} Cells per row. */
  var cellMatrix = []
  /** @type {number[][]} Sizes of each cell per row. */
  var sizeMatrix = []
  /** @type {number[]} */
  var longestCellByColumn = []
  var mostCellsPerRow = 0
  /** @type {number} */
  var columnIndex
  /** @type {string[]} Cells of current row */
  var row
  /** @type {number[]} Sizes of current row */
  var sizes
  /** @type {number} Sizes of current cell */
  var size
  /** @type {string} Current cell */
  var cell
  /** @type {string[]} */
  var lines
  /** @type {string[]} Chunks of current line. */
  var line
  /** @type {string} */
  var before
  /** @type {string} */
  var after
  /** @type {number} */
  var code

  // This is a superfluous loop if we don’t align delimiters, but otherwise we’d
  // do superfluous work when aligning, so optimize for aligning.
  while (++rowIndex < table.length) {
    columnIndex = -1
    row = []
    sizes = []

    if (table[rowIndex].length > mostCellsPerRow) {
      mostCellsPerRow = table[rowIndex].length
    }

    while (++columnIndex < table[rowIndex].length) {
      cell = serialize(table[rowIndex][columnIndex])

      if (settings.alignDelimiters !== false) {
        size = stringLength(cell)
        sizes[columnIndex] = size

        if (
          longestCellByColumn[columnIndex] === undefined ||
          size > longestCellByColumn[columnIndex]
        ) {
          longestCellByColumn[columnIndex] = size
        }
      }

      row.push(cell)
    }

    cellMatrix[rowIndex] = row
    sizeMatrix[rowIndex] = sizes
  }

  // Figure out which alignments to use.
  columnIndex = -1

  if (typeof align === 'object' && 'length' in align) {
    while (++columnIndex < mostCellsPerRow) {
      alignments[columnIndex] = toAlignment(align[columnIndex])
    }
  } else {
    code = toAlignment(align)

    while (++columnIndex < mostCellsPerRow) {
      alignments[columnIndex] = code
    }
  }

  // Inject the alignment row.
  columnIndex = -1
  row = []
  sizes = []

  while (++columnIndex < mostCellsPerRow) {
    code = alignments[columnIndex]
    before = ''
    after = ''

    if (code === 99 /* `c` */) {
      before = ':'
      after = ':'
    } else if (code === 108 /* `l` */) {
      before = ':'
    } else if (code === 114 /* `r` */) {
      after = ':'
    }

    // There *must* be at least one hyphen-minus in each alignment cell.
    size =
      settings.alignDelimiters === false
        ? 1
        : Math.max(
            1,
            longestCellByColumn[columnIndex] - before.length - after.length
          )

    cell = before + repeat('-', size) + after

    if (settings.alignDelimiters !== false) {
      size = before.length + size + after.length

      if (size > longestCellByColumn[columnIndex]) {
        longestCellByColumn[columnIndex] = size
      }

      sizes[columnIndex] = size
    }

    row[columnIndex] = cell
  }

  // Inject the alignment row.
  cellMatrix.splice(1, 0, row)
  sizeMatrix.splice(1, 0, sizes)

  rowIndex = -1
  lines = []

  while (++rowIndex < cellMatrix.length) {
    row = cellMatrix[rowIndex]
    sizes = sizeMatrix[rowIndex]
    columnIndex = -1
    line = []

    while (++columnIndex < mostCellsPerRow) {
      cell = row[columnIndex] || ''
      before = ''
      after = ''

      if (settings.alignDelimiters !== false) {
        size = longestCellByColumn[columnIndex] - (sizes[columnIndex] || 0)
        code = alignments[columnIndex]

        if (code === 114 /* `r` */) {
          before = repeat(' ', size)
        } else if (code === 99 /* `c` */) {
          if (size % 2) {
            before = repeat(' ', size / 2 + 0.5)
            after = repeat(' ', size / 2 - 0.5)
          } else {
            before = repeat(' ', size / 2)
            after = before
          }
        } else {
          after = repeat(' ', size)
        }
      }

      if (settings.delimiterStart !== false && !columnIndex) {
        line.push('|')
      }

      if (
        settings.padding !== false &&
        // Don’t add the opening space if we’re not aligning and the cell is
        // empty: there will be a closing space.
        !(settings.alignDelimiters === false && cell === '') &&
        (settings.delimiterStart !== false || columnIndex)
      ) {
        line.push(' ')
      }

      if (settings.alignDelimiters !== false) {
        line.push(before)
      }

      line.push(cell)

      if (settings.alignDelimiters !== false) {
        line.push(after)
      }

      if (settings.padding !== false) {
        line.push(' ')
      }

      if (
        settings.delimiterEnd !== false ||
        columnIndex !== mostCellsPerRow - 1
      ) {
        line.push('|')
      }
    }

    lines.push(
      settings.delimiterEnd === false
        ? line.join('').replace(/ +$/, '')
        : line.join('')
    )
  }

  return lines.join('\n')
}

/**
 * @param {string|null|undefined} [value]
 * @returns {string}
 */
function serialize(value) {
  return value === null || value === undefined ? '' : String(value)
}

/**
 * @param {string} value
 * @returns {number}
 */
function defaultStringLength(value) {
  return value.length
}

/**
 * @param {string} value
 * @returns {number}
 */
function toAlignment(value) {
  var code = typeof value === 'string' ? value.charCodeAt(0) : 0

  return code === 67 /* `C` */ || code === 99 /* `c` */
    ? 99 /* `c` */
    : code === 76 /* `L` */ || code === 108 /* `l` */
    ? 108 /* `l` */
    : code === 82 /* `R` */ || code === 114 /* `r` */
    ? 114 /* `r` */
    : 0
}