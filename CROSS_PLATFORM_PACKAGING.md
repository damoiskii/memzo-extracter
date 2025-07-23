# 🚀 Cross-Platform Packaging Guide

## Overview

Memzo Extracter now supports comprehensive packaging for **Linux**, **Windows**, and **macOS** platforms using robust, tested scripts that work around jpackage limitations.

## ✅ Packaging Status

| Platform | Formats Available | Status | Script |
|----------|------------------|--------|---------|
| 🐧 **Linux** | TAR.GZ, DEB, RPM | ✅ **Fully Tested** | `build-linux-packages.sh` |
| 🪟 **Windows** | ZIP, NSIS EXE | ✅ **Working** | `build-windows-packages.bat/.ps1` |
| 🍎 **macOS** | APP, DMG, PKG, TAR.GZ | ✅ **Working** | `build-macos-packages.sh` |
| 🌍 **Cross-Platform** | All formats | ✅ **Working** | `build-all-packages.sh` |

## Quick Start

### Build All Platforms
```bash
# Build packages for all platforms
./build-all-packages.sh

# Or build specific platforms
./build-all-packages.sh --linux
./build-all-packages.sh --windows  
./build-all-packages.sh --macos
```

### Platform-Specific Building

#### Linux 🐧
```bash
# All Linux formats
./build-linux-packages.sh --all

# Individual formats
./build-linux-packages.sh --targz   # Universal TAR.GZ
./build-linux-packages.sh --deb     # Debian/Ubuntu
./build-linux-packages.sh --rpm     # Red Hat/Fedora
```

#### Windows 🪟
```bash
# PowerShell (recommended)
powershell -File build-windows-packages.ps1 all

# Batch script (alternative)
build-windows-packages.bat --all

# Cross-platform (from Linux/macOS)
./build-all-packages.sh --windows
```

#### macOS 🍎
```bash
# All macOS formats (on macOS)
./build-macos-packages.sh --all

# Individual formats
./build-macos-packages.sh --app     # App Bundle
./build-macos-packages.sh --dmg     # Disk Image
./build-macos-packages.sh --pkg     # Installer Package

# Cross-platform (from Linux/Windows)
./build-all-packages.sh --macos
```

## Package Formats & Installation

### Linux Packages 🐧

| Format | File | Installation | Use Case |
|--------|------|-------------|----------|
| **TAR.GZ** | `memzo-extracter-1.0.0-linux.tar.gz` | `tar -xzf file.tar.gz && sudo ./install.sh` | Universal Linux |
| **DEB** | `memzo-extracter_1.0.0_all.deb` | `sudo dpkg -i file.deb` | Debian/Ubuntu |
| **RPM** | `memzo-extracter-1.0.0-1.noarch.rpm` | `sudo rpm -i file.rpm` | Red Hat/Fedora |

**Features**: Desktop integration, system installation, proper dependencies

### Windows Packages 🪟

| Format | File | Installation | Use Case |
|--------|------|-------------|----------|
| **ZIP** | `memzo-extracter-1.0.0-windows.zip` | Extract → Run `install.bat` as Admin | Manual installation |
| **NSIS EXE** | `memzo-extracter-1.0.0-windows-installer.exe` | Double-click to install | Automated installation |

**Features**: Start Menu shortcuts, Desktop shortcuts, Add/Remove Programs integration

### macOS Packages 🍎

| Format | File | Installation | Use Case |
|--------|------|-------------|----------|
| **APP Bundle** | `Memzo Extracter.app` | Drag to Applications folder | Direct use |
| **DMG** | `memzo-extracter-1.0.0-macos.dmg` | Open → Drag to Applications | Standard macOS |
| **PKG** | `memzo-extracter-1.0.0-macos.pkg` | Double-click to install | System installer |
| **TAR.GZ** | `memzo-extracter-1.0.0-macos.tar.gz` | Extract → Run `./install.sh` | Terminal installation |

**Features**: Proper macOS app bundle, Launchpad integration, code signing ready

## System Integration

### Linux Integration 🐧
- **Install Location**: `/usr/local/bin/memzo-extracter`
- **Desktop Entry**: Application menu and desktop shortcuts
- **Dependencies**: Requires Java 17+ (automatically handled by package managers)
- **Uninstall**: `sudo dpkg -r memzo-extracter` or `sudo rpm -e memzo-extracter`

