<%@page import="javax.servlet.http.HttpServletRequest"%>
<%@page import="com.google.appinventor.server.flags.Flag"%>
<%@page import="com.google.appinventor.server.util.UriBuilder"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<%@page import="org.apache.commons.lang3.StringEscapeUtils"%>
<!doctype html>
<%
   String error = StringEscapeUtils.escapeHtml4(request.getParameter("error"));
   String locale = StringEscapeUtils.escapeHtml4(request.getParameter("locale"));
   String redirect = StringEscapeUtils.escapeHtml4(request.getParameter("redirect"));
   String repo = StringEscapeUtils.escapeHtml4((String) request.getAttribute("repo"));
   String autoload = StringEscapeUtils.escapeHtml4((String) request.getAttribute("autoload"));
   String galleryId = StringEscapeUtils.escapeHtml4((String) request.getAttribute("galleryId"));
   boolean useGoogle = Flag.createFlag("auth.usegoogle", false).get();
   String googleClientId = Flag.createFlag("auth.googleclientid", "").get();
   boolean anonOK = Flag.createFlag("auth.useanon", false).get();
   boolean showLogin = Flag.createFlag("auth.showlogin", true).get();
   String newGalleryId = StringEscapeUtils.escapeHtml4(request.getParameter("ng"));
   String uiPreference = StringEscapeUtils.escapeHtml4(request.getParameter("ui"));
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
    <meta name="google-signin-scope" content="profile email">
    <meta name="google-signin-client_id" content="<%= googleClientId %>">
    <script src="https://apis.google.com/js/platform.js" async defer></script>


    <title>MIT App Inventor</title>
    <link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet" type="text/css"/>
<% if (useGoogle) { %>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
    <script language=javascript>
     var initialized = false;
     function onSignIn(googleUser) {
         console.log('onSignIn called, initialized = ' + initialized);
         if (!initialized) {
             // ignore onSignIn if we haven't completed the signout below
             // the idea here is to make sure when we visit this page (login)
             // that people get a login dialog and don't just immediately get
             // logged in because they still have authentication cookies in
             // Google Land
             return;
         }
         var profile = googleUser.getBasicProfile();
         /* console.log("ID: " + profile.getId()); // Don't send this directly to your server!
          * console.log('Full Name: ' + profile.getName());
          * console.log('Given Name: ' + profile.getGivenName());
          * console.log('Family Name: ' + profile.getFamilyName());
          * console.log("Image URL: " + profile.getImageUrl());
          * console.log("Email: " + profile.getEmail());*/
         // The ID token you need to pass to your backend:
         var id_token = googleUser.getAuthResponse().id_token;
         /* console.log("ID Token: " + id_token);*/
         xhr = new XMLHttpRequest();
         xhr.open('POST', '/login/');
         xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
         xhr.onreadystatechange = function() {
             if (this.readyState != 4) return;
             if (this.status == 200) {
                 // login OK, reload to the index page
                 window.location = "<%= new UriBuilder("/")
                                               .add("locale", locale)
                                               .add("repo", repo)
                                               .add("ng", newGalleryId)
                                               .add("galleryId", galleryId).build() %>";
             } else {
                 window.alert("Login Incorrect");
             }
         };
         xhr.send('idtoken=' + id_token);
    };
    function signOut() {
      gapi.auth2.init().then(function() {
        var auth2 = gapi.auth2.getAuthInstance();
        if (auth2.isSignedIn.get()) {
          auth2.signOut().then(function () {
            initialized = true;
            console.log('User signed out.');
          });
        } else {
          initialized = true;
          console.log("User wasn't logged in");
     }
     });
     };
     window.onload = function() {
         signOut();
     };
    </script>
<% } %>
  <style type="text/css">
    #customBtn {
      display: inline-block;
      background: #4285f4;
      color: white;
      width: 190px;
      border-radius: 5px;
      white-space: nowrap;
    }
    #customBtn:hover {
      cursor: pointer;
    }
    span.label {
      font-weight: bold;
    }
    span.icon {
      background: url('/images/g-normal.png') transparent 5px 50% no-repeat;
      display: inline-block;
      vertical-align: middle;
      width: 42px;
      height: 42px;
      border-right: #2265d4 1px solid;
    }
    span.buttonText {
      display: inline-block;
      vertical-align: middle;
      padding-left: 42px;
      padding-right: 42px;
      font-size: 14px;
      font-weight: bold;
      /* Use the Roboto font that is loaded in the <head> */
      font-family: 'Roboto', sans-serif;
    }
  </style>
  </head>
