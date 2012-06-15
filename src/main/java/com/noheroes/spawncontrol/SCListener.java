/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.spawncontrol;


import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author PIETER
 */
public class SCListener implements Listener {
    
    private SpawnControl sc;
    private SpawnControlMap scm;
    
    public SCListener(SpawnControl sc, SpawnControlMap scm) {
        this.sc = sc;
        this.scm = scm;
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Skip if not a normal spawn
        if (!event.getSpawnReason().equals(SpawnReason.NATURAL)) {
            return;
        }
        EntityType et = event.getEntityType();
        if (this.isMob(et)) {
            if (event.getEntity().isDead()) {
                return;
            }
            boolean accepted = scm.offerMonster((LivingEntity)event.getEntity(), event.getLocation());
            if (!accepted) {
                if (Properties.debugSpam) {
                    sc.log("Prevented spawn because no player could be linked");
                }
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        EntityType et = event.getEntityType();
        if (this.isMob(et)) {
            scm.removeMonster((LivingEntity)event.getEntity());
        }
    }
    
    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        EntityType et = event.getEntityType();
        if (this.isMob(et)) {
            scm.removeMonster((LivingEntity)event.getEntity());
        }        
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        scm.playerQuit(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        scm.playerQuit(event.getPlayer());
    }
    
    private boolean isMob(EntityType et) {
        return (et.equals(EntityType.CREEPER) || (et.equals(EntityType.SKELETON)) || (et.equals(EntityType.ENDERMAN)) || 
                et.equals(EntityType.SPIDER) || et.equals(EntityType.ZOMBIE) || et.equals(EntityType.PIG_ZOMBIE) || et.equals(EntityType.SLIME));
    }
}
