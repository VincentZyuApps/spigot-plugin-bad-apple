# 🎵🎶 BadApple11MusicPack 📦✨

[![Release](https://img.shields.io/badge/Release-bad--apple--music--resource--pack-1DB954?style=for-the-badge&logo=github&logoColor=white)](https://github.com/VincentZyuApps/spigot-plugin-bad-apple/releases/tag/bad-apple-music-resource-pack)
[![ZIP](https://img.shields.io/badge/ZIP-BadApple11MusicPack-0078D4?style=for-the-badge&logo=github&logoColor=white)](https://github.com/VincentZyuApps/spigot-plugin-bad-apple/releases/download/bad-apple-music-resource-pack/BadApple11MusicPack.zip)
[![Plugin](https://img.shields.io/badge/Plugin-spigot--plugin--bad--apple-ED8106?style=for-the-badge&logo=spigotmc&logoColor=white)](https://github.com/VincentZyuApps/spigot-plugin-bad-apple)

> 🎬 Bad Apple 音频资源包发布说明。 🔊

📖 本资源包用于给 `spigot-plugin-bad-apple` 提供客户端音频资源。  
⚡ 插件本身不会内置或推流音频，而是通过服务端执行 `playsound` / `stopsound` 来触发客户端播放资源包里的声音。


> [![Minecraft Resource Pack](https://raw.githubusercontent.com/VincentZyuApps/spigot-plugin-bad-apple/main/docs/images/badge/minecraft.svg)](https://github.com/VincentZyuApps/spigot-plugin-bad-apple/releases/download/bad-apple-music-resource-pack/BadApple11MusicPack.zip)

---

## 📦 资源包文件

- 📄 **文件名：** `BadApple11MusicPack.zip`
- 🏷️ **当前包内命名空间：** `niacl`
- 📂 **主要音频目录：** `assets/niacl/sounds/records/`
- 🗺️ **声音映射文件：** `assets/niacl/sounds.json`

当前资源包内包含以下音频 🎵：

- 🎵 `niacl:music_disc.bad_apple`
- 🎵 `niacl:music_disc.daa_drum_bass`
- 🎵 `niacl:music_disc.wrong_world`
- 🎵 `niacl:music_disc.tententengoku_jigokukoku`
- 🎵 `niacl:music_disc.hakimi_otherside`

其中，Bad Apple 主音频对应：

```json
{
  "music_disc.bad_apple": {
    "sounds": [
      {
        "name": "niacl:records/bad_apple",
        "stream": true
      }
    ]
  }
}
```

---

## 🔗 与插件的对应关系

如果你使用当前仓库默认配置，请确保插件配置中的音频 ID 与这个资源包一致：

```yml
playback:
  enableAudio: true
  audioSoundId: niacl:music_disc.bad_apple
```

含义如下：

- `enableAudio: true` 表示允许插件触发音频播放
- `audioSoundId: niacl:music_disc.bad_apple` 表示插件会执行对应的 `playsound` / `stopsound`
- 如果你以后修改了资源包命名空间或 `sounds.json` 的键名，也要同步修改插件配置里的 `audioSoundId`

---

## 📥 安装方法

① ⬇️ 下载 `BadApple11MusicPack.zip`  
② 📦 将资源包提供给客户端  
③ ✅ 确保客户端已成功加载该资源包  
④ ⚙️ 在插件配置中启用音频，并将 `audioSoundId` 设置为 `niacl:music_disc.bad_apple`  
⑤ 🔄 重启服务器或重载插件配置后再测试播放

---

## 🎯 适用场景

- ⌨️ `play_bad_apple text`
- ⌨️ `play_bad_apple block`
- 🔘 按钮触发播放
- 🟫 压力板触发播放

这些路径在启用音频时，都会调用同一个配置化的声音 ID。

---

## ⚠️ 注意事项

- 🔇 没有安装资源包的客户端，通常听不到音频
- 🏷️ 命名空间 `niacl` 是资源包内定义的，不是 Minecraft 原版自带内容
- 🤖 插件侧只负责触发命令，不负责校验客户端是否真的装好了资源包
- 🔍 如果音频没响，优先检查客户端资源包是否加载成功，以及 `audioSoundId` 是否与 `sounds.json` 一致

---

## 👍 推荐搭配版本

- 🎬 插件：`spigot-plugin-bad-apple`
- ⚙️ 建议配置项：

```yml
playback:
  enableAudio: true
  audioSoundId: niacl:music_disc.bad_apple

controls:
  sound_delay_ticks: 1
```

如果你使用的是当前仓库已经更新过的版本，那么音频 ID 已经支持从配置文件读取，不再需要改 Java 源码里的硬编码。
