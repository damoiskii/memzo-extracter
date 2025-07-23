# Memzo Extracter - Packaging Status

## ⚠️ Native Installers Status

**jpackage-based native installers (exe, dmg, msi) are NOT WORKING** due to OpenJDK module system limitations.

## ✅ Working Packaging Solutions (Tested & Confirmed)

### 1. Linux Native Packages ✅ **FULLY TESTED**
- **Script**: `build-linux-packages.sh`
- **Formats**: TAR.GZ, DEB, RPM
- **Status**: ✅ Successfully tested on MX Linux (Debian-based)
- **Features**: System integration with desktop files and proper installation

### 2. JAR Distribution ✅ **CROSS-PLATFORM**
- **Guide**: See `DISTRIBUTION.md`
- **Compatibility**: Windows, Linux, macOS with Java 17+
- **Features**: Launcher scripts for all platforms

## Current Status Summary

- ✅ **Linux Packages**: TAR.GZ, DEB, RPM - **FULLY WORKING**
- ✅ **JAR Distribution**: Cross-platform - **FULLY WORKING**  
- ❌ **Native Installers**: jpackage exe/dmg/msi - **NOT WORKING**

## ✅ Linux Package Building (Tested & Working)

### Quick Usage

```bash
# Build all package formats
./build-linux-packages.sh --all

# Build specific formats
./build-linux-packages.sh --targz   # Universal Linux TAR.GZ
./build-linux-packages.sh --deb     # Debian/Ubuntu DEB package  
./build-linux-packages.sh --rpm     # Red Hat/CentOS/Fedora RPM package
```

### Test Results ✅

**Successfully tested on MX Linux (Debian-based)**:
- ✅ TAR.GZ: `dist/memzo-extracter-1.0.0-linux.tar.gz` (39 MB)
- ✅ DEB: `dist/memzo-extracter_1.0.0_all.deb` (39 MB)  
- ✅ RPM: `dist/memzo-extracter-1.0.0-1.noarch.rpm` (39 MB)

**Successfully created cross-platform packages**:
- ✅ Windows: `dist/memzo-extracter-1.0.0-windows.zip` (39 MB)
- ✅ macOS: `dist/memzo-extracter-1.0.0-macos.tar.gz` (39 MB)

### Installation Instructions

```bash
# TAR.GZ (universal Linux)
tar -xzf dist/memzo-extracter-1.0.0-linux.tar.gz
cd memzo-extracter-1.0.0
sudo ./install.sh

# DEB (Debian/Ubuntu)
sudo dpkg -i dist/memzo-extracter_1.0.0_all.deb

# RPM (Red Hat/CentOS/Fedora)  
sudo rpm -i dist/memzo-extracter-1.0.0-*.rpm
```

### Package Features

- 🖥️ **Desktop Integration**: Application menu entry with icon
- 🔧 **System Installation**: Installs to `/usr/local/`
- 📝 **Proper Dependencies**: Requires Java 17+ runtime
- 🗑️ **Clean Uninstall**: Proper package removal support

## Issues Encountered with jpackage

The jpackage build fails with module resolution errors:
- Missing modules: `jdk.management.jfr`, `java.rmi`, etc.
- Runtime library issues: `libatk-wrapper.so` missing on Linux
- Module system conflicts with Spring Boot and dependency libraries

## Recommended Approach

**Use the JAR distribution method:**

1. Build the application:
   ```bash
   mvn clean package -DskipTests
   ```

2. Distribute the JAR file:
   ```bash
   target/memzo-extracter-0.0.1-SNAPSHOT.jar
   ```

3. Use the launcher scripts for convenience:
   - Linux/macOS: `./run-memzo-extracter.sh`
   - Windows: `run-memzo-extracter.bat`

## Alternative Native Packaging Solutions

If native packaging is required, consider these alternatives:

1. **GraalVM Native Image**: Compile to native executable
2. **Launch4j**: Create Windows exe wrapper for JAR
3. **jlink + jpackage** with full Oracle JDK (not OpenJDK)
4. **Platform-specific packaging**: Use system package managers

## Technical Details

