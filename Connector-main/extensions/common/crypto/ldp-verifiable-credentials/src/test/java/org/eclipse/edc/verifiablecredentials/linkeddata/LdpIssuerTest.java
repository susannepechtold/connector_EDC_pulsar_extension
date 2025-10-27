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

package org.eclipse.edc.verifiablecredentials.linkeddata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import jakarta.json.JsonArray;
import jakarta.json.JsonValue;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.jsonld.spi.JsonLdKeywords;
import org.eclipse.edc.security.signature.jws2020.JsonWebKeyPair;
import org.eclipse.edc.security.signature.jws2020.Jws2020ProofDraft;
import org.eclipse.edc.security.signature.jws2020.Jws2020SignatureSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.jsonld.util.JacksonJsonLd.createObjectMapper;
import static org.eclipse.edc.security.signature.jws2020.TestFunctions.createKeyPair;
import static org.eclipse.edc.security.signature.jws2020.TestFunctions.readResourceAsJson;
import static org.mockito.Mockito.mock;

class LdpIssuerTest {
    private final ObjectMapper mapper = createObjectMapper();

    @Nested
    class JsonWebSignature2020 {
        private final Jws2020SignatureSuite jws2020suite = new Jws2020SignatureSuite(mapper);
        private LdpIssuer issuer;

        @BeforeEach
        void setup() throws URISyntaxException {
            var jsonLd = new TitaniumJsonLd(mock());
            var ccl = Thread.currentThread().getContextClassLoader();
            jsonLd.registerCachedDocument("https://www.w3.org/ns/odrl.jsonld", ccl.getResource("odrl.jsonld").toURI());
            jsonLd.registerCachedDocument("https://www.w3.org/ns/did/v1", ccl.getResource("jws2020.json").toURI());
            jsonLd.registerCachedDocument("https://w3id.org/security/suites/jws-2020/v1", ccl.getResource("jws2020.json").toURI());
            jsonLd.registerCachedDocument("https://www.w3.org/2018/credentials/v1", ccl.getResource("credentials.v1.json").toURI());
            jsonLd.registerCachedDocument("https://www.w3.org/2018/credentials/examples/v1", ccl.getResource("examples.v1.json").toURI());
            issuer = LdpIssuer.Builder.newInstance()
                    .jsonLd(jsonLd)
                    .monitor(mock())
                    .build();
        }

        @DisplayName("t0001: a simple credential to sign (EC Key)")
        @Test
        void signSimpleCredential_ecKey() throws JOSEException {
            var vc = readResourceAsJson("jws2020/issuing/0001_vc.json");
            var keypair = createKeyPair(new ECKeyGenerator(Curve.P_384).keyID("test-kid").generate());

            var verificationMethodUrl = "https://org.eclipse.edc/verification-method";

            var proofOptions = Jws2020ProofDraft.Builder.newInstance()
                    .mapper(mapper)
                    .created(Instant.parse("2022-12-31T23:00:00Z"))
                    .verificationMethod(new JsonWebKeyPair(URI.create(verificationMethodUrl), null, null, null))
                    .proofPurpose(URI.create("https://w3id.org/security#assertionMethod"))
                    .build();


            var result = issuer.signDocument(jws2020suite, vc, keypair, proofOptions);
            assertThat(result.succeeded()).withFailMessage(result::getFailureDetail).isTrue();
            var verificationMethod = result.getContent().getJsonArray("https://w3id.org/security#proof").get(0).asJsonObject().getJsonArray("@graph").get(0).asJsonObject().get("https://w3id.org/security#verificationMethod");

            assertThat(verificationMethod.getValueType()).describedAs("Expected a String!").isEqualTo(JsonValue.ValueType.ARRAY);
            assertThat(verificationMethod.asJsonArray().get(0).asJsonObject().toString()).contains(verificationMethodUrl);
        }

