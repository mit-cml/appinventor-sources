<%@page import="com.google.appinventor.server.Server,com.google.appinventor.common.version.AppInventorFeatures,msg.i18n" %>
<%@page import="com.google.appinventor.server.flags.Flag" %>
<%
   if (request.getScheme().equals("http") && Server.isProductionServer()
       && AppInventorFeatures.enableHttpRedirect()) {
        String qs = request.getQueryString();
        String host = request.getServerName();
        if (qs != null) {
           String redirect = "https://" + host + "/?" + qs;
           response.sendRedirect(redirect);
        } else {
           String redirect = "https://" + host;
           response.sendRedirect(redirect);
        }
     return;
   }
   if (AppInventorFeatures.enableHttpRedirect()) {
       response.setHeader("Strict-Transport-Security", "max-age=3600");
   }
  String cachePostfix = "@blocklyeditor_isRelease@".equals("true") ? "cache" : "nocache";
  String locale = request.getParameter("locale");
  if (locale == null || locale.isEmpty()) {
    locale = "en";
  }
  String hash = i18n.mapping.getOrDefault(locale, "");
  if (!hash.isEmpty()) {
    hash = "_" + hash;
  }
  final String odeBase = Flag.createFlag("ode.base", "").get();
  String translation = odeBase + "ode/messages" + hash + "." + cachePostfix + ".js";
%>
<!-- Copyright 2007-2009 Google Inc. All Rights Reserved. -->
<!-- Copyright 2011-2024 Massachusetts Institute of Technology. All Rights Reserved. -->
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=10">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!--meta name="gwt:property" content="locale=en_US"-->
    <!-- Title is set at runtime. -->
    <title> </title>
    <link type="text/css" rel="stylesheet" href="static/css/gwt.css">
    <link type="text/css" rel="stylesheet" href="static/css/blockly.css">
    <link type="text/css" rel="stylesheet" href="static/css/ai2blockly.css">
    <link type="text/css" rel="stylesheet" href="static/closure-library/closure/goog/css/dialog.css">
    <link type="text/css" rel="stylesheet" href="static/closure-library/closure/goog/css/hsvapalette.css">
    <link type="text/css" rel="stylesheet" href="static/font-awesome/css/font-awesome.min.css">
    <link type="text/css" rel="stylesheet" href="static/leaflet/leaflet.css">
    <link type="text/css" rel="stylesheet" href="static/leaflet/leaflet.toolbar.css">
    <link type="text/css" rel="stylesheet" href="static/leaflet/leaflet-vector-markers.css">
    <link type="text/css" rel="stylesheet" href="static/css/Ya.css">
    <link type="text/css" rel="stylesheet" href="static/css/android_holo.css">
    <link type="text/css" rel="stylesheet" href="static/css/android_material.css">
    <link type="text/css" rel="stylesheet" href="static/css/iOS.css">
    <link type="text/css" rel="stylesheet" href="static/css/DarkTheme.css">
    <link type="text/css" rel="stylesheet" href="static/css/fonts.css">
    <noscript>
      <div class="floatingBox">
        <h2> App Inventor needs JavaScript enabled to run.</h2>
      </div>
    </noscript>
    <script type="text/javascript" src="<%=translation%>"></script>
  </head>

  <!-- ODE scripts -->
  <body class="gwt-bodyRob">
    <div class="floatingBox" style="display:none" id="unsupported">
      <h2> Your browser might not be compatible. </h2>
      To use App Inventor, you must use a compatible browser.<br>
      Currently the supported browsers are:
      <ul>
        <li> Google Chrome 55+ </li>
        <li> Safari 11+ </li>
        <li> Firefox 52+ </li>
      </ul>
    </div>
    <% if (!odeBase.isEmpty()) { %>
    <div id=odeblock style="display: none;">
        <h1>If you see this message for an extended period of time, it might be because
        your internet service is blocking requests to <%= odeBase %>. Contact your
        administrator to check on this and remove the block.
        </h1>
    </div>
    <% } %>
    <script type="text/javascript">
      (function() {
        setTimeout(function() {
          var block = document.getElementById('odeblock');
          if (block) {
            block.style.display = 'block';
          }
        }, 2000);
      })();
    </script>
    <script type="text/javascript" src="static/closure-library/closure/goog/base.js"></script>
    <script type="text/javascript" src="<%= odeBase %>ode/aiblockly-@blocklyeditor_BlocklyChecksum@.js"></script>
    <script type="text/javascript" src="static/js/scroll-options-5.0.11.min.js"></script>
    <script type="text/javascript" src="static/js/workspace-search.min.js"></script>
    <script type="text/javascript" src="static/js/block-dynamic-connection-0.6.0.min.js"></script>
    <script type="text/javascript" src="static/js/workspace-multiselect-0.1.14-beta1.min.js"></script>
    <script type="text/javascript" src="static/js/keyboard-navigation-0.5.13.min.js"></script>
    <script type="text/javascript" src="<%= odeBase %>ode/cdnok.js"></script>
    <script type="text/javascript" src="<%= odeBase %>ode/ode.nocache.js"></script>
    <script src="static/leaflet/leaflet.js"></script>
    <script src="static/leaflet/leaflet.toolbar.js"></script>
    <script src="static/leaflet/leaflet-vector-markers.min.js"></script>
    <script src="static/leaflet/leaflet-imgicon.js"></script>
    <script src="static/leaflet/Path.Drag.js"></script>
    <script src="static/leaflet/Leaflet.Editable.js"></script>
    <script src="static/leaflet/leaflet.geometryutil.js"></script>
    <script src="static/leaflet/leaflet.snap.js"></script>
    <script>
      if (window.navigator.userAgent.indexOf("MSIE") != -1){
          document.getElementById("unsupported").style.display = 'block';
      }
    </script>
  </body>
</html>
