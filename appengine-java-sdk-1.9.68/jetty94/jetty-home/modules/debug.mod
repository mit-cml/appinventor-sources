DO NOT EDIT - See: https://www.eclipse.org/jetty/documentation/current/startup-modules.html

[description]
Enables the DebugListener to generate additional 
logging regarding detailed request handling events.
Renames threads to include request URI.

[tags]
debug

[depend]
deploy

[files]
logs/

[xml]
etc/jetty-debug.xml

[ini-template]

## How many days to retain old log files
# jetty.debug.retainDays=14

## Timezone of the log entries
# jetty.debug.timezone=GMT

## Show Request/Response headers
# jetty.debug.showHeaders=true

## Rename threads while in context scope
# jetty.debug.renameThread=false

## Dump context as deployed
# jetty.debug.dumpContext=true
