#!/bin/bash

# Memzo Extracter - Beautiful CSV Data Manager
# Development run script for easy access

echo "🚀 Starting Memzo Extracter (Development Mode)..."
echo "   A beautiful CSV data management application"
echo ""

# Change to project directory
cd "$(dirname "$0")"

# Run the application using Maven exec plugin
mvn exec:java -Dexec.mainClass="com.devdam.memzo_extracter.ui.ApplicationLauncher"

echo ""
echo "Application stopped. Thank you for using Memzo Extracter!"
