package mc.graves;

import mc.graves.commands.GravesCommand;
import mc.graves.commands.graves.BuryCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class PluginMain extends JavaPlugin {

    private static PluginMain instance = null;
    public static PluginMain instance() { return instance; }

    //

    private boolean dependencySetupSucceeded = false;

    //

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.setupDependencies();
        if(!this.doesDependencySetupSucceeded()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        //

        Bukkit.getPluginManager().registerEvents(new Graves(), this);

        //

        this.getCommand(GravesCommand.LABEL).setTabCompleter(new GravesCommand());
        this.getCommand(GravesCommand.LABEL).setExecutor(new GravesCommand());

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
        if(!this.doesDependencySetupSucceeded()) return;

        this.reloadConfig();
        this.saveConfig();
    }

    //

    public boolean doesDependencySetupSucceeded() {
        return this.dependencySetupSucceeded;
    }

    private void setupDependencies() {
        boolean dependencyPluginsValid = true;

        for(Map.Entry<String, Boolean> dependencyPluginInfo : Map.ofEntries(
                Map.entry("Compendium", this.isDevBuild())
        ).entrySet()) {
            boolean shouldProcessDependency = dependencyPluginInfo.getValue();

            if(shouldProcessDependency) {
                String dependencyPluginName = dependencyPluginInfo.getKey();

                Plugin dependencyPlugin = Bukkit.getPluginManager().getPlugin(dependencyPluginName);
                if (dependencyPlugin == null) {
                    getLogger().warning("\033[31m\033[1mUnable to find dependency plugin " + dependencyPluginName + ".");
                    dependencyPluginsValid = false;
                }
            }
        }

        this.dependencySetupSucceeded = dependencyPluginsValid;
    }

    private boolean isDevBuild() {
        try {
            Class.forName(this.getClass().getPackage().getName() + ".Dev");
            return true;
        }
        catch (ClassNotFoundException e) { return false; }
    }

}
