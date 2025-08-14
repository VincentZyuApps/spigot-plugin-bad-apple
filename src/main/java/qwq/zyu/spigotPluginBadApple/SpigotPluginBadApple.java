package qwq.zyu.spigotPluginBadApple;

import Command.playBadAppleCommand;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SpigotPluginBadApple extends JavaPlugin {

    private static SpigotPluginBadApple instance;
    private boolean isPlaying = false;
    private long lastPlayTime = 0;
    private Location wallPosition;
    private String wallDirection;
    private boolean videoEnabled;
    private int cooldownSeconds;

    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默认配置文件
        saveDefaultConfig();
        
        // 加载配置
        loadConfiguration();
        
        // Plugin startup logic
        Objects.requireNonNull(
                getCommand("play_bad_apple")
        ).setExecutor(new playBadAppleCommand(this));
        
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
        
        // 读取墙体位置配置
        int x = getConfig().getInt("video_wall.position.x", 0);
        int y = getConfig().getInt("video_wall.position.y", 64);
        int z = getConfig().getInt("video_wall.position.z", 0);
        wallPosition = new Location(getServer().getWorlds().get(0), x, y, z);
        
        // 读取墙体朝向
        wallDirection = getConfig().getString("video_wall.direction", "NORTH");
        
        // 读取播放设置
        videoEnabled = getConfig().getBoolean("playback.enabled", true);
        cooldownSeconds = getConfig().getInt("playback.cooldown", 235);
        
        getLogger().info("配置已加载: 墙体位置(" + x + "," + y + "," + z + "), 朝向: " + wallDirection);
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
    
    public int getRemainingCooldown() {
        if (!isPlaying) return 0;
        
        long currentTime = System.currentTimeMillis();
        long timePassed = (currentTime - lastPlayTime) / 1000;
        return Math.max(0, cooldownSeconds - (int)timePassed);
    }
}
