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

package org.eclipse.edc.connector.controlplane.callback;


import org.eclipse.edc.connector.controlplane.services.spi.callback.CallbackProtocolResolver;
import org.eclipse.edc.connector.controlplane.services.spi.callback.CallbackProtocolResolverRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CallbackProtocolResolverRegistryImpl implements CallbackProtocolResolverRegistry {

    private final List<CallbackProtocolResolver> resolvers = new ArrayList<>();

    @Override
    public void registerResolver(CallbackProtocolResolver resolver) {
        resolvers.add(resolver);
    }

    @Override
    public String resolve(String scheme) {
        return resolvers.stream()
                .map(resolver -> resolver.resolve(scheme))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