The project includes jpackage configuration in `pom.xml` and build scripts (`build-installer.sh`), but they are currently non-functional due to OpenJDK module system limitations. The configuration remains for future reference when these limitations are resolved.

### Error Examples

Typical errors encountered:
```
jlink failed with: Error: Module jdk.management.jfr not found
java.nio.file.NoSuchFileException: /usr/lib/jvm/java-17-openjdk-amd64/lib/libatk-wrapper.so
```

### Why JAR Distribution Works Better

1. **No Module System Issues**: JAR files bypass jlink/jpackage module problems
2. **Easier Debugging**: Direct Java execution shows clear error messages  
3. **Smaller Download**: No bundled JVM reduces file size
4. **Cross-platform**: Single JAR works everywhere with Java 17+
5. **Simpler Deployment**: No platform-specific installer complications

## Quick Start Guide

**For end users (recommended)**:
1. Ensure Java 17+ is installed
2. Download `memzo-extracter-0.0.1-SNAPSHOT.jar`
3. Run: `java -jar memzo-extracter-0.0.1-SNAPSHOT.jar`
4. Or use the launcher scripts for convenience

**For developers**:
1. Clone the repository
2. Run: `mvn clean package -DskipTests`
3. Run: `./run-memzo-extracter.sh` (Linux/macOS) or `run-memzo-extracter.bat` (Windows)

For current deployment, the JAR approach provides excellent cross-platform compatibility and is the most reliable option.

## Linux Package Distribution (Working Solutions)

Since jpackage has limitations, here are practical Linux packaging approaches:

### Method 1: Simple TAR.GZ Distribution (Recommended)

Create a distributable package with launcher scripts:

```bash
# Build the application
mvn clean package -DskipTests

# Create distribution directory
mkdir -p dist/memzo-extracter-1.0.0
cp target/memzo-extracter-0.0.1-SNAPSHOT.jar dist/memzo-extracter-1.0.0/
cp run-memzo-extracter.sh dist/memzo-extracter-1.0.0/
cp DISTRIBUTION.md dist/memzo-extracter-1.0.0/README.md

# Create tar.gz package
cd dist
tar -czf memzo-extracter-1.0.0-linux.tar.gz memzo-extracter-1.0.0/
```

### Method 2: AppImage (Universal Linux Package)

AppImage provides a single executable that works across all Linux distributions:

```bash
# Download linuxdeploy and create AppImage
wget https://github.com/linuxdeploy/linuxdeploy/releases/download/continuous/linuxdeploy-x86_64.AppImage
chmod +x linuxdeploy-x86_64.AppImage

# Create AppDir structure
mkdir -p AppDir/usr/bin
mkdir -p AppDir/usr/share/applications
mkdir -p AppDir/usr/share/icons/hicolor/256x256/apps

# Copy application files
cp target/memzo-extracter-0.0.1-SNAPSHOT.jar AppDir/usr/bin/
cp run-memzo-extracter.sh AppDir/usr/bin/memzo-extracter

# Create desktop file
cat > AppDir/usr/share/applications/memzo-extracter.desktop << EOF
[Desktop Entry]
Name=Memzo Extracter
Exec=memzo-extracter
Icon=memzo-extracter
Type=Application
Categories=Office;Utility;
EOF

# Build AppImage
./linuxdeploy-x86_64.AppImage --appdir AppDir --output appimage
```

### Method 3: Debian Package (DEB) - Manual Creation

Create a proper DEB package manually:

```bash
# Create package structure
mkdir -p package/memzo-extracter/DEBIAN
mkdir -p package/memzo-extracter/usr/local/bin
mkdir -p package/memzo-extracter/usr/local/share/memzo-extracter
mkdir -p package/memzo-extracter/usr/share/applications

# Copy application files
cp target/memzo-extracter-0.0.1-SNAPSHOT.jar package/memzo-extracter/usr/local/share/memzo-extracter/
cp run-memzo-extracter.sh package/memzo-extracter/usr/local/bin/memzo-extracter

# Create control file
cat > package/memzo-extracter/DEBIAN/control << EOF
Package: memzo-extracter
Version: 1.0.0
Section: utils
Priority: optional
Architecture: all
Depends: openjdk-17-jre | openjdk-18-jre | openjdk-19-jre | openjdk-20-jre | openjdk-21-jre
Maintainer: DevDam <contact@devdam.com>
Description: Memzo Extracter - CSV Data Analysis Tool
 A professional Java application for analyzing CSV data with export capabilities.
EOF

# Make executable
chmod +x package/memzo-extracter/usr/local/bin/memzo-extracter

# Build DEB package
dpkg-deb --build package/memzo-extracter
```

