# Bad Apple Spigot Plugin

一个功能丰富的 Minecraft Spigot 插件，可以在游戏世界中播放经典的 Bad Apple 视频。支持两种播放模式、多种触发方式和灵活的配置选项。

## 🎯 功能特性

### 🎬 双播放模式
- **Block 模式**: 使用黑白混凝土方块在墙体上显示视频画面
- **Text 模式**: 使用背靠背的 TextDisplay 实体显示视频（更精细的显示效果）

### 🎵 音频播放
- 支持同步播放 Bad Apple 音乐（通过自定义音乐唱片）
- 可配置音频播放的启用/禁用
- Text 模式支持音频延迟配置以同步画面

### � 多种触发方式
- **指令触发**: `/play_bad_apple` 和 `/stop_bad_apple` 命令
- **压力板触发**: 
  - 苍白橡木压力板：开始 Block 模式播放
  - 黑石压力板：停止 Block 模式播放
- **按钮触发**:
  - 苍白橡木按钮：开始 Text 模式播放
  - 黑石按钮：停止 Text 模式播放

### ⚙️ 灵活配置
- 可配置的视频墙位置和朝向（支持四个方向）
- 独立的模式特定冷却时间
- 可配置的清理行为（播放完毕/手动停止时是否清理）
- 触发方式的独立开关控制

### �️ 性能优化
- 预加载视频帧数据到内存
- 异步创建 TextDisplay 实体
- 智能的冷却时间管理
- 分批处理大量实体创建

## 📦 安装方法

1. 将生成的 `spigot-plugin-bad-apple-x.x.x.jar` 放入服务器的 `plugins` 文件夹
2. 重启服务器或使用 `/reload` 命令
3. 编辑生成的配置文件 `plugins/spigot-plugin-bad-apple/config.yml`
4. 配置资源包以包含 Bad Apple 音乐文件

## ⚙️ 完整配置文件

```yaml
# Bad Apple Plugin Configuration

# 视频播放墙体配置 (block模式)
video_wall:
  # 墙体左下角方块的坐标
  position:
    x: 0
    y: 64
    z: 0
  # 墙体朝向 (NORTH, SOUTH, EAST, WEST)
  direction: NORTH

# 视频播放文本展示配置 (text模式)
video_text:
  # 文本展示左下角的坐标
  position:
    x: 10.1
    y: -60.2
    z: 10.3
  # 墙体朝向 (NORTH, SOUTH, EAST, WEST)
  direction: NORTH
  
# 播放设置
playback:
  # 是否启用视频播放功能
  enabled: true
  # 播放冷却时间（秒）
  cooldown: 235  # 3分55秒
  # 是否启用音频播放控制
  enableAudio: true

# 清理设置
cleanup:
  # Block 模式清理配置
  block:
    # 播放完毕后是否清除方块（3分55秒后）
    clear_on_complete: true
    # 手动停止播放后是否清除方块（stop命令或黑色压力板）
    clear_on_stop: true
  
  # Text 模式清理配置
  text:
    # 播放完毕后是否清除文本展示实体（3分55秒后）
    clear_on_complete: true
    # 手动停止播放后是否清除文本展示实体（stop命令或黑色按钮）
    clear_on_stop: true

# 按钮控制：使用两个按钮控制播放/停止（text 模式）
controls:
  # 声音延迟（tick）。例如10 tick ≈ 0.5秒
  sound_delay_ticks: 0

# 触发方式配置
triggers:
  # Block 模式触发配置
  block:
    # 允许通过指令触发 block 模式的开始
    command_start_enabled: true
    # 允许通过指令触发 block 模式的停止
    command_stop_enabled: true
    # 允许通过压力板触发 block 模式的开始
    pressure_plate_start_enabled: true
    # 允许通过压力板触发 block 模式的停止
    pressure_plate_stop_enabled: true
  
  # Text 模式触发配置
  text:
    # 允许通过指令触发 text 模式的开始
    command_start_enabled: true
    # 允许通过指令触发 text 模式的停止
    command_stop_enabled: true
    # 允许通过按钮触发 text 模式的开始
    button_start_enabled: true
    # 允许通过按钮触发 text 模式的停止
    button_stop_enabled: true
```

### 📋 配置说明

#### 基础位置配置
- **video_wall.position**: Block 模式的视频墙左下角方块坐标
- **video_text.position**: Text 模式的视频墙左下角位置坐标（支持小数）
- **direction**: 墙体朝向，决定视频的显示方向
  - `NORTH`: 面向北方
  - `SOUTH`: 面向南方  
  - `EAST`: 面向东方
  - `WEST`: 面向西方

