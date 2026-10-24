<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.demos.jdoexamples.NamedCounter" %>
<%@ page import="com.google.appengine.demos.jdoexamples.NamedCounterUtils" %>

<html>
<body>
<%
String name = request.getParameter("name");
if (name == null) {
  name = "default";
}
NamedCounter counter = NamedCounterUtils.getByName(name);
%>
<h2>Count for <%= name %>: <%= counter.getCount() %></h2>
<form action="/namedcounter" method="post">
	<p>Get value for <input name="name" value="<%= name %>">
	<input type="submit" name="submit" value="Get"></p>
	<p>Offset by delta <input name="delta" value="1">
	<input type="submit" name="submit" value="Offset"></p>
    <p><input type="submit" name="submit" value="Reset"></p>
</form>

</body>
</html>
