/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.token;

import org.eclipse.edc.jwt.signer.spi.JwsSignerProvider;
import org.eclipse.edc.jwt.validation.jti.JtiValidationStore;
import org.eclipse.edc.keys.spi.PrivateKeyResolver;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.security.token.jwt.DefaultJwsSignerProvider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.token.spi.TokenDecoratorRegistry;
import org.eclipse.edc.token.spi.TokenValidationRulesRegistry;
import org.eclipse.edc.token.spi.TokenValidationService;

import static org.eclipse.edc.token.TokenServicesExtension.NAME;

/**
 * This extension registers the {@link TokenValidationService} and the {@link TokenValidationRulesRegistry}
 * which can then be used by downstream modules.
 */
@Extension(value = NAME, categories = { "token", "security", "auth" })
public class TokenServicesExtension implements ServiceExtension {
    public static final String NAME = "Token Services Extension";

    @Inject
    private PrivateKeyResolver privateKeyResolver;

    @Provider
    public TokenValidationRulesRegistry tokenValidationRulesRegistry() {
        return new TokenValidationRulesRegistryImpl();
    }

    @Provider
    public TokenValidationService validationService() {
        return new TokenValidationServiceImpl();
    }

    @Provider
    public TokenDecoratorRegistry tokenDecoratorRegistry() {
        return new TokenDecoratorRegistryImpl();
    }

    @Provider(isDefault = true)
    public JwsSignerProvider defaultSignerProvider() {
        return new DefaultJwsSignerProvider(privateKeyResolver);
    }

    @Provider(isDefault = true)
    public JtiValidationStore inMemoryJtiValidationStore() {
        return new InMemoryJtiValidationStore();
    }
}
