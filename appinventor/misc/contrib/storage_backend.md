# Storage Back Ends

## Introduction

The MIT App Inventor server storage is backed on Objectify interface, which allows
the server to access Google Cloud Datastore. There are alternatives from unofficial
contrib packages, if you desire back ends other than Objectify. Note that non-Objectify
back end is only available on local development server, given that Google Cloud Service
does not allow wild socket connections.

To enable this feature, build server with additional flag `ant -Denable.contrib=true`.
Follow relevant section below to configure your server.

## PostgreSQL back end

Before you start App Inventor server, make sure PostgreSQL installed and started.
You may refer to instructions on [Ubuntu community](https://help.ubuntu.com/community/PostgreSQL)
, [ArchWiki](https://wiki.archlinux.org/index.php/PostgreSQL), or [PostgreSQL official document](https://www.postgresql.org/docs/).

Open `appengine-web.xml` and uncomment these properties to configure the database connection.

```xml
<!-- Set to "postgrsql" -->
<property name="storage.backend" value="postgresql" />

<!-- JDBC driver URL. You may refer to this link: -->
<!-- https://jdbc.postgresql.org/documentation/head/connect.html -->
<property name="jdbc.url" value="jdbc:postgresql://localhost/appinventor" />

<!-- Login credential -->
<property name="jdbc.user" value="postgres" />
<property name="jdbc.password" value="" />
```
