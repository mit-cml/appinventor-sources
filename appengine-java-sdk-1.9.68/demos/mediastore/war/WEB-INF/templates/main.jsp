<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.demos.mediastore.MediaObject" %>
<%@ page import="java.util.List" %>
<%
  User user = (User) request.getAttribute("user");
  String authURL = (String) request.getAttribute("authURL");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN"
  "http://www.w3.org/TR/html4/strict.dtd">

<html lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>MediaStore</title>
  </head>
  <body>
    <div align="right">
      <%
        if (user != null) {
      %>
        Hi there, <%= user.getNickname() %>
      <% } %>

      <a href="<%= authURL %>">
        <% if (user != null) { %>Log out<% } else  { %>Log in<% } %>
      </a>
    </div>

    <ul>
        <% 
           String[] errors = (String[]) request.getAttribute("errors");
           for (int i = 0; i < errors.length; i++) { %>
          <li style="color: red"><%= errors[i]%></li>
        <% } %>
      </ul>

    <h1 align="center">
      <% if (user != null) { %>
        Your media
      <% } else { %>
        Welcome to The MediaStore!
      <% } %>
    </h1>

    <hr>
      <%
         List<MediaObject> files =
           (List<MediaObject>) request.getAttribute("files");
         if (files.size() > 0) {
           for (int i = 0; i < files.size(); i++) {
             MediaObject item = files.get(i);
      %>
            <b>
              <% if (item.isImage()) { %>
                <a href="<%=item.getDisplayURL()%>">
              <% } else { %>
                <a href="<%=item.getURLPath()%>">
              <% } %>
               "<%=item.getTitle()%>"
            </a></b>:
            <%=item.getSize()%> <%=item.getCreationTime()%>
            <%=item.getContentType()%></a><br>
            <%=item.getDescription()%><br>
      <%
           }
         } else {
      %>
        <div align="center">
          <% if (user != null) { %>
            No media found.
          <% } else { %>
            Log in or look for users media
          <% } %>
        </div>
      <% } %>

      <% if (user != null) { %>
        <hr>
        <a href="/upload">Upload new media</a>
      <% } %>
  </body>
</html>
