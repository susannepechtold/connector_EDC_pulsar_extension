/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.connector.controlplane.api.management.transferprocess.validation;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import org.eclipse.edc.validator.spi.ValidationFailure;
import org.eclipse.edc.validator.spi.Validator;
import org.eclipse.edc.validator.spi.Violation;
import org.junit.jupiter.api.Test;

import static jakarta.json.Json.createArrayBuilder;
import static jakarta.json.Json.createObjectBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_CONTRACT_ID;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_COUNTER_PARTY_ADDRESS;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_DATA_DESTINATION;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_PROTOCOL;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_TRANSFER_TYPE;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.VALUE;
import static org.eclipse.edc.junit.assertions.AbstractResultAssert.assertThat;
import static org.eclipse.edc.spi.types.domain.DataAddress.EDC_DATA_ADDRESS_TYPE_PROPERTY;
import static org.mockito.Mockito.mock;

class TransferRequestValidatorTest {

    private final Validator<JsonObject> validator = TransferRequestValidator.instance(mock());

    @Test
    void shouldSucceed_whenObjectIsValid() {
        var input = Json.createObjectBuilder()
                .add(TRANSFER_REQUEST_COUNTER_PARTY_ADDRESS, value("http://connector-address"))
                .add(TRANSFER_REQUEST_CONTRACT_ID, value("contract-id"))
                .add(TRANSFER_REQUEST_PROTOCOL, value("protocol"))
                .add(TRANSFER_REQUEST_TRANSFER_TYPE, value("transferType"))
                .add(TRANSFER_REQUEST_DATA_DESTINATION, createArrayBuilder().add(createObjectBuilder()
                        .add(EDC_DATA_ADDRESS_TYPE_PROPERTY, value("type"))
                ))
                .build();

        var result = validator.validate(input);

        assertThat(result).isSucceeded();
    }

    @Test
    void shouldSucceed_whenDataDestinationIsMissing() {
        var input = Json.createObjectBuilder()
                .add(TRANSFER_REQUEST_COUNTER_PARTY_ADDRESS, value("http://connector-address"))
                .add(TRANSFER_REQUEST_CONTRACT_ID, value("contract-id"))
                .add(TRANSFER_REQUEST_PROTOCOL, value("protocol"))
                .add(TRANSFER_REQUEST_TRANSFER_TYPE, value("transferType"))
                .build();

        var result = validator.validate(input);

        assertThat(result).isSucceeded();
    }

    @Test
    void shouldFail_whenIdIsBlank() {
        var contractDefinition = createObjectBuilder()
                .add(ID, " ")
                .build();

        var result = validator.validate(contractDefinition);

        assertThat(result).isFailed().extracting(ValidationFailure::getViolations).asInstanceOf(list(Violation.class))
                .isNotEmpty()
                .filteredOn(it -> ID.equals(it.path()))
                .anySatisfy(violation -> assertThat(violation.message()).contains("blank"));
    }

    @Test
    void shouldFail_whenMandatoryPropertiesAreMissing() {
        var input = Json.createObjectBuilder().build();

        var result = validator.validate(input);

        assertThat(result).isFailed().extracting(ValidationFailure::getViolations).asInstanceOf(list(Violation.class))
                .isNotEmpty()
                .anySatisfy(violation -> assertThat(violation.path()).isEqualTo(TRANSFER_REQUEST_COUNTER_PARTY_ADDRESS))
                .anySatisfy(violation -> assertThat(violation.path()).isEqualTo(TRANSFER_REQUEST_CONTRACT_ID))
                .anySatisfy(violation -> assertThat(violation.path()).isEqualTo(TRANSFER_REQUEST_PROTOCOL))
                .anySatisfy(violation -> assertThat(violation.path()).isEqualTo(TRANSFER_REQUEST_TRANSFER_TYPE));
    }

    private JsonArrayBuilder value(String value) {
        return createArrayBuilder().add(createObjectBuilder().add(VALUE, value));
    }
}
