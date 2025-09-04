# Use a base image with Ubuntu
FROM mcr.microsoft.com/vscode/devcontainers/base:ubuntu

# Install Java 8, curl, and Python (required for Google Cloud SDK)
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk curl python3 python3-pip && \
    apt-get clean;

# Set JAVA_HOME for Java 8
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $JAVA_HOME/bin:$PATH

# Install Google Cloud SDK
RUN curl -sSL https://sdk.cloud.google.com | bash && \
    /root/google-cloud-sdk/install.sh && \
    /root/google-cloud-sdk/bin/gcloud components install beta

# Add gcloud to PATH
ENV PATH $PATH:/root/google-cloud-sdk/bin

# Optional: Verify installations
RUN java -version && gcloud --version

# Set default shell to bash
SHELL ["/bin/bash", "-c"]
