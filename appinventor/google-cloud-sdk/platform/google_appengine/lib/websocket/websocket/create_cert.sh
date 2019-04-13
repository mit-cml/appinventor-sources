#!/bin/bash

# A good key+cert to test that ssl works.
openssl req -x509 -sha256 -nodes -days 36500 -newkey rsa:2048 \
    -subj '/CN=localhost' -keyout localhost.key -out localhost.crt

# A bad key+cert to test that ssl validation is enforced.
openssl req -x509 -sha256 -nodes -days 36500 -newkey rsa:2048 \
    -subj '/CN=badhost' -keyout badhost.key -out badhost.crt
