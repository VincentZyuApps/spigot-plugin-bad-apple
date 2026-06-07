package qwq.zyu.spigotPluginBadApple;

import Command.playBadAppleCommand;
import Command.stopBadAppleCommand;
import Command.debugTextDisplayChessCommand;
import Command.clearChessDebugCommand;
import Command.reloadBadAppleConfigCommand;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SpigotPluginBadApple extends JavaPlugin {

    private static SpigotPluginBadApple instance;
    private boolean isPlaying = false;
    private long lastPlayTime = 0;
    
    // 分别针对 text 和 block 模式的冷却时间
    private long lastTextPlayTime = 0;
    private long lastBlockPlayTime = 0;
    
    private Location wallPosition;
    private String wallDirection;
    private Location textPosition; // text模式的位置
    private String textDirection;  // text模式的方向
    private boolean videoEnabled;
    private int cooldownSeconds;
    private boolean enableAudio;
    private String audioSoundId;
    private boolean horizontalFlip; // 是否启用水平翻转
    private debugTextDisplayChessCommand chessCommand;
    private VideoPlayer videoPlayer;
    // Button controls
    private int soundDelayTicks;
    
    // Trigger configurations
    private boolean blockCommandStartEnabled;
    private boolean blockCommandStopEnabled;
    private boolean blockPressurePlateStartEnabled;
    private boolean blockPressurePlateStopEnabled;
    private boolean textCommandStartEnabled;
    private boolean textCommandStopEnabled;
    private boolean textButtonStartEnabled;
    private boolean textButtonStopEnabled;
    private Material textStartButtonMaterial;
    private Material textStopButtonMaterial;
    private Material blockStartPressurePlateMaterial;
    private Material blockStopPressurePlateMaterial;
    
    // Cleanup configurations
    private boolean blockClearOnComplete;
    private boolean blockClearOnStop;
    private boolean textClearOnComplete;
    private boolean textClearOnStop;
    
    // Text display configurations
    private boolean enableBothSide;

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

        Objects.requireNonNull(
                getCommand("reload_bad_apple_config")
        ).setExecutor(new reloadBadAppleConfigCommand(this));
        
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
        
        // 读取全局画面设置
        horizontalFlip = getConfig().getBoolean("video_settings.horizontal_flip", true);
        
        // 读取墙体位置配置 (block模式)
        double x = getConfig().getDouble("video_wall.position.x", 0.0);
        double y = getConfig().getDouble("video_wall.position.y", 64.0);
        double z = getConfig().getDouble("video_wall.position.z", 0.0);
        wallPosition = new Location(getServer().getWorlds().get(0), x, y, z);
        wallDirection = getConfig().getString("video_wall.direction", "NORTH");
        
        // 读取文本展示位置配置 (text模式)
        double textX = getConfig().getDouble("video_text.position.x", 100.0);
        double textY = getConfig().getDouble("video_text.position.y", 64.0);
        double textZ = getConfig().getDouble("video_text.position.z", 100.0);
        textPosition = new Location(getServer().getWorlds().get(0), textX, textY, textZ);
        textDirection = getConfig().getString("video_text.direction", "NORTH");
        enableBothSide = getConfig().getBoolean("video_text.enableBothSide", false);
        
        // 读取播放设置
        videoEnabled = getConfig().getBoolean("playback.enabled", true);
        cooldownSeconds = getConfig().getInt("playback.cooldown", 235);
        enableAudio = getConfig().getBoolean("playback.enableAudio", true);
        audioSoundId = getConfig().getString("playback.audioSoundId", "niacl:music_disc.bad_apple");

        // 读取按钮控制配置
        soundDelayTicks = getConfig().getInt("controls.sound_delay_ticks", 10);
        
        // 读取触发方式配置
        blockCommandStartEnabled = getConfig().getBoolean("triggers.block.command_start_enabled", true);
        blockCommandStopEnabled = getConfig().getBoolean("triggers.block.command_stop_enabled", true);
        blockPressurePlateStartEnabled = getConfig().getBoolean("triggers.block.pressure_plate_start_enabled", true);
        blockPressurePlateStopEnabled = getConfig().getBoolean("triggers.block.pressure_plate_stop_enabled", true);
        
        textCommandStartEnabled = getConfig().getBoolean("triggers.text.command_start_enabled", true);
        textCommandStopEnabled = getConfig().getBoolean("triggers.text.command_stop_enabled", true);
        textButtonStartEnabled = getConfig().getBoolean("triggers.text.button_start_enabled", true);
        textButtonStopEnabled = getConfig().getBoolean("triggers.text.button_stop_enabled", true);
        textStartButtonMaterial = parseMaterialConfig(
                "triggers.text.button_start_material",
                Material.PALE_OAK_BUTTON
        );
        textStopButtonMaterial = parseMaterialConfig(
                "triggers.text.button_stop_material",
                Material.POLISHED_BLACKSTONE_BUTTON
        );
        blockStartPressurePlateMaterial = parseMaterialConfig(
                "triggers.block.pressure_plate_start_material",
                Material.PALE_OAK_PRESSURE_PLATE
        );
        blockStopPressurePlateMaterial = parseMaterialConfig(
                "triggers.block.pressure_plate_stop_material",
                Material.POLISHED_BLACKSTONE_PRESSURE_PLATE
        );
        
        // 读取清理配置
        blockClearOnComplete = getConfig().getBoolean("cleanup.block.clear_on_complete", true);
        blockClearOnStop = getConfig().getBoolean("cleanup.block.clear_on_stop", true);
        textClearOnComplete = getConfig().getBoolean("cleanup.text.clear_on_complete", true);
        textClearOnStop = getConfig().getBoolean("cleanup.text.clear_on_stop", true);
        
        getLogger().info("配置已加载:");
        getLogger().info("Block模式 - 墙体位置(" + x + "," + y + "," + z + "), 朝向: " + wallDirection);
        getLogger().info("Text模式 - 墙体位置(" + textX + "," + textY + "," + textZ + "), 朝向: " + textDirection + ", 双面显示: " + enableBothSide);
        getLogger().info("触发方式配置已加载完成");
        getLogger().info("触发器材质 - Text[开始:" + textStartButtonMaterial + ", 停止:" + textStopButtonMaterial +
                "], Block[开始:" + blockStartPressurePlateMaterial + ", 停止:" + blockStopPressurePlateMaterial + "]");
        getLogger().info("清理配置 - Block[完成:" + blockClearOnComplete + ", 停止:" + blockClearOnStop + "], Text[完成:" + textClearOnComplete + ", 停止:" + textClearOnStop + "]");
    }

    private Material parseMaterialConfig(String path, Material defaultMaterial) {
        String rawValue = getConfig().getString(path, defaultMaterial.name());
        if (rawValue == null || rawValue.isBlank()) {
            return defaultMaterial;
        }

        Material material = Material.matchMaterial(rawValue.trim(), false);
        if (material == null) {
            getLogger().warning("无效材质配置 " + path + "=" + rawValue + "，回退到默认值 " + defaultMaterial);
            return defaultMaterial;
        }
        return material;
    }

    public boolean reloadPluginConfiguration() {
        loadConfiguration();

        if (videoPlayer == null) {
            getLogger().warning("VideoPlayer 尚未初始化，跳过视频帧重载");
            return false;
        }

        boolean framesLoaded = videoPlayer.loadFrames();
        if (framesLoaded) {
            getLogger().info("Bad Apple 插件配置与视频帧已重新加载");
        } else {
            getLogger().severe("配置已重新加载，但视频帧重载失败");
        }
        return framesLoaded;
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
    
    /**
     * 检查指定模式是否可以播放（考虑模式特定的冷却时间）
     */
    public boolean canPlay(String mode) {
        if (!videoEnabled) return false;
        
        long currentTime = System.currentTimeMillis();
        if ("text".equals(mode)) {
            long timePassed = (currentTime - lastTextPlayTime) / 1000;
            return timePassed >= cooldownSeconds;
        } else if ("block".equals(mode)) {
            long timePassed = (currentTime - lastBlockPlayTime) / 1000;
            return timePassed >= cooldownSeconds;
        }
        return canPlay(); // 后备方案
    }
    
    /**
     * 设置指定模式开始播放
     */
    public void setPlaying(String mode, boolean playing) {
        this.isPlaying = playing;
        if (playing) {
            long currentTime = System.currentTimeMillis();
            this.lastPlayTime = currentTime;
            
            if ("text".equals(mode)) {
                this.lastTextPlayTime = currentTime;
            } else if ("block".equals(mode)) {
                this.lastBlockPlayTime = currentTime;
            }
        }
    }
    
    /**
     * 获取指定模式的剩余冷却时间（秒）
     */
    public double getRemainingCooldown(String mode) {
        long currentTime = System.currentTimeMillis();
        double timePassed;
        
        if ("text".equals(mode)) {
            timePassed = (currentTime - lastTextPlayTime) / 1000.0;
        } else if ("block".equals(mode)) {
            timePassed = (currentTime - lastBlockPlayTime) / 1000.0;
        } else {
            timePassed = (currentTime - lastPlayTime) / 1000.0;
        }
        
        return Math.max(0, cooldownSeconds - timePassed);
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
    
    /**
     * 重置指定模式的冷却时间
     */
    public void resetCooldown(String mode) {
        this.isPlaying = false;
        this.lastPlayTime = 0;
        
        if ("text".equals(mode)) {
            this.lastTextPlayTime = 0;
        } else if ("block".equals(mode)) {
            this.lastBlockPlayTime = 0;
        }
    }

    public VideoPlayer getVideoPlayer() { return videoPlayer; }
    public int getSoundDelayTicks() { return soundDelayTicks; }
    public boolean isAudioEnabled() { return enableAudio; }
    public String getAudioSoundId() { return audioSoundId; }
    public String getPlaySoundCommand() {
        return "playsound " + audioSoundId + " record @a ~ ~ ~ 10000 1.0";
    }
    public String getStopSoundCommand() {
        return "stopsound @a record " + audioSoundId;
    }
    
    // Block 模式触发配置
    public boolean isBlockCommandStartEnabled() { return blockCommandStartEnabled; }
    public boolean isBlockCommandStopEnabled() { return blockCommandStopEnabled; }
    public boolean isBlockPressurePlateStartEnabled() { return blockPressurePlateStartEnabled; }
    public boolean isBlockPressurePlateStopEnabled() { return blockPressurePlateStopEnabled; }
    
    // Text 模式触发配置
    public boolean isTextCommandStartEnabled() { return textCommandStartEnabled; }
    public boolean isTextCommandStopEnabled() { return textCommandStopEnabled; }
    public boolean isTextButtonStartEnabled() { return textButtonStartEnabled; }
    public boolean isTextButtonStopEnabled() { return textButtonStopEnabled; }
    public Material getTextStartButtonMaterial() { return textStartButtonMaterial; }
    public Material getTextStopButtonMaterial() { return textStopButtonMaterial; }
    public Material getBlockStartPressurePlateMaterial() { return blockStartPressurePlateMaterial; }
    public Material getBlockStopPressurePlateMaterial() { return blockStopPressurePlateMaterial; }

    // 清理配置获取方法
    public boolean isBlockClearOnComplete() { return blockClearOnComplete; }
    public boolean isBlockClearOnStop() { return blockClearOnStop; }
    public boolean isTextClearOnComplete() { return textClearOnComplete; }
    public boolean isTextClearOnStop() { return textClearOnStop; }
    
    // Text display 配置获取方法
    public boolean isEnableBothSide() { return enableBothSide; }
    
    // 视频画面设置获取方法
    public boolean isHorizontalFlip() { return horizontalFlip; }

}
