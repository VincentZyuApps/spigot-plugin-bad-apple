package listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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
        
        // 苍白按钮 - 开始播放
        if (type == Material.PALE_OAK_BUTTON) {
            // 延迟播放声音
            int delay = Math.max(0, plugin.getSoundDelayTicks());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.startPlayback("text");
            });
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        "playsound music:music_disc.bad_apple music @a ~ ~ ~ 10000 1.0");
            }, delay);
        }
        // 黑石按钮 - 停止播放
        else if (type == Material.POLISHED_BLACKSTONE_BUTTON) {
            player.stopPlayback("text", true); // 保留当前画面，不清除实体
            plugin.resetCooldown();
            // 停止音乐
            if (e.getPlayer() != null) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        "stopsound " + e.getPlayer().getName() + " music music:music_disc.bad_apple");
            }
        }
    }
}
