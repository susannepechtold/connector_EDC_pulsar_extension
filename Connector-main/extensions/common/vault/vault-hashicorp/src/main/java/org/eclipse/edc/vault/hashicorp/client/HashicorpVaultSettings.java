/*
 *  Copyright (c) 2024 Mercedes-Benz Tech Innovation GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Mercedes-Benz Tech Innovation GmbH - Implement automatic Hashicorp Vault token renewal
 *
 */

package org.eclipse.edc.vault.hashicorp.client;

import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;

import static java.util.Objects.requireNonNull;

/**
 * Settings for the {@link HashicorpVaultHealthService}.
 */
@Settings
public class HashicorpVaultSettings {
    public static final String VAULT_API_HEALTH_PATH_DEFAULT = "/v1/sys/health";
    public static final String VAULT_API_SECRET_PATH_DEFAULT = "/v1/secret";
    public static final String VAULT_API_TRANSIT_PATH_DEFAULT = "/v1/transit";
    public static final boolean VAULT_HEALTH_CHECK_STANDBY_OK_DEFAULT = false;
    public static final long VAULT_TOKEN_RENEW_BUFFER_DEFAULT = 30;
    public static final long VAULT_TOKEN_TTL_DEFAULT = 300;
    public static final boolean VAULT_HEALTH_CHECK_ENABLED_DEFAULT = true;
    public static final boolean VAULT_TOKEN_SCHEDULED_RENEW_ENABLED_DEFAULT = true;

    @Setting(description = "The URL of the Hashicorp Vault", key = "edc.vault.hashicorp.url")
    private String url;
    @Setting(description = "Whether or not the vault health check is enabled", defaultValue = VAULT_HEALTH_CHECK_ENABLED_DEFAULT + "", key = "edc.vault.hashicorp.health.check.enabled")
    private boolean healthCheckEnabled;
    @Setting(description = "The URL path of the vault's /health endpoint", defaultValue = VAULT_API_HEALTH_PATH_DEFAULT, key = "edc.vault.hashicorp.api.health.check.path")
    private String healthCheckPath;
    @Setting(description = "Specifies if being a standby should still return the active status code instead of the standby status code", defaultValue = VAULT_HEALTH_CHECK_STANDBY_OK_DEFAULT + "", key = "edc.vault.hashicorp.health.check.standby.ok")
    private boolean healthStandbyOk;
    @Setting(description = "Whether the automatic token renewal process will be triggered or not. Should be disabled only for development and testing purposes",
            defaultValue = VAULT_TOKEN_SCHEDULED_RENEW_ENABLED_DEFAULT + "", key = "edc.vault.hashicorp.token.scheduled-renew-enabled")
    private boolean scheduledTokenRenewEnabled;
    @Setting(description = "The time-to-live (ttl) value of the Hashicorp Vault token in seconds", defaultValue = VAULT_TOKEN_TTL_DEFAULT + "", key = "edc.vault.hashicorp.token.ttl")
    private long ttl;
    @Setting(description = "The renew buffer of the Hashicorp Vault token in seconds", defaultValue = VAULT_TOKEN_RENEW_BUFFER_DEFAULT + "", key = "edc.vault.hashicorp.token.renew-buffer")
    private long renewBuffer;
    @Setting(description = "The URL path of the vault's /secret endpoint", defaultValue = VAULT_API_SECRET_PATH_DEFAULT, key = "edc.vault.hashicorp.api.secret.path")
    private String secretPath;
    @Setting(description = "The path of the folder that the secret is stored in, relative to VAULT_FOLDER_PATH", required = false, key = "edc.vault.hashicorp.folder")

    private String folderPath;

    private HashicorpVaultSettings() {
    }

    public String url() {
        return url;
    }

    public boolean healthCheckEnabled() {
        return healthCheckEnabled;
    }

    public String healthCheckPath() {
        return healthCheckPath;
    }

    public String secretsEnginePath() {
        return VAULT_API_TRANSIT_PATH_DEFAULT;
    }

    public boolean healthStandbyOk() {
        return healthStandbyOk;
    }

    public boolean scheduledTokenRenewEnabled() {
        return scheduledTokenRenewEnabled;
    }

    public long ttl() {
        return ttl;
    }

    public long renewBuffer() {
        return renewBuffer;
    }

    public String secretPath() {
        return secretPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public static class Builder {
        private final HashicorpVaultSettings values;

        private Builder() {
            values = new HashicorpVaultSettings();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder url(String url) {
            requireNonNull(url, "Vault url must not be null");
            values.url = url;
            return this;
        }

        public Builder healthCheckEnabled(boolean healthCheckEnabled) {
            values.healthCheckEnabled = healthCheckEnabled;
            return this;
        }

        public Builder healthCheckPath(String healthCheckPath) {
            values.healthCheckPath = healthCheckPath;
            return this;
        }

        public Builder healthStandbyOk(boolean healthStandbyOk) {
            values.healthStandbyOk = healthStandbyOk;
            return this;
        }

        public Builder scheduledTokenRenewEnabled(boolean scheduledTokenRenewEnabled) {
            values.scheduledTokenRenewEnabled = scheduledTokenRenewEnabled;
            return this;
        }

        public Builder ttl(long ttl) {
            values.ttl = ttl;
            return this;
        }

        public Builder renewBuffer(long renewBuffer) {
            values.renewBuffer = renewBuffer;
            return this;
        }

        public Builder secretPath(String secretPath) {
            values.secretPath = secretPath;
            return this;
        }

        public Builder folderPath(String folderPath) {
            values.folderPath = folderPath;
            return this;
        }

        public HashicorpVaultSettings build() {
            requireNonNull(values.url, "Vault url must be valid");
            requireNonNull(values.healthCheckPath, "Vault health check path must not be null");

            if (values.ttl < 5) {
                throw new IllegalArgumentException("Vault token ttl minimum value is 5");
            }

            if (values.renewBuffer >= values.ttl) {
                throw new IllegalArgumentException("Vault token renew buffer value must be less than ttl value");
            }

            return values;
        }
    }
}
