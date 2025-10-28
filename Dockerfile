FROM eclipse-temurin:17-jre-jammy

WORKDIR /appinventor

# Copy prebuilt App Inventor WAR files
COPY appinventor/appengine/build/war /appinventor/appengine/build/war

# Install minimal dependencies and Cloud SDK with Java runtime only
RUN apt-get update && apt-get install -y --no-install-recommends \
      curl gnupg ca-certificates && \
    echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] http://packages.cloud.google.com/apt cloud-sdk main" \
      | tee /etc/apt/sources.list.d/google-cloud-sdk.list && \
    curl https://packages.cloud.google.com/apt/doc/apt-key.gpg \
      | apt-key --keyring /usr/share/keyrings/cloud.google.gpg add - && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
      google-cloud-cli google-cloud-cli-app-engine-java && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV PATH="/usr/lib/google-cloud-sdk/bin:${PATH}"

EXPOSE 8888

CMD ["bash", "-c", "java_dev_appserver.sh --address=0.0.0.0 --port=8888 /appinventor/appengine/build/war"]
