#!/bin/bash

# Memzo Extracter - Native Installer Builder
# This script builds native installers for different platforms

echo "🚀 Memzo Extracter - Native Installer Builder"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Java 17+ is available
check_java() {
    print_status "Checking Java version..."
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    
    print_success "Java $JAVA_VERSION detected"
}

# Check if jpackage is available
check_jpackage() {
    print_status "Checking jpackage availability..."
    if ! command -v jpackage &> /dev/null; then
        print_error "jpackage is not available. Make sure you're using a JDK (not JRE) version 17+"
        exit 1
    fi
    print_success "jpackage is available"
}

# Build the application JAR
build_jar() {
    print_status "Building application JAR..."
    mvn clean package -DskipTests
    if [ $? -eq 0 ]; then
        print_success "JAR built successfully"
    else
        print_error "Failed to build JAR"
        exit 1
    fi
}

# Detect the current platform
detect_platform() {
    case "$(uname -s)" in
        Linux*)     PLATFORM=linux;;
        Darwin*)    PLATFORM=mac;;
        MINGW*|CYGWIN*|MSYS*) PLATFORM=windows;;
        *)          PLATFORM=unknown;;
    esac
    print_status "Detected platform: $PLATFORM"
}

# Build native installer
build_installer() {
    print_status "Building native installer for $PLATFORM..."
    
    # Create target/installer directory if it doesn't exist
    mkdir -p target/installer
    
    case $PLATFORM in
        linux)
            build_linux_installer
            ;;
        mac)
            build_macos_installer
            ;;
        windows)
            build_windows_installer
            ;;
        *)
            print_error "Unsupported platform: $PLATFORM"
            exit 1
            ;;
    esac
}

# Build Linux DEB installer using two-step approach
build_linux_installer() {
    print_status "Building DEB package for Linux..."
    
    # Step 1: Create app-image (avoids module resolution issues)
    print_status "Creating application image..."
    jpackage \
        --input target/ \
        --main-jar memzo-extracter-0.0.1-SNAPSHOT-ui.jar \
        --main-class com.devdam.memzo_extracter.ui.ApplicationLauncher \
        --name MemzoExtracter \
        --app-version 1.0.0 \
        --vendor DevDam \
        --type app-image \
        --dest target/installer \
        --java-options "-Dfile.encoding=UTF-8" \
        --java-options "-Djava.awt.headless=false"
    
    if [ $? -eq 0 ]; then
        print_success "App image created successfully"
        
        # Step 2: Create DEB from app-image
        print_status "Creating DEB package from app image..."
        jpackage \
            --app-image target/installer/MemzoExtracter \
            --name MemzoExtracter \
            --app-version 1.0.0 \
            --vendor DevDam \
            --type deb \
            --linux-package-name memzo-extracter \
            --linux-app-category Office \
            --linux-shortcut \
            --linux-menu-group "Office;Utility;" \
            --dest target/installer
        
        if [ $? -eq 0 ]; then
            print_success "DEB package created successfully"
        else
            print_error "Failed to build DEB package"
            exit 1
        fi
    else
        print_error "Failed to build app image"
        exit 1
    fi
}

# Build macOS DMG installer using two-step approach
build_macos_installer() {
    print_status "Building DMG package for macOS..."
    
    # Step 1: Create app-image
    print_status "Creating application image..."
    jpackage \
        --input target/ \
        --main-jar memzo-extracter-0.0.1-SNAPSHOT-ui.jar \
        --main-class com.devdam.memzo_extracter.ui.ApplicationLauncher \
        --name MemzoExtracter \
        --app-version 1.0.0 \
        --vendor DevDam \
        --type app-image \
        --dest target/installer \
        --java-options "-Dfile.encoding=UTF-8" \
        --java-options "-Djava.awt.headless=false"
    
    if [ $? -eq 0 ]; then
        print_success "App image created successfully"
        
        # Step 2: Create DMG from app-image
        print_status "Creating DMG package from app image..."
        jpackage \
            --app-image target/installer/MemzoExtracter.app \
            --name MemzoExtracter \
            --app-version 1.0.0 \
            --vendor DevDam \
            --type dmg \
            --dest target/installer
        
        if [ $? -eq 0 ]; then
            print_success "DMG package created successfully"
        else
            print_error "Failed to build DMG package"
            exit 1
        fi
    else
        print_error "Failed to build app image"
        exit 1
    fi
}

