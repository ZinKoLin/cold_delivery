# Cold Delivery

Cold Delivery is a native Android application for offline delivery operations.

## Technology

- Native Android Kotlin
- Jetpack Compose UI
- Room local database
- Offline-first workflows

## Features

- FIFO stock management across inventory batches
- Daily product price management
- Customer delivery-day filtering
- Delivery history and status tracking
- Local backup and restore

## Build

Open the project in Android Studio, allow Gradle sync to finish, and use the bundled JDK recommended by Android Studio. Ensure `local.properties` points to the local Android SDK; it is intentionally not committed.

From a Windows PowerShell terminal at the project root:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

The debug APK is generated at `app\build\outputs\apk\debug\app-debug.apk`.

## Run in the Android Studio emulator

1. Install Android SDK Platform-Tools, Android Emulator, and an Android 14 or 15 system image.
2. Create or select a Pixel emulator in Android Studio Device Manager.
3. Start the emulator.
4. Select the `app` run configuration and press Run.

The app stores operational data locally and does not require network access for its core delivery workflow.
