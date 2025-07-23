#!/bin/bash

# Cross-Platform Package Builder for Memzo Extracter
# This script builds packages for Linux, Windows, and macOS

set -e

# Configuration
APP_NAME="memzo-extracter"
APP_VERSION="1.0.0"
DIST_DIR="dist"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
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

print_header() {
    echo -e "${CYAN}$1${NC}"
}

show_usage() {
    echo "Usage: $0 [options]"
    echo ""
    echo "Options:"
    echo "  --linux         Build Linux packages only (TAR.GZ, DEB, RPM)"
    echo "  --windows       Build Windows packages only (ZIP, NSIS)"
    echo "  --macos         Build macOS packages only (APP, DMG, PKG)"
    echo "  --all           Build all platform packages (default)"
    echo "  --help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --linux      # Build only Linux packages"
    echo "  $0 --all        # Build packages for all platforms"
    echo "  $0              # Same as --all"
}

check_prerequisites() {
    print_info "Checking global prerequisites..."
    
    # Check if JAR exists
    if [ ! -f "target/memzo-extracter-0.0.1-SNAPSHOT.jar" ]; then
        print_error "JAR file not found. Please build the application first:"
        echo "  mvn clean package -DskipTests"
        exit 1
    fi
    
    # Check if required scripts exist
    local missing_scripts=()
    
    if [ ! -f "run-memzo-extracter.sh" ]; then
        missing_scripts+=("run-memzo-extracter.sh")
    fi
    
    if [ ! -f "run-memzo-extracter.bat" ]; then
        missing_scripts+=("run-memzo-extracter.bat")
    fi
    
    if [ ${#missing_scripts[@]} -gt 0 ]; then
        print_error "Missing launcher scripts: ${missing_scripts[*]}"
        exit 1
    fi
    
    print_success "Global prerequisites check passed"
}

build_linux_packages() {
    print_header "🐧 Building Linux Packages"
    
    if [ ! -f "build-linux-packages.sh" ]; then
        print_error "Linux build script not found: build-linux-packages.sh"
        return 1
    fi
    
    if [ ! -x "build-linux-packages.sh" ]; then
        chmod +x build-linux-packages.sh
    fi
    
    ./build-linux-packages.sh --all
    print_success "Linux packages completed"
}

build_windows_packages() {
    print_header "🪟 Building Windows Packages"
    
    # Check for Windows build scripts
    if [ -f "build-windows-packages.ps1" ]; then
        print_info "Using PowerShell script for Windows packages"
        if command -v pwsh &> /dev/null; then
            pwsh -File build-windows-packages.ps1 all
        elif command -v powershell &> /dev/null; then
            powershell -File build-windows-packages.ps1 all
        else
            print_warning "PowerShell not found. Creating Windows packages manually..."
            build_windows_manual
        fi
    elif [ -f "build-windows-packages.bat" ]; then
        print_info "Using batch script for Windows packages"
        if command -v wine &> /dev/null; then
            wine cmd /c build-windows-packages.bat --all
        else
            print_warning "Wine not found. Creating Windows packages manually..."
            build_windows_manual
        fi
    else
        print_warning "No Windows build scripts found. Creating packages manually..."
        build_windows_manual
    fi
    
    print_success "Windows packages completed"
}

build_windows_manual() {
    print_info "Creating Windows ZIP package manually..."
    
    local zip_dir="$DIST_DIR/$APP_NAME-$APP_VERSION-windows"
    
    # Create Windows package directory
    rm -rf "$zip_dir"
    mkdir -p "$zip_dir"
    
    # Copy files
    cp "target/memzo-extracter-0.0.1-SNAPSHOT.jar" "$zip_dir/"
    cp "run-memzo-extracter.bat" "$zip_dir/"
    cp "DISTRIBUTION.md" "$zip_dir/README.md"
    
    # Create Windows installer script
    cat > "$zip_dir/install.bat" << 'EOF'
@echo off
REM Memzo Extracter Windows Installer
echo Installing Memzo Extracter...

REM Create installation directory
set INSTALL_DIR=%ProgramFiles%\MemzoExtracter
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

REM Copy files
copy "memzo-extracter-0.0.1-SNAPSHOT.jar" "%INSTALL_DIR%\"
copy "run-memzo-extracter.bat" "%INSTALL_DIR%\memzo-extracter.bat"

REM Create Start Menu shortcut
set SHORTCUT_DIR=%ProgramData%\Microsoft\Windows\Start Menu\Programs
powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%SHORTCUT_DIR%\Memzo Extracter.lnk'); $Shortcut.TargetPath = '%INSTALL_DIR%\memzo-extracter.bat'; $Shortcut.WorkingDirectory = '%INSTALL_DIR%'; $Shortcut.Description = 'CSV Data Analysis Tool'; $Shortcut.Save()"

echo Installation completed successfully!
echo You can find Memzo Extracter in the Start Menu.
pause
EOF
    
    # Create uninstaller
    cat > "$zip_dir/uninstall.bat" << 'EOF'
@echo off
REM Memzo Extracter Windows Uninstaller
echo Uninstalling Memzo Extracter...

set INSTALL_DIR=%ProgramFiles%\MemzoExtracter
set SHORTCUT_DIR=%ProgramData%\Microsoft\Windows\Start Menu\Programs

REM Remove shortcuts
if exist "%SHORTCUT_DIR%\Memzo Extracter.lnk" del "%SHORTCUT_DIR%\Memzo Extracter.lnk"

REM Remove installation directory
if exist "%INSTALL_DIR%" rmdir /s /q "%INSTALL_DIR%"

echo Uninstallation completed successfully!
pause
EOF
    
    # Create ZIP archive
    if command -v zip &> /dev/null; then
        cd "$DIST_DIR"
        zip -r "$APP_NAME-$APP_VERSION-windows.zip" "$APP_NAME-$APP_VERSION-windows/"
        cd ..
        print_success "Windows ZIP created: $DIST_DIR/$APP_NAME-$APP_VERSION-windows.zip"
    else
        print_warning "zip command not found. Windows directory created but not archived."
        print_info "Windows package directory: $zip_dir"
    fi
}

build_macos_packages() {
    print_header "🍎 Building macOS Packages"
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # Running on macOS
        if [ ! -f "build-macos-packages.sh" ]; then
            print_error "macOS build script not found: build-macos-packages.sh"
            return 1
        fi
        
        if [ ! -x "build-macos-packages.sh" ]; then
            chmod +x build-macos-packages.sh
        fi
        
        ./build-macos-packages.sh --all
    else
        # Cross-platform build for macOS
        print_warning "Not running on macOS. Creating basic macOS package..."
        build_macos_manual
    fi
    
    print_success "macOS packages completed"
}

build_macos_manual() {
    print_info "Creating macOS TAR.GZ package manually..."
    
    local macos_dir="$DIST_DIR/$APP_NAME-$APP_VERSION-macos"
    
    # Create macOS package directory
    rm -rf "$macos_dir"
    mkdir -p "$macos_dir"
    
    # Copy files
    cp "target/memzo-extracter-0.0.1-SNAPSHOT.jar" "$macos_dir/"
    cp "run-memzo-extracter.sh" "$macos_dir/"
    cp "DISTRIBUTION.md" "$macos_dir/README.md"
    
    # Create macOS installer script
    cat > "$macos_dir/install.sh" << 'EOF'
#!/bin/bash
# Memzo Extracter macOS Installer

echo "Installing Memzo Extracter..."

# Installation directory
INSTALL_DIR="/Applications/MemzoExtracter"

# Create installation directory
sudo mkdir -p "$INSTALL_DIR"

# Copy files
sudo cp memzo-extracter-0.0.1-SNAPSHOT.jar "$INSTALL_DIR/"
sudo cp run-memzo-extracter.sh "$INSTALL_DIR/memzo-extracter"
sudo chmod +x "$INSTALL_DIR/memzo-extracter"

# Create symlink for command line access
sudo ln -sf "$INSTALL_DIR/memzo-extracter" /usr/local/bin/memzo-extracter

echo "Installation completed successfully!"
echo "You can run Memzo Extracter from the command line with: memzo-extracter"
EOF
    
    chmod +x "$macos_dir/install.sh"
    
    # Create TAR.GZ archive
    cd "$DIST_DIR"
    tar -czf "$APP_NAME-$APP_VERSION-macos.tar.gz" "$APP_NAME-$APP_VERSION-macos/"
    cd ..
    
    print_success "macOS TAR.GZ created: $DIST_DIR/$APP_NAME-$APP_VERSION-macos.tar.gz"
}

show_summary() {
    print_header "📦 Package Build Summary"
    print_info "Generated packages in $DIST_DIR/:"
    
    if [ -d "$DIST_DIR" ]; then
        # List all package files with sizes
        find "$DIST_DIR" -name "*.tar.gz" -o -name "*.zip" -o -name "*.deb" -o -name "*.rpm" -o -name "*.dmg" -o -name "*.pkg" -o -name "*.exe" -o -name "*.app" | while read -r file; do
            if [ -f "$file" ]; then
                size=$(du -h "$file" | cut -f1)
                echo "  📁 $(basename "$file") ($size)"
            elif [ -d "$file" ]; then
                echo "  📂 $(basename "$file")/ (directory)"
            fi
        done
    else
        print_warning "No packages found in $DIST_DIR/"
    fi
    
    echo ""
    print_info "Platform-specific installation instructions:"
    echo "  🐧 Linux:"
    echo "    TAR.GZ: tar -xzf file.tar.gz && cd dir && sudo ./install.sh"
    echo "    DEB: sudo dpkg -i file.deb"
    echo "    RPM: sudo rpm -i file.rpm"
    echo ""
    echo "  🪟 Windows:"
    echo "    ZIP: Extract and run install.bat as Administrator"
    echo "    EXE: Double-click to install"
    echo ""
    echo "  🍎 macOS:"
    echo "    APP: Drag to Applications folder"
    echo "    DMG: Open and drag app to Applications"
    echo "    PKG: Double-click to install"
    echo "    TAR.GZ: Extract and run ./install.sh"
}

main() {
    echo ""
    print_header "🚀 Memzo Extracter Cross-Platform Package Builder"
    print_header "=================================================="
    
    # Parse command line arguments
    BUILD_LINUX=false
    BUILD_WINDOWS=false
    BUILD_MACOS=false
    BUILD_ALL=false
    
    case "${1:-}" in
        --linux)
            BUILD_LINUX=true
            ;;
        --windows)
            BUILD_WINDOWS=true
            ;;
        --macos)
            BUILD_MACOS=true
            ;;
        --all|"")
            BUILD_ALL=true
            ;;
        --help|-h)
            show_usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
    
    if [ "$BUILD_ALL" = true ]; then
        BUILD_LINUX=true
        BUILD_WINDOWS=true
        BUILD_MACOS=true
    fi
    
    # Check prerequisites
    check_prerequisites
    
    # Create distribution directory
    mkdir -p "$DIST_DIR"
    
    # Build packages
    local build_count=0
    
    if [ "$BUILD_LINUX" = true ]; then
        build_linux_packages
        ((build_count++))
    fi
    
    if [ "$BUILD_WINDOWS" = true ]; then
        build_windows_packages
        ((build_count++))
    fi
    
    if [ "$BUILD_MACOS" = true ]; then
        build_macos_packages
        ((build_count++))
    fi
    
    if [ $build_count -eq 0 ]; then
        print_error "No build targets specified"
        show_usage
        exit 1
    fi
    
    # Show summary
    show_summary
    
    print_success "Cross-platform package build completed! 🎉"
}

# Run main function
main "$@"
