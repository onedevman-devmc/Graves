package mc.graves.commands;

import mc.graves.PluginMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class GravesCommand implements CommandExecutor, TabCompleter {

    public static final String LABEL = "graves";

    //

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();

        if(args.length == 1) {
            result.add("reload-config");
        }

        return result;
    }

    //

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload-config")) {
                if(sender.hasPermission("mc.graves.commands.graves.reload-config")) {
                    PluginMain.instance().reloadConfig();
                    sender.sendMessage("§aConfig reloaded.");
                }
                else {
                    sender.sendMessage("§cYou don't have permission to execute this command!");
                }

                return true;
            }
        }

        return false;
    }
}
