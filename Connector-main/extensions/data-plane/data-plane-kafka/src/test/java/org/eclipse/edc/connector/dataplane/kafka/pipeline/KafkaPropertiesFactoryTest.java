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

package org.eclipse.edc.connector.dataplane.kafka.pipeline;

import org.eclipse.edc.connector.dataplane.kafka.config.KafkaPropertiesFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;

class KafkaPropertiesFactoryTest {

    private final KafkaPropertiesFactory factory = new KafkaPropertiesFactory();

    @Test
    void verifyGetConsumerProperties() {
        var properties = Map.<String, Object>of(
                EDC_NAMESPACE + "kafka.bootstrap.servers", "kafka:9092",
                EDC_NAMESPACE + "kafka.foo.bar", "value1",
                EDC_NAMESPACE + "kafka.hello.world", "value2",
                EDC_NAMESPACE + "non.kafka.property", "value3"
        );

        var result = factory.getConsumerProperties(properties);

        assertThat(result.succeeded()).isTrue();
        assertThat(result.getContent())
                .hasSize(5)
                .containsEntry("bootstrap.servers", "kafka:9092")
                .containsEntry("foo.bar", "value1")
                .containsEntry("hello.world", "value2")
                .containsEntry("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                .containsEntry("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
    }

    @Test
    void verifyGetProducerProperties() {
        var properties = Map.<String, Object>of(
                EDC_NAMESPACE + "kafka.bootstrap.servers", "kafka:9092",
                EDC_NAMESPACE + "kafka.foo.bar", "value1",
                EDC_NAMESPACE + "kafka.hello.world", "value2",
                EDC_NAMESPACE + "non.kafka.property", "value3"
        );

        var result = factory.getProducerProperties(properties);

        assertThat(result.succeeded()).isTrue();
        assertThat(result.getContent())
                .hasSize(5)
                .containsEntry("bootstrap.servers", "kafka:9092")
                .containsEntry("foo.bar", "value1")
                .containsEntry("hello.world", "value2")
                .containsEntry("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                .containsEntry("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
    }

}