#### 播放控制配置
- **playback.enabled**: 是否启用视频播放功能
- **playback.cooldown**: 播放冷却时间（秒）
- **playback.enableAudio**: 是否启用音频播放
- **controls.sound_delay_ticks**: Text 模式音频延迟（游戏刻）

#### 清理行为配置
- **cleanup.block.clear_on_complete**: Block 模式播放完毕后是否清除方块
- **cleanup.block.clear_on_stop**: Block 模式手动停止后是否清除方块
- **cleanup.text.clear_on_complete**: Text 模式播放完毕后是否清除文本实体
- **cleanup.text.clear_on_stop**: Text 模式手动停止后是否清除文本实体

#### 触发方式配置
每种模式的每种触发方式都可以独立启用/禁用：
- **triggers.block.command_start_enabled**: 允许指令开始 Block 模式
- **triggers.block.pressure_plate_start_enabled**: 允许压力板开始 Block 模式
- **triggers.text.button_start_enabled**: 允许按钮开始 Text 模式
- 以此类推...

## 🎮 使用方法

### 命令
```
/play_bad_apple [模式] [音频]
```
- **模式**: `block` 或 `text`（默认：text）
- **音频**: `true`/`false` 或 `on`/`off` 或 `1`/`0`（默认：配置文件设置）

```
/stop_bad_apple [模式]
```
- **模式**: `block` 或 `text`（默认：text）

### 物理触发器

#### Block 模式（压力板）
- **苍白橡木压力板**: 踩踏开始播放
- **黑石压力板**: 踩踏停止播放

#### Text 模式（按钮）
- **苍白橡木按钮**: 点击开始播放
- **黑石按钮**: 点击停止播放

### 权限
目前插件没有设置特殊权限要求，所有玩家都可以使用。

## 🔧 技术细节

### 视频规格
- **分辨率**: 96×54 像素
- **帧率**: 10 FPS
- **更新频率**: 每2游戏刻更新一次（20tick/s ÷ 10fps = 2tick/frame）
- **总时长**: 3分55秒
- **墙体尺寸**: 96×54 方块/实体

### 显示技术
#### Block 模式
- **白色像素**: 白色混凝土 (WHITE_CONCRETE)
- **黑色像素**: 黑色混凝土 (BLACK_CONCRETE)

#### Text 模式
- **背靠背 TextDisplay 实体**: 每个像素使用两个实体实现双面显示
- **颜色渲染**: 使用 ARGB 颜色值精确控制显示
- **标签管理**: 所有实体带有 `bad_apple` 和 `screen` 标签便于管理

### 数据格式
插件使用 ZIP 压缩的二进制文件格式存储视频帧数据：

**文件**: `assets/bin_96x54_10fps.zip`
1. **文件头** (8字节):
   - 前4字节: 宽度 (大端序)
   - 后4字节: 高度 (大端序)
2. **像素数据**:
   - 每个字节代表一个像素
   - 0 = 黑色像素
   - 1 = 白色像素

## ⚠️ 注意事项

1. **服务器性能**: 
   - Block 模式会短时间内更新大量方块
   - Text 模式会创建大量实体（96×54×2 = 10,368个）
   - 建议在配置良好的服务器上使用

2. **区域保护**: 确保播放区域没有被其他插件保护

3. **世界备份**: 建议在专门的区域播放，避免覆盖重要建筑

4. **资源包**: 需要配合包含 `music:music_disc.bad_apple` 音乐的资源包

5. **清理管理**: 合理配置清理选项，避免实体/方块堆积

## 📝 开发信息

- **版本**: 基于最新开发版本
- **API版本**: 1.21
- **主类**: `qwq.zyu.spigotPluginBadApple.SpigotPluginBadApple`
- **构建工具**: Gradle
- **依赖**: Spigot API 1.21

## 更新日志

### v0.1.0-alpha3
- ✅ 所有玩家都能听到声音

### v0.1.0-alpha1
- 🐛 修复二进制文件读取问题（解决"像素数据读取不完整"警告）
- ✅ 改进数据流读取逻辑，确保完整读取文件头和像素数据
- ✅ 优化错误处理和日志输出

### v0.0.1-alpha2
- ✅ 添加视频播放功能
- ✅ 实现配置文件系统
- ✅ 添加冷却时间机制
- ✅ 优化内存使用（预加载帧数据）
- ✅ 支持多朝向墙体显示

### v0.0.1-alpha1
- ✅ 基础音乐播放功能
- ✅ 命令系统框架

---

**享受Bad Apple在Minecraft中的精彩演出！** 🍎✨