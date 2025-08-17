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

    private final SpigotPluginBadApple plugin;
    private final List<byte[][]> frames;
    private final List<TextDisplayPair> textDisplayPairs; // 用于text模式的背靠背实体对列表
    private Location wallPosition;
    private String direction;
    private String playMode = "block"; // 播放模式, "block" 或 "text"
    private boolean isPlaying = false;
    private VideoPlaybackTask currentPlaybackTask; // 当前播放任务的引用

    // 记录背靠背的两个TextDisplay实体
    private record TextDisplayPair(TextDisplay front, TextDisplay back) {}

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
        byte[][] frameData = new byte[height][width];
        for (int y = 0; y < height; y++) {
            stream.readNBytes(frameData[y], 0, width);
        }
        return frameData;
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
                // 计算左下角像素的中心坐标
                Vector3f leftBottomCenter = getPixelTranslation(0, FRAME_HEIGHT - 1, 0);
                Location leftBottomLocation = wallPosition.clone().add(
                    leftBottomCenter.x, leftBottomCenter.y, leftBottomCenter.z);
                
                plugin.getLogger().info("Text模式 - 左下角像素格子中心坐标: " +
                    String.format("X=%.3f, Y=%.3f, Z=%.3f",
                    leftBottomLocation.getX(), leftBottomLocation.getY(), leftBottomLocation.getZ()));
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

        plugin.getLogger().info("开始异步创建 " + (FRAME_WIDTH * FRAME_HEIGHT * 2) + " 个背靠背 TextDisplay 实体（已添加 bad_apple 和 screen 标签）...");

        new BukkitRunnable() {
            private int x = 0;
            private int y = 0;

            @Override
            public void run() {
                int entitiesCreatedThisTick = 0;

                while (y < FRAME_HEIGHT && entitiesCreatedThisTick < ENTITIES_PER_TICK) {
                    // 创建背靠背的实体对
                    Vector3f translation = getPixelTranslation(x, y, 0);
                    
                    // 正面朝向（朝向玩家方向）
                    Quaternionf frontRotation = new Quaternionf().rotationYXZ((float) Math.toRadians(180), 0, 0);
                    TextDisplay frontDisplay = createPixelEntity(world, 0xFF000000, translation, frontRotation); // 黑色 ARGB
                    
                    // 背面朝向（背向玩家方向）
                    Quaternionf backRotation = new Quaternionf().rotationYXZ(0, 0, 0); // 不旋转，面向反方向
                    TextDisplay backDisplay = createPixelEntity(world, 0xFF000000, translation, backRotation); // 黑色 ARGB

                    textDisplayPairs.add(new TextDisplayPair(frontDisplay, backDisplay));
                    entitiesCreatedThisTick += 2;

                    x++;
                    if (x >= FRAME_WIDTH) {
                        x = 0;
                        y++;
                    }
                }

                if (y >= FRAME_HEIGHT) {
                    plugin.getLogger().info("背靠背 TextDisplay 实体创建完成！");
                    cancel();
                    Bukkit.getScheduler().runTask(plugin, onComplete);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private Vector3f getPixelTranslation(int x, int y, float zOffset) {
        double pixelSize = 0.06; // 像素间距
        return new Vector3f(
                (float) ((x - FRAME_WIDTH / 2.0) * pixelSize),
                (float) ((FRAME_HEIGHT / 2.0 - y) * pixelSize),
                zOffset
        );
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
            plugin.getLogger().info("正在清除 " + textDisplayPairs.size() * 2 + " 个背靠背 TextDisplay 实体...");
            for (TextDisplayPair pair : textDisplayPairs) {
                if (pair.front != null && !pair.front.isDead()) {
                    pair.front.remove();
                }
                if (pair.back != null && !pair.back.isDead()) {
                    pair.back.remove();
                }
            }
            textDisplayPairs.clear();
            plugin.getLogger().info("背靠背 TextDisplay 实体清除完成！");
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
                isPlaying = false;
                plugin.setPlaying(false);
                currentPlaybackTask = null; // 清理任务引用
                // if ("text".equals(playMode)) {
                //     clearTextDisplayEntities();
                // }
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
     * @param frameData 帧数据。
     */
    private void renderFrameTextDisplay(byte[][] frameData) {
        if (textDisplayPairs.size() != FRAME_WIDTH * FRAME_HEIGHT) return;

        // ARGB 颜色常量
        final int WHITE_ARGB = 0xFFFFFFFF; // 白色，完全不透明
        final int BLACK_ARGB = 0xFF000000; // 黑色，完全不透明

        for (int y = 0; y < FRAME_HEIGHT; y++) {
            for (int x = 0; x < FRAME_WIDTH; x++) {
                int pixelIndex = y * FRAME_WIDTH + x;
                TextDisplayPair pair = textDisplayPairs.get(pixelIndex);
                byte pixelValue = frameData[y][x];

                // 根据像素值直接设置背景颜色，同时更新正面和背面
                int backgroundColor = (pixelValue == 1) ? WHITE_ARGB : BLACK_ARGB;
                org.bukkit.Color color = org.bukkit.Color.fromARGB(backgroundColor);
                
                // 同时更新正面和背面实体的颜色
                pair.front.setBackgroundColor(color);
                pair.back.setBackgroundColor(color);
            }
        }
    }

    /**
     * 渲染方块模式的视频帧。
     * @param frameData 帧数据。
     */
    private void renderFrameBlock(byte[][] frameData) {
        World world = wallPosition.getWorld();
        if (world == null) return;
        for (int y = 0; y < FRAME_HEIGHT; y++) {
            for (int x = 0; x < FRAME_WIDTH; x++) {
                Location blockLoc = getBlockLocation(x, y);
                Material material = (frameData[y][x] == 1) ? Material.WHITE_CONCRETE : Material.BLACK_CONCRETE;
                world.getBlockAt(blockLoc).setType(material, false);
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
     */
    public void stopPlayback(String mode, boolean preserveFrame) {
        if (!isPlaying) return;
        
        isPlaying = false;
        plugin.setPlaying(false);
        
        // 取消当前的播放任务
        if (currentPlaybackTask != null && !currentPlaybackTask.isCancelled()) {
            currentPlaybackTask.cancel();
            currentPlaybackTask = null;
        }
        
        if (preserveFrame) {
            plugin.getLogger().info("已停止 " + ("text".equals(mode) ? "Text" : "Block") + " 模式播放并保留当前画面");
        } else {
            if ("text".equals(mode)) {
                clearTextDisplayEntities();
                plugin.getLogger().info("已停止 Text 模式播放并清除所有 TextDisplay 实体");
            } else if ("block".equals(mode)) {
                clearBlocks();
                plugin.getLogger().info("已停止 Block 模式播放并清除所有方块");
            }
        }
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
        
        for (int y = 0; y < FRAME_HEIGHT; y++) {
            for (int x = 0; x < FRAME_WIDTH; x++) {
                Location blockLoc = getBlockLocation(x, y);
                world.getBlockAt(blockLoc).setType(Material.AIR, false);
            }
        }
    }
}