package com.oasis.murder;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Enderqura on 16/07/2017 at 09:28.
 */
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new GameMechanics(), this);
        getCommand("forcestart").setExecutor(new GameMechanics());
    }

    @Override
    public void onDisable() {

    }
}
