package dev.niekv.sysutils.bungee;

import com.google.common.base.Predicates;
import dev.niekv.sysutils.AutoServer;
import dev.niekv.sysutils.bungee.listener.PlayerServerSwitchListener;
import dev.niekv.sysutils.redis.RedisModule;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.redisson.api.RMapCache;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryExpiredListener;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BungeePlugin extends Plugin {

    private final List<Integer> listenerIds = new ArrayList<>();
    private RedisModule redisModule;

    private final Map<String, AutoServer> localCache = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        Configuration configuration = this.loadConfiguration();

        this.redisModule = new RedisModule(configuration.getString("redis.host", "localhost"));

        RMapCache<String, AutoServer> mapCache = this.redisModule.getRedissonClient().getMapCache("servers");

        listenerIds.add(mapCache.addListener((EntryUpdatedListener) entryEvent -> this.refresh()));
        listenerIds.add(mapCache.addListener((EntryCreatedListener) entryEvent -> this.refresh()));
        listenerIds.add(mapCache.addListener((EntryExpiredListener) entryEvent -> this.refresh()));
        listenerIds.add(mapCache.addListener((EntryRemovedListener) entryEvent -> this.refresh()));

        this.getProxy().getPluginManager().registerListener(this, new PlayerServerSwitchListener(this));
    }

    @Override
    public void onDisable() {
        RMapCache<String, AutoServer> mapCache = this.redisModule.getRedissonClient().getMapCache("servers");
        this.listenerIds.forEach(mapCache::removeListener);

        this.redisModule.shutdown();
    }

    private void refresh() {
        this.getLogger().log(Level.FINE, "Resolving proxy changes");
        RMapCache<String, AutoServer> mapCache = this.redisModule.getRedissonClient().getMapCache("servers");
        mapCache.forEach((key, value) -> this.getLogger().log(Level.FINE, key + ":" + value));

        List<String> newServers = mapCache.keySet().stream().filter(Predicates.in(this.getProxy().getServers().keySet()).negate()).collect(Collectors.toList());
        List<String> oldServers = this.getProxy().getServers().keySet().stream().filter(Predicates.in(mapCache.keySet()).negate()).collect(Collectors.toList());

        for (String newServer : newServers) {
            AutoServer dataObject = mapCache.get(newServer);
            this.localCache.put(newServer, dataObject);

            ServerInfo serverInfo = this.getProxy().constructServerInfo(
                    dataObject.getServerName().replace(" ", "_"),
                    InetSocketAddress.createUnresolved(dataObject.getServerHost(), dataObject.getServerPort()),
                    newServer,
                    false);
            this.getProxy().getServers().put(dataObject.getServerName().replace(" ", "_"), serverInfo);

            this.getLogger().info("New server connected: " + newServer);
        }

        for (String oldServer : oldServers) {
            this.getProxy().getServers().remove(oldServer);
            this.localCache.remove(oldServer);

            this.getLogger().info("Disconnecting from old server " + oldServer);
        }

        this.getLogger().log(Level.FINE, "Updated servers");
    }

    public Map<String, AutoServer> getLocalCache() {
        return this.localCache;
    }

    private void initializeConfiguration() {
        final File configFile = new File(this.getDataFolder(), "config.yml");
        if(!configFile.getParentFile().exists()) {
            configFile.mkdirs();
        }

        if(!configFile.exists()) {
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load("");

            configuration.set("redis.host", "localhost");

            this.saveConfiguration(configuration);
        }
    }

    private Configuration loadConfiguration() {
        this.initializeConfiguration();

        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(this.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveConfiguration(Configuration configuration) {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(this.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

