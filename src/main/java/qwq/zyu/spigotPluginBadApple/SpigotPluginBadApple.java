package qwq.zyu.spigotPluginBadApple;

import Command.playBadAppleCommand;
import Command.stopBadAppleCommand;
import Command.debugTextDisplayChessCommand;
import Command.clearChessDebugCommand;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SpigotPluginBadApple extends JavaPlugin {

    private static SpigotPluginBadApple instance;
    private boolean isPlaying = false;
    private long lastPlayTime = 0;
    private Location wallPosition;
    private String wallDirection;
    private Location textPosition; // text模式的位置
    private String textDirection;  // text模式的方向
    private boolean videoEnabled;
    private int cooldownSeconds;
    private debugTextDisplayChessCommand chessCommand;
    private VideoPlayer videoPlayer;
    // Button controls
    private int soundDelayTicks;

    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默认配置文件
        saveDefaultConfig();
        
        // 加载配置
        loadConfiguration();
        
        // 创建共享的VideoPlayer实例
        videoPlayer = new VideoPlayer(this);
        // 异步预加载视频帧
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            if (videoPlayer.loadFrames()) {
                getLogger().info("视频帧数据预加载完成！");
            } else {
                getLogger().severe("视频帧数据预加载失败！");
            }
        });
        
        // Plugin startup logic
        playBadAppleCommand playCommand = new playBadAppleCommand(this, videoPlayer);
        Objects.requireNonNull(
                getCommand("play_bad_apple")
        ).setExecutor(playCommand);
        
        // 注册停止播放命令
        Objects.requireNonNull(
                getCommand("stop_bad_apple")
        ).setExecutor(new stopBadAppleCommand(this, videoPlayer));
        
        // 注册调试命令
        chessCommand = new debugTextDisplayChessCommand(this);
        Objects.requireNonNull(
                getCommand("debug_text_display_chess")
        ).setExecutor(chessCommand);
        
        Objects.requireNonNull(
                getCommand("clear_chess_debug")
        ).setExecutor(new clearChessDebugCommand(this, chessCommand));
        
    // 注册按钮监听
    getServer().getPluginManager().registerEvents(
        new listeners.ButtonControlListener(this, videoPlayer), this);

    getLogger().info("Bad Apple Plugin 已启用！");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        instance = null;
        getLogger().info("Bad Apple Plugin 已禁用！");
    }
    
    public static SpigotPluginBadApple getInstance() {
        return instance;
    }
    
    private void loadConfiguration() {
        reloadConfig();
        
        // 读取墙体位置配置 (block模式)
        int x = getConfig().getInt("video_wall.position.x", 0);
        int y = getConfig().getInt("video_wall.position.y", 64);
        int z = getConfig().getInt("video_wall.position.z", 0);
        wallPosition = new Location(getServer().getWorlds().get(0), x, y, z);
        wallDirection = getConfig().getString("video_wall.direction", "NORTH");
        
        // 读取文本展示位置配置 (text模式)
        int textX = getConfig().getInt("video_text.position.x", 100);
        int textY = getConfig().getInt("video_text.position.y", 64);
        int textZ = getConfig().getInt("video_text.position.z", 100);
        textPosition = new Location(getServer().getWorlds().get(0), textX, textY, textZ);
        textDirection = getConfig().getString("video_text.direction", "NORTH");
        
        // 读取播放设置
        videoEnabled = getConfig().getBoolean("playback.enabled", true);
        cooldownSeconds = getConfig().getInt("playback.cooldown", 235);

        // 读取按钮控制配置
        soundDelayTicks = getConfig().getInt("controls.sound_delay_ticks", 10);        getLogger().info("配置已加载:");
        getLogger().info("Block模式 - 墙体位置(" + x + "," + y + "," + z + "), 朝向: " + wallDirection);
        getLogger().info("Text模式 - 墙体位置(" + textX + "," + textY + "," + textZ + "), 朝向: " + textDirection);
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        if (playing) {
            this.lastPlayTime = System.currentTimeMillis();
        }
    }
    
    public boolean canPlay() {
        if (!videoEnabled) return false;
        if (!isPlaying) return true;
        
        long currentTime = System.currentTimeMillis();
        long timePassed = (currentTime - lastPlayTime) / 1000; // 转换为秒
        return timePassed >= cooldownSeconds;
    }
    
    public Location getWallPosition() {
        return wallPosition;
    }
    
    public String getWallDirection() {
        return wallDirection;
    }
    
    // 根据播放模式获取位置
    public Location getWallPosition(String mode) {
        return "text".equals(mode) ? textPosition : wallPosition;
    }
    
    // 根据播放模式获取方向
    public String getWallDirection(String mode) {
        return "text".equals(mode) ? textDirection : wallDirection;
    }
    
    public int getRemainingCooldown() {
        if (!isPlaying) return 0;
        
        long currentTime = System.currentTimeMillis();
        long timePassed = (currentTime - lastPlayTime) / 1000;
        return Math.max(0, cooldownSeconds - (int)timePassed);
    }
    
    /**
     * 重置冷却时间
     */
    public void resetCooldown() {
        this.isPlaying = false;
        this.lastPlayTime = 0;
    }

    public VideoPlayer getVideoPlayer() { return videoPlayer; }
    public int getSoundDelayTicks() { return soundDelayTicks; }

}
