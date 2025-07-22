# Memzo Extracter 📊

A beautiful and modern CSV data management application built with Java Swing, FlatLaf, and Spring Boot. Designed to handle selfie details data with an elegant user interface and comprehensive statistics.

## ✨ Features

### 🎨 Beautiful Modern UI
- **FlatLaf Theme System**: Supports both light and dark themes with seamless switching
- **Responsive Design**: Clean, professional interface with intuitive navigation
- **Interactive Tables**: Sortable, searchable data tables with custom cell rendering
- **Visual Charts**: Beautiful pie charts and bar charts for data visualization

### 📁 CSV Data Management
- **Smart CSV Parsing**: Robust CSV file upload and parsing with error handling
- **Data Validation**: Email validation highlighting and data completeness checks
- **Search & Filter**: Real-time search across all data fields
- **Background Processing**: Non-blocking file loading with progress indicators

### 📈 Comprehensive Statistics
- **Data Completeness Analysis**: Track complete registrations vs partial data
- **Email Statistics**: Monitor records with valid email addresses
- **Photo Analytics**: Analyze photo upload patterns and statistics
- **Visual Dashboards**: Interactive charts showing data distribution

### ⚡ Advanced Features
- **Builder Pattern**: Lombok-powered model classes for clean code
- **Spring Boot Integration**: Robust backend with dependency injection
- **Multi-threading**: Background processing for smooth UI experience
- **Error Handling**: Comprehensive error handling with user-friendly messages

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Installation & Running

1. **Clone or navigate to the project directory:**
   ```bash
   cd /path/to/memzo-extracter
   ```

2. **Run the application:**
   ```bash
   # Option 1: Using the run script
   ./run.sh
   
   # Option 2: Using Maven directly
   mvn spring-boot:run
   
   # Option 3: Build and run JAR
   mvn clean package
   java -jar target/memzo-extracter-0.0.1-SNAPSHOT.jar
   ```

3. **The application will start and display a welcome dialog**

## 📋 Usage Guide

### Data Management Tab
1. **Upload CSV File**: Click the "📁 Upload CSV File" button
2. **Select File**: Choose your CSV file (must have the required columns)
3. **View Data**: Browse your data in the beautiful table
4. **Search**: Use the search box to filter records
5. **Sort**: Click column headers to sort data

### Statistics Tab
1. **Automatic Updates**: Statistics update automatically when you switch tabs
2. **Overview Cards**: View key metrics in colorful stat cards
3. **Visual Charts**: Analyze data patterns with interactive charts
4. **Real-time**: All statistics update in real-time as data changes

### Application Features
- **Theme Toggle**: Use Ctrl+T to switch between light and dark themes
- **Refresh**: Press F5 to refresh statistics
- **Keyboard Shortcuts**: Full keyboard navigation support

## 📊 Supported CSV Format

The application expects CSV files with the following columns:

| Column | Description | Required |
|--------|-------------|----------|
| `image` | URL to the selfie image | Yes |
| `name` | User's name | No |
| `email` | User's email address | No |
| `contact` | User's contact number | No |
| `download_requests` | Download request information | No |
| `photos` | Number of photos (integer) | No |
| `photos_shared` | Information about shared photos | No |
| `photos_download` | Downloaded photos information | No |
| `date` | Date in format "dd MMMM yyyy HH:mm" | No |

### Example CSV Data:
```csv
image,name,email,contact,download_requests,photos,photos_shared,photos_download,date
https://example.com/selfie1.jpg,John Doe,john@example.com,+1-555-123-4567,,5,,,08 July 2025 14:30
https://example.com/selfie2.jpg,Jane Smith,jane@example.com,+1-555-987-6543,,3,,,09 July 2025 09:15
```

## 🛠️ Technology Stack

- **Frontend**: Java Swing with FlatLaf Look & Feel
- **Backend**: Spring Boot 3.5.3
- **Data Processing**: Apache Commons CSV
- **Charts**: JFreeChart
- **Build Tool**: Maven
- **Java Version**: 17
- **Code Enhancement**: Lombok for reduced boilerplate

## 🎯 Key Statistics Tracked

### Data Quality Metrics
- **Total Records**: Count of all imported records
- **Complete Registrations**: Records with name, email, and contact filled
- **Email Coverage**: Percentage of records with valid email addresses
- **Photo Analytics**: Statistics on photo uploads and sharing

### Visual Analytics
- **Data Completeness Pie Chart**: Visual breakdown of data quality
- **Photo Distribution Bar Chart**: Analysis of photo upload patterns
- **Real-time Updates**: All charts update automatically with new data

## 🔧 Development

### Project Structure
```
src/
├── main/java/com/devdam/memzo_extracter/
│   ├── model/          # Data models (SelfieDetail)
│   ├── service/        # Business logic (CsvService)
│   ├── ui/            # User interface components
│   │   ├── panel/     # UI panels (DataPanel, StatsPanel)
│   │   └── model/     # Table models
│   └── MemzoExtracterApplication.java
└── main/resources/
    ├── application.properties
    └── data.csv       # Sample data file
```

### Building from Source
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn clean package

# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🎨 Customization

### Themes
The application supports theme customization through FlatLaf:
- **Light Theme**: Clean, bright interface
- **Dark Theme**: Modern dark interface
- **Toggle**: Runtime theme switching with Ctrl+T

### Adding New Statistics
To add new statistics:
1. Add calculation method to `CsvService`
2. Add display component to `StatsPanel`
3. Update the refresh logic

## 📝 License

This project is built for educational and demonstration purposes.

## 🤝 Contributing

Feel free to fork this project and submit pull requests for improvements!

## 🆘 Support

If you encounter any issues:
1. Check the console output for error messages
2. Ensure your CSV file follows the expected format
3. Verify Java 17+ is installed
4. Check that all Maven dependencies are downloaded

---

**Made with ❤️ using Java Swing, FlatLaf, and Spring Boot**

*Bringing beautiful, modern UI to CSV data management!*
