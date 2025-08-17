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
        // 记录命令执行日志
        String senderType = "Unknown";
        String senderName = commandSender.getName();
        
        if (commandSender instanceof Player) {
            senderType = "Player";
        } else if (commandSender.equals(plugin.getServer().getConsoleSender())) {
            senderType = "Console";
        } else {
            senderType = "CommandBlock";
        }
        
        plugin.getLogger().info("[stopBadApple] 命令执行者: " + senderType + " (" + senderName + ")");
        
        // 获取停止模式参数，默认为 "text"
        String stopMode = "text";
        if (strings.length > 0) {
            stopMode = strings[0].toLowerCase();
        }
        plugin.getLogger().info("[stopBadApple] 请求停止模式: " + stopMode);
        
        // 验证停止模式
        if (!stopMode.equals("block") && !stopMode.equals("text")) {
            commandSender.sendMessage(ChatColor.RED + "无效的播放模式: " + stopMode);
            commandSender.sendMessage(ChatColor.YELLOW + "支持的模式: block, text");
            plugin.getLogger().warning("[stopBadApple] 无效的播放模式: " + stopMode);
            return true;
        }
        
        // 检查是否允许通过指令触发该模式的停止
        if ("block".equals(stopMode) && !plugin.isBlockCommandStopEnabled()) {
            commandSender.sendMessage(ChatColor.RED + "指令触发 Block 模式停止播放已被禁用！");
            plugin.getLogger().warning("[stopBadApple] Block 模式指令停止已被禁用");
            return true;
        }
        if ("text".equals(stopMode) && !plugin.isTextCommandStopEnabled()) {
            commandSender.sendMessage(ChatColor.RED + "指令触发 Text 模式停止播放已被禁用！");
            plugin.getLogger().warning("[stopBadApple] Text 模式指令停止已被禁用");
            return true;
        }
        
        // 检查是否正在播放
        if (!videoPlayer.isPlaying() && !plugin.isPlaying()) {
            commandSender.sendMessage(ChatColor.YELLOW + "当前没有播放中的Bad Apple视频！");
            plugin.getLogger().info("[stopBadApple] 当前没有播放中的视频");
            return true;
        }
        
        // 停止播放
        videoPlayer.stopPlayback(stopMode, true); // 保留当前画面，不清除实体
        plugin.getLogger().info("[stopBadApple] 成功停止播放，模式: " + stopMode);
        
        // 停止所有玩家的音乐
        if (plugin.isAudioEnabled()) {
            plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "stopsound @a music music:music_disc.bad_apple");
            plugin.getLogger().info("[stopBadApple] 已停止所有玩家的音乐播放");
        } else {
            plugin.getLogger().info("[stopBadApple] 音频已禁用，跳过音乐停止");
        }
        
        // 重置对应模式的冷却时间
        plugin.resetCooldown(stopMode);
        plugin.getLogger().info("[stopBadApple] 已重置 " + stopMode + " 模式的冷却时间");
        
        // 向所有在线玩家发送消息
        String executorDisplayName = commandSender instanceof Player ? 
            commandSender.getName() : senderType;
        plugin.getServer().broadcastMessage(ChatColor.RED + "[Bad Apple] " + executorDisplayName + " 停止了Bad Apple播放！(模式: " + stopMode + ")");
        plugin.getServer().broadcastMessage(ChatColor.GREEN + "[Bad Apple] 冷却时间已重置，可以重新播放！");
        
        plugin.getLogger().info("[stopBadApple] 命令执行完成，执行者: " + senderType + " (" + senderName + ")");
        return true;
    }
}
