# Pinning

Android teaching app for the "Development security in Android" course.
The trainer provides the exercise instructions and expected outcomes.

The maintained `main` contains Challenge, Solution and Solution HTTP/3 cases,
selected with the C/S/3 rail. Source for each case lives in
`app/src/main/kotlin/flows/`.

## Requirements

- Android Studio Rabbit 2 (2026.2.2) or newer
- Android Studio's bundled JetBrains Runtime 25; no separate JDK installation required
- Android SDK platform 37 and build-tools 37.0.0
- A device or emulator running Android 11 (API 30) or newer
- Network access to the training server

Case 3 requires Android 14 (API 34) or newer and the supplied hosted endpoint.
Its rail item is disabled when unavailable; local endpoints are not supported
for this case.

The app targets Android 17 (API 37). Gradle uses the daemon JVM criteria in
`gradle/gradle-daemon-jvm.properties`; Java/Kotlin compilation targets Java 25,
with Kotlin 2.4.21.

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
`app/src/main/assets/licenses/fonts.txt`. Button icon attribution is in
`app/src/main/assets/licenses/material-symbols.txt`.
