#!/bin/bash

# Memzo Extracter - Beautiful CSV Data Manager
# Run script for easy access

echo "🚀 Starting Memzo Extracter..."
echo "   A beautiful CSV data management application"
echo ""

# Change to project directory
cd "$(dirname "$0")"

# Run the application
mvn spring-boot:run

echo ""
echo "Application stopped. Thank you for using Memzo Extracter!"
