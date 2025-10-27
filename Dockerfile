# ========= Runtime Image =========
FROM eclipse-temurin:11-jre

WORKDIR /appinventor

# Copy only the already-built artifacts from GitHub Actions
COPY appinventor/build/war /appinventor/build/war

# Install App Engine SDK (for running the dev server)
RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget https://storage.googleapis.com/appengine-sdks/featured/appengine-java-sdk-1.9.92.zip && \
    unzip appengine-java-sdk-1.9.92.zip -d /opt && \
    ln -s /opt/appengine-java-sdk-1.9.92 /opt/appengine-java-sdk && \
    rm appengine-java-sdk-1.9.92.zip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV PATH="/opt/appengine-java-sdk/bin:${PATH}"

EXPOSE 8888

# Run the MIT App Inventor local server
CMD ["java", "-Xmx1G", "-cp", "/opt/appengine-java-sdk/lib/appengine-tools-api.jar", "com.google.appengine.tools.development.DevAppServerMain", "/appinventor/build/war", "--address=0.0.0.0", "--port=8888"]
