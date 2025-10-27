/*
 *  Copyright (c) 2023 Amadeus
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Amadeus - initial API and implementation
 *
 */

package org.eclipse.edc.connector.dataplane.pulsar.pipeline;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.edc.connector.dataplane.spi.pipeline.ParallelSink;
import org.eclipse.edc.connector.dataplane.spi.pipeline.Part;
import org.eclipse.edc.spi.monitor.Monitor;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

class KafkaDataSink extends ParallelSink implements Closeable {

    private PulsarDataSink() {
    }

    private Monitor monitor;
    private Producer<byte[]> producer;
    private int partitionSize;

    @Override
    public void close() {
        if (producer != null) {
            producer.close();
        }
    }

    @Override
    protected Runnable createTask(Function<Part, Boolean> onPart) {
        return () -> {
            var now = Instant.now();
            var idx = new AtomicInteger(0);
            while (isActive()) {
                var part = take();
                if (part == null) {
                    sleep();
                    continue;
                }

                try (var is = part.openStream()) {
                    var requestId = getRequestId();
                    var key = requestId + "-" + idx.getAndIncrement();
                    var payload = inputStreamToBytes(is);
                    var record = new ProducerRecord<>(getTopic(), key, payload);
                    producer.send(record);
                    onPart.apply(part);
                } catch (Exception e) {
                    monitor.severe("failed to publish message to kafka topic: " + e.getMessage());
                }
            }
        };
    }

    public static class Builder extends ParallelSink.Builder<Builder, PulsarDataSink> {

        private Properties producerProperties;

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder monitor(Monitor monitor) {
            getInstance().monitor = monitor;
            return this;
        }

        public Builder requestId(String requestId) {
            getInstance().setRequestId(requestId);
            return this;
        }

        public Builder topic(String topic) {
            getInstance().setTopic(topic);
            return this;
        }

        public Builder producerProperties(Properties producerProperties) {
            getInstance().producer = new Producer<>(producerProperties); // Update to Pulsar Producer
            return this;
        }

        public Builder partitionSize(int partitionSize) {
            getInstance().partitionSize = partitionSize;
            return this;
        }

        public Builder executorService(ExecutorService executorService) {
            getInstance().setExecutor(executorService);
            return this;
        }

        @Override
        protected PulsarDataSink getInstance() {
            return (PulsarDataSink) super.getInstance();
        }

        @Override
        protected Builder self() {
            return this;
        }

        private Builder() {
            super(new KafkaDataSink());
        }
    }

    private byte[] inputStreamToBytes(InputStream is) throws Exception {
        return is.readAllBytes();
    }
}
