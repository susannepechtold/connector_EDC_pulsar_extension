package org.eclipse.edc.connector.dataplane.pulsar.config;

import java.util.Map;

/**
 * Minimal properties factory that extracts Pulsar-related settings from EDC properties.
 */
public class PulsarPropertiesFactory {

    public static final String PULSAR_SERVICE_URL = "edc.pulsar.serviceUrl";

    public String getServiceUrl(Map<String, String> edcProperties) {
        return edcProperties.getOrDefault(PULSAR_SERVICE_URL, "pulsar://localhost:6650");
    }
}
