# Windows Package Builder for Memzo Extracter (PowerShell)
# This script creates distributable Windows packages using working methods

param(
    [Parameter(Position=0)]
    [string]$BuildType = "all"
)

# Configuration
$APP_NAME = "memzo-extracter"
$APP_VERSION = "1.0.0"
$APP_JAR = "memzo-extracter-0.0.1-SNAPSHOT.jar"
$APP_DISPLAY_NAME = "Memzo Extracter"
$APP_DESCRIPTION = "CSV Data Analysis Tool with Export Capabilities"
$VENDOR = "DevDam"
$DIST_DIR = "dist"

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Info($message) {
    Write-ColorOutput Blue "[INFO] $message"
}

function Write-Success($message) {
    Write-ColorOutput Green "[SUCCESS] $message"
}

function Write-Error($message) {
    Write-ColorOutput Red "[ERROR] $message"
}

function Write-Warning($message) {
    Write-ColorOutput Yellow "[WARNING] $message"
}

Write-Host ""
Write-Host "🪟 Memzo Extracter Windows Package Builder" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# Check prerequisites
function Test-Prerequisites {
    Write-Info "Checking prerequisites..."
    
    if (!(Test-Path "target\$APP_JAR")) {
        Write-Error "JAR file not found. Please build the application first:"
        Write-Host "  mvn clean package -DskipTests"
        exit 1
    }
    
    if (!(Test-Path "run-memzo-extracter.bat")) {
        Write-Error "Windows launcher script not found: run-memzo-extracter.bat"
        exit 1
    }
    
    Write-Success "Prerequisites check passed"
}

# Build ZIP package
function Build-ZipPackage {
    Write-Info "Building ZIP package..."
    
    $zipDir = "$DIST_DIR\$APP_NAME-$APP_VERSION-windows"
    
    if (Test-Path $zipDir) {
        Remove-Item -Recurse -Force $zipDir
    }
    New-Item -ItemType Directory -Path $zipDir -Force | Out-Null
    
    # Copy application files
    Copy-Item "target\$APP_JAR" $zipDir
    Copy-Item "run-memzo-extracter.bat" $zipDir
    Copy-Item "DISTRIBUTION.md" "$zipDir\README.md"
    
    # Create Windows installer script
    $installScript = @"
@echo off
REM Memzo Extracter Windows Installer
echo Installing Memzo Extracter...

REM Create installation directory
set INSTALL_DIR=%ProgramFiles%\MemzoExtracter
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

REM Copy files
copy "$APP_JAR" "%INSTALL_DIR%\"
copy "run-memzo-extracter.bat" "%INSTALL_DIR%\$APP_NAME.bat"

REM Create Start Menu shortcut
set SHORTCUT_DIR=%ProgramData%\Microsoft\Windows\Start Menu\Programs
powershell -Command "`$WshShell = New-Object -comObject WScript.Shell; `$Shortcut = `$WshShell.CreateShortcut('%SHORTCUT_DIR%\$APP_DISPLAY_NAME.lnk'); `$Shortcut.TargetPath = '%INSTALL_DIR%\$APP_NAME.bat'; `$Shortcut.WorkingDirectory = '%INSTALL_DIR%'; `$Shortcut.Description = '$APP_DESCRIPTION'; `$Shortcut.Save()"

REM Create Desktop shortcut
set DESKTOP_DIR=%PUBLIC%\Desktop
powershell -Command "`$WshShell = New-Object -comObject WScript.Shell; `$Shortcut = `$WshShell.CreateShortcut('%DESKTOP_DIR%\$APP_DISPLAY_NAME.lnk'); `$Shortcut.TargetPath = '%INSTALL_DIR%\$APP_NAME.bat'; `$Shortcut.WorkingDirectory = '%INSTALL_DIR%'; `$Shortcut.Description = '$APP_DESCRIPTION'; `$Shortcut.Save()"

echo Installation completed successfully!
echo You can find $APP_DISPLAY_NAME in the Start Menu or on the Desktop.
pause
"@
    
    $installScript | Out-File -FilePath "$zipDir\install.bat" -Encoding ASCII
    
    # Create uninstaller
    $uninstallScript = @"
@echo off
REM Memzo Extracter Windows Uninstaller
echo Uninstalling Memzo Extracter...

set INSTALL_DIR=%ProgramFiles%\MemzoExtracter
set SHORTCUT_DIR=%ProgramData%\Microsoft\Windows\Start Menu\Programs
set DESKTOP_DIR=%PUBLIC%\Desktop

REM Remove shortcuts
if exist "%SHORTCUT_DIR%\$APP_DISPLAY_NAME.lnk" del "%SHORTCUT_DIR%\$APP_DISPLAY_NAME.lnk"
if exist "%DESKTOP_DIR%\$APP_DISPLAY_NAME.lnk" del "%DESKTOP_DIR%\$APP_DISPLAY_NAME.lnk"

REM Remove installation directory
if exist "%INSTALL_DIR%" rmdir /s /q "%INSTALL_DIR%"

echo Uninstallation completed successfully!
pause
"@
    
    $uninstallScript | Out-File -FilePath "$zipDir\uninstall.bat" -Encoding ASCII
    
    # Create ZIP archive
    $zipPath = "$DIST_DIR\$APP_NAME-$APP_VERSION-windows.zip"
    if (Test-Path $zipPath) {
        Remove-Item $zipPath
    }
    
    Compress-Archive -Path $zipDir -DestinationPath $zipPath -Force
    
    Write-Success "ZIP package created: $zipPath"
}

