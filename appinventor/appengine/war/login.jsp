<%@page import="javax.servlet.http.HttpServletRequest"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%!
   public String buildUri(String uri, String locale, String repo, String galleryId) {
     String separator = "?";
     if (locale != null && !locale.equals("")) {
       uri += separator + "locale=" + locale;
       separator = "&";
     }
     if (repo != null && !repo.equals("")) {
       uri += separator + "repo=" + repo;
       separator = "&";
     }
     if (galleryId != null && !galleryId.equals("")) {
       uri += separator + "galleryId=" + galleryId;
     }
     return (uri);
   }
%>
<!doctype html>
<%
   String error = request.getParameter("error");
   String useGoogleLabel = (String) request.getAttribute("useGoogleLabel");
   String locale = request.getParameter("locale");
   String repo = (String) request.getAttribute("repo");
   String galleryId = (String) request.getAttribute("galleryId");
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
    <h1>${pleaselogin}</h1></center>
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
   if (galleryId != null && !galleryId.equals("")) {
   %>
<input type=hidden name=galleryId value="<%= galleryId %>">
<% } %>
<p></p>
<center><input type=Submit value="${login}" style="font-size: 300%;"></center>
</form>
<p></p>
<center><p><a href="/login/sendlink"  style="text-decoration:none;">${passwordclickhereLabel}</a></p></center>
<%    if (useGoogleLabel != null && useGoogleLabel.equals("true")) { %>
<center><p><a href="<%= buildUri("/login/google", locale, repo, galleryId) %>" style="text-decoration:none;">Click Here to use your Google Account to login</a></p></center>
<%    } %>
<footer>
<center><a href="<%= buildUri("/login", "zh_CN", repo, galleryId) %>"  style="text-decoration:none;" >中文</a>&nbsp;
<a href="<%= buildUri("/login", "en", repo, galleryId) %>"  style="text-decoration:none;" >English</a></center>
<p></p>
<center>
<%    if (locale != null && locale.equals("zh_CN")) { %>
<a href="http://www.weibo.com/mitappinventor" target="_blank"><img class="img-scale"
                  src="/images/mzl.png" width="30" height="30" title="Sina WeiBo"></a>&nbsp;
<%    } %>
<a href="http://www.appinventor.mit.edu" target="_blank"><img class="img-scale"
                src="/images/login-app-inventor.jpg" width="50" height="30" title="MIT App Inventor"></a></center>
<p></p>

<p style="text-align: center; clear:both;"><a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/"
                                              target="_blank"><img alt="Creative Commons License" src="/images/cc3.png"></a> <br>
  <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/" target="_blank"></a></p>
</footer>
</body></html>

