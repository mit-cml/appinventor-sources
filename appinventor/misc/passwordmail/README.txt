This directory contains the code used to send out password reminders
from the local authentication code.

When a person selects the “Mail me a password link” the App Engine
service performs a REST POST request to a server (at MIT). The code in
getmail.fcgi receives that request and queues it using the RabbitMQ
AMQP server on the “passmail” queue. The request itself is sent in a
Protocol Buffer. The POST itself is sent via SSL and includes a
password. This password is known to this code and to the App Engine
service. This isn’t super secure, but its purpose is to just prevent
people who discover the REST interface from trivially using it to send
annoying spam.

The mailsender.py program listens for messages on the passmail queue
and turns each one into the appropriate email message.

To use this code you need Python 2.7, The Bottle web framework
(included). A running RabbitMQ server (available in most Linux
distributions) and the Python “pika” package (to use RabbitMQ). You
will also need the Google Protocol Buffers package. Note: It should be
pretty easy to just use jsonlib if you don’t want to mess with
Protocol Buffers.
