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

package org.eclipse.edc.validator.jsonobject.validators.model;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import org.eclipse.edc.spi.query.CriterionOperatorRegistry;
import org.eclipse.edc.validator.spi.ValidationFailure;
import org.eclipse.edc.validator.spi.Validator;
import org.eclipse.edc.validator.spi.Violation;
import org.junit.jupiter.api.Test;

import static jakarta.json.Json.createArrayBuilder;
import static jakarta.json.Json.createObjectBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.VALUE;
import static org.eclipse.edc.junit.assertions.AbstractResultAssert.assertThat;
import static org.eclipse.edc.spi.query.Criterion.CRITERION_OPERAND_LEFT;
import static org.eclipse.edc.spi.query.Criterion.CRITERION_OPERAND_RIGHT;
import static org.eclipse.edc.spi.query.Criterion.CRITERION_OPERATOR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CriterionValidatorTest {

    private final CriterionOperatorRegistry criterionOperatorRegistry = mock();
    private final Validator<JsonObject> validator = CriterionValidator.instance(criterionOperatorRegistry);

    @Test
    void shouldSucceed_whenObjectIsValid() {
        when(criterionOperatorRegistry.isSupported(any())).thenReturn(true);
        var input = Json.createObjectBuilder()
                .add(CRITERION_OPERAND_LEFT, value("operand left"))
                .add(CRITERION_OPERATOR, value("="))
                .add(CRITERION_OPERAND_RIGHT, value("operand right"))
                .build();

        var result = validator.validate(input);

        assertThat(result).isSucceeded();
    }

    @Test
    void shouldFail_whenOperatorIsNotSupported() {
        when(criterionOperatorRegistry.isSupported(any())).thenReturn(false);
        var input = Json.createObjectBuilder()
                .add(CRITERION_OPERAND_LEFT, value("operand left"))
                .add(CRITERION_OPERATOR, value("not-supported"))
                .add(CRITERION_OPERAND_RIGHT, value("operand right"))
                .build();

        var result = validator.validate(input);

        assertThat(result).isFailed().extracting(ValidationFailure::getViolations).asInstanceOf(list(Violation.class))
                .hasSize(1)
                .anySatisfy(violation -> assertThat(violation.path()).isEqualTo(CRITERION_OPERATOR));
    }

    @Test
    void shouldFail_whenMandatoryFieldsAreMissing() {
        var input = Json.createObjectBuilder()
                .build();

        var result = validator.validate(input);

        assertThat(result).isFailed().extracting(ValidationFailure::getViolations).asInstanceOf(list(Violation.class))
                .hasSize(3)
                .anySatisfy(violation -> assertThat(violation.path()).isEqualTo(CRITERION_OPERAND_LEFT))
                .anySatisfy(violation -> assertThat(violation.path()).isEqualTo(CRITERION_OPERATOR))
                .anySatisfy(violation -> assertThat(violation.path()).isEqualTo(CRITERION_OPERAND_RIGHT));
    }

    @Test
    void shouldFail_whenOperandRightIsEmpty() {
        when(criterionOperatorRegistry.isSupported(any())).thenReturn(true);
        var input = Json.createObjectBuilder()
                .add(CRITERION_OPERAND_LEFT, value("operand left"))
                .add(CRITERION_OPERATOR, value("="))
                .add(CRITERION_OPERAND_RIGHT, createArrayBuilder().build())
                .build();

        var result = validator.validate(input);

        assertThat(result).isFailed().extracting(ValidationFailure::getViolations).asInstanceOf(list(Violation.class))
                .hasSize(1)
                .anySatisfy(violation -> {
                    assertThat(violation.path()).isEqualTo(CRITERION_OPERAND_RIGHT);
                    assertThat(violation.message()).contains("contains '1' elements");
                });
    }

    @Test
    void shouldFail_whenOperandRightHasMultipleValuesAndOperatorIsNotIn() {
        when(criterionOperatorRegistry.isSupported(any())).thenReturn(true);
        var input = Json.createObjectBuilder()
                .add(CRITERION_OPERAND_LEFT, value("operand left"))
                .add(CRITERION_OPERATOR, value("="))
                .add(CRITERION_OPERAND_RIGHT, createArrayBuilder()
                        .add(createObjectBuilder().add(VALUE, "value1"))
                        .add(createObjectBuilder().add(VALUE, "value2"))
                )
                .build();

        var result = validator.validate(input);

        assertThat(result).isFailed().extracting(ValidationFailure::getViolations).asInstanceOf(list(Violation.class))
                .hasSize(1)
                .anySatisfy(violation -> assertThat(violation.path()).isEqualTo(CRITERION_OPERAND_RIGHT));
    }

    private JsonArrayBuilder value(String value) {
        return createArrayBuilder().add(createObjectBuilder().add(VALUE, value));
    }
}
