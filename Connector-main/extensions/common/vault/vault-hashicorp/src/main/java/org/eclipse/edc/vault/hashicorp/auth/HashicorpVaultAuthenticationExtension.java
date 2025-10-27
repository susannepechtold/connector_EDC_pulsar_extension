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

package org.eclipse.edc.vault.hashicorp.auth;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.vault.hashicorp.spi.auth.HashicorpVaultTokenProvider;

import static java.util.Objects.requireNonNull;

@Extension(value = HashicorpVaultAuthenticationExtension.NAME)
public class HashicorpVaultAuthenticationExtension implements ServiceExtension {
    
    public static final String NAME = "Hashicorp Vault Authentication";
    
    @Setting(description = "The token used to access the Hashicorp Vault. Only required, if default token authentication is used.", key = "edc.vault.hashicorp.token", required = false)
    private String token;
    
    @Provider(isDefault = true)
    public HashicorpVaultTokenProvider tokenProvider() {
        requireNonNull(token, "Using default TokenProvider: Configuration of vault token is required (setting: 'edc.vault.hashicorp.token').");
        return new HashicorpVaultTokenProviderImpl(token);
    }
}
