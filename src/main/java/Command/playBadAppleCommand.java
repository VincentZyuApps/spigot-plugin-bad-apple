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
        
        plugin.getLogger().info("[playBadApple] 命令执行者: " + senderType + " (" + senderName + ")");
        
        // 获取播放模式参数，默认为 "text"
        String playMode = "text";
        if (strings.length > 0) {
            playMode = strings[0].toLowerCase();
        }
        plugin.getLogger().info("[playBadApple] 请求播放模式: " + playMode);
        
        // 获取音频控制参数，默认使用配置文件中的值
        boolean enableAudio = plugin.isAudioEnabled(); // 默认值
        if (strings.length > 1) {
            String audioParam = strings[1].toLowerCase();
            if ("true".equals(audioParam) || "1".equals(audioParam) || "on".equals(audioParam)) {
                enableAudio = true;
            } else if ("false".equals(audioParam) || "0".equals(audioParam) || "off".equals(audioParam)) {
                enableAudio = false;
            } else {
                commandSender.sendMessage(ChatColor.RED + "无效的音频参数: " + audioParam);
                commandSender.sendMessage(ChatColor.YELLOW + "支持的音频参数: true/false, 1/0, on/off");
                plugin.getLogger().warning("[playBadApple] 无效的音频参数: " + audioParam);
                return true;
            }
        }
        plugin.getLogger().info("[playBadApple] 音频控制: " + (enableAudio ? "启用" : "禁用"));
        
        // 验证播放模式
        if (!playMode.equals("block") && !playMode.equals("text")) {
            commandSender.sendMessage(ChatColor.RED + "无效的播放模式: " + playMode);
            commandSender.sendMessage(ChatColor.YELLOW + "当前支持的模式: block, text");
            plugin.getLogger().warning("[playBadApple] 无效的播放模式: " + playMode);
            return true;
        }
        
        // 检查是否允许通过指令触发该模式
        if ("block".equals(playMode) && !plugin.isBlockCommandStartEnabled()) {
            commandSender.sendMessage(ChatColor.RED + "指令触发 Block 模式开始播放已被禁用！");
            plugin.getLogger().warning("[playBadApple] Block 模式指令开始已被禁用");
            return true;
        }
        if ("text".equals(playMode) && !plugin.isTextCommandStartEnabled()) {
            commandSender.sendMessage(ChatColor.RED + "指令触发 Text 模式开始播放已被禁用！");
            plugin.getLogger().warning("[playBadApple] Text 模式指令开始已被禁用");
            return true;
        }
        
        // 检查是否可以播放（冷却时间检查）
        if (!plugin.canPlay(playMode)) {
            double remainingTime = plugin.getRemainingCooldown(playMode);
            
            commandSender.sendMessage(ChatColor.YELLOW + "Bad Apple " + playMode.toUpperCase() + " 模式正在播放中或处于冷却期！");
            commandSender.sendMessage(ChatColor.YELLOW + String.format("剩余冷却时间: %.2f 秒", remainingTime));
            plugin.getLogger().info("[playBadApple] " + playMode + " 模式正在冷却中，剩余时间: " + String.format("%.2f", remainingTime) + " 秒");
            return true;
        }
        
        // 对所有在线玩家播放音乐 - text 模式下增加一点延迟以对齐画面
        if (enableAudio) {
            final String playsoundCmd = "playsound music:music_disc.bad_apple music @a ~ ~ ~ 10000 1.0";
            final int configuredDelay = plugin.getSoundDelayTicks();
            if ("text".equals(playMode)) {
                int delayTicks = Math.max(0, configuredDelay); // 配置化延迟
                plugin.getLogger().info("[playBadApple] Text 模式播放音乐，延迟: " + delayTicks + " ticks");
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), playsoundCmd);
                }, delayTicks);
            } else {
                plugin.getLogger().info("[playBadApple] Block 模式播放音乐，无延迟");
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), playsoundCmd);
            }
        } else {
            plugin.getLogger().info("[playBadApple] 音频已禁用，跳过音乐播放");
        }
        
        // 向所有在线玩家发送消息
        String executorDisplayName = commandSender instanceof Player ? 
            commandSender.getName() : senderType;
        plugin.getServer().broadcastMessage(ChatColor.GREEN + "[Bad Apple] " + executorDisplayName + " 开始播放Bad Apple！(模式: " + playMode + ")");
        plugin.getServer().broadcastMessage(ChatColor.YELLOW + "[Bad Apple] 请前往视频墙观看精彩演出！");
        
        // 开始视频播放
        final String finalPlayMode = playMode; // 创建final变量用于lambda
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            videoPlayer.startPlayback(finalPlayMode);
        });
        
        plugin.getLogger().info("[playBadApple] 成功开始播放，模式: " + playMode + "，执行者: " + senderType + " (" + senderName + ")");
        return true;
    }
}
