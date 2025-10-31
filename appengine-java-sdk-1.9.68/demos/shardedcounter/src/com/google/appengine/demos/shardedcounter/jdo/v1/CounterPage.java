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

package com.google.appengine.demos.shardedcounter.jdo.v1;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet provides the HTML for the counter web page.
 *
 */
public class CounterPage extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.getWriter().println("<html>");
    resp.getWriter().println("  <body>");

    Counter counter = new Counter();

    resp.getWriter().println("  <p>Current count: " + counter.getCount()
        + "</p>");

    resp.getWriter().println("<form action='.' method='post'>");
    resp.getWriter().println(
        "  <div><input type='submit' value='+1' /></div>");
    resp.getWriter().println("</form>");

    resp.getWriter().println("  </body>");
    resp.getWriter().println("</html>");
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    Counter counter = new Counter();
    counter.addShard();
    doGet(req, resp);
  }
}
