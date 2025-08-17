package Command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import qwq.zyu.spigotPluginBadApple.SpigotPluginBadApple;
import qwq.zyu.spigotPluginBadApple.VideoPlayer;

public class stopBadAppleCommand implements CommandExecutor {

    private final SpigotPluginBadApple plugin;
    private VideoPlayer videoPlayer;

    public stopBadAppleCommand(SpigotPluginBadApple plugin, VideoPlayer videoPlayer) {
        this.plugin = plugin;
        this.videoPlayer = videoPlayer;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "此命令只能由玩家执行！");
            return true;
        }
        
        Player player = (Player) commandSender;
        
        // 获取停止模式参数
        if (strings.length == 0) {
            player.sendMessage(ChatColor.RED + "请指定要停止的播放模式！");
            player.sendMessage(ChatColor.YELLOW + "用法: /stop_bad_apple <text|block>");
            return true;
        }
        
        String stopMode = strings[0].toLowerCase();
        
        // 验证停止模式
        if (!stopMode.equals("block") && !stopMode.equals("text")) {
            player.sendMessage(ChatColor.RED + "无效的播放模式: " + stopMode);
            player.sendMessage(ChatColor.YELLOW + "支持的模式: block, text");
            return true;
        }
        
        // 检查是否正在播放
        if (!videoPlayer.isPlaying() && !plugin.isPlaying()) {
            player.sendMessage(ChatColor.YELLOW + "当前没有播放中的Bad Apple视频！");
            return true;
        }
        
        // 停止播放
        videoPlayer.stopPlayback(stopMode, true); // 保留当前画面，不清除实体
        // 停止该玩家的音乐
        plugin.getServer().dispatchCommand(
            plugin.getServer().getConsoleSender(),
            "stopsound " + player.getName() + " music music:music_disc.bad_apple");
        
        // 重置冷却时间
        plugin.resetCooldown();
        
        // 向所有在线玩家发送消息
        plugin.getServer().broadcastMessage(ChatColor.RED + "[Bad Apple] " + player.getName() + " 停止了Bad Apple播放！(模式: " + stopMode + ")");
        plugin.getServer().broadcastMessage(ChatColor.GREEN + "[Bad Apple] 冷却时间已重置，可以重新播放！");
        
        return true;
    }
}
