# ===== Stage 1: Extract tools =====
FROM eclipse-temurin:17-jre-jammy AS builder

RUN apt-get update && apt-get install -y --no-install-recommends \
      curl gnupg ca-certificates && \
    echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] http://packages.cloud.google.com/apt cloud-sdk main" \
      | tee /etc/apt/sources.list.d/google-cloud-sdk.list && \
    curl https://packages.cloud.google.com/apt/doc/apt-key.gpg \
      | apt-key --keyring /usr/share/keyrings/cloud.google.gpg add - && \
    apt-get update && \
    apt-get install -y --no-install-recommends google-cloud-cli-app-engine-java && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /extract/usr/lib/google-cloud-sdk && \
    cp -r /usr/lib/google-cloud-sdk/platform /extract/usr/lib/google-cloud-sdk/platform && \
    cp -r /usr/lib/google-cloud-sdk/bin /extract/usr/lib/google-cloud-sdk/bin

# ===== Stage 2: Final runtime =====
FROM eclipse-temurin:17-jre-jammy

WORKDIR /appinventor

COPY appinventor/appengine/build/war /appinventor/appengine/build/war

# Copy only the App Engine Java runtime + dev server
COPY --from=builder /extract/usr/lib/google-cloud-sdk /usr/lib/google-cloud-sdk

ENV PATH="/usr/lib/google-cloud-sdk/bin:${PATH}"

EXPOSE 8888

CMD ["bash", "-c", "java_dev_appserver.sh --address=0.0.0.0 --port=8888 /appinventor/appengine/build/war"]
