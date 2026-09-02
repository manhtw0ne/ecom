package com.manh.ecom_be.components.health;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Custom health indicator that checks Kafka broker availability
 * by listing topics within a timeout window.
 */
@Component("kafkaCustom")
@Slf4j
public class KafkaHealthIndicator implements HealthIndicator {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private static final int TIMEOUT_MS = 5000;

    @Override
    public Health health() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, TIMEOUT_MS);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, TIMEOUT_MS);

        try (AdminClient adminClient = AdminClient.create(props)) {
            ListTopicsResult topics = adminClient.listTopics();
            Set<String> topicNames = topics.names().get(TIMEOUT_MS, TimeUnit.MILLISECONDS);

            return Health.up()
                    .withDetail("bootstrap-servers", bootstrapServers)
                    .withDetail("topic-count", topicNames.size())
                    .withDetail("topics", topicNames)
                    .build();

        } catch (Exception e) {
            log.error("Kafka health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("bootstrap-servers", bootstrapServers)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
