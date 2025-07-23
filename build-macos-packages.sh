#!/bin/bash

# macOS Package Builder for Memzo Extracter
# This script creates distributable macOS packages using working methods

set -e

# Configuration
APP_NAME="memzo-extracter"
APP_VERSION="1.0.0"
APP_JAR="memzo-extracter-0.0.1-SNAPSHOT.jar"
APP_DISPLAY_NAME="Memzo Extracter"
APP_DESCRIPTION="CSV Data Analysis Tool with Export Capabilities"
BUNDLE_ID="com.devdam.memzo-extracter"
VENDOR="DevDam"
DIST_DIR="dist"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

echo ""
echo "🍎 Memzo Extracter macOS Package Builder"
echo "========================================"

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
    # Check if running on macOS
    if [[ "$OSTYPE" != "darwin"* ]]; then
        print_error "This script must be run on macOS"
        exit 1
    fi
    
    # Check if JAR exists
    if [ ! -f "target/$APP_JAR" ]; then
        print_error "JAR file not found. Please build the application first:"
        echo "  mvn clean package -DskipTests"
        exit 1
    fi
    
    # Check if launcher script exists
    if [ ! -f "run-memzo-extracter.sh" ]; then
        print_error "Launcher script not found: run-memzo-extracter.sh"
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Build macOS App Bundle (.app)
build_app_bundle() {
    print_info "Building macOS App Bundle..."
    
    local app_bundle="$DIST_DIR/$APP_DISPLAY_NAME.app"
    local contents_dir="$app_bundle/Contents"
    local macos_dir="$contents_dir/MacOS"
    local resources_dir="$contents_dir/Resources"
    local java_dir="$contents_dir/Java"
    
    # Create app bundle structure
    rm -rf "$app_bundle"
    mkdir -p "$macos_dir" "$resources_dir" "$java_dir"
    
    # Copy JAR file
    cp "target/$APP_JAR" "$java_dir/"
    
    # Create launcher script
    cat > "$macos_dir/$APP_NAME" << 'EOF'
#!/bin/bash
# macOS launcher for Memzo Extracter

# Get the directory containing this script
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAVA_DIR="$APP_DIR/Java"
JAR_FILE="$JAVA_DIR/memzo-extracter-0.0.1-SNAPSHOT.jar"

# Check if Java is available
if ! command -v java &> /dev/null; then
    osascript -e 'display dialog "Java 17 or later is required to run Memzo Extracter.\n\nPlease install Java from:\nhttps://adoptium.net/" with title "Java Required" buttons {"OK"} default button "OK" with icon stop'
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    osascript -e 'display dialog "Java 17 or later is required.\n\nCurrent Java version: '$JAVA_VERSION'\n\nPlease update Java from:\nhttps://adoptium.net/" with title "Java Update Required" buttons {"OK"} default button "OK" with icon stop'
    exit 1
fi

# Launch the application
cd "$JAVA_DIR"
exec java -jar "$JAR_FILE"
EOF
    
    chmod +x "$macos_dir/$APP_NAME"
    
    # Create Info.plist
    cat > "$contents_dir/Info.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDevelopmentRegion</key>
    <string>en</string>
    <key>CFBundleDisplayName</key>
    <string>$APP_DISPLAY_NAME</string>
    <key>CFBundleExecutable</key>
    <string>$APP_NAME</string>
    <key>CFBundleIconFile</key>
    <string>AppIcon</string>
    <key>CFBundleIdentifier</key>
    <string>$BUNDLE_ID</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>$APP_DISPLAY_NAME</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>$APP_VERSION</string>
    <key>CFBundleVersion</key>
    <string>$APP_VERSION</string>
    <key>LSMinimumSystemVersion</key>
    <string>10.14</string>
    <key>NSHighResolutionCapable</key>
    <true/>
    <key>NSRequiresAquaSystemAppearance</key>
    <false/>
    <key>LSApplicationCategoryType</key>
    <string>public.app-category.productivity</string>
    <key>NSHumanReadableCopyright</key>
    <string>© 2024 $VENDOR</string>
</dict>
</plist>
EOF
    
    # Create a simple icon (placeholder)
    if command -v sips &> /dev/null; then
        # Create a simple colored rectangle as icon
        sips -s format png --out "$resources_dir/AppIcon.png" -c 512 512 -b 512 512 "#4A90E2" /dev/null 2>/dev/null || true
    fi
    
    print_success "App Bundle created: $app_bundle"
}

