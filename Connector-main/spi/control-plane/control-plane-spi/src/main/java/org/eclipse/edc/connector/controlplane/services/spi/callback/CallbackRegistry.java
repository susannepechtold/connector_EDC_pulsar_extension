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

package org.eclipse.edc.connector.controlplane.services.spi.callback;

import org.eclipse.edc.runtime.metamodel.annotation.ExtensionPoint;
import org.eclipse.edc.spi.types.domain.callback.CallbackAddress;

import java.util.List;

/**
 * Registry for static callbacks
 */
@ExtensionPoint
public interface CallbackRegistry {

    /**
     * Register a callback
     *
     * @param address The {@link CallbackAddress}
     */
    void register(CallbackAddress address);

    /**
     * Returns all the callbacks associated with the input event
     *
     * @param event The event
     * @return The list of callbacks
     */
    List<CallbackAddress> resolve(String event);
}
