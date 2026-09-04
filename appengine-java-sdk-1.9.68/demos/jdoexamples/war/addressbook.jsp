<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.demos.jdoexamples.AddressBookEntry" %>
<%@ page import="com.google.appengine.demos.jdoexamples.AddressBookUtils" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<html>
<body>
<h2>Entries</h2>
<%
Long keyOffsetId = null;
String keyOffset = request.getParameter("page");
if (keyOffset != null) keyOffsetId = Long.decode(keyOffset);
if (keyOffset == null) keyOffset = "";
String indexString = request.getParameter("startIndex");
if (indexString == null) indexString = "0";
int indexOffset = Integer.parseInt(indexString);
String lastNameSelected = request.getParameter("lastNameSelected");
if (lastNameSelected == null) lastNameSelected = "";
String stateSelected = request.getParameter("stateSelected");
if (stateSelected == null) stateSelected = "";
%>

<form action="/addressbook.jsp" method="get">
  <p>
  <p>Select entries with 
  Last name: <input name="lastNameSelected" value="<%= lastNameSelected %>">
  and/or State: <input name="stateSelected" value="<%= stateSelected %>">
  <input type="submit" value="Search">
  </p>
</form>

<%
List<AddressBookEntry> entries = new ArrayList<AddressBookEntry>(
    AddressBookUtils.getPage(keyOffsetId, indexOffset, lastNameSelected, stateSelected));
AddressBookEntry lastEntry = null;
if (entries.size() > 3) {
  lastEntry = entries.get(3);
  entries.remove(3);
}

if (entries.isEmpty()) {%>
There are no entries.
<%} else {
  for (AddressBookEntry entry : entries) {
    %>
    <div>
      <div><%= entry.getPersonalInfo().firstName %> <%= entry.getPersonalInfo().lastName %></div>
      <div><%= entry.getAddressInfo().city %>, <%= entry.getAddressInfo().state %></div>
      <div><%= entry.getContactInfo().phoneNumber %></div>
    </div>
    <br>
    <%
  }
  
  if (lastEntry != null) {%>
  <!--<a href="/addressbook.jsp?page=<%= lastEntry.getId().toString() %>&startIndex=0">Next page by key offset</a>-->
  <!--<a href="/addressbook.jsp?page=<%= keyOffset %>&startIndex=<%= indexOffset + 3 %>">Next page by index offset</a>-->
  <a href="/addressbook.jsp?page=<%= lastEntry.getId().toString() %>">Next page by key offset</a>
  <a href="/addressbook.jsp?startIndex=<%= indexOffset + 3 %>">Next page by index offset</a>
    <%
  } else {
    %>
    No more pages of entries.
    <%
  }
}%>

<h2>New entry</h2>
<form action="/addressbook" method="post">
  <p>First name: <input name="firstName"> Last name: <input name="lastName"></p>
  <p>City: <input name="city">, State: <input name="state"></p>
  <p>Phone number: <input name="phoneNumber"></p>
  <p><input type="submit" value="Submit"></p>
</form>
<br>


</body>
</html>
