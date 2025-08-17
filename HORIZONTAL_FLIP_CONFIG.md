# 水平翻转配置功能

## 功能介绍
新增全局配置项，允许在读取 bin 文件时对画面进行水平翻转（镜像），提供更灵活的画面显示控制。

## 配置选项

### 配置文件位置
在 `config.yml` 文件的最顶部，新增了 `video_settings` 部分：

```yaml
# 全局画面设置
video_settings:
  # 是否在读取bin文件时进行水平翻转（镜像）
  # true: 画面左右翻转，false: 保持原始画面
  horizontal_flip: true
```

### 配置参数说明
- **配置路径**: `video_settings.horizontal_flip`
- **数据类型**: `boolean`
- **默认值**: `true`
- **作用时机**: 插件启动时读取 bin 文件
- **影响范围**: 所有播放模式（Block 和 TextDisplay）

## 技术实现

### 1. 配置读取
在 `SpigotPluginBadApple.java` 中添加了配置读取：
```java
// 读取全局画面设置
horizontalFlip = getConfig().getBoolean("video_settings.horizontal_flip", true);
```

### 2. 水平翻转逻辑
在 `VideoPlayer.java` 的 `loadFrameFromStream()` 方法中实现：
```java
// 检查是否需要水平翻转
boolean shouldFlip = plugin.isHorizontalFlip();

// 如果启用水平翻转，翻转X坐标
if (shouldFlip) {
    originalX = FRAME_WIDTH - 1 - originalX;
}
```

### 3. 处理流程
1. **加载时处理**: 在读取 bin 文件时就进行翻转，而不是渲染时
2. **性能优化**: 翻转操作只在加载时执行一次
3. **内存友好**: 翻转后的数据直接存储，无额外内存开销

## 使用场景

### 默认启用翻转 (horizontal_flip: true)
- 画面左右镜像显示
- 适合特定的观看角度或装饰需求
- 符合某些艺术效果要求

### 禁用翻转 (horizontal_flip: false)
- 保持原始画面方向
- 与原始视频文件完全一致
- 适合标准播放需求

## 日志输出

启动时会显示当前的画面设置：
```
[INFO] 画面设置 - 水平翻转: true, 裁剪区域: 72x54 (左12 右12 上0 下0)
```

## 配置示例

### 启用翻转（默认）
```yaml
video_settings:
  horizontal_flip: true
```

### 禁用翻转
```yaml
video_settings:
  horizontal_flip: false
```

## 兼容性

### 向后兼容
- ✅ 如果配置文件中没有此项，默认启用翻转 (`true`)
- ✅ 现有配置无需修改即可继续使用
- ✅ 不影响其他配置项的功能

### 与其他功能的关系
- ✅ 与朝向配置完全兼容
- ✅ 与裁剪功能完全兼容
- ✅ 与双面显示功能完全兼容
- ✅ 同时影响 Block 和 TextDisplay 模式

## 应用效果

### Block 模式
- 方块排列会根据翻转设置改变
- 画面内容左右镜像

### TextDisplay 模式
- TextDisplay 实体显示翻转后的内容
- 保持正确的朝向和排列

## 调试建议

### 测试步骤
1. **设置 `horizontal_flip: true`**，重启插件，观察画面是否翻转
2. **设置 `horizontal_flip: false`**，重启插件，观察画面是否恢复原始方向
3. **检查日志输出**，确认配置被正确读取

### 预期结果
- 配置为 `true` 时：画面应该与原始方向左右相反
- 配置为 `false` 时：画面应该与原始视频文件完全一致

## 版本信息
- 功能版本: v1.0
- 添加日期: 2025-08-18
- 功能类型: 画面显示控制
- 影响范围: 全局画面显示
- 配置位置: `video_settings.horizontal_flip`
