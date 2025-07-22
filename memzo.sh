#!/bin/bash

# Memzo Extracter - Beautiful CSV Data Manager
# Simple JAR Runner

cd "$(dirname "$0")"

JAR_FILE_SPRING="target/memzo-extracter-0.0.1-SNAPSHOT.jar"

echo "🚀 Memzo Extracter - JAR Runner"
echo ""
echo "Choose how to run the application:"
echo "1) Swing UI (Recommended) - Desktop application with modern interface"
echo "2) Spring Boot - Web application (default Spring Boot behavior)"
echo ""
read -p "Enter your choice (1 or 2): " choice

case $choice in
    1)
        echo ""
        echo "🎨 Starting Swing UI Application..."
        java -Dspring.main.class=com.devdam.memzo_extracter.ui.ApplicationLauncher -jar "$JAR_FILE_SPRING"
        ;;
    2)
        echo ""
        echo "🌐 Starting Spring Boot Application..."
        java -jar "$JAR_FILE_SPRING"
        ;;
    *)
        echo "Invalid choice. Starting Swing UI (default)..."
        java -Dspring.main.class=com.devdam.memzo_extracter.ui.ApplicationLauncher -jar "$JAR_FILE_SPRING"
        ;;
esac

echo ""
echo "Application stopped. Thank you for using Memzo Extracter!"
