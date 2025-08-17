package Command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import qwq.zyu.spigotPluginBadApple.SpigotPluginBadApple;
import qwq.zyu.spigotPluginBadApple.VideoPlayer;

public class playBadAppleCommand implements CommandExecutor {

    private final SpigotPluginBadApple plugin;
    private final VideoPlayer videoPlayer;

    public playBadAppleCommand(SpigotPluginBadApple plugin, VideoPlayer videoPlayer) { 
        this.plugin = plugin; 
        this.videoPlayer = videoPlayer;
    }
    
    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "此命令只能由玩家执行！");
            return true;
        }
        
        Player player = (Player) commandSender;
        
        // 获取播放模式参数，默认为 "text"
        String playMode = "text";
        if (strings.length > 0) {
            playMode = strings[0].toLowerCase();
        }
        
        // 验证播放模式
        if (!playMode.equals("block") && !playMode.equals("text")) {
            player.sendMessage(ChatColor.RED + "无效的播放模式: " + playMode);
            player.sendMessage(ChatColor.YELLOW + "当前支持的模式: block, text");
            return true;
        }
        
        // 检查是否可以播放（冷却时间检查）
        if (!plugin.canPlay()) {
            int remainingTime = plugin.getRemainingCooldown();
            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            
            player.sendMessage(ChatColor.YELLOW + "Bad Apple正在播放中或处于冷却期！");
            player.sendMessage(ChatColor.YELLOW + "剩余冷却时间: " + minutes + "分" + seconds + "秒");
            return true;
        }
        
        // 对所有在线玩家播放音乐 - text 模式下增加一点延迟以对齐画面
        final String playsoundCmd = "playsound music:music_disc.bad_apple music @a ~ ~ ~ 10000 1.0";
        final int configuredDelay = plugin.getSoundDelayTicks();
        if ("text".equals(playMode)) {
            int delayTicks = Math.max(0, configuredDelay); // 配置化延迟
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), playsoundCmd);
            }, delayTicks);
        } else {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), playsoundCmd);
        }
        
        // 向所有在线玩家发送消息
        plugin.getServer().broadcastMessage(ChatColor.GREEN + "[Bad Apple] " + player.getName() + " 开始播放Bad Apple！(模式: " + playMode + ")");
        plugin.getServer().broadcastMessage(ChatColor.YELLOW + "[Bad Apple] 请前往视频墙观看精彩演出！");
        
        // 开始视频播放
        final String finalPlayMode = playMode; // 创建final变量用于lambda
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            videoPlayer.startPlayback(finalPlayMode);
        });
        
        return true;
    }
}
