#!/bin/bash

# Memzo Extracter - Beautiful CSV Data Manager
# JAR Runner Script

echo "🚀 Starting Memzo Extracter (JAR Version)..."
echo "   A beautiful CSV data management application"
echo ""

# Change to project directory
cd "$(dirname "$0")"

# Check if JAR file exists
JAR_FILE="target/memzo-extracter-0.0.1-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "   Please build the JAR first using: mvn clean package -DskipTests"
    exit 1
fi

# Check Java version
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed or not in PATH"
    echo "   Please install Java 17 or higher"
    exit 1
fi

echo "✅ JAR file found: $JAR_FILE"
echo "✅ Java version:"
java -version
echo ""
echo "🏁 Launching application..."
echo ""

# Run the JAR file with the ApplicationLauncher
java -Dspring.main.class=com.devdam.memzo_extracter.ui.ApplicationLauncher -jar "$JAR_FILE"

echo ""
echo "Application stopped. Thank you for using Memzo Extracter!"
