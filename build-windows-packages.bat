@echo off
REM Windows Package Builder for Memzo Extracter
REM This script creates distributable Windows packages using working methods

setlocal EnableDelayedExpansion

REM Configuration
set APP_NAME=memzo-extracter
set APP_VERSION=1.0.0
set APP_JAR=memzo-extracter-0.0.1-SNAPSHOT.jar
set APP_DISPLAY_NAME=Memzo Extracter
set APP_DESCRIPTION=CSV Data Analysis Tool with Export Capabilities
set VENDOR=DevDam
set DIST_DIR=dist

echo.
echo 🪟 Memzo Extracter Windows Package Builder
echo ========================================

REM Check prerequisites
echo [INFO] Checking prerequisites...

if not exist "target\%APP_JAR%" (
    echo [ERROR] JAR file not found. Please build the application first:
    echo   mvn clean package -DskipTests
    exit /b 1
)

if not exist "run-memzo-extracter.bat" (
    echo [ERROR] Windows launcher script not found: run-memzo-extracter.bat
    exit /b 1
)

echo [SUCCESS] Prerequisites check passed

REM Parse command line arguments
set BUILD_ZIP=false
set BUILD_MSI=false
set BUILD_ALL=false

if "%1"=="--zip" set BUILD_ZIP=true
if "%1"=="--msi" set BUILD_MSI=true
if "%1"=="--all" set BUILD_ALL=true
if "%1"=="" set BUILD_ALL=true

if "%BUILD_ALL%"=="true" (
    set BUILD_ZIP=true
    set BUILD_MSI=true
)

REM Create distribution directory
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

REM Build ZIP package
if "%BUILD_ZIP%"=="true" (
    echo [INFO] Building ZIP package...
    call :build_zip
)

REM Build MSI package (requires WiX)
if "%BUILD_MSI%"=="true" (
    echo [INFO] Building MSI package...
    call :build_msi
)

echo [SUCCESS] Package build completed!
echo [INFO] Generated packages:
dir /B "%DIST_DIR%\*.zip" "%DIST_DIR%\*.msi" 2>nul
echo [INFO] Installation instructions:
echo   ZIP: Extract and run install.bat as Administrator
echo   MSI: Double-click to install (if WiX build succeeded)
goto :end

:build_zip
    set ZIP_DIR=%DIST_DIR%\%APP_NAME%-%APP_VERSION%-windows
    if exist "%ZIP_DIR%" rmdir /s /q "%ZIP_DIR%"
    mkdir "%ZIP_DIR%"
    
    REM Copy application files
    copy "target\%APP_JAR%" "%ZIP_DIR%\"
    copy "run-memzo-extracter.bat" "%ZIP_DIR%\"
    copy "DISTRIBUTION.md" "%ZIP_DIR%\README.md"
    
    REM Create Windows installer script
    (
        echo @echo off
        echo REM Memzo Extracter Windows Installer
        echo echo Installing Memzo Extracter...
        echo.
        echo REM Create installation directory
        echo set INSTALL_DIR=%%ProgramFiles%%\MemzoExtracter
        echo if not exist "%%INSTALL_DIR%%" mkdir "%%INSTALL_DIR%%"
        echo.
        echo REM Copy files
        echo copy "%APP_JAR%" "%%INSTALL_DIR%%\"
        echo copy "run-memzo-extracter.bat" "%%INSTALL_DIR%%\%APP_NAME%.bat"
        echo.
        echo REM Create Start Menu shortcut
        echo set SHORTCUT_DIR=%%ProgramData%%\Microsoft\Windows\Start Menu\Programs
        echo powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%%SHORTCUT_DIR%%\%APP_DISPLAY_NAME%.lnk'^); $Shortcut.TargetPath = '%%INSTALL_DIR%%\%APP_NAME%.bat'; $Shortcut.WorkingDirectory = '%%INSTALL_DIR%%'; $Shortcut.Description = '%APP_DESCRIPTION%'; $Shortcut.Save(^)"
        echo.
        echo REM Create Desktop shortcut
        echo set DESKTOP_DIR=%%PUBLIC%%\Desktop
        echo powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%%DESKTOP_DIR%%\%APP_DISPLAY_NAME%.lnk'^); $Shortcut.TargetPath = '%%INSTALL_DIR%%\%APP_NAME%.bat'; $Shortcut.WorkingDirectory = '%%INSTALL_DIR%%'; $Shortcut.Description = '%APP_DESCRIPTION%'; $Shortcut.Save(^)"
        echo.
        echo echo Installation completed successfully!
        echo echo You can find %APP_DISPLAY_NAME% in the Start Menu or on the Desktop.
        echo pause
    ) > "%ZIP_DIR%\install.bat"
    
    REM Create uninstaller
    (
        echo @echo off
        echo REM Memzo Extracter Windows Uninstaller
        echo echo Uninstalling Memzo Extracter...
        echo.
        echo set INSTALL_DIR=%%ProgramFiles%%\MemzoExtracter
        echo set SHORTCUT_DIR=%%ProgramData%%\Microsoft\Windows\Start Menu\Programs
        echo set DESKTOP_DIR=%%PUBLIC%%\Desktop
        echo.
        echo REM Remove shortcuts
        echo if exist "%%SHORTCUT_DIR%%\%APP_DISPLAY_NAME%.lnk" del "%%SHORTCUT_DIR%%\%APP_DISPLAY_NAME%.lnk"
        echo if exist "%%DESKTOP_DIR%%\%APP_DISPLAY_NAME%.lnk" del "%%DESKTOP_DIR%%\%APP_DISPLAY_NAME%.lnk"
        echo.
        echo REM Remove installation directory
        echo if exist "%%INSTALL_DIR%%" rmdir /s /q "%%INSTALL_DIR%%"
        echo.
        echo echo Uninstallation completed successfully!
        echo pause
    ) > "%ZIP_DIR%\uninstall.bat"
    
    REM Create ZIP archive
    cd "%DIST_DIR%"
    powershell -Command "Compress-Archive -Path '%APP_NAME%-%APP_VERSION%-windows' -DestinationPath '%APP_NAME%-%APP_VERSION%-windows.zip' -Force"
    cd ..
    
    echo [SUCCESS] ZIP package created: %DIST_DIR%\%APP_NAME%-%APP_VERSION%-windows.zip
    goto :eof