        @DisplayName("t0001: a simple credential to sign (RSA Key)")
        @ParameterizedTest(name = "keySize = {0} bits")
        @ValueSource(ints = { 2048, 3072, 4096 })
        void signSimpleCredential_rsaKey(int keysize) throws NoSuchAlgorithmException {
            var vc = readResourceAsJson("jws2020/issuing/0001_vc.json");

            var gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(keysize);
            var keyPair = gen.generateKeyPair();

            var jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(UUID.randomUUID().toString())
                    .issueTime(new Date())
                    .build();
            var keypair = createKeyPair(jwk);

            var verificationMethodUrl = "https://org.eclipse.edc/verification-method";

            var proofOptions = Jws2020ProofDraft.Builder.newInstance()
                    .mapper(mapper)
                    .created(Instant.parse("2022-12-31T23:00:00Z"))
                    .verificationMethod(new JsonWebKeyPair(URI.create(verificationMethodUrl), null, null, null))
                    .proofPurpose(URI.create("https://w3id.org/security#assertionMethod"))
                    .build();

            var result = issuer.signDocument(jws2020suite, vc, keypair, proofOptions);
            assertThat(result.succeeded()).withFailMessage(result::getFailureDetail).isTrue();
            var verificationMethod = result.getContent().getJsonArray("https://w3id.org/security#proof").get(0).asJsonObject().getJsonArray("@graph").get(0).asJsonObject().get("https://w3id.org/security#verificationMethod");

            assertThat(verificationMethod.getValueType()).describedAs("Expected a String!").isEqualTo(JsonValue.ValueType.ARRAY);
            assertThat(verificationMethod.asJsonArray().get(0).asJsonObject().toString()).contains(verificationMethodUrl);
        }

        @DisplayName("t0001: a simple credential to sign (OctetKeyPair)")
        @Test
        void signSimpleCredential_octetKeyPair() throws JOSEException {
            var vc = readResourceAsJson("jws2020/issuing/0001_vc.json");

            var jwk = new OctetKeyPairGenerator(Curve.Ed25519).generate();
            var keypair = createKeyPair(jwk);

            var verificationMethodUrl = "https://org.eclipse.edc/verification-method";

            var proofOptions = Jws2020ProofDraft.Builder.newInstance()
                    .mapper(mapper)
                    .created(Instant.parse("2022-12-31T23:00:00Z"))
                    .verificationMethod(new JsonWebKeyPair(URI.create(verificationMethodUrl), null, null, null))
                    .proofPurpose(URI.create("https://w3id.org/security#assertionMethod"))
                    .build();


            var result = issuer.signDocument(jws2020suite, vc, keypair, proofOptions);
            assertThat(result.succeeded()).withFailMessage(result::getFailureDetail).isTrue();
            var verificationMethod = result.getContent().getJsonArray("https://w3id.org/security#proof").get(0).asJsonObject().getJsonArray("@graph").get(0).asJsonObject().get("https://w3id.org/security#verificationMethod");

            assertThat(verificationMethod.getValueType()).describedAs("Expected a String!").isEqualTo(JsonValue.ValueType.ARRAY);
            assertThat(verificationMethod.asJsonArray().get(0).asJsonObject().toString()).contains(verificationMethodUrl);
        }

        @DisplayName("t0003: signed embedded verificationMethod")
        @Test
        void signEmbeddedVerificationMethod() throws JOSEException {
            var vc = readResourceAsJson("jws2020/issuing/0001_vc.json");
            var keypair = createKeyPair(new ECKeyGenerator(Curve.P_384).keyID("test-kid").generate());

            var proofOptions = Jws2020ProofDraft.Builder.newInstance()
                    .mapper(mapper)
                    .created(Instant.parse("2022-12-31T23:00:00Z"))
                    .verificationMethod(keypair)
                    .proofPurpose(URI.create("https://w3id.org/security#assertionMethod"))
                    .build();


            var result = issuer.signDocument(jws2020suite, vc, keypair, proofOptions);
            assertThat(result.succeeded()).withFailMessage(result::getFailureDetail).isTrue();
            var verificationMethod = result.getContent().getJsonArray("https://w3id.org/security#proof").get(0).asJsonObject().getJsonArray("@graph").get(0).asJsonObject().get("https://w3id.org/security#verificationMethod");

            assertThat(verificationMethod).withFailMessage("Expected an JsonArray!").isInstanceOf(JsonArray.class);
            assertThat(verificationMethod.asJsonArray().get(0).asJsonObject().get("https://w3id.org/security#publicKeyJwk"))
                    .isInstanceOf(JsonArray.class)
                    .satisfies(jv -> {
                        var jwk = ((JsonArray) jv).get(0).asJsonObject().get(JsonLdKeywords.VALUE);
                        assertThat(jwk.asJsonObject().get("x")).isNotNull();
                        assertThat(jwk.asJsonObject().get("crv")).isNotNull();
                        assertThat(jwk.asJsonObject().get("kty")).isNotNull();
                    });
        }

