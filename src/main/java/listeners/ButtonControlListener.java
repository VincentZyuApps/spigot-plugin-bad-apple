package listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import qwq.zyu.spigotPluginBadApple.SpigotPluginBadApple;
import qwq.zyu.spigotPluginBadApple.VideoPlayer;

public class ButtonControlListener implements Listener {
    private final SpigotPluginBadApple plugin;
    private final VideoPlayer player;

    public ButtonControlListener(SpigotPluginBadApple plugin, VideoPlayer player) {
        this.plugin = plugin;
        this.player = player;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        
        Material type = block.getType();
        if (type != Material.PALE_OAK_BUTTON && type != Material.POLISHED_BLACKSTONE_BUTTON) return;

        e.setCancelled(true);
        
        // 苍白按钮 - 开始播放 text 模式
        if (type == Material.PALE_OAK_BUTTON) {
            e.getPlayer().sendMessage("§b[Bad Apple] 你按下了苍白橡木按钮，尝试进行：Text 模式开始播放");
            
            if (!plugin.isTextButtonStartEnabled()) {
                e.getPlayer().sendMessage("§c[Bad Apple] 按钮触发 Text 模式开始播放已被禁用！");
                return;
            }
            
            // 检查冷却时间
            if (!plugin.canPlay("text")) {
                double remainingTime = plugin.getRemainingCooldown("text");
                e.getPlayer().sendMessage(String.format("§e冷却中，还剩 %.2f 秒", remainingTime));
                return;
            }
            
            // 延迟播放声音
            int delay = Math.max(0, plugin.getSoundDelayTicks());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.startPlayback("text");
            });
            
            // 只有启用音频时才播放声音
            if (plugin.isAudioEnabled()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                            "playsound niacl:music_disc.bad_apple record @a ~ ~ ~ 10000 1.0");
                }, delay);
            }
        }
        // 黑石按钮 - 停止播放 text 模式
        else if (type == Material.POLISHED_BLACKSTONE_BUTTON) {
            e.getPlayer().sendMessage("§b[Bad Apple] 你按下了黑石按钮，尝试进行：Text 模式停止播放");
            
            if (!plugin.isTextButtonStopEnabled()) {
                e.getPlayer().sendMessage("§c[Bad Apple] 按钮触发 Text 模式停止播放已被禁用！");
                return;
            }
            
            player.stopPlayback("text", true, true); // 第三个参数表示手动停止
            plugin.resetCooldown("text"); // 重置 text 模式的冷却时间
            
            // 只有启用音频时才停止声音
            if (plugin.isAudioEnabled()) {
                // 停止音乐
                if (e.getPlayer() != null) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                            "stopsound @a record niacl:music_disc.bad_apple");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Block blockFrom = e.getFrom().getBlock().getRelative(0, -1, 0); // 脚下方块
        Block blockTo = e.getTo() != null ? e.getTo().getBlock().getRelative(0, -1, 0) : null;
        
        // 检查是否踩到压力板
        if (blockTo != null && blockFrom != blockTo) {
            Material type = blockTo.getType();
            
            // 苍白橡木压力板 - 开始播放 block 模式
            if (type == Material.PALE_OAK_PRESSURE_PLATE) {
                e.getPlayer().sendMessage("§b[Bad Apple] 你踩下了苍白橡木压力板，尝试进行：Block 模式开始播放");
                
                if (!plugin.isBlockPressurePlateStartEnabled()) {
                    e.getPlayer().sendMessage("§c[Bad Apple] 压力板触发 Block 模式开始播放已被禁用！");
                    return;
                }
                
                // 检查冷却时间
                if (!plugin.canPlay("block")) {
                    double remainingTime = plugin.getRemainingCooldown("block");
                    e.getPlayer().sendMessage(String.format("§e冷却中，还剩 %.2f 秒", remainingTime));
                    return;
                }
                
                // 延迟播放声音
                int delay = Math.max(0, plugin.getSoundDelayTicks());
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.startPlayback("block");
                });
                
                // 只有启用音频时才播放声音
                if (plugin.isAudioEnabled()) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                                "playsound niacl:music_disc.bad_apple record @a ~ ~ ~ 10000 1.0");
                    }, delay);
                }
                
                e.getPlayer().sendMessage("§a[Bad Apple] 开始播放 Block 模式！");
            }
            // 黑石压力板 - 停止播放 block 模式
            else if (type == Material.POLISHED_BLACKSTONE_PRESSURE_PLATE) {
                e.getPlayer().sendMessage("§b[Bad Apple] 你踩下了黑石压力板，尝试进行：Block 模式停止播放");
                
                if (!plugin.isBlockPressurePlateStopEnabled()) {
                    e.getPlayer().sendMessage("§c[Bad Apple] 压力板触发 Block 模式停止播放已被禁用！");
                    return;
                }
                
                player.stopPlayback("block", true, true); // 第三个参数表示手动停止
                plugin.resetCooldown("block"); // 重置 block 模式的冷却时间
                
                // 只有启用音频时才停止声音
                if (plugin.isAudioEnabled()) {
                    // 停止音乐
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                            "stopsound @a record niacl:music_disc.bad_apple");
                }
                
                e.getPlayer().sendMessage("§c[Bad Apple] 停止播放 Block 模式！");
            }
        }
    }
}
