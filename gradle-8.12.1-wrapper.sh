#!/bin/bash
# Custom wrapper to force Gradle 8.12.1 usage for GitHub Actions

# Download Gradle 8.12.1 if not present
if [ ! -d "/tmp/gradle-8.12.1" ]; then
    echo "Downloading Gradle 8.12.1..."
    rm -rf /tmp/gradle-8.12.1 || true
    rm -rf /tmp/gradle.zip || true
    wget -q https://services.gradle.org/distributions/gradle-8.12.1-bin.zip -O /tmp/gradle.zip
    unzip -qq /tmp/gradle.zip -d /tmp/
fi

# Set environment
export GRADLE_HOME=/tmp/gradle-8.12.1
export PATH=$GRADLE_HOME/bin:$PATH

# Run gradle with 8.12.1
exec /tmp/gradle-8.12.1/bin/gradle "$@"