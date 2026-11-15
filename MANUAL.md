# Pinning lab — run steps

This branch is the trainer's reference solution; the exercise runs on the
default `main.challenge` branch. The course server is configured in the
supplied `lab.properties`; build and run normally without creating a local
configuration file.

## 1. Prerequisites

- Android Studio Rabbit (2026.2) or newer, with Android SDK platform 37 and
  build-tools 37.0.0
- JDK 17 or newer; Android Studio's bundled JBR works
- A device or emulator running Android 11 (API 30) or newer
- Network access to the training server

## 2. Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 3. Run

1. Launch **Pinning Lab**.
2. Read the **Pinned identities** pane.
3. Press **Connect** and read the **Status** and **Last attempt** panes.

## 4. Switching branches

This branch installs as `defdev.labdroid.pinning`. Switching source branches
does not update the installed app. Rebuild and reinstall the selected branch,
then launch the app you just built. If differently named Pinning apps are
installed side by side, use the selected branch's application ID from
`app/build.gradle.kts` to identify it.

```sh
git switch main.challenge
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Android Studio can keep files from the branch you left; run **Sync Project with
Gradle Files** (or reopen the project) after a switch made outside the IDE.

## 5. Validated emulator setup

The earlier runtime rows used this setup:

| Item | Value |
| --- | --- |
| AVD | `Pixel_9_API36_Play` |
| System image | Google Play arm64 image for API 36, product `sdk_gphone64_arm64` (Play Store present) |
| API / extension level | 36 (Android 16) / SDK extensions 17 |
| ABI | arm64-v8a |
| Build | `BE2A.250530.026.D1` |
| Emulator | reachable over adb as `emulator-5554`; the emulator console is not exposed on this connection, so the emulator build is not readable from here |

## 6. Alternative endpoint (exceptional instructor setup)

Only on explicit instructor direction, create ignored `lab.local.properties`
with `lab.pinning.url=THE_HTTPS_URL`, then sync, rebuild and reinstall. Remove
that file and rebuild to return to the supplied course configuration. Follow
the instructor's server-specific setup; an endpoint override does not configure
a different server automatically. An invalid setup disables Connect and shows
the configuration error.
