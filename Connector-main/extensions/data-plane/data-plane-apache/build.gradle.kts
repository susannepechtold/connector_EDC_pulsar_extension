plugins {
    `java-library`
}

dependencies {
    // TODO: replace kafka SPI with a Pulsar equivalent when available
    api(project(":spi:common:data-address:data-address-kafka-spi"))
    api(project(":spi:data-plane:data-plane-spi"))

    implementation(project(":core:common:lib:util-lib"))
    implementation(project(":core:common:lib:validator-lib"))
    implementation(project(":core:data-plane:data-plane-util"))
    // TODO: switch validator and client dependency to Pulsar equivalents
    implementation(project(":extensions:common:validator:validator-data-address-kafka"))
    // Placeholder: keep kafka client dependency for now to allow incremental refactor
    // Pulsar client added for migration. Remove kafkaClients once migration is complete.
    implementation("org.apache.pulsar:pulsar-client:2.11.1")
    // Keep kafka client temporarily to allow existing tests to run until migration completes
    implementation(libs.kafkaClients)

    testImplementation(project(":core:common:junit"))
    testImplementation(libs.restAssured)
    testImplementation(libs.awaitility)
}
