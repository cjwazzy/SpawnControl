/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.spawncontrol;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author PIETER
 */
public class SCCommandHandler implements CommandExecutor {
    
    private SpawnControl sc;
    private SpawnControlMap scm;
    
    public SCCommandHandler(SpawnControl sc, SpawnControlMap scm) {
        this.sc = sc;
        this.scm = scm;
    }

    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        // No permissions, command is ignored entirely
        if (!this.hasPermission(cs, Properties.userPerms)) {
            return true;
        }
        String com;
        if (args.length == 0) {
            com = "help";
        }
        else {
            com = args[0];
        }
        String pname;
        if (args.length == 1) {
            if (!(cs instanceof Player)) {
                pname = null;
            }
            else {
                pname = cs.getName();
            }
        }
        else if (args.length >= 2) {
            if (this.hasPermission(cs, Properties.adminPerms)) {
                pname = args[1];
            }
            else {
                pname = cs.getName();
            }
        }
        else {
            pname = null;
        }
        if (com.equalsIgnoreCase("info")) {
            if (pname == null) {
                cs.sendMessage("You must specify a player");
            }
            else {
                cs.sendMessage(scm.mobInfo(pname));   
            }
        }
        else if (com.equalsIgnoreCase("details")) {
            if (this.hasPermission(cs, Properties.adminPerms)) {
                if (pname == null) {
                    cs.sendMessage("You must specify a player");
                }
                else {
                    cs.sendMessage(scm.detailedInfo(pname));   
                }
            }
        }
        else if (com.equalsIgnoreCase("cap")) {
            if (this.hasPermission(cs, Properties.adminPerms)) {
                if (args.length < 2) {
                    cs.sendMessage("Current cap is " + Properties.maxMobsPerPlayer);
                }
                else {
                    Integer cap;
                    try {
                        cap = Integer.valueOf(args[1]);
                    } catch (NumberFormatException ex) {
                        cs.sendMessage("You must type in a number for the new cap");
                        return true;
                    }
                    Properties.maxMobsPerPlayer = cap;
                    cs.sendMessage("Cap changed to " + cap);
                }
            }
        }
        else if (com.equalsIgnoreCase("range")) {
            if (this.hasPermission(cs, Properties.adminPerms)) {
                if (args.length < 2) {
                    cs.sendMessage("Current range is " + Properties.maxLinkRange);
                }
                else {
                    Integer range;
                    try {
                        range = Integer.valueOf(args[1]);
                    } catch (NumberFormatException ex) {
                        cs.sendMessage("You must type in a number for the new range");
                        return true;
                    }
                    Properties.maxLinkRange = range;
                    cs.sendMessage("Range changed to " + range);
                }
            }
        }
        else if (com.equalsIgnoreCase("dump")) {
            if (this.hasPermission(cs, Properties.adminPerms)) {
                if (args.length >= 2) {
                    scm.dumpPlayerLog(args[1]);
                }
                else {
                    scm.dumpAllLog();
                }
            }
        }
        else if (com.equalsIgnoreCase("debug")) {
            if (this.hasPermission(cs, Properties.adminPerms)) {
                if (Properties.debugSpam) {
                    cs.sendMessage("Debug spam turned off");
                    Properties.debugSpam = false;
                }
                else {
                    cs.sendMessage("Debug spam turned on");
                    Properties.debugSpam = true;
                }
            }
        }
        else if (com.equalsIgnoreCase("allinfo") || com.equalsIgnoreCase("list")) {
            if (this.hasPermission(cs, Properties.adminPerms)) {
                cs.sendMessage(scm.allInfo());
            }
        }
        return true;
    }    
    
    public boolean hasPermission(CommandSender cs, String perm) {
        if (perm == null) {
            return true;
        }
        if (cs instanceof ConsoleCommandSender) {
            return true;
        }
        if (cs.isOp()) {
            return true;
        }
        if (cs.hasPermission(Properties.adminPerms)) {
            return true;
        }
        return cs.hasPermission(perm);
    }
}
