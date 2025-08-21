# BooredCoding02

**QuickSave** is an Android application that allows users to download videos from various social media platforms including Instagram, Facebook, TikTok, and many others. Built with modern Android development practices, it provides a seamless video downloading experience with streaming capabilities.

## 🚀 Features

- **Multi-Platform Support**: Download videos from Instagram, Facebook, TikTok, YouTube, and many other social media platforms
- **Streaming Capability**: Stream videos directly within the app before downloading
- **Modern UI**: Material Design interface with theme customization options
- **High Performance**: Built with RxJava for reactive programming and efficient background processing
- **Cross-Platform**: Supports multiple CPU architectures (x86, x86_64, ARM, ARM64)
- **Storage Management**: Smart storage handling for Android 10+ with scoped storage support

## 📱 Requirements

- **Minimum SDK**: Android 7.0 (API level 24)
- **Target SDK**: Android 16 (API level 36)
- **Java Version**: Java 8
- **Architecture**: Supports x86, x86_64, armeabi-v7a, arm64-v8a

## 🛠️ Technology Stack

- **Language**: Java
- **Build System**: Gradle 8.7.0
- **Core Libraries**:
  - [youtube-dl-android](https://github.com/junkfood02/youtube-dl-android) v0.17.4 - Core video downloading engine
  - [RxJava](https://github.com/ReactiveX/RxJava) v2.1.0 - Reactive programming
  - [ExoMedia](https://github.com/brianwernick/ExoMedia) v5.2.0 - Media playback
  - [Lottie](https://github.com/airbnb/lottie-android) v3.4.0 - Animation support
  - [Material Components](https://material.io/develop/android) v1.6.0 - UI components

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/shadow/quicksave/
│   │   ├── MainActivity.java          # Main application interface
│   │   ├── DownloadActivity.java      # Video download functionality
│   │   ├── StreamingActivity.java    # Video streaming interface
│   │   ├── SplashScreen.java         # App launch screen
│   │   ├── CommandExampleActivity.java # Command examples
│   │   └── App.java                  # Application class
│   ├── res/                          # Resources (layouts, strings, etc.)
│   └── AndroidManifest.xml          # App configuration
├── build.gradle                      # App-level build configuration
└── proguard-rules.pro               # Code obfuscation rules

build.gradle                         # Project-level build configuration
gradle.properties                    # Gradle properties
```

## 🔧 Installation & Setup

### Prerequisites
- Android Studio (latest version recommended)
- JDK 8 or higher
- Android SDK with API level 24-36

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/Wasay-Tahir/BooredCoding02.git
   cd BooredCoding02
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Open the project folder
   - Wait for Gradle sync to complete

3. **Build and Run**
   - Connect an Android device or start an emulator
   - Click "Run" or press Shift+F10
   - Select your target device

### Building from Command Line

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## 📋 Permissions

The app requires the following permissions:
- `INTERNET` - For downloading videos and streaming
- `WRITE_EXTERNAL_STORAGE` - For saving downloaded videos (Android 10 and below)
- `READ_EXTERNAL_STORAGE` - For accessing storage (Android 10 and below)
- `MANAGE_EXTERNAL_STORAGE` - For storage access on Android 11+

## 🎯 Usage

1. **Launch the app** - Start with the splash screen
2. **Choose action** - Select between streaming or downloading
3. **Enter URL** - Paste the social media video URL
4. **Download/Stream** - Choose your preferred option
5. **Save** - Videos are saved to your device storage

## 🔄 Version Information

- **Current Version**: 0.13.1
- **Version Code**: 130100
- **Build Date**: Latest commit

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the terms specified in the [LICENSE](LICENSE) file.

## ⚠️ Disclaimer

This application is for educational and personal use only. Please respect the terms of service of the platforms you're downloading from and ensure you have the right to download the content.

## 🐛 Known Issues

- Some platforms may have anti-bot measures that could affect download success
- Video quality depends on the source platform's available formats
- Download speed may vary based on network conditions and platform restrictions

## 📞 Support

For issues, questions, or contributions, please:
- Check existing issues in the repository
- Create a new issue with detailed information
- Provide logs and device information when reporting bugs

---

**Note**: This project is actively maintained and updated to support the latest social media platforms and Android versions.
