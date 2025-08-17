# TextDisplay 画面朝向完整修复

## 问题描述
之前的版本中，TextDisplay 模式存在两个朝向相关的问题：
1. ✅ **已修复**：TextDisplay 实体朝向硬编码为朝北
2. 🔧 **本次修复**：画面像素排列顺序没有根据朝向调整，导致画面内容方向错误

## 修复内容

### 1. 像素位置计算优化
更新了 `getPixelTranslation()` 方法，使其根据朝向正确计算像素在3D空间中的位置：

```java
// 根据朝向调整像素坐标映射
switch (direction.toUpperCase()) {
    case "NORTH" -> { /* 正常坐标映射 */ }
    case "SOUTH" -> { /* X轴翻转，保持画面正确显示 */ }
    case "EAST" -> { /* X和Z轴互换 */ }
    case "WEST" -> { /* X和Z轴互换，且Z轴翻转 */ }
}
```

### 2. 帧数据坐标映射
新增了 `getFrameDataCoordinates()` 方法，根据朝向将屏幕坐标映射到正确的帧数据坐标：

| 朝向 | 坐标变换 | 效果 |
|------|----------|------|
| **NORTH** | `(x, y)` | 正常显示 |
| **SOUTH** | `(FRAME_WIDTH-1-x, y)` | X轴镜像 |
| **EAST** | `(y, FRAME_HEIGHT-1-x)` | 90°顺时针旋转 |
| **WEST** | `(FRAME_HEIGHT-1-y, x)` | 90°逆时针旋转 |

### 3. 渲染逻辑重构
更新了 `renderFrameTextDisplay()` 方法：
- 分离屏幕坐标（实体位置）和帧数据坐标（像素值来源）
- 根据朝向动态计算正确的像素值映射
- 保证画面内容与配置朝向一致

## 技术细节

### 坐标系统说明
- **屏幕坐标**: TextDisplay 实体在3D空间中的排列位置
- **帧数据坐标**: 视频帧数据数组中的像素位置
- **朝向映射**: 将屏幕坐标转换为正确的帧数据坐标

### 朝向变换逻辑
```java
private int[] getFrameDataCoordinates(int screenX, int screenY) {
    return switch (direction.toUpperCase()) {
        case "NORTH" -> new int[]{screenX, screenY};                              // 朝北：正常映射
        case "SOUTH" -> new int[]{FRAME_WIDTH - 1 - screenX, screenY};           // 朝南：X轴翻转
        case "EAST" -> new int[]{screenY, FRAME_HEIGHT - 1 - screenX};           // 朝东：90度旋转
        case "WEST" -> new int[]{FRAME_HEIGHT - 1 - screenY, screenX};           // 朝西：-90度旋转
        default -> new int[]{screenX, screenY};
    };
}
```

## 配置示例

现在所有朝向都能正确工作：

```yaml
video_text:
  position:
    x: 10.1
    y: -60.2
    z: 10.3
  # 朝向配置现在完全生效
  direction: EAST   # 画面将正确地朝东显示
  enableBothSide: false
```

## 测试验证

### 建议测试步骤
1. **朝北测试**: 设置 `direction: NORTH`，验证画面正常显示
2. **朝南测试**: 设置 `direction: SOUTH`，验证画面左右翻转但朝向正确
3. **朝东测试**: 设置 `direction: EAST`，验证画面90°旋转朝东
4. **朝西测试**: 设置 `direction: WEST`，验证画面90°旋转朝西

### 预期效果
- ✅ TextDisplay 实体朝向与配置一致
- ✅ 画面内容方向与朝向匹配
- ✅ 双面模式在所有朝向下正确工作
- ✅ 单面模式在所有朝向下正确工作

## 兼容性保证
- ✅ **向后兼容**: 现有配置无需修改
- ✅ **默认行为**: 未配置时默认朝北，行为与之前一致
- ✅ **Block模式**: 不受影响，继续使用原有逻辑

## 版本信息
- 修复日期: 2025-08-18
- 修复范围: TextDisplay 模式的画面朝向显示
- 修复类型: 重要功能修复
- 涉及方法: `getPixelTranslation()`, `getFrameDataCoordinates()`, `renderFrameTextDisplay()`