# Build Windows MSI installer using two-step approach
build_windows_installer() {
    print_status "Building MSI package for Windows..."
    
    # Step 1: Create app-image
    print_status "Creating application image..."
    jpackage \
        --input target/ \
        --main-jar memzo-extracter-0.0.1-SNAPSHOT-ui.jar \
        --main-class com.devdam.memzo_extracter.ui.ApplicationLauncher \
        --name MemzoExtracter \
        --app-version 1.0.0 \
        --vendor DevDam \
        --type app-image \
        --dest target/installer \
        --java-options "-Dfile.encoding=UTF-8" \
        --java-options "-Djava.awt.headless=false"
    
    if [ $? -eq 0 ]; then
        print_success "App image created successfully"
        
        # Step 2: Create MSI from app-image
        print_status "Creating MSI package from app image..."
        jpackage \
            --app-image target/installer/MemzoExtracter \
            --name MemzoExtracter \
            --app-version 1.0.0 \
            --vendor DevDam \
            --type msi \
            --win-menu \
            --win-dir-chooser \
            --win-shortcut \
            --win-menu-group DevDam \
            --dest target/installer
        
        if [ $? -eq 0 ]; then
            print_success "MSI package created successfully"
        else
            print_error "Failed to build MSI package"
            exit 1
        fi
    else
        print_error "Failed to build app image"
        exit 1
    fi
}

# Show final results
show_results() {
    print_success "Build completed!"
    echo ""
    print_status "Generated files can be found in:"
    echo "  📁 target/installer/"
    echo ""
    
    if [ -d "target/installer" ]; then
        print_status "Contents:"
        ls -la target/installer/
    fi
    
    echo ""
    print_status "Installation instructions:"
    case $PLATFORM in
        linux)
            echo "  • DEB: sudo dpkg -i target/installer/*.deb"
            echo "  • RPM: sudo rpm -i target/installer/*.rpm"
            ;;
        mac)
            echo "  • DMG: Open the .dmg file and drag the app to Applications"
            ;;
        windows)
            echo "  • MSI: Double-click the .msi file to install"
            ;;
    esac
}

# Main execution
main() {
    check_java
    check_jpackage
    detect_platform
    build_jar
    build_installer
    show_results
}

# Handle command line arguments
case "${1:-}" in
    --help|-h)
        echo "Usage: $0 [options]"
        echo ""
        echo "Options:"
        echo "  --help, -h     Show this help message"
        echo "  --jar-only     Build only the JAR file"
        echo "  --linux-rpm    Build RPM package on Linux"
        echo ""
        echo "Platform-specific builds:"
        echo "  Linux:   Creates .deb package (default) or .rpm with --linux-rpm"
        echo "  macOS:   Creates .dmg package"
        echo "  Windows: Creates .msi installer"
        exit 0
        ;;
    --jar-only)
        check_java
        build_jar
        print_success "JAR build completed. File: target/memzo-extracter-0.0.1-SNAPSHOT-ui.jar"
        exit 0
        ;;
    --linux-rpm)
        if [[ "$(uname -s)" == "Linux"* ]]; then
            check_java
            check_jpackage
            build_jar
            print_status "Building RPM package for Linux..."
            mvn jpackage:jpackage -Plinux-rpm
            show_results
        else
            print_error "RPM packages can only be built on Linux"
            exit 1
        fi
        exit 0
        ;;
    "")
        main
        ;;
    *)
        print_error "Unknown option: $1"
        echo "Use --help for usage information"
        exit 1
        ;;
esac
