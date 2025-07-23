#!/bin/bash

# Linux Package Builder for Memzo Extracter
# This script creates distributable Linux packages using working methods

set -e

# Configuration
APP_NAME="memzo-extracter"
APP_VERSION="1.0.0"
APP_JAR="memzo-extracter-0.0.1-SNAPSHOT.jar"
APP_DISPLAY_NAME="Memzo Extracter"
APP_DESCRIPTION="CSV Data Analysis Tool with Export Capabilities"
MAINTAINER="DevDam <contact@devdam.com>"

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

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
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

# Build TAR.GZ distribution
build_targz() {
    print_info "Building TAR.GZ distribution..."
    
    local dist_dir="dist/${APP_NAME}-${APP_VERSION}"
    
    # Clean and create distribution directory
    rm -rf dist
    mkdir -p "$dist_dir"
    
    # Copy application files
    cp "target/$APP_JAR" "$dist_dir/"
    cp "run-memzo-extracter.sh" "$dist_dir/"
    cp "DISTRIBUTION.md" "$dist_dir/README.md"
    
    # Create install script
    cat > "$dist_dir/install.sh" << 'EOF'
#!/bin/bash
# Simple installer script for Memzo Extracter

INSTALL_DIR="/opt/memzo-extracter"
BIN_LINK="/usr/local/bin/memzo-extracter"

echo "Installing Memzo Extracter..."

# Check for root privileges
if [ "$EUID" -ne 0 ]; then
    echo "Please run as root (use sudo)"
    exit 1
fi

# Create installation directory
mkdir -p "$INSTALL_DIR"

# Copy files
cp memzo-extracter-*.jar "$INSTALL_DIR/"
cp run-memzo-extracter.sh "$INSTALL_DIR/"
chmod +x "$INSTALL_DIR/run-memzo-extracter.sh"

# Create symlink
ln -sf "$INSTALL_DIR/run-memzo-extracter.sh" "$BIN_LINK"

echo "Installation complete!"
echo "Run 'memzo-extracter' from anywhere to start the application"
EOF
    
    chmod +x "$dist_dir/install.sh"
    
    # Create tar.gz
    cd dist
    tar -czf "${APP_NAME}-${APP_VERSION}-linux.tar.gz" "${APP_NAME}-${APP_VERSION}/"
    cd ..
    
    print_success "TAR.GZ package created: dist/${APP_NAME}-${APP_VERSION}-linux.tar.gz"
}

