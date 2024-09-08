package mc.graves.commands.graves;

import mc.graves.Graves;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class BuryCommand implements CommandExecutor, TabCompleter {

    public static String LABEL = "bury";

    //

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 1 ? Bukkit.getServer().matchPlayer(args[0]).stream().map(Player::getName).toList() : null;
    }


    //

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 0: {
                if(!(sender instanceof Player player)) {
                    sender.sendMessage("§cThis command is player-only.");
                    return true;
                }

                if(!player.hasPermission("mc.graves.commands.graves.bury.self")) {
                    player.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }

                Graves.bury(player);

                break;
            }
            case 1: {
                if(!sender.hasPermission("mc.graves.commands.graves.bury.others")) {
                    sender.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);
                if(target == null) {
                    sender.sendMessage("§cPlayer not found.");
                    return true;
                }

                Graves.bury(target);
                sender.sendMessage("§e" + target.getName() + "§a has been buried successfully.");

                break;
            }
            default: {
                return false;
            }
        }

        return true;
    }
}
