package Command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import qwq.zyu.spigotPluginBadApple.SpigotPluginBadApple;

public class clearChessDebugCommand implements CommandExecutor {

    private final SpigotPluginBadApple plugin;
    private final debugTextDisplayChessCommand chessCommand;

    public clearChessDebugCommand(SpigotPluginBadApple plugin, debugTextDisplayChessCommand chessCommand) {
        this.plugin = plugin;
        this.chessCommand = chessCommand;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "此命令只能由玩家执行！");
            return true;
        }

        Player player = (Player) commandSender;
        
        int boardSize = chessCommand.getChessBoard().size();
        chessCommand.clearChessBoard();
        
        player.sendMessage(ChatColor.GREEN + "已清除 " + boardSize + " 个棋盘实体。");
        
        return true;
    }
}