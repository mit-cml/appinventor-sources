<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.demos.jdoexamples.GuestbookEntry" %>
<%@ page import="java.util.List" %>

<html>
<body>
<h2>Entries</h2>
<%
List<GuestbookEntry> entries = GuestbookEntry.getEntries();

if (entries.isEmpty()) {%>
Sorry, no entries.
<%} else {
  for (GuestbookEntry entry : entries) {
    %>
    <div><%= entry.getMessage() %> from <%= entry.getWho() %> at <%= entry.getWhen() %></div>
    <%
  }
}
%>

<h2>New entry</h2>
<form action="/guestbook" method="post">
	<p>Who: <input name="who"></p>
	<p>Message: <input name="message"></p>
	<p><input type="submit" value="Submit"></p>
</form>
</body>
</html>
