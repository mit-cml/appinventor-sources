/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.demos.jdoexamples;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NamedCounterServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String action = req.getParameter("submit");
    if (action == null) {
      throw new ServletException("No action supplied!");
    }
    if (action.equals("Reset")) {
      NamedCounterUtils.reset(req.getParameter("name"));
    } else if (action.equals("Offset")) {
      NamedCounterUtils.addAndGet(req.getParameter("name"),
                                  Integer.parseInt(req.getParameter("delta")));
    } else if (action.equals("Get")) {
    }
    resp.sendRedirect("/namedcounter.jsp?name=" + req.getParameter("name"));
  }
}
