FROM alpine:3.21
MAINTAINER Jeffrey I. Schiller <jis@mit.edu>

RUN apk add tini curl nginx openjdk11-jdk zip
RUN apk add tzdata bash

ADD opt /opt
ADD passwd /etc/passwd
ADD nginx.conf /etc/nginx/http.d/default.conf
RUN mkdir -p /opt/appinventor/webapps; ln -s /etc/secrets/appinventor.ini /opt/appinventor/webapps/appinventor.ini
ADD appinventor.war /opt/appinventor/webapps/appinventor.war
ADD appinventor.tpl /opt/appinventor/webapps/appinventor.tpl
ADD starter.jar /opt/appinventor/webapps/starter.jar
RUN ln -s /etc/secrets/authkey /opt/appinventor/webapps/authkey
RUN ln -s /etc/secrets/tokenkey /opt/appinventor/webapps/tokenkey
RUN ln -s /etc/secrets/gallerykey /opt/appinventor/webapps/gallerykey
RUN ln -s /etc/secrets/surveykey /opt/appinventor/webapps/surveykey
RUN chown -R 105 /opt/appinventor
ADD start.sh /etc/start.sh
RUN chmod 755 /etc/start.sh

RUN cd /opt/appinventor; unzip webapps/appinventor.war ode/*.cache.js; zip webapps/appinventor.war -d ode/*.cache.js


EXPOSE 8889

ENTRYPOINT ["/sbin/tini", "--"]
CMD /etc/start.sh