# Build NSIS installer (if NSIS is available)
function Build-NSISInstaller {
    Write-Info "Building NSIS installer..."
    
    # Check if NSIS is available
    $nsisPath = Get-Command "makensis" -ErrorAction SilentlyContinue
    if (!$nsisPath) {
        $nsisPath = Get-Command "C:\Program Files (x86)\NSIS\makensis.exe" -ErrorAction SilentlyContinue
    }
    
    if (!$nsisPath) {
        Write-Warning "NSIS not found. Skipping NSIS installer build."
        Write-Info "To build NSIS installers, install NSIS from: https://nsis.sourceforge.io/"
        return
    }
    
    # Create NSIS script
    $nsisScript = @"
; Memzo Extracter NSIS Installer Script

!define APP_NAME "$APP_DISPLAY_NAME"
!define APP_VERSION "$APP_VERSION"
!define APP_PUBLISHER "$VENDOR"
!define APP_EXE "$APP_NAME.bat"

; Installer attributes
Name "`${APP_NAME}"
OutFile "$DIST_DIR\$APP_NAME-$APP_VERSION-windows-installer.exe"
InstallDir "`$PROGRAMFILES\MemzoExtracter"
InstallDirRegKey HKLM "Software\MemzoExtracter" "InstallDir"
RequestExecutionLevel admin

; Pages
Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

; Install section
Section "MainSection" SEC01
    SetOutPath "`$INSTDIR"
    File "target\$APP_JAR"
    File "run-memzo-extracter.bat"
    
    ; Rename launcher
    Rename "`$INSTDIR\run-memzo-extracter.bat" "`$INSTDIR\`${APP_EXE}"
    
    ; Create shortcuts
    CreateDirectory "`$SMPROGRAMS\`${APP_NAME}"
    CreateShortcut "`$SMPROGRAMS\`${APP_NAME}\`${APP_NAME}.lnk" "`$INSTDIR\`${APP_EXE}"
    CreateShortcut "`$SMPROGRAMS\`${APP_NAME}\Uninstall.lnk" "`$INSTDIR\uninstall.exe"
    CreateShortcut "`$DESKTOP\`${APP_NAME}.lnk" "`$INSTDIR\`${APP_EXE}"
    
    ; Write uninstaller
    WriteUninstaller "`$INSTDIR\uninstall.exe"
    
    ; Registry entries
    WriteRegStr HKLM "Software\MemzoExtracter" "InstallDir" "`$INSTDIR"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MemzoExtracter" "DisplayName" "`${APP_NAME}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MemzoExtracter" "UninstallString" "`$INSTDIR\uninstall.exe"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MemzoExtracter" "Publisher" "`${APP_PUBLISHER}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MemzoExtracter" "DisplayVersion" "`${APP_VERSION}"
SectionEnd

; Uninstall section
Section "Uninstall"
    ; Remove files
    Delete "`$INSTDIR\$APP_JAR"
    Delete "`$INSTDIR\`${APP_EXE}"
    Delete "`$INSTDIR\uninstall.exe"
    
    ; Remove shortcuts
    Delete "`$SMPROGRAMS\`${APP_NAME}\`${APP_NAME}.lnk"
    Delete "`$SMPROGRAMS\`${APP_NAME}\Uninstall.lnk"
    Delete "`$DESKTOP\`${APP_NAME}.lnk"
    RMDir "`$SMPROGRAMS\`${APP_NAME}"
    
    ; Remove installation directory
    RMDir "`$INSTDIR"
    
    ; Remove registry entries
    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MemzoExtracter"
    DeleteRegKey HKLM "Software\MemzoExtracter"
SectionEnd
"@
    
    $nsisScriptPath = "$env:TEMP\memzo-extracter.nsi"
    $nsisScript | Out-File -FilePath $nsisScriptPath -Encoding ASCII
    
    # Compile NSIS installer
    try {
        & $nsisPath.Source $nsisScriptPath
        Write-Success "NSIS installer created: $DIST_DIR\$APP_NAME-$APP_VERSION-windows-installer.exe"
    }
    catch {
        Write-Error "NSIS compilation failed: $_"
    }
    finally {
        if (Test-Path $nsisScriptPath) {
            Remove-Item $nsisScriptPath
        }
    }
}

# Main execution
function Main {
    Test-Prerequisites
    
    # Create distribution directory
    if (!(Test-Path $DIST_DIR)) {
        New-Item -ItemType Directory -Path $DIST_DIR | Out-Null
    }
    
    # Parse build type
    $buildZip = $false
    $buildNsis = $false
    
    switch ($BuildType.ToLower()) {
        "zip" { $buildZip = $true }
        "nsis" { $buildNsis = $true }
        "all" { $buildZip = $true; $buildNsis = $true }
        default { $buildZip = $true; $buildNsis = $true }
    }
    
    # Build packages
    if ($buildZip) {
        Build-ZipPackage
    }
    
    if ($buildNsis) {
        Build-NSISInstaller
    }
    
    Write-Success "Package build completed!"
    Write-Info "Generated packages:"
    Get-ChildItem "$DIST_DIR\*.zip", "$DIST_DIR\*.exe" -ErrorAction SilentlyContinue | Format-Table Name, Length
    Write-Info "Installation instructions:"
    Write-Host "  ZIP: Extract and run install.bat as Administrator"
    Write-Host "  EXE: Double-click to install (if NSIS build succeeded)"
}

# Run main function
Main
