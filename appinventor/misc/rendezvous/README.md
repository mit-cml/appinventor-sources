

# MIT App Inventor Rendezvous Server

This directory contains the code needed to run the MIT App Inventor
Rendezvous Server version 2.

Version 2 supports both our “legacy” httpd connection to the MIT AI2
Companion and the newer WebRTC based system.


# Installation

This code is designed to run in a docker container. You can build the
needed image simply with:

    docker build -t <imagename> .

Run the resulting image on your server as:

    docker run --restart=always -d -p 80:3000 --name=rendezvous <imagename>

<imagename> is a name you pick for your image. The image expects (and
should create) a docker volume which it mounts in “/data”. An sqlite3
database will be created here which will gather statistics. Keep an
eye on this database as it will grow without bound. You may need to
trim it periodically.

Logs for the running server are in /var/log/supervisor within the
container. These logs are automatically managed (trimmed as needed) so
you need not worry about them. They mostly contain debugging output
which isn’t of much value (and which a newer version may flush
completely).

