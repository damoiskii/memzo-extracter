# Memzo Extracter - Distribution Guide

## Quick Start (Recommended)

The ea### Performance issues:
- Increase memory allocation: `java -Xmx2G -jar memzo-extracter-0.0.1-SNAPSHOT.jar`

## Data Files

The application creates and manages these files in the project directory:
- `db.csv` - Unique email database
- Exported CSV/PDF files in chosen locations

## Building from Source

1. **Prerequisites**: Java 17+, Maven 3.6+
2. **Build**: `mvn clean package -DskipTests`
3. **Run**: `java -jar target/memzo-extracter-0.0.1-SNAPSHOT.jar`un Memzo Extracter is using the JAR file:

### Prerequisites
- Java 17 or higher installed on your system
- Download from: https://openjdk.org/install/

### Running the Application

1. **Download the JAR file** (from target folder after building):
   ```bash
   memzo-extracter-0.0.1-SNAPSHOT.jar
   ```

2. **Run the application**:
   
   **Linux/macOS:**
   ```bash
   java -jar memzo-extracter-0.0.1-SNAPSHOT.jar
   ```
   
   **Windows:**
   ```cmd
   java -jar memzo-extracter-0.0.1-SNAPSHOT.jar
   ```

3. **Double-click option** (if Java is properly configured):
   - Simply double-click the JAR file to launch

## Alternative: Launcher Scripts

Use the provided launcher scripts for convenience:

### Linux/macOS:
```bash
./run-memzo-extracter.sh
```

### Windows:
```cmd
run-memzo-extracter.bat
```

## Features

- **CSV Upload & Analysis**: Import CSV files with selfie details
- **Data Visualization**: View statistics and charts
- **Export Options**: Export to CSV and PDF formats
- **Email Management**: 
  - View all email records
  - Manage unique email database
  - Track new emails from uploads
- **Date Filtering**: Filter records by date range
- **Professional UI**: Modern FlatLaf interface with multiple themes

## System Requirements

- **Java**: Version 17 or higher
- **Memory**: Minimum 512MB RAM (1GB recommended)
- **Disk Space**: 50MB for application + space for data files
- **Operating System**: Windows 10+, macOS 10.14+, Linux (Ubuntu 18.04+)

## Troubleshooting

### "Java not found" error:
1. Install Java 17+ from https://openjdk.org/install/
2. Verify installation: `java --version`

### Application won't start:
1. Try running from command line to see error messages
2. Ensure you have Java 17+ (not just Java 8 or 11)
3. Check that the JAR file isn't corrupted

### Performance issues:
- Increase memory allocation: `java -Xmx2G -jar memzo-extracter-0.0.1-SNAPSHOT-ui.jar`

## Data Files

The application creates and manages these files in the project directory:
- `db.csv` - Unique email database
- Exported CSV/PDF files in chosen locations

## Building from Source

1. **Prerequisites**: Java 17+, Maven 3.6+
2. **Build**: `mvn clean package -DskipTests`
3. **Run**: `java -jar target/memzo-extracter-0.0.1-SNAPSHOT-ui.jar`

## Support

For issues or questions:
1. Check this documentation
2. Verify Java version compatibility
3. Run from command line to see detailed error messages

---

**Note**: Native installers (exe, deb, dmg) are not available due to OpenJDK module system limitations. The JAR distribution provides excellent cross-platform compatibility and is the recommended deployment method.
