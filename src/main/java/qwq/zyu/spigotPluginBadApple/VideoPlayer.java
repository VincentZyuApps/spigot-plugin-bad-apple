package qwq.zyu.spigotPluginBadApple;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 负责视频帧的加载、播放和渲染。
 */
public class VideoPlayer {

    // 视频播放的常量配置
    private static final int FRAME_WIDTH = 96;
    private static final int FRAME_HEIGHT = 54;
    private static final int FRAMES_PER_SECOND = 10;
    private static final int TICKS_PER_FRAME = 2; // (20 ticks/s) / (10 fps) = 2 ticks/frame
    private static final int ENTITIES_PER_TICK = 1000; // 每 tick 创建的实体数量上限
    
    // 画面裁剪配置 - 去除左右黑边，减少实体数量
    private static final int CROP_LEFT = 12;   // 左侧裁剪像素数
    private static final int CROP_RIGHT = 12;  // 右侧裁剪像素数
    private static final int CROP_TOP = 0;     // 顶部裁剪像素数（暂不裁剪）
    private static final int CROP_BOTTOM = 0;  // 底部裁剪像素数（暂不裁剪）
    
    // 实际显示区域尺寸
    private static final int DISPLAY_WIDTH = FRAME_WIDTH - CROP_LEFT - CROP_RIGHT;   // 96 - 12 - 12 = 72
    private static final int DISPLAY_HEIGHT = FRAME_HEIGHT - CROP_TOP - CROP_BOTTOM; // 54 - 0 - 0 = 54

    private final SpigotPluginBadApple plugin;
    private final List<byte[][]> frames;
    private final List<TextDisplayPair> textDisplayPairs; // 用于text模式的实体对列表
    private Location wallPosition;
    private String direction;
    private String playMode = "block"; // 播放模式, "block" 或 "text"
    private boolean isPlaying = false;
    private VideoPlaybackTask currentPlaybackTask; // 当前播放任务的引用

    // 记录TextDisplay实体（可能是单个或背靠背的两个）
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

    public VideoPlayer(SpigotPluginBadApple plugin) {
        this.plugin = plugin;
        this.frames = new ArrayList<>();
        this.textDisplayPairs = new ArrayList<>();
    }

