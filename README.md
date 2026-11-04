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

Karoo HR depends on the `io.hammerhead:karoo-ext:1.1.3` library, hosted in GitHub Packages:

`https://maven.pkg.github.com/hammerheadnav/karoo-ext`

Access to this package is managed separately from access to this public source repository. You must have permission to download the package.

### GitHub Actions authentication

The included GitHub Actions workflow uses the following repository secrets:

- `GPR_USER` – your GitHub username.
- `GPR_KEY` – a GitHub Personal Access Token (classic) with the `read:packages` scope.

To configure them:

1. Open your repository on GitHub.
2. Go to **Settings → Secrets and variables → Actions**.
3. Select **New repository secret**.
4. Add `GPR_USER` and `GPR_KEY`.

Never commit access tokens or credentials to the repository.

Alternatively, GitHub Actions can use its automatically generated `GITHUB_TOKEN`. This requires:
- Granting the workflow repository read access to the `karoo-ext` package under **Manage Actions access** in the package settings.
- Adding `packages: read` to the workflow permissions.
- Configuring the workflow to use `GITHUB_TOKEN` instead of a personal access token.

### Building locally

For local builds, configure your GitHub username and Personal Access Token as environment variables:

- `GPR_USER`
- `GPR_KEY`

Then run:

```bash
./gradlew :app:assembleDebug
```

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

The generated APK is located at:

`app/build/outputs/apk/debug/app-debug.apk`


## Developer

Developed by [mirml](https://github.com/mirml).
