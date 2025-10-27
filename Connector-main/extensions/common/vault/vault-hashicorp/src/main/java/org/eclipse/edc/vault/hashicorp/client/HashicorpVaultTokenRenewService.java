/*
 *  Copyright (c) 2025 Cofinity-X
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Cofinity-X - initial API and implementation
 *
 */

package org.eclipse.edc.vault.hashicorp.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.http.spi.FallbackFactory;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.vault.hashicorp.spi.auth.HashicorpVaultTokenProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for enabling the renewal of vault tokens. Facilitates checking if a token
 * is renewable and renewing the token with the provided TTL.
 */
public class HashicorpVaultTokenRenewService {
    
    private static final String VAULT_TOKEN_HEADER = "X-Vault-Token";
    private static final String VAULT_REQUEST_HEADER = "X-Vault-Request";
    private static final MediaType MEDIA_TYPE_APPLICATION_JSON = MediaType.get("application/json");
    
    private static final String TOKEN_LOOK_UP_SELF_PATH = "v1/auth/token/lookup-self";
    private static final String TOKEN_RENEW_SELF_PATH = "v1/auth/token/renew-self";
    private static final List<FallbackFactory> FALLBACK_FACTORIES = List.of(new HashicorpVaultClientFallbackFactory());
    private static final String DATA_KEY = "data";
    private static final String RENEWABLE_KEY = "renewable";
    private static final String AUTH_KEY = "auth";
    private static final String LEASE_DURATION_KEY = "lease_duration";
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };
    private static final String INCREMENT_SECONDS_FORMAT = "%ds";
    private static final String INCREMENT_KEY = "increment";
    
    private final EdcHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HashicorpVaultSettings settings;
    private final HashicorpVaultTokenProvider tokenProvider;
    private final Monitor monitor;
    
    /**
     * Constructor for the HashicorpVaultTokenRenewService.
     *
     * @param httpClient    the HTTP client
     * @param objectMapper  the object mapper
     * @param settings      the configuration for interacting with HashiCorp Vault
     * @param tokenProvider the token provider for retrieving the vault authentication token
     * @param monitor       the monitor
     */
    public HashicorpVaultTokenRenewService(@NotNull EdcHttpClient httpClient,
                                           @NotNull ObjectMapper objectMapper,
                                           @NotNull HashicorpVaultSettings settings,
                                           @NotNull HashicorpVaultTokenProvider tokenProvider,
                                           @NotNull Monitor monitor) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.settings = settings;
        this.tokenProvider = tokenProvider;
        this.monitor = monitor;
    }
    
    /**
     * Attempts to look up the current Vault token and returns a boolean indicating if the token
     * is renewable.
     * <p>
     * Will retry in some error cases.
     *
     * @return boolean indicating if the token is renewable
     */
    public Result<Boolean> isTokenRenewable() {
        var uri = HttpUrl.parse(settings.url())
                .newBuilder()
                .addPathSegments(TOKEN_LOOK_UP_SELF_PATH)
                .build();
        var request = httpGet(uri);
        
        try (var response = httpClient.execute(request, FALLBACK_FACTORIES)) {
            if (response.isSuccessful()) {
                var responseBody = response.body();
                if (responseBody == null) {
                    return Result.failure("Token look up returned empty body");
                }
                var payload = objectMapper.readValue(responseBody.string(), MAP_TYPE_REFERENCE);
                var parseRenewableResult = parseRenewable(payload);
                if (parseRenewableResult.failed()) {
                    return Result.failure("Token look up response could not be parsed: %s".formatted(parseRenewableResult.getFailureDetail()));
                }
                var isRenewable = parseRenewableResult.getContent();
                return Result.success(isRenewable);
            } else {
                return Result.failure("Token look up failed with status %d".formatted(response.code()));
            }
        } catch (IOException e) {
            return Result.failure("Failed to look up token with reason: %s".formatted(e.getMessage()));
        }
    }
    
    /**
     * Attempts to renew the Vault token with the configured ttl. Note that Vault will not honor the passed
     * ttl (or increment) for periodic tokens. Therefore, the ttl returned by this operation should always be used
     * for further calculations.
     * <p>
     * Will retry in some error cases.
     *
     * @return long representing the remaining ttl of the token in seconds
     */
    public Result<Long> renewToken() {
        var uri = HttpUrl.parse(settings.url())
                .newBuilder()
                .addPathSegments(TOKEN_RENEW_SELF_PATH)
                .build();
        var requestPayload = getTokenRenewRequestPayload();
        var request = httpPost(uri, requestPayload);
        
        try (var response = httpClient.execute(request, FALLBACK_FACTORIES)) {
            if (response.isSuccessful()) {
                var responseBody = response.body();
                if (responseBody == null) {
                    return Result.failure("Token renew returned empty body");
                }
                var payload = objectMapper.readValue(responseBody.string(), MAP_TYPE_REFERENCE);
                var parseTtlResult = parseTtl(payload);
                if (parseTtlResult.failed()) {
                    return Result.failure("Token renew response could not be parsed: %s".formatted(parseTtlResult.getFailureDetail()));
                }
                var ttl = parseTtlResult.getContent();
                return Result.success(ttl);
            } else {
                return Result.failure("Token renew failed with status: %d".formatted(response.code()));
            }
        } catch (IOException e) {
            return Result.failure("Failed to renew token with reason: %s".formatted(e.getMessage()));
        }
    }
    
    @NotNull
    private Request httpGet(HttpUrl requestUri) {
        return new Request.Builder()
                .url(requestUri)
                .headers(getHeaders())
                .get()
                .build();
    }
    
    @NotNull
    private Request httpPost(HttpUrl requestUri, Object requestBody) {
        return new Request.Builder()
                .url(requestUri)
                .headers(getHeaders())
                .post(createRequestBody(requestBody))
                .build();
    }
    
    private Headers getHeaders() {
        var headersBuilder = new Headers.Builder().add(VAULT_REQUEST_HEADER, Boolean.toString(true));
        headersBuilder.add(VAULT_TOKEN_HEADER, tokenProvider.vaultToken());
        return headersBuilder.build();
    }
    
    private RequestBody createRequestBody(Object requestPayload) {
        String jsonRepresentation;
        try {
            jsonRepresentation = objectMapper.writeValueAsString(requestPayload);
        } catch (JsonProcessingException e) {
            throw new EdcException(e);
        }
        return RequestBody.create(jsonRepresentation, MEDIA_TYPE_APPLICATION_JSON);
    }
    
    private Map<String, String> getTokenRenewRequestPayload() {
        return Map.of(INCREMENT_KEY, INCREMENT_SECONDS_FORMAT.formatted(settings.ttl()));
    }
    
    private Result<Boolean> parseRenewable(Map<String, Object> map) {
        try {
            var data = objectMapper.convertValue(getValueFromMap(map, DATA_KEY), new TypeReference<Map<String, Object>>() {
            });
            var isRenewable = objectMapper.convertValue(getValueFromMap(data, RENEWABLE_KEY), Boolean.class);
            return Result.success(isRenewable);
        } catch (IllegalArgumentException e) {
            var errMsgFormat = "Failed to parse renewable flag from token look up response %s with reason: %s";
            monitor.warning(errMsgFormat.formatted(map, e.getMessage()), e);
            return Result.failure(errMsgFormat.formatted(map, e.getMessage()));
        }
    }
    
    private Result<Long> parseTtl(Map<String, Object> map) {
        try {
            var auth = objectMapper.convertValue(getValueFromMap(map, AUTH_KEY), new TypeReference<Map<String, Object>>() {
            });
            var ttl = objectMapper.convertValue(getValueFromMap(auth, LEASE_DURATION_KEY), Long.class);
            return Result.success(ttl);
        } catch (IllegalArgumentException e) {
            var errMsgFormat = "Failed to parse ttl from token renewal response %s with reason: %s";
            monitor.warning(errMsgFormat.formatted(map, e.getMessage()), e);
            return Result.failure(errMsgFormat.formatted(map, e.getMessage()));
        }
    }
    
    private Object getValueFromMap(Map<String, Object> map, String key) {
        var value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Key '%s' does not exist".formatted(key));
        }
        return value;
    }
}
