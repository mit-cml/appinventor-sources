<%@page import="javax.servlet.http.HttpServletRequest"%>
<%@page import="com.google.appinventor.server.util.UriBuilder"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<%
String error = request.getParameter("error");
String useGoogleLabel = (String) request.getAttribute("useGoogleLabel");
String locale = request.getParameter("locale");
String redirect = request.getParameter("redirect");
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
        <div style="text-align: center; margin: 0 auto; width: 800px;">
            <h1>${pleaselogin}</h1>
            <% if (error != null) { %>
                <span style="font-weight: bold; color: red; margin: 0 auto;"><%= error %></span><br>
            <% } %>

            <form method="post">
                <table style="margin: 0 auto;">
                    <tr>
                        <td style="text-align: right;">${emailAddressLabel}</td>
                        <td style="text-align: left;"><input type="text" name="email" value="" size="35"></td>
                    </tr>
                    <tr>
                        <td style="text-align: right;">${passwordLabel}</td>
                        <td style="text-align: left;"><input type="password" name="password" value="" size="35"></td>
                    </tr>
                </table>
                <% if (locale != null && !locale.equals("")) { %>
                <input type="hidden" name="locale" value="<%= locale %>">
                <% } %>
                <%  if (repo != null && !repo.equals("")) { %>
                <input type="hidden" name="repo" value="<%= repo %>">
                <% } %>
                <% if (galleryId != null && !galleryId.equals("")) { %>
                <input type="hidden" name="galleryId" value="<%= galleryId %>">
                <% } %>
                <% if (redirect != null && !redirect.equals("")) { %>
                <input type="hidden" name="redirect" value="<%= redirect %>">
                <% } %>
                <input type="submit" value="${login}" style="font-size: 300%;">
            </form>
            <div>
                <a href="/login/sendlink"  style="text-decoration:none;">${passwordclickhereLabel}</a>
            </div>
            <% if (useGoogleLabel != null && useGoogleLabel.equals("true")) { %>
            <div>
                <a href="<%= new UriBuilder("/login/google")
                         .add("locale", locale)
                         .add("repo", repo)
                         .add("galleryId", galleryId)
                         .add("redirect", redirect).build() %>" style="text-decoration:none;">Click Here to use your Google Account to login</a>
            </div>
            <% } %>
            <div>
                <a href="<%= new UriBuilder("/login")
                         .add("locale", "zh_CN")
                         .add("repo", repo)
                         .add("galleryId", galleryId)
                         .add("redirect", redirect).build() %>"  style="text-decoration:none;" >中文</a>&nbsp;
                <a href="<%= new UriBuilder("/login")
                         .add("locale", "pt")
                         .add("repo", repo)
                         .add("galleryId", galleryId)
                         .add("redirect", redirect).build() %>"  style="text-decoration:none;" >Português</a>&nbsp;
                <a href="<%= new UriBuilder("/login")
                         .add("locale", "en")
                         .add("repo", repo)
                         .add("galleryId", galleryId)
                         .add("redirect", redirect).build() %>"  style="text-decoration:none;" >English</a>
            </div>
            <div>
                <% if (locale != null && locale.equals("zh_CN")) { %>
                <a href="http://www.weibo.com/mitappinventor" target="_blank">
                    <img class="img-scale" src="/images/mzl.png" width="30" height="30" title="Sina WeiBo">
                </a>
                <% } %>
                <a href="http://www.appinventor.mit.edu" target="_blank">
                    <img class="img-scale" src="/images/login-app-inventor.jpg" width="50" height="30" title="MIT App Inventor">
                </a>
            </div>
            <div style="text-align: center; clear:both;">
                <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/" target="_blank">
                    <img alt="Creative Commons License" src="/images/cc3.png">
                </a>
                <br>
                <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/" target="_blank"></a>
            </div>
        </div>
    </body>
</html>
