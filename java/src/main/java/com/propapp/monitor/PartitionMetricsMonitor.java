package com.propapp.monitor;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class PartitionMetricsMonitor {

    @Autowired
    private KafkaAdmin kafkaAdmin;

    private static final String TOPIC_NAME = "property-events-prod";

    @Scheduled(fixedRate = 60000) // Evaluates cluster data balance metrics every 60 seconds
    public void monitorPartitionZScores() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            
            // 1. Query Confluent Cloud for active partition metadata details
            TopicDescription description = adminClient.describeTopics(Collections.singletonList(TOPIC_NAME))
                .allTopicNames().get(10, TimeUnit.SECONDS).get(TOPIC_NAME);
            
            int totalPartitions = description.partitions().size();
            if (totalPartitions == 0) return;

            // 2. Fetch log-end offsets or real-time metrics per partition.
            // For statistical calculations, evaluate recent message counts/deltas
            Map<Integer, Long> partitionTraffic = fetchRealtimeTrafficMap(adminClient, totalPartitions);

            // 3. Compute the Arithmetic Mean (μ)
            double sum = 0;
            for (long count : partitionTraffic.values()) {
                sum += count;
            }
            double mean = sum / totalPartitions;

            // 4. Compute the Standard Deviation (σ)
            double varianceSum = 0;
            for (long count : partitionTraffic.values()) {
                varianceSum += Math.pow(count - mean, 2);
            }
            double standardDeviation = Math.sqrt(varianceSum / totalPartitions);

            // 5. Evaluate and screen the Z-Score for each individual partition channel
            for (Map.Entry<Integer, Long> entry : partitionTraffic.entrySet()) {
                int partitionId = entry.getKey();
                long actualCount = entry.getValue();
                
                // Protect against division by zero in perfectly uniform message allocation scenarios
                double zScore = (standardDeviation == 0) ? 0 : (actualCount - mean) / standardDeviation;

                // Statistical Guardrail Threshold: flag any partition skew beyond 2.5 standard deviations
                if (Math.abs(zScore) > 2.5) {
                    System.err.printf("[CRITICAL ALERT] Severe Kafka Data Skew Detected on Partition %d! " +
                                      "Z-Score: %.2f (Actual Volume: %d, System Mean Allocation: %.1f)%n", 
                                      partitionId, zScore, actualCount, mean);
                    // Proactively hook this loop branch to Slack Webhooks, Prometheus Alerts, or Micrometer metrics counter
                }
            }
            
        } catch (Exception e) {
            System.err.println("Failed to compute Kafka partition Z-Scores via AdminClient: " + e.getMessage());
        }
    }

    private Map<Integer, Long> fetchRealtimeTrafficMap(AdminClient client, int totalPartitions) {
        // In real execution setups, query real-time consumer tracking loops,
        // Confluent metrics API counters, or track internal atomic long arrays mapping traffic spikes.
        Map<Integer, Long> sampleMetrics = new HashMap<>();
        for (int i = 0; i < totalPartitions; i++) {
            sampleMetrics.put(i, 10000L + (long)(Math.random() * 1500)); // Simulating production values
        }
        return sampleMetrics;
    }
}
