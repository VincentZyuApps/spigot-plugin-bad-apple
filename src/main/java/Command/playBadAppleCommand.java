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
    private VideoPlayer videoPlayer;

    public playBadAppleCommand(SpigotPluginBadApple plugin) { 
        this.plugin = plugin; 
        this.videoPlayer = new VideoPlayer(plugin);
        
        // 在插件启动时预加载视频帧数据
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (videoPlayer.loadFrames()) {
                plugin.getLogger().info("视频帧数据预加载完成！");
            } else {
                plugin.getLogger().severe("视频帧数据预加载失败！");
            }
        });
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "此命令只能由玩家执行！");
            return true;
        }
        
        Player player = (Player) commandSender;
        
        // 检查是否可以播放（冷却时间检查）
        if (!plugin.canPlay()) {
            int remainingTime = plugin.getRemainingCooldown();
            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            
            player.sendMessage(ChatColor.YELLOW + "Bad Apple正在播放中或处于冷却期！");
            player.sendMessage(ChatColor.YELLOW + "剩余冷却时间: " + minutes + "分" + seconds + "秒");
            return true;
        }
        
        // 播放音乐
        player.playSound(player.getLocation(), "minecraft:music_disc.11", 1.0f, 1.0f);
        
        // 向所有在线玩家发送消息
        plugin.getServer().broadcastMessage(ChatColor.GREEN + "[Bad Apple] " + player.getName() + " 开始播放Bad Apple！");
        plugin.getServer().broadcastMessage(ChatColor.YELLOW + "[Bad Apple] 请前往视频墙观看精彩演出！");
        
        // 开始视频播放
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            videoPlayer.startPlayback();
        });
        
        return true;
    }
}