# Build DMG disk image
build_dmg() {
    print_info "Building DMG disk image..."
    
    local app_bundle="$DIST_DIR/$APP_DISPLAY_NAME.app"
    local dmg_name="$APP_NAME-$APP_VERSION-macos.dmg"
    local temp_dmg="$DIST_DIR/temp.dmg"
    local final_dmg="$DIST_DIR/$dmg_name"
    
    # Check if app bundle exists
    if [ ! -d "$app_bundle" ]; then
        print_error "App bundle not found. Build the app bundle first."
        return 1
    fi
    
    # Create temporary DMG
    rm -f "$temp_dmg" "$final_dmg"
    
    # Calculate size needed (app bundle size + 50MB buffer)
    local app_size=$(du -sm "$app_bundle" | cut -f1)
    local dmg_size=$((app_size + 50))
    
    hdiutil create -size ${dmg_size}m -fs HFS+ -volname "$APP_DISPLAY_NAME" "$temp_dmg"
    
    # Mount the DMG
    local mount_point=$(hdiutil attach "$temp_dmg" | grep "/Volumes" | cut -f3)
    
    # Copy app bundle to DMG
    cp -R "$app_bundle" "$mount_point/"
    
    # Create Applications symlink
    ln -s /Applications "$mount_point/Applications"
    
    # Create a README file
    cat > "$mount_point/README.txt" << EOF
$APP_DISPLAY_NAME v$APP_VERSION

Installation:
1. Drag "$APP_DISPLAY_NAME.app" to the Applications folder
2. Launch from Launchpad or Applications folder

Requirements:
- macOS 10.14 or later
- Java 17 or later

For more information, visit: https://github.com/damoiskii/memzo-extracter
EOF
    
    # Unmount the DMG
    hdiutil detach "$mount_point"
    
    # Convert to compressed read-only DMG
    hdiutil convert "$temp_dmg" -format UDZO -o "$final_dmg"
    rm -f "$temp_dmg"
    
    print_success "DMG created: $final_dmg"
}

# Build PKG installer
build_pkg() {
    print_info "Building PKG installer..."
    
    local app_bundle="$DIST_DIR/$APP_DISPLAY_NAME.app"
    local pkg_name="$APP_NAME-$APP_VERSION-macos.pkg"
    local pkg_path="$DIST_DIR/$pkg_name"
    local temp_dir="$DIST_DIR/pkg-temp"
    
    # Check if app bundle exists
    if [ ! -d "$app_bundle" ]; then
        print_error "App bundle not found. Build the app bundle first."
        return 1
    fi
    
    # Create temporary directory structure
    rm -rf "$temp_dir"
    mkdir -p "$temp_dir/Applications"
    
    # Copy app bundle
    cp -R "$app_bundle" "$temp_dir/Applications/"
    
    # Build the PKG
    pkgbuild --root "$temp_dir" \
             --identifier "$BUNDLE_ID" \
             --version "$APP_VERSION" \
             --install-location "/" \
             "$pkg_path"
    
    # Clean up
    rm -rf "$temp_dir"
    
    print_success "PKG created: $pkg_path"
}

# Main execution
main() {
    check_prerequisites
    
    # Create distribution directory
    mkdir -p "$DIST_DIR"
    
    # Parse command line arguments
    BUILD_APP=false
    BUILD_DMG=false
    BUILD_PKG=false
    BUILD_ALL=false
    
    case "${1:-}" in
        --app)
            BUILD_APP=true
            ;;
        --dmg)
            BUILD_APP=true
            BUILD_DMG=true
            ;;
        --pkg)
            BUILD_APP=true
            BUILD_PKG=true
            ;;
        --all|"")
            BUILD_ALL=true
            ;;
        *)
            echo "Usage: $0 [--app|--dmg|--pkg|--all]"
            echo "  --app: Build .app bundle only"
            echo "  --dmg: Build .app bundle and .dmg disk image"
            echo "  --pkg: Build .app bundle and .pkg installer"
            echo "  --all: Build all formats (default)"
            exit 1
            ;;
    esac
    
    if [ "$BUILD_ALL" = true ]; then
        BUILD_APP=true
        BUILD_DMG=true
        BUILD_PKG=true
    fi
    
    # Build packages
    if [ "$BUILD_APP" = true ]; then
        build_app_bundle
    fi
    
    if [ "$BUILD_DMG" = true ]; then
        build_dmg
    fi
    
    if [ "$BUILD_PKG" = true ]; then
        build_pkg
    fi
    
    print_success "Package build completed!"
    print_info "Generated packages:"
    ls -lah "$DIST_DIR"/*.app "$DIST_DIR"/*.dmg "$DIST_DIR"/*.pkg 2>/dev/null || true
    print_info "Installation instructions:"
    echo "  APP: Drag to Applications folder"
    echo "  DMG: Open and drag app to Applications"
    echo "  PKG: Double-click to install"
}

# Run main function
main "$@"
