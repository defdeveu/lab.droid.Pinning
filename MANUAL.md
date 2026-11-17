# Pinning lab — run steps

Use the maintained `main` branch. The trainer provides the exercise instructions
and tells you which case to work on.

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

Launch **Pinning Lab** and select **C** (Challenge) or **S** (Solution) in the
left rail. Follow the trainer's exercise instructions. Case switching is
unavailable during a connection attempt; use **Cancel request** if needed.

The supplied `lab.properties` configures the course endpoints; no local
configuration file is required. The app installs as `defdev.labs.droid.pinning`.

## 4. Other endpoints

This is an exceptional instructor setup. Only when the trainer requests it,
create ignored `lab.local.properties` using the trainer's supplied values for
`lab.pinning.url`, `lab.cleartext.url`, `lab.solution.url` and, when required,
`lab.leaf.pin`. Sync, rebuild and reinstall after changing it. Remove that file
and rebuild to return to the supplied course configuration. Follow the
trainer's routing and certificate-setup instructions; an endpoint override
does not configure a local server automatically.
