package com.google.appinventor.server.http;

/*
 * net/balusc/http/multipart/MultipartMap.java
 *
 * Copyright (C) 2009 BalusC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library.
 * If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/**
 * The MultipartMap. It simulates the <code>HttpServletRequest#getParameterXXX()</code> methods to
 * ease the processing in <code>@MultipartConfig</code> servlets. You can access the normal request
 * parameters by <code>{@link #getParameter(String)}</code> and you can access multiple request
 * parameter values by <code>{@link #getParameterValues(String)}</code>.
 * <p>
 * On creation, the <code>MultipartMap</code> will put itself in the request scope, identified by
 * the attribute name <code>parts</code>, so that you can access the parameters in EL by for example
 * <code>${parts.fieldname}</code> where you would have used <code>${param.fieldname}</code>. In
 * case of file fields, the <code>${parts.filefieldname}</code> returns a <code>{@link File}</code>.
 * <p>
 * It was a design decision to extend <code>HashMap&lt;String, Object&gt;</code> instead of having
 * just <code>Map&lt;String, String[]&gt;</code> and <code>Map&lt;String, File&gt;</code>
 * properties, because of the accessibility in Expression Language. Also, when the value is obtained
 * by <code>{@link #get(Object)}</code>, as will happen in EL, then multiple parameter values will
 * be converted from <code>String[]</code> to <code>List&lt;String&gt;</code>, so that you can use
 * it in the JSTL <code>fn:contains</code> function.
 *
 * @author BalusC
 * @link http://balusc.blogspot.com/2009/12/uploading-files-in-servlet-30.html
 */
public class MultipartMap extends HashMap<String, Object> {

  // Constants ----------------------------------------------------------------------------------

  private static final String ATTRIBUTE_NAME = "parts";
  private static final String CONTENT_DISPOSITION = "content-disposition";
  private static final String CONTENT_DISPOSITION_FILENAME = "filename";
  private static final String DEFAULT_ENCODING = "UTF-8";
  private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

  // Vars ---------------------------------------------------------------------------------------

  private String encoding;
  private String location;
  private boolean multipartConfigured;

  // Constructors -------------------------------------------------------------------------------

  /**
   * Construct multipart map based on the given multipart request and the servlet associated with
   * the request. The file upload location will be extracted from <code>@MultipartConfig</code>
   * of the servlet. When the encoding is not specified in the given request, then it will default
   * to <tt>UTF-8</tt>.
   * @param multipartRequest The multipart request to construct the multipart map for.
   * @param servlet The servlet which is responsible for the given request.
   * @throws ServletException If something fails at Servlet level.
   * @throws IOException If something fails at I/O level.
   */
  public MultipartMap(HttpServletRequest multipartRequest, Servlet servlet)
      throws ServletException, IOException
  {
    this(multipartRequest, new MultipartConfigElement(
        servlet.getClass().getAnnotation(MultipartConfig.class)).getLocation(), true);
  }

  /**
   * Construct multipart map based on the given multipart request and file upload location. When
   * the encoding is not specified in the given request, then it will default to <tt>UTF-8</tt>.
   * @param multipartRequest The multipart request to construct the multipart map for.
   * @param location The location to save uploaded files in.
   * @throws ServletException If something fails at Servlet level.
   * @throws IOException If something fails at I/O level.
   */
  public MultipartMap(HttpServletRequest multipartRequest, String location)
      throws ServletException, IOException
  {
    this(multipartRequest, location, false);
  }

  /**
   * Global constructor.
   */
  private MultipartMap
  (HttpServletRequest multipartRequest, String location, boolean multipartConfigured)
      throws ServletException, IOException
  {
    multipartRequest.setAttribute(ATTRIBUTE_NAME, this);

    this.encoding = multipartRequest.getCharacterEncoding();
    if (this.encoding == null) {
      multipartRequest.setCharacterEncoding(this.encoding = DEFAULT_ENCODING);
    }
    this.location = location;
    this.multipartConfigured = multipartConfigured;

    for (Part part : multipartRequest.getParts()) {
      String filename = getFilename(part);
      if (filename == null) {
        processTextPart(part);
      }
    }
  }

  // Actions ------------------------------------------------------------------------------------

  @Override
  public Object get(Object key) {
    Object value = super.get(key);
    if (value instanceof String[]) {
      String[] values = (String[]) value;
      return values.length == 1 ? values[0] : Arrays.asList(values);
    } else {
      return value; // Can be File or null.
    }
  }

