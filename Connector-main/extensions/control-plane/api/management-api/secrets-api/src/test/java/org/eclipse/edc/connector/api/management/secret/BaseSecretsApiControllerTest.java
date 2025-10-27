/*
 *  Copyright (c) 2024 Amadeus
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Amadeus - Initial API and Implementation
 *
 */

package org.eclipse.edc.connector.api.management.secret;

import io.restassured.specification.RequestSpecification;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.eclipse.edc.api.model.IdResponse;
import org.eclipse.edc.connector.spi.service.SecretService;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.result.ServiceResult;
import org.eclipse.edc.spi.types.domain.secret.Secret;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.validator.spi.JsonObjectValidatorRegistry;
import org.eclipse.edc.validator.spi.ValidationResult;
import org.eclipse.edc.web.jersey.testfixtures.RestControllerTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.http.ContentType.JSON;
import static jakarta.json.Json.createObjectBuilder;
import static org.eclipse.edc.api.model.IdResponse.ID_RESPONSE_CREATED_AT;
import static org.eclipse.edc.api.model.IdResponse.ID_RESPONSE_TYPE;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.CONTEXT;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.VOCAB;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_PREFIX;
import static org.eclipse.edc.spi.types.domain.secret.Secret.EDC_SECRET_TYPE;
import static org.eclipse.edc.spi.types.domain.secret.Secret.EDC_SECRET_VALUE;
import static org.eclipse.edc.validator.spi.Violation.violation;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ApiTest
public abstract class BaseSecretsApiControllerTest extends RestControllerTestBase {

    private static final String TEST_SECRET_ID = "test-secret-id";
    private static final String TEST_SECRET_VALUE = "test-secret-value";
    protected final SecretService service = mock();
    protected final TypeTransformerRegistry transformerRegistry = mock();
    protected final JsonObjectValidatorRegistry validator = mock();

    @BeforeEach
    void setup() {
        when(transformerRegistry.transform(isA(IdResponse.class), eq(JsonObject.class))).thenAnswer(a -> {
            var idResponse = (IdResponse) a.getArgument(0);
            return Result.success(createObjectBuilder()
                    .add(TYPE, ID_RESPONSE_TYPE)
                    .add(ID, idResponse.getId())
                    .add(ID_RESPONSE_CREATED_AT, idResponse.getCreatedAt())
                    .build()
            );
        });
    }

    @Test
    void getSecret() {
        var secret = Secret.Builder.newInstance().id(TEST_SECRET_ID).value(TEST_SECRET_VALUE).build();
        when(service.findById(TEST_SECRET_ID)).thenReturn(secret);
        var secretJson = createSecretJson().build();
        when(transformerRegistry.transform(isA(Secret.class), eq(JsonObject.class))).thenReturn(Result.success(secretJson));

        baseRequest()
                .get("/secrets/%s".formatted(TEST_SECRET_ID))
                .then()
                .statusCode(200)
                .contentType(JSON);

        verify(transformerRegistry).transform(isA(Secret.class), eq(JsonObject.class));
        verifyNoMoreInteractions(transformerRegistry);
    }

    @Test
    void getSecret_notFound() {
        when(service.findById(any())).thenReturn(null);

        baseRequest()
                .get("/secrets/not-existent-id")
                .then()
                .statusCode(404);
    }

    @Test
    void getSecretById_shouldReturnNotFound_whenTransformFails() {
        when(service.findById("id")).thenReturn(Secret.Builder.newInstance().id(TEST_SECRET_ID).value(TEST_SECRET_VALUE).build());
        when(transformerRegistry.transform(isA(Secret.class), eq(JsonObject.class))).thenReturn(Result.failure("failure"));

        baseRequest()
                .get("/secrets/id")
                .then()
                .statusCode(500);
    }

    @Test
    void createSecret() {
        var secret = createSecretBuilder().build();
        when(transformerRegistry.transform(any(JsonObject.class), eq(Secret.class))).thenReturn(Result.success(secret));
        when(service.create(isA(Secret.class))).thenReturn(ServiceResult.success(secret));
        when(validator.validate(any(), any())).thenReturn(ValidationResult.success());

        baseRequest()
                .contentType(JSON)
                .body(createSecretJson().build())
                .post("/secrets")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(ID, is(TEST_SECRET_ID))
                .body("'" + EDC_NAMESPACE + "createdAt'", greaterThan(0L));

        verify(transformerRegistry).transform(any(), eq(Secret.class));
        verify(transformerRegistry).transform(isA(IdResponse.class), eq(JsonObject.class));
        verify(service).create(isA(Secret.class));
        verifyNoMoreInteractions(service, transformerRegistry);
    }

    @Test
    void createSecret_shouldReturnBadRequest_whenValidationFails() {
        when(validator.validate(any(), any())).thenReturn(ValidationResult.failure(violation("a failure", "a path")));

        baseRequest()
                .contentType(JSON)
                .body(createSecretJson().build())
                .post("/secrets")
                .then()
                .statusCode(400);

        verify(validator).validate(eq(EDC_SECRET_TYPE), isA(JsonObject.class));
        verifyNoInteractions(service, transformerRegistry);
    }

