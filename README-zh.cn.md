![spigot-plugin-bad-apple](https://socialify.git.ci/VincentZyuApps/spigot-plugin-bad-apple/image?custom_description=%F0%9F%8E%AC+Spigot+%E6%8F%92%E4%BB%B6%EF%BC%8C%E7%94%A8%E6%96%B9%E5%9D%97%E6%88%96+TextDisplay+%E5%AE%9E%E4%BD%93%E5%9C%A8+Minecraft+%E4%B8%AD%E6%92%AD%E6%94%BE+Bad+Apple&description=1&font=JetBrains+Mono&forks=1&issues=1&language=1&logo=https%3A%2F%2Favatars.githubusercontent.com%2Fu%2F4350249%3Fs%3D200%26v%3D4&name=1&owner=1&pulls=1&stargazers=1&theme=Light)

> **[📖 English](README.md)**
> **[📖 中文](README-zh.cn.md)**

# 🎬🍎🧱📝 spigot-plugin-bad-apple

> 🎬 Minecraft Spigot 服务端插件，可灵活配置。用方块或 TextDisplay 实体在游戏内播放 Bad Apple 视频。🍎✨

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/VincentZyuApps/spigot-plugin-bad-apple)

[![Spigot](https://img.shields.io/badge/Spigot-1.21.8-ED8106?style=for-the-badge&logo=spigotmc&logoColor=white)](https://www.spigotmc.org/)
[![Paper](https://img.shields.io/badge/Paper-1.21.8-0A0A0A?style=for-the-badge&logo=papermc&logoColor=white)](https://papermc.io/)

[![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.8-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org)

---

## 🌟 功能特性

> 插件内置的视频帧压缩包已经预处理好，并会在运行时从插件 JAR 里读取加载。

| 功能 | 命令 | 说明 |
|------|------|------|
| 🧱 **方块模式** | `/play_bad_apple block` | 用黑白混凝土方块在墙面上渲染视频画面 |
| 📝 **文本模式** | `/play_bad_apple text` | 用 TextDisplay 实体渲染更密集的视频像素屏幕 |
| ⏹️ **停止播放** | `/stop_bad_apple <text\|block>` | 停止播放、清除冷却，并按配置决定是否清理方块或实体 |

> 插件会把 `assets/bin_96x54_10fps.zip` 打包进 JAR，并在播放前预加载全部帧数据到内存。
> 指令触发与物理触发都可以在配置里分别启用或关闭。

### 🖼️ 效果预览

![Preview](./preview.png)

### 🗺️ 支持情况

> 当前仓库默认值已经按生产环境 [`0.2.4-rc1`](https://github.com/VincentZyuApps/spigot-plugin-bad-apple/releases/tag/0.2.4-rc1) 风格对齐到同一套资源布局和配置结构。

| | |
|---|---|
| 🎯 **服务端类型** | Spigot / Paper |
| 🌎 **测试 API** | **1.21.8** |
| 📦 **运行环境** | Java 21 |

---

## 🛠 技术栈

| | |
|---|---|
| 🧱 **服务端 API** | [![Spigot API](https://img.shields.io/badge/Spigot_API_1.21.8-ED8106?style=for-the-badge&logo=spigotmc&logoColor=white)](https://www.spigotmc.org/) |
| 📝 **语言** | [![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/) |
| 🏗 **构建** | [![Gradle](https://img.shields.io/badge/Gradle-8.8-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org) |

---

## 📦 下载与安装

[![Download](https://img.shields.io/badge/Download-GitHub_Releases-ED8106?style=for-the-badge&logo=spigotmc&logoColor=white)](https://github.com/VincentZyuApps/spigot-plugin-bad-apple/releases)

将生成好的 `.jar` 文件放进服务器 `plugins/` 目录后重启即可。

默认配置如下：

```yml
# Bad Apple Plugin Configuration

# 全局画面设置
video_settings:
  # 读取 bin 压缩包时是否做水平镜像
  # true: 左右翻转，false: 保持原始方向
  horizontal_flip: true

# 方块模式视频墙配置
video_wall:
  # 墙体左下角方块坐标
  position:
    x: -20
    y: 0
    z: -70
  # 墙体朝向 (NORTH, SOUTH, EAST, WEST)
  direction: NORTH

# 文本模式视频屏幕配置
video_text:
  # 文本展示左下角锚点坐标
  position:
    x: 44.5
    y: -51.13
    z: -18.913
  # 墙体朝向 (NORTH, SOUTH, EAST, WEST)
  direction: SOUTH
  # 是否启用背靠背双面显示
  enableBothSide: false

# 播放设置
playback:
  # 是否启用播放功能
  enabled: true
  # 播放冷却时间（秒）
  cooldown: 235
  # 是否启用音频控制
  enableAudio: false
  # playsound/stopsound 使用的资源包声音 ID
  audioSoundId: niacl:music_disc.bad_apple

# 清理设置
cleanup:
  block:
    clear_on_complete: true
    clear_on_stop: true
  text:
    clear_on_complete: true
    clear_on_stop: true

# 文本模式按钮控制
controls:
  # 音频延迟（tick）
  sound_delay_ticks: 1

# 触发开关
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

配置说明：

- `video_settings.horizontal_flip`：解码帧压缩包时是否水平镜像
- `video_wall.position`：方块模式的左下角方块坐标
- `video_text.position`：文本模式的左下角锚点坐标
- `playback.cooldown`：重复播放前的共享冷却时间
- `playback.audioSoundId`：客户端资源包里的声音标识，需包含命名空间，例如 `niacl:music_disc.bad_apple`
- `cleanup.*`：控制播放完毕或手动停止后是否清理方块或实体
- `triggers.*`：分别控制指令、压力板、按钮触发是否启用

---

## 🔧 构建

### 本地构建

```bash
./gradlew build
```

产物 JAR 会生成在 `build/libs/` 目录下。

### GitHub Actions

push 到 `main` 或 `master` 时，可以用 commit 关键词控制 CI：

| 关键字 | 行为 |
|--------|------|
| `build action` | 构建插件并上传 artifact |
| `build release` | 构建插件并发布 GitHub Release |

示例：

```bash
git commit -m "feat: sync bundled video archive and config; build action"
git commit -m "build release. chore: publish bad apple plugin release"
```

PR 到 `main` 或 `master` 时也会触发构建，但不会自动发布 release。
