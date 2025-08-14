package qwq.zyu.spigotPluginBadApple;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VideoPlayer {
    
    private static final int FRAME_WIDTH = 384;
    private static final int FRAME_HEIGHT = 216;
    private static final int FRAMES_PER_SECOND = 5; // 每秒5帧
    private static final int TICKS_PER_FRAME = 4; // 每4tick更新一次 (20tick/s ÷ 5fps = 4tick/frame)
    
    private final SpigotPluginBadApple plugin;
    private final List<byte[][]> frames;
    private Location wallPosition;
    private String direction;
    private boolean isPlaying = false;
    
    public VideoPlayer(SpigotPluginBadApple plugin) {
        this.plugin = plugin;
        this.frames = new ArrayList<>();
    }
    
    /**
     * 从bin.zip文件加载所有帧数据到内存
     */
    public boolean loadFrames() {
        frames.clear();
        
        try {
            // 获取资源文件
            InputStream resourceStream = plugin.getResource("assets/bin.zip");
            if (resourceStream == null) {
                plugin.getLogger().severe("无法找到 assets/bin.zip 文件！");
                return false;
            }
            
            ZipInputStream zipStream = new ZipInputStream(resourceStream);
            ZipEntry entry;
            
            plugin.getLogger().info("开始加载视频帧数据...");
            
            int frameIndex = 0;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".bin")) {
                    // 只读取每3个文件中的第1个 (0, 3, 6, 9, ...)
                    if (frameIndex % 3 == 0) {
                        byte[][] frameData = loadFrameFromStream(zipStream);
                        if (frameData != null) {
                            frames.add(frameData);
                        }
                    } else {
                        // 跳过不需要的帧，但仍需要读取数据以移动到下一个entry
                        skipFrameData(zipStream);
                    }
                    frameIndex++;
                }
                zipStream.closeEntry();
            }
            
            zipStream.close();
            resourceStream.close();
            
            plugin.getLogger().info("成功加载 " + frames.size() + " 帧视频数据到内存");
            return !frames.isEmpty();
            
        } catch (IOException e) {
            plugin.getLogger().severe("加载视频帧数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 从输入流读取单帧数据
     */
    private byte[][] loadFrameFromStream(InputStream stream) throws IOException {
        // 读取文件头 (8字节: 4字节宽度 + 4字节高度)
        byte[] header = new byte[8];
        int headerBytesRead = 0;
        while (headerBytesRead < 8) {
            int bytesRead = stream.read(header, headerBytesRead, 8 - headerBytesRead);
            if (bytesRead == -1) {
                return null;
            }
            headerBytesRead += bytesRead;
        }
        
        // 解析宽度和高度 (大端序)
        int width = ((header[0] & 0xFF) << 24) | ((header[1] & 0xFF) << 16) | 
                   ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
        int height = ((header[4] & 0xFF) << 24) | ((header[5] & 0xFF) << 16) | 
                    ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
        
        if (width != FRAME_WIDTH || height != FRAME_HEIGHT) {
            plugin.getLogger().warning("帧尺寸不匹配: " + width + "x" + height + ", 期望: " + FRAME_WIDTH + "x" + FRAME_HEIGHT);
            return null;
        }
        
        // 读取像素数据
        byte[] pixelData = new byte[width * height];
        int pixelBytesRead = 0;
        while (pixelBytesRead < pixelData.length) {
            int bytesRead = stream.read(pixelData, pixelBytesRead, pixelData.length - pixelBytesRead);
            if (bytesRead == -1) {
                plugin.getLogger().warning("像素数据读取不完整: 期望 " + pixelData.length + " 字节，实际读取 " + pixelBytesRead + " 字节");
                return null;
            }
            pixelBytesRead += bytesRead;
        }
        
        // 转换为二维数组
        byte[][] frameMatrix = new byte[height][width];
        for (int y = 0; y < height; y++) {
            System.arraycopy(pixelData, y * width, frameMatrix[y], 0, width);
        }
        
        return frameMatrix;
    }
    
    /**
     * 跳过帧数据（用于不需要加载的帧）
     */
    private void skipFrameData(InputStream stream) throws IOException {
        // 读取并丢弃文件头 (8字节)
        stream.skip(8);
        // 读取并丢弃像素数据 (384 * 216 字节)
        stream.skip(FRAME_WIDTH * FRAME_HEIGHT);
    }
    
    /**
     * 开始播放视频
     */
    public void startPlayback() {
        if (isPlaying) {
            return;
        }
        
        if (frames.isEmpty()) {
            plugin.getLogger().warning("没有加载视频帧数据，无法播放");
            return;
        }
        
        wallPosition = plugin.getWallPosition();
        direction = plugin.getWallDirection();
        
        if (wallPosition == null) {
            plugin.getLogger().severe("墙体位置未配置！");
            return;
        }
        
        isPlaying = true;
        plugin.setPlaying(true);
        
        plugin.getLogger().info("开始播放Bad Apple视频，共 " + frames.size() + " 帧");
        
        // 启动播放任务
        new VideoPlaybackTask().runTaskTimer(plugin, 0L, TICKS_PER_FRAME);
    }
    
    /**
     * 根据朝向和坐标计算实际方块位置
     */
    private Location getBlockLocation(int frameX, int frameY) {
        World world = wallPosition.getWorld();
        int baseX = wallPosition.getBlockX();
        int baseY = wallPosition.getBlockY();
        int baseZ = wallPosition.getBlockZ();
        
        // 计算实际坐标，frameY需要反转因为Minecraft的Y轴向上
        int actualY = baseY + (FRAME_HEIGHT - 1 - frameY);

        return switch (direction.toUpperCase()) {
            case "NORTH" -> // 面向北，墙体在Z轴负方向
                    new Location(world, baseX + frameX, actualY, baseZ);
            case "SOUTH" -> // 面向南，墙体在Z轴正方向
                    new Location(world, baseX + (FRAME_WIDTH - 1 - frameX), actualY, baseZ);
            case "EAST" -> // 面向东，墙体在X轴正方向
                    new Location(world, baseX, actualY, baseZ + frameX);
            case "WEST" -> // 面向西，墙体在X轴负方向
                    new Location(world, baseX, actualY, baseZ + (FRAME_WIDTH - 1 - frameX));
            default -> new Location(world, baseX + frameX, actualY, baseZ);
        };
    }
    
    /**
     * 视频播放任务
     */
    private class VideoPlaybackTask extends BukkitRunnable {
        private int currentFrame = 0;
        
        @Override
        public void run() {
            if (currentFrame >= frames.size()) {
                // 播放完成
                isPlaying = false;
                plugin.setPlaying(false);
                plugin.getLogger().info("Bad Apple视频播放完成！");
                cancel();
                return;
            }
            
            // 渲染当前帧
            renderFrame(frames.get(currentFrame));
            currentFrame++;
        }
    }
    
    /**
     * 渲染单帧到墙体
     */
    private void renderFrame(byte[][] frameData) {
        World world = wallPosition.getWorld();
        if (world == null) {
            plugin.getLogger().severe("世界不存在！");
            return;
        }
        
        // 批量更新方块
        for (int y = 0; y < FRAME_HEIGHT; y++) {
            for (int x = 0; x < FRAME_WIDTH; x++) {
                Location blockLoc = getBlockLocation(x, y);
                Block block = world.getBlockAt(blockLoc);
                
                // 根据像素值设置方块类型
                Material material = (frameData[y][x] == 1) ? Material.WHITE_CONCRETE : Material.BLACK_CONCRETE;
                block.setType(material);
            }
        }
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
}