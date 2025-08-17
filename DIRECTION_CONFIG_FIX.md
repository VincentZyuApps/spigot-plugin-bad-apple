# TextDisplay 朝向配置修复

## 问题描述
在之前的版本中，TextDisplay 模式的实体朝向是硬编码的，始终面朝北方，没有应用配置文件中的 `direction` 参数。

## 修复内容

### 1. 添加动态朝向计算方法
新增了 `getTextDisplayRotation(boolean isFront)` 方法，能够根据配置文件中的 `direction` 参数动态计算正确的旋转角度。

### 2. 朝向映射逻辑
- **NORTH (朝北)**: 基础角度 0°
- **SOUTH (朝南)**: 基础角度 180°
- **EAST (朝东)**: 基础角度 270° (-90°)
- **WEST (朝西)**: 基础角度 90°

### 3. 双面模式处理
- **正面实体**: 基础角度 + 180° (朝向玩家)
- **背面实体**: 基础角度 (背向玩家)

## 技术实现

```java
private Quaternionf getTextDisplayRotation(boolean isFront) {
    // 基础朝向角度（相对于NORTH方向的偏移）
    float baseYaw = switch (direction.toUpperCase()) {
        case "NORTH" -> 0f;          // 朝北，无偏移
        case "SOUTH" -> 180f;        // 朝南，旋转180度
        case "EAST" -> 270f;         // 朝东，旋转270度（-90度）
        case "WEST" -> 90f;          // 朝西，旋转90度
        default -> 0f;
    };
    
    // 如果是正面，需要额外旋转180度让文字朝向玩家
    // 如果是背面，则不需要额外旋转
    float finalYaw = isFront ? baseYaw + 180f : baseYaw;
    
    return new Quaternionf().rotationYXZ((float) Math.toRadians(finalYaw), 0, 0);
}
```

## 配置示例

现在可以在 `config.yml` 中正确配置 TextDisplay 模式的朝向：

```yaml
video_text:
  position:
    x: 10.1
    y: -60.2
    z: 10.3
  # 墙体朝向现在会正确应用到 TextDisplay 实体
  direction: SOUTH  # 可选: NORTH, SOUTH, EAST, WEST
  enableBothSide: false
```

## 测试建议

1. 分别设置 `direction` 为 `NORTH`, `SOUTH`, `EAST`, `WEST`
2. 启动 TextDisplay 模式播放
3. 验证视频画面是否按照配置的方向正确显示
4. 测试双面模式和单面模式在不同朝向下的表现

## 兼容性
- ✅ 向后兼容：现有配置无需修改
- ✅ 默认行为：如果未配置方向，默认为 NORTH
- ✅ Block 模式：不受影响，继续使用原有的朝向逻辑

## 版本信息
- 修复日期: 2025-08-18
- 影响范围: TextDisplay 模式的实体朝向
- 修复类型: 功能增强 + Bug 修复
