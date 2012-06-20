/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.spawncontrol;

import java.util.List;

/**
 *
 * @author PIETER
 */
public class Properties {
    
    public static int maxLinkRange = 40;
    public static int maxMobsPerPlayer = 15;    
    public static int ticksPerUpdate = 200;
    public static List<String> worldList;
    
    public static boolean debugSpam = false;
    
    public static final String adminPerms = "spawncontrol.admin";
    public static final String userPerms = "spawncontrol.user";
}
