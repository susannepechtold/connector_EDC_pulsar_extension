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

package org.eclipse.edc.api.auth.spi.registry;

import org.eclipse.edc.api.auth.spi.AuthenticationService;
import org.jetbrains.annotations.NotNull;

/**
 * Registry for {@link AuthenticationService}.
 */
public interface ApiAuthenticationRegistry {

    /**
     * Register {@link AuthenticationService} for the specified API context.
     *
     * @param context the context.
     * @param service the service.
     */
    void register(String context, AuthenticationService service);

    /**
     * Resolve {@link AuthenticationService} for the specified context. If no service was registered, an "all-pass" will
     * be returned.
     *
     * @param context the context.
     * @return the {@link AuthenticationService}
     */
    @NotNull
    AuthenticationService resolve(String context);

    /**
     * Determines whether a specific authentication service registration exists for a given context.
     *
     * @param context The context name
     * @return {@code true} if a non-default, non-null service is registered, {@code false} otherwise
     */
    boolean hasService(String context);
}
