# Pinning lab — run steps

Use the maintained `main` branch. The trainer provides the exercise instructions
and tells you which case to work on.

## 1. Prerequisites

- Android Studio Rabbit 2 (2026.2.2) or newer, with Android SDK platform 37 and
  build-tools 37.0.0
- JetBrains Runtime 25 from Android Studio's bundled JBR, selected through
  `gradle/gradle-daemon-jvm.properties`; no separate JDK installation needed
- A device or emulator running Android 11 (API 30) or newer
- Network access to the training server

The app targets Android 17 (API 37), with Java/Kotlin compilation target 25.
For a shell build, select your installed bundled JBR through `JAVA_HOME` if needed.

For Android Studio Quail 4, change only your local checkout's `agp` value in
`gradle/libs.versions.toml` from `9.5.1` to `9.4.1` before sync, and use
JetBrains Runtime 25. Leave that compatibility edit uncommitted. The default
AGP 9.5.1 configuration requires Rabbit 2 or newer.

## 2. Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 3. Run

Launch **Pinning Lab** and select **C** (Challenge), **S** (Solution) or **3**
(Solution HTTP/3) in the left rail. Case 3 requires Android 14 (API 34) or
newer and uses the supplied hosted endpoint. Its rail item is disabled when
unavailable. Follow the trainer's exercise instructions. Case switching is
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

Case 3 does not support local servers. Its public default is supplied by
`lab.h3.url` in tracked `lab.properties`; an override to a different origin
disables the case. No local-network setup or permission prompt is needed for it.
