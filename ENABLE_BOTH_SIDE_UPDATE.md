# TextDisplay 单双面显示配置更新

## 新增配置项

在 `config.yml` 的 `video_text` 配置段中新增了 `enableBothSide` 配置项：

```yaml
# 视频播放文本展示配置 (text模式)
video_text:
  # 文本展示左下角的坐标
  position:
    x: 10.1
    y: -60.2
    z: 10.3
  # 墙体朝向 (NORTH, SOUTH, EAST, WEST)
  direction: NORTH
  # 是否启用双面显示（背靠背实体）
  enableBothSide: false
```

## 配置说明

### `enableBothSide` 配置项
- **类型**: 布尔值 (true/false)
- **默认值**: `false`
- **功能**: 控制 Text 模式下 TextDisplay 实体的创建方式

#### `enableBothSide: false`（单面模式，默认）
- 每个像素只创建 **1个** TextDisplay 实体
- 实体朝向玩家方向（180度旋转）
- 总实体数量：96 × 54 = **5,184个**
- **适用场景**：
  - 玩家只从一个方向观看
  - 服务器性能有限
  - 减少实体数量以提升性能

#### `enableBothSide: true`（双面模式）
- 每个像素创建 **2个** TextDisplay 实体（背靠背）
- 正面朝向玩家，背面朝向相反方向
- 总实体数量：96 × 54 × 2 = **10,368个**
- **适用场景**：
  - 玩家需要从多个方向观看
  - 制作透明或半透明的视频墙
  - 服务器性能充足

## 性能对比

| 模式 | 实体数量 | 内存占用 | 渲染负载 | 推荐场景 |
|------|----------|----------|----------|----------|
| 单面模式 | 5,184个 | 较低 | 较低 | 性能优先，单向观看 |
| 双面模式 | 10,368个 | 较高 | 较高 | 效果优先，多向观看 |

## 代码实现要点

### 1. 灵活的数据结构
```java
// 重构的 TextDisplayPair 记录类
private record TextDisplayPair(TextDisplay front, TextDisplay back) {
    // 构造单个实体的情况
    public TextDisplayPair(TextDisplay single) {
        this(single, null);
    }
    
    // 检查是否为双面模式
    public boolean isBothSide() {
        return back != null;
    }
}
```

### 2. 动态实体创建
- 根据 `enableBothSide` 配置动态决定创建单个或双个实体
- 自动调整日志输出显示正确的实体数量
- 实体创建速度限制保持不变（每tick最多1000个实体）

### 3. 智能渲染
- 单面模式：只更新 `front` 实体
- 双面模式：同时更新 `front` 和 `back` 实体
- 避免空指针异常

### 4. 优化的清理
- 根据实际创建的实体数量进行清理
- 动态计算清理的实体总数
- 提供准确的日志信息

## 使用建议

### 性能优先配置（推荐）
```yaml
video_text:
  enableBothSide: false  # 单面模式，减少50%实体数量
```

### 效果优先配置
```yaml
video_text:
  enableBothSide: true   # 双面模式，完整视觉效果
```

### 服务器配置建议

**小型服务器**（4GB内存以下）：
- 建议使用 `enableBothSide: false`
- 配合适当的实体创建延迟

**中型服务器**（4-8GB内存）：
- 可以使用 `enableBothSide: true`
- 监控服务器性能表现

**大型服务器**（8GB内存以上）：
- 推荐使用 `enableBothSide: true`
- 可以享受完整的视觉效果

## 兼容性说明

- **向后兼容**：现有配置文件不需要修改，默认为单面模式
- **配置升级**：升级后配置文件会自动添加新的配置项
- **运行时切换**：修改配置后需要重启插件或服务器生效

这个更新让用户可以根据自己的服务器性能和使用场景，灵活选择最适合的显示模式。
