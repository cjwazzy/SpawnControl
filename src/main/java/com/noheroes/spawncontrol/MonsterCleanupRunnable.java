/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.spawncontrol;

/**
 *
 * @author PIETER
 */
public class MonsterCleanupRunnable implements Runnable {

    private SpawnControl sc;
    private SpawnControlMap scm;
    
    public MonsterCleanupRunnable(SpawnControl sc, SpawnControlMap scm) {
        this.sc = sc;
        this.scm = scm;
    }
    
    public void run() {
        scm.cleanUp();
    }
    
}