**macOS (DMG package):**
```bash
mvn jpackage:jpackage -Pmac
```

## Platform-Specific Notes

### Linux
- **DEB packages:** Work on Debian, Ubuntu, and derivatives
- **RPM packages:** Work on Red Hat, CentOS, Fedora, and derivatives
- **Dependencies:** Most Linux distributions include required tools
- **Installation:** 
  - DEB: `sudo dpkg -i target/installer/*.deb`
  - RPM: `sudo rpm -i target/installer/*.rpm`

### Windows
- **Requires WiX Toolset:** Download from https://wixtoolset.org/
- **Output:** MSI installer package
- **Installation:** Double-click the .msi file
- **Features:** 
  - Start menu shortcuts
  - Desktop shortcuts
  - Add/Remove Programs integration
  - Directory chooser during installation

### macOS
- **Requires Xcode:** Install via App Store or download from Apple Developer
- **Output:** DMG disk image
- **Installation:** Open .dmg and drag app to Applications folder
- **Features:**
  - Proper macOS app bundle
  - Code signing support (if certificates are configured)
  - Notarization ready (requires Apple Developer account)

## Application Icons

The build process looks for platform-specific icons in `src/main/resources/`:

- **Windows:** `icon.ico` (ICO format)
- **macOS:** `icon.icns` (ICNS format)  
- **Linux:** `icon.png` (PNG format, recommended 512x512px)

If icons are not found, jpackage will use default icons.

### Creating Icons

You can create icons from a high-resolution PNG image:

```bash
# For macOS (requires iconutil on macOS)
iconutil -c icns icon.iconset

# For Windows (requires ImageMagick)
convert icon.png -resize 256x256 icon.ico

# For Linux, just use a high-resolution PNG
cp icon.png src/main/resources/icon.png
```

## Output

Successful builds create installers in `target/installer/`:

```
target/installer/
├── MemzoExtracter-1.0.0.deb     # Linux DEB package
├── MemzoExtracter-1.0.0.rpm     # Linux RPM package  
├── MemzoExtracter-1.0.0.dmg     # macOS DMG image
└── MemzoExtracter-1.0.0.msi     # Windows MSI installer
```

## Configuration

The jpackage configuration can be customized in `pom.xml`:

- **App name:** `<app.name>MemzoExtracter</app.name>`
- **App version:** `<app.version>1.0.0</app.version>`
- **Vendor:** `<app.vendor>DevDam</app.vendor>`
- **Main class:** `<app.main.class>com.devdam.memzo_extracter.ui.ApplicationLauncher</app.main.class>`

## Troubleshooting

### Common Issues

1. **"jpackage not found"**
   - Ensure you're using JDK 17+ (not JRE)
   - Verify `JAVA_HOME` points to a JDK installation

2. **"WiX Toolset not found" (Windows)**
   - Download and install WiX Toolset 3.11+
   - Add WiX bin directory to PATH

3. **"dpkg-deb not found" (Linux)**
   - Install dpkg-dev: `sudo apt install dpkg-dev`

4. **Permission denied**
   - Make build script executable: `chmod +x build-installer.sh`

5. **Large installer size**
   - The installer includes a full JRE (50-100MB)
   - This ensures the app runs on systems without Java installed

### Debug Mode

For verbose output during packaging:
```bash
mvn jpackage:jpackage -Plinux -X
```

## Distribution

The generated installers are self-contained and include:
- The application JAR file
- All required dependencies
- A bundled Java runtime
- Platform-specific launchers
- Desktop integration (shortcuts, file associations)

Users don't need Java installed to run the application!

## Advanced Configuration

For advanced jpackage options, you can modify the plugin configuration in `pom.xml`. See the [jpackage documentation](https://docs.oracle.com/en/java/javase/17/jpackage/) for all available options.
