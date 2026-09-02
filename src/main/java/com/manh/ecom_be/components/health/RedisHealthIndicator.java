package com.manh.ecom_be.components.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Custom health indicator that verifies Redis connectivity
 * and reports server version in health details.
 */
@Component("redisCustom")
@RequiredArgsConstructor
@Slf4j
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            // PING returns "PONG" if Redis is alive
            String pong = connection.ping();

            Properties info = connection.serverCommands().info("server");
            String redisVersion = info != null ? info.getProperty("redis_version", "unknown") : "unknown";

            return Health.up()
                    .withDetail("ping", pong)
                    .withDetail("version", redisVersion)
                    .build();

        } catch (Exception e) {
            log.error("Redis health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
