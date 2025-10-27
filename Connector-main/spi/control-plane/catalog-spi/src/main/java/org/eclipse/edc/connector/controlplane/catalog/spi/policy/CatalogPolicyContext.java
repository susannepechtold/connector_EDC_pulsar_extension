/*
 *  Copyright (c) 2024 Cofinity-X
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

package org.eclipse.edc.connector.controlplane.catalog.spi.policy;

import org.eclipse.edc.participant.spi.ParticipantAgent;
import org.eclipse.edc.participant.spi.ParticipantAgentPolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyContextImpl;
import org.eclipse.edc.policy.engine.spi.PolicyScope;

/**
 * Policy Context for "catalog" scope
 */
public class CatalogPolicyContext extends PolicyContextImpl implements ParticipantAgentPolicyContext {

    @PolicyScope
    public static final String CATALOG_SCOPE = "catalog";

    private final ParticipantAgent agent;

    public CatalogPolicyContext(ParticipantAgent agent) {
        this.agent = agent;
    }

    @Override
    public ParticipantAgent participantAgent() {
        return agent;
    }

    @Override
    public String scope() {
        return CATALOG_SCOPE;
    }
}
