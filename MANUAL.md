# Pinning lab — run steps

The exercise runs on the default `main.challenge` branch; the trainer's
solution lives on `main.solution` and is compared after the session. Work on
`main.challenge` unless the trainer says otherwise.

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

Launch **Pinning Lab** and follow the trainer's exercise instructions. The
supplied `lab.properties` already configures the course endpoints; no local
configuration file is required.

## 4. Pointing at a local server

This is an exceptional instructor setup. Only when the trainer requests it,
create ignored `lab.local.properties` using the trainer's supplied values for
`lab.pinning.url`, `lab.cleartext.url` and, when required, `lab.leaf.pin`.
Sync, rebuild and reinstall after changing it. Remove that file and rebuild
to return to the supplied course configuration. Follow the trainer's routing
and certificate-setup instructions; an endpoint override does not configure
a local server automatically.

## 5. Switching branches

Both refined branches install as `defdev.labdroid.pinning`. Switching source
branches does not update the installed app; rebuild and reinstall the selected
branch before running it:

```sh
git switch main.solution
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Android Studio can keep files from the branch you left; run **Sync Project with
Gradle Files** (or reopen the project) after a switch made outside the IDE.