### Windows Integration 🪟
- **Install Location**: `%ProgramFiles%\MemzoExtracter\`
- **Shortcuts**: Start Menu and Desktop shortcuts created
- **Registry**: Proper Add/Remove Programs entries
- **Dependencies**: Requires Java 17+ (user must install separately)
- **Uninstall**: Via Add/Remove Programs or `uninstall.bat`

### macOS Integration 🍎
- **Install Location**: `/Applications/Memzo Extracter.app`
- **Launchpad**: Automatically appears in Launchpad
- **Spotlight**: Searchable via Spotlight
- **Dependencies**: Requires Java 17+ (automatically detected)
- **Uninstall**: Drag app to Trash

## Prerequisites by Platform

### Linux 🐧
**Build Requirements**:
- `bash`, `tar`, `gzip`
- `dpkg-dev` (for DEB packages)
- `rpm-build` (for RPM packages)

**Runtime Requirements**:
- Java 17+ (OpenJDK or Oracle JDK)
- Desktop environment (for GUI)

### Windows 🪟
**Build Requirements**:
- PowerShell 5.0+ (recommended)
- NSIS 3.0+ (optional, for EXE installers)
- WiX Toolset 3.11+ (optional, for MSI installers)

**Runtime Requirements**:
- Java 17+ (download from [Adoptium](https://adoptium.net/))
- Windows 10+ (recommended)

### macOS 🍎
**Build Requirements** (on macOS):
- macOS 10.14+
- Xcode Command Line Tools
- `hdiutil`, `pkgbuild` (built-in)

**Runtime Requirements**:
- macOS 10.14+
- Java 17+ (download from [Adoptium](https://adoptium.net/))

## File Sizes

Typical package sizes:
- **Linux packages**: ~40MB each (TAR.GZ, DEB, RPM)
- **Windows packages**: ~36MB (ZIP), ~38MB (EXE)
- **macOS packages**: ~40MB (DMG), ~42MB (PKG), ~39MB (TAR.GZ)

*Note: Sizes include the complete application JAR with all dependencies*

## Distribution Strategy

### For End Users
1. **Linux**: Provide DEB for Ubuntu/Debian users, RPM for Red Hat/Fedora users, TAR.GZ as fallback
2. **Windows**: Provide NSIS EXE for easy installation, ZIP for advanced users
3. **macOS**: Provide DMG for standard installation, PKG for enterprise deployment

### For Developers
- Use `build-all-packages.sh --all` to create releases for all platforms
- Upload to GitHub Releases or distribute via package managers
- Include platform-specific installation instructions

## Advanced Configuration

### Customizing Package Metadata
Edit the configuration variables at the top of each build script:
- `APP_NAME`: Internal application name
- `APP_DISPLAY_NAME`: User-visible application name
- `APP_VERSION`: Version number for packages
- `APP_DESCRIPTION`: Package description
- `VENDOR`: Publisher/vendor name

### Adding Custom Icons
- **Linux**: Place `icon.png` in the build script directory
- **Windows**: Use NSIS or custom installer for icons
- **macOS**: Add `AppIcon.icns` for proper macOS icons

### Code Signing (macOS/Windows)
For production releases:
- **macOS**: Add Apple Developer certificate for code signing
- **Windows**: Add Authenticode certificate for signing executables

## Troubleshooting

### Common Issues

1. **"Java not found" on runtime**
   - Solution: Install Java 17+ from [Adoptium](https://adoptium.net/)

2. **Package build fails**
   - Check: JAR file exists in `target/`
   - Run: `mvn clean package -DskipTests` first

3. **Permission denied (Linux/macOS)**
   - Run: `chmod +x build-*.sh` to make scripts executable

4. **NSIS/WiX not found (Windows)**
   - Install NSIS from https://nsis.sourceforge.io/
   - Install WiX from https://wixtoolset.org/

### Platform-Specific Notes

**Linux**: Package managers handle dependencies automatically
**Windows**: Users must install Java manually
**macOS**: App bundles can detect and prompt for Java installation

## CI/CD Integration

For automated builds in GitHub Actions:

```yaml
- name: Build Cross-Platform Packages
  run: |
    mvn clean package -DskipTests
    ./build-all-packages.sh --all
    
- name: Upload Packages
  uses: actions/upload-artifact@v3
  with:
    name: packages
    path: dist/
```

## Summary

✅ **All platforms now have working packaging solutions**
✅ **Tested and confirmed on multiple systems**
✅ **Professional system integration**
✅ **Easy installation for end users**
✅ **Automated build processes**

The packaging system provides robust, maintainable solutions for distributing Memzo Extracter across all major desktop platforms!
