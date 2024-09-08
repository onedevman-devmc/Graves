package mc.graves;

import mc.graves.commands.SurvivalTweaksCommand;
import mc.graves.commands.graves.BuryCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin {

    private static PluginMain instance = null;
    public static PluginMain instance() { return instance; }

    //

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new Graves(), this);

        //

        this.getCommand(SurvivalTweaksCommand.LABEL).setTabCompleter(new SurvivalTweaksCommand());
        this.getCommand(SurvivalTweaksCommand.LABEL).setExecutor(new SurvivalTweaksCommand());

        this.getCommand(BuryCommand.LABEL).setTabCompleter(new BuryCommand());
        this.getCommand(BuryCommand.LABEL).setExecutor(new BuryCommand());

        //

        this.reloadConfig();
        this.getConfig().setDefaults(YamlConfiguration.loadConfiguration(this.getTextResource("config.yml")));
        this.getConfig().options().copyDefaults(true);

        this.saveConfig();
    }

    @Override
    public void onDisable() {
        this.reloadConfig();
        this.saveConfig();
    }

    //

}
