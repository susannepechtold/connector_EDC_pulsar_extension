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

package org.eclipse.edc.token.spi;

import org.eclipse.edc.spi.iam.TokenParameters;
import org.jetbrains.annotations.Nullable;

/**
 * This decorator adds the {@code kid} header to a JWT. Does not return any claims.
 *
 * @param keyId The key-id. Can be null, in which case this decorator is a NOOP
 */
public record KeyIdDecorator(@Nullable String keyId) implements TokenDecorator {

    @Override
    public TokenParameters.Builder decorate(TokenParameters.Builder tokenParameters) {
        if (keyId != null) {
            tokenParameters.header("kid", keyId);
        }
        return tokenParameters;
    }
}
