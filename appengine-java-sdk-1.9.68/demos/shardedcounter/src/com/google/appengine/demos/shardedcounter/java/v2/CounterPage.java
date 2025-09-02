/* Copyright (c) 2011 Google Inc.
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

package com.google.appengine.demos.shardedcounter.java.v2;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Renders an HTML page showing current count of the sharded counter.
 * 
 * Through the form, the user can increment the counter or add new shards.
 */
public class CounterPage extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    displayPageStart(resp);
    ShardedCounter counter = new ShardedCounter("test-counter");
    displayCounts(counter, resp);
    displayInputFormAndClose(resp);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    displayPageStart(resp);
    ShardedCounter counter = new ShardedCounter("test-counter");
    if (Boolean.parseBoolean(req.getParameter("addShard"))) {
      int inc = Integer.parseInt(req.getParameter("inc"));
      displayCount(counter, resp);
      displayNumberOfShards(counter.addShards(inc), resp);
    } else {
      counter.increment();
      displayCount(counter, resp);
      displayNumberOfShards(counter.getShardCount(), resp);
    }
    displayInputFormAndClose(resp);
  }

  private void displayPageStart(HttpServletResponse resp) throws IOException {
    resp.getWriter().println("<html>");
    resp.getWriter().println("  <body>");
  }

  private void displayCount(ShardedCounter counter, HttpServletResponse resp)
      throws IOException {
    resp.getWriter()
        .println("<p>Current count: " + counter.getCount() + "</p>");
  }

  private void displayCounts(ShardedCounter counter, HttpServletResponse resp)
      throws IOException {
    displayCount(counter, resp);
    displayNumberOfShards(counter.getShardCount(), resp);
  }

  private void displayNumberOfShards(long shards, HttpServletResponse resp)
      throws IOException {
    resp.getWriter().print("<p>Counter has " + shards);
    if (shards == 1) {
      resp.getWriter().println(" shard</p>");
    } else {
      resp.getWriter().println(" shards</p>");
    }
  }

  private void displayInputFormAndClose(HttpServletResponse resp)
      throws IOException {
    resp.getWriter().println("<form action='.' method='post'>");
    resp.getWriter().println("  <div><input type='submit' value='+1' /></div>");
    resp.getWriter().println("</form>");

    resp.getWriter().println("<form action='.' method='post'>");
    resp.getWriter().println(
        "  <input type='hidden' name='addShard' value='true'>");
    resp.getWriter().println("  <input type='hidden' name='inc' value='1'>");
    resp.getWriter().println(
        "  <div><input type='submit' value='Add Shard' /></div>");
    resp.getWriter().println("</form>");

    resp.getWriter().println("  </body>");
    resp.getWriter().println("</html>");
  }
}
