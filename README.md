![spigot-plugin-bad-apple](https://socialify.git.ci/VincentZyuApps/spigot-plugin-bad-apple/image?custom_description=%F0%9F%8E%AC+Spigot+plugin+for+playing+Bad+Apple+in+Minecraft+using+blocks+or+TextDisplay+entities&description=1&font=JetBrains+Mono&forks=1&issues=1&language=1&logo=https%3A%2F%2Favatars.githubusercontent.com%2Fu%2F4350249%3Fs%3D200%26v%3D4&name=1&owner=1&pulls=1&stargazers=1&theme=Light)

> **[📖 English](README.md)**
> **[📖 中文](README-zh.cn.md)**

# 🎬🍎🧱📝 spigot-plugin-bad-apple

> 🎬 A flexible Spigot plugin for playing Bad Apple in Minecraft using blocks or TextDisplay entities. 🍎✨

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/VincentZyuApps/spigot-plugin-bad-apple)

[![Spigot](https://img.shields.io/badge/Spigot-1.21.8-ED8106?style=for-the-badge&logo=spigotmc&logoColor=white)](https://www.spigotmc.org/)
[![Paper](https://img.shields.io/badge/Paper-1.21.8-0A0A0A?style=for-the-badge&logo=papermc&logoColor=white)](https://papermc.io/)

[![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.8-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org)

---

## 🌟 Features

> The bundled video frame archive is preprocessed and loaded from the plugin JAR at runtime.

| Feature | Command | Description |
|------|------|------|
| 🧱 **Block Mode** | `/play_bad_apple block` | Renders the video on a wall using black and white concrete blocks |
| 📝 **Text Mode** | `/play_bad_apple text` | Renders the video using TextDisplay entities for a denser pixel screen |
| ⏹️ **Stop Playback** | `/stop_bad_apple <text\|block>` | Stops playback, clears cooldown, and optionally cleans blocks or entities |

> The plugin ships with `assets/bin_96x54_10fps.zip` inside the JAR and preloads all frames into memory before playback.
> Both command triggers and physical triggers can be controlled independently from the config.

### 🖼️ Preview

![Preview](./preview.png)

### 🗺️ Support

> Current repository defaults are aligned to the production [`0.2.4-rc1`](https://github.com/VincentZyuApps/spigot-plugin-bad-apple/releases/tag/0.2.4-rc1) style resource layout and config structure.

| | |
|---|---|
| 🎯 **Server Type** | Spigot / Paper |
| 🌎 **Tested API** | **1.21.8** |
| 📦 **Runtime** | Java 21 |

---

## 🛠 Tech Stack

| | |
|---|---|
| 🧱 **Server API** | [![Spigot API](https://img.shields.io/badge/Spigot_API_1.21.8-ED8106?style=for-the-badge&logo=spigotmc&logoColor=white)](https://www.spigotmc.org/) |
| 📝 **Language** | [![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/) |
| 🏗 **Build** | [![Gradle](https://img.shields.io/badge/Gradle-8.8-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org) |

---

## 📦 Download & Install

[![Download](https://img.shields.io/badge/Download-GitHub_Releases-ED8106?style=for-the-badge&logo=spigotmc&logoColor=white)](https://github.com/VincentZyuApps/spigot-plugin-bad-apple/releases)

Place the generated `.jar` file into the server `plugins/` directory and restart the server.

Default config:

```yml
# Bad Apple Plugin Configuration

# Global video settings
video_settings:
  # Whether to mirror frames horizontally while reading the bin archive
  # true: flip left/right, false: keep original orientation
  horizontal_flip: true

# Video wall config for block mode
video_wall:
  # Lower-left block position of the wall
  position:
    x: -20
    y: 0
    z: -70
  # Wall direction (NORTH, SOUTH, EAST, WEST)
  direction: NORTH

# Video screen config for text mode
video_text:
  # Lower-left anchor position for the text display screen
  position:
    x: 44.5
    y: -51.13
    z: -18.913
  # Wall direction (NORTH, SOUTH, EAST, WEST)
  direction: SOUTH
  # Whether to enable back-to-back displays for both-side rendering
  enableBothSide: false

# Playback settings
playback:
  # Whether playback is enabled
  enabled: true
  # Cooldown in seconds
  cooldown: 235
  # Whether audio control is enabled
  enableAudio: false
  # Resource-pack sound ID used by playsound/stopsound
  audioSoundId: niacl:music_disc.bad_apple

# Cleanup settings
cleanup:
  block:
    clear_on_complete: true
    clear_on_stop: true
  text:
    clear_on_complete: true
    clear_on_stop: true

# Button controls for text mode
controls:
  # Sound delay in ticks
  sound_delay_ticks: 1

# Trigger switches
triggers:
  block:
    command_start_enabled: true
    command_stop_enabled: true
    pressure_plate_start_enabled: true
    pressure_plate_stop_enabled: true
  text:
    command_start_enabled: true
    command_stop_enabled: true
    button_start_enabled: true
    button_stop_enabled: true
```

Config notes:

- `video_settings.horizontal_flip`: mirrors the loaded frame archive while decoding
- `video_wall.position`: lower-left block coordinate for block playback
- `video_text.position`: lower-left anchor position for text playback
- `playback.cooldown`: shared cooldown for replaying the video
- `playback.audioSoundId`: sound identifier from the client resource pack, including namespace such as `niacl:music_disc.bad_apple`
- `cleanup.*`: control whether blocks or entities are removed after playback or manual stop
- `triggers.*`: enable or disable command, pressure plate, and button triggers independently

---

## 🔧 Build

### Local Build

```bash
./gradlew build
```

The output JAR is generated under `build/libs/`.

### GitHub Actions

Push to `main` or `master` and use commit keywords to control CI:

| Keyword | Behavior |
|--------|------|
| `build action` | Build the plugin and upload the artifact |
| `build release` | Build the plugin and publish a GitHub Release |

Example:

```bash
git commit -m "feat: sync bundled video archive and config; build action"
git commit -m "build release. chore: publish bad apple plugin release"
```

Pull requests to `main` or `master` also run the build job, but do not publish releases.
