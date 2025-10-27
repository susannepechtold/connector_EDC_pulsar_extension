package org.eclipse.edc.connector.dataplane.pulsar.pipeline;

import org.apache.pulsar.client.api.PulsarClient;
import org.eclipse.edc.connector.dataplane.pulsar.config.PulsarPropertiesFactory;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSourceFactory;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowStartMessage;
import org.eclipse.edc.validator.dataaddress.kafka.KafkaDataAddressValidator;
import org.eclipse.edc.validator.spi.ValidationResult;
import org.eclipse.edc.validator.spi.Validator;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;

import static org.eclipse.edc.dataaddress.kafka.spi.KafkaDataAddressSchema.KAFKA_TYPE;

/**
 * DataSourceFactory for Pulsar-based sources. For now this re-uses the existing Kafka data-address validator/schema
 * so the module remains compatible with existing EDC data addresses until a Pulsar-specific schema/validator is
 * introduced.
 */
public class PulsarDataSourceFactory implements DataSourceFactory {

    private final Monitor monitor;
    private final Validator<DataAddress> validation;
    private final PulsarPropertiesFactory propertiesFactory;
    private final Clock clock;

    public PulsarDataSourceFactory(Monitor monitor, PulsarPropertiesFactory propertiesFactory, Clock clock) {
        this.monitor = monitor;
        this.propertiesFactory = propertiesFactory;
        this.validation = new KafkaDataAddressValidator(); // reuse existing validator for now
        this.clock = clock;
    }

    @Override
    public String supportedType() {
        // keep compatibility with existing data addresses until a Pulsar data-address SPI exists
        return KAFKA_TYPE;
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowStartMessage request) {
        var source = request.getSourceDataAddress();
        return validation.validate(source).flatMap(ValidationResult::toResult);
    }

    @Override
    public DataSource createSource(DataFlowStartMessage request) {
        var validationResult = validateRequest(request);
        if (validationResult.failed()) {
            throw new EdcException(validationResult.getFailureDetail());
        }

        var source = request.getSourceDataAddress();

        // Build Pulsar client from EDC properties
        try {
            var serviceUrl = propertiesFactory.getServiceUrl(source.getProperties());
            PulsarClient client = PulsarClient.builder().serviceUrl(serviceUrl).build();

            return new PulsarDataSource(client);
        } catch (Exception e) {
            throw new EdcException("Failed to create Pulsar client: " + e.getMessage(), e);
        }
    }
}
