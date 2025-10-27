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

plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":spi:common:identity-trust-spi"))
    api(project(":spi:common:json-ld-spi"))
    api(project(":spi:common:transform-spi"))
    api(project(":spi:common:transform-spi"))
    api(libs.nimbus.jwt)

    testImplementation(project(":core:common:lib:json-ld-lib"))
    testImplementation(project(":extensions:common:json-ld"))
    testImplementation(project(":core:common:lib:transform-lib")) //for the TransformerContextImpl
    testImplementation(project(":core:common:junit")) //for the TestUtils
    testImplementation(testFixtures(project(":spi:common:identity-trust-spi")))
}

