/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - Initial implementation
 *
 */

package org.eclipse.edc.keys.spi;

import org.eclipse.edc.runtime.metamodel.annotation.ExtensionPoint;
import org.eclipse.edc.spi.result.Result;

import java.security.PrivateKey;

/**
 * Resolves security keys by type.
 */
@FunctionalInterface
@ExtensionPoint
public interface PrivateKeyResolver {

    /**
     * Resolves a {@link PrivateKey} identified by its ID.
     *
     * @param id The ID under which the PrivateKey is held in secure storage.
     * @return The {@link PrivateKey}, or a failure, if no key material was found for this ID, or the referenced material did not contain a private key.
     */
    Result<PrivateKey> resolvePrivateKey(String id);
}