:build_msi
    REM Check if WiX is available
    where candle >nul 2>&1
    if errorlevel 1 (
        echo [WARNING] WiX Toolset not found. Skipping MSI build.
        echo [INFO] To build MSI packages, install WiX Toolset from: https://wixtoolset.org/
        goto :eof
    )
    
    REM Create WiX source file
    set WXS_FILE=%TEMP%\memzo-extracter.wxs
    (
        echo ^<?xml version="1.0" encoding="UTF-8"?^>
        echo ^<Wix xmlns="http://schemas.microsoft.com/wix/2006/wi"^>
        echo   ^<Product Id="*" Name="%APP_DISPLAY_NAME%" Language="1033" Version="%APP_VERSION%.0" Manufacturer="%VENDOR%" UpgradeCode="12345678-1234-1234-1234-123456789012"^>
        echo     ^<Package InstallerVersion="200" Compressed="yes" InstallScope="perMachine" /^>
        echo     ^<MajorUpgrade DowngradeErrorMessage="A newer version is already installed." /^>
        echo     ^<MediaTemplate EmbedCab="yes" /^>
        echo.
        echo     ^<Feature Id="ProductFeature" Title="%APP_DISPLAY_NAME%" Level="1"^>
        echo       ^<ComponentGroupRef Id="ProductComponents" /^>
        echo     ^</Feature^>
        echo   ^</Product^>
        echo.
        echo   ^<Fragment^>
        echo     ^<Directory Id="TARGETDIR" Name="SourceDir"^>
        echo       ^<Directory Id="ProgramFilesFolder"^>
        echo         ^<Directory Id="INSTALLFOLDER" Name="%APP_NAME%" /^>
        echo       ^</Directory^>
        echo     ^</Directory^>
        echo   ^</Fragment^>
        echo.
        echo   ^<Fragment^>
        echo     ^<ComponentGroup Id="ProductComponents" Directory="INSTALLFOLDER"^>
        echo       ^<Component Id="ApplicationFiles" Guid="*"^>
        echo         ^<File Id="AppJAR" Source="target\%APP_JAR%" /^>
        echo         ^<File Id="LauncherBAT" Source="run-memzo-extracter.bat" /^>
        echo       ^</Component^>
        echo     ^</ComponentGroup^>
        echo   ^</Fragment^>
        echo ^</Wix^>
    ) > "%WXS_FILE%"
    
    REM Compile and link MSI
    set OBJ_FILE=%TEMP%\memzo-extracter.wixobj
    set MSI_FILE=%DIST_DIR%\%APP_NAME%-%APP_VERSION%-windows.msi
    
    candle -out "%OBJ_FILE%" "%WXS_FILE%"
    if errorlevel 1 (
        echo [ERROR] WiX compilation failed
        goto :eof
    )
    
    light -out "%MSI_FILE%" "%OBJ_FILE%"
    if errorlevel 1 (
        echo [ERROR] WiX linking failed
        goto :eof
    )
    
    REM Clean up temporary files
    if exist "%WXS_FILE%" del "%WXS_FILE%"
    if exist "%OBJ_FILE%" del "%OBJ_FILE%"
    
    echo [SUCCESS] MSI package created: %MSI_FILE%
    goto :eof

:end
endlocal
