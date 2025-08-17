package Command;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import qwq.zyu.spigotPluginBadApple.SpigotPluginBadApple;

import java.util.ArrayList;
import java.util.List;

public class debugTextDisplayChessCommand implements CommandExecutor {

    private final SpigotPluginBadApple plugin;
    private final List<TextDisplay> chessBoard = new ArrayList<>();

    public debugTextDisplayChessCommand(SpigotPluginBadApple plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "此命令只能由玩家执行！");
            return true;
        }

        Player player = (Player) commandSender;

        // 1. 清理旧棋盘
        clearChessBoard();

        player.sendMessage(ChatColor.GREEN + "正在创建棋盘...");

        // 2. 解析参数并定义属性
        double squareSize = 0.3; // 默认值
        float scaleMultiplier = 5f; // 默认值
        final int boardSize = 3;

        // 解析命令参数
        if (strings.length >= 1) {
            try {
                squareSize = Double.parseDouble(strings[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "无效的 squareSize 参数: " + strings[0]);
                return true;
            }
        }
        
        if (strings.length >= 2) {
            try {
                scaleMultiplier = Float.parseFloat(strings[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "无效的 scaleMultiplier 参数: " + strings[1]);
                return true;
            }
        }

        player.sendMessage(ChatColor.YELLOW + String.format("使用参数 - squareSize: %.3f, scaleMultiplier: %.1f", squareSize, scaleMultiplier));

        // 3. 确定棋盘位置和朝向
        // 棋盘中心将位于玩家正北方5格，与玩家视线等高
        Location playerLoc = player.getLocation();
        // 固定朝向正北方向 (0, 0, -1)，不受玩家朝向影响
        Location boardCenter = player.getEyeLocation().add(0, 0, -5);

        // 棋盘固定朝向正北 (Z轴负方向)
        float yaw = 180; // 180度Yaw对应Minecraft中的正北方
        org.joml.Quaternionf boardRotation = new org.joml.Quaternionf().rotationYXZ(
                (float) Math.toRadians(yaw),
                0, // Pitch为0，保持垂直
                0  // Roll为0
        );

        // 4. 循环创建方块
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {

                // 计算每个方块相对于棋盘中心的本地坐标
                Vector3f localOffset = new Vector3f(
                        (float) ((col - 1) * squareSize), // X: -1, 0, 1
                        (float) ((row - 1) * squareSize), // Y: -1, 0, 1
                        0                                 // Z: 0
                );

                // 将本地坐标根据棋盘的朝向进行旋转，得到世界坐标中的位移
                Vector3f worldTranslation = boardRotation.transform(new Vector3f(localOffset));

                // 为了实现双面可见，我们为每个方块创建两个TextDisplay实体
                for (int i = 0; i < 2; i++) {
                    // 所有实体都生成在同一个锚点
                    TextDisplay textDisplay = player.getWorld().spawn(boardCenter, TextDisplay.class);

                    // 使用一个空格作为文本，来显示背景颜色
                    textDisplay.setText("  ");

                    // 设置黑白相间的背景色
                    boolean isBlack = (row + col) % 2 == 0;
                    Color backgroundColor = isBlack ? Color.BLACK : Color.WHITE;
                    textDisplay.setBackgroundColor(backgroundColor);

                    // 计算最终的位移，背面稍微向后偏移以避免闪烁
                    Vector3f finalTranslation = new Vector3f(worldTranslation);
                    if (i == 1) { // Back side - 向后偏移一点点
                        Vector3f backOffset = boardRotation.transform(new Vector3f(0, 0, -0.001f));
                        finalTranslation.add(backOffset);
                    }

                    // 背面需要额外旋转180度
                    org.joml.Quaternionf finalRotation = new org.joml.Quaternionf(boardRotation);
                    if (i == 1) { // Back side
                        finalRotation.rotateY((float) Math.PI);
                    }

                    // 设置变换
                    // 使用传入的缩放倍数，X方向稍微放大以消除列间隙
                    float visualScaleY = (float) squareSize * scaleMultiplier;  // Y方向（水平）
                    float visualScaleX = visualScaleY * 1.09f;  // X方向（竖直）稍微放大
                    float visualScaleZ = visualScaleY;  // Z方向保持一致
                    Transformation transformation = new Transformation(
                            finalTranslation,
                            new AxisAngle4f(finalRotation),
                            new Vector3f(visualScaleX, visualScaleY, visualScaleZ),
                            new AxisAngle4f(0, 0, 0, 1)
                    );
                    textDisplay.setTransformation(transformation);

                    // 固定朝向
                    textDisplay.setBillboard(Display.Billboard.FIXED);
                    textDisplay.setSeeThrough(false);
                    textDisplay.setShadowed(false);

                    // 添加到列表以便清除
                    chessBoard.add(textDisplay);
                }
            }
        }

        player.sendMessage(ChatColor.GREEN + "棋盘创建完成！");
        player.sendMessage(ChatColor.YELLOW + String.format("棋盘中心坐标: X: %.2f, Y: %.2f, Z: %.2f",
                boardCenter.getX(), boardCenter.getY(), boardCenter.getZ()));
        player.sendMessage(ChatColor.YELLOW + "使用 /clear_chess_debug 清除。");

        return true;
    }
    
    /**
     * 清除之前创建的棋盘
     */
    public void clearChessBoard() {
        for (TextDisplay display : chessBoard) {
            if (display != null && !display.isDead()) {
                display.remove();
            }
        }
        chessBoard.clear();
    }
    
    /**
     * 获取当前棋盘实体列表
     */
    public List<TextDisplay> getChessBoard() {
        return new ArrayList<>(chessBoard);
    }
}