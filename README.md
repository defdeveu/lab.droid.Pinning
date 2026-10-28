# Pinning

Android teaching app for the "Development security in Android" course.
The trainer provides the exercise instructions and expected outcomes.

## Requirements

- Android Studio Rabbit (2026.2) or newer
- JDK 17 or newer (Android Studio's bundled JBR works)
- Android SDK platform 37 and build-tools 37.0.0
- A device or emulator running Android 7.0 (API 24) or newer
- Network access to the training server

## Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio and run the `app` configuration.

## Endpoint

The app uses the hosted training server by default. A machine-local override
goes in `lab.local.properties`, which is gitignored;
`lab.local.properties.example` shows the keys.

## Tests

```sh
./gradlew testDebugUnitTest
```