# Build DEB package
build_deb() {
    print_info "Building DEB package..."
    
    # Check if dpkg-deb is available
    if ! command -v dpkg-deb &> /dev/null; then
        print_warning "dpkg-deb not found. Installing dpkg-dev..."
        sudo apt-get update && sudo apt-get install -y dpkg-dev
    fi
    
    local pkg_dir="package/${APP_NAME}"
    
    # Clean and create package structure
    rm -rf package
    mkdir -p "$pkg_dir/DEBIAN"
    mkdir -p "$pkg_dir/usr/local/bin"
    mkdir -p "$pkg_dir/usr/local/share/${APP_NAME}"
    mkdir -p "$pkg_dir/usr/share/applications"
    mkdir -p "$pkg_dir/usr/share/doc/${APP_NAME}"
    
    # Copy application files
    cp "target/$APP_JAR" "$pkg_dir/usr/local/share/${APP_NAME}/"
    
    # Create wrapper script
    cat > "$pkg_dir/usr/local/bin/${APP_NAME}" << EOF
#!/bin/bash
# Memzo Extracter launcher

INSTALL_DIR="/usr/local/share/${APP_NAME}"
JAR_FILE="\$INSTALL_DIR/$APP_JAR"

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java 17+ is required but not found"
    echo "Please install OpenJDK 17 or higher:"
    echo "  sudo apt install openjdk-17-jre"
    exit 1
fi

# Launch the application
exec java -Xmx1G -Dfile.encoding=UTF-8 -Djava.awt.headless=false -jar "\$JAR_FILE" "\$@"
EOF
    
    chmod +x "$pkg_dir/usr/local/bin/${APP_NAME}"
    
    # Create desktop entry
    cat > "$pkg_dir/usr/share/applications/${APP_NAME}.desktop" << EOF
[Desktop Entry]
Name=${APP_DISPLAY_NAME}
Comment=${APP_DESCRIPTION}
Exec=${APP_NAME}
Icon=application-x-executable
Terminal=false
Type=Application
Categories=Office;Utility;Development;
StartupNotify=true
EOF
    
    # Create control file
    cat > "$pkg_dir/DEBIAN/control" << EOF
Package: ${APP_NAME}
Version: ${APP_VERSION}
Section: utils
Priority: optional
Architecture: all
Depends: openjdk-17-jre | openjdk-18-jre | openjdk-19-jre | openjdk-20-jre | openjdk-21-jre
Maintainer: ${MAINTAINER}
Description: ${APP_DISPLAY_NAME}
 ${APP_DESCRIPTION}
 .
 A professional Java application for analyzing CSV data with statistics,
 charts, email management, and export capabilities to CSV and PDF formats.
 Features include data filtering, unique email tracking, and modern UI.
EOF
    
    # Create postinst script
    cat > "$pkg_dir/DEBIAN/postinst" << 'EOF'
#!/bin/bash
# Post-installation script

echo "Memzo Extracter installed successfully!"
echo "You can now run 'memzo-extracter' from the command line"
echo "or find it in your applications menu."

# Update desktop database
if command -v update-desktop-database &> /dev/null; then
    update-desktop-database /usr/share/applications
fi

exit 0
EOF
    
    chmod +x "$pkg_dir/DEBIAN/postinst"
    
    # Create prerm script
    cat > "$pkg_dir/DEBIAN/prerm" << 'EOF'
#!/bin/bash
# Pre-removal script

echo "Removing Memzo Extracter..."
exit 0
EOF
    
    chmod +x "$pkg_dir/DEBIAN/prerm"
    
    # Copy documentation
    cp "DISTRIBUTION.md" "$pkg_dir/usr/share/doc/${APP_NAME}/README"
    
    # Build DEB package
    dpkg-deb --build "$pkg_dir"
    mv "package/${APP_NAME}.deb" "dist/${APP_NAME}_${APP_VERSION}_all.deb"
    
    print_success "DEB package created: dist/${APP_NAME}_${APP_VERSION}_all.deb"
}

