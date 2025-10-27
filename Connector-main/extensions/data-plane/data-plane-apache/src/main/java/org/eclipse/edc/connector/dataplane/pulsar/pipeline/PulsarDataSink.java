package org.eclipse.edc.connector.dataplane.pulsar.pipeline;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.Schema;

import java.util.concurrent.CompletableFuture;

/**
 * Minimal Pulsar-based DataSink placeholder. Replace with DataSink contract integration.
 */
public class PulsarDataSink {

    private final PulsarClient client;

    public PulsarDataSink(PulsarClient client) {
        this.client = client;
    }

    public Producer<byte[]> createProducer(String topic) throws Exception {
        ProducerBuilder<byte[]> builder = client.newProducer(Schema.BYTES).topic(topic);
        return builder.create();
    }

    public CompletableFuture<Void> sendAsync(Producer<byte[]> producer, byte[] payload) {
        return producer.sendAsync(payload).thenAccept(msgId -> { /* no-op */ });
    }
}
