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

package org.eclipse.edc.iam.verifiablecredentials.spi.model.revocation.statuslist2021;

import org.assertj.core.api.ThrowableAssert;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.CredentialSubject;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.Issuer;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.revocation.bitstringstatuslist.BitstringStatusListCredential;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.revocation.bitstringstatuslist.BitstringStatusListStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.edc.iam.verifiablecredentials.spi.model.revocation.statuslist2021.StatusList2021Credential.STATUS_LIST_ENCODED_LIST;
import static org.eclipse.edc.iam.verifiablecredentials.spi.model.revocation.statuslist2021.StatusList2021Status.STATUS_LIST_PURPOSE;

public class StatusList2021CredentialTest {

    @Test
    void parseStatusList2021() {
        // values taken from https://www.w3.org/TR/2023/WD-vc-status-list-20230427/#statuslist2021credential
        //noinspection unchecked
        var statusListCred = StatusList2021Credential.Builder.newInstance()
                .id("https://example.com/credentials/status/3")
                .types(List.of("VerifiableCredential", "StatusList2021Credential"))
                .issuer(new Issuer("did:example:12345", Map.of()))
                .issuanceDate(Instant.parse("2021-04-05T14:27:40Z"))
                .credentialSubject(CredentialSubject.Builder.newInstance()
                        .id("https://example.com/status/3#list")
                        .claim("type", "StatusList2021")
                        .claim(STATUS_LIST_PURPOSE, "revocation")
                        .claim(STATUS_LIST_ENCODED_LIST, "H4sIAAAAAAAAA-3BMQEAAADCoPVPbQwfoAAAAAAAAAAAAAAAAAAAAIC3AYbSVKsAQAAA")
                        .build())
                .build();


        assertThat(statusListCred.encodedList()).isNotNull();
        assertThat(statusListCred.statusPurpose()).isEqualTo("revocation");
    }

    @Test
    void parse_noPurpose_expectException() {
        ThrowableAssert.ThrowingCallable action = () -> StatusList2021Credential.Builder.newInstance()
                .id("https://example.com/credentials/status/3")
                .types(List.of("VerifiableCredential", "StatusList2021Credential"))
                .issuer(new Issuer("did:example:12345", Map.of()))
                .issuanceDate(Instant.parse("2021-04-05T14:27:40Z"))
                .credentialSubject(CredentialSubject.Builder.newInstance()
                        .id("https://example.com/status/3#list")
                        .claim("type", "BitstringStatusList")
                        // .claim(STATUS_LIST_PURPOSE, "revocation")
                        .claim(BitstringStatusListCredential.STATUS_LIST_ENCODED_LIST, "uH4sIAAAAAAAAA-3BMQEAAADCoPVPbQwfoAAAAAAAAAAAAAAAAAAAAIC3AYbSVKsAQAAA")
                        .build())
                .build();

        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parse_noEncodedList_expectException() {
        ThrowableAssert.ThrowingCallable action = () -> StatusList2021Credential.Builder.newInstance()
                .id("https://example.com/credentials/status/3")
                .types(List.of("VerifiableCredential", "StatusList2021Credential"))
                .issuer(new Issuer("did:example:12345", Map.of()))
                .issuanceDate(Instant.parse("2021-04-05T14:27:40Z"))
                .credentialSubject(CredentialSubject.Builder.newInstance()
                        .id("https://example.com/status/3#list")
                        .claim("type", "BitstringStatusList")
                        .claim(BitstringStatusListStatus.STATUS_LIST_PURPOSE, "revocation")
                        //.claim(STATUS_LIST_ENCODED_LIST, "uH4sIAAAAAAAAA-3BMQEAAADCoPVPbQwfoAAAAAAAAAAAAAAAAAAAAIC3AYbSVKsAQAAA")
                        .build())
                .build();

        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

}
