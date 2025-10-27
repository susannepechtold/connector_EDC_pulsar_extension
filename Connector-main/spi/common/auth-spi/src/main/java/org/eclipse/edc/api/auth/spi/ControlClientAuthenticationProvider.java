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

package org.eclipse.edc.api.auth.spi;

import java.util.Map;

/**
 * Provide headers to be used in intra-components (control-api) communication.
 */
@FunctionalInterface
public interface ControlClientAuthenticationProvider {

    /**
     * Return headers needed to authenticate the client
     *
     * @return the headers stored in a {@link Map}.
     */
    Map<String, String> authenticationHeaders();

}
