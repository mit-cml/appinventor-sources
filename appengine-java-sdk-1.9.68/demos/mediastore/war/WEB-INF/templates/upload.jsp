<%@ page import="com.google.appengine.api.users.User" %>
<%
  User user = (User) request.getAttribute("user");
  String authURL = (String) request.getAttribute("authURL");
  String uploadURL = (String) request.getAttribute("uploadURL");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" 
  "http://www.w3.org/TR/html4/strict.dtd">

<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <title>MediaStore Upload</title>
</head>
<body>
  <div align="right">
    Hi there, <%= user.getNickname() %> | <a href="<%= authURL %>">Log out</a>
  </div>

  <form action="<%= uploadURL %>" method="POST" enctype="multipart/form-data">
    Sharing: <select name="share">
      <option value="private">Private</option>
          <option value="public">Public</option>
        </select>
    Title: <input type="text" size="40" name="title"><br>
    Description:<br>
    <textarea cols="80" rows="20" name="description"></textarea><br>
    Upload File: <input type="file" name="file"><br>
    <input type="submit" name="submit" value="Submit">
  </form>

  <hr>
  <a href="/">Cancel</a>
</body>
</html>