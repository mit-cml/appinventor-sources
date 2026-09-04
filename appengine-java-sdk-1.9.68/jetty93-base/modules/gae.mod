#
# GAE Module
#

[depend]
resources
http
annotations
plus
quickstart
jsp
jstl

[optional]
testMetadataServer

[xml]
etc/gae.xml

[lib]
lib/gae/*.jar

[ini-template]

## Google AppEngine Defaults

## Make the aggregation size the same as the output buffer size
jetty.httpConfig.outputAggregationSize=32768

jetty.httpConfig.headerCacheSize=512


## Override server.ini
jetty.threadPool.minThreads=10
jetty.threadPool.maxThreads=500
jetty.threadPool.idleTimeout=60000
jetty.httpConfig.outputBufferSize=32768
jetty.httpConfig.requestHeaderSize=8192
jetty.httpConfig.responseHeaderSize=8192
jetty.httpConfig.sendServerVersion=true
jetty.httpConfig.sendDateHeader=false
jetty.server.dumpAfterStart=false
jetty.server.dumpBeforeStop=false
jetty.httpConfig.delayDispatchUntilContent=false

