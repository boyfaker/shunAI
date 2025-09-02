package com.zsy.shunai.config;


import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;

    private Integer port;

    private Integer database;

    private String password;

/**
 * Configures and creates a Redisson client bean for Redis operations.
 * Redisson is a Redis Java client that provides more features than Jedis.
 *
 * @return RedissonClient configured Redis client instance
 */
    @Bean
    public RedissonClient redissonClient() {
    // Create a new Redisson configuration object
        Config config = new Config();
    // Configure Redisson to use a single server instance
        config.useSingleServer()
        // Set the Redis server address with host and port
        .setAddress("redis://" + host + ":" + port)
        // Specify the database number to use
        .setDatabase(database)
        // Set the password for Redis authentication
    // Create and return the Redisson client with the configured settings
        .setPassword(password);
        return Redisson.create(config);
    }
}