        @DisplayName("t0004: a credential with DID key as verification method")
        @Test
        void signVerificationDidKey() throws ParseException {
            var vc = readResourceAsJson("jws2020/issuing/0001_vc.json");
            var eckey = (ECKey) JWK.parse("""
                    {
                        "kty": "EC",
                        "d": "UEUJVbKZC3vR-y65gXx8NZVnE0QD5xe6qOk4eiObj-qVOg5zqt9zc0d6fdu4mUuu",
                        "use": "sig",
                        "crv": "P-384",
                        "x": "l6IS348kIFEANYl3CWueMYVXcZmK0eMI0vejkF1GHbl77dOZuZwi9L2IQmuA27ux",
                        "y": "m-8s5FM8Tn00OKVFxE-wfCs3J2keE2EBAYYZgAmfI1LCRD9iU2LBced-EBK18Da9",
                        "alg": "ES384"
                    }
                    """);
            var keypair = createKeyPair(eckey);

            // check https://w3c-ccg.github.io/did-method-key/#create for details
            var didKey = "did:key:zC2zU1wUHhYYX4CDwNwky9f5jtSvp5aQy5aNRQMHEdpK5xkJMy6TcMbWBP3scHbR6hhidR3RRjfAA7cuLxjydXgEiZUzRzguozYFeR3G6SzjAwswJ6hXKBWhFEHm2L6Rd6GRAw8r3kyPovxvcabdMF2gBy5TAioY1mVYFeT6";

            var proofOptions = Jws2020ProofDraft.Builder.newInstance()
                    .mapper(mapper)
                    .created(Instant.parse("2022-12-31T23:00:00Z"))
                    .verificationMethod(new JsonWebKeyPair(URI.create(didKey), null, null, null))
                    .proofPurpose(URI.create("https://w3id.org/security#assertionMethod"))
                    .build();


            var result = issuer.signDocument(jws2020suite, vc, keypair, proofOptions);
            assertThat(result.succeeded()).withFailMessage(result::getFailureDetail).isTrue();
            var verificationMethod = result.getContent().getJsonArray("https://w3id.org/security#proof").get(0).asJsonObject().getJsonArray("@graph").get(0).asJsonObject().get("https://w3id.org/security#verificationMethod");

            assertThat(verificationMethod.getValueType()).describedAs("Expected a String!").isEqualTo(JsonValue.ValueType.ARRAY);
            assertThat(verificationMethod.asJsonArray().get(0).asJsonObject().toString()).contains(didKey);


        }

        @DisplayName("t0005: compacted signed presentation")
        @Test
        void signCompactedPresentation() throws JOSEException {
            var vp = readResourceAsJson("jws2020/issuing/0005_vp_compacted_signed.json");

            var keypair = createKeyPair(new ECKeyGenerator(Curve.P_384).keyID("test-kid").generate());

            var verificationMethodUrl = "https://org.eclipse.edc/verification-method";

            var proofOptions = Jws2020ProofDraft.Builder.newInstance()
                    .mapper(mapper)
                    .created(Instant.parse("2022-12-31T23:00:00Z"))
                    .verificationMethod(new JsonWebKeyPair(URI.create(verificationMethodUrl), null, null, null))
                    .proofPurpose(URI.create("https://w3id.org/security#assertionMethod"))
                    .build();


            var result = issuer.signDocument(jws2020suite, vp, keypair, proofOptions);
            assertThat(result.succeeded()).withFailMessage(result::getFailureDetail).isTrue();
            var verificationMethod = result.getContent().getJsonArray("https://w3id.org/security#proof").get(0).asJsonObject().getJsonArray("@graph").get(0).asJsonObject().get("https://w3id.org/security#verificationMethod");

            assertThat(verificationMethod.getValueType()).describedAs("Expected a String!").isEqualTo(JsonValue.ValueType.ARRAY);
            assertThat(verificationMethod.asJsonArray().get(0).asJsonObject().toString()).contains(verificationMethodUrl);
        }
    }
}