    @Test
    void createSecret_shouldReturnBadRequest_whenTransformFails() {
        when(transformerRegistry.transform(isA(JsonObject.class), eq(Secret.class))).thenReturn(Result.failure("failed"));
        when(validator.validate(any(), any())).thenReturn(ValidationResult.success());

        baseRequest()
                .body(createSecretJson().build())
                .contentType(JSON)
                .post("/secrets")
                .then()
                .statusCode(400);
        verifyNoInteractions(service);
    }

    @Test
    void createSecret_alreadyExists() {
        var secret = createSecretBuilder().build();
        when(transformerRegistry.transform(any(JsonObject.class), eq(Secret.class))).thenReturn(Result.success(secret));
        when(service.create(isA(Secret.class))).thenReturn(ServiceResult.conflict("already exists"));
        when(validator.validate(any(), any())).thenReturn(ValidationResult.success());

        baseRequest()
                .body(createSecretJson().build())
                .contentType(JSON)
                .post("/secrets")
                .then()
                .statusCode(409);
    }

    @Test
    void deleteSecret() {
        when(service.delete("secretId"))
                .thenReturn(ServiceResult.success(createSecretBuilder().build()));

        baseRequest()
                .contentType(JSON)
                .delete("/secrets/secretId")
                .then()
                .statusCode(204);
        verify(service).delete("secretId");
    }

    @Test
    void deleteSecret_notExists() {
        when(service.delete(any())).thenReturn(ServiceResult.notFound("not found"));

        baseRequest()
                .contentType(JSON)
                .delete("/secrets/not-existent-id")
                .then()
                .statusCode(404);
    }

    @Test
    void updateSecret_whenExists() {
        var secret = Secret.Builder.newInstance().id(TEST_SECRET_ID).value(TEST_SECRET_VALUE).build();
        when(transformerRegistry.transform(isA(JsonObject.class), eq(Secret.class))).thenReturn(Result.success(secret));
        when(service.update(any(Secret.class))).thenReturn(ServiceResult.success());
        when(validator.validate(any(), any())).thenReturn(ValidationResult.success());

        baseRequest()
                .body(createSecretJson().build())
                .contentType(JSON)
                .put("/secrets")
                .then()
                .statusCode(204);
        verify(service).update(eq(secret));
    }

    @Test
    void updateSecret_shouldReturnNotFound_whenItDoesNotExists() {
        var secret = Secret.Builder.newInstance().id(TEST_SECRET_ID).value(TEST_SECRET_VALUE).build();
        when(transformerRegistry.transform(isA(JsonObject.class), eq(Secret.class))).thenReturn(Result.success(secret));
        when(service.update(any(Secret.class))).thenReturn(ServiceResult.notFound("not found"));
        when(validator.validate(any(), any())).thenReturn(ValidationResult.success());

        baseRequest()
                .body(createSecretJson().build())
                .contentType(JSON)
                .put("/secrets")
                .then()
                .statusCode(404);
    }

    @Test
    void updateSecret_shouldReturnBadRequest_whenTransformFails() {
        when(transformerRegistry.transform(isA(JsonObject.class), eq(Secret.class))).thenReturn(Result.failure("error"));
        when(validator.validate(any(), any())).thenReturn(ValidationResult.success());

        baseRequest()
                .body(createSecretJson().build())
                .contentType(JSON)
                .put("/secrets")
                .then()
                .statusCode(400);
        verifyNoInteractions(service);
    }

    @Test
    void updateSecret_shouldReturnBadRequest_whenValidationFails() {
        when(transformerRegistry.transform(isA(JsonObject.class), eq(Secret.class))).thenReturn(Result.failure("error"));
        when(validator.validate(any(), any())).thenReturn(ValidationResult.failure(violation("validation failure", "path")));

        baseRequest()
                .body(createSecretJson().build())
                .contentType(JSON)
                .put("/secrets")
                .then()
                .statusCode(400);
        verify(validator).validate(eq(EDC_SECRET_TYPE), isA(JsonObject.class));
        verifyNoInteractions(service, transformerRegistry);
    }

    protected abstract RequestSpecification baseRequest();

    private JsonObjectBuilder createSecretJson() {
        return createObjectBuilder()
                .add(CONTEXT, createContextBuilder().build())
                .add(TYPE, EDC_SECRET_TYPE)
                .add(ID, TEST_SECRET_ID)
                .add(EDC_SECRET_VALUE, TEST_SECRET_VALUE);
    }


    private JsonObjectBuilder createContextBuilder() {
        return createObjectBuilder()
                .add(VOCAB, EDC_NAMESPACE)
                .add(EDC_PREFIX, EDC_NAMESPACE);
    }


    private Secret.Builder createSecretBuilder() {
        return Secret.Builder.newInstance()
                .id(TEST_SECRET_ID)
                .value(TEST_SECRET_VALUE);
    }
}
