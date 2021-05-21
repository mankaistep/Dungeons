package me.manaki.plugin.dungeons.v4.world;

import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.util.Tasks;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WorldLoader {

    private static final String PATH = "worlds";

    private final Dungeons plugin;

    private final Map<String, Integer> highestID;
    private final List<WorldCache> caches;

    private Set<String> isLoading;

    public WorldLoader(Dungeons plugin) {
        this.plugin = plugin;
        this.highestID = new HashMap<>();
        this.caches = new ArrayList<>();
        this.isLoading = new HashSet<>();
        this.checkTemplateFolder();
    }

    public List<WorldCache> getCaches() {
        return caches;
    }

    public void clearCache(WorldCache cache) {
        this.caches.remove(cache);
    }

    public int getHighestID(String world) {
        return this.highestID.getOrDefault(world, 0);
    }

    public void setHighestID(String world, int id) {
       this.highestID.put(world, id);
    }

    public WorldCache getCache(int id) {
        for (WorldCache cache : caches) {
            if (cache.getId() == id) return cache;
        }
        return null;
    }

    public World getWorld(int id) {
        var cache = getCache(id);
        var name = cache.toWorldName();
        return Bukkit.getWorld(name);
    }

    public void checkTemplateFolder() {
        var file = new File(plugin.getDataFolder() + "//" + PATH);
        if (!file.exists()) file.mkdirs();
    }

    private void setLoading(String world, boolean value) {
        if (value) this.isLoading.add(world);
        else this.isLoading.remove(world);
    }

    public boolean isLoading(String world) {
        return this.isLoading.contains(world);
    }

    public WorldCache load(WorldTemplate template, boolean ignoreExist, boolean isAsync) {
        // Get and save id
        var nameid = template.getName();
        int highestID = this.getHighestID(nameid);
        int id = highestID + 1;
        this.setHighestID(nameid, id);

        // Create cache
        var cache = new WorldCache(id, template.getName());
        try {
            // Set loading = true on start
            setLoading(cache.toWorldName(), true);

            var name = cache.toWorldName();
            var creator = template.asWorldCreator(name);
            var tempWorldFolder = new File(Bukkit.getWorldContainer() + "//" + name);

            // Check exist
            if (tempWorldFolder.exists() && Bukkit.getWorld(name) != null && ignoreExist) {
                plugin.getLogger().info("World " + name + " exists -> Ignore");
                return null;
            }
            else if (Bukkit.getWorld(name) != null) {
                unload(name, isAsync);
                plugin.getLogger().info("World " + name + " exists -> Unload");
            }

            // Copy from source to temporary world
            var sourceWorld = new File(plugin.getDataFolder() + "//" + PATH + "//" + template.getName());
            if (!sourceWorld.exists()) {
                plugin.getLogger().warning(sourceWorld.getName() + " world doesnt exist!");
                return null;
            }

            // Remove uid.dat file
            var uiddatfile = new File(sourceWorld, "uid.dat");
            if (uiddatfile.exists()) {
                try {
                    FileUtils.forceDelete(uiddatfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Remove and create folder
            FileUtils.deleteDirectory(tempWorldFolder);
            tempWorldFolder.mkdirs();
            FileUtils.copyDirectory(sourceWorld, tempWorldFolder);

            try {
                // Load and add cache
                if (isAsync) Tasks.sync(() -> {
                    long start = System.currentTimeMillis();
                    plugin.getLogger().warning("Loading world " + name + "... (from async task)");
                    creator.createWorld();
                    plugin.getLogger().warning("Loaded world " + name + "!");
                    plugin.getLogger().warning("Took " + (System.currentTimeMillis() - start) + "ms");
                });
                else {
                    long start = System.currentTimeMillis();
                    plugin.getLogger().warning("Loading world " + name + "...");
                    creator.createWorld();
                    plugin.getLogger().warning("Loaded world " + name + "!");
                    plugin.getLogger().warning("Took " + (System.currentTimeMillis() - start) + "ms");
                }
            }
            catch (Exception e) {
                unload(name, isAsync);
                plugin.getLogger().warning("It seems like there is an exception interfere it from loading");
                plugin.getLogger().warning("Unloaded world " + name);
                e.printStackTrace();
            }

            // Add cache
            caches.add(cache);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Set false if end or fail
            setLoading(cache.toWorldName(), false);
        }

        return cache;
    }

    public void unload(String worldName, boolean isAsync) {
        // Unload
        var world = Bukkit.getWorld(worldName);
        if (world == null) return;

        // Check player
        for (Player player : world.getPlayers()) {
            Utils.toSpawn(player);
        }

        // Unload async
        if (isAsync) Tasks.sync(() -> {
            // Async unload bukkit world
            long start = System.currentTimeMillis();
            plugin.getLogger().warning("Unloading world " + worldName + "... (from async task)");
            Bukkit.unloadWorld(world, false);
            Bukkit.getWorlds().remove(world);
            plugin.getLogger().warning("Unloaded world " + worldName + "!");
            plugin.getLogger().warning("Took " + (System.currentTimeMillis() - start) + "ms");

            // Async I/O
            Tasks.async(() -> {
                // Delete file
                deleteTemporaryWorld(worldName);
            });
        });

        // Unload sync
        else {
            long start = System.currentTimeMillis();
            plugin.getLogger().info("Unloading world " + worldName + "...");
            Bukkit.unloadWorld(world, false);
            Bukkit.getWorlds().remove(world);
            plugin.getLogger().info("Unloaded world " + worldName + "!");
            plugin.getLogger().info("Took " + (System.currentTimeMillis() - start) + "ms");

            // Delete file
            deleteTemporaryWorld(worldName);
        }
    }

    public void deleteTemporaryWorld(String worldName) {
        var worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        try {
            if (worldFolder.exists()) FileUtils.deleteDirectory(worldFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.getLogger().info("Deleted temporary world " + worldName + "!");
    }


}
