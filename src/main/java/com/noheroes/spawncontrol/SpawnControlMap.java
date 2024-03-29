/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.spawncontrol;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author PIETER
 */
public class SpawnControlMap {
    
    private final ConcurrentHashMap<String, Set<LivingEntity>> playerToMob = new ConcurrentHashMap<String, Set<LivingEntity>>();
    private final ConcurrentHashMap<LivingEntity, String> mobToPlayer = new ConcurrentHashMap<LivingEntity, String>();
    private SpawnControl sc;
    
    public SpawnControlMap(SpawnControl sc) {
        this.sc = sc;
    }
    
    // Offers LivingEntity to map, returns true if accepted and linked to player, false if no link can be made and ignores players with name pname
    public boolean offerMonster(LivingEntity mob, Location spawnLoc, String pname) {
        if ((mob == null) || spawnLoc == null) {
            return false;
        }
        // Get all players in the same world
        World curWorld = spawnLoc.getWorld();
        List<Player> playerList = curWorld.getPlayers();
        Set<Player> playersInRange = new HashSet<Player>();
        // Loop through each player and check range
        for (Player player : playerList) {
            if ((player.getLocation().distance(spawnLoc) <= Properties.maxLinkRange) && !(player.getName().toLowerCase().equals(pname))) {
                playersInRange.add(player);
            }
        }
        // If no players were in range, cancel event
        if (playersInRange.isEmpty()) {
            return false;
        }
        // Attempt to link to any player in map and return true if we find a link
        for (Player player : playersInRange) {
            if (this.linkMonster(mob, player)) {
                return true;
            }
        }
        return false;        
    }
    
    // Offers LivingEntity to map, returns true if accepted and linked to player, false if no link can be made
    public boolean offerMonster(LivingEntity mob, Location spawnLoc) {
        return this.offerMonster(mob, spawnLoc, null);
    }
    
    // Attempts to link LivingEntity to player, returns true if accepted, false if player is capped
    public boolean linkMonster(LivingEntity mob, Player player) {
        String pname = player.getName().toLowerCase();
        if (!playerToMob.containsKey(pname)) {
            Set<LivingEntity> mobSet = new HashSet<LivingEntity>();
            mobSet.add(mob);
            playerToMob.put(pname, mobSet);
            mobToPlayer.put(mob, pname);
            return true;
        }
        else {
            // Player is at cap
            if (playerToMob.get(pname).size() >= Properties.maxMobsPerPlayer) {
                return false;
            }
            // Add mob
            playerToMob.get(pname).add(mob);
            mobToPlayer.put(mob, pname);
            return true;
        }
    }
    
    public void removeMonster(LivingEntity mob) {
        if (!mobToPlayer.containsKey(mob)) {
            return;
        }
        else {
            String pname = mobToPlayer.get(mob);
            mobToPlayer.remove(mob);
            playerToMob.get(pname).remove(mob);
        }
    }
    
    public void playerQuit(Player player) {
        String pname = player.getName().toLowerCase();
        if (!playerToMob.containsKey(pname)) {
            return;
        }
        else {
            // Loops through all LivingEntitys tied to player and attempt to offer them to another player
            Set<LivingEntity> mobList = playerToMob.get(pname);
            boolean accepted;
            for (LivingEntity mob : mobList) {
                // Remove link to current player and attempt to link to a new one or despawn otherwise
                mobToPlayer.remove(mob);
                if (!mob.isDead()) {
                    accepted = this.offerMonster(mob, mob.getLocation(), pname);
                    if (!accepted) {
                        mob.remove();
                        if (Properties.debugSpam) {
                            sc.log("Player " + pname + " logged off and LivingEntity could not be offered to another player, despawning...");
                        }
                    }
                }
            }
            playerToMob.remove(pname);
        }
    }
    
    public void cleanUp() {
        if (Properties.debugSpam) {
            //sc.log("Cleanup running");
        }
        Integer counter = 0;
        for (LivingEntity mob : mobToPlayer.keySet()) {
            String pname = mobToPlayer.get(mob);
            if (mob.isDead()) {
                playerToMob.get(pname).remove(mob);
                mobToPlayer.remove(mob);
                if (Properties.debugSpam) {
                    counter++;
                }
            }
            else {
                Player player = Bukkit.getPlayer(pname);
                if (player == null) {
                    sc.log(Level.WARNING, "Error attempting to look up player during cleanup");
                    return;
                }
                // If player changed worlds or moved out of range, attempt to tie mob to new player or despawn
                if (!(player.getLocation().getWorld().equals(mob.getLocation().getWorld())) || 
                        (player.getLocation().distance(mob.getLocation()) > Properties.maxLinkRange)) {
                    mobToPlayer.remove(mob);
                    playerToMob.get(pname).remove(mob);
                    boolean accepted = this.offerMonster(mob, mob.getLocation());
                    if (!accepted) {
                        if (Properties.debugSpam) {
                            sc.log("Mob out of range of " + player.getName() + " and no new one can be found, despawning...");
                        }
                        mob.remove();
                    }
                }
            }
        }
        if (counter != 0) {
            sc.log("Cleanup thread removed " + counter + " mobs from mappings");
        }
    }
    
    public String mobInfo(String name) {
        String pname = name.toLowerCase();
        Integer mobNr;
        if (playerToMob.containsKey(pname)) {
            mobNr = playerToMob.get(pname).size();
        }
        else {
            mobNr = 0;
        }
        return "Player " + pname + " has " + mobNr + "/" + Properties.maxMobsPerPlayer + " mobs spawned";
    }
    
    public String detailedInfo(String name) {
        String pname = name.toLowerCase();
        if (!playerToMob.containsKey(pname)) {
            return "No mobs spawned";
        }
        if (playerToMob.get(pname).isEmpty()) {
            return "No mobs spawned";
        }
        Set<LivingEntity> mobList = playerToMob.get(pname);
        String msg = "LivingEntitys:";
        for (LivingEntity mob : mobList) {
            msg = msg + mob.toString() + ",";
        }
        return msg;
    }
    
    public String allInfo() {
        String msg = "Player caps - ";
        for (String pname : playerToMob.keySet()) {
            msg = msg + pname + ":" + playerToMob.get(pname).size() + ",";
        }
        return msg;
    }
    
    public void dumpPlayerLog(String name) {
        String pname = name.toLowerCase();
        if (!(playerToMob.containsKey(pname))) {
            sc.log("Player " + pname + " has no mobs");
        }
        else {
            sc.log("Player " + pname + " " + playerToMob.get(pname).size() + "/" + Properties.maxMobsPerPlayer);
            String msg = "";
            for (LivingEntity mob : playerToMob.get(pname)) {
                msg = msg + "[" + mob.toString() + "@" + mob.getLocation().getBlockX() + "," + mob.getLocation().getBlockY() 
                        + "," + mob.getLocation().getBlockZ() + "," + mob.getLocation().getWorld().getName() + "],";
            }
            sc.log(msg);
        }
    }
    
    public void dumpAllLog() {
        for (String pname : playerToMob.keySet()) {
            this.dumpPlayerLog(pname);
        }
    }
    
    public void despawnAllMobs() {
        for (LivingEntity mob : mobToPlayer.keySet()) {
            mob.remove();
        }
    }
}