<body>
  <center>
    <h1>${pleaselogin}</h1>
  </center>
<% if (error != null) {
out.println("<center><font color=red><b>" + error + "</b></font></center><br/>");
   } %>
<% if (showLogin) { %>
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
<% }
   if (uiPreference != null && !uiPreference.equals("")) {
   %>
<input type=hidden name=ui value="<%= uiPreference %>">
<% } %>
<% if (redirect != null && !redirect.equals("")) {
   %>
<input type=hidden name=redirect value="<%= redirect %>">
<% } %>
<p></p>
<center><input type=Submit value="${login}" style="font-size: 300%;"></center>
</form>
<p></p>
<center><p><a href="<%= new UriBuilder("/login/sendlink")
                              .add("locale", locale).build() %>"  style="text-decoration:none;">${passwordclickhereLabel}</a></p></center>
<% } %> <!-- End of showLogin block -->
<% if (anonOK) { %>
<center>
<center>
<form method=POST action="/login">
<input type=hidden name=noaccount value=true>
<input type=submit style="font-size: 20px" value="Continue Without An Account">
</form>
</center>
<br/><br/>or<br/><br/>
<table border=1 cellpadding=10>
<tr><td><center>
<form method=POST action="/login">
<input type=hidden name=revisit value=true>
Your Revisit Code:&nbsp;<input type=text name=A value="" size=4 maxlength=4>-<input type=text name=B value="" size=4 maxlength=4>-<input type=text name=C size=4 maxlength=4 value="">-
<input type=text name=D size=4 maxlength=4 value=""><br/><br/>
<input type=submit value="Enter with Revisit Code">
</form>
</center>
</td></tr></table>
</center>
<% } %>
<% if (useGoogle) { %>
<br/><br/>
<center>
    <div class="g-signin2" data-onsuccess="onSignIn" data-theme="dark"></div>
    <br/>
</center>
<%    } %>
<footer>
<center><a href="<%= new UriBuilder("/login")
                           .add("locale", "zh_CN")
                           .add("repo", repo)
                           .add("autoload", autoload)
                           .add("galleryId", galleryId)
                           .add("ng", newGalleryId)
                           .add("ui", uiPreference)
                           .add("redirect", redirect).build() %>"  style="text-decoration:none;" >中文</a>&nbsp;
<a href="<%= new UriBuilder("/login")
                           .add("locale", "pt")
                           .add("repo", repo)
                           .add("autoload", autoload)
                           .add("galleryId", galleryId)
                           .add("ng", newGalleryId)
                           .add("ui", uiPreference)
                           .add("redirect", redirect).build() %>"  style="text-decoration:none;" >Português</a>&nbsp;
<a href="<%= new UriBuilder("/login")
                   .add("locale", "en")
                   .add("repo", repo)
                   .add("autoload", autoload)
                   .add("galleryId", galleryId)
                   .add("ng", newGalleryId)
                   .add("ui", uiPreference)
                   .add("redirect", redirect).build() %>"  style="text-decoration:none;" >English</a></center>
<p></p>

<p style="text-align: center; clear:both;"><a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/"
                                                 target="_blank"><img alt="Creative Commons License" src="/static/images/cc3.png"></a> <br>
  <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/" target="_blank"></a></p>
</footer>
</body></html>
