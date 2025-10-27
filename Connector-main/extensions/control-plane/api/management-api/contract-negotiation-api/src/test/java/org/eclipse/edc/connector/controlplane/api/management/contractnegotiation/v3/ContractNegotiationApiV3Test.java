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

package org.eclipse.edc.connector.controlplane.api.management.contractnegotiation.v3;

import org.eclipse.edc.api.transformer.JsonObjectToCallbackAddressTransformer;
import org.eclipse.edc.connector.controlplane.api.management.contractnegotiation.BaseContractNegotiationApiTest;
import org.eclipse.edc.connector.controlplane.api.management.contractnegotiation.transform.JsonObjectToContractOfferTransformer;
import org.eclipse.edc.connector.controlplane.api.management.contractnegotiation.transform.JsonObjectToContractRequestTransformer;
import org.eclipse.edc.connector.controlplane.api.management.contractnegotiation.transform.JsonObjectToTerminateNegotiationCommandTransformer;
import org.eclipse.edc.connector.controlplane.transform.odrl.OdrlTransformersFactory;
import org.eclipse.edc.participant.spi.ParticipantIdMapper;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContractNegotiationApiV3Test extends BaseContractNegotiationApiTest {

    @BeforeEach
    void setUp() {
        transformer.register(new JsonObjectToContractRequestTransformer());
        transformer.register(new JsonObjectToContractOfferTransformer());
        //missing: registering the deprecated transformer
        transformer.register(new JsonObjectToCallbackAddressTransformer());
        transformer.register(new JsonObjectToTerminateNegotiationCommandTransformer());
        ParticipantIdMapper participantIdMapper = mock();
        when(participantIdMapper.fromIri(any())).thenAnswer(a -> a.getArgument(0));
        OdrlTransformersFactory.jsonObjectToOdrlTransformers(participantIdMapper).forEach(transformer::register);
    }

}