    /**
     * 从 bin.zip 文件加载所有帧数据到内存。
     * @return 如果加载成功返回 true，否则返回 false。
     */
    public boolean loadFrames() {
        frames.clear();
        try {
            InputStream resourceStream = plugin.getResource("assets/bin_96x54_10fps.zip");
            if (resourceStream == null) {
                plugin.getLogger().severe("无法找到 assets/bin_96x54_10fps.zip 文件！");
                return false;
            }
            ZipInputStream zipStream = new ZipInputStream(resourceStream);
            ZipEntry entry;
            plugin.getLogger().info("开始加载视频帧数据...");
            plugin.getLogger().info("画面设置 - 水平翻转: " + plugin.isHorizontalFlip() + 
                ", 裁剪区域: " + DISPLAY_WIDTH + "x" + DISPLAY_HEIGHT + 
                " (左" + CROP_LEFT + " 右" + CROP_RIGHT + " 上" + CROP_TOP + " 下" + CROP_BOTTOM + ")");
            int frameIndex = 0;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".bin")) {
                    // 10fps 视频，不跳帧，全部读取
                    byte[][] frameData = loadFrameFromStream(zipStream);
                    if (frameData != null) {
                        frames.add(frameData);
                    }
                    frameIndex++;
                }
                zipStream.closeEntry();
            }
            zipStream.close();
            plugin.getLogger().info("成功加载 " + frames.size() + " 帧视频数据到内存");
            return !frames.isEmpty();
        } catch (IOException e) {
            plugin.getLogger().severe("加载视频帧数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private byte[][] loadFrameFromStream(InputStream stream) throws IOException {
        byte[] header = new byte[8];
        if (stream.readNBytes(header, 0, 8) != 8) return null;
        int width = ((header[0] & 0xFF) << 24) | ((header[1] & 0xFF) << 16) | ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
        int height = ((header[4] & 0xFF) << 24) | ((header[5] & 0xFF) << 16) | ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
        if (width != FRAME_WIDTH || height != FRAME_HEIGHT) return null;
        
        // 读取完整的原始帧数据
        byte[][] originalFrameData = new byte[height][width];
        for (int y = 0; y < height; y++) {
            stream.readNBytes(originalFrameData[y], 0, width);
        }
        
        // 检查是否需要水平翻转
        boolean shouldFlip = plugin.isHorizontalFlip();
        
        // 裁剪帧数据，只保留有效显示区域
        byte[][] croppedFrameData = new byte[DISPLAY_HEIGHT][DISPLAY_WIDTH];
        for (int y = 0; y < DISPLAY_HEIGHT; y++) {
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                // 从原始帧数据中提取裁剪区域的像素
                int originalX = x + CROP_LEFT;
                int originalY = y + CROP_TOP;
                
                // 如果启用水平翻转，翻转X坐标
                if (shouldFlip) {
                    originalX = FRAME_WIDTH - 1 - originalX;
                }
                
                croppedFrameData[y][x] = originalFrameData[originalY][originalX];
            }
        }
        
        return croppedFrameData;
    }

    /**
     * 开始播放视频。
     * @param mode 播放模式 ("block" 或 "text")。
     */
    public void startPlayback(String mode) {
        if (isPlaying) return;
        if (frames.isEmpty()) {
            plugin.getLogger().warning("没有加载视频帧数据，无法播放");
            return;
        }
        this.playMode = mode;
        wallPosition = plugin.getWallPosition(mode);
        direction = plugin.getWallDirection(mode);
        if (wallPosition == null) {
            plugin.getLogger().severe("墙体位置未配置！");
            return;
        }

        Runnable startPlaybackTask = () -> {
            isPlaying = true;
            plugin.setPlaying(playMode, true);
            plugin.getLogger().info("开始播放 Bad Apple 视频 (模式: " + playMode + ")，共 " + frames.size() + " 帧");
            
            // 如果是 text 模式，输出左下角像素格子中心坐标
            if ("text".equals(playMode)) {
                // 计算显示区域左下角像素的中心坐标
                Vector3f leftBottomCenter = getPixelTranslation(0, DISPLAY_HEIGHT - 1, 0);
                Location leftBottomLocation = wallPosition.clone().add(
                    leftBottomCenter.x, leftBottomCenter.y, leftBottomCenter.z);
                
                plugin.getLogger().info("Text模式 - 显示区域左下角像素格子中心坐标: " +
                    String.format("X=%.3f, Y=%.3f, Z=%.3f",
                    leftBottomLocation.getX(), leftBottomLocation.getY(), leftBottomLocation.getZ()));
                plugin.getLogger().info("显示区域: " + DISPLAY_WIDTH + "x" + DISPLAY_HEIGHT + 
                    " (裁剪: 左" + CROP_LEFT + " 右" + CROP_RIGHT + " 上" + CROP_TOP + " 下" + CROP_BOTTOM + ")");
            }
            
            // 创建并启动新的播放任务
            currentPlaybackTask = new VideoPlaybackTask();
            currentPlaybackTask.runTaskTimer(plugin, 0L, TICKS_PER_FRAME);
        };

        if ("text".equals(playMode)) {
            createTextDisplayEntities(startPlaybackTask);
        } else {
            startPlaybackTask.run();
        }
    }

    /**
     * 异步创建 TextDisplay 实体网格。
     */
    private void createTextDisplayEntities(Runnable onComplete) {
        clearTextDisplayEntities();
        World world = wallPosition.getWorld();
        if (world == null) return;

        boolean enableBothSide = plugin.isEnableBothSide();
        // 使用裁剪后的显示区域计算实体数量
        int totalEntities = DISPLAY_WIDTH * DISPLAY_HEIGHT * (enableBothSide ? 2 : 1);
        
        plugin.getLogger().info("开始异步创建 " + totalEntities + " 个 TextDisplay 实体" +
            "（显示区域: " + DISPLAY_WIDTH + "x" + DISPLAY_HEIGHT + "，原始: " + FRAME_WIDTH + "x" + FRAME_HEIGHT + "）" +
            (enableBothSide ? "（双面模式，背靠背）" : "（单面模式）") + "，已添加 bad_apple 和 screen 标签...");

        new BukkitRunnable() {
            private int x = 0;     // 直接使用显示区域坐标
            private int y = 0;     // 直接使用显示区域坐标

            @Override
            public void run() {
                int entitiesCreatedThisTick = 0;

                while (y < DISPLAY_HEIGHT && entitiesCreatedThisTick < ENTITIES_PER_TICK) {
                    Vector3f translation = getPixelTranslation(x, y, 0);
                    
                    if (enableBothSide) {
                        // 双面模式：创建背靠背的实体对
                        // 正面朝向（朝向玩家方向）
                        Quaternionf frontRotation = getTextDisplayRotation(true);
                        TextDisplay frontDisplay = createPixelEntity(world, 0xFF000000, translation, frontRotation);
                        
                        // 背面朝向（背向玩家方向）
                        Quaternionf backRotation = getTextDisplayRotation(false);
                        TextDisplay backDisplay = createPixelEntity(world, 0xFF000000, translation, backRotation);

                        textDisplayPairs.add(new TextDisplayPair(frontDisplay, backDisplay));
                        entitiesCreatedThisTick += 2;
                    } else {
                        // 单面模式：只创建一个朝向玩家的实体
                        Quaternionf rotation = getTextDisplayRotation(true);
                        TextDisplay display = createPixelEntity(world, 0xFF000000, translation, rotation);
                        
                        textDisplayPairs.add(new TextDisplayPair(display));
                        entitiesCreatedThisTick += 1;
                    }

                    x++;
                    if (x >= DISPLAY_WIDTH) {
                        x = 0;  // 重置到显示区域起始位置
                        y++;
                    }
                }

                if (y >= DISPLAY_HEIGHT) {
                    plugin.getLogger().info("TextDisplay 实体创建完成！实际创建: " + textDisplayPairs.size() + 
                        " 个实体对，节省: " + (FRAME_WIDTH * FRAME_HEIGHT - DISPLAY_WIDTH * DISPLAY_HEIGHT) + " 个实体" +
                        (enableBothSide ? "（双面模式）" : "（单面模式）"));
                    cancel();
                    Bukkit.getScheduler().runTask(plugin, onComplete);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private Vector3f getPixelTranslation(int x, int y, float zOffset) {
        double pixelSize = 0.06; // 像素间距
        
        // 现在 x, y 已经是显示区域内的坐标，无需额外转换
        // 参考 Block 模式的坐标映射逻辑，保持一致性
        float adjustedX, adjustedZ;
        
        switch (direction.toUpperCase()) {
            case "NORTH" -> {
                // 朝北：X轴正常排列，基于显示区域居中
                adjustedX = (float) ((x - DISPLAY_WIDTH / 2.0) * pixelSize);
                adjustedZ = zOffset;
            }
            case "SOUTH" -> {
                // 朝南：X轴翻转排列（与Block模式一致）
                adjustedX = (float) (((DISPLAY_WIDTH - 1 - x) - DISPLAY_WIDTH / 2.0) * pixelSize);
                adjustedZ = zOffset;
            }
            case "EAST" -> {
                // 朝东：沿Z轴排列（与Block模式一致）
                adjustedX = zOffset;
                adjustedZ = (float) ((x - DISPLAY_WIDTH / 2.0) * pixelSize);
            }
            case "WEST" -> {
                // 朝西：沿Z轴翻转排列（与Block模式一致）
                adjustedX = zOffset;
                adjustedZ = (float) (((DISPLAY_WIDTH - 1 - x) - DISPLAY_WIDTH / 2.0) * pixelSize);
            }
            default -> {
                // 默认朝北
                adjustedX = (float) ((x - DISPLAY_WIDTH / 2.0) * pixelSize);
                adjustedZ = zOffset;
            }
        }
        
        return new Vector3f(
                adjustedX,
                (float) ((DISPLAY_HEIGHT / 2.0 - y) * pixelSize), // Y轴基于显示区域居中
                adjustedZ
        );
    }

    /**
     * 根据配置的朝向计算 TextDisplay 实体的旋转角度
     * @param isFront 是否为正面（true=正面朝向玩家，false=背面朝向玩家）
     * @return 旋转四元数
     */
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

    /**
     * 根据朝向计算正确的像素索引
     * @param x 原始X坐标
     * @param y 原始Y坐标
     * @return 调整后的像素索引
     */
    private int getDirectionAdjustedPixelIndex(int x, int y) {
        // 根据朝向调整X坐标
        int adjustedX = switch (direction.toUpperCase()) {
            case "NORTH" -> x;                    // 朝北：正常顺序
            case "SOUTH" -> FRAME_WIDTH - 1 - x;  // 朝南：X轴翻转
            case "EAST" -> x;                     // 朝东：正常顺序（旋转由实体朝向处理）
            case "WEST" -> FRAME_WIDTH - 1 - x;   // 朝西：X轴翻转
            default -> x;
        };
        
        return y * FRAME_WIDTH + adjustedX;
    }

    /**
     * 根据朝向调整帧数据读取坐标
     * 现在简化为直接使用原始坐标，因为像素位置已经在getPixelTranslation中正确调整
     * @param screenX 屏幕X坐标（实体位置）
     * @param screenY 屏幕Y坐标（实体位置）
     * @return 原始帧数据坐标 [frameX, frameY]
     */
    private int[] getFrameDataCoordinates(int screenX, int screenY) {
        // 简化逻辑：直接使用原始坐标，因为实体位置已经正确调整
        return new int[]{screenX, screenY};
    }

    private TextDisplay createPixelEntity(World world, int backgroundColor, Vector3f translation, Quaternionf rotation) {
        return world.spawn(wallPosition, TextDisplay.class, (e) -> {
            e.setText(" ");
            
            // 添加两个标签，方便命令方块管理
            e.addScoreboardTag("bad_apple");
            e.addScoreboardTag("screen");
            
            // 使用 reflection 或者直接通过 NBT 设置 background 属性
            // 这里我们使用 Bukkit API 的 setBackgroundColor，但传入 ARGB 格式的颜色
            org.bukkit.Color color = org.bukkit.Color.fromARGB(backgroundColor);
            e.setBackgroundColor(color);

            float pixelSize = 0.06f; // 匹配像素间距
            float baseScale = pixelSize * 4f; // 比间距略小，防止像素重叠
            float visualScaleY = baseScale;
            float visualScaleX = baseScale * 2.0013f; // 修正横纵比

            Transformation transformation = new Transformation(
                    translation,
                    new AxisAngle4f(rotation),
                    new Vector3f(visualScaleX, visualScaleY, visualScaleY),
                    new AxisAngle4f(0, 0, 0, 1)
            );
            e.setTransformation(transformation);
            e.setBillboard(Display.Billboard.FIXED);
            e.setSeeThrough(false);
            e.setShadowed(false);
        });
    }

        /**
     * 清除所有 TextDisplay 实体。
     */
    private void clearTextDisplayEntities() {
        if (!textDisplayPairs.isEmpty()) {
            int totalEntities = 0;
            for (TextDisplayPair pair : textDisplayPairs) {
                if (pair.front != null && !pair.front.isDead()) {
                    pair.front.remove();
                    totalEntities++;
                }
                if (pair.back != null && !pair.back.isDead()) {
                    pair.back.remove();
                    totalEntities++;
                }
            }
            plugin.getLogger().info("正在清除 " + totalEntities + " 个 TextDisplay 实体...");
            textDisplayPairs.clear();
            plugin.getLogger().info("TextDisplay 实体清除完成！");
        }
    }

    private Location getBlockLocation(int frameX, int frameY) {
        World world = wallPosition.getWorld();
        int baseX = wallPosition.getBlockX();
        int baseY = wallPosition.getBlockY();
        int baseZ = wallPosition.getBlockZ();
        int actualY = baseY + (FRAME_HEIGHT - 1 - frameY);
        return switch (direction.toUpperCase()) {
            case "NORTH" -> new Location(world, baseX + frameX, actualY, baseZ);
            case "SOUTH" -> new Location(world, baseX + (FRAME_WIDTH - 1 - frameX), actualY, baseZ);
            case "EAST" -> new Location(world, baseX, actualY, baseZ + frameX);
            case "WEST" -> new Location(world, baseX, actualY, baseZ + (FRAME_WIDTH - 1 - frameX));
            default -> new Location(world, baseX + frameX, actualY, baseZ);
        };
    }

    private class VideoPlaybackTask extends BukkitRunnable {
        private int currentFrame = 0;

        @Override
        public void run() {
            if (currentFrame >= frames.size()) {
                // 播放完毕，停止播放
                isPlaying = false;
                plugin.setPlaying(false);
                currentPlaybackTask = null; // 清理任务引用
                
                // 根据配置决定是否清理（播放完毕）
                boolean shouldClear = false;
                if ("block".equals(playMode)) {
                    shouldClear = plugin.isBlockClearOnComplete();
                } else if ("text".equals(playMode)) {
                    shouldClear = plugin.isTextClearOnComplete();
                }
                
                if (shouldClear) {
                    if ("text".equals(playMode)) {
                        clearTextDisplayEntities();
                        plugin.getLogger().info("[VideoPlayer] 播放完毕 - 已清理 Text 模式的文本展示实体");
                    } else {
                        clearBlocks();
                        plugin.getLogger().info("[VideoPlayer] 播放完毕 - 已清理 Block 模式的方块");
                    }
                } else {
                    plugin.getLogger().info("[VideoPlayer] 播放完毕 - 根据配置保留当前画面");
                }
                
                plugin.getLogger().info("Bad Apple 视频播放完成！");
                cancel();
                return;
            }
            renderFrame(frames.get(currentFrame));
            currentFrame++;
        }
    }

    private void renderFrame(byte[][] frameData) {
        if ("text".equals(playMode)) {
            renderFrameTextDisplay(frameData);
        } else {
            renderFrameBlock(frameData);
        }
    }

    /**
     * 渲染 TextDisplay 模式的视频帧。
     * @param frameData 已裁剪的帧数据。
     */
    private void renderFrameTextDisplay(byte[][] frameData) {
        if (textDisplayPairs.size() != DISPLAY_WIDTH * DISPLAY_HEIGHT) return;

        // ARGB 颜色常量
        final int WHITE_ARGB = 0xFFFFFFFF; // 白色，完全不透明
        final int BLACK_ARGB = 0xFF000000; // 黑色，完全不透明

        int entityIndex = 0;
        // 直接遍历裁剪后的帧数据
        for (int y = 0; y < DISPLAY_HEIGHT; y++) {
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                if (entityIndex >= textDisplayPairs.size()) break;
                
                TextDisplayPair pair = textDisplayPairs.get(entityIndex);
                byte pixelValue = frameData[y][x];  // 直接使用裁剪后的数据

                // 根据像素值设置背景颜色
                int backgroundColor = (pixelValue == 1) ? WHITE_ARGB : BLACK_ARGB;
                org.bukkit.Color color = org.bukkit.Color.fromARGB(backgroundColor);
                
                // 更新正面实体
                if (pair.front != null) {
                    pair.front.setBackgroundColor(color);
                }
                
                // 如果是双面模式，同时更新背面实体
                if (pair.isBothSide() && pair.back != null) {
                    pair.back.setBackgroundColor(color);
                }
                
                entityIndex++;
            }
        }
    }

        /**
     * 渲染方块模式的视频帧。
     * @param frameData 已裁剪的帧数据。
     */
    private void renderFrameBlock(byte[][] frameData) {
        World world = wallPosition.getWorld();
        if (world == null) return;
        
        // 现在 frameData 是裁剪后的数据，尺寸为 DISPLAY_WIDTH × DISPLAY_HEIGHT
        for (int y = 0; y < DISPLAY_HEIGHT; y++) {
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                // 计算在原始坐标系中的位置（考虑裁剪偏移），用于确定方块位置
                int originalX = x + CROP_LEFT;
                int originalY = y + CROP_TOP;
                
                Location blockLoc = getBlockLocation(originalX, originalY);
                Block block = world.getBlockAt(blockLoc);
                
                // 直接使用裁剪后的坐标访问frameData
                Material material = (frameData[y][x] == 1) ? Material.WHITE_CONCRETE : Material.BLACK_CONCRETE;
                block.setType(material);
            }
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

        /**
     * 停止播放并清理资源。
     * @param mode 播放模式。
     * @param preserveFrame 是否保留当前画面。
     * @param isManualStop 是否为手动停止（true=手动停止，false=播放完毕）
     */
    public void stopPlayback(String mode, boolean preserveFrame, boolean isManualStop) {
        if (!isPlaying) return;
        
        isPlaying = false;
        plugin.setPlaying(false);
        
        // 取消当前的播放任务
        if (currentPlaybackTask != null && !currentPlaybackTask.isCancelled()) {
            currentPlaybackTask.cancel();
            currentPlaybackTask = null;
        }
        
        // 根据配置决定是否清理
        boolean shouldClear = false;
        if ("block".equals(mode)) {
            shouldClear = isManualStop ? plugin.isBlockClearOnStop() : plugin.isBlockClearOnComplete();
            plugin.getLogger().info("[VideoPlayer] Block模式停止 - " + 
                (isManualStop ? "手动停止" : "播放完毕") + "，清理配置: " + shouldClear);
        } else if ("text".equals(mode)) {
            shouldClear = isManualStop ? plugin.isTextClearOnStop() : plugin.isTextClearOnComplete();
            plugin.getLogger().info("[VideoPlayer] Text模式停止 - " + 
                (isManualStop ? "手动停止" : "播放完毕") + "，清理配置: " + shouldClear);
        }
        
        if (shouldClear) {
            if ("text".equals(mode)) {
                clearTextDisplayEntities();
                plugin.getLogger().info("[VideoPlayer] 已清理 Text 模式的文本展示实体");
            } else {
                clearBlocks();
                plugin.getLogger().info("[VideoPlayer] 已清理 Block 模式的方块");
            }
        } else {
            plugin.getLogger().info("[VideoPlayer] 根据配置保留当前画面");
        }
        
        plugin.getLogger().info("Bad Apple 视频播放已停止 (模式: " + mode + ")");
    }
    
    /**
     * 停止播放并清理资源。
     * @param mode 播放模式。
     * @param preserveFrame 是否保留当前画面。
     */
    public void stopPlayback(String mode, boolean preserveFrame) {
        // 向后兼容，默认为手动停止
        stopPlayback(mode, preserveFrame, true);
    }
    
    /**
     * 停止播放并清理资源（向后兼容方法）。
     * @param mode 播放模式。
     */
    public void stopPlayback(String mode) {
        stopPlayback(mode, false);
    }
    
    /**
     * 清除视频墙区域的所有方块（恢复为空气）。
     */
    private void clearBlocks() {
        World world = wallPosition.getWorld();
        if (world == null) return;
        
        // 清除裁剪后的显示区域
        for (int y = 0; y < DISPLAY_HEIGHT; y++) {
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                // 计算在原始坐标系中的位置（考虑裁剪偏移）
                int originalX = x + CROP_LEFT;
                int originalY = y + CROP_TOP;
                
                Location blockLoc = getBlockLocation(originalX, originalY);
                world.getBlockAt(blockLoc).setType(Material.AIR, false);
            }
        }
    }
}