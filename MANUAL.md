# Pinning lab — run steps

This branch is the trainer's reference solution; the exercise runs on the
default `main.challenge` branch. The solution is hosted-only: the endpoint is
committed and there is no local override.

## 1. Prerequisites

- Android Studio Rabbit (2026.2) or newer, with Android SDK platform 37 and
  build-tools 37.0.0
- JDK 17 or newer; Android Studio's bundled JBR works
- A device or emulator running Android 7.0 (API 24) or newer
- Network access to the training server

## 2. Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 3. Run

1. Launch **Pinning**.
2. Read the **Pinned identities** pane.
3. Press **Connect** and read the **Status** and **Last attempt** panes.

## 4. Switching branches

Both branches install under the same application id, so the installed app still
belongs to the branch you left until you rebuild and reinstall:

```sh
git switch main.challenge
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Android Studio can keep files from the branch you left; run **Sync Project with
Gradle Files** (or reopen the project) after a switch made outside the IDE.

## 5. Validated emulator setup

The runtime rows were run against this setup:

| Item | Value |
| --- | --- |
| AVD | `Pixel_9_API36_Play` |
| System image | Google Play arm64 image for API 36, product `sdk_gphone64_arm64` (Play Store present) |
| API / extension level | 36 (Android 16) / SDK extensions 17 |
| ABI | arm64-v8a |
| Build | `BE2A.250530.026.D1` |
| Emulator | reachable over adb as `emulator-5554`; the emulator console is not exposed on this connection, so the emulator build is not readable from here |

This workspace's SDK also carries the API 37 Play arm64 image
(`system-images;android-37.0;google_apis_playstore;arm64-v8a`) and the local
emulator binary 37.2.12.0, which is the intended target once a local emulator
can run.
