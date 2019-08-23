# Contrib Features

## Introduction

MIT App Inventor provides third-party contributions that are not officially maintained.
These features are not compiled by default. Developers are free to include these features
by additional flag `ant -Denable.contrib=true`. By enabling this flag, you have to
acknowledge the risk of oudated code and lacking official support.

To develop contrib features, the code is located in `com.google.appinventor.contrib.*`
namespace, in which Java sources are compiled only when `-Denable.contrib=true`
flag is present. Make sure you confiture `build.xml` correctly if your code requires
additional JAR dependency.

## Supported Features

* PostgreSQL storage back end

   This provides the ability to store data on PostgreSQL database, instead of Google
   Cloud Service. This feature only works in local development server.