  /**
   * @see ServletRequest#getParameter(String)
   */
  public String getParameter(String name) {
    Object value = super.get(name);
    if (value instanceof File) {
      return ((File) value).getName();
    }
    String[] values = (String[]) value;
    return values != null ? values[0] : null;
  }

  /**
   * @see ServletRequest#getParameterValues(String)
   */
  public String[] getParameterValues(String name) {
    Object value = super.get(name);
    if (value instanceof File) {
      return new String[] { ((File) value).getName() };
    }
    return (String[]) value;
  }

  /**
   * @see ServletRequest#getParameterNames()
   */
  public Enumeration<String> getParameterNames() {
    return Collections.enumeration(keySet());
  }

  /**
   * @see ServletRequest#getParameterMap()
   */
  public Map<String, String[]> getParameterMap() {
    Map<String, String[]> map = new HashMap<String, String[]>();
    for (Entry<String, Object> entry : entrySet()) {
      Object value = entry.getValue();
      if (value instanceof String[]) {
        map.put(entry.getKey(), (String[]) value);
      } else {
        map.put(entry.getKey(), new String[] { ((File) value).getName() });
      }
    }
    return map;
  }

  /**
   * Returns uploaded file associated with given request parameter name.
   * @param name Request parameter name to return the associated uploaded file for.
   * @return Uploaded file associated with given request parameter name.
   * @throws IllegalArgumentException If this field is actually a Text field.
   */
  public File getFile(String name) {
    Object value = super.get(name);
    if (value instanceof String[]) {
      throw new IllegalArgumentException("This is a Text field. Use #getParameter() instead.");
    }
    return (File) value;
  }

  // Helpers ------------------------------------------------------------------------------------

  /**
   * Returns the filename from the content-disposition header of the given part.
   */
  private String getFilename(Part part) {
    for (String cd : part.getHeader(CONTENT_DISPOSITION).split(";")) {
      if (cd.trim().startsWith(CONTENT_DISPOSITION_FILENAME)) {
        return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
      }
    }
    return null;
  }

  /**
   * Returns the text value of the given part.
   */
  private String getValue(Part part) throws IOException {
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(part.getInputStream(), encoding));
    StringBuilder value = new StringBuilder();
    char[] buffer = new char[DEFAULT_BUFFER_SIZE];
    for (int length = 0; (length = reader.read(buffer)) > 0;) {
      value.append(buffer, 0, length);
    }
    return value.toString();
  }

  /**
   * Process given part as Text part.
   */
  private void processTextPart(Part part) throws IOException {
    String name = part.getName();
    String[] values = (String[]) super.get(name);

    if (values == null) {
      // Not in parameter map yet, so add as new value.
      put(name, new String[] { getValue(part) });
    } else {
      // Multiple field values, so add new value to existing array.
      int length = values.length;
      String[] newValues = new String[length + 1];
      System.arraycopy(values, 0, newValues, 0, length);
      newValues[length] = getValue(part);
      put(name, newValues);
    }
  }

  /**
   * Process given part as File part which is to be saved in temp dir with the given filename.
   */
  private void processFilePart(Part part, String filename) throws IOException {
    // First fix stupid MSIE behaviour (it passes full client side path along filename).
    filename = filename
        .substring(filename.lastIndexOf('/') + 1)
        .substring(filename.lastIndexOf('\\') + 1);

    // Get filename prefix (actual name) and suffix (extension).
    String prefix = filename;
    String suffix = "";
    if (filename.contains(".")) {
      prefix = filename.substring(0, filename.lastIndexOf('.'));
      suffix = filename.substring(filename.lastIndexOf('.'));
    }

    // Write uploaded file.
    File file = File.createTempFile(prefix + "_", suffix, new File(location));
    if (multipartConfigured) {
      part.write(file.getName()); // Will be written to the very same File.
    } else {
      InputStream input = null;
      OutputStream output = null;
      try {
        input = new BufferedInputStream(part.getInputStream(), DEFAULT_BUFFER_SIZE);
        output = new BufferedOutputStream(new FileOutputStream(file), DEFAULT_BUFFER_SIZE);
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        for (int length = 0; ((length = input.read(buffer)) > 0);) {
          output.write(buffer, 0, length);
        }
      } finally {
        if (output != null) try { output.close(); } catch (IOException logOrIgnore) { /**/ }
        if (input != null) try { input.close(); } catch (IOException logOrIgnore) { /**/ }
      }
    }

    put(part.getName(), file);
    part.delete(); // Cleanup temporary storage.
  }

}