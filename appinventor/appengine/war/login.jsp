<%@page import="javax.servlet.http.HttpServletRequest"%>
<%@page import="com.google.appinventor.server.util.UriBuilder"%>
<%@page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!doctype html>
<%
   String error = StringEscapeUtils.escapeHtml4(request.getParameter("error"));
   String useGoogleLabel = (String) request.getAttribute("useGoogleLabel");
   String locale = StringEscapeUtils.escapeHtml4(request.getParameter("locale"));
   String redirect = StringEscapeUtils.escapeHtml4(request.getParameter("redirect"));
   String repo = StringEscapeUtils.escapeHtml4((String) request.getAttribute("repo"));
   String autoload = StringEscapeUtils.escapeHtml4((String) request.getAttribute("autoload"));
   String galleryId = StringEscapeUtils.escapeHtml4((String) request.getAttribute("galleryId"));
   String newGalleryId = StringEscapeUtils.escapeHtml4(request.getParameter("ng"));
   if (locale == null) {
       locale = "en";
   }

%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta HTTP-EQUIV="pragma" CONTENT="no-cache"/>
    <meta HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate"/>
    <meta HTTP-EQUIV="expires" CONTENT="0"/>
    <title>MIT App Inventor</title>
  </head>
<body>
  <center>
    <h1>${pleaselogin}</h1>
  </center>
<% if (error != null) {
out.println("<center><font color=red><b>" + error + "</b></font></center><br/>");
   } %>
<form method=POST action="/login">
<center><table>
<tr><td>${emailAddressLabel}</td><td><input type=text name=email value="" size="35"></td></tr>
<tr><td></td></td>
<tr><td>${passwordLabel}</td><td><input type=password name=password value="" size="35"></td></tr>
</table></center>
<% if (locale != null && !locale.equals("")) {
   %>
<input type=hidden name=locale value="<%= locale %>">
<% }
   if (repo != null && !repo.equals("")) {
   %>
<input type=hidden name=repo value="<%= repo %>">
<% }
   if (autoload != null && !autoload.equals("")) {
   %>
<input type=hidden name=autoload value="<%= autoload %>">
<% }
   if (galleryId != null && !galleryId.equals("")) {
   %>
<input type=hidden name=galleryId value="<%= galleryId %>">
<% }
   if (newGalleryId != null && !newGalleryId.equals("")) {
   %>
<input type=hidden name=ng value="<%= newGalleryId %>">
<% } %>
<% if (redirect != null && !redirect.equals("")) {
   %>
<input type=hidden name=redirect value="<%= redirect %>">
<% } %>
<p></p>
<center><input type=Submit value="${login}" style="font-size: 300%;"></center>
</form>
<p></p>
<center><p><a href="/login/sendlink?locale=<%= locale %>"  style="text-decoration:none;">${passwordclickhereLabel}</a></p></center>
<%    if (useGoogleLabel != null && useGoogleLabel.equals("true")) { %>
<center><p><a href="<%= new UriBuilder("/login/google")
                              .add("locale", locale)
                              .add("autoload", autoload)
                              .add("repo", repo)
                              .add("galleryId", galleryId)
                              .add("ng", newGalleryId)
                              .add("redirect", redirect).build() %>" style="text-decoration:none;">Click Here to use your Google Account to login</a></p></center>
<%    } %>
<footer>
<center><a href="<%= new UriBuilder("/login")
                           .add("locale", "zh_CN")
                           .add("repo", repo)
                           .add("autoload", autoload)
                           .add("galleryId", galleryId)
                           .add("redirect", redirect).build() %>"  style="text-decoration:none;" >中文</a>&nbsp;
<a href="<%= new UriBuilder("/login")
                           .add("locale", "pt")
                           .add("repo", repo)
                           .add("autoload", autoload)
                           .add("galleryId", galleryId)
                           .add("redirect", redirect).build() %>"  style="text-decoration:none;" >Português</a>&nbsp;
<a href="<%= new UriBuilder("/login")
                   .add("locale", "en")
                   .add("repo", repo)
                   .add("autoload", autoload)
                   .add("galleryId", galleryId)
                   .add("ng", newGalleryId)
                   .add("redirect", redirect).build() %>"  style="text-decoration:none;" >English</a></center>
<p></p>
<center>
<%    if (locale != null && locale.equals("zh_CN")) { %>
<a href="http://www.weibo.com/mitappinventor" target="_blank"><img class="img-scale"
                  src="/static/images/mzl.png" width="30" height="30" title="Sina WeiBo"></a>&nbsp;
<%    } %>
<a href="http://www.appinventor.mit.edu" target="_blank"><img class="img-scale"
                src="/static/images/login-app-inventor.jpg" width="50" height="30" title="MIT App Inventor"></a></center>
<p></p>

<p style="text-align: center; clear:both;"><a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/"
                                              target="_blank"><img alt="Creative Commons License" src="/static/images/cc3.png"></a> <br>
  <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/" target="_blank"></a></p>
</footer>
</body></html>

