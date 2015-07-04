// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * Note: this file was copied, more or less verbatim, from:
 *   java/com/google/collaboration/tables/util/CsvParser.java
 * by Debby Wallach on 7/20/2010.
 *
 * More efficient re-implementation of CsvQuotedParser. This is about 5X faster on large files. A
 * CSV cell can be quoted or unquoted. A quoted cell escapes quotes by doubling them. Whitespace
 * between the closing quote and the delimiter is ignored. An unquoted cell ends at the
 * delimiter. The delimiter is comma or newline (\n). A carriage return (\r) before the newline is
 * part of the delimiter.
 *
 * Quotes are handled subtly differently in CsvQuotedParser. There, quoted content can appear in
 * unquoted cells. This is not
 * <a href="http://en.wikipedia.org/wiki/Comma-separated_values">standard</a> and hampers error
 * reporting. Consider an unquoted cell value {@code 17" screen}. By allowing quoted content inside
 * cells the parser has to keep looking for the matching end quote. By restricting quotes to cell
 * boundaries many accidental quotes can be reported immediately.
 *
 */
public final class CsvParser implements Iterator<List<String>> {

  /**
   * Escaped quotes in quoted cells are doubled.
   */
  private static final Pattern ESCAPED_QUOTE_PATTERN = Pattern.compile("\"\"");

  /**
   * Character buffer for cell parsing. The size limits the largest parsable cell. Specifically, if
   * an unquoted cell and its trailing delimiter exceed this limit, the cell will be split at the
   * limit. Moreover, a quoted large cell will cause a syntax error as when we reach end-of-file
   * without reading a closing quote.
   */
  private final char[] buf = new char[10240];

  private final Reader in;

  /**
   * The beginning of the currently parsed cell in {@code buf}. Everything before it is discarded
   * during compaction. The beginning includes the quote for a quoted cell.
   */
  private int pos;

  /**
   * The end of valid content in {@code buf}.
   */
  private int limit;

  /**
   * Indicates whether more content might be in the reader.
   */
  private boolean opened = true;

  /**
   * Length of a successfully parsed cell. For a quoted cell this includes the closing quote. Set
   * whenever parsing of a cell succeeds. The value should be ignored when cell parsing fails, but
   * is set to -1 to help debugging.
   */
  private int cellLength = -1;

  /**
   * Length of a successfully parsed cell including its trailing delimiter. Set whenever parsing of
   * a cell with trailing delimiter succeeds. The value should be ignored when cell parsing fails,
   * but is set to -1 to help debugging.
   */
  private int delimitedCellLength = -1;

  /**
   * Last exception encountered. Saved here to properly implement {@code Iterator}.
   */
  private IOException lastException;

  private long previouslyRead;

  public CsvParser(InputStream in) {
    this.in = new InputStreamReader(in, Charsets.UTF_8);
  }

  public void skip(long charPosition) throws IOException {
    while (charPosition > 0) {
      int n = in.read(buf, 0, Math.min((int) charPosition, buf.length));
      if (n < 0) break;
      previouslyRead += n;
      charPosition -= n;
    }
  }

  public boolean hasNext() {
    if (limit == 0) {
      fill();
    }
    return (pos < limit || indexAfterCompactionAndFilling(pos) < limit) && lookingAtCell();
  }

  public List<String> next() {
    List<String> result = Lists.newArrayList();
    boolean trailingComma;
    boolean haveMoreData;
    do {
      // Invariant: pos < limit && lookingAtCell() from hasNext() or previous iteration
      if (buf[pos] != '"') {
        result.add(new String(buf, pos, cellLength));
      } else {
        String cell = new String(buf, pos + 1, cellLength - 2);
        result.add(ESCAPED_QUOTE_PATTERN.matcher(cell).replaceAll("\""));
      }
      trailingComma = delimitedCellLength > 0 && buf[pos + delimitedCellLength - 1] == ',';
      pos += delimitedCellLength;
      delimitedCellLength = cellLength = -1;
      haveMoreData = pos < limit || indexAfterCompactionAndFilling(pos) < limit;
    } while (trailingComma && haveMoreData && lookingAtCell());
    return result;
  }

  public long getCharPosition() {
    return previouslyRead + pos;
  }

  /**
   * Compacts and fills the buffer. Returns the possibly shifted index for the given index.
   */
  private int indexAfterCompactionAndFilling(int i) {
    if (pos > 0) {
      i = compact(i);
    }
    fill();
    return i;
  }

  /**
   * Moves the contents between {@code pos} and {@code limit} to the beginning of {@code buf}.
   * Returns the new position of the given index.
   */
  private int compact(int i) {
    int oldPos = pos;
    pos = 0;
    int toMove = limit - oldPos;
    if (toMove > 0) {
      System.arraycopy(buf, oldPos, buf, 0, toMove);
    }
    limit -= oldPos;
    previouslyRead += oldPos;
    return i - oldPos;
  }

  /**
   * Fills {@code buf} from the reader.
   */
  private void fill() {
    int toFill = buf.length - limit;
    while (opened && toFill > 0) {
      try {
        int n = in.read(buf, limit, toFill);
        if (n == -1) {
          opened = false;
        } else {
          limit += n;
          toFill -= n;
        }
      } catch (IOException e) {
        lastException = e;
        opened = false;
      }
    }
  }

  private boolean lookingAtCell() {
    return (buf[pos] == '"' ? findUnescapedEndQuote(pos + 1) : findUnquotedCellEnd(pos));
  }

  private boolean findUnescapedEndQuote(int i) {
    for (; i < limit || (i = indexAfterCompactionAndFilling(i)) < limit; i++) {
      if (buf[i] == '"') {
        i = checkedIndex(i + 1);
        if (i == limit || buf[i] != '"') {
          cellLength = i - pos;
          return findDelimOrEnd(i);
        }
      }
    }
    lastException = new IOException("Syntax Error. unclosed quoted cell");
    return false;
  }

  /**
   * Determines that we are looking at the end of a cell, tolerating some whitespace.  Called after
   * consuming the end quote of a quoted cell.
   */
  private boolean findDelimOrEnd(int i) {
    for (; i < limit || (i = indexAfterCompactionAndFilling(i)) < limit; i++) {
      switch (buf[i]) {
        case ' ':
        case '\t':
          // whitespace after closing quote
          continue;
        case '\r':
          // In standard CSV \r\n terminates a cell. However, Macintosh uses one \r instead of \n.
          int j = checkedIndex(i + 1);
          delimitedCellLength = (buf[j] == '\n' ? checkedIndex(j + 1) : j) - pos;
          return true;
        case ',':
        case '\n':
          delimitedCellLength = (checkedIndex(i + 1) - pos);
          return true;
        default:
          lastException = new IOException(
              "Syntax Error: non-whitespace between closing quote and delimiter or end");
          return false;
      }
    }
    delimitedCellLength = (limit - pos);
    return true;
  }

  /**
   * Returns the given index, after trying to read its corresponding buffered character. The
   * resulting index will be shifted if compaction was triggered.
   */
  private int checkedIndex(int i) {
    return i < limit ? i : indexAfterCompactionAndFilling(i);
  }

  private boolean findUnquotedCellEnd(int i) {
    for (; i < limit || (i = indexAfterCompactionAndFilling(i)) < limit; i++) {
      switch (buf[i]) {
        case ',':
        case '\n':
          cellLength = i - pos;
          delimitedCellLength = cellLength + 1;
          return true;
        case '\r':
          // In standard CSV \r\n terminates a cell. However, Macintosh uses one \r instead of \n.
          cellLength = i - pos;
          int j = checkedIndex(i + 1);
          delimitedCellLength = (buf[j] == '\n' ? checkedIndex(j + 1) : j) - pos;
          return true;
        case '"':
          lastException = new IOException("Syntax Error: quote in unquoted cell");
          return false;
      }
    }
    delimitedCellLength = cellLength = (limit - pos);
    return true;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public void throwAnyProblem() throws IOException {
    if (lastException != null) {
      throw lastException;
    }
  }
}
