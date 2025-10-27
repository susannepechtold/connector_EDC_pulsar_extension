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

package org.eclipse.edc.iam.identitytrust.transform.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonObject;
import org.eclipse.edc.iam.identitytrust.transform.TestContextProvider;
import org.eclipse.edc.iam.identitytrust.transform.from.JsonObjectFromPresentationResponseMessageTransformer;
import org.eclipse.edc.iam.identitytrust.transform.to.JsonObjectToPresentationResponseMessageTransformer;
import org.eclipse.edc.jsonld.util.JacksonJsonLd;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.TransformerContextImpl;
import org.eclipse.edc.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PresentationResponseMessageSerdeTest {


    private final ObjectMapper mapper = JacksonJsonLd.createObjectMapper();
    private final TypeManager typeManager = mock();
    private final TypeTransformerRegistry trr = new TypeTransformerRegistryImpl();
    private final TransformerContext context = new TransformerContextImpl(trr);


    @BeforeEach
    void setUp() {
        when(typeManager.getMapper("test")).thenReturn(mapper);
    }


    @ParameterizedTest
    @ArgumentsSource(TestContextProvider.class)
    void serde(TestContextProvider.TestContext ctx) throws JsonProcessingException {
        var fromTransformer = new JsonObjectFromPresentationResponseMessageTransformer(ctx.namespace());
        var toTransformer = new JsonObjectToPresentationResponseMessageTransformer(typeManager, "test", ctx.namespace());

        var obj = """
                {
                         "@context": [
                             "%s"
                         ],
                         "@type": "PresentationResponseMessage",
                         "presentation": [
                             {
                                 "@context": [
                                     "https://www.w3.org/2018/credentials/v1"
                                 ],
                                 "type": [
                                     "VerifiablePresentation"
                                 ]
                             },
                             "jwtPresentation"
                         ]
                     }
                """
                .formatted(ctx.context());

        var json = mapper.readValue(obj, JsonObject.class);
        var jo = ctx.jsonLd().expand(json);

        var query = toTransformer.transform(jo.getContent(), context);
        assertThat(query).isNotNull();

        var expandedJson = fromTransformer.transform(query, context);

        var compacted = ctx.jsonLd().compact(expandedJson).getContent();

        assertThat(json.getJsonArray("@context")).isEqualTo(compacted.getJsonArray("@context"));
        assertThat(json.getJsonArray("presentation")).isEqualTo(compacted.getJsonArray("presentation"));
        assertThat(json.getJsonString("@type")).isEqualTo(compacted.getJsonString("type"));
    }
}
