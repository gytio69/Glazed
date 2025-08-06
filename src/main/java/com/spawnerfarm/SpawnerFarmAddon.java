package com.spawnerfarm;

import com.spawnerfarm.modules.SpawnerFarm;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

/**
 * Main addon class for SpawnerFarm - integrates with Meteor Client
 * This class is responsible for registering our custom modules with Meteor Client
 */
public class SpawnerFarmAddon extends MeteorAddon {
    
    /**
     * Called when the addon is initialized by Meteor Client
     * This is where we register our custom modules, commands, and other features
     */
    @Override
    public void onInitialize() {
        // Create a custom category for our addon modules
        Category SPAWNER_FARM_CATEGORY = new Category("Spawner Farm");
        
        // Register our SpawnerFarm module with Meteor Client
        Modules.get().add(new SpawnerFarm());
        
        // Log that our addon has been loaded successfully
        LOG.info("SpawnerFarm addon has been initialized successfully!");
    }
    
    /**
     * Called when Meteor Client is shutting down
     * Use this for cleanup if needed
     */
    @Override
    public void onRegisterCategories() {
        // Register our custom category
        Modules.registerCategory(new Category("Spawner Farm"));
    }
    
    /**
     * Returns the package name for this addon
     * This helps Meteor Client organize addon classes
     */
    @Override
    public String getPackage() {
        return "com.spawnerfarm";
    }
}