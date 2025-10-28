# Use a lightweight Java 17 runtime
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /appinventor

# Copy prebuilt App Inventor WAR files
COPY appinventor/appengine/build/war /appinventor/appengine/build/war

# Install only required dependencies and App Engine Java SDK (not full gcloud)
RUN apt-get update && apt-get install -y --no-install-recommends \
      wget unzip ca-certificates && \
    wget -q https://storage.googleapis.com/appengine-sdks/featured/appengine-java-sdk-1.9.92.zip && \
    unzip appengine-java-sdk-1.9.92.zip -d /opt && \
    rm appengine-java-sdk-1.9.92.zip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Add App Engine SDK to PATH
ENV PATH="/opt/appengine-java-sdk-1.9.92/bin:${PATH}"

EXPOSE 8888

# Run the App Inventor dev server
CMD ["bash", "-c", "dev_appserver.sh --address=0.0.0.0 --port=8888 /appinventor/appengine/build/war"]
