# Pinning

Android teaching app for the "Development security in Android" course.
The trainer provides the exercise instructions and expected outcomes.

## Requirements

- Android Studio Rabbit (2026.2) or newer
- JDK 17 or newer (Android Studio's bundled JBR works)
- Android SDK platform 37 and build-tools 37.0.0
- A device or emulator running Android 11 (API 30) or newer
- Network access to the training server

The app targets Android 15 (API 35).

## Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio and run the `app` configuration.

## Endpoint

The course endpoints are configured in the supplied `lab.properties`. Normal
course use needs no local configuration file or endpoint setup.

## Tests

```sh
./gradlew testDebugUnitTest
```

## Font and icon licenses

Bundled variable fonts and their licenses are listed in
`app/src/main/assets/licenses/fonts.txt`. The network icon attribution is in
`app/src/main/assets/licenses/material-symbols.txt`.
