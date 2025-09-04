#
# Base server
#

[optional]
jvm
jmx
ext
resources

[lib]
lib/tomcat-servlet-api-8.0.5.jar
#lib/jetty-schemas-3.1.jar
lib/jetty-http-${jetty.version}.jar
lib/jetty-continuation-${jetty.version}.jar
lib/jetty-server-${jetty.version}.jar
lib/jetty-xml-${jetty.version}.jar
lib/jetty-util-${jetty.version}.jar
lib/jetty-io-${jetty.version}.jar

[xml]
# Annotations needs annotations configuration
etc/jetty.xml

[ini-template]
threads.min=10
threads.max=200
threads.timeout=60000
#jetty.host=myhost.com
jetty.dump.start=false
jetty.dump.stop=false

