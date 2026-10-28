# Pinning

Android teaching app for the "Development security in Android" course. It
connects to the training server over TLS and reports the result.

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

## What the app does

- Shows the pinned identities the app is built with.
- **Connect** requests the hosted HTTPS endpoint and reports the result.

## Endpoint

The app uses the hosted training server. The endpoint and the pin set are
committed in the repository; there is no local override.

## Tests

```sh
./gradlew testDebugUnitTest
```

The unit tests cover the view model. The instrumented tests cover the screen's
initial render and the committed configuration:

```sh
./gradlew connectedDebugAndroidTest
```
