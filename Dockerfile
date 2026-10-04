# Stage 1: Install Google Cloud SDK
FROM eclipse-temurin:17-jre-jammy AS gcloud

RUN apt-get update && apt-get install -y --no-install-recommends \
      bash curl unzip ca-certificates python3-minimal lsb-release locales && \
    ln -sf /usr/bin/python3 /usr/bin/python && \
    curl -sSL https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-cli-474.0.0-linux-x86_64.tar.gz \
      | tar -xz -C /opt && \
    bash /opt/google-cloud-sdk/install.sh --quiet --usage-reporting=false --command-completion=false --path-update=false && \
    # Only install App Engine Java local dev tools \
    /opt/google-cloud-sdk/bin/gcloud components install app-engine-java --quiet --verbosity=error || true && \
    # Strip heavy unused parts \
    rm -rf /opt/google-cloud-sdk/.install/.backup \
           /opt/google-cloud-sdk/help \
           /opt/google-cloud-sdk/.backup \
           /opt/google-cloud-sdk/.install/.download \
           /opt/google-cloud-sdk/platform/bq \
           /opt/google-cloud-sdk/platform/gsutil \
           /opt/google-cloud-sdk/platform/pubsub-emulator \
           /opt/google-cloud-sdk/.kube \
           /opt/google-cloud-sdk/.github \
           /opt/google-cloud-sdk/anthoscli \
           /var/lib/apt/lists/* && \
    apt-get clean && rm -rf /tmp/* /var/tmp/*

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy

# Copy only necessary parts of gcloud SDK (App Engine runtime + scripts)
COPY --from=gcloud /opt/google-cloud-sdk /opt/google-cloud-sdk
COPY --from=gcloud /usr/lib/python3.10 /usr/lib/python3.10
COPY --from=gcloud /usr/bin/python3 /usr/bin/python3
COPY --from=gcloud /usr/bin/python /usr/bin/python

COPY appinventor/appengine/build/war /appinventor/appengine/build/war

ENV PATH="/opt/google-cloud-sdk/bin:${PATH}"

WORKDIR /appinventor

EXPOSE 8888

CMD ["bash", "-c", "java_dev_appserver.sh --address=0.0.0.0 --port=8888 /appinventor/appengine/build/war"]
