DO NOT EDIT - See: https://www.eclipse.org/jetty/documentation/current/startup-modules.html

[description]
Enables GCloud Datastore API and implementation

[tags]
3rdparty
gcloud

[depends]
gcloud
jcl-slf4j
jul-impl


[files]
maven://com.fasterxml.jackson.core/jackson-core/2.1.3|lib/gcloud/jackson-core-2.1.3.jar
maven://com.google.api-client/google-api-client/1.21.0|lib/gcloud/google-api-client-1.21.0.jar
maven://com.google.api.grpc/grpc-google-common-protos/0.1.0|lib/gcloud/grpc-google-common-protos-0.1.0.jar
maven://com.google.api.grpc/proto-google-common-protos/0.1.9|lib/gcloud/proto-google-common-protos-0.1.9.jar
maven://com.google.api.grpc/proto-google-iam-v1/0.1.9|lib/gcloud/proto-google-iam-v1-0.1.9.jar
maven://com.google.api/api-common/1.0.0|lib/gcloud/api-common-1.0.0.jar
maven://com.google.api/gax/1.0.0|lib/gcloud/gax-1.0.0.jar
maven://com.google.auth/google-auth-library-credentials/0.6.1|lib/gcloud/google-auth-library-credentials-0.6.1.jar
maven://com.google.auth/google-auth-library-oauth2-http/0.6.1|lib/gcloud/google-auth-library-oauth2-http-0.6.1.jar
maven://com.google.auto.value/auto-value/1.1|lib/gcloud/auto-value-1.1.jar
maven://com.google.cloud.datastore/datastore-v1-proto-client/1.3.0|lib/gcloud/datastore-v1-proto-client-1.3.0.jar
maven://com.google.cloud.datastore/datastore-v1-protos/1.3.0|lib/gcloud/datastore-v1-protos-1.3.0.jar
maven://com.google.cloud/google-cloud-core-http/1.0.0|lib/gcloud/google-cloud-core-http-1.0.0.jar
maven://com.google.cloud/google-cloud-core/1.0.0|lib/gcloud/google-cloud-core-1.0.0.jar
maven://com.google.cloud/google-cloud-datastore/1.0.0|lib/gcloud/google-cloud-datastore-1.0.0.jar
maven://com.google.code.findbugs/jsr305/3.0.0|lib/gcloud/jsr305-3.0.0.jar
maven://com.google.code.gson/gson/2.7|lib/gcloud/gson-2.7.jar
maven://com.google.guava/guava/19.0|lib/gcloud/guava-19.0.jar
maven://com.google.http-client/google-http-client-appengine/1.21.0|lib/gcloud/google-http-client-appengine-1.21.0.jar
maven://com.google.http-client/google-http-client-jackson2/1.19.0|lib/gcloud/google-http-client-jackson2-1.19.0.jar
maven://com.google.http-client/google-http-client-jackson/1.21.0|lib/gcloud/google-http-client-jackson-1.21.0.jar
maven://com.google.http-client/google-http-client-protobuf/1.20.0|lib/gcloud/google-http-client-protobuf-1.20.0.jar
maven://com.google.http-client/google-http-client/1.21.0|lib/gcloud/google-http-client-1.21.0.jar
maven://com.google.oauth-client/google-oauth-client/1.21.0|lib/gcloud/google-oauth-client-1.21.0.jar
maven://com.google.protobuf.nano/protobuf-javanano/3.0.0-alpha-5|lib/gcloud/protobuf-javanano-3.0.0-alpha-5.jar
maven://com.google.protobuf/protobuf-java-util/3.2.0|lib/gcloud/protobuf-java-util-3.2.0.jar
maven://com.google.protobuf/protobuf-java/3.0.0|lib/gcloud/protobuf-java-3.0.0.jar
maven://com.google.protobuf/protobuf-lite/3.0.1|lib/gcloud/protobuf-lite-3.0.1.jar
maven://com.squareup.okhttp/okhttp/2.5.0|lib/gcloud/okhttp-2.5.0.jar
maven://com.squareup.okio/okio/1.6.0|lib/gcloud/okio-1.6.0.jar
maven://commons-codec/commons-codec/1.3|lib/gcloud/commons-codec-1.3.jar
maven://commons-logging/commons-logging/1.1.1|lib/gcloud/commons-logging-1.1.1.jar
maven://io.grpc/grpc-all/1.0.1|lib/gcloud/grpc-all-1.0.1.jar
maven://io.grpc/grpc-auth/1.0.1|lib/gcloud/grpc-auth-1.0.1.jar
maven://io.grpc/grpc-context/1.0.1|lib/gcloud/grpc-context-1.0.1.jar
maven://io.grpc/grpc-core/1.0.1|lib/gcloud/grpc-core-1.0.1.jar
maven://io.grpc/grpc-netty/1.0.1|lib/gcloud/grpc-netty-1.0.1.jar
maven://io.grpc/grpc-okhttp/1.0.1|lib/gcloud/grpc-okhttp-1.0.1.jar
maven://io.grpc/grpc-protobuf-lite/1.0.1|lib/gcloud/grpc-protobuf-lite-1.0.1.jar
maven://io.grpc/grpc-protobuf-nano/1.0.1|lib/gcloud/grpc-protobuf-nano-1.0.1.jar
maven://io.grpc/grpc-protobuf/1.0.1|lib/gcloud/grpc-protobuf-1.0.1.jar
maven://io.grpc/grpc-stub/1.0.1|lib/gcloud/grpc-stub-1.0.1.jar
maven://io.netty/netty-buffer/4.1.3.Final|lib/gcloud/netty-buffer-4.1.3.Final.jar
maven://io.netty/netty-codec-http2/4.1.3.Final|lib/gcloud/netty-codec-http2-4.1.3.Final.jar
maven://io.netty/netty-codec-http/4.1.3.Final|lib/gcloud/netty-codec-http-4.1.3.Final.jar
maven://io.netty/netty-codec/4.1.3.Final|lib/gcloud/netty-codec-4.1.3.Final.jar
maven://io.netty/netty-common/4.1.3.Final|lib/gcloud/netty-common-4.1.3.Final.jar
maven://io.netty/netty-handler/4.1.3.Final|lib/gcloud/netty-handler-4.1.3.Final.jar
maven://io.netty/netty-resolver/4.1.3.Final|lib/gcloud/netty-resolver-4.1.3.Final.jar
maven://io.netty/netty-transport/4.1.3.Final|lib/gcloud/netty-transport-4.1.3.Final.jar
maven://joda-time/joda-time/2.9.2|lib/gcloud/joda-time-2.9.2.jar
maven://org.apache.httpcomponents/httpclient/4.0.1|lib/gcloud/httpclient-4.0.1.jar
maven://org.apache.httpcomponents/httpcore/4.0.1|lib/gcloud/httpcore-4.0.1.jar
maven://org.codehaus.jackson/jackson-core-asl/1.9.11|lib/gcloud/jackson-core-asl-1.9.11.jar
maven://org.json/json/20160810|lib/gcloud/json-20160810.jar
maven://org.threeten/threetenbp/1.3.3|lib/gcloud/threetenbp-1.3.3.jar

