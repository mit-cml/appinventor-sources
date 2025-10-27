# ========= Runtime Image =========
FROM eclipse-temurin:11-jre

WORKDIR /appinventor

# Copy already-built App Inventor WAR files
COPY appinventor/appengine/build/war /appinventor/appengine/build/war

# Install Cloud SDK and App Engine Java runtime
RUN apt-get update && \
    apt-get install -y wget unzip curl gnupg && \
    echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] http://packages.cloud.google.com/apt cloud-sdk main" \
      | tee -a /etc/apt/sources.list.d/google-cloud-sdk.list && \
    curl https://packages.cloud.google.com/apt/doc/apt-key.gpg \
      | apt-key --keyring /usr/share/keyrings/cloud.google.gpg add - && \
    apt-get update && \
    apt-get install -y google-cloud-cli google-cloud-cli-app-engine-java && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Add App Engine Java SDK to PATH
ENV PATH="/usr/lib/google-cloud-sdk/bin:${PATH}"

EXPOSE 8888

# Run the App Inventor dev server
CMD ["bash", "/usr/lib/google-cloud-sdk/bin/java_dev_appserver.sh", "--port=8888", "--host=0.0.0.0", "/appinventor/appengine/build/war"]
