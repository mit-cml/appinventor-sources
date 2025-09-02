<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.demos.taskqueueexamples.Counter" %>

<html>
<head>
  <title>Simple Counter</title>
</head>
<body>

<h1>Count is: <%= Counter.getCount("thecounter") %>

<form action="/simplecounter" method="post">
  <input type="submit" value="Increment">
</form>

</body>
</html>
