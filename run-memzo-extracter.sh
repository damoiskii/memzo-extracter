#!/bin/bash

# Memzo Extracter Launcher Script for Linux/macOS
# This script provides a simple way to launch the application

set -e

# Configuration
APP_NAME="Memzo Extracter"
JAR_NAME="memzo-extracter-0.0.1-SNAPSHOT.jar"
MIN_JAVA_VERSION=17

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored messages
print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if Java is installed and get version
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        print_info "Please install Java ${MIN_JAVA_VERSION}+ from: https://openjdk.org/install/"
        exit 1
    fi
    
    # Get Java version
    local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    
    # Handle newer Java version format (e.g., "17.0.1" vs "1.8.0")
    if [[ $java_version == "1."* ]]; then
        java_version=$(echo $java_version | cut -d'.' -f2)
    fi
    
    if [ "$java_version" -lt "$MIN_JAVA_VERSION" ]; then
        print_error "Java version $java_version detected. Minimum required: $MIN_JAVA_VERSION"
        print_info "Please upgrade Java from: https://openjdk.org/install/"
        exit 1
    fi
    
    print_success "Java $java_version detected"
}

# Find JAR file
find_jar() {
    local jar_path=""
    
    # Look in current directory first
    if [ -f "$JAR_NAME" ]; then
        jar_path="$JAR_NAME"
    # Look in target directory
    elif [ -f "target/$JAR_NAME" ]; then
        jar_path="target/$JAR_NAME"
    # Look in parent directory
    elif [ -f "../$JAR_NAME" ]; then
        jar_path="../$JAR_NAME"
    else
        print_error "Cannot find $JAR_NAME"
        print_info "Please ensure the JAR file exists in:"
        print_info "  - Current directory"
        print_info "  - target/ directory"
        print_info "  - Parent directory"
        print_info ""
        print_info "To build the JAR file, run: mvn clean package -DskipTests"
        exit 1
    fi
    
    echo "$jar_path"
}

# Main execution
main() {
    echo ""
    echo "🚀 $APP_NAME Launcher"
    echo "========================"
    
    print_info "Checking Java installation..."
    check_java
    
    print_info "Locating application JAR..."
    local jar_path=$(find_jar)
    print_success "Found JAR: $jar_path"
    
    print_info "Starting $APP_NAME..."
    echo ""
    
    # Launch the application with optimal settings
    java -Xmx1G \
         -Dfile.encoding=UTF-8 \
         -Djava.awt.headless=false \
         -jar "$jar_path"
}

# Run the launcher
main "$@"
