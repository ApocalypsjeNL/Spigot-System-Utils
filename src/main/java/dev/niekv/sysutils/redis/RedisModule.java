package dev.niekv.sysutils.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

public class RedisModule {

    private static final Codec CODEC = new JsonJacksonCodec();

    private final RedissonClient redissonClient;

    public RedisModule(String host) {
        Config config = new Config().setCodec(CODEC);
        config.useSingleServer().setAddress("redis://" + host + ":6379").setTimeout(30);

        this.redissonClient = Redisson.create(config);
    }

    public void shutdown() {
        if(this.redissonClient != null) {
            this.redissonClient.shutdown();
        }
    }

    public RedissonClient getRedissonClient() {
        return this.redissonClient;
    }
}
