<?xml version="1.0"  encoding="ISO-8859-1"?>
<!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure_9_0.dtd">

<Configure class="org.eclipse.jetty.webapp.WebAppContext">
  <Set name="contextPath">/</Set>
  <Set name="war">appinventor.war</Set>
<!--  <Set name="war"><SystemProperty name="appinventor.root"/>/appinventor/appengine/build/war</Set> -->

  <Call name="setInitParameter">
    <Arg>org.eclipse.jetty.servlet.MaxAge</Arg>
    <Arg>-1</Arg>
  </Call>

<!-- Note: Closing </Configure> tag is provided by StartSystem.java -->

