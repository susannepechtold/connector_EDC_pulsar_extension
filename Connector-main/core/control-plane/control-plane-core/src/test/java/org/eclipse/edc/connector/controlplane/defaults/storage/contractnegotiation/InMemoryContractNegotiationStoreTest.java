/*
 *  Copyright (c) 2020 - 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.connector.controlplane.defaults.storage.contractnegotiation;


import org.eclipse.edc.connector.controlplane.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.controlplane.contract.spi.testfixtures.negotiation.store.ContractNegotiationStoreTestBase;
import org.eclipse.edc.query.CriterionOperatorRegistryImpl;

import java.time.Duration;

class InMemoryContractNegotiationStoreTest extends ContractNegotiationStoreTestBase {

    private final InMemoryContractNegotiationStore store = new InMemoryContractNegotiationStore(CONNECTOR_NAME, clock, CriterionOperatorRegistryImpl.ofDefaults());

    @Override
    protected ContractNegotiationStore getContractNegotiationStore() {
        return store;
    }

    @Override
    protected void leaseEntity(String negotiationId, String owner, Duration duration) {
        store.acquireLease(negotiationId, owner, duration);
    }

    @Override
    protected boolean isLeasedBy(String negotiationId, String owner) {
        return store.isLeasedBy(negotiationId, owner);
    }

}
