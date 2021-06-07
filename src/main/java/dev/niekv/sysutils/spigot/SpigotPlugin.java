package dev.niekv.sysutils.spigot;

import dev.niekv.sysutils.AutoServer;
import dev.niekv.sysutils.redis.RedisModule;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.redisson.api.RMapCache;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class SpigotPlugin extends JavaPlugin {

    private RedisModule redisModule;
    private BukkitTask taskId;

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);

        this.getConfig().addDefault("redis.host", "localhost");
        this.getConfig().addDefault("server.name", "default");
        this.getConfig().addDefault("server.accessPermission", "");

        this.saveConfig();

        this.redisModule = new RedisModule(this.getConfig().getString("redis.host", "localhost"));

        this.taskId = this.getServer().getScheduler().runTaskTimerAsynchronously(this, this::refresh, 0L, 20 * 30L);

        // Really hacky way until the Docker Image is fixed
        Runtime.getRuntime().addShutdownHook(new Thread(this::remove));
    }

    @Override
    public void onDisable() {
        if(this.taskId != null) {
            this.taskId.cancel();
        }

        this.remove();

        this.redisModule.shutdown();
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // TODO probably should add a fallback for this
            return "unknown";
        }
    }

    private void refresh() {
        RMapCache<String, AutoServer> mapCache = this.redisModule.getRedissonClient().getMapCache("servers");

        AutoServer autoServer = new AutoServer(SpigotPlugin.getHostname(),
                this.getServer().getPort(),
                this.getConfig().getString("server.name", "default"),
                this.getConfig().getString("server.accessPermission", ""));
        mapCache.put(SpigotPlugin.getHostname() + ":" + this.getServer().getPort(), autoServer, 1, TimeUnit.MINUTES);
    }

    private void remove() {
        if(this.redisModule.getRedissonClient().isShutdown() || this.redisModule.getRedissonClient().isShuttingDown()) {
            this.getLogger().warning("RedissonPool is already closed");
            return;
        }

        RMapCache<String, AutoServer> mapCache = this.redisModule.getRedissonClient().getMapCache("servers");
        mapCache.remove(SpigotPlugin.getHostname() + ":" + this.getServer().getPort());
    }
}
