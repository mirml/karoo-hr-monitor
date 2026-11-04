# Karoo HR Monitor

**Heart rate and heart rate zone monitoring for Hammerhead Karoo 3.**

Karoo HR is an extension for Hammerhead Karoo that displays your heart rate directly on your cycling computer. Monitor your current, average and maximum heart rate, together with dynamic heart rate zones, while riding.

## Screenshots

### Karoo HR Monitor  – Heart Rate Field

![Karoo HR heart rate field](screenshots/karoo_hr_field.png)

### Karoo 3 – Full Screen

![Karoo HR on the full Karoo 3 screen](screenshots/karoo_hr.png)

## Features

- **Current heart rate** – see your live heart rate.
- **Average heart rate** – track your average heart rate during a ride.
- **Maximum heart rate** – monitor your peak heart rate.
- **Dynamic heart rate zones** – see which heart rate zone you are currently in.
- **Designed for Hammerhead Karoo** – integrated into the Karoo Extensions ecosystem.

## Installation

1. Open the Extensions section on your Hammerhead Karoo.
2. Find Karoo HR in the extension catalog.
3. Select Install.
4. Add the Karoo HR data field to your ride screen.

## Get Karoo HR Monitor

Find and install Karoo HR through the Extensions section on your Hammerhead Karoo device.

For the latest version and release information, visit the [Releases page](https://github.com/mirml/karoo-hr-monitor/releases).


## Building from source

### Requirements

- Android Studio
- JDK 21
- Android SDK
- Git

### Karoo SDK dependency

Karoo HR depends on `io.hammerhead:karoo-ext:1.1.3`, hosted in GitHub Packages:

`https://maven.pkg.github.com/hammerheadnav/karoo-ext`

Access to this package is managed separately from access to this public repository. You must have permission to download the package.

### Building locally

Configure your GitHub credentials as environment variables:

- `GPR_USER` – your GitHub username.
- `GPR_KEY` – a GitHub Personal Access Token (classic) with the `read:packages` scope.

Never commit access tokens or credentials to the repository.

Build the debug APK:

**Windows (PowerShell):**

```powershell
.\gradlew.bat :app:assembleDebug
```

**Linux / macOS:**

```bash
./gradlew :app:assembleDebug
```

The generated APK is located at:

`app/build/outputs/apk/debug/app-debug.apk`

### Creating a release

To publish a new version:

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Build the APK locally and verify that the build succeeds.
3. Create a new GitHub Release with a matching version tag (for example, `v0.3.0`).
4. Upload the generated `app-debug.apk` to the release.
5. Update `latestApkUrl`, `latestVersion` and `latestVersionCode` in `manifest.json`.
6. Verify that the APK download URL is accessible.

APK builds are currently performed locally because GitHub Actions does not have the required access to the private Hammerhead SDK package.


## Developer

Developed by [mirml](https://github.com/mirml).
