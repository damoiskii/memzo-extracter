@echo off
setlocal enabledelayedexpansion

REM Memzo Extracter Launcher Script for Windows
REM This script provides a simple way to launch the application

set "APP_NAME=Memzo Extracter"
set "JAR_NAME=memzo-extracter-0.0.1-SNAPSHOT.jar"
set "MIN_JAVA_VERSION=17"

echo.
echo 🚀 %APP_NAME% Launcher
echo ========================

REM Check if Java is installed
echo [INFO] Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo [INFO] Please install Java %MIN_JAVA_VERSION%+ from: https://openjdk.org/install/
    pause
    exit /b 1
)

REM Get Java version (simplified check)
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr "version"') do (
    set "java_version=%%i"
    set "java_version=!java_version:"=!"
)

echo [SUCCESS] Java detected: !java_version!

REM Find JAR file
echo [INFO] Locating application JAR...
set "jar_path="

if exist "%JAR_NAME%" (
    set "jar_path=%JAR_NAME%"
) else if exist "target\%JAR_NAME%" (
    set "jar_path=target\%JAR_NAME%"
) else if exist "..\%JAR_NAME%" (
    set "jar_path=..\%JAR_NAME%"
) else (
    echo [ERROR] Cannot find %JAR_NAME%
    echo [INFO] Please ensure the JAR file exists in:
    echo [INFO]   - Current directory
    echo [INFO]   - target\ directory  
    echo [INFO]   - Parent directory
    echo [INFO]
    echo [INFO] To build the JAR file, run: mvn clean package -DskipTests
    pause
    exit /b 1
)

echo [SUCCESS] Found JAR: !jar_path!

REM Launch the application
echo [INFO] Starting %APP_NAME%...
echo.

java -Xmx1G -Dfile.encoding=UTF-8 -Djava.awt.headless=false -jar "!jar_path!"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Application failed to start
    echo [INFO] Please check the error messages above
    pause
)