# Build RPM package (if rpmbuild is available)
build_rpm() {
    print_info "Building RPM package..."
    
    if ! command -v rpmbuild &> /dev/null; then
        print_warning "rpmbuild not found. Skipping RPM package creation."
        print_info "To install: sudo apt install rpm (Ubuntu/Debian) or dnf install rpm-build (Fedora)"
        return
    fi
    
    # Create RPM build structure
    local rpm_dir="$HOME/rpmbuild"
    mkdir -p "$rpm_dir"/{BUILD,RPMS,SOURCES,SPECS,SRPMS}
    
    # Create source tarball
    local source_dir="${APP_NAME}-${APP_VERSION}"
    mkdir -p "rpm-source/$source_dir"
    cp "target/$APP_JAR" "rpm-source/$source_dir/"
    cp "run-memzo-extracter.sh" "rpm-source/$source_dir/"
    cd rpm-source
    tar -czf "$rpm_dir/SOURCES/${APP_NAME}-${APP_VERSION}.tar.gz" "$source_dir/"
    cd ..
    rm -rf rpm-source
    
    # Create RPM spec file
    cat > "$rpm_dir/SPECS/${APP_NAME}.spec" << EOF
Name:           ${APP_NAME}
Version:        ${APP_VERSION}
Release:        1%{?dist}
Summary:        ${APP_DESCRIPTION}

License:        Proprietary
URL:            https://github.com/damoiskii/memzo-extracter
Source0:        %{name}-%{version}.tar.gz

BuildArch:      noarch
Requires:       java-17-openjdk

%description
${APP_DISPLAY_NAME} is a professional Java application for analyzing CSV data
with statistics, charts, email management, and export capabilities.

%prep
%setup -q

%build
# Nothing to build

%install
mkdir -p %{buildroot}/usr/local/share/%{name}
mkdir -p %{buildroot}/usr/local/bin
mkdir -p %{buildroot}/usr/share/applications

# Install JAR file
cp ${APP_JAR} %{buildroot}/usr/local/share/%{name}/

# Create launcher script
cat > %{buildroot}/usr/local/bin/%{name} << 'LAUNCHER_EOF'
#!/bin/bash
INSTALL_DIR="/usr/local/share/${APP_NAME}"
JAR_FILE="\$INSTALL_DIR/${APP_JAR}"
exec java -Xmx1G -Dfile.encoding=UTF-8 -Djava.awt.headless=false -jar "\$JAR_FILE" "\$@"
LAUNCHER_EOF

chmod +x %{buildroot}/usr/local/bin/%{name}

# Create desktop entry
cat > %{buildroot}/usr/share/applications/%{name}.desktop << 'DESKTOP_EOF'
[Desktop Entry]
Name=${APP_DISPLAY_NAME}
Comment=${APP_DESCRIPTION}
Exec=${APP_NAME}
Icon=application-x-executable
Terminal=false
Type=Application
Categories=Office;Utility;Development;
DESKTOP_EOF

%files
/usr/local/share/%{name}/${APP_JAR}
/usr/local/bin/%{name}
/usr/share/applications/%{name}.desktop

%changelog
* $(date '+%a %b %d %Y') DevDam <contact@devdam.com> - ${APP_VERSION}-1
- Initial package
EOF
    
    # Build RPM
    rpmbuild -ba "$rpm_dir/SPECS/${APP_NAME}.spec"
    
    # Copy resulting RPM
    cp "$rpm_dir/RPMS/noarch/${APP_NAME}-${APP_VERSION}-1."*.rpm "dist/" 2>/dev/null || true
    
    print_success "RPM package created in dist/"
}

# Show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --targz     Build TAR.GZ distribution (default)"
    echo "  --deb       Build DEB package"
    echo "  --rpm       Build RPM package"
    echo "  --all       Build all package types"
    echo "  --help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                  # Build TAR.GZ package"
    echo "  $0 --deb           # Build DEB package"
    echo "  $0 --all           # Build all package types"
}

# Main execution
main() {
    echo ""
    echo "🐧 Memzo Extracter Linux Package Builder"
    echo "========================================"
    
    check_prerequisites
    
    # Create dist directory
    mkdir -p dist
    
    case "${1:-targz}" in
        --targz)
            build_targz
            ;;
        --deb)
            build_deb
            ;;
        --rpm)
            build_rpm
            ;;
        --all)
            build_targz
            build_deb
            build_rpm
            ;;
        --help)
            show_usage
            exit 0
            ;;
        *)
            if [ "$1" = "targz" ] || [ -z "$1" ]; then
                build_targz
            else
                print_error "Unknown option: $1"
                show_usage
                exit 1
            fi
            ;;
    esac
    
    echo ""
    print_success "Package build completed!"
    print_info "Generated packages:"
    ls -la dist/
    echo ""
    print_info "Installation instructions:"
    echo "  TAR.GZ: Extract and run ./install.sh as root"
    echo "  DEB:    sudo dpkg -i dist/${APP_NAME}_${APP_VERSION}_all.deb"
    echo "  RPM:    sudo rpm -i dist/${APP_NAME}-${APP_VERSION}-*.rpm"
}

# Run main function with all arguments
main "$@"
