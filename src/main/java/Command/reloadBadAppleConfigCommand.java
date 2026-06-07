package Command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import qwq.zyu.spigotPluginBadApple.SpigotPluginBadApple;

public class reloadBadAppleConfigCommand implements CommandExecutor {

    private final SpigotPluginBadApple plugin;

    public reloadBadAppleConfigCommand(SpigotPluginBadApple plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String senderType;
        String senderName = commandSender.getName();

        if (commandSender instanceof Player) {
            senderType = "Player";
        } else if (commandSender.equals(plugin.getServer().getConsoleSender())) {
            senderType = "Console";
        } else {
            senderType = "CommandBlock";
        }

        plugin.getLogger().info("[reloadBadAppleConfig] 命令执行者: " + senderType + " (" + senderName + ")");

        if (plugin.getVideoPlayer() != null && plugin.getVideoPlayer().isPlaying()) {
            commandSender.sendMessage(ChatColor.RED + "Bad Apple 正在播放中，请先停止播放后再重载配置。");
            plugin.getLogger().warning("[reloadBadAppleConfig] 播放中拒绝重载配置");
            return true;
        }

        boolean success = plugin.reloadPluginConfiguration();
        if (success) {
            commandSender.sendMessage(ChatColor.GREEN + "Bad Apple 配置已重载，视频帧缓存也已刷新。");
            plugin.getLogger().info("[reloadBadAppleConfig] 配置重载成功");
        } else {
            commandSender.sendMessage(ChatColor.RED + "配置已重载，但视频帧缓存刷新失败，请检查控制台日志。");
            plugin.getLogger().severe("[reloadBadAppleConfig] 配置重载部分失败：视频帧缓存刷新失败");
        }

        return true;
    }
}
