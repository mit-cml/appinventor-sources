<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.demos.jdoexamples.Friend" %>
<%@ page import="com.google.appengine.demos.jdoexamples.FriendUtils" %>
<%@ page import="java.util.List" %>

<html><body>

<%
String lastName = request.getParameter("lastName");
if (lastName == null) lastName = "Rubble";
String firstName = request.getParameter("firstName");
if (firstName == null) firstName = "Barney";

List<Friend> friends = FriendUtils.getFriendsOf(lastName, firstName);
%>
<h2><%= firstName %> <%= lastName %></h2>
<%if (friends.isEmpty()) {%>
<div>No friends.</div>
<%} else {
    for (Friend friend : friends) {%>
      <div>
        <a href="/friends.jsp?lastName=<%= friend.getLastName() %>&firstName=<%= friend.getFirstName() %>"><%= friend.getLastName() %>, <%= friend.getFirstName() %></a>
        <span>
          Connects to 
          <%
            List<String> connectedFriends = friend.getFriends();
            if (connectedFriends.isEmpty()) {%>
              nobody
          <%} else {
              for (String name : friend.getFriends()) {%>
                <span><%=name %>;</span>
            <%}
            }%>
        </span>
      </div>	

  <%}
  }%>

<h2>Add friend</h2>
<form action="/friends" method="post">
  <input type="hidden" name="lastName" value="<%= lastName %>">
  <input type="hidden" name="firstName" value="<%= firstName %>">
  <p>First name: <input name="friendFirstName"> Last name: <input name="friendLastName">
  <input type="submit" value="Add"></p>
</form>

<h2>Lookup</h2>
<form action="/friends.jsp" method="get">
  <p>
    First name: <input name="firstName" value="<%= firstName %>">
    Last name: <input name="lastName" value="<%= lastName %>">
    <input type="submit" value="Lookup">
  </p>
</form>

</body></html>
