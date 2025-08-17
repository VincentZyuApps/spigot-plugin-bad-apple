# 视频帧加载时裁剪优化

## 优化思路
将画面裁剪从运行时移动到加载时，在插件启动读取 bin 文件时就进行裁剪，而不是在播放时裁剪。

## 优化收益

### 1. 内存优化
- **之前**: 存储完整的 96x54 = 5,184 像素/帧
- **现在**: 存储裁剪后的 72x54 = 3,888 像素/帧
- **节省内存**: 25% 的帧数据内存占用

### 2. 性能优化
- **实体数量减少**: 从 5,184 个减少到 3,888 个 TextDisplay 实体
- **渲染性能提升**: 减少 25% 的实体更新操作
- **加载时一次性处理**: 避免运行时的复杂坐标映射

### 3. 代码简化
- **消除运行时裁剪逻辑**
- **简化坐标计算**
- **统一数据结构**

## 技术实现

### 裁剪配置
```java
// 画面裁剪配置 - 去除左右黑边，减少实体数量
private static final int CROP_LEFT = 12;   // 左侧裁剪像素数
private static final int CROP_RIGHT = 12;  // 右侧裁剪像素数
private static final int CROP_TOP = 0;     // 顶部裁剪像素数（暂不裁剪）
private static final int CROP_BOTTOM = 0;  // 底部裁剪像素数（暂不裁剪）

// 实际显示区域尺寸
private static final int DISPLAY_WIDTH = FRAME_WIDTH - CROP_LEFT - CROP_RIGHT;   // 72
private static final int DISPLAY_HEIGHT = FRAME_HEIGHT - CROP_TOP - CROP_BOTTOM; // 54
```

### 加载时裁剪
```java
private byte[][] loadFrameFromStream(InputStream stream) throws IOException {
    // 1. 读取完整的原始帧数据 (96x54)
    byte[][] originalFrameData = new byte[height][width];
    
    // 2. 裁剪帧数据，只保留有效显示区域 (72x54)
    byte[][] croppedFrameData = new byte[DISPLAY_HEIGHT][DISPLAY_WIDTH];
    for (int y = 0; y < DISPLAY_HEIGHT; y++) {
        for (int x = 0; x < DISPLAY_WIDTH; x++) {
            int originalX = x + CROP_LEFT;
            int originalY = y + CROP_TOP;
            croppedFrameData[y][x] = originalFrameData[originalY][originalX];
        }
    }
    
    return croppedFrameData; // 返回裁剪后的数据
}
```

### 简化的实体创建
```java
// 直接使用显示区域尺寸
for (int y = 0; y < DISPLAY_HEIGHT; y++) {
    for (int x = 0; x < DISPLAY_WIDTH; x++) {
        Vector3f translation = getPixelTranslation(x, y, 0);
        // 创建实体...
    }
}
```

### 简化的渲染逻辑
```java
// 直接遍历裁剪后的帧数据
for (int y = 0; y < DISPLAY_HEIGHT; y++) {
    for (int x = 0; x < DISPLAY_WIDTH; x++) {
        byte pixelValue = frameData[y][x];  // 直接使用
        // 更新实体颜色...
    }
}
```

## 性能对比

| 项目 | 优化前 | 优化后 | 改进 |
|------|--------|--------|------|
| **实体数量** (单面) | 5,184 | 3,888 | -25% |
| **实体数量** (双面) | 10,368 | 7,776 | -25% |
| **内存占用** | 100% | 75% | -25% |
| **渲染操作** | 5,184次/帧 | 3,888次/帧 | -25% |
| **代码复杂度** | 复杂坐标映射 | 简化直接访问 | 显著简化 |

## 配置调整

### 当前裁剪参数
- **左侧裁剪**: 12 像素 (去除左侧黑边)
- **右侧裁剪**: 12 像素 (去除右侧黑边)
- **上下裁剪**: 0 像素 (保持原始高度)

### 自定义裁剪
如需调整裁剪参数，修改以下常量：
```java
private static final int CROP_LEFT = 12;   // 可调整
private static final int CROP_RIGHT = 12;  // 可调整
private static final int CROP_TOP = 0;     // 可调整
private static final int CROP_BOTTOM = 0;  // 可调整
```

## 验证方法

### 检查实体数量
启动插件时查看日志输出：
```
开始异步创建 3888 个 TextDisplay 实体（显示区域: 72x54，原始: 96x54）
TextDisplay 实体创建完成！实际创建: 3888 个实体对，节省: 1296 个实体
```

### 确认画面比例
- 画面应该显示为更合理的比例
- 左右黑边应该被移除
- 主要内容应该居中显示

## 版本信息
- 优化版本: v3 (加载时裁剪)
- 优化日期: 2025-08-18
- 优化类型: 性能优化 + 内存优化
- 影响范围: 帧数据加载、实体创建、渲染逻辑
- 预期效果: 25% 性能提升，更合理的画面比例
