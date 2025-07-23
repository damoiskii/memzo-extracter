# Creating a simple 512x512 PNG icon using ImageMagick would be ideal, but for now
# we'll create a simple placeholder text file that explains how to add icons

# Application Icons for jpackage

This folder should contain platform-specific icons:

## Required Icons:

### Windows
- **icon.ico** - Windows icon file (ICO format)
- Recommended sizes: 16x16, 32x32, 48x48, 256x256 pixels

### macOS  
- **icon.icns** - macOS icon file (ICNS format)
- Contains multiple resolutions (16x16 to 1024x1024)

### Linux
- **icon.png** - PNG icon file
- Recommended size: 512x512 pixels

## Creating Icons:

If you have a high-resolution PNG image (1024x1024), you can create these icons:

### For Windows (ICO):
```bash
# Using ImageMagick
convert icon-1024.png -resize 256x256 -colors 256 icon.ico
```

### For macOS (ICNS):
```bash
# Create iconset folder
mkdir icon.iconset

# Create required sizes
sips -z 16 16 icon-1024.png --out icon.iconset/icon_16x16.png
sips -z 32 32 icon-1024.png --out icon.iconset/icon_16x16@2x.png
sips -z 32 32 icon-1024.png --out icon.iconset/icon_32x32.png
sips -z 64 64 icon-1024.png --out icon.iconset/icon_32x32@2x.png
sips -z 128 128 icon-1024.png --out icon.iconset/icon_128x128.png
sips -z 256 256 icon-1024.png --out icon.iconset/icon_128x128@2x.png
sips -z 256 256 icon-1024.png --out icon.iconset/icon_256x256.png
sips -z 512 512 icon-1024.png --out icon.iconset/icon_256x256@2x.png
sips -z 512 512 icon-1024.png --out icon.iconset/icon_512x512.png
sips -z 1024 1024 icon-1024.png --out icon.iconset/icon_512x512@2x.png

# Create ICNS file
iconutil -c icns icon.iconset
```

### For Linux (PNG):
```bash
# Simply resize to 512x512
convert icon-1024.png -resize 512x512 icon.png
```

## Note:
If no icons are provided, jpackage will use default system icons.
