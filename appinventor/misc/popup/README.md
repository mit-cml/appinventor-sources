# Popup.html

The new "Legacy" mode uses a popup window to communicate between the
browser and the MIT AI2 Companion running on a device. This is done to
work around browser restrictions that do not permit non-https
connections from an https loaded page.

The popup itself is loaded by App Inventor by opening a popup window
with a URL that points to /_proxy on the Companion's builtin web
server.

The Companion's web server in turn downloads the contents of this page
from the Rendezvous server where it is stored in an SQLite
database. This database permits different versions to be loaded for
different Companion versions, with a default as well.

As of this writing, we have not release our Rust based Rendezvous
server in our open source. Only this version has the functionality for
providing the _proxy page.

However other "builders" can include the popup HTML directly in either
the AppInvHTTP module or the PhoneStatus component.

popup.html in this directory provides the actual HTML we are currently
using.
