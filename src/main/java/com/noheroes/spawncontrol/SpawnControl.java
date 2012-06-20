/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.spawncontrol;

import java.util.LinkedList;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;


/**
 *
 * @author PIETER
 */
public class SpawnControl extends JavaPlugin {
    
    private static SpawnControl instance;
    private SCListener listener;
    private SpawnControlMap scm;
    private Integer taskID = null;
    
    @Override
    public void onEnable() {
        instance = this;
        scm = new SpawnControlMap(this);
        listener = new SCListener(this, scm);
        this.getServer().getPluginManager().registerEvents(listener, this);
        this.getCommand("spawncontrol").setExecutor(new SCCommandHandler(this, scm));
        loadConfig(this.getConfig());
        taskID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new MonsterCleanupRunnable(this, scm), 
                Properties.ticksPerUpdate, Properties.ticksPerUpdate);
        PluginDescriptionFile pdf = this.getDescription();
        this.log(pdf.getName() + " version " + pdf.getVersion() + " is enabled");
    }
    
    @Override
    public void onDisable() {
        if (taskID != null) {
            this.getServer().getScheduler().cancelTask(taskID);
        }
        scm.despawnAllMobs();
        this.log(this.getDescription().getName() + " disabled");
    }
    
    public static void log(String message) {
        instance.log(Level.INFO, message);
    }
    
    public static void log(Level level, String message) {
        instance.getLogger().log(level, message);
    }
    
    public static SpawnControl getInstance() {
        return instance;
    }
    
    public void loadConfig(FileConfiguration config) {
        config.options().copyDefaults(true);
        Properties.maxMobsPerPlayer = config.getInt("MaxMobsPerPlayer");
        Properties.maxLinkRange = config.getInt("MaxLinkRange");
        Properties.ticksPerUpdate = config.getInt("TicksPerUpdate");
        Properties.worldList = config.getStringList("ActiveWorlds");
        if (Properties.worldList == null) {
            Properties.worldList = new LinkedList<String>();
        }
        this.saveConfig();
    }
}
