# Memzo Extracter - JAR Distribution

## Built JAR Files
- **Spring Boot JAR**: `target/memzo-extracter-0.0.1-SNAPSHOT.jar` (~19MB)
- **UI Shaded JAR**: `target/memzo-extracter-0.0.1-SNAPSHOT-ui.jar` (~40MB)
- **Original JAR**: `target/original-memzo-extracter-0.0.1-SNAPSHOT.jar` (classes only)

**Recommended**: Use the Spring Boot JAR with runtime properties for best compatibility.

## How to Run

### Option 1: Interactive Runner (Recommended)
```bash
./memzo.sh
```
This will give you a choice between:
1. **Swing UI** - Modern desktop application with FlatLaf theme
2. **Spring Boot** - Web-based application (if needed)

### Option 2: Direct JAR Runners

#### For Swing Desktop UI:
```bash
./run-jar.sh
```
or manually:
```bash
java -Dspring.main.class=com.devdam.memzo_extracter.ui.ApplicationLauncher -jar target/memzo-extracter-0.0.1-SNAPSHOT.jar
```

#### For Spring Boot Web:
```bash
java -jar target/memzo-extracter-0.0.1-SNAPSHOT.jar
```

### Option 3: Development Mode
```bash
./run.sh
```
Runs directly from source using Maven (requires Maven installed)

## Features Available in JAR
✅ **Modern Swing UI** with FlatLaf dark theme  
✅ **CSV Data Import** and validation  
✅ **Data Visualization** with interactive charts  
✅ **Email Records Management** with advanced filtering  
✅ **Calendar Date Pickers** for date range filtering  
✅ **Export Functionality** with customizable field selection  
✅ **Date-Filtered Export** - exports only records matching selected date range  
✅ **Search and Filter** capabilities  
✅ **Statistics Dashboard** with beautiful charts  

## System Requirements
- **Java Version**: Java 17 or higher
- **Memory**: Minimum 512MB RAM recommended
- **Display**: GUI environment for Swing UI

## Distribution
The Spring Boot JAR file is completely self-contained and can be distributed independently. Just ensure the target system has Java 17+ installed.

## Building the JAR
To rebuild the JAR:
```bash
mvn clean package -DskipTests
```

This creates both a Spring Boot JAR and a UI-specific Shaded JAR for maximum compatibility.
