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
        if(!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command is player-only.");
            return true;
        }

        switch (args.length) {
            case 0: {
                if(!player.hasPermission("mc.eredhel.survivaltweaks.commands.graves.bury.self")) {
                    player.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }

                Graves.bury(player);

                break;
            }
            case 1: {
                if(!player.hasPermission("mc.eredhel.survivaltweaks.commands.graves.bury.others")) {
                    player.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }



                break;
            }
            default: {
                return false;
            }
        }

        return true;
    }
}